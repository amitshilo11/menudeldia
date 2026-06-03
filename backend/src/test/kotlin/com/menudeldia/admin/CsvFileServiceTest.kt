package com.menudeldia.admin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CsvFileServiceTest {

    private val svc = CsvFileService()

    private fun parse(csv: String) = svc.parseRows(csv.trimIndent().reader())

    @Test
    fun `parseRows maps all fields correctly`() {
        val rows = parse("""
            id,name,cuisine_type,price_normal,menu_details,Vegeterian options,Gluten free options,days_from,days_to,excluded_day,open_time,close_time,phone,website,google_maps_url,google_place_id
            1,MOSCADA,Mediterranean,18.80,Starter + Main + Dessert + Drink,Yes,No,Mon,Fri,,12:30,16:00,+34931018764,https://www.moscadabcn.com/,https://maps.app.goo.gl/abc,ChIJbzY6rRWjpBIRNVxSPbboqZA
        """)

        assertEquals(1, rows.size)
        val r = rows[0]
        assertEquals("MOSCADA", r.name)
        assertEquals("Mediterranean", r.cuisineType)
        assertEquals(BigDecimal("18.80"), r.menuPrice)
        assertEquals("Starter + Main + Dessert + Drink", r.menuDetailsRaw)
        assertTrue(r.vegetarianOptions)
        assertEquals(false, r.glutenFreeOptions)
        assertEquals("Mon", r.daysFrom)
        assertEquals("Fri", r.daysTo)
        assertNull(r.excludedDay)
        assertEquals("12:30", r.openTime)
        assertEquals("16:00", r.closeTime)
        assertEquals("+34931018764", r.phone)
        assertEquals("https://www.moscadabcn.com/", r.website)
        assertEquals("https://maps.app.goo.gl/abc", r.googleMapsUrl)
        assertEquals("ChIJbzY6rRWjpBIRNVxSPbboqZA", r.googlePlaceId)
    }

    @Test
    fun `parseRows skips rows with blank name`() {
        val rows = parse("""
            id,name,cuisine_type,price_normal,menu_details,Vegeterian options,Gluten free options,days_from,days_to,excluded_day,open_time,close_time,phone,website,google_maps_url,google_place_id
            1,Kemo,Asian,12.50,,No,No,Mon,Fri,,12:30,16:00,,,,
            2,  ,Asian,12.50,,No,No,Mon,Fri,,12:30,16:00,,,,
            3,Valid,Spanish,10.00,,No,No,Mon,Fri,,13:00,15:30,,,,
        """)

        assertEquals(2, rows.size)
        assertEquals("Kemo", rows[0].name)
        assertEquals("Valid", rows[1].name)
    }

    @Test
    fun `parseRows treats blank optional fields as null`() {
        val rows = parse("""
            id,name,cuisine_type,price_normal,menu_details,Vegeterian options,Gluten free options,days_from,days_to,excluded_day,open_time,close_time,phone,website,google_maps_url,google_place_id
            1,Kemo,,,,,No,,,,,,,,,
        """)

        val r = rows[0]
        assertNull(r.cuisineType)
        assertNull(r.menuPrice)
        assertNull(r.menuDetailsRaw)
        assertNull(r.daysFrom)
        assertNull(r.googlePlaceId)
    }
}
