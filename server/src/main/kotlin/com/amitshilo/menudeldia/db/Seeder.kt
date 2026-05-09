package com.amitshilo.menudeldia.db

import com.amitshilo.menudeldia.data.local.mockMenus
import com.amitshilo.menudeldia.data.local.mockRestaurants
import com.amitshilo.menudeldia.data.remote.dto.OpeningHoursDto
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Seeder {

    fun seedIfEmpty(json: Json) {
        transaction {
            if (RestaurantsTable.selectAll().count() > 0L) return@transaction

            mockRestaurants.forEach { r ->
                RestaurantsTable.insert {
                    it[id] = r.id
                    it[name] = r.name
                    it[address] = r.address
                    it[lat] = r.lat
                    it[lng] = r.lng
                    it[phone] = r.phone
                    it[thumbnailUrl] = r.thumbnailUrl
                    it[photosJson] = json.encodeToString(r.photos)
                    it[descriptionEs] = r.descriptionEs
                    it[descriptionEn] = r.descriptionEn
                    it[openingHoursJson] = json.encodeToString(
                        r.openingHours.map { hours ->
                            OpeningHoursDto(
                                dayOfWeek = hours.dayOfWeek.value,
                                openTime = hours.openTime.toString(),
                                closeTime = hours.closeTime.toString(),
                                isClosed = hours.isClosed,
                            )
                        }
                    )
                    it[menuPrice] = r.menuPrice
                    it[currency] = r.currency
                    it[todayHasMenu] = r.todayHasMenu
                    it[cuisineEmoji] = r.cuisineEmoji
                    it[cuisineType] = r.cuisineType
                }
            }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            mockMenus.values.forEach { m ->
                MenusTable.insert {
                    it[id] = m.id
                    it[restaurantId] = m.restaurantId
                    it[date] = today
                    it[price] = m.price
                    it[currency] = m.currency
                    it[firstsJson] = json.encodeToString(m.firsts.map { d -> d.name })
                    it[secondsJson] = json.encodeToString(m.seconds.map { d -> d.name })
                    it[dessertsJson] = json.encodeToString(m.desserts.map { d -> d.name })
                    it[notes] = m.notes
                }
            }
        }
    }
}
