package me.modmuss50.mpp.platforms.hangar.platform

import me.modmuss50.mpp.platforms.hangar.enums.HangarPlatformType
import org.gradle.api.file.RegularFileProperty

data class HangarGradlePlatform(
    val platform: HangarPlatformType,
    val versions: List<String>,
    val file: RegularFileProperty,
)
