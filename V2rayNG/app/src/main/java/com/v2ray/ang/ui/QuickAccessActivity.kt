package com.v2ray.ang.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.compose.AppTopBar

class QuickAccessActivity : BaseComponentActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = filePathCallback
        filePathCallback = null
        callback?.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        CookieManager.getInstance().flush()
        super.onPause()
    }

    fun launchFileChooser(
        callback: ValueCallback<Array<Uri>>?,
        params: WebChromeClient.FileChooserParams?
    ) {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = callback
        val intent = params?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        runCatching { fileChooserLauncher.launch(intent) }.onFailure {
            filePathCallback = null
            callback?.onReceiveValue(null)
        }
    }

    fun openMediaExternally(url: String) {
        if (url.isBlank()) return
        val open = {
            val uri = Uri.parse(url)
            val mime = guessVideoMime(url)
            val view = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val fallback = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching {
                startActivity(Intent.createChooser(view, getString(R.string.title_site_video)))
            }.recoverCatching {
                startActivity(Intent.createChooser(fallback, getString(R.string.title_site_video)))
            }.recoverCatching {
                startActivity(fallback)
            }
            Unit
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            open()
        } else {
            Handler(Looper.getMainLooper()).post(open)
        }
    }

    @Composable
    override fun ScreenContent() {
        val requested = intent.getStringExtra(EXTRA_URL).orEmpty()
        val startUrl = if (requested.startsWith("https://mbarani.ir")) {
            requested
        } else {
            AppConfig.SITE_URL
        }
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            .ifBlank { stringResource(R.string.title_quick_access) }
        SiteScreen(
            startUrl = startUrl,
            title = title,
            onBackClick = { finish() },
            onHomeClick = { finish() },
            onOpenMedia = ::openMediaExternally
        )
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }
}

private fun guessVideoMime(url: String): String {
    val path = Uri.parse(url).path.orEmpty().lowercase()
    return when {
        path.endsWith(".mp4") || path.endsWith(".m4v") -> "video/mp4"
        path.endsWith(".webm") -> "video/webm"
        // Many Android players reject video/quicktime even when they can play the file.
        path.endsWith(".mov") || path.endsWith(".qt") -> "video/*"
        path.endsWith(".mkv") -> "video/*"
        else -> "video/*"
    }
}

private const val SITE_FIX_SCRIPT = """
(function(){
  if (window.__mbAppFix) return;
  window.__mbAppFix = true;
  var css = document.createElement('style');
  css.textContent = [
    'html,body,#root{background-color:#07111f;min-height:100%;color:#e8eef8}',
    '.aspect-video{min-height:160px}',
    'video,iframe{max-width:100%;background:#111}',
    'video{pointer-events:auto !important}',
    '[class*="92dvh"]{max-height:min(92vh,40rem)}'
  ].join('');
  document.documentElement.appendChild(css);

  function mediaUrl(v){
    return (v.currentSrc || v.src || '').trim();
  }

  function needsExternal(url){
    return /\.(mov|qt|avi|wmv|flv|mkv)(\?|#|$)/i.test(url || '');
  }

  function isModalVideo(v){
    return !!(v.closest('[class*="fixed inset-0"]') || v.closest('[class*="z-[120]"]'));
  }

  function openExternal(url){
    if (!url || !window.MbaraniApp || !window.MbaraniApp.openMedia) return false;
    try { window.MbaraniApp.openMedia(url); return true; } catch (_) { return false; }
  }

  function mark(v){
    if (!v || v.dataset.mbInline) return;
    v.dataset.mbInline = '1';
    v.setAttribute('playsinline','');
    v.setAttribute('webkit-playsinline','');
    v.playsInline = true;

    var url = mediaUrl(v);
    var modal = isModalVideo(v);

    if (needsExternal(url)) {
      var open = function(e){
        if (e) {
          try { e.preventDefault(); e.stopPropagation(); } catch (_) {}
        }
        try { v.pause(); } catch (_) {}
        openExternal(mediaUrl(v) || url);
      };
      v.addEventListener('play', open, true);
      v.addEventListener('click', open, true);
      v.addEventListener('error', open, true);
      // Fullscreen player for .mov cannot play inside Android WebView.
      if (modal) {
        setTimeout(function(){ open(null); }, 50);
      }
      return;
    }

    if (modal) {
      v.setAttribute('controls','controls');
      v.controls = true;
    }

    v.addEventListener('error', function(){
      openExternal(mediaUrl(v));
    });

    v.addEventListener('play', function(){
      if (v.readyState < 2) {
        setTimeout(function(){
          if (v.paused) return;
          if (v.currentTime < 0.05 && v.readyState < 2) {
            openExternal(mediaUrl(v));
          }
        }, 1200);
      }
    }, true);

    if (modal) {
      try {
        var p = v.play();
        if (p && p.catch) {
          p.catch(function(){ openExternal(mediaUrl(v)); });
        }
      } catch (_) {
        openExternal(mediaUrl(v));
      }
    }
  }

  function unlockMedia(){
    document.querySelectorAll('video').forEach(mark);
    document.querySelectorAll('iframe').forEach(function(frame){
      try {
        frame.setAttribute('allow', 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen');
        frame.setAttribute('allowfullscreen', 'true');
      } catch (_) {}
    });
  }
  new MutationObserver(unlockMedia).observe(document.documentElement, {childList:true, subtree:true});
  unlockMedia();
})();
"""

private val legacyCssCache = HashMap<String, ByteArray>()

private fun legacyStylesheet(request: WebResourceRequest): WebResourceResponse? {
    val uri = request.url ?: return null
    if (!uri.host.equals("mbarani.ir", ignoreCase = true)) return null
    val path = uri.path ?: return null
    if (!path.startsWith("/assets/index-") || !path.endsWith(".css") || path.contains(".legacy.")) {
        return null
    }
    val legacy = uri.buildUpon().path(path.removeSuffix(".css") + ".legacy.css").build().toString()
    val cached = synchronized(legacyCssCache) { legacyCssCache[legacy] }
    val bytes = cached ?: downloadLegacyCss(legacy) ?: return null
    return WebResourceResponse("text/css", "utf-8", ByteArrayInputStream(bytes))
}

private fun downloadLegacyCss(url: String): ByteArray? {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        instanceFollowRedirects = true
        connectTimeout = 15000
        readTimeout = 20000
        setRequestProperty("Accept", "text/css,*/*")
    }
    return try {
        if (connection.responseCode !in 200..299) return null
        connection.inputStream.use { it.readBytes() }.also { bytes ->
            synchronized(legacyCssCache) { legacyCssCache[url] = bytes }
        }
    } catch (_: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}

private class SiteJsBridge(
    private val onOpenMedia: (String) -> Unit
) {
    @JavascriptInterface
    fun openMedia(url: String?) {
        val target = url?.trim().orEmpty()
        if (target.startsWith("https://") || target.startsWith("http://")) {
            onOpenMedia(target)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SiteScreen(
    startUrl: String,
    title: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onOpenMedia: (String) -> Unit
) {
    var progress by remember { mutableIntStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun goBack() {
        val page = webView
        if (page != null && page.canGoBack()) page.goBack() else onBackClick()
    }

    BackHandler { goBack() }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppTopBar(
                title = title,
                onBackClick = { goBack() },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onHomeClick) {
                            Icon(
                                painterResource(R.drawable.ic_home_24dp),
                                contentDescription = stringResource(R.string.acc_home)
                            )
                        }
                        IconButton(onClick = { goBack() }) {
                            Icon(
                                painterResource(R.drawable.ic_chevron_left_24dp),
                                contentDescription = stringResource(R.string.acc_back)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        val cookies = CookieManager.getInstance()
                        cookies.setAcceptCookie(true)
                        cookies.setAcceptThirdPartyCookies(this, true)
                        setBackgroundColor(Color.parseColor("#07111F"))
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = false
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setSupportMultipleWindows(false)
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            settings.isAlgorithmicDarkeningAllowed = false
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            @Suppress("DEPRECATION")
                            settings.forceDark = WebSettings.FORCE_DARK_OFF
                        }
                        addJavascriptInterface(SiteJsBridge(onOpenMedia), "MbaraniApp")
                        webChromeClient = object : WebChromeClient() {
                            private var customView: View? = null
                            private var customCallback: CustomViewCallback? = null

                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }

                            override fun getDefaultVideoPoster(): Bitmap {
                                return Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
                            }

                            override fun onPermissionRequest(request: PermissionRequest?) {
                                request?.grant(request.resources)
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                val host = context as? QuickAccessActivity
                                if (host == null) {
                                    filePathCallback?.onReceiveValue(null)
                                    return false
                                }
                                host.launchFileChooser(filePathCallback, fileChooserParams)
                                return true
                            }

                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                if (view == null) return
                                onHideCustomView()
                                customView = view
                                customCallback = callback
                                (context as? Activity)?.addContentView(
                                    view,
                                    FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )
                            }

                            override fun onHideCustomView() {
                                val shown = customView ?: return
                                (shown.parent as? ViewGroup)?.removeView(shown)
                                customCallback?.onCustomViewHidden()
                                customView = null
                                customCallback = null
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest
                            ): WebResourceResponse? = legacyStylesheet(request)

                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.evaluateJavascript(SITE_FIX_SCRIPT, null)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
                                val target = request.url?.toString().orEmpty()
                                if (QuickAccessVideo.shouldOpenExternally(target)) {
                                    onOpenMedia(target)
                                    return true
                                }
                                val scheme = request.url.scheme.orEmpty()
                                if (scheme == "http" || scheme == "https") return false
                                runCatching {
                                    view.context.startActivity(
                                        Intent(Intent.ACTION_VIEW, request.url)
                                    )
                                }
                                return true
                            }
                        }
                        loadUrl(startUrl)
                        webView = this
                    }
                }
            )
        }
    }
}
