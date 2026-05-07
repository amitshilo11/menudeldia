package com.amitshilo.menudeldia.routes

import com.amitshilo.menudeldia.data.remote.dto.MenuDto
import com.amitshilo.menudeldia.data.remote.dto.OpeningHoursDto
import com.amitshilo.menudeldia.data.remote.dto.RestaurantDto
import com.amitshilo.menudeldia.data.remote.dto.RestaurantListResponse
import com.amitshilo.menudeldia.db.MenusTable
import com.amitshilo.menudeldia.db.RestaurantsTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun Application.configureRestaurantRoutes(json: Json) {
    routing {
        route("/api/v1") {
            get("/restaurants") {
                val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing lat")
                val lng = call.request.queryParameters["lng"]?.toDoubleOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing lng")
                val radius = call.request.queryParameters["radius"]?.toIntOrNull() ?: 5000

                val all =
                    transaction { RestaurantsTable.selectAll().map { it.toRestaurantDto(json) } }
                val nearby = all
                    .filter { haversineMeters(lat, lng, it.lat, it.lng) <= radius }
                    .sortedBy { haversineMeters(lat, lng, it.lat, it.lng) }

                call.respond(RestaurantListResponse(nearby))
            }

            get("/restaurants/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")

                val restaurant = transaction {
                    RestaurantsTable.selectAll()
                        .where { RestaurantsTable.id eq id }
                        .singleOrNull()
                        ?.toRestaurantDto(json)
                } ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(restaurant)
            }

            get("/restaurants/{id}/menu/today") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                val menu = transaction {
                    MenusTable.selectAll()
                        .where { (MenusTable.restaurantId eq id) and (MenusTable.date eq today) }
                        .singleOrNull()
                        ?.toMenuDto(json)
                } ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(menu)
            }
        }
    }
}

private fun ResultRow.toRestaurantDto(json: Json) = RestaurantDto(
    id = this[RestaurantsTable.id],
    name = this[RestaurantsTable.name],
    lat = this[RestaurantsTable.lat],
    lng = this[RestaurantsTable.lng],
    address = this[RestaurantsTable.address],
    phone = this[RestaurantsTable.phone],
    thumbnailUrl = this[RestaurantsTable.thumbnailUrl],
    photos = json.decodeFromString(this[RestaurantsTable.photosJson]),
    descriptionEs = this[RestaurantsTable.descriptionEs],
    descriptionEn = this[RestaurantsTable.descriptionEn],
    openingHours = json.decodeFromString<List<OpeningHoursDto>>(this[RestaurantsTable.openingHoursJson]),
    menuPrice = this[RestaurantsTable.menuPrice],
    currency = this[RestaurantsTable.currency],
    todayHasMenu = this[RestaurantsTable.todayHasMenu],
)

private fun ResultRow.toMenuDto(json: Json) = MenuDto(
    id = this[MenusTable.id],
    restaurantId = this[MenusTable.restaurantId],
    date = this[MenusTable.date].toString(),
    price = this[MenusTable.price],
    currency = this[MenusTable.currency],
    firsts = json.decodeFromString(this[MenusTable.firstsJson]),
    seconds = json.decodeFromString(this[MenusTable.secondsJson]),
    desserts = json.decodeFromString(this[MenusTable.dessertsJson]),
    notes = this[MenusTable.notes],
)

private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6_371_000.0
    val φ1 = lat1 * PI / 180
    val φ2 = lat2 * PI / 180
    val Δφ = (lat2 - lat1) * PI / 180
    val Δλ = (lng2 - lng1) * PI / 180
    val a = sin(Δφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}
