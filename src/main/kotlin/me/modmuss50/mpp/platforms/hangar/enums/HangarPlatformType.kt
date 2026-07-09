package me.modmuss50.mpp.platforms.hangar.enums

import org.gradle.api.Incubating
import java.lang.IllegalArgumentException

@Incubating
enum class HangarPlatformType {
    PAPER,
    VELOCITY,
    WATERFALL,
    ;

    companion object {
        @JvmStatic
        fun of(value: String): HangarPlatformType {
            val upper = value.uppercase()
            try {
                return valueOf(upper)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid platform type: $upper. Must be one of: PAPER, VELOCITY, WATEFALL")
            }
        }
    }
}
