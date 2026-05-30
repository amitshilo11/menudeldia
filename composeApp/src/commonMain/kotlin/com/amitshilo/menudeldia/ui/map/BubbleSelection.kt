package com.amitshilo.menudeldia.ui.map

import com.amitshilo.menudeldia.domain.model.Restaurant

internal fun pickBubbleIds(
    restaurants: List<Restaurant>,
    selectedId: String?,
    screenPosition: (Restaurant) -> Pair<Float, Float>?,
    collisionPxSq: Float,
): Set<String> {
    val sorted = restaurants.sortedWith(
        compareByDescending<Restaurant> { it.id == selectedId }
            .thenByDescending { it.todayHasMenu }
            .thenBy { it.id },
    )
    val claimed = mutableListOf<Pair<Float, Float>>()
    val result = mutableSetOf<String>()
    for (r in sorted) {
        val (x, y) = screenPosition(r) ?: continue
        val overlaps = claimed.any { (bx, by) ->
            val dx = x - bx
            val dy = y - by
            dx * dx + dy * dy < collisionPxSq
        }
        if (!overlaps) {
            claimed += Pair(x, y)
            result += r.id
        }
    }
    return result
}
