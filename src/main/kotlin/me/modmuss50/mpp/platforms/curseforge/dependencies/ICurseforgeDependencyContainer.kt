package me.modmuss50.mpp.platforms.curseforge.dependencies

import me.modmuss50.mpp.PlatformDependency
import me.modmuss50.mpp.PlatformDependencyContainer
import org.gradle.api.tasks.Internal

/**
 * Provides shorthand methods for adding dependencies to CurseForge
 */
interface ICurseforgeDependencyContainer : PlatformDependencyContainer<ICurseforgeDependency> {
    fun requires(vararg slugs: String) {
        addInternal(PlatformDependency.DependencyType.REQUIRED, slugs)
    }

    fun optional(vararg slugs: String) {
        addInternal(PlatformDependency.DependencyType.OPTIONAL, slugs)
    }

    fun incompatible(vararg slugs: String) {
        addInternal(PlatformDependency.DependencyType.INCOMPATIBLE, slugs)
    }

    fun embeds(vararg slugs: String) {
        addInternal(PlatformDependency.DependencyType.EMBEDDED, slugs)
    }

    @Internal
    fun addInternal(
        type: PlatformDependency.DependencyType,
        slugs: Array<out String>,
    ) {
        slugs.forEach {
            dependencies.add(
                objectFactory.newInstance(ICurseforgeDependency::class.java).apply {
                    this.slug.set(it)
                    this.type.set(type)
                },
            )
        }
    }
}
