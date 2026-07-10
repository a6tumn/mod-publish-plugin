package me.modmuss50.mpp.platforms.modrinth.options

import org.gradle.api.provider.Property

interface IModrinthVersionRangeOptions {
    /**
     * The start version of the range (inclusive)
     */
    val start: Property<String>

    /**
     * The end version of the range (inclusive)
     */
    val end: Property<String>

    /**
     * Whether to include snapshot versions in the range
     */
    val includeSnapshots: Property<Boolean>
}
