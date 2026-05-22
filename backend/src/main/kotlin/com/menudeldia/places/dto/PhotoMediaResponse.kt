package com.menudeldia.places.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PhotoMediaResponse(
    val name: String,
    val photoUri: String,
)
