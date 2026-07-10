package me.modmuss50.mpp.platforms.hangar

import me.modmuss50.mpp.HangarPublishResult
import me.modmuss50.mpp.Platform
import me.modmuss50.mpp.PublishContext
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.PublishWorkAction
import me.modmuss50.mpp.PublishWorkParameters
import me.modmuss50.mpp.ReleaseType
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
            channel = HangarApi.ChannelType.valueOf(ReleaseType.STABLE).name,
            url = "https://hangar.papermc.io/$projectSlug/versions/$version",
            title = announcementTitle.getOrElse("Download from Hangar"),
        )
    }

    override fun printDryRunInfo(logger: Logger) {}

    interface UploadParams :
        PublishWorkParameters,
        HangarOptions

    abstract class UploadWorkAction : PublishWorkAction<UploadParams> {
        override fun publish(): PublishResult {
            with(parameters) {
                val api = HangarApi(
                    apiKey = accessToken.get(),
                    apiEndpoint = apiEndpoint.orNull
                        ?: "https://hangar.papermc.io/api/v1/",
                )

                val channel = HangarApi.ChannelType.valueOf(type.get())

                val response = api.publishVersion(
                    projectSlug = id.get(),
                    version = version.get(),
                    channel = channel,
                    changelog = changelog.get(),
                    platform = projectType.get(),
                    platformVersions = platformVersions.get(),
                    file = file.get().asFile,
                    pluginDependencies = emptyList(), // [NYI]
                )

                return HangarPublishResult(
                    projectSlug = id.get(),
                    version = version.get(),
                    channel = channel.name,
                    url = response.url,
                    title = announcementTitle.getOrElse("Download from Hangar"),
                )
            }
        }
    }
}
