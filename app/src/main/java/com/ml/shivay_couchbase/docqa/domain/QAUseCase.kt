package com.ml.couchbase.docqa.domain

import android.util.Log
import com.ml.couchbase.docqa.data.QueryResult
import com.ml.couchbase.docqa.data.RetrievedContext
import com.ml.couchbase.docqa.domain.llm.GeminiRemoteAPI
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class QAUseCase
@Inject
constructor(
    private val documentsUseCase: DocumentsUseCase,
    private val chunksUseCase: ChunksUseCase,
    private val geminiRemoteAPI: GeminiRemoteAPI
) {

    fun getAnswer(query: String, prompt: String, onResponse: ((QueryResult) -> Unit)) {
        // Move all work to background thread to avoid blocking the main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.e("APP", "Starting RAG query: $query")
                var jointContext = ""
                val retrievedContextList = ArrayList<RetrievedContext>()
                
                Log.e("APP", "Retrieving similar chunks...")
                chunksUseCase.getSimilarChunks(query, n = 5).forEach {
                    jointContext += " " + it.second.chunkData
                    retrievedContextList.add(RetrievedContext(it.second.docFileName, it.second.chunkData))
                }
                Log.e("APP", "Context retrieved, found ${retrievedContextList.size} chunks")
                
                val inputPrompt = prompt.replace("\$CONTEXT", jointContext).replace("\$QUERY", query)
                Log.e("APP", "Calling Gemini API...")
                
                val llmResponse = geminiRemoteAPI.getResponse(inputPrompt)
                
                if (llmResponse != null) {
                    Log.e("APP", "Response received from Gemini")
                    // Switch to Main dispatcher before updating UI state
                    CoroutineScope(Dispatchers.Main).launch {
                        onResponse(QueryResult(llmResponse, retrievedContextList))
                    }
                } else {
                    Log.e("APP", "Gemini API returned null response")
                    // Notify UI with error message
                    CoroutineScope(Dispatchers.Main).launch {
                        onResponse(QueryResult("Error: Unable to get response from AI. Please check your API key and internet connection.", retrievedContextList))
                    }
                }
            } catch (e: Exception) {
                Log.e("APP", "Error in getAnswer: ${e.message}", e)
                e.printStackTrace()
                // Notify UI about the error instead of leaving it in loading state
                CoroutineScope(Dispatchers.Main).launch {
                    onResponse(QueryResult("Error: ${e.message ?: "Unknown error occurred"}", emptyList()))
                }
            }
        }
    }

    fun canGenerateAnswers(): Boolean {
        return documentsUseCase.getDocsCount() > 0
    }
}


// package com.ml.couchbase.docqa.domain

// import com.ml.couchbase.docqa.data.QueryResult
// import com.ml.couchbase.docqa.data.RetrievedContext
// import com.ml.couchbase.docqa.domain.llm.GeminiRemoteAPI
// import javax.inject.Inject
// import javax.inject.Singleton
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.launch

// @Singleton
// class QAUseCase @Inject constructor(
//     private val documentsUseCase: DocumentsUseCase,
//     private val chunksUseCase: ChunksUseCase,
//     private val geminiRemoteAPI: GeminiRemoteAPI
// ) {
//     fun getAnswer(query: String, prompt: String, onResponse: ((QueryResult) -> Unit)) {
//         // Placeholder implementation
//         CoroutineScope(Dispatchers.IO).launch {
//             val mockResponse = "This is a mock answer to the query: $query"
//             val mockContext = List(2) { RetrievedContext("mock_doc.pdf", "Mock context") }
//             onResponse(QueryResult(mockResponse, mockContext))
//         }
//     }

//     fun canGenerateAnswers(): Boolean = true
// }