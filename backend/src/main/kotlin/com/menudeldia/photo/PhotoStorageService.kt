package com.menudeldia.photo

import com.menudeldia.config.AppProperties
import com.menudeldia.places.GooglePlacesClient
import com.menudeldia.places.PlacesException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

private const val DISK_WARN_BYTES = 4L * 1024 * 1024 * 1024

@Service
class PhotoStorageService(
    private val props: AppProperties,
    private val places: GooglePlacesClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun downloadPhotos(restaurantId: UUID, photoNames: List<String>): Int {
        if (photoNames.isEmpty()) return 0
        val dir = Path.of(props.photos.storageRoot, restaurantId.toString())
        Files.createDirectories(dir)
        var count = 0
        photoNames.forEachIndexed { n, photoName ->
            val file = dir.resolve("$n.jpg")
            if (!Files.exists(file)) {
                try {
                    val bytes = places.photoBytes(photoName, 800)
                    Files.write(file, bytes)
                } catch (ex: PlacesException) {
                    log.warn("Failed to download photo {} for {}: {}", n, restaurantId, ex.message)
                    return@forEachIndexed
                }
            }
            count++
        }
        checkDiskUsage()
        return count
    }

    fun pathFor(restaurantId: UUID, n: Int): Path =
        Path.of(props.photos.storageRoot, restaurantId.toString(), "$n.jpg")

    private fun checkDiskUsage() {
        val root = Path.of(props.photos.storageRoot)
        if (!Files.exists(root)) return
        val totalBytes = Files.walk(root).filter(Files::isRegularFile).mapToLong(Files::size).sum()
        if (totalBytes > DISK_WARN_BYTES) {
            log.warn("Photos directory {} exceeds 4 GB ({} bytes total)", root, totalBytes)
        }
    }
}
