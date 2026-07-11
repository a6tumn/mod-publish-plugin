package me.modmuss50.mpp.platforms.gitea.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.modmuss50.mpp.network.HttpApi.get
import me.modmuss50.mpp.network.HttpApi.patch
import me.modmuss50.mpp.network.HttpApi.post
import me.modmuss50.mpp.network.HttpException
import me.modmuss50.mpp.network.MultipartBodyBuilder
import me.modmuss50.mpp.network.RequestContext
import java.io.File
import java.net.http.HttpRequest

class GiteaCompatibleApi(
    private val accessToken: String,
    private val baseUrl: String,
    private val repository: String,
) {
    companion object {
        val httpContext = RequestContext(
            json = RequestContext.Default.json,
            userAgent = RequestContext.Default.userAgent,
            client = RequestContext.Default.client,
            exceptionFactory = HttpException.jsonErrorFactory<ErrorResponse>(
                json = RequestContext.Default.json,
                messageExtractor = { it.message },
            ),
        )
    }

    @Serializable
    // https://docs.gitea.com/api/1.24/#tag/repository/operation/repoGetRelease
    data class Release(
        val id: Long,
        @SerialName("html_url")
        val htmlUrl: String,
        @SerialName("upload_url")
        val uploadUrl: String,
    )

    // Some of the below are nullable, but we don't need their nullability here.
    // https://docs.gitea.com/api/1.24/#tag/repository/operation/repoCreateRelease
    @Serializable
    data class CreateRelease(
        val body: String? = null,
        val draft: Boolean,
        val name: String? = null,
        val prerelease: Boolean,
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("target_commitish")
        val targetCommitish: String,
    )

    // Error responses are consistent between hooks.
    @Serializable
    data class ErrorResponse(
        val message: String,
        val url: String,
    )

    private val headers: Map<String, String>
        get() =
            mapOf(
                "Authorization" to "token $accessToken",
                "Content-Type" to "application/json",
            )

    // https://docs.gitea.com/api/1.24/#tag/repository/operation/repoGetRelease
    fun getRelease(id: Long): Release = httpContext.get(
        url = "$baseUrl/repos/$repository/releases/$id",
        headers = headers,
    )

    // https://docs.gitea.com/api/1.24/#tag/repository/operation/repoCreateRelease
    fun createRelease(metadata: CreateRelease): Release {
        val body = HttpRequest.BodyPublishers.ofString(Json.encodeToString(metadata))

        return httpContext.post(
            url = "$baseUrl/repos/$repository/releases",
            body = body,
            headers = headers,
        )
    }

    // https://docs.gitea.com/api/1.24/#tag/repository/operation/repoCreateReleaseAttachment
    fun uploadAsset(
        release: Release,
        file: File,
    ) {
        val bodyBuilder =
            MultipartBodyBuilder()
                .addFormDataPart("attachment", file.name, file, "application/java-archive")

        val multipartHeaders = headers.toMutableMap()
        multipartHeaders["Content-Type"] = bodyBuilder.getContentType()

        return httpContext.post(
            url = release.uploadUrl,
            body = bodyBuilder.build(),
            headers = multipartHeaders,
        )
    }

    // https://docs.gitea.com/api/1.24/#tag/repository/operation/repoEditRelease
    fun publishRelease(release: Release) {
        val body =
            HttpRequest.BodyPublishers.ofString(
                """
                {
                "draft": false
                }
                """.trimIndent(),
            )

        return httpContext.patch(
            url = "$baseUrl/repos/$repository/releases/${release.id}",
            body = body,
            headers = headers,
        )
    }
}
