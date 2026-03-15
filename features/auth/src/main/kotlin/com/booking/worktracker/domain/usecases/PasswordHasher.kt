package com.booking.worktracker.domain.usecases

import com.booking.worktracker.di.Singleton
import me.tatarka.inject.annotations.Inject
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Inject
@Singleton
class PasswordHasher {
    private val iterations = 65536
    private val keyLength = 256
    private val algorithm = "PBKDF2WithHmacSHA256"

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance(algorithm)
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val actualHash = hashPassword(password, salt)
        return actualHash == expectedHash
    }
}
