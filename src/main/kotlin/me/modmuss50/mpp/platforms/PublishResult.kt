package me.modmuss50.mpp.platforms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.ApiStatus
import java.lang.IllegalStateException

@ApiStatus.Internal
@Serializable
sealed class PublishResult {
    abstract val type: String
    abstract val link: String
    abstract val title: String
    abstract val brandColor: Int

    companion object {
        fun fromJson(@Language("json") string: String): PublishResult {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString(string)
        }
    }

    @Serializable
    @SerialName("curseforge")
    data class CurseForge(
        val projectId: String,
        val projectSlug: String?,
        val fileId: Int,
        override val title: String,
    ) : PublishResult() {
        override val type: String
            get() = "curseforge"
        override val link: String
            get() {
                if (projectSlug == null) {
                    // Thanks CF...
                    throw IllegalStateException("The CurseForge projectSlug property must be set to generate a link to the uploaded file")
                }

                return "https://curseforge.com/minecraft/mc-mods/$projectSlug/files/$fileId"
            }
        override val brandColor: Int
            get() = 0xF16436
    }

    @Serializable
    @SerialName("github")
    data class Github(
        val repository: String,
        val releaseId: Long,
        val url: String,
        override val title: String,
    ) : PublishResult() {
        override val type: String
            get() = "github"
        override val link: String
            get() = url
        override val brandColor: Int
            get() = 0xF6F0FC
    }

    @Serializable
    @SerialName("modrinth")
    data class Modrinth(
        val id: String,
        val projectId: String,
        override val title: String,
    ) : PublishResult() {
        override val type: String
            get() = "modrinth"
        override val link: String
            get() = "https://modrinth.com/mod/$projectId/version/$id"
        override val brandColor: Int
            get() = 0x1BD96A
    }

    @Serializable
    @SerialName("gitea")
    data class GiteaCompatible(
        val repository: String,
        val releaseId: Long,
        val url: String,
        override val title: String,
        override val brandColor: Int,
    ) : PublishResult() {
        override val type: String
            get() = "gitea"
        override val link: String
            get() = url
    }

    @Serializable
    @SerialName("gitlab")
    data class Gitlab(
        val projectId: Long,
        val tagName: String,
        val url: String,
        override val title: String,
    ) : PublishResult() {
        override val type: String
            get() = "gitlab"

        override val link: String
            get() = url

        override val brandColor: Int
            get() = 0xFC6D26
    }
}
