package me.modmuss50.mpp.platforms.hangar

import me.modmuss50.mpp.PlatformOptions
import me.modmuss50.mpp.PlatformOptionsInternal
import me.modmuss50.mpp.platforms.hangar.enums.HangarChannel
import me.modmuss50.mpp.platforms.hangar.platform.HangarGradlePlatform
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface HangerOptions : PlatformOptions, PlatformOptionsInternal<HangerOptions> {
    @get:Input
    val project: Property<String>

    @get:Input
    val channel: Property<HangarChannel>

    @get:Input
    val platforms: ListProperty<HangarGradlePlatform>

    @get:Input
    @get:Optional
    val apiEndpoint: Property<String>

    override fun setInternalDefaults() {
        channel.convention(HangarChannel.RELEASE)
    }
}