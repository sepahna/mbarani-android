package com.v2ray.ang.dto

import java.io.Serializable

/** Bytes since the previous core query, plus the running total for this connection. */
data class TrafficSample(
    val uploadBytes: Long,
    val downloadBytes: Long,
    val elapsedMillis: Long,
    val sessionUploadBytes: Long,
    val sessionDownloadBytes: Long,
) : Serializable
