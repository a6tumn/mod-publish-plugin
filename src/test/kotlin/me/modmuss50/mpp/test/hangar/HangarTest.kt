package me.modmuss50.mpp.test.hangar

import me.modmuss50.mpp.test.IntegrationTest
import me.modmuss50.mpp.test.MockWebServer
import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HangarTest : IntegrationTest {
    lateinit var server: MockWebServer<MockHangarApi>

    @BeforeTest
    fun construct() {
        server = MockWebServer(MockHangarApi())
    }

    @AfterTest
    fun teardown() {
        server.close()
    }

    @Test
    fun uploadHangar() {
        val result = gradleTest()
            .buildScript(
                """
                publishMods {
                    file = tasks.jar.flatMap { it.archiveFile }
                    changelog = "Hello!"
                    version = "1.0.0"
                    type = STABLE

                    hangar {
                        accessToken = "123"
                        id = "mock-project"
                        projectType = PAPER
                        platformVersions.add("1.20.1")
                        apiEndpoint = "${server.endpoint}"
                    }
                }
            """.trimIndent(),
            )
            .run("publishHangar")

        assertEquals(TaskOutcome.SUCCESS, result.task(":publishHangar")!!.outcome)
    }

    @Test
    fun uploadHangarExistingVersion() {
        val result = gradleTest()
            .buildScript(
                """
                publishMods {
                    file = tasks.jar.flatMap { it.archiveFile }
                    changelog = "Hello!"
                    version = "1.0.0"
                    type = STABLE

                    hangar {
                        accessToken = "123"
                        id = "mock-project"
                        projectType = PAPER
                        platformVersions.add("1.20.1")
                        apiEndpoint = "${server.endpoint}"
                    }

                    hangar("hangarOther") {
                        accessToken = "123"
                        apiEndpoint = "${server.endpoint}"
                        parent(tasks.named("publishHangar"))
                    }
                }
            """.trimIndent(),
            )
            .run("publishHangarOther")

        assertEquals(TaskOutcome.SUCCESS, result.task(":publishHangarOther")!!.outcome)
    }

    @Test
    fun requireFile() {
        val result = gradleTest()
            .buildScript(
                """
                publishMods {
                    changelog = "Hello!"
                    version = "1.0.0"
                    type = STABLE

                    hangar {
                        accessToken = "123"
                        id = "mock-project"
                        projectType = PAPER
                        platformVersions.add("1.20.1")
                        apiEndpoint = "${server.endpoint}"
                    }
                }
            """.trimIndent(),
            )
            .run("publishHangar")

        assertEquals(TaskOutcome.FAILED, result.task(":publishHangar")!!.outcome)
    }
}