package me.modmuss50.mpp.platforms.hangar

import me.modmuss50.mpp.PlatformOptions
import me.modmuss50.mpp.PlatformOptionsInternal
import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.PublishOptions
import me.modmuss50.mpp.platforms.hangar.enums.HangarChannel
import me.modmuss50.mpp.platforms.hangar.platform.HangarGradlePlatform
import org.gradle.api.Incubating
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskProvider

@Incubating
interface HangerOptions : PlatformOptions, PlatformOptionsInternal<HangerOptions> {
    @get:Input
    val project: Property<String>

    @get:Input
    val channel: Property<HangarApi.ChannelType>

    @get:Input
    val platforms: ListProperty<HangarGradlePlatform>

    @get:Input
    @get:Optional
    val apiEndpoint: Property<String>

    override fun setInternalDefaults() {
        channel.convention(HangarApi.ChannelType.valueOf(HangarChannel.RELEASE))
    }

    fun from(other: HangerOptions) {
        super.from(other)
        project.convention(other.project)
        channel.convention(other.channel)
        apiEndpoint.convention(other.apiEndpoint)
    }

    fun from(other: Provider<HangerOptions>) {
        from(other.get())
    }

    fun from(
        other: Provider<HangerOptions>,
        publishOptions: Provider<PublishOptions>,
    ) {
        from(other)
        from(publishOptions.get())
    }

    /**
     * Publish to an existing release, created by another task.
     */
    fun parent(task: TaskProvider<Task>) {
        val publishTask = task.map { it as PublishModTask }
        val options = publishTask.map { it.platform as HangerOptions }
        version.set(options.flatMap { it.version })
        version.finalizeValue()
        changelog.set(options.flatMap { it.changelog })
        changelog.finalizeValue()
        type.set(options.flatMap { it.type })
        type.finalizeValue()
        displayName.set(options.flatMap { it.displayName })
        displayName.finalizeValue()
        project.set(options.flatMap { it.project })
        project.finalizeValue()
        channel.set(options.flatMap { it.channel })
        channel.finalizeValue()
        platforms.set(options.flatMap { it.platforms })
        platforms.finalizeValue()
    }
}