package com.menudeldia.auth

import com.menudeldia.auth.dto.UserDto
import com.menudeldia.common.ApiPaths
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Authenticated user profile. */
@RestController
@RequestMapping("${ApiPaths.V1}/me")
class MeController {

    @GetMapping
    fun me(@AuthenticationPrincipal user: User): UserDto {
        // TODO B3.3.7: map User -> UserDto.
        TODO("Phase 3 — task B3.3.7")
    }
}
