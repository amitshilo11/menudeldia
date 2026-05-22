package com.menudeldia.auth

import com.menudeldia.auth.dto.SignInRequest
import com.menudeldia.auth.dto.SignInResponse
import com.menudeldia.auth.dto.UserDto
import com.menudeldia.common.ApiPaths
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiPaths.V1}/auth")
class AuthController(
    private val google: GoogleIdTokenVerifierBean,
    private val apple: AppleIdTokenVerifier,
    private val users: UserService,
    private val jwt: JwtService,
) {

    @PostMapping("/google")
    fun signInWithGoogle(@Valid @RequestBody body: SignInRequest): SignInResponse {
        val claims = google.verify(body.idToken)
        val user = users.upsertFromIdToken("google", claims)
        return SignInResponse(accessToken = jwt.issue(user.id), user = user.toDto())
    }

    @PostMapping("/apple")
    fun signInWithApple(@Valid @RequestBody body: SignInRequest): SignInResponse {
        val claims = apple.verify(body.idToken, body.nonce)
        val user = users.upsertFromIdToken("apple", claims)
        return SignInResponse(accessToken = jwt.issue(user.id), user = user.toDto())
    }
}

fun User.toDto() = UserDto(id = id, email = email, displayName = displayName, avatarUrl = avatarUrl)
