package me.modmuss50.mpp.platforms.modrinth.dependencies

import me.modmuss50.mpp.PlatformDependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface IModrinthDependency : PlatformDependency {
    @get:Input
    @get:Optional
    val id: Property<String>

    @get:Input
    @get:Optional
    val slug: Property<String>

    @get:Input
    @get:Optional
    val version: Property<String>

    override fun validate() {
        if (slug.orNull.isNullOrBlank() && id.orNull.isNullOrBlank()) {
            throw IllegalStateException("Modrinth dependency must have either an id or slug specified")
        }
    }
}
