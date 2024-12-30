/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import coil.ImageLoader
import coil.disk.DiskCache
import com.vitorpamplona.amethyst.service.ots.OkHttpBlockstreamExplorer
import com.vitorpamplona.amethyst.service.ots.OkHttpCalendarBuilder
import com.vitorpamplona.amethyst.service.playback.VideoCache
import com.vitorpamplona.quartz.events.OtsEvent
import com.vitorpamplona.quartz.ots.OpenTimestamps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.measureTimedValue

class Amethyst : Application() {
    val applicationIOScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onTerminate() {
        super.onTerminate()
        applicationIOScope.cancel()
    }

    val videoCache: VideoCache by lazy {
        val newCache = VideoCache()
        newCache.initFileCache(this)
        newCache
    }

    val coilCache: DiskCache by lazy {
        DiskCache.Builder()
            .directory(applicationContext.safeCacheDir.resolve("image_cache"))
            .maxSizePercent(0.2)
            .maximumMaxSizeBytes(500L * 1024 * 1024) // 250MB
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        OtsEvent.otsInstance = OpenTimestamps(OkHttpBlockstreamExplorer(), OkHttpCalendarBuilder())

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder().detectAll().penaltyLog().build(),
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder().detectAll().penaltyLog().build(),
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            val (value, elapsed) =
                measureTimedValue {
                    // initializes the video cache in a thread
                    videoCache
                }
            Log.d("Rendering Metrics", "VideoCache initialized in $elapsed")
        }
    }

    fun imageLoaderBuilder(): ImageLoader.Builder {
        return ImageLoader.Builder(applicationContext).diskCache { coilCache }
    }

    fun encryptedStorage(npub: String? = null): EncryptedSharedPreferences {
        return EncryptedStorage.preferences(instance, npub)
    }

    companion object {
        lateinit var instance: Amethyst
            private set
    }
}

internal val Context.safeCacheDir: File
    get() {
        val cacheDir = checkNotNull(cacheDir) { "cacheDir == null" }
        return cacheDir.apply { mkdirs() }
    }
