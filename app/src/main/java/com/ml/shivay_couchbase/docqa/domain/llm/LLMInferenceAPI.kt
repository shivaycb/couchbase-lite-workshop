package com.ml.shivay_couchbase.docqa.domain.llm

abstract class LLMInferenceAPI {
    abstract suspend fun getResponse(prompt: String): String?
}

