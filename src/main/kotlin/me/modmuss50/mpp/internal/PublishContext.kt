package me.modmuss50.mpp.internal

import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.workers.WorkQueue
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KClass

@ApiStatus.Internal
class PublishContext(
    private val queue: WorkQueue,
    private val result: RegularFile,
) {
    fun <T : IPublishWorkParameters> submit(
        workActionClass: KClass<out IPublishWorkAction<T>>,
        parameterAction: Action<in T>,
    ) {
        queue.submit(workActionClass.java) {
            it.result.set(result)
            parameterAction.execute(it)
        }
    }
}
