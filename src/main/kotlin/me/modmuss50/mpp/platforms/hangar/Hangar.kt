package me.modmuss50.mpp.platforms.hangar

import me.modmuss50.mpp.HangarPublishResult
import me.modmuss50.mpp.Platform
import me.modmuss50.mpp.PublishContext
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.PublishWorkAction
import me.modmuss50.mpp.PublishWorkParameters
import org.gradle.api.logging.Logger
import javax.inject.Inject
import kotlin.random.Random

abstract class Hangar @Inject constructor(
    name: String,
) : Platform(name), HangarOptions {
    override fun publish(context: PublishContext) {
        context.submit(UploadWorkAction::class) {
            it.from(this)
        }
    }

    override fun dryRunPublishResult(): PublishResult {
        val projectSlug = "dry-run/example-project-${Random.nextInt(0, 1000000)}"
        val version = "1.0.0"

        return HangarPublishResult(
            projectSlug = projectSlug,
            version = version,
            channel = channel.get(),
            url = "https://hangar.papermc.io/$projectSlug/versions/$version",
            title = announcementTitle.getOrElse("Download from Hangar"),
        )
    }

    override fun printDryRunInfo(logger: Logger) {}

    interface UploadParams : PublishWorkParameters, HangarOptions

    abstract class UploadWorkAction : PublishWorkAction<UploadParams> {
        override fun publish(): PublishResult {
            with(parameters) {
                val api = HangarApi(
                    apiKey = accessToken.get(),
                    apiEndpoint = apiEndpoint.get(),
                )

                val mainFile = file.orNull?.asFile
                val declared = files.getOrElse(emptyList())

                val externalEntries = declared
                    .filter { it.externalUrl.isPresent }
                    .map {
                        HangarApi.FileUpload(
                            externalUrl = it.externalUrl.get(),
                            platforms = it.platforms.get(),
                        )
                    }

                // Merge platforms declared for a file
                val declaredPlatforms = declared
                    .filterNot { it.externalUrl.isPresent }
                    .flatMap { it.platforms.get() }
                    .distinct()

                check(mainFile != null || declaredPlatforms.isEmpty()) {
                    "file {} was configured but no file was set"
                }

                // Fall back to the platforms named in platformDependencies
                val mainPlatforms = declaredPlatforms.ifEmpty {
                    platformDependencies.getOrElse(emptyMap()).keys
                        .map { HangarApi.HangarPlatformType.of(it) }
                }

                check(mainFile == null || mainPlatforms.isNotEmpty()) {
                    "The file has no platforms. Add a file {} block or set platformDependencies"
                }

                val entries = externalEntries + listOfNotNull(
                    mainFile?.let { HangarApi.FileUpload(externalUrl = null, platforms = mainPlatforms) },
                )

                check(entries.isNotEmpty() || allowEmptyFiles.get()) {
                    "No files were provided and allowEmptyFiles is false"
                }

                val pluginDeps = pluginDependencies.getOrElse(emptyMap())
                    .values
                    .flatten()
                    .map {
                        HangarApi.DependencyUpload(
                            externalUrl = it.externalUrl.orNull,
                            name = it.name.orNull,
                            platform = HangarApi.HangarPlatformType.of(it.platform.get()),
                            projectId = it.projectId.orNull,
                            required = it.required.get(),
                        )
                    }
                    .groupBy { it.platform.name }

                val upload = HangarApi.VersionUpload(
                    channel = channel.get(),
                    description = description.orNull,
                    files = entries.ifEmpty { null },
                    platformDependencies = platformDependencies.orNull,
                    pluginDependencies = pluginDeps.ifEmpty { null },
                    version = version.get(),
                )

                val projectSlug = id.get()
                val response = api.publishVersion(projectSlug, upload, listOfNotNull(mainFile))

                return HangarPublishResult(
                    projectSlug = projectSlug,
                    version = version.get(),
                    channel = channel.get(),
                    url = response.url,
                    title = announcementTitle.getOrElse("Download from Hangar"),
                )
            }
        }
    }
}
