package com.ml.couchbase.docqa.domain.embeddings

import android.content.Context
import com.ml.shubham0204.sentence_embeddings.SentenceEmbedding
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SentenceEmbeddingProvider(private val context: Context) {

    private val sentenceEmbedding = SentenceEmbedding()

    init {
        val modelPath = copyModelToLocalStorage()
        val tokenizerBytes = context.assets.open("tokenizer.json").readBytes()
        runBlocking(Dispatchers.IO) { 
            sentenceEmbedding.init(
                modelPath, 
                tokenizerBytes,
                useTokenTypeIds = false,
                outputTensorName = "token_embeddings",
                normalizeEmbeddings = false
            ) 
        }
    }

    fun encode(text: String): FloatArray =
        runBlocking(Dispatchers.Default) {
            return@runBlocking sentenceEmbedding.encode(text)
        }
        
    fun encodeText(text: String): FloatArray = encode(text)

    private fun copyModelToLocalStorage(): String {
        val modelBytes = context.assets.open("all-MiniLM-L6-V2.onnx").readBytes()
        val storageFile = File(context.filesDir, "all-MiniLM-L6-V2.onnx")
        if (!storageFile.exists()) {
            storageFile.writeBytes(modelBytes)
        }
        return storageFile.absolutePath
    }
}
