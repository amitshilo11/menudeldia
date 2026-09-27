package com.menudeldia.auth

import com.menudeldia.auth.dto.UserDto
import com.menudeldia.common.ApiPaths
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiPaths.V1}/me")
class MeController(private val users: UserService) {

    @GetMapping
    fun me(@AuthenticationPrincipal user: User): UserDto = user.toDto()

    /**
     * Erases the account and every personal field attached to it. Required by App Store
     * guideline 5.1.1(v): signing out is not enough — the record itself has to go.
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMe(@AuthenticationPrincipal user: User) = users.deleteAccount(user)
}
