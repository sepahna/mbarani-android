package com.v2ray.ang.handler

import android.os.Build
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.dto.AppUpdateFeed
import com.v2ray.ang.dto.CheckUpdateResult
import com.v2ray.ang.dto.GitHubRelease
import com.v2ray.ang.dto.UrlContentRequest
import com.v2ray.ang.extension.concatUrl
import com.v2ray.ang.util.HttpUtil
import com.v2ray.ang.util.JsonUtil
import com.v2ray.ang.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateCheckerManager {
    suspend fun checkForUpdate(includePreRelease: Boolean = false): CheckUpdateResult =
        withContext(Dispatchers.IO) {
            val currentVersion = versionParts(BuildConfig.VERSION_NAME)
                ?: return@withContext CheckUpdateResult(hasUpdate = false)

            val feedErrors = mutableListOf<String>()
            var newestMiss: CheckUpdateResult? = null
            for (url in feedUrls(includePreRelease)) {
                try {
                    val body = fetchText(url) ?: continue
                    if (AppUpdateFeedParser.looksLikeHtml(body)) {
                        feedErrors += "$url returned HTML"
                        continue
                    }
                    val feed = AppUpdateFeedParser.parse(body)
                        ?: parseGitHubRelease(body, includePreRelease)
                    if (feed == null) {
                        feedErrors += "$url: unrecognized update payload"
                        continue
                    }
                    val result = toResult(feed, currentVersion)
                    if (result.hasUpdate) {
                        return@withContext result
                    }
                    // Stale CDN/site feed can report the current version while a newer
                    // mirror already has the next release — keep searching.
                    newestMiss = result
                } catch (error: Exception) {
                    feedErrors += "$url: ${error.message}"
                    LogUtil.e(AppConfig.TAG, "Update check failed for $url", error)
                }
            }

            newestMiss?.let { return@withContext it }
            throw IllegalStateException(
                feedErrors.lastOrNull() ?: "Failed to check for updates"
            )
        }

    private fun feedUrls(includePreRelease: Boolean): List<String> {
        val urls = linkedSetOf(
            AppConfig.APP_UPDATE_FEED_URL,
            AppConfig.APP_UPDATE_RAW_URL,
            AppConfig.APP_UPDATE_CDN_URL,
        )
        if (includePreRelease) {
            urls += AppConfig.APP_API_URL
        } else {
            urls += AppConfig.APP_API_URL.concatUrl("latest")
        }
        return urls.toList()
    }

    private fun fetchText(url: String): String? {
        val direct = HttpUtil.getUrlContent(
            UrlContentRequest(url = url, timeout = 12_000)
        )
        if (!direct.isNullOrBlank()) return direct

        val httpPort = SettingsManager.getHttpPort()
        if (httpPort == 0) return null
        return HttpUtil.getUrlContent(
            UrlContentRequest(
                url = url,
                timeout = 12_000,
                httpPort = httpPort,
                proxyUsername = SettingsManager.getSocksUsername(),
                proxyPassword = SettingsManager.getSocksPassword(),
            )
        )
    }

    private fun parseGitHubRelease(body: String, includePreRelease: Boolean): AppUpdateFeed? {
        val release = if (includePreRelease) {
            JsonUtil.fromJsonSafe(body, Array<GitHubRelease>::class.java)?.firstOrNull()
        } else {
            JsonUtil.fromJsonSafe(body, GitHubRelease::class.java)
        } ?: return null

        val version = release.tagName.removePrefix("v").trim()
        if (version.isEmpty()) return null
        val apk = resolveDownloadUrl(release, Build.SUPPORTED_ABIS.firstOrNull().orEmpty())
            ?: return null
        return AppUpdateFeed(
            version = version,
            apk = apk,
            notes = release.body,
        )
    }

    private fun toResult(feed: AppUpdateFeed, currentVersion: List<Int>): CheckUpdateResult {
        val latestVersion = feed.version.removePrefix("v").trim()
        LogUtil.i(
            AppConfig.TAG,
            "Found update feed version=$latestVersion current=${BuildConfig.VERSION_NAME}"
        )
        val comparableVersion = versionParts(latestVersion)
            ?: return CheckUpdateResult(hasUpdate = false)
        if (compareVersions(comparableVersion, currentVersion) <= 0) {
            return CheckUpdateResult(hasUpdate = false)
        }
        return CheckUpdateResult(
            hasUpdate = true,
            latestVersion = latestVersion,
            releaseNotes = feed.notes,
            downloadUrl = feed.apk,
            isPreRelease = false,
        )
    }

    internal fun resolveDownloadUrl(release: GitHubRelease, abi: String): String? {
        val apks = release.assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apks.isEmpty()) return null
        val fDroid = "fdroid"
        val preferFdroid = BuildConfig.APPLICATION_ID.contains(fDroid, ignoreCase = true)

        fun matchesChannel(name: String): Boolean {
            val hasFdroid = name.contains(fDroid, ignoreCase = true)
            return if (preferFdroid) hasFdroid else !hasFdroid
        }

        // Universal builds first: ABI-named assets are optional for this fork.
        apks.firstOrNull {
            matchesChannel(it.name) && it.name.contains("universal", ignoreCase = true)
        }?.browserDownloadUrl?.let { return it }

        if (abi.isNotBlank()) {
            apks.firstOrNull {
                matchesChannel(it.name) && it.name.contains(abi, ignoreCase = true)
            }?.browserDownloadUrl?.let { return it }
        }

        return apks.firstOrNull { matchesChannel(it.name) }?.browserDownloadUrl
            ?: apks.firstOrNull()?.browserDownloadUrl
    }

    internal fun versionParts(raw: String): List<Int>? {
        val cleaned = raw.removePrefix("v").trim()
        if (cleaned.isEmpty() || !cleaned.first().isDigit()) return null
        return cleaned.split('.', '-', '_').map { token ->
            token.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
        }
    }

    internal fun compareVersions(version1: List<Int>, version2: List<Int>): Int {
        for (i in 0 until maxOf(version1.size, version2.size)) {
            val num1 = version1.getOrElse(i) { 0 }
            val num2 = version2.getOrElse(i) { 0 }
            if (num1 != num2) return num1 - num2
        }
        return 0
    }

}
