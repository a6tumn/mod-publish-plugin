package me.modmuss50.mpp.platforms.github

import me.modmuss50.mpp.internal.IPublishWorkAction
import me.modmuss50.mpp.internal.IPublishWorkParameters
import me.modmuss50.mpp.internal.Platform
import me.modmuss50.mpp.internal.PublishContext
import me.modmuss50.mpp.platforms.PublishResult
import me.modmuss50.mpp.util.ReleaseType
import org.gradle.api.logging.Logger
import javax.inject.Inject
import kotlin.random.Random

abstract class Github @Inject constructor(
    name: String,
) : Platform(name), IGithubOptions {
    override fun publish(context: PublishContext) {
        val files = additionalFiles.files.toMutableList()

        if (file.isPresent) {
            files.add(file.get().asFile)
        }

        if (files.isEmpty() && !allowEmptyFiles.get()) {
            throw IllegalStateException("No files to upload to GitHub.")
        }

        context.submit(UploadWorkAction::class) {
            it.from(this)
        }
    }

    override fun dryRunPublishResult(): PublishResult {
        return PublishResult.Github(
            repository = repository.get(),
            releaseId = 0,
            url = "https://github.com/modmuss50/mod-publish-plugin/dry-run?random=${Random.nextInt(0, 1000000)}",
            title = announcementTitle.getOrElse("Download from GitHub"),
        )
    }

    override fun printDryRunInfo(logger: Logger) {
    }

    interface IUploadParams :
        IPublishWorkParameters,
        IGithubOptions

    abstract class UploadWorkAction : IPublishWorkAction<IUploadParams> {
        override fun publish(): PublishResult {
            with(parameters) {
                val api = GithubApi(
                    accessToken = accessToken.get(),
                    apiEndpoint = apiEndpoint.orNull ?: "https://api.github.com",
                )

                val repo = api.getRepository(repository.get())
                val (release, created) = getOrCreateRelease(api, repo)

                val files = additionalFiles.files.toMutableList()

                if (file.isPresent) {
                    files.add(file.get().asFile)
                }

                val noneUnique = files.groupingBy { it.name }.eachCount().filter { it.value > 1 }
                if (noneUnique.isNotEmpty()) {
                    val noneUniqueNames = noneUnique.keys.joinToString(", ")
                    throw IllegalStateException("Github file names must be unique within a release, found duplicates: $noneUniqueNames")
                }

                for (file in files) {
                    api.uploadAsset(release, file)
                }

                if (created) {
                    // Publish the release after all assets are uploaded.
                    api.updateRelease(repo.fullName, release.id, GithubApi.UpdateReleaseRequest(draft = false))
                }

                return PublishResult.Github(
                    repository = repository.get(),
                    releaseId = release.id,
                    url = release.htmlUrl,
                    title = announcementTitle.getOrElse("Download from GitHub"),
                )
            }
        }

        data class ReleaseResult(val release: GithubApi.Release, val created: Boolean)

        private fun getOrCreateRelease(api: GithubApi, repo: GithubApi.Repository): ReleaseResult {
            with(parameters) {
                if (releaseResult.isPresent) {
                    val result = PublishResult.fromJson(releaseResult.get().asFile.readText()) as PublishResult.Github
                    return ReleaseResult(api.getRelease(repo.fullName, result.releaseId), false)
                }

                val release = api.createRelease(
                    repo.fullName,
                    GithubApi.CreateReleaseRequest(
                        tagName = tagName.get(),
                        targetCommitish = commitish.get(),
                        name = displayName.get(),
                        body = changelog.get(),
                        draft = true, // Create a draft to allow uploading assets before publishing.
                        prerelease = type.get() != ReleaseType.STABLE,
                    ),
                )
                return ReleaseResult(release, true)
            }
        }
    }
}
