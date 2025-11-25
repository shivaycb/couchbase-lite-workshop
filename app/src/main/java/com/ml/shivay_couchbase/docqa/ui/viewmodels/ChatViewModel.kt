package com.ml.shivay_couchbase.docqa.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ml.shivay_couchbase.docqa.ChatNavEvent
import com.ml.couchbase.docqa.data.RetrievedContext
import com.ml.shivay_couchbase.docqa.data.GeminiAPIKey
import com.ml.couchbase.docqa.domain.ChunksUseCase
import com.ml.couchbase.docqa.domain.DocumentsUseCase
import com.ml.couchbase.docqa.domain.embeddings.SentenceEmbeddingProvider
import com.ml.shivay_couchbase.docqa.domain.llm.GeminiRemoteAPI
import com.ml.shivay_couchbase.docqa.domain.llm.LLMInferenceAPI
import com.ml.shivay_couchbase.docqa.ui.components.createAlertDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatScreenUIEvent {
    data object OnEditCredentialsClick : ChatScreenUIEvent

    data object OnOpenDocsClick : ChatScreenUIEvent

    data object OnLocalModelsClick : ChatScreenUIEvent

    sealed class ResponseGeneration {
        data class Start(
            val query: String,
            val prompt: String,
        ) : ChatScreenUIEvent

        data class StopWithSuccess(
            val response: String,
            val retrievedContextList: List<RetrievedContext>,
        ) : ChatScreenUIEvent

        data class StopWithError(
            val errorMessage: String,
        ) : ChatScreenUIEvent
    }
}

data class ChatScreenUIState(
    val question: String = "",
    val response: String = "",
    val isGeneratingResponse: Boolean = false,
    val retrievedContextList: List<RetrievedContext> = emptyList(),
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentsUseCase: DocumentsUseCase,
    private val chunksUseCase: ChunksUseCase,
    private val geminiAPIKey: GeminiAPIKey,
    private val sentenceEncoder: SentenceEmbeddingProvider,
    private val llmInferenceAPI: LLMInferenceAPI,
) : ViewModel() {
    private val _chatScreenUIState = MutableStateFlow(ChatScreenUIState())
    val chatScreenUIState: StateFlow<ChatScreenUIState> = _chatScreenUIState

    private val _navEventChannel = Channel<ChatNavEvent>()
    val navEventChannel = _navEventChannel.receiveAsFlow()

    fun onChatScreenEvent(event: ChatScreenUIEvent) {
        when (event) {
            is ChatScreenUIEvent.ResponseGeneration.Start -> {
                if (!checkNumDocuments()) {
                    Toast
                        .makeText(
                            context,
                            "Add documents to execute queries",
                            Toast.LENGTH_LONG,
                        ).show()
                    return
                }
                if (!checkValidAPIKey() && !llmInferenceAPI.isLoaded) {
                    createAlertDialog(
                        dialogTitle = "Invalid API Key",
                        dialogText = "Please enter a Gemini API key or load a local model to use for generating responses.",
                        dialogPositiveButtonText = "Add API key",
                        onPositiveButtonClick = {
                            onChatScreenEvent(ChatScreenUIEvent.OnEditCredentialsClick)
                        },
                        dialogNegativeButtonText = "Open Gemini Console",
                        onNegativeButtonClick = {
                            Intent(Intent.ACTION_VIEW).apply {
                                data = "https://aistudio.google.com/apikey".toUri()
                                context.startActivity(this)
                            }
                        },
                    )
                    return
                }
                if (event.query.trim().isEmpty()) {
                    Toast
                        .makeText(context, "Enter a query to execute", Toast.LENGTH_LONG)
                        .show()
                    return
                }
                _chatScreenUIState.value =
                    _chatScreenUIState.value.copy(isGeneratingResponse = true)
                _chatScreenUIState.value =
                    _chatScreenUIState.value.copy(question = event.query)

                val llm =
                    if (llmInferenceAPI.isLoaded) {
                        Toast.makeText(context, "Using local model...", Toast.LENGTH_LONG).show()
                        llmInferenceAPI
                    } else {
                        val apiKey = geminiAPIKey.getAPIKey() ?: throw Exception("Gemini API key is null")
                        Toast.makeText(context, "Using Gemini cloud model...", Toast.LENGTH_LONG).show()
                        GeminiRemoteAPI(apiKey)
                    }
                getAnswer(llm, event.query, event.prompt)
            }

            is ChatScreenUIEvent.ResponseGeneration.StopWithSuccess -> {
                _chatScreenUIState.value =
                    _chatScreenUIState.value.copy(isGeneratingResponse = false)
                _chatScreenUIState.value = _chatScreenUIState.value.copy(response = event.response)
                _chatScreenUIState.value =
                    _chatScreenUIState.value.copy(retrievedContextList = event.retrievedContextList)
            }

            is ChatScreenUIEvent.ResponseGeneration.StopWithError -> {
                _chatScreenUIState.value =
                    _chatScreenUIState.value.copy(isGeneratingResponse = false)
                _chatScreenUIState.value = _chatScreenUIState.value.copy(question = "")
            }

            is ChatScreenUIEvent.OnOpenDocsClick -> {
                viewModelScope.launch {
                    _navEventChannel.send(ChatNavEvent.ToDocsScreen)
                }
            }

            is ChatScreenUIEvent.OnEditCredentialsClick -> {
                viewModelScope.launch {
                    _navEventChannel.send(ChatNavEvent.ToEditAPIKeyScreen)
                }
            }

            is ChatScreenUIEvent.OnLocalModelsClick -> {
                viewModelScope.launch {
                    _navEventChannel.send(ChatNavEvent.ToLocalModelsScreen)
                }
            }
        }
    }

    private fun getAnswer(
        llm: LLMInferenceAPI,
        query: String,
        prompt: String,
    ) {
        try {
            var jointContext = ""
            val retrievedContextList = ArrayList<RetrievedContext>()
            chunksUseCase.getSimilarChunks(query, n = 5).forEach {
                jointContext += " " + it.second.chunkData
                retrievedContextList.add(
                    RetrievedContext(
                        it.second.docFileName,
                        it.second.chunkData,
                    ),
                )
            }
            val inputPrompt = prompt.replace("\$CONTEXT", jointContext).replace("\$QUERY", query)
            CoroutineScope(Dispatchers.IO).launch {
                llm.getResponse(inputPrompt)?.let { llmResponse ->
                    onChatScreenEvent(
                        ChatScreenUIEvent.ResponseGeneration.StopWithSuccess(
                            llmResponse,
                            retrievedContextList,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            onChatScreenEvent(ChatScreenUIEvent.ResponseGeneration.StopWithError(e.message ?: ""))
            throw e
        }
    }

    fun checkNumDocuments(): Boolean = documentsUseCase.getDocsCount() > 0

    fun checkValidAPIKey(): Boolean = geminiAPIKey.getAPIKey() != null
}
