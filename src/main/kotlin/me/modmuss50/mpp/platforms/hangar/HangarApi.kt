package me.modmuss50.mpp.platforms.hangar

import kotlinx.serialization.Serializable
import me.modmuss50.mpp.networking.HttpApi.get
import me.modmuss50.mpp.networking.HttpApi.post
import me.modmuss50.mpp.networking.MultipartBodyBuilder
import me.modmuss50.mpp.networking.RequestContext
import me.modmuss50.mpp.platforms.hangar.dependency.HangarDependency
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
        val channel: String,
    )

    @Serializable
    data class PluginDependency(
        val name: String,
        val type: HangarDependency.DependencyType,
        val externalUrl: String? = null,
    )

    @Serializable
    data class FileData(
        val platforms: List<String>,
        val externalUrl: String? = null,
    )

    @Serializable
    data class VersionResponse(
        val url: String,
    )

    @Serializable
    data class PlatformVersionResponse(
        val version: String,
        val subVersions: List<String>,
    )

    private val headers: Map<String, String>
        get() = mapOf("Authorization" to "Bearer $apiKey")

    fun publishVersion(
        projectSlug: String,
        version: String,
        channel: String,
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

    fun getPlatformVersions(platform: HangarPlatformType): List<PlatformVersionResponse> =
        httpContext.get(
            url = "$apiEndpoint/platforms/${platform.name}/versions",
            headers = headers,
        )

    fun encodeSlug(slug: String): String =
        slug.split("/")
            .joinToString("/") { URLEncoder.encode(it, Charsets.UTF_8) }
}
