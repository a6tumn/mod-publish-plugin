package me.modmuss50.mpp.platforms.curseforge.options

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * Options for additional files to upload alongside the main file
 */
interface IAdditionalFileOptions {
    /**
     * The display name of the additional file
     */
    @get:Input
    val name: Property<String>
}
