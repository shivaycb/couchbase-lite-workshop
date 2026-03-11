# Couchbase Lite DocQA - Android

An Android document question-answering app that demonstrates how to combine **Couchbase Lite**, **vector search**, and **Gemini** to build an on-device/offline-first retrieval-augmented generation (RAG) experience.

The app is built with **Jetpack Compose** and uses **Couchbase Lite Enterprise Edition** with vector indexing support to store and search document content locally. Users can browse documents, ingest sample files, and ask natural-language questions through a chat experience backed by retrieval and LLM-based answer generation.

## Quick Start

If your machine is already configured with Android Studio and the required Couchbase Lite dependencies:

```bash
git clone https://github.com/shivaycb/couchbase-lite-workshop.git
cd couchbase-lite-workshop
./gradlew assembleDebug
./gradlew installDebug
```

Then open the app on an emulator or Android device.

## What This App Demonstrates

This application appears to be a workshop/demo project for:

- **Local-first document storage**
- **Vector indexing and semantic retrieval**
- **PDF/document ingestion**
- **Question answering over indexed content**
- **Gemini-powered answer generation**
- **Compose-based Android UI**
- **Dependency injection with Hilt**

The app structure suggests a RAG-style workflow:

1. Documents are added to the app
2. Text is extracted and chunked
3. Embeddings are generated
4. Chunks and vectors are stored locally in Couchbase Lite
5. User questions are matched against relevant chunks using vector search
6. Retrieved context is sent to Gemini to generate answers

## Requirements

- **Android Studio**: Recent stable version recommended
- **Android SDK**:
  - **Minimum SDK**: 26
  - **Target SDK**: 34
  - **Compile SDK**: 34
- **Java**: 8-compatible build target is configured
- **Kotlin Android project support**
- **Internet access**: Required for Gemini API calls
- **Gemini API key**: Required for LLM responses
- **Access to Couchbase Maven repository**: Required for Couchbase Lite EE dependencies

## Technology Stack

### Core Android
- **Kotlin**
- **Jetpack Compose**
- **AndroidX Navigation Compose**
- **Material 3**
- **Hilt**

### Document / AI / Search
- **Couchbase Lite Android EE KTX**
- **Couchbase Lite Vector Search**
- **Google Gemini SDK**
- **Sentence Embeddings for Android**
- **Apache POI**
- **iTextPDF**
- **compose-markdown**

## Dependencies

Key dependencies visible in the project include:

- `com.couchbase.lite:couchbase-lite-android-ee-ktx`
- `com.couchbase.lite:couchbase-lite-android-vector-search-arm64`
- `com.google.ai.client.generativeai:generativeai:0.6.0`
- `com.github.shubham0204:Sentence-Embeddings-Android:0.0.3`
- `com.itextpdf:itextpdf:5.5.13.3`
- `org.apache.poi:poi`
- `org.apache.poi:poi-ooxml`
- `com.github.jeziellago:compose-markdown:0.5.0`

Versions are managed partly through Gradle version catalogs in `gradle/libs.versions.toml`.

## Project Setup

### 1. Clone the repository

```bash
git clone https://github.com/shivaycb/couchbase-lite-workshop.git
cd couchbase-lite-workshop
```

### 2. Add your Gemini API key

The app reads `geminiKey` from `local.properties`. If `local.properties` is missing, it falls back to `local.properties.default` when present.

Create a file named `local.properties` in the project root:

```properties
geminiKey="YOUR_ACTUAL_API_KEY_HERE"
```

If no key is provided, the build may still succeed, but Gemini-backed features will not function correctly.

### 3. Sync Gradle

Open the project in Android Studio and allow Gradle to sync.

Or from the command line:

```bash
./gradlew tasks
```

### 4. Build the app

```bash
./gradlew assembleDebug
```

### 5. Install on a device or emulator

```bash
./gradlew installDebug
```

## Couchbase Lite Repository Setup

This project depends on the Couchbase mobile Maven repository.

The repository is configured in Gradle:

- top-level build configuration
- settings plugin management
- dependency resolution repositories

If dependency resolution fails, verify access to:

- Couchbase mobile Maven repository
- Maven Central
- Google Maven
- JitPack

## App Configuration

### Android configuration

- **Namespace**: `com.ml.couchbase.docqa`
- **Application ID**: `com.ml.couchbase.docqa`
- **App class**: `DocQAApplication`
- **Launcher activity**: `MainActivity`

### BuildConfig fields

The app injects the Gemini key into `BuildConfig` for both:

- `debug`
- `release`

So your `geminiKey` in `local.properties` becomes available at build time.

## Application Architecture

## App startup

At launch, the application class initializes the database layer:

- `DocQAApplication`
- calls `DatabaseManager.init(this)`

This indicates that database setup happens early in app startup before user interaction begins.

## Navigation

The main navigation flow is simple and Compose-based:

- `chat` → main question-answering screen
- `docs` → document browsing / ingestion screen

The app starts on the **chat** route.

This suggests a workflow where:
- the user primarily interacts through chat
- documents can be opened or managed via a dedicated docs screen

## UI Screens

### Chat Screen
The `ChatScreen` is the main entry point of the app. Based on naming and dependencies, it likely handles:

- question input
- retrieval-triggered prompts
- answer rendering
- markdown display
- navigation to the docs screen

### Docs Screen
The `DocsScreen` likely supports:

- browsing available documents
- importing or indexing documents
- selecting sample content
- returning to chat after ingestion

## Database Layer

The repository includes a `DatabaseManager` abstraction.

The currently visible file suggests a placeholder/mock implementation, but commented code strongly indicates the intended design is:

- initialize **Couchbase Lite**
- enable **vector search**
- open a local database
- create/query indexes
- save documents
- run SQL++/query-based retrieval

The commented implementation references:

- `CouchbaseLite.init(context)`
- `CouchbaseLite.enableVectorSearch()`
- local database creation
- index creation
- query execution
- document save/delete operations

This suggests the workshop may progressively replace mock implementations with full Couchbase Lite-backed behavior across branches or exercises.

## Retrieval-Augmented Generation Flow

Based on the dependencies and package structure, the app’s intended RAG pipeline likely looks like this:

### 1. Document ingestion
Supported document-related libraries indicate the app can parse:

- **PDFs** via iTextPDF
- **Office documents / structured docs** via Apache POI

### 2. Text extraction
Document content is extracted into plain text.

### 3. Chunking
Large documents are typically split into smaller chunks so they can be embedded and searched efficiently.

### 4. Embedding generation
The app includes a sentence embeddings library for Android, which suggests embeddings are generated locally on-device.

### 5. Vector persistence
Chunks and embeddings are stored in Couchbase Lite documents.

### 6. Vector search
A vector index is used to retrieve semantically similar chunks for the user’s question.

### 7. Prompt construction
Relevant chunks are assembled into context for the LLM.

### 8. Gemini answer generation
The Gemini SDK is used to generate a final answer grounded in the retrieved document context.

## Sample Content

The repository includes a `sample_pdfs` folder with example content and prompts.

Examples referenced in the repository include:

- **Couchbase Shell Documentation.pdf**
- **fina_sample_reports**

Example prompt themes include:

- configuring Bedrock as the LLM in CB Shell
- registering a new Capella cluster in CB Shell
- finance-related ratio questions from report data

These samples appear intended to validate RAG quality and retrieval behavior.

## Project Structure

```text
.
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ml/couchbase/docqa/
│       │   ├── DocQAApplication.kt
│       │   ├── MainActivity.kt
│       │   └── data/
│       │       └── DatabaseManager.kt
│       └── res/
├── gradle/
│   └── libs.versions.toml
├── sample_pdfs/
│   └── README.md
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Important Files

### Application entry point
- `app/src/main/java/com/ml/couchbase/docqa/DocQAApplication.kt`

Initializes the app and database layer.

### Main activity
- `app/src/main/java/com/ml/couchbase/docqa/MainActivity.kt`

Sets up Compose UI and navigation between chat and docs screens.

### Database abstraction
- `app/src/main/java/com/ml/couchbase/docqa/data/DatabaseManager.kt`

Encapsulates database initialization and storage/query operations.

### App manifest
- `app/src/main/AndroidManifest.xml`

Registers the application class, main activity, and internet permission.

### App build configuration
- `app/build.gradle.kts`

Defines Android build settings, dependency declarations, Hilt, Compose, and Gemini key handling.

### Dependency and plugin repositories
- `settings.gradle.kts`
- `build.gradle.kts`

Configure plugin management and repositories including Couchbase’s Maven endpoint.

## Build Commands

### Assemble debug APK

```bash
./gradlew assembleDebug
```

### Install debug build

```bash
./gradlew installDebug
```

### Run tests

```bash
./gradlew test
```

### Lint the project

```bash
./gradlew lint
```

## Running the App

1. Launch the app
2. Open the documents screen if needed
3. Add or inspect sample documents
4. Return to the chat screen
5. Ask questions about the indexed content
6. Review the generated answer and retrieved context behavior
