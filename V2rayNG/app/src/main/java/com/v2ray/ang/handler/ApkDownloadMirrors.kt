package com.v2ray.ang.handler

object ApkDownloadMirrors {
    private val githubPrefixes = listOf(
        "https://ghfast.top/",
        "https://ghproxy.net/",
        "https://gh-proxy.com/",
    )

    fun candidates(url: String): List<String> {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return emptyList()
        val out = linkedSetOf(trimmed)
        if (isGitHubDownload(trimmed)) {
            githubPrefixes.forEach { prefix -> out += prefix + trimmed }
        }
        return out.toList()
    }

    private fun isGitHubDownload(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("://github.com/") ||
            lower.contains("://objects.githubusercontent.com/") ||
            lower.contains("://release-assets.githubusercontent.com/")
    }
}
