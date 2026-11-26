package com.nexus.app.core.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the session encrypted with an Android Keystore AES-GCM key.
 * Format is identical to NEXUS 1.x: SharedPreferences `nexus_secure_session` / `encrypted_session`,
 * Base64(NO_WRAP) of IV(12) + ciphertext+tag, Keystore alias `nexus.session.aes`.
 * The key and the stored value are discarded only when the data is provably unusable (bad tag, bad format,
 * permanently invalidated key); transient Keystore errors leave everything in place.
 */
class SecureSessionStore(context: Context) : SessionStorage {
    private val preferences =
        context.applicationContext.getSharedPreferences("nexus_secure_session", Context.MODE_PRIVATE)

    override fun save(value: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val encoded = Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
            preferences.edit().putString(SESSION_KEY, encoded).commit()
        } catch (e: Exception) {
            throw IllegalStateException("تعذّر حفظ جلسة الدخول بأمان", e)
        }
    }

    override fun read(): String? {
        val encoded = preferences.getString(SESSION_KEY, null) ?: return null
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            require(bytes.size > IV_LENGTH) { "Invalid encrypted session" }
            val iv = bytes.copyOfRange(0, IV_LENGTH)
            val payload = bytes.copyOfRange(IV_LENGTH, bytes.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
            String(cipher.doFinal(payload), Charsets.UTF_8)
        } catch (e: AEADBadTagException) {
            clear()
            null
        } catch (e: IllegalArgumentException) {
            clear()
            null
        } catch (e: KeyPermanentlyInvalidatedException) {
            clear()
            null
        } catch (e: UnrecoverableKeyException) {
            clear()
            null
        } catch (e: Exception) {
            // Transient Keystore/provider failure: report "no session" for now but keep the stored data and key.
            null
        }
    }

    /** Removes the stored session and the Keystore key (sign out, or corrupted data). */
    override fun clear() {
        preferences.edit().remove(SESSION_KEY).commit()
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) {
            // Best effort.
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "nexus.session.aes"
        const val SESSION_KEY = "encrypted_session"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_BITS = 128
    }
}

/** Plain SharedPreferences persistence of the short-lived web-auth state/nonce. */
class SharedPrefsAuthFlowStore(context: Context) : AuthFlowStore {
    private val preferences = context.applicationContext.getSharedPreferences("nexus_auth_flow", Context.MODE_PRIVATE)

    override fun save(flow: AuthFlow) {
        preferences.edit()
            .putString("state", flow.state)
            .putString("nonce", flow.nonce)
            .putLong("created_at", flow.createdAtMillis)
            .commit()
    }

    override fun read(): AuthFlow? {
        val state = preferences.getString("state", null) ?: return null
        val nonce = preferences.getString("nonce", null) ?: return null
        return AuthFlow(state, nonce, preferences.getLong("created_at", 0L))
    }

    override fun clear() {
        preferences.edit().clear().commit()
    }
}
