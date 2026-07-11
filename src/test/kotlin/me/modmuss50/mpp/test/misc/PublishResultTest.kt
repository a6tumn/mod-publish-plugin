package me.modmuss50.mpp.test.misc

import me.modmuss50.mpp.platforms.PublishResult
import kotlin.test.Test
import kotlin.test.assertEquals

class PublishResultTest {
    @Test
    fun decodeGithubJson() {
        val result = PublishResult.fromJson(
            """
            {
                "type": "github",
                "repository": "test/test",
                "releaseId": 123,
                "url": "https://github.com",
                "title": "test"
            }
            """.trimIndent(),
        )

        val github = result as PublishResult.Github
        assertEquals("test/test", github.repository)
        assertEquals(123, github.releaseId)
        assertEquals("https://github.com", github.url)
        assertEquals("test", github.title)
    }

    @Test
    fun decodeCurseforgeJson() {
        val result = PublishResult.fromJson(
            """
            {
                "type": "curseforge",
                "projectId": "abc",
                "fileId": 123,
                "projectSlug": "example",
                "title": "test"
            }
            """.trimIndent(),
        )

        val curseforge = result as PublishResult.CurseForge
        assertEquals("abc", curseforge.projectId)
        assertEquals(123, curseforge.fileId)
        assertEquals("test", curseforge.title)
    }

    @Test
    fun decodeModrinthJson() {
        val result = PublishResult.fromJson(
            """
            {
                "type": "modrinth",
                "id": "test",
                "projectId": "123",
                "title": "test"
            }
            """.trimIndent(),
        )

        val modrinth = result as PublishResult.Modrinth
        assertEquals("test", modrinth.id)
        assertEquals("123", modrinth.projectId)
        assertEquals("test", modrinth.title)
    }
}
