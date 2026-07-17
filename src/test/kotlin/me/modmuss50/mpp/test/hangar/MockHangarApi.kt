package me.modmuss50.mpp.test.hangar

import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.EndpointGroup
import io.javalin.http.Context
import me.modmuss50.mpp.test.MockWebServer

class MockHangarApi : MockWebServer.MockApi {
    override fun routes(): EndpointGroup =
        EndpointGroup {
            path("projects") {
                path("{projectSlug}") {
                    post("upload", this::uploadVersion)
                }
            }
        }

    private fun uploadVersion(
        context: Context,
    ) {
        val projectSlug = context.pathParam("projectSlug")

        context.result(
            """
            {
              "url": "http://localhost:${context.port()}/projects/$projectSlug/versions/mock-version"
            }
            """.trimIndent(),
        )
    }
}
