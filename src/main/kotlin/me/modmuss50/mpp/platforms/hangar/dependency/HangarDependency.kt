package me.modmuss50.mpp.platforms.hangar.dependency

import org.gradle.api.Incubating
import org.gradle.api.provider.Property
import org.jetbrains.annotations.ApiStatus

@Incubating
interface HangarDependency {
    val name: Property<String>

    val url: Property<String>

    val type: Property<DependencyType>

    enum class DependencyType {
        REQUIRED,
        OPTIONAL,
    }

    @ApiStatus.Internal
    fun validate()
}
