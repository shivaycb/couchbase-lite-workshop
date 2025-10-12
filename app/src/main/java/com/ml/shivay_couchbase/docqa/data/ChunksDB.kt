 package com.ml.couchbase.docqa.data

import com.couchbase.lite.*
import com.couchbase.lite.VectorEncoding
import com.couchbase.lite.VectorIndexConfiguration

class ChunksDB {

    private val database: Database = DatabaseManager.getDatabase()
    private val INDEX_NAME = "chunk_embedding_index"

    init {
        // Create an index if it doesn't exist
        createVectorIndex()
    }

    private fun createVectorIndex() {
        // Create a vector index on chunkEmbedding field with VectorIndexConfiguration
        val config = VectorIndexConfiguration("chunkEmbedding", 384, 3) // assuming embedding size of 128
        config.encoding = VectorEncoding.none()  // Customize based on how you encoded vectors (none, normalized, etc.)
        database.createIndex(INDEX_NAME, config)
    }

    fun addChunk(chunk: Chunk) {
        val mutableDoc = MutableDocument()
        mutableDoc.setString("docFileName", chunk.docFileName)
        mutableDoc.setString("chunkData", chunk.chunkData)
        mutableDoc.setArray("chunkEmbedding", MutableArray().apply {
            chunk.chunkEmbedding.forEach { addFloat(it) }
        })
        database.save(mutableDoc)
    }

fun getSimilarChunks(queryEmbedding: FloatArray, n: Int = 1): List<Pair<Float, Chunk>> {
    // SQL query to find similar chunks using the vector index
    val sql = """
    SELECT docFileName, chunkData, chunkEmbedding, APPROX_VECTOR_DISTANCE(chunkEmbedding, ${'$'}embeddingArray ) as distance
    FROM ${database.name}
    ORDER BY distance
    LIMIT $n
    """
    val query = database.createQuery(sql)

    // Convert FloatArray to a MutableArray for embedding
    val embeddingArray = MutableArray().apply {
        queryEmbedding.forEach { addFloat(it) }
    }

    // Set the query parameter using positional binding
    query.parameters = Parameters().apply {
        setArray("embeddingArray", embeddingArray)  // Bind the vector embedding to the first positional parameter (?)
    }

    // Execute the query and gather results
    val chunksWithScores = mutableListOf<Pair<Float, Chunk>>()
    query.execute().use { resultSet ->
        for (result in resultSet) {
            val docFileName = result.getString("docFileName") ?: ""
            val chunkData = result.getString("chunkData") ?: ""
            val chunkEmbeddingArray = result.getArray("chunkEmbedding") ?: MutableArray()
            val distance = result.getFloat("distance")

            val chunkEmbedding = FloatArray(chunkEmbeddingArray.count()) { i ->
                chunkEmbeddingArray.getFloat(i)
            }

            val chunk = Chunk(
                chunkId = 0,  // Handle chunk ID as per your needs
                docFileName = docFileName,
                chunkData = chunkData,
                chunkEmbedding = chunkEmbedding
            )
            chunksWithScores.add(Pair(distance, chunk))
        }
    }
    return chunksWithScores
}



    fun removeChunks(docId: Long) {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("docId").equalTo(Expression.longValue(docId)))

        val result = query.execute()
        result.forEach { row ->
            val docId = row.getString("id") ?: return@forEach
            val doc = database.getDocument(docId)
            doc?.let { database.delete(it) }
        }
    }
}



// //package com.ml.couchbase.docqa.data

// import com.couchbase.lite.*
// import com.couchbase.lite.VectorEncoding
// import com.couchbase.lite.VectorIndexConfiguration

// class ChunksDB {
//     private val database: Database = DatabaseManager.getDatabase()

//     fun addChunk(chunk: Chunk) {
//         // Placeholder implementation
//     }

//     fun getSimilarChunks(queryEmbedding: FloatArray, n: Int = 5): List<Pair<Float, Chunk>> {
//         // Placeholder implementation
//         return List(n) { index ->
//             Pair(0.5f, Chunk(chunkId = index.toLong(), docFileName = "mock_doc.pdf", chunkData = "Mock chunk data"))
//         }
//     }

//     fun removeChunks(docId: Long) {
//         // Placeholder implementation
//     }
// }

































