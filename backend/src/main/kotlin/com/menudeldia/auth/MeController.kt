package com.menudeldia.auth

import com.menudeldia.auth.dto.UserDto
import com.menudeldia.common.ApiPaths
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiPaths.V1}/me")
class MeController {

    @GetMapping
    fun me(@AuthenticationPrincipal user: User): UserDto = user.toDto()
}
