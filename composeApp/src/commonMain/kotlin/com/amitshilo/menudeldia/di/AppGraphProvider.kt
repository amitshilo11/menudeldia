package com.amitshilo.menudeldia.di

import com.amitshilo.menudeldia.di.AppGraph
import dev.zacsweers.metro.createGraph

object AppGraphProvider {
    val appGraph: AppGraph by lazy { createGraph<AppGraph>() }
}
