package com.amitshilo.menudeldia.data.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache

/** A few hundred restaurant thumbnails at typical card resolution. */
private const val DISK_CACHE_MAX_SIZE_BYTES = 100L * 1024 * 1024

/**
 * The app's singleton [ImageLoader], set via `setSingletonImageLoaderFactory` at the app root.
 *
 * Coil ships with no disk cache configured outside Android — `PlatformContext` on iOS and web
 * carries no filesystem of its own for Coil to default to — so without this, every restaurant
 * thumbnail that scrolled off screen and back on was re-fetched over the network instead of read
 * from disk. That's what made scrolling the list feel stuck: each re-entering card blocked on a
 * network round trip rather than a cache hit.
 */
internal fun newImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .diskCache {
            imageCacheDirectory(context)?.let { directory ->
                DiskCache.Builder()
                    .directory(directory)
                    .maxSizeBytes(DISK_CACHE_MAX_SIZE_BYTES)
                    .build()
            }
        }
        .build()
