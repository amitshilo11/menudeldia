package com.menudeldia

import com.menudeldia.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = [AppProperties::class])
class MenuDelDiaApplication

fun main(args: Array<String>) {
    runApplication<MenuDelDiaApplication>(*args)
}
