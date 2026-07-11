package me.modmuss50.mpp.platforms.github

import me.modmuss50.mpp.internal.IPlatformOptions
import me.modmuss50.mpp.internal.IPlatformOptionsInternal
import me.modmuss50.mpp.internal.IPublishOptions
import me.modmuss50.mpp.internal.PublishModTask
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.annotations.ApiStatus.Internal

interface IGithubOptions : IPlatformOptions, IPlatformOptionsInternal<IGithubOptions> {
    @get:InputFile
    @get:Optional
    override val file: RegularFileProperty

    /**
     * "owner/repo"
     */
    @get:Input
    val repository: Property<String>

    /**
     * Specifies the commitish value that determines where the Git tag is created from. Can be any branch or commit SHA.
     */
    @get:Input
    val commitish: Property<String>

    @get:Input
    val tagName: Property<String>

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

    fun from(other: IGithubOptions) {
        super.from(other)
        repository.convention(other.repository)
        commitish.convention(other.commitish)
        tagName.convention(other.tagName)
        apiEndpoint.convention(other.apiEndpoint)
        allowEmptyFiles.convention(other.allowEmptyFiles)
        releaseResult.convention(other.releaseResult)
    }

    fun from(other: Provider<IGithubOptions>) {
        from(other.get())
    }

    fun from(other: Provider<IGithubOptions>, publishOptions: Provider<IPublishOptions>) {
        from(other)
        from(publishOptions.get())
    }

    /**
     * Publish to an existing release, created by another task.
     */
    fun parent(task: TaskProvider<Task>) {
        val publishTask = task.map { it as PublishModTask }
        releaseResult.set(publishTask.flatMap { it.result })

        val options = publishTask.map { it.platform as IGithubOptions }
        version.set(options.flatMap { it.version })
        version.finalizeValue()
        changelog.set(options.flatMap { it.changelog })
        changelog.finalizeValue()
        type.set(options.flatMap { it.type })
        type.finalizeValue()
        displayName.set(options.flatMap { it.displayName })
        displayName.finalizeValue()
        repository.set(options.flatMap { it.repository })
        repository.finalizeValue()
        commitish.set(options.flatMap { it.commitish })
        commitish.finalizeValue()
        tagName.set(options.flatMap { it.tagName })
        tagName.finalizeValue()
    }
}
