package me.modmuss50.mpp.platforms.curseforge.options

import me.modmuss50.mpp.internal.IPlatformOptions
import me.modmuss50.mpp.internal.IPlatformOptionsInternal
import me.modmuss50.mpp.util.MinecraftApi
import me.modmuss50.mpp.internal.IPublishOptions
import me.modmuss50.mpp.platforms.curseforge.dependencies.ICurseforgeDependency
import me.modmuss50.mpp.platforms.curseforge.dependencies.ICurseforgeDependencyContainer
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KClass

interface ICurseforgeOptions : IPlatformOptions, IPlatformOptionsInternal<ICurseforgeOptions>, ICurseforgeDependencyContainer {
    @get:Input
    val projectId: Property<String>

    // Project slug, used by Discord webhook to link to the uploaded file.
    @get:Input
    @get:Optional
    val projectSlug: Property<String>

    @get:Input
    val minecraftVersions: ListProperty<String>

    @get:Input
    @get:Optional
    val client: Property<Boolean>

    @get:Input
    @get:Optional
    val server: Property<Boolean>

    @Deprecated("Renamed to client", ReplaceWith("client"))
    @get:Input
    @get:Optional
    val clientRequired: Property<Boolean>
        get() = client

    @Deprecated("Renamed to server", ReplaceWith("server"))
    @get:Input
    @get:Optional
    val serverRequired: Property<Boolean>
        get() = server

    @get:Input
    val javaVersions: ListProperty<JavaVersion>

    @get:Input
    val apiEndpoint: Property<String>

    @get:Input
    val changelogType: Property<String>

    @get:Nested
    @get:ApiStatus.Internal
    val additionalFilesExt: MapProperty<ConfigurableFileCollection, IAdditionalFileOptions>

    fun from(other: ICurseforgeOptions) {
        super.from(other)
        fromDependencies(other)
        projectId.convention(other.projectId)
        projectSlug.convention(other.projectSlug)
        minecraftVersions.convention(other.minecraftVersions)
        client.convention(other.client)
        server.convention(other.server)
        javaVersions.convention(other.javaVersions)
        apiEndpoint.convention(other.apiEndpoint)
        changelogType.convention(other.changelogType)
        additionalFilesExt.convention(other.additionalFilesExt)
    }

    fun from(other: Provider<ICurseforgeOptions>) {
        from(other.get())
    }

    fun from(
        other: Provider<ICurseforgeOptions>,
        publishOptions: Provider<IPublishOptions>,
    ) {
        from(other)
        from(publishOptions.get())
    }

    fun minecraftVersionList(csv: String) {
        addMinecraftVersions(
            providerFactory.provider {
                csv
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            },
        )
    }

    fun minecraftVersionRange(action: Action<ICurseforgeVersionRangeOptions>) {
        val options = objectFactory.newInstance(ICurseforgeVersionRangeOptions::class.java)
        action.execute(options)

        addMinecraftVersions(
            providerFactory.provider {
                MinecraftApi()
                    .getVersionsInRange(options.start.get(), options.end.get())
            },
        )
    }

    private fun addMinecraftVersions(provider: Provider<List<String>>) {
        minecraftVersions.addAll(provider)
    }

    fun additionalFile(
        file: Any,
        action: Action<IAdditionalFileOptions>,
    ) {
        val options = objectFactory.newInstance(IAdditionalFileOptions::class.java)
        action.execute(options)

        val fileCollection = objectFactory.fileCollection()
        fileCollection.from(
            when (file) {
                is Project -> {
                    val configuration =
                        _thisProject.configurations.detachedConfiguration(
                            _thisProject.dependencyFactory.create(file).setTransitive(false),
                        )
                    configuration.elements.map { it.single().asFile }
                }

                else -> {
                    file
                }
            },
        )

        additionalFiles.from(fileCollection)
        additionalFilesExt.put(fileCollection, options)
    }

    override fun setInternalDefaults() {
        apiEndpoint.convention("https://minecraft.curseforge.com")
        changelogType.convention("markdown")
    }

    override val platformDependencyKClass: KClass<ICurseforgeDependency>
        get() = ICurseforgeDependency::class
}
