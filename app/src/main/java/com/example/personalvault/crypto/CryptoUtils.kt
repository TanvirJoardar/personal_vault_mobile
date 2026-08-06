package com.example.personalvault.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    private const val PBKDF2_ITERATIONS = 50_000
    private const val SALT_BYTES = 32
    private const val IV_BYTES = 12
    private const val KEY_BITS = 256
    private const val GCM_TAG_LENGTH_BITS = 128

    private val random = SecureRandom()

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return salt
    }

    fun generateIV(): ByteArray {
        val iv = ByteArray(IV_BYTES)
        random.nextBytes(iv)
        return iv
    }

    fun generateVaultKey(): ByteArray {
        val key = ByteArray(32)
        random.nextBytes(key)
        return key
    }

    fun toBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun fromBase64(base64Str: String): ByteArray {
        return Base64.decode(base64Str, Base64.NO_WRAP)
    }

    fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptData(key: SecretKey, plaintext: String): Pair<String, String> {
        val iv = generateIV()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Pair(toBase64(ciphertext), toBase64(iv))
    }

    fun decryptData(key: SecretKey, ciphertextB64: String, ivB64: String): String {
        val ciphertext = fromBase64(ciphertextB64)
        val iv = fromBase64(ivB64)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    fun encryptVaultKey(vaultKeyRaw: ByteArray, wrappingKey: SecretKey): Pair<String, String> {
        val iv = generateIV()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey, gcmSpec)
        val encrypted = cipher.doFinal(vaultKeyRaw)
        return Pair(toBase64(encrypted), toBase64(iv))
    }

    fun decryptVaultKey(encryptedVKB64: String, ivB64: String, wrappingKey: SecretKey): SecretKey {
        val encryptedVK = fromBase64(encryptedVKB64)
        val iv = fromBase64(ivB64)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey, gcmSpec)
        val rawKey = cipher.doFinal(encryptedVK)
        return SecretKeySpec(rawKey, "AES")
    }

    fun generateRecoveryPhrase(): String {
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val hex = bytes.joinToString("") { "%02X".format(it) }
        return hex.chunked(4).joinToString("-")
    }

    fun generatePassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        var charPool = ""
        val required = mutableListOf<Char>()

        if (includeUpper) {
            charPool += upper
            required.add(upper[random.nextInt(upper.length)])
        }
        if (includeLower) {
            charPool += lower
            required.add(lower[random.nextInt(lower.length)])
        }
        if (includeNumbers) {
            charPool += numbers
            required.add(numbers[random.nextInt(numbers.length)])
        }
        if (includeSymbols) {
            charPool += symbols
            required.add(symbols[random.nextInt(symbols.length)])
        }

        if (charPool.isEmpty()) charPool = lower + numbers

        val result = mutableListOf<Char>()
        result.addAll(required)

        while (result.size < length) {
            result.add(charPool[random.nextInt(charPool.length)])
        }

        result.shuffle(random)
        return result.joinToString("")
    }
}
