package me.modmuss50.mpp.platforms.hangar.enums

import org.gradle.api.Incubating
import java.lang.IllegalArgumentException

@Incubating
enum class HangarChannel {
    RELEASE,
    SNAPSHOT,
    ALPHA;

    companion object {
        @JvmStatic
        fun of(value: String): HangarChannel {
            val upper = value.uppercase()
            try {
                return valueOf(upper)
            } catch (_ : IllegalArgumentException) {
                throw IllegalArgumentException("Invalid channel type: $upper. Must be one of: RELEASE, SNAPSHOT, ALPHA")
            }
        }
    }
}