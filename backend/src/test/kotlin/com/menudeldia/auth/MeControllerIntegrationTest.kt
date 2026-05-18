package com.menudeldia.auth

import com.menudeldia.config.AppProperties
import com.menudeldia.config.CorsConfig
import com.menudeldia.config.SecurityConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Duration
import java.util.Optional
import java.util.UUID

@WebMvcTest(MeController::class)
@Import(
    SecurityConfig::class,
    CorsConfig::class,
    JwtAuthFilter::class,
    JwtService::class,
    JsonAuthenticationEntryPoint::class,
    MeControllerIntegrationTest.TestConfig::class,
)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource(
    properties = [
        "menudeldia.google.places-api-key=",
        "menudeldia.google.places-cache-ttl=PT1H",
        "menudeldia.google.places-refresh-batch-size=5",
        "menudeldia.auth.google-client-id=",
        "menudeldia.auth.apple-client-id=com.test.app",
        "menudeldia.auth.jwt-signing-key=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        "menudeldia.auth.jwt-ttl=P30D",
        "menudeldia.cors.allowed-origins=http://localhost:3000",
        "menudeldia.photos.storage-root=./var",
        "menudeldia.rate-limit.read-rpm=60",
        "menudeldia.rate-limit.auth-rpm=10",
        "menudeldia.admin-token=",
    ]
)
class MeControllerIntegrationTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun userRepository(): UserRepository = mock(UserRepository::class.java)

        @Bean
        fun userService(repo: UserRepository) = UserService(repo)
    }

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var jwtService: JwtService
    @Autowired
    lateinit var userRepository: UserRepository

    private val userId = UUID.randomUUID()
    private val user = User(
        id = userId,
        provider = "google",
        externalId = "sub-test",
        email = "test@example.com",
        displayName = "Test User",
    )

    @BeforeEach
    fun seedUser() {
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
    }

    @Test
    fun `GET me with valid token returns 200 and user`() {
        val token = jwtService.issue(userId)
        mockMvc.get("/api/v1/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(userId.toString()) }
            jsonPath("$.email") { value("test@example.com") }
            jsonPath("$.displayName") { value("Test User") }
        }
    }

    @Test
    fun `GET me with invalid token returns 401`() {
        mockMvc.get("/api/v1/me") {
            header("Authorization", "Bearer not-a-jwt")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error") { value("unauthorized") }
        }
    }

    @Test
    fun `GET me with expired token returns 401`() {
        val expiredJwt = JwtService(
            AppProperties(
                google = AppProperties.GoogleProps("", Duration.ofHours(1), 5),
                auth = AppProperties.AuthProps(
                    googleClientId = "",
                    appleClientId = "",
                    jwtSigningKey = "a".repeat(32),
                    jwtTtl = Duration.ofMillis(1),
                ),
                cors = AppProperties.CorsProps(listOf("http://localhost:3000")),
                photos = AppProperties.PhotoProps("./var"),
                rateLimit = AppProperties.RateLimitProps(60, 10),
            )
        ).issue(userId)
        Thread.sleep(10)

        mockMvc.get("/api/v1/me") {
            header("Authorization", "Bearer $expiredJwt")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error") { value("unauthorized") }
        }
    }

    @Test
    fun `GET me with no Authorization header returns 401`() {
        mockMvc.get("/api/v1/me").andExpect {
            status { isUnauthorized() }
            jsonPath("$.error") { value("unauthorized") }
        }
    }
}
