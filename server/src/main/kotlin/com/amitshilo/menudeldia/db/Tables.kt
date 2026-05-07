package com.amitshilo.menudeldia.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object RestaurantsTable : Table("restaurants") {
    val id = varchar("id", 36)
    val name = text("name")
    val address = text("address")
    val lat = double("lat")
    val lng = double("lng")
    val phone = text("phone").nullable()
    val thumbnailUrl = text("thumbnail_url").nullable()
    val photosJson = text("photos_json").default("[]")
    val descriptionEs = text("description_es").nullable()
    val descriptionEn = text("description_en").nullable()
    val openingHoursJson = text("opening_hours_json").default("[]")
    val menuPrice = double("menu_price").nullable()
    val currency = varchar("currency", 3).default("EUR")
    val todayHasMenu = bool("today_has_menu").default(false)
    val cuisineEmoji = text("cuisine_emoji").nullable()
    override val primaryKey = PrimaryKey(id)
}

object MenusTable : Table("menus") {
    val id = varchar("id", 36)
    val restaurantId = varchar("restaurant_id", 36).references(RestaurantsTable.id)
    val date = date("date")
    val price = double("price")
    val currency = varchar("currency", 3).default("EUR")
    val firstsJson = text("firsts_json").default("[]")
    val secondsJson = text("seconds_json").default("[]")
    val dessertsJson = text("desserts_json").default("[]")
    val notes = text("notes").nullable()
    override val primaryKey = PrimaryKey(id)
}
