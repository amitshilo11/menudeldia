package com.amitshilo.menudeldia.data.image

import coil3.PlatformContext
import okio.Path

/**
 * Writable directory Coil's disk cache can use, or null where the platform has no filesystem
 * Coil can address. Null on web: browsers give Kotlin/Wasm and Kotlin/JS no arbitrary filesystem
 * access, so [newImageLoader] falls back to its in-memory cache there — a normal browser HTTP
 * cache still covers repeat loads within a session.
 */
internal expect fun imageCacheDirectory(context: PlatformContext): Path?
