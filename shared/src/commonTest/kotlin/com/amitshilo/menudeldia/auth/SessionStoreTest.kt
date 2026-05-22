package com.amitshilo.menudeldia.auth

import com.amitshilo.menudeldia.data.auth.local.SessionStore
import com.amitshilo.menudeldia.domain.auth.model.AuthSession
import com.amitshilo.menudeldia.domain.auth.model.AuthUser
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionStoreTest {

    private fun store(settings: MapSettings = MapSettings()) = SessionStore(settings)

    private val session = AuthSession(
        accessToken = "tok",
        user = AuthUser("uid", "a@b.com", "Alice", null),
    )

    @Test
    fun `empty store has no session and no guest`() {
        val s = store()
        assertNull(s.sessionFlow.value)
        assertFalse(s.isGuestFlow.value)
    }

    @Test
    fun `save emits session`() {
        val s = store()
        s.save(session)
        assertEquals(session, s.sessionFlow.value)
        assertEquals("tok", s.currentToken())
    }

    @Test
    fun `clear removes session and token`() {
        val s = store()
        s.save(session)
        s.clear()
        assertNull(s.sessionFlow.value)
        assertNull(s.currentToken())
    }

    @Test
    fun `saveGuest sets guest flag and clears session`() {
        val s = store()
        s.save(session)
        s.saveGuest()
        assertTrue(s.isGuestFlow.value)
        assertNull(s.sessionFlow.value)
    }

    @Test
    fun `store survives re-init with same backing settings`() {
        val backing = MapSettings()
        store(backing).save(session)
        val s2 = store(backing)
        assertEquals("tok", s2.currentToken())
    }
}
