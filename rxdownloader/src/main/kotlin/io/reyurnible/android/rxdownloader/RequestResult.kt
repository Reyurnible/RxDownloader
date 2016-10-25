package io.reyurnible.android.rxdownloader

/**
 * Download result object
 */
data class RequestResult(
        val id: Long,
        val remoteUri: String,
        val localUri: String,
        val mediaType: String,
        val totalSize: Int,
        val title: String?,
        val description: String?
)
