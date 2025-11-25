package com.ml.couchbase.docqa.di

import com.ml.shivay_couchbase.docqa.domain.llm.ExecuTorchAPI
import com.ml.shivay_couchbase.docqa.domain.llm.LLMInferenceAPI
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LLMModule {

    @Binds
    @Singleton
    abstract fun bindLLMInferenceAPI(
        execuTorchAPI: ExecuTorchAPI
    ): LLMInferenceAPI
}
