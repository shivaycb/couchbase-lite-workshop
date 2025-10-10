// This file is kept for backwards compatibility but is not actively used
// The new ChatViewModel handles question answering directly using LLM abstraction

package com.ml.couchbase.docqa.domain

import android.util.Log
import com.ml.couchbase.docqa.data.QueryResult
import com.ml.couchbase.docqa.data.RetrievedContext
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
) {

    fun getAnswer(query: String, prompt: String, onResponse: ((QueryResult) -> Unit)) {
        // Real implementation from exercise4
        var jointContext = ""
        val retrievedContextList = ArrayList<RetrievedContext>()
        chunksUseCase.getSimilarChunks(query, n = 5).forEach {
            jointContext += " " + it.second.chunkData
            retrievedContextList.add(RetrievedContext(it.second.docFileName, it.second.chunkData))
        }
        Log.e("APP", "Context: $jointContext")
        val inputPrompt = prompt.replace("\$CONTEXT", jointContext).replace("\$QUERY", query)
        
        // Note: This requires GeminiRemoteAPI but we're using the new architecture
        // This method is kept for backwards compatibility but not actively used
        CoroutineScope(Dispatchers.IO).launch {
            val mockResponse = "Please use the new ChatViewModel with LLM support"
            onResponse(QueryResult(mockResponse, retrievedContextList))
        }
    }

    fun canGenerateAnswers(): Boolean {
        return documentsUseCase.getDocsCount() > 0
    }
}
