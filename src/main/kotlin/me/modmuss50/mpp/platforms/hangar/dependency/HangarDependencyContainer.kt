package me.modmuss50.mpp.platforms.hangar.dependency

import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.jetbrains.annotations.ApiStatus
import javax.inject.Inject

@Incubating
interface HangarDependencyContainer {
    @get:Input
    val dependencies: ListProperty<HangarDependency>

    fun requires(action: Action<HangarDependency>) {
        addInternal(HangarDependency.DependencyType.REQUIRED, action)
    }

    fun optional(action: Action<HangarDependency>) {
        addInternal(HangarDependency.DependencyType.OPTIONAL, action)
    }

    fun fromDependencies(other: HangarDependencyContainer) {
        dependencies.convention(other.dependencies)
    }

    @get:ApiStatus.Internal
    @get:Inject
    val objectFactory: ObjectFactory

    @Internal
    fun addInternal(type: HangarDependency.DependencyType, action: Action<HangarDependency>) {
        val dep = objectFactory.newInstance(HangarDependency::class.java)
        dep.type.set(type)
        dep.type.finalizeValue()
        action.execute(dep)
        dep.validate()
        dependencies.add(dep)
    }
}
