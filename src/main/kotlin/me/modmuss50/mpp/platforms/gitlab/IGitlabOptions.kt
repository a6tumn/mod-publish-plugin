package me.modmuss50.mpp.platforms.gitlab

import me.modmuss50.mpp.internal.IPlatformOptions
import me.modmuss50.mpp.internal.IPlatformOptionsInternal
import me.modmuss50.mpp.internal.PublishModTask
import me.modmuss50.mpp.internal.IPublishOptions
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.annotations.ApiStatus.Internal

interface IGitlabOptions : IPlatformOptions, IPlatformOptionsInternal<IGitlabOptions> {
    @get:InputFile
    @get:Optional
    override val file: RegularFileProperty

    /**
     * GitLab uses project IDs as opposed to repository links.
     */
    @get:Input
    val projectId: Property<Long>

    @get:Input
    val tagName: Property<String>

    @get:Input
    val commitish: Property<String>

    @get:Input
    @get:Optional
    val apiEndpoint: Property<String>

    @get:Input
    val allowEmptyFiles: Property<Boolean>

    @get:InputFile
    @get:Optional
    @get:Internal
    val releaseResult: RegularFileProperty

    override fun setInternalDefaults() {
        tagName.convention(version)
        allowEmptyFiles.convention(false)
    }

    fun from(other: IGitlabOptions) {
        super.from(other)
        projectId.convention(other.projectId)
        tagName.convention(other.tagName)
        commitish.convention(other.commitish)
        apiEndpoint.convention(other.apiEndpoint)
        allowEmptyFiles.convention(other.allowEmptyFiles)
        releaseResult.convention(other.releaseResult)
    }

    fun from(other: Provider<IGitlabOptions>) {
        from(other.get())
    }

    fun from(
        other: Provider<IGitlabOptions>,
        publishOptions: Provider<IPublishOptions>,
    ) {
        from(other)
        from(publishOptions.get())
    }

    /**
     * Publish to an existing release, created by another task.
     */
    fun parent(task: TaskProvider<Task>) {
        val publishTask = task.map { it as PublishModTask }
        releaseResult.set(publishTask.flatMap { it.result })

        val options = publishTask.map { it.platform as IGitlabOptions }
        version.set(options.flatMap { it.version })
        version.finalizeValue()
        changelog.set(options.flatMap { it.changelog })
        changelog.finalizeValue()
        type.set(options.flatMap { it.type })
        type.finalizeValue()
        displayName.set(options.flatMap { it.displayName })
        displayName.finalizeValue()
        projectId.set(options.flatMap { it.projectId })
        projectId.finalizeValue()
        commitish.set(options.flatMap { it.commitish })
        commitish.finalizeValue()
        tagName.set(options.flatMap { it.tagName })
        tagName.finalizeValue()
    }
}
