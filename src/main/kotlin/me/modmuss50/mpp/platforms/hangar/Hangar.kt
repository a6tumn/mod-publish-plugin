package me.modmuss50.mpp.platforms.hangar

import me.modmuss50.mpp.HangarPublishResult
import me.modmuss50.mpp.Platform
import me.modmuss50.mpp.PublishContext
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.PublishWorkAction
import me.modmuss50.mpp.PublishWorkParameters
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.hangar.platform.HangarPlatform
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

    override fun dryRunPublishResult(): PublishResult =
        HangarPublishResult(
            projectSlug = "dry-run/example-project-${Random.nextInt(0, 1000000)}",
            version = "1.0.0-Stable",
            channel = HangarApi.ChannelType.valueOf(ReleaseType.STABLE).name,
            title = announcementTitle.getOrElse("Download from Hangar"),
        )

    override fun printDryRunInfo(logger: Logger) {}

    interface UploadParams :
        PublishWorkParameters,
        HangarOptions

    abstract class UploadWorkAction : PublishWorkAction<UploadParams> {
        override fun publish(): PublishResult {
            with(parameters) {
                val resolvedPlatforms = platforms.get().map {
                    HangarPlatform(
                        platform = it.platform,
                        versions = it.versions,
                        file = it.file.asFile.get(),
                    )
                }

                val api = HangarApi(
                    apiKey = accessToken.get(),
                    apiEndpoint = apiEndpoint.orNull ?: "https://hangar.papermc.io/api/v1",
                )

                val response = api.publishVersion(
                    projectSlug = project.get(),
                    version = version.get(),
                    channel = channel.get(),
                    changelog = changelog.get(),
                    platforms = resolvedPlatforms,
                )

                return HangarPublishResult(
                    projectSlug = project.get(),
                    version = response.name,
                    channel = response.channel,
                    title = announcementTitle.getOrElse("Download from Hangar"),
                )
            }
        }
    }
}
