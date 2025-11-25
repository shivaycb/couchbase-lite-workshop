package com.ml.shivay_couchbase.docqa

import android.util.Log

class ExecuTorchRunner {

    private var nativeHandle: Long = 0

    companion object {
        init {
            try {
                System.loadLibrary("executorch_jni")
                Log.i("ExecuTorchRunner", "Loaded executorch_jni library")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("ExecuTorchRunner", "Failed to load executorch_jni library", e)
            }
        }
    }

    fun initialize(modelPath: String): Boolean {
        nativeHandle = nativeInit(modelPath)
        return nativeHandle != 0L
    }

    fun generate(prompt: String): String {
        if (nativeHandle == 0L) {
            throw IllegalStateException("ExecuTorch runner not initialized")
        }
        return nativeGenerate(nativeHandle, prompt)
    }

    fun close() {
        if (nativeHandle != 0L) {
            nativeClose(nativeHandle)
            nativeHandle = 0
        }
    }

    private external fun nativeInit(modelPath: String): Long
    private external fun nativeGenerate(handle: Long, prompt: String): String
    private external fun nativeClose(handle: Long)
}
