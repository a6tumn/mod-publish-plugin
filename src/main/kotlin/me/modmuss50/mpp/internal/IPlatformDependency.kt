package me.modmuss50.mpp.internal

import org.gradle.api.provider.Property
import org.jetbrains.annotations.ApiStatus

interface IPlatformDependency {
    val type: Property<DependencyType>

    enum class DependencyType {
        REQUIRED,
        OPTIONAL,
        INCOMPATIBLE,
        EMBEDDED,
    }

    @ApiStatus.Internal
    fun validate()
}