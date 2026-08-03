package com.amitshilo.menudeldia.ui.theme

import platform.UIKit.UIColor

fun primaryUIColor(): UIColor = UIColor(
    red = PrimaryColor.red.toDouble(),
    green = PrimaryColor.green.toDouble(),
    blue = PrimaryColor.blue.toDouble(),
    alpha = PrimaryColor.alpha.toDouble(),
)
