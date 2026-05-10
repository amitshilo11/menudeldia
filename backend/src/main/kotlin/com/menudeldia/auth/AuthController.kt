package com.menudeldia.auth

import com.menudeldia.auth.dto.SignInRequest
import com.menudeldia.auth.dto.SignInResponse
import com.menudeldia.common.ApiPaths
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Sign-in endpoints. Both verify the platform ID token, upsert a user, then issue our session JWT.
 * TODO B3.3.6: implement signInWithGoogle / signInWithApple.
 */
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
        // TODO: google.verify(body.idToken) -> claims -> users.upsert -> jwt.issue -> SignInResponse.
        TODO("Phase 3 — task B3.3.6")
    }

    @PostMapping("/apple")
    fun signInWithApple(@Valid @RequestBody body: SignInRequest): SignInResponse {
        // TODO: apple.verify(body.idToken) -> claims -> users.upsert -> jwt.issue -> SignInResponse.
        TODO("Phase 3 — task B3.3.6")
    }
}
