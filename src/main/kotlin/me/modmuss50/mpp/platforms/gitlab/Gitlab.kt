package me.modmuss50.mpp.platforms.gitlab

import me.modmuss50.mpp.internal.IPublishWorkAction
import me.modmuss50.mpp.internal.IPublishWorkParameters
import me.modmuss50.mpp.internal.Platform
import me.modmuss50.mpp.internal.PublishContext
import me.modmuss50.mpp.platforms.PublishResult
import me.modmuss50.mpp.network.HttpException
import org.gradle.api.logging.Logger
import javax.inject.Inject
import kotlin.random.Random

abstract class Gitlab @Inject constructor(
    name: String,
) : Platform(name), IGitlabOptions {
    override fun publish(context: PublishContext) {
        val files = additionalFiles.files.toMutableList()
        if (file.isPresent) files.add(file.get().asFile)

        if (files.isEmpty() && !allowEmptyFiles.get()) {
            throw IllegalStateException("No files to upload to GitLab.")
        }

        context.submit(UploadWorkAction::class) {
            it.from(this)
        }
    }

    override fun dryRunPublishResult(): PublishResult =
        PublishResult.Gitlab(
            projectId = projectId.get(),
            tagName = tagName.get(),
            url = "https://gitlab.com/dry-run?random=${Random.nextInt(0, 1000000)}",
            title = announcementTitle.getOrElse("Download from GitLab"),
        )

    override fun printDryRunInfo(logger: Logger) {}

    interface IUploadParams :
        IPublishWorkParameters,
        IGitlabOptions

    abstract class UploadWorkAction : IPublishWorkAction<IUploadParams> {
        override fun publish(): PublishResult {
            with(parameters) {
                val api =
                    GitlabApi(
                        accessToken = accessToken.get(),
                        apiEndpoint = apiEndpoint.orNull ?: "https://gitlab.com/api/v4",
                    )

                val releaseResult = getOrCreateRelease(api)

                val files = additionalFiles.files.toMutableList()
                if (file.isPresent) {
                    files.add(file.get().asFile)
                }

                if (files.isEmpty() && !allowEmptyFiles.get()) {
                    throw IllegalStateException("No files to upload to GitLab.")
                }

                val duplicates =
                    files
                        .groupingBy { it.name }
                        .eachCount()
                        .filter { it.value > 1 }

                if (duplicates.isNotEmpty()) {
                    val duplicateNames = duplicates.keys.joinToString(", ")
                    throw IllegalStateException(
                        "GitLab file names must be unique within a release, found duplicates: $duplicateNames",
                    )
                }

                val uploadedAssets =
                    files.map { file ->
                        api.uploadAsset(projectId.get(), file)
                    }

                val baseRelease =
                    if (releaseResult.created) {
                        releaseResult.release
                    } else {
                        api.getRelease(projectId.get(), tagName.get())
                    }

                val finalDescription =
                    buildString {
                        append(baseRelease.description)

                        if (!baseRelease.description.endsWith("\n")) {
                            append("\n")
                        }

                        uploadedAssets.forEach { asset ->
                            append("[${asset.name}](${asset.url})\n")
                        }
                    }

                api.updateRelease(
                    projectId.get(),
                    tagName.get(),
                    GitlabApi.UpdateReleaseRequest(
                        description = finalDescription,
                    ),
                )

                return PublishResult.Gitlab(
                    projectId = projectId.get(),
                    tagName = tagName.get(),
                    url = "https://gitlab.com/projects/${projectId.get()}/releases/${tagName.get()}",
                    title = announcementTitle.getOrElse("Download from GitLab"),
                )
            }
        }

        data class ReleaseResult(
            val release: GitlabApi.Release,
            val created: Boolean,
        )

        private fun getOrCreateRelease(api: GitlabApi): ReleaseResult {
            with(parameters) {
                if (releaseResult.isPresent) {
                    val result =
                        PublishResult.fromJson(
                            releaseResult.get().asFile.readText(),
                        ) as PublishResult.Gitlab

                    return ReleaseResult(
                        api.getRelease(projectId.get(), result.tagName),
                        false,
                    )
                }

                // This is a little more complicated than GitHub due to how GitLab treats tags as unique per release
                return try {
                    val release =
                        api.createRelease(
                            projectId.get(),
                            GitlabApi.CreateReleaseRequest(
                                name = displayName.get(),
                                tagName = tagName.get(),
                                description = changelog.get(),
                                ref = commitish.get(),
                            ),
                        )
                    ReleaseResult(release, true)
                } catch (e: HttpException) {
                    if (e.statusCode == 409) {
                        val existing = api.getRelease(projectId.get(), tagName.get())
                        ReleaseResult(existing, false)
                    } else {
                        throw e
                    }
                }
            }
        }
    }
}
