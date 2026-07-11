package me.modmuss50.mpp.platforms.modrinth.dependencies

import me.modmuss50.mpp.internal.IPlatformDependency
import me.modmuss50.mpp.internal.IPlatformDependencyContainer
import org.gradle.api.tasks.Internal

/**
 * Provides shorthand methods for adding dependencies to Modrinth
 */
interface IModrinthDependencyContainer : IPlatformDependencyContainer<IModrinthDependency> {
    fun requires(vararg slugs: String) {
        addInternal(IPlatformDependency.DependencyType.REQUIRED, slugs)
    }

    fun optional(vararg slugs: String) {
        addInternal(IPlatformDependency.DependencyType.OPTIONAL, slugs)
    }

    fun incompatible(vararg slugs: String) {
        addInternal(IPlatformDependency.DependencyType.INCOMPATIBLE, slugs)
    }

    fun embeds(vararg slugs: String) {
        addInternal(IPlatformDependency.DependencyType.EMBEDDED, slugs)
    }

    @Internal
    fun addInternal(
        type: IPlatformDependency.DependencyType,
        slugs: Array<out String>,
    ) {
        slugs.forEach {
            dependencies.add(
                objectFactory.newInstance(IModrinthDependency::class.java).apply {
                    this.slug.set(it)
                    this.type.set(type)
                },
            )
        }
    }
}
