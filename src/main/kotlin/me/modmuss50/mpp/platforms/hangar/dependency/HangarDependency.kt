package me.modmuss50.mpp.platforms.hangar.dependency

import org.gradle.api.Incubating
import org.gradle.api.provider.Property
import org.jetbrains.annotations.ApiStatus

@Incubating
interface HangarDependency {
    val name: Property<String>

    val url: Property<String>

    val required: Property<Boolean>

    @ApiStatus.Internal
    fun validate()
}
