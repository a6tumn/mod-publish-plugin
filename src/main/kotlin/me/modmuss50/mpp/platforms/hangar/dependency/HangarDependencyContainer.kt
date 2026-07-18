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

    fun requires(
        platform: String,
        action: Action<HangarDependency>,
    ) {
        addInternal(platform, true, action)
    }

    fun optional(
        platform: String,
        action: Action<HangarDependency>,
    ) {
        addInternal(platform, false, action)
    }

    fun fromDependencies(other: HangarDependencyContainer) {
        dependencies.convention(other.dependencies)
    }

    @get:ApiStatus.Internal
    @get:Inject
    val objectFactory: ObjectFactory

    @Internal
    fun addInternal(
        platform: String,
        required: Boolean,
        action: Action<HangarDependency>,
    ) {
        val dep = objectFactory.newInstance(HangarDependency::class.java)

        dep.platform.set(platform)
        dep.platform.finalizeValue()

        dep.required.set(required)
        dep.required.finalizeValue()

        action.execute(dep)

        dep.validate()
        dependencies.add(dep)
    }
}
