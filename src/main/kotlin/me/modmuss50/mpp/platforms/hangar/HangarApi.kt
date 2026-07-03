package me.modmuss50.mpp.platforms.hangar

import me.modmuss50.mpp.networking.RequestContext
import kotlinx.serialization.Serializable
import me.modmuss50.mpp.networking.HttpApi.get
import me.modmuss50.mpp.networking.HttpApi.post
import me.modmuss50.mpp.networking.MultipartBodyBuilder
import java.io.File
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
    data class Version(
        val name: String,
        val channel: String,
        val description: String? = null,
    )

    @Serializable
    data class Project(
        val name: String,
        val namespace: String,
    )

    private val headers: Map<String, String>
        get() = mapOf("Authorization" to "Bearer $apiKey")

    fun publishVersion(
        projectSlug: String,
        version: String,
        channel: String,
        changelog: String,
        platform: String,
        platformVersions: List<String>,
        file: File,
    ): Version {
        val encodedSlug = URLEncoder.encode(projectSlug, Charsets.UTF_8)
        val url = "$apiEndpoint/projects/$encodedSlug/versions"
        val builder = MultipartBodyBuilder()
            .addFormDataPart("version", version)
            .addFormDataPart("channel", channel)
            .addFormDataPart("description", changelog)
            .addFormDataPart("platform", platform)
            .addFormDataPart("platformVersions", platformVersions.joinToString(","))
            .addFormDataPart("file", file.name, file)
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
    ): Project {
        val encodedSlug = URLEncoder.encode(projectSlug, Charsets.UTF_8)
        val url = "$apiEndpoint/projects/$encodedSlug"

        return httpContext.get(
            url,
            headers
        )
    }
}