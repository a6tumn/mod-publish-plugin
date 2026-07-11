package me.modmuss50.mpp.internal

import kotlinx.serialization.json.Json
import me.modmuss50.mpp.platforms.PublishResult
import org.gradle.workers.WorkAction
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
interface IPublishWorkAction<T : IPublishWorkParameters> : WorkAction<T> {
    fun publish(): PublishResult

    override fun execute() {
        val result = publish()

        parameters.result.get().asFile.writeText(
            Json.encodeToString(result),
        )
    }
}