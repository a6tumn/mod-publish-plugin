package me.modmuss50.mpp.platforms.curseforge.dependencies

import me.modmuss50.mpp.internal.IPlatformDependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface ICurseforgeDependency : IPlatformDependency {
    @get:Input
    val slug: Property<String>

    override fun validate() {
        if (slug.orNull.isNullOrBlank()) {
            throw IllegalArgumentException("Dependency slug cannot be null or blank")
        }
    }
}
