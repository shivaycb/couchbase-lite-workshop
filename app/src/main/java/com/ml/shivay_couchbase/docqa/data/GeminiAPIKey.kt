package com.ml.shivay_couchbase.docqa.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiAPIKey @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val securedSharedPrefFileName = "secret_shared_prefs"
    private val apiKeySharedPrefKey = "gemini_api_key"

    /*
    Using encrypted SharedPreferences to store the Gemini API key
    See https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
     */
    private val masterKey: MasterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val sharedPreferences: SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            securedSharedPrefFileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    fun saveAPIKey(apiKey: String) {
        sharedPreferences.edit().putString(apiKeySharedPrefKey, apiKey).apply()
    }

    fun getAPIKey(): String? = sharedPreferences.getString(apiKeySharedPrefKey, null)
}

