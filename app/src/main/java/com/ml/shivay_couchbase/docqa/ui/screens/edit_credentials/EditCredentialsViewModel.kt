package com.ml.shivay_couchbase.docqa.ui.screens.edit_credentials

import androidx.lifecycle.ViewModel
import com.ml.shivay_couchbase.docqa.data.GeminiAPIKey
import com.ml.shivay_couchbase.docqa.data.HFAccessToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditCredentialsViewModel @Inject constructor(
    private val geminiAPIKey: GeminiAPIKey,
    private val hfAccessToken: HFAccessToken,
) : ViewModel() {
    fun getGeminiAPIKey(): String? = geminiAPIKey.getAPIKey()

    fun saveGeminiAPIKey(apiKey: String) {
        geminiAPIKey.saveAPIKey(apiKey)
    }

    fun getHFAccessToken(): String? = hfAccessToken.getToken()

    fun saveHFAccessToken(accessToken: String) {
        hfAccessToken.saveToken(accessToken)
    }
}

