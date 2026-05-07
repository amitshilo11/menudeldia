package com.amitshilo.menudeldia

import com.amitshilo.menudeldia.db.DatabaseFactory
import com.amitshilo.menudeldia.db.Seeder
import com.amitshilo.menudeldia.routes.configureRestaurantRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

val appJson = Json {
    prettyPrint = false
    isLenient = true
    ignoreUnknownKeys = true
}

fun main() {
    DatabaseFactory.init()
    Seeder.seedIfEmpty(appJson)
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(appJson)
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    install(StatusPages)
    install(CallLogging)

    routing {
        get("/api/v1/health") {
            call.respondText("OK")
        }
    }
    configureRestaurantRoutes(appJson)
}
