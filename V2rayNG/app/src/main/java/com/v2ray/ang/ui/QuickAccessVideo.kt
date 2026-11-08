package com.v2ray.ang.ui

internal object QuickAccessVideo {
    private val externalExtensions = setOf("mov", "qt", "avi", "wmv", "flv", "mkv")

    fun shouldOpenExternally(url: String): Boolean {
        if (url.isBlank()) return false
        val withoutQuery = url.substringBefore('#').substringBefore('?')
        val fileName = withoutQuery.substringAfterLast('/').substringAfterLast('\\')
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return extension in externalExtensions
    }
}
