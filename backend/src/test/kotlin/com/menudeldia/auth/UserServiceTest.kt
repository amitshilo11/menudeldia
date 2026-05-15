package com.menudeldia.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class UserServiceTest {

    private lateinit var repo: UserRepository
    private lateinit var svc: UserService

    @BeforeEach
    fun setUp() {
        repo = mock(UserRepository::class.java)
        `when`(repo.save(any(User::class.java))).thenAnswer { it.getArgument(0) }
        svc = UserService(repo)
    }

    @Test
    fun `creates new user on first sign-in`() {
        `when`(repo.findByProviderAndExternalId("google", "sub123")).thenReturn(null)
        val claims = VerifiedClaims("sub123", "a@b.com", "Alice", "https://pic")
        val user = svc.upsertFromIdToken("google", claims)
        assertEquals("google", user.provider)
        assertEquals("sub123", user.externalId)
        assertEquals("a@b.com", user.email)
        assertEquals("Alice", user.displayName)
        assertNotNull(user.lastLogin)
    }

    @Test
    fun `updates lastLogin on subsequent sign-in`() {
        val existing =
            User(provider = "apple", externalId = "sub456", email = "x@y.com", displayName = "Bob")
        `when`(repo.findByProviderAndExternalId("apple", "sub456")).thenReturn(existing)
        val claims = VerifiedClaims("sub456", null, null, null)
        val user = svc.upsertFromIdToken("apple", claims)
        assertNotNull(user.lastLogin)
        // name/email preserved when claims are null
        assertEquals("x@y.com", user.email)
        assertEquals("Bob", user.displayName)
    }

    @Test
    fun `does not overwrite existing name and email with null`() {
        val existing = User(
            provider = "apple",
            externalId = "sub789",
            email = "keep@me.com",
            displayName = "Keep"
        )
        `when`(repo.findByProviderAndExternalId("apple", "sub789")).thenReturn(existing)
        svc.upsertFromIdToken("apple", VerifiedClaims("sub789", null, null, null))
        assertEquals("keep@me.com", existing.email)
        assertEquals("Keep", existing.displayName)
    }

    @Test
    fun `byId throws on missing user`() {
        val id = UUID.randomUUID()
        `when`(repo.findById(id)).thenReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { svc.byId(id) }
    }
}
