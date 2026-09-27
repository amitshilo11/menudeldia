package com.amitshilo.menudeldia.util

/** Outbound links surfaced in the UI. Kept in one place so they can't drift between screens. */
object AppLinks {
    const val PRIVACY_POLICY = "https://menudiz.duckdns.org/privacy.html"
    const val SUPPORT = "https://menudiz.duckdns.org/support.html"

    /**
     * Apple's standard EULA — the terms an App Store app falls back to when the developer
     * supplies none. Swap for our own terms page once one exists.
     */
    const val TERMS = "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/"

    const val FEEDBACK_EMAIL = "alssamit@gmail.com"

    fun feedbackMailto(subject: String) =
        "mailto:$FEEDBACK_EMAIL?subject=${subject.encodeForMailto()}"

    /** Minimal percent-encoding — enough for the fixed subjects we pass in. */
    private fun String.encodeForMailto() = buildString {
        this@encodeForMailto.encodeToByteArray().forEach { byte ->
            val char = byte.toInt().toChar()
            if (char.isLetterOrDigit() || char in "-_.~") append(char)
            else append('%').append(
                byte.toInt().and(0xFF).toString(16).uppercase().padStart(2, '0')
            )
        }
    }
}
