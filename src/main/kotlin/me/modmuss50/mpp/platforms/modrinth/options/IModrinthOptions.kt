package me.modmuss50.mpp.platforms.modrinth.options

import me.modmuss50.mpp.internal.IPlatformOptions
import me.modmuss50.mpp.internal.IPlatformOptionsInternal
import me.modmuss50.mpp.internal.IPublishOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import me.modmuss50.mpp.platforms.modrinth.dependencies.IModrinthDependency
import me.modmuss50.mpp.platforms.modrinth.dependencies.IModrinthDependencyContainer
import me.modmuss50.mpp.util.MinecraftApi
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KClass

interface IModrinthOptions : IPlatformOptions, IPlatformOptionsInternal<IModrinthOptions>, IModrinthDependencyContainer {
    companion object {
        // https://github.com/modrinth/labrinth/blob/ae1c5342f2017c1c93008d1e87f1a29549dca92f/src/scheduler.rs#L112
        @JvmStatic
        val WALL_OF_SHAME =
            mapOf(
                "1.14.2 Pre-Release 4" to "1.14.2-pre4",
                "1.14.2 Pre-Release 3" to "1.14.2-pre3",
                "1.14.2 Pre-Release 2" to "1.14.2-pre2",
                "1.14.2 Pre-Release 1" to "1.14.2-pre1",
                "1.14.1 Pre-Release 2" to "1.14.1-pre2",
                "1.14.1 Pre-Release 1" to "1.14.1-pre1",
                "1.14 Pre-Release 5" to "1.14-pre5",
                "1.14 Pre-Release 4" to "1.14-pre4",
                "1.14 Pre-Release 3" to "1.14-pre3",
                "1.14 Pre-Release 2" to "1.14-pre2",
                "1.14 Pre-Release 1" to "1.14-pre1",
                "3D Shareware v1.34" to "3D-Shareware-v1.34",
            )
    }

    @get:Input
    val projectId: Property<String>

    @get:Input
    val minecraftVersions: ListProperty<String>

    @get:Input
    val featured: Property<Boolean>

    /**
     * The environment to upload the version with. ie. client-only, server-only, etc.
     *
     * See [me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment] for the list of available environments and their use cases.
     */
    @get:Input
    @get:Optional
    val environment: Property<ModrinthEnvironment>

    /**
     * When set, this will update the project description to the provided value.
     */
    @get:Input
    @get:Optional
    val projectDescription: Property<String>

    @get:Input
    val apiEndpoint: Property<String>

    @ApiStatus.Internal
    override fun setInternalDefaults() {
        featured.convention(false)
        apiEndpoint.convention("https://api.modrinth.com/v2")
    }

    fun from(other: IModrinthOptions) {
        super.from(other)
        fromDependencies(other)
        projectId.convention(other.projectId)
        minecraftVersions.convention(other.minecraftVersions)
        featured.convention(other.featured)
        environment.convention(other.environment)
        projectDescription.convention(other.projectDescription)
        apiEndpoint.convention(other.apiEndpoint)
    }

    fun from(other: Provider<IModrinthOptions>) {
        from(other.get())
    }

    fun from(
        other: Provider<IModrinthOptions>,
        publishOptions: Provider<IPublishOptions>,
    ) {
        from(other)
        from(publishOptions.get())
    }

    override val platformDependencyKClass: KClass<IModrinthDependency>
        get() = IModrinthDependency::class

    fun minecraftVersionList(csv: String) {
        addMinecraftVersions(
            providerFactory.provider {
                csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            },
        )
    }

    fun minecraftVersionRange(action: Action<IModrinthVersionRangeOptions>) {
        val options = objectFactory.newInstance(IModrinthVersionRangeOptions::class.java)
        options.includeSnapshots.convention(false)
        action.execute(options)

        addMinecraftVersions(
            providerFactory.provider {
                MinecraftApi()
                    .getVersionsInRange(
                        options.start.get(),
                        options.end.get(),
                        options.includeSnapshots.get(),
                    ).map { WALL_OF_SHAME.getOrDefault(it, it) }
            },
        )
    }

    private fun addMinecraftVersions(provider: Provider<List<String>>) {
        minecraftVersions.addAll(provider)
    }
}
