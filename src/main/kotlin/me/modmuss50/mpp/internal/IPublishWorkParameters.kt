package me.modmuss50.mpp.internal

import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkParameters
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
interface IPublishWorkParameters : WorkParameters {
    val result: RegularFileProperty
}