package com.amitshilo.menudeldia.domain.model

data class SearchFilterState(
    val query: String = "",
    val openNowOnly: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val cuisineType: String? = null,
    val maxDistanceMeters: Double? = null,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || openNowOnly || isVegan || isGlutenFree ||
                minPrice != null || maxPrice != null ||
                cuisineType != null || maxDistanceMeters != null

    val activeCount: Int
        get() = listOf(
            query.isNotBlank(),
            openNowOnly,
            isVegan,
            isGlutenFree,
            minPrice != null || maxPrice != null,
            cuisineType != null,
            maxDistanceMeters != null,
        ).count { it }
}
