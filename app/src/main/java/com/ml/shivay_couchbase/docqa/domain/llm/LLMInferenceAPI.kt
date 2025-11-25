package com.ml.shivay_couchbase.docqa.domain.llm

import android.content.Context

abstract class LLMInferenceAPI {
    abstract var isLoaded: Boolean
    abstract var loadedModelPath: String?

    abstract fun load(
        context: Context,
        modelPath: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    abstract suspend fun getResponse(prompt: String): String?
    
    abstract fun unload()
}

