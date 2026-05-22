package com.menudeldia.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/** Authenticated end-user. One row per (provider, externalId) pair. */
@Entity
@Table(name = "users")
class User(
    @Id
    val id: UUID = UUID.randomUUID(),

    /** "google" or "apple". */
    @Column(nullable = false)
    var provider: String,

    /** The `sub` claim from the verified ID token. */
    @Column(name = "external_id", nullable = false)
    var externalId: String,

    var email: String? = null,

    @Column(name = "display_name")
    var displayName: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "last_login")
    var lastLogin: Instant? = null,
)
