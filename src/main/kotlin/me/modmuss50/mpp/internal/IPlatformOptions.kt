package me.modmuss50.mpp.internal

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface IPlatformOptions : IPublishOptions {
    @get:Optional
    @get:Input
    val accessToken: Property<String>

    @get:Optional
    @get:Input
    val announcementTitle: Property<String>

    fun from(other: IPlatformOptions) {
        super.from(other)
        accessToken.convention(other.accessToken)
        announcementTitle.convention(other.announcementTitle)
    }
}
