package me.modmuss50.mpp.platforms.curseforge.options

import org.gradle.api.provider.Property

interface ICurseforgeVersionRangeOptions {
    /**
     * The start version of the range (inclusive)
     */
    val start: Property<String>

    /**
     * The end version of the range (inclusive)
     */
    val end: Property<String>
}
