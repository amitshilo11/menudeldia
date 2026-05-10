package com.menudeldia.photo

import com.menudeldia.config.AppProperties
import com.menudeldia.places.GooglePlacesClient
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.UUID

/**
 * Downloads Google Places photos to disk and serves them back via a local path lookup.
 *
 * Layout: {storageRoot}/{restaurantId}/{n}.jpg  where n is 0..photoCount-1.
 *
 * TODO B2.3.1: implement download + idempotency (skip if file exists for current places_fetched_at).
 * TODO B2.3.4: log WARN when total disk usage exceeds 4GB.
 */
@Service
class PhotoStorageService(
    private val props: AppProperties,
    private val places: GooglePlacesClient,
) {

    /** Downloads up to N photos for a restaurant. Returns count actually persisted. */
    fun downloadPhotos(restaurantId: UUID, photoNames: List<String>): Int {
        // TODO: ensure dir, fetch bytes via places.photoBytes, write to {root}/{id}/{n}.jpg.
        TODO("Phase 2 — task B2.3.1")
    }

    /** Resolves the on-disk path for photo `n` of a restaurant. */
    fun pathFor(restaurantId: UUID, n: Int): Path {
        return Path.of(props.photos.storageRoot, restaurantId.toString(), "$n.jpg")
    }
}
