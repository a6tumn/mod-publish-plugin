package me.modmuss50.mpp.internal

import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
interface IPlatformOptionsInternal<T : IPlatformOptions> {
    fun setInternalDefaults()
}