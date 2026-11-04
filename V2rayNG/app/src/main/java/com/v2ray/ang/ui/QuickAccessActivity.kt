package com.v2ray.ang.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
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
            onHomeClick = { finish() }
        )
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }
}

private const val SITE_FIX_SCRIPT = """
(function(){
  if (window.__mbAppFix) return;
  window.__mbAppFix = true;
  var css = document.createElement('style');
  css.textContent = '[class*="92dvh"]{max-height:min(92vh,40rem)!important}[class*="100dvh"]{height:100vh!important;min-height:100vh!important}[class*="backdrop-blur"]{-webkit-backdrop-filter:none!important;backdrop-filter:none!important}button video{pointer-events:none!important}video{background:#111;max-width:100%}';
  document.documentElement.appendChild(css);
  function frame(v){
    if (!v || v.dataset.mbFrame) return;
    v.dataset.mbFrame = '1';
    v.setAttribute('playsinline','');
    v.setAttribute('webkit-playsinline','');
    v.playsInline = true;
    if (v.closest('button')) {
      v.muted = true;
      var show = function(){
        var p = v.play();
        if (p && p.then) p.then(function(){ setTimeout(function(){ try { v.pause(); } catch (e) {} }, 400); }).catch(function(){});
      };
      if (v.readyState >= 2) show();
      else v.addEventListener('loadeddata', show, {once:true});
    }
  }
  function scan(){ document.querySelectorAll('video').forEach(frame); }
  new MutationObserver(scan).observe(document.documentElement, {childList:true, subtree:true});
  document.addEventListener('click', function(){
    setTimeout(function(){
      document.querySelectorAll('[role="dialog"] video').forEach(function(v){
        v.playsInline = true;
        v.setAttribute('playsinline','');
        var p = v.play();
        if (p && p.catch) p.catch(function(){ v.muted = true; v.play().catch(function(){}); });
      });
    }, 180);
  }, true);
  scan();
})();
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SiteScreen(
    startUrl: String,
    title: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
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
                                painterResource(R.drawable.ic_arrow_back_24dp),
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
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setSupportMultipleWindows(true)
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36"
                        webChromeClient = object : WebChromeClient() {
                            private var customView: View? = null
                            private var customCallback: CustomViewCallback? = null

                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }

                            override fun getDefaultVideoPoster(): Bitmap {
                                return Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
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

                            override fun onCreateWindow(
                                view: WebView,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: android.os.Message?
                            ): Boolean {
                                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                                val popup = WebView(view.context)
                                popup.webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        popupView: WebView,
                                        request: WebResourceRequest
                                    ): Boolean {
                                        view.loadUrl(request.url.toString())
                                        return true
                                    }
                                }
                                transport.webView = popup
                                resultMsg.sendToTarget()
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
                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.evaluateJavascript(SITE_FIX_SCRIPT, null)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
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
