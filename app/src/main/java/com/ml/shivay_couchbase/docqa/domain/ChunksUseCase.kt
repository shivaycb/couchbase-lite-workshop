package com.ml.couchbase.docqa.domain

import android.util.Log
import com.ml.couchbase.docqa.data.Chunk
import com.ml.couchbase.docqa.data.ChunksDB
import com.ml.couchbase.docqa.domain.embeddings.SentenceEmbeddingProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChunksUseCase
@Inject
constructor(private val chunksDB: ChunksDB, private val sentenceEncoder: SentenceEmbeddingProvider) {

    fun addChunk(docId: String, docFileName: String, chunkText: String) {
        val embedding = sentenceEncoder.encodeText(chunkText)
        Log.e("APP", "Embedding dims ${embedding.size}")
        chunksDB.addChunk(
            Chunk(
                docId = docId,
                docFileName = docFileName,
                chunkData = chunkText,
                chunkEmbedding = embedding
            )
        )
    }

    fun removeChunks(docId: Long) {
        chunksDB.removeChunks(docId)
    }

    fun getSimilarChunks(query: String, n: Int = 5): List<Pair<Float, Chunk>> {
        return try {
            Log.e("APP", "Encoding query text: $query")
            val queryEmbedding = sentenceEncoder.encodeText(query)
            Log.e("APP", "Query embedding generated, size: ${queryEmbedding.size}")
            val results = chunksDB.getSimilarChunks(queryEmbedding, n)
            Log.e("APP", "Found ${results.size} similar chunks")
            results
        } catch (e: Exception) {
            Log.e("APP", "Error in getSimilarChunks: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
}


// package com.ml.couchbase.docqa.domain

// import com.ml.couchbase.docqa.data.Chunk
// import com.ml.couchbase.docqa.data.ChunksDB
// import javax.inject.Inject
// import javax.inject.Singleton

// @Singleton
// class ChunksUseCase @Inject constructor(private val chunksDB: ChunksDB) {
//     fun addChunk(docId: String, docFileName: String, chunkText: String) {
//         // Placeholder implementation
//     }

//     fun removeChunks(docId: Long) {
//         // Placeholder implementation
//     }

//     fun getSimilarChunks(query: String, n: Int = 5): List<Pair<Float, Chunk>> {
//         // Placeholder implementation
//         return List(n) { index ->
//             Pair(0.5f, Chunk(chunkId = index.toLong(), docFileName = "mock_doc.pdf", chunkData = "Mock chunk data"))
//         }
//     }
// }