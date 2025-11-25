package com.ml.shivay_couchbase.docqa.domain.llm

import android.content.Context
import android.util.Log
import com.ml.shivay_couchbase.docqa.ExecuTorchRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecuTorchAPI @Inject constructor() : LLMInferenceAPI() {
    private val runner = ExecuTorchRunner()
    override var isLoaded = false
    override var loadedModelPath: String? = null

    override fun load(
        context: Context,
        modelPath: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        try {
            val success = runner.initialize(modelPath)
            if (success) {
                isLoaded = true
                loadedModelPath = modelPath
                onSuccess()
            } else {
                throw RuntimeException("Failed to initialize ExecuTorch runner")
            }
        } catch (e: Exception) {
            Log.e("ExecuTorchAPI", "Failed to initialize ExecuTorch: ${e.message}", e)
            onError(e)
        }
    }

    override suspend fun getResponse(prompt: String): String? =
        withContext(Dispatchers.Default) {
            Log.d("ExecuTorchAPI", "Generating response for prompt: $prompt")
            try {
                runner.generate(prompt)
            } catch (e: Exception) {
                Log.e("ExecuTorchAPI", "Generation failed", e)
                null
            }
        }

    override fun unload() {
        runner.close()
        isLoaded = false
        loadedModelPath = null
    }
}
