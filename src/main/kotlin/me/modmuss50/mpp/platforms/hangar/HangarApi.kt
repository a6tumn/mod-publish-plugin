package me.modmuss50.mpp.platforms.hangar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.modmuss50.mpp.networking.HttpApi.get
import me.modmuss50.mpp.networking.HttpApi.post
import me.modmuss50.mpp.networking.MultipartBodyBuilder
import me.modmuss50.mpp.networking.RequestContext
import me.modmuss50.mpp.platforms.hangar.enums.HangarChannel
import me.modmuss50.mpp.platforms.hangar.platform.HangarPlatform
import java.net.URLEncoder

/*
* https://hangar.papermc.io/api-docs#overview
* https://docs.rs/hangar-api/latest/hangar_api/index.html
*/
class HangarApi(
    private val apiKey: String,
    private val apiEndpoint: String = "https://hangar.papermc.io/api/v1"
) {
    companion object {
        val httpContext = RequestContext(
            json = RequestContext.Default.json,
            userAgent = RequestContext.Default.userAgent,
            client = RequestContext.Default.client,
            exceptionFactory = RequestContext.Default.exceptionFactory
        )
    }

    @Serializable
    data class VersionResponse(
        val name: String,
        val channel: String,
        val description: String? = null
    )

    @Serializable
    data class ProjectResponse(
        val name: String,
        val namespace: String
    )

    @Serializable
    enum class ChannelType {
        @SerialName("Release")
        RELEASE,

        @SerialName("Snapshot")
        SNAPSHOT,
        ;

        companion object {
            fun valueOf(type: HangarChannel): ChannelType =
                when (type) {
                    HangarChannel.RELEASE -> RELEASE
                    HangarChannel.SNAPSHOT -> SNAPSHOT
                }
        }
    }

    private val headers: Map<String, String>
        get() = mapOf("Authorization" to "Bearer $apiKey")

    fun publishVersion(
        projectSlug: String,
        version: String,
        channel: ChannelType,
        changelog: String,
        platforms: List<HangarPlatform>
    ): VersionResponse {
        val url = "$apiEndpoint/projects/${encodeSlug(projectSlug)}/versions"

        val builder = MultipartBodyBuilder()
            .addFormDataPart("version", version)
            .addFormDataPart("channel", channel.name)
            .addFormDataPart("description", changelog)

        platforms.forEachIndexed { index, platform ->
            builder
                .addFormDataPart("platforms[$index].platform", platform.platform.name)
                .addFormDataPart("platforms[$index].platformVersions", platform.versions.joinToString(","))
                .addFormDataPart("platforms[$index].file", platform.file.name, platform.file)
        }

        val bodyPublisher = builder.build()
        val headersWithContentType = headers + ("Content-Type" to builder.getContentType())

        return httpContext.post(
            url,
            bodyPublisher,
            headersWithContentType
        )
    }

    fun getProject(
        projectSlug: String
    ): ProjectResponse {
        val url = "$apiEndpoint/projects/${encodeSlug(projectSlug)}"

        return httpContext.get(
            url,
            headers
        )
    }

    private fun encodeSlug(slug: String): String =
        slug.split("/")
            .joinToString("/") { URLEncoder.encode(it, Charsets.UTF_8) }
}