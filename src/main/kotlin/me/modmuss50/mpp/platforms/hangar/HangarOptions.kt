package me.modmuss50.mpp.platforms.hangar

import me.modmuss50.mpp.PlatformOptions
import me.modmuss50.mpp.PlatformOptionsInternal
import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.PublishOptions
import org.gradle.api.Incubating
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskProvider

/*
* https://github.com/HangarMC/hangar-publish-plugin/blob/master/plugin/src/main/kotlin/io/papermc/hangarpublishplugin/model/HangarPublication.kt
*/

@Incubating
interface HangarOptions : PlatformOptions, PlatformOptionsInternal<HangarOptions> {

    /**
     * The API URL, defaults to https://hangar.papermc.io/api/v1/.
     */
    @get:Input
    @get:Optional
    val apiEndpoint: Property<String>

    /**
     * The id of the Hangar project this publication is for.
     */
    @get:Input
    val id: Property<String>

    /**
     * The type of plugin for this publication, i.e. "Paper", "Velocity", or "Waterfall".
     */
    @get:Input
    val projectType: Property<HangarPlatformType>

    /**
     * List of supported platform versions, i.e. `listOf("26.1", "26.1.1")`.
     */
    @get:Input
    val platformVersions: ListProperty<String>

    fun from(other: HangarOptions) {
        super.from(other)
        apiEndpoint.set(other.apiEndpoint)
        id.set(other.id)
        projectType.convention(other.projectType)
        platformVersions.convention(other.platformVersions)
    }

    fun from(other: Provider<HangarOptions>) {
        from(other.get())
    }

    fun from(
        other: Provider<HangarOptions>,
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
        val options = publishTask.map { it.platform as HangarOptions }

        version.set(options.flatMap { it.version })
        version.finalizeValue()
        changelog.set(options.flatMap { it.changelog })
        changelog.finalizeValue()
        type.set(options.flatMap { it.type })
        type.finalizeValue()
        displayName.set(options.flatMap { it.displayName })
        displayName.finalizeValue()

        id.set(options.flatMap { it.id })
        id.finalizeValue()
        projectType.set(options.flatMap { it.projectType })
        projectType.finalizeValue()
        platformVersions.set(options.flatMap { it.platformVersions })
        platformVersions.finalizeValue()
    }
}
