package me.joxquin.notivas.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun TecsupAuthWebView(
    authUrl: String,
    onTokenReceived: (token: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    val cookieManager = remember { CookieManager.getInstance() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    userAgentString = userAgentString.replace("; wv", "").replace("Version/4.0 ", "")
                }

                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    private var hasHandled = false

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val currentUrl = request?.url?.toString() ?: return false
                        return handleUrl(currentUrl)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        val currentUrl = url ?: return false
                        return handleUrl(currentUrl)
                    }

                    override fun onPageStarted(view: WebView?, urlStr: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, urlStr, favicon)
                        urlStr?.let { handleUrl(it) }
                    }

                    private fun handleUrl(currentUrl: String): Boolean {
                        if (hasHandled) return true
                        if (currentUrl.startsWith("com.tecsup.uexperience://")) {
                            val queryOrFragment = currentUrl.substringAfter("?", "").ifEmpty {
                                currentUrl.substringAfter("#", "")
                            }
                            if (queryOrFragment.isNotEmpty()) {
                                val params = queryOrFragment.split("&").associate { param ->
                                    val parts = param.split("=", limit = 2)
                                    if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                                }
                                val token = params["token"] ?: params["access_token"] ?: params["suite-token"]
                                if (!token.isNullOrBlank()) {
                                    hasHandled = true
                                    onTokenReceived(token)
                                    return true
                                }
                            }
                        }
                        return false
                    }
                }

                loadUrl(authUrl)
            }
        }
    )
}
