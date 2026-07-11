package me.modmuss50.mpp.internal

import me.modmuss50.mpp.platforms.PublishResult
import org.gradle.api.Named
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.jetbrains.annotations.ApiStatus
import java.util.Locale
import javax.inject.Inject

abstract class Platform @Inject constructor(
    private val name: String
) : Named, IPlatformOptions {
    @ApiStatus.Internal
    open fun validateInputs() {
    }

    @ApiStatus.Internal
    abstract fun publish(context: PublishContext)

    @ApiStatus.Internal
    abstract fun dryRunPublishResult(): PublishResult

    @ApiStatus.Internal
    abstract fun printDryRunInfo(logger: Logger)

    @get:ApiStatus.Internal
    @get:Internal
    val taskName: String
        get() = "publish" + titlecase(name)

    init {
        (this as IPlatformOptionsInternal<*>).setInternalDefaults()
    }

    @Input
    override fun getName(): String {
        return name
    }

    fun titlecase(string: String): String {
        return string.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.ROOT)
            } else {
                it.toString()
            }
        }
    }
}