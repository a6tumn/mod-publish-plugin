package me.modmuss50.mpp.platforms.hangar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.networking.HttpApi.post
import me.modmuss50.mpp.networking.MultipartBodyBuilder
import me.modmuss50.mpp.networking.RequestContext
import java.io.File
import java.net.URLEncoder

/*
* https://github.com/HangarMC/hangar-publish-plugin/blob/master/plugin/src/main/java/io/papermc/hangarpublishplugin/internal/HangarVersion.java
*/
class HangarApi(
    private val apiKey: String,
    private val apiEndpoint: String = "https://hangar.papermc.io/api/v1/",
) {
    companion object {
        val httpContext = RequestContext(
            json = RequestContext.Default.json,
            userAgent = RequestContext.Default.userAgent,
            client = RequestContext.Default.client,
            exceptionFactory = RequestContext.Default.exceptionFactory,
        )
    }

    @Serializable
    data class VersionUpload(
        val version: String,
        val pluginDependencies: Map<String, List<PluginDependency>> = emptyMap(),
        val platformDependencies: Map<String, List<String>>,
        val description: String? = null,
        val files: List<FileData>,
        val channel: ChannelType,
    )

    @Serializable
    data class PluginDependency(
        val name: String,
        val required: Boolean = true,
        val externalUrl: String? = null,
    )

    @Serializable
    data class FileData(
        val platforms: List<String>,
        val externalUrl: String? = null,
    )

    @Serializable
    enum class ChannelType {
        @SerialName("Release")
        RELEASE,

        @SerialName("Snapshot")
        SNAPSHOT,

        @SerialName("Alpha")
        ALPHA,
        ;

        companion object {
            fun valueOf(type: ReleaseType): ChannelType =
                when (type) {
                    ReleaseType.STABLE -> RELEASE
                    ReleaseType.BETA -> SNAPSHOT
                    ReleaseType.ALPHA -> ALPHA
                }
        }
    }

    @Serializable
    data class VersionResponse(
        val url: String,
    )

    private val headers: Map<String, String>
        get() = mapOf("Authorization" to "Bearer $apiKey")

    fun publishVersion(
        projectSlug: String,
        version: String,
        channel: ChannelType,
        changelog: String,
        platform: HangarPlatformType,
        platformVersions: List<String>,
        file: File,
        pluginDependencies: List<PluginDependency> = emptyList(),
    ): VersionResponse {
        val url = "$apiEndpoint/projects/${encodeSlug(projectSlug)}/upload"

        val upload = VersionUpload(
            version = version,
            channel = channel,
            description = changelog,
            platformDependencies = mapOf(
                platform.name to platformVersions,
            ),
            pluginDependencies = mapOf(
                platform.name to pluginDependencies,
            ),
            files = listOf(
                FileData(
                    platforms = listOf(platform.name),
                ),
            ),
        )

        val builder = MultipartBodyBuilder()
            .addFormDataPart("versionUpload", httpContext.json.encodeToString(upload))
            .addFormDataPart("files", file.name, file)

        return httpContext.post(
            url = url,
            body = builder.build(),
            headers = headers + ("Content-Type" to builder.getContentType()),
        )
    }

    private fun encodeSlug(slug: String): String =
        slug.split("/")
            .joinToString("/") { URLEncoder.encode(it, Charsets.UTF_8) }
}
