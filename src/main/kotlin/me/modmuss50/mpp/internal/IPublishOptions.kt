package me.modmuss50.mpp.internal

import me.modmuss50.mpp.util.ReleaseType
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.annotations.ApiStatus
import javax.inject.Inject

// Contains options shared by each platform and the extension
interface IPublishOptions {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    val file: RegularFileProperty

    @get:Input
    val version: Property<String> // Should this use a TextResource?

    @get:Input
    val changelog: Property<String>

    @get:Input
    val type: Property<ReleaseType>

    @get:Input
    val displayName: Property<String>

    @get:Input
    val modLoaders: ListProperty<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    val additionalFiles: ConfigurableFileCollection

    @get:Input
    val maxRetries: Property<Int>

    @get:Inject
    @get:ApiStatus.Internal
    val _thisProject: Project

    fun from(other: IPublishOptions) {
        file.convention(other.file)
        version.convention(other.version)
        changelog.convention(other.changelog)
        type.convention(other.type)
        displayName.convention(other.displayName)
        modLoaders.convention(other.modLoaders)
        additionalFiles.convention(other.additionalFiles)
        maxRetries.convention(other.maxRetries)
    }

    /**
     * A helper function to add a file from the output of another project
     */
    fun file(project: Project) {
        var configuration = _thisProject.configurations.detachedConfiguration(
            _thisProject.dependencyFactory.create(project).setTransitive(false),
        )
        file.fileProvider(
            configuration.elements.map { it.single().asFile },
        )
    }

    /**
     * A helper function to add an additional file from the output of another project
     */
    fun additionalFile(project: Project) {
        var configuration = _thisProject.configurations.detachedConfiguration(
            _thisProject.dependencyFactory.create(project).setTransitive(false),
        )
        additionalFiles.from(configuration)
    }
}