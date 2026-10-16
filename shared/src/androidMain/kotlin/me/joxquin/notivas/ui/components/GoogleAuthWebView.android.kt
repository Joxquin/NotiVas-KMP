package me.joxquin.notivas.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
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
actual fun GoogleAuthWebView(
    authUrl: String,
    redirectUri: String,
    onAuthCodeReceived: (authCode: String) -> Unit,
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
                    // Google blockeada webviews embebidas si detecta '; wv' (Chrome Custom Tabs lookalike)
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
                        if (currentUrl.startsWith(redirectUri) || currentUrl.contains("code=") || currentUrl.contains("oauth2callback")) {
                            try {
                                val uri = Uri.parse(currentUrl)
                                val code = uri.getQueryParameter("code")
                                if (!code.isNullOrBlank()) {
                                    hasHandled = true
                                    onAuthCodeReceived(code)
                                    return true
                                }
                                val error = uri.getQueryParameter("error")
                                if (!error.isNullOrBlank()) {
                                    hasHandled = true
                                    onCancel()
                                    return true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
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
