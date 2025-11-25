package com.ml.shivay_couchbase.docqa.ui.screens.local_models

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ketch.Ketch
import com.ketch.Status
import com.ml.shivay_couchbase.docqa.data.HFAccessToken
import com.ml.shivay_couchbase.docqa.data.LocalModel
import com.ml.shivay_couchbase.docqa.domain.llm.LLMInferenceAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

sealed class LocalModelsUIEvent {
    data class OnModelDownloadClick(
        val model: LocalModel,
    ) : LocalModelsUIEvent()

    data class OnUseModelClick(
        val model: LocalModel,
    ) : LocalModelsUIEvent()

    data object RefreshModelsList : LocalModelsUIEvent()
}

data class LocalModelsUIState(
    val models: List<LocalModel> = emptyList(),
    val downloadModelDialogState: DownloadModelDialogUIState = DownloadModelDialogUIState(),
)

data class DownloadModelDialogUIState(
    val isDialogVisible: Boolean = false,
    val showProgress: Boolean = false,
    val progress: Int = 0,
)

@HiltViewModel
class LocalModelsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val llmInferenceAPI: LLMInferenceAPI,
    private val hfAccessToken: HFAccessToken,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            LocalModelsUIState(
                models =
                    listOf(
                        LocalModel(
                            name = "Qwen2.5 0.5B Instruct Q8",
                            description = "A Qwen family model series",
                            downloadUrl = "https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct/resolve/main/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task",
                        ),
                        LocalModel(
                            name = "Qwen2.5 1.5B Instruct Q8",
                            description = "A Qwen family model series",
                            downloadUrl = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_seq128_q8_ekv4096.task",
                        ),
                        LocalModel(
                            name = "Qwen2.5 3B Instruct Q8",
                            description = "A Qwen family model series",
                            downloadUrl = "https://huggingface.co/litert-community/Qwen2.5-3B-Instruct/resolve/main/Qwen2.5-3B-Instruct_multi-prefill-seq_q8_ekv1280.task",
                        ),
                        LocalModel(
                            name = "Phi 4 Mini Instruct Q8",
                            description = "A Microsoft Phi 4 model series",
                            downloadUrl = "https://huggingface.co/litert-community/Phi-4-mini-instruct/resolve/main/Phi-4-mini-instruct_multi-prefill-seq_q8_ekv1280.task",
                        ),
                        LocalModel(
                            name = "DeepSeek R1 Distill Qwen 1.5B Q8",
                            description = "DeepSeek R1",
                            downloadUrl = "https://huggingface.co/litert-community/DeepSeek-R1-Distill-Qwen-1.5B/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B_multi-prefill-seq_q8_ekv4096.task",
                        ),
                        LocalModel(
                            name = "Gemma3 1B IT",
                            description = "Gemma 3 1B Instruction-Tuned (gated model)",
                            downloadUrl = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q8_ekv4096.task",
                        ),
                        LocalModel(
                            name = "Gemma3 4B IT",
                            description = "Gemma 3 4B Instruction-Tuned (gated model)",
                            downloadUrl = "https://huggingface.co/litert-community/Gemma3-4B-IT/resolve/main/gemma3-4b-it-int8-web.task",
                        ),
                        LocalModel(
                            name = "Llama 3.2 1B Q8",
                            description = "Llama 3.2 1B (gated model)",
                            downloadUrl = "https://huggingface.co/litert-community/Llama-3.2-1B-Instruct/resolve/main/Llama-3.2-1B-Instruct_multi-prefill-seq_q8_ekv1280.task",
                        ),
                        LocalModel(
                            name = "Llama 3.2 3B Q8",
                            description = "Llama 3.2 3B (gated model)",
                            downloadUrl = "https://huggingface.co/litert-community/Llama-3.2-3B-Instruct/resolve/main/Llama-3.2-3B-Instruct_multi-prefill-seq_q8_ekv1280.task",
                        ),
                    ),
            ),
        )
    val uiState: StateFlow<LocalModelsUIState> = _uiState.asStateFlow()

    private var ketch: Ketch =
        Ketch
            .builder()
            .setOkHttpClient(
                OkHttpClient
                    .Builder()
                    .connectTimeout(120L, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(120L, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(120L, java.util.concurrent.TimeUnit.SECONDS)
                    .callTimeout(300L, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor { chain ->
                        val request = chain.request()
                        Log.d("APP", "Downloading from: ${request.url}")
                        try {
                            chain.proceed(request)
                        } catch (e: Exception) {
                            Log.e("APP", "Network error: ${e.message}", e)
                            throw e
                        }
                    }
                    .build(),
            ).build(context)

    fun onEvent(event: LocalModelsUIEvent) {
        when (event) {
            is LocalModelsUIEvent.OnModelDownloadClick -> {
                viewModelScope.launch(Dispatchers.IO) {
                    downloadModel(event.model)
                }
            }
            is LocalModelsUIEvent.OnUseModelClick -> {
                if (llmInferenceAPI.isLoaded) {
                    llmInferenceAPI.unload()
                }
                viewModelScope.launch(Dispatchers.IO) {
                    loadModel(event.model)
                    onEvent(LocalModelsUIEvent.RefreshModelsList)
                }
            }
            is LocalModelsUIEvent.RefreshModelsList -> {
                _uiState.update {
                    it.copy(
                        models =
                            it.models.map { model ->
                                model.copy(
                                    isLoaded =
                                        llmInferenceAPI.loadedModelPath == model.getLocalModelPath(context.filesDir.absolutePath),
                                )
                            },
                    )
                }
            }
        }
    }

    private suspend fun loadModel(model: LocalModel) =
        withContext(Dispatchers.IO) {
            val modelPath = model.getLocalModelPath(context.filesDir.absolutePath)
            Log.d("APP", "Attempting to load model from: $modelPath")
            
            // Verify file exists before attempting to load
            val modelFile = java.io.File(modelPath)
            if (!modelFile.exists()) {
                Log.e("APP", "Model file does not exist at: $modelPath")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Model file not found. Please re-download the model.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@withContext
            }
            
            Log.d("APP", "Model file exists, size: ${modelFile.length()} bytes")
            
            llmInferenceAPI.load(
                context,
                modelPath,
                onSuccess = {
                    Log.d("APP", "Model loaded successfully")
                },
                onError = { exception ->
                    Log.e("APP", "Failed to load LiteRT model: ${exception.message}", exception)
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to load model: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
            )
        }

    private suspend fun downloadModel(model: LocalModel) {
        Log.d("APP", "Starting download for model: ${model.name}")
        Log.d("APP", "Download URL: ${model.downloadUrl}")
        Log.d("APP", "Save to: ${context.filesDir.absolutePath}/${model.getFileName()}")
        
        // Clean up old downloads and temp files for this model before starting
        val targetFileName = model.getFileName()
        val filesDir = java.io.File(context.filesDir.absolutePath)
        filesDir.listFiles()?.forEach { file ->
            // Remove old versions with suffixes like (1), (2), etc. and .temp files
            if (file.name.startsWith(targetFileName.substringBeforeLast(".")) && 
                (file.name.contains("(") || file.name.endsWith(".temp"))) {
                Log.d("APP", "Cleaning up old file: ${file.name}")
                file.delete()
            }
            // Also remove exact match if it exists (to ensure clean download)
            if (file.name == targetFileName) {
                Log.d("APP", "Removing existing file: ${file.name}")
                file.delete()
            }
        }
        
        val headers =
            if (hfAccessToken.getToken() != null) {
                Log.d("APP", "Using HuggingFace access token")
                HashMap(
                    mapOf("Authorization" to "Bearer ${hfAccessToken.getToken()}"),
                )
            } else {
                Log.d("APP", "No HuggingFace access token provided")
                HashMap()
            }
        
        try {
            val downloadId =
                ketch.download(
                    model.downloadUrl,
                    context.filesDir.absolutePath,
                    model.getFileName(),
                    headers = headers,
                )
            Log.d("APP", "Download ID: $downloadId")
            ketch
                .observeDownloadById(downloadId)
                .flowOn(Dispatchers.IO)
                .collect { downloadModel ->
                    downloadModel?.let { ketchDownload ->
                        Log.d("APP", "Download status: ${ketchDownload.status}, Progress: ${ketchDownload.progress}%")
                        when (ketchDownload.status) {
                            Status.QUEUED -> {
                                Log.d("APP", "Download queued")
                                _uiState.update {
                                    it.copy(
                                        downloadModelDialogState = 
                                            it.downloadModelDialogState.copy(
                                                isDialogVisible = true,
                                                showProgress = true,
                                                progress = 0
                                            ),
                                    )
                                }
                            }

                            Status.PROGRESS -> {
                                Log.d("APP", "Download progress: ${ketchDownload.progress}%")
                                _uiState.update {
                                    it.copy(
                                        downloadModelDialogState = 
                                            it.downloadModelDialogState.copy(
                                                isDialogVisible = true,
                                                showProgress = true,
                                                progress = ketchDownload.progress
                                            ),
                                    )
                                }
                            }

                            Status.SUCCESS -> {
                                Log.d("APP", "Download completed successfully")
                                Log.d("APP", "Ketch download path: ${ketchDownload.path}")
                                Log.d("APP", "Ketch download fileName: ${ketchDownload.fileName}")
                                
                                val expectedFilePath = model.getLocalModelPath(context.filesDir.absolutePath)
                                val expectedFile = java.io.File(expectedFilePath)
                                val targetFileName = model.getFileName()
                                val baseFileName = targetFileName.substringBeforeLast(".")
                                val fileExtension = targetFileName.substringAfterLast(".")
                                
                                // Search for the downloaded file in the directory
                                val filesDir = java.io.File(context.filesDir.absolutePath)
                                val downloadedFiles = filesDir.listFiles { file ->
                                    // Find files that match our model name (including those with (1), (2) suffixes)
                                    file.isFile && 
                                    file.name.startsWith(baseFileName) && 
                                    file.name.endsWith(".$fileExtension") &&
                                    !file.name.endsWith(".temp") &&
                                    file.length() > 1_000_000 // Model files should be large (> 1MB)
                                }?.sortedByDescending { it.lastModified() } // Get most recent
                                
                                Log.d("APP", "Found ${downloadedFiles?.size ?: 0} potential model files")
                                downloadedFiles?.forEach { file ->
                                    Log.d("APP", "  - ${file.name} (${file.length()} bytes, modified: ${file.lastModified()})")
                                }
                                
                                val actualFile = downloadedFiles?.firstOrNull()
                                
                                if (actualFile != null && actualFile.exists()) {
                                    Log.d("APP", "Using downloaded file: ${actualFile.absolutePath}")
                                    Log.d("APP", "File size: ${actualFile.length()} bytes")
                                    
                                    // Rename to expected filename if different
                                    if (actualFile.absolutePath != expectedFilePath) {
                                        Log.d("APP", "Renaming file from ${actualFile.absolutePath} to $expectedFilePath")
                                        // Delete any existing file at target location
                                        if (expectedFile.exists()) {
                                            Log.d("APP", "Deleting existing file at target location")
                                            expectedFile.delete()
                                        }
                                        if (actualFile.renameTo(expectedFile)) {
                                            Log.d("APP", "File renamed successfully")
                                        } else {
                                            Log.e("APP", "Failed to rename file. Trying to copy instead...")
                                            try {
                                                actualFile.copyTo(expectedFile, overwrite = true)
                                                actualFile.delete()
                                                Log.d("APP", "File copied successfully")
                                            } catch (e: Exception) {
                                                Log.e("APP", "Failed to copy file: ${e.message}", e)
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("APP", "ERROR: Could not find downloaded model file")
                                    Log.d("APP", "All files in directory:")
                                    filesDir.listFiles()?.forEach { file ->
                                        Log.d("APP", "  - ${file.name} (${file.length()} bytes)")
                                    }
                                }
                                
                                _uiState.update {
                                    it.copy(
                                        downloadModelDialogState = it.downloadModelDialogState.copy(isDialogVisible = false),
                                    )
                                }
                                onEvent(LocalModelsUIEvent.OnUseModelClick(model))
                                withContext(Dispatchers.Main) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Model downloaded successfully",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                }
                            }

                            Status.FAILED -> {
                                Log.e("APP", "Download failed: ${ketchDownload.failureReason}")
                                _uiState.update {
                                    it.copy(
                                        downloadModelDialogState = it.downloadModelDialogState.copy(isDialogVisible = false),
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Download failed: ${ketchDownload.failureReason}",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                }
                            }

                            Status.STARTED -> {
                                Log.d("APP", "Download started")
                                _uiState.update {
                                    it.copy(
                                        downloadModelDialogState =
                                            it.downloadModelDialogState.copy(
                                                isDialogVisible = true,
                                                showProgress = true,
                                                progress = 0
                                            ),
                                    )
                                }
                            }

                            else -> {
                                Log.d("APP", "Download status: ${ketchDownload.status}")
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("APP", "Error starting download: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Failed to start download: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

