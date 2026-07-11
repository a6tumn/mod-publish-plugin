package me.modmuss50.mpp.internal

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.jetbrains.annotations.ApiStatus
import javax.inject.Inject
import kotlin.reflect.KClass

interface IPlatformDependencyContainer<T : IPlatformDependency> {
    @get:Input
    val dependencies: ListProperty<T>

    fun requires(action: Action<T>) {
        addInternal(IPlatformDependency.DependencyType.REQUIRED, action)
    }

    fun optional(action: Action<T>) {
        addInternal(IPlatformDependency.DependencyType.OPTIONAL, action)
    }

    fun incompatible(action: Action<T>) {
        addInternal(IPlatformDependency.DependencyType.INCOMPATIBLE, action)
    }

    fun embeds(action: Action<T>) {
        addInternal(IPlatformDependency.DependencyType.EMBEDDED, action)
    }

    fun fromDependencies(other: IPlatformDependencyContainer<T>) {
        dependencies.convention(other.dependencies)
    }

    @get:ApiStatus.Internal
    @get:Inject
    val objectFactory: ObjectFactory

    @get:ApiStatus.Internal
    @get:Inject
    val providerFactory: ProviderFactory

    @get:ApiStatus.OverrideOnly
    @get:Internal
    val platformDependencyKClass: KClass<T>

    @Internal
    fun addInternal(type: IPlatformDependency.DependencyType, action: Action<T>) {
        val dep = objectFactory.newInstance(platformDependencyKClass.java)
        dep.type.set(type)
        dep.type.finalizeValue()
        action.execute(dep)
        dep.validate()
        dependencies.add(dep)
    }
}
