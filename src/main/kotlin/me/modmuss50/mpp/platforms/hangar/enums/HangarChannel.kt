package me.modmuss50.mpp.platforms.hangar.enums

import java.lang.IllegalArgumentException

enum class HangarChannel {
    RELEASE,
    SNAPSHOT;

    companion object {
        @JvmStatic
        fun of(value: String): HangarChannel {
            val upper = value.uppercase()
            try {
                return valueOf(upper)
            } catch (_ : IllegalArgumentException) {
                throw IllegalArgumentException("Invalid channel type: $upper. Must be one of: RELEASE, SNAPSHOT")
            }
        }
    }
}