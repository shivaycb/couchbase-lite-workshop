# Couchbase-ExecuTorch-App

Android application integrating Couchbase Lite with ExecuTorch for on-device LLM inference.

## Overview

This project demonstrates integration of:
- **Couchbase Lite** - Local database with vector search
- **ExecuTorch** - PyTorch's on-device inference runtime
- **Qualcomm QNN** - Hardware acceleration backend
- **Jetpack Compose** - Modern Android UI

## Current Status

⚠️ **IMPORTANT:** The ExecuTorch integration is **partially complete** with known limitations.

### What Works ✅
- App builds and runs successfully
- Couchbase Lite database integration
- Document parsing (PDF, DOCX)
- Vector search with embeddings
- ExecuTorch JNI bindings configured
- QNN libraries integrated

### Known Limitations ❌
- **Qwen models crash during generation** due to tokenizer incompatibility
- ExecuTorch AAR removed from dependencies (incompatible with Qwen)
- Only Llama models with SentencePiece tokenizers are supported

## Architecture

### LLM Backend System

```
LLMInferenceAPI (Abstract)
├── GeminiRemoteAPI (Cloud-based)
├── LiteRTAPI (MediaPipe)
└── ExecuTorchAPI (On-device)
    └── ExecuTorchRunner
        └── LlmModule (from AAR)
```

### Native Layer

```
app/src/main/cpp/
├── CMakeLists.txt          # Build configuration
└── executorch_jni.cpp      # JNI bindings (stub implementation)

app/src/main/jniLibs/arm64-v8a/
├── libQnnHtp.so           # QNN HTP backend
├── libQnnSystem.so        # QNN system library
└── libQnn*.so             # Additional QNN libraries
```

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog or later
- Android NDK 27.0+
- Qualcomm QNN SDK 2.28+ (for QNN libraries)
- Java 17+
- Gradle 8.9+

### QNN Libraries Setup

Run the PowerShell script to copy QNN libraries:

```powershell
.\setup_qnn_libs.ps1
```

Or manually copy from QNN SDK:
```
$QNN_SDK/lib/aarch64-android/*.so → app/src/main/jniLibs/arm64-v8a/
```

### Build

```bash
./gradlew assembleDebug
```

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Model Compatibility

### Supported Models ✅
- Llama 3.2 (1B, 3B)
- Llama 3.1 (8B)
- Mistral 7B
- Any model with **SentencePiece tokenizer** (.bin or .model)

### Unsupported Models ❌
- Qwen 2.5 (uses BPE tokenizer)
- GPT-2 / GPT-J (uses BPE tokenizer)
- Models with tokenizer.json format

### Why Qwen Doesn't Work

The standard ExecuTorch Android AAR (`org.pytorch:executorch-android:1.0.0`) only supports **SentencePiece tokenizers**. Qwen models use **BPE (Byte-Pair Encoding)** tokenizers stored in JSON format, which causes:

1. Model loads successfully
2. **Native crash (SIGSEGV)** when calling `generate()`
3. Crash cannot be caught by try-catch (happens in C++ layer)

See [crash_report.md](crash_report.md) for detailed analysis.

## File Structure

```
app/src/main/
├── cpp/
│   ├── CMakeLists.txt                    # Native build config
│   └── executorch_jni.cpp                # JNI bindings
├── java/com/ml/shivay_couchbase/docqa/
│   ├── ExecuTorchRunner.kt              # Kotlin wrapper for LlmModule
│   ├── di/LLMModule.kt                   # Hilt dependency injection
│   └── domain/llm/
│       ├── LLMInferenceAPI.kt           # Abstract interface
│       ├── ExecuTorchAPI.kt             # ExecuTorch implementation
│       ├── GeminiRemoteAPI.kt           # Gemini implementation
│       └── LiteRTAPI.kt                 # MediaPipe implementation
└── jniLibs/arm64-v8a/
    └── libQnn*.so                        # QNN libraries
```

## Next Steps

### Option 1: Use Llama Model (Recommended)

1. Download Llama 3.2 1B model:
   ```
   https://huggingface.co/meta-llama/Llama-3.2-1B-Instruct
   ```

2. Export to ExecuTorch format (.pte)

3. Place model and tokenizer.model in app's files directory

4. Re-add ExecuTorch dependency:
   ```kotlin
   implementation("org.pytorch:executorch-android:1.0.0")
   ```

5. Load model - should work without crashes

### Option 2: Build ExecuTorch from Source

To support Qwen models:

1. Set up WSL/Ubuntu environment
2. Clone ExecuTorch repository
3. Build with custom tokenizer support
4. Build QNN backend
5. Generate custom AAR
6. Replace standard AAR in project

**Estimated time:** 4-8 hours  
**Difficulty:** Advanced

## Dependencies

```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.15.0")
implementation("androidx.compose.material3:material3")

// Couchbase
implementation("com.couchbase.lite:couchbase-lite-android:3.2.1")
implementation("com.couchbase.lite:vector-search:3.2.1")

// ExecuTorch (currently removed due to incompatibility)
// implementation("org.pytorch:executorch-android:1.0.0")

// Gemini SDK
implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

// MediaPipe
implementation("com.google.mediapipe:tasks-genai:0.10.29")
```

## Known Issues

1. **Qwen Model Crash** - Native SIGSEGV during text generation
   - **Cause:** BPE tokenizer incompatibility
   - **Workaround:** Use Llama model instead

2. **ExecuTorch AAR Removed** - Dependency removed to prevent build issues
   - **Impact:** ExecuTorch functionality disabled
   - **Fix:** Re-add after obtaining compatible model

3. **Large File Warning** - `all-MiniLM-L6-V2.onnx` (86MB) exceeds GitHub limit
   - **Recommendation:** Use Git LFS for model files

## Documentation

- [Implementation Plan](implementation_plan.md) - Technical design
- [Walkthrough](walkthrough.md) - Integration steps
- [Crash Report](crash_report.md) - Detailed crash analysis

## License

[Your License Here]

## Contributors

- Shivay Rawat (@carrycooldude)
