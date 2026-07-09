package me.modmuss50.mpp.platforms.hangar.platform

import me.modmuss50.mpp.platforms.hangar.enums.HangarPlatformType
import java.io.File

data class HangarPlatform(
    val platform: HangarPlatformType,
    val versions: List<String>,
    val file: File,
)
