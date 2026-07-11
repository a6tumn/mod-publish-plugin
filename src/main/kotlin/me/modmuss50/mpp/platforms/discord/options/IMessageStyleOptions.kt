package me.modmuss50.mpp.platforms.discord.options

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@Suppress("MemberVisibilityCanBePrivate")
interface IMessageStyleOptions {
    @get:Input
    val look: Property<String>

    @get:Input
    @get:Optional
    val thumbnailUrl: Property<String>

    @get:Input
    @get:Optional
    val color: Property<String>

    @get:Input
    @get:Optional
    val link: Property<String>

    fun from(other: IMessageStyleOptions) {
        look.convention(other.look)
        thumbnailUrl.convention(other.thumbnailUrl)
        color.convention(other.color)
        link.convention(other.link)
    }
}