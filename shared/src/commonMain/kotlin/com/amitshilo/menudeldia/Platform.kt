package com.amitshilo.menudeldia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform