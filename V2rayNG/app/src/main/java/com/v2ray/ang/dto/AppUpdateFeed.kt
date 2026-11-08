package com.v2ray.ang.dto

import com.google.gson.annotations.SerializedName

data class AppUpdateFeed(
    @SerializedName("version")
    val version: String,
    @SerializedName("apk")
    val apk: String,
    @SerializedName("notes")
    val notes: String? = null,
)
