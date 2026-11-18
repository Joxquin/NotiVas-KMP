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
                        return handleUrl(view, currentUrl)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        val currentUrl = url ?: return false
                        return handleUrl(view, currentUrl)
                    }

                    override fun onPageStarted(view: WebView?, urlStr: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, urlStr, favicon)
                        urlStr?.let { handleUrl(view, it) }
                    }

                    private fun handleUrl(view: WebView?, currentUrl: String): Boolean {
                        if (hasHandled) return true

                        if (currentUrl.startsWith("com.tecsup.uexperience://")) {
                            view?.stopLoading()

                            val queryOrFragment = currentUrl.substringAfter("?", "").ifEmpty {
                                currentUrl.substringAfter("#", "")
                            }

                            if (queryOrFragment.isNotEmpty()) {
                                val params = queryOrFragment.split("&").associate { param ->
                                    val parts = param.split("=", limit = 2)
                                    val key = parts[0]
                                    val rawVal = if (parts.size == 2) parts[1] else ""
                                    val decodedVal = try {
                                        java.net.URLDecoder.decode(rawVal, "UTF-8")
                                    } catch (_: Exception) {
                                        rawVal
                                    }
                                    key to decodedVal
                                }

                                val candidate = params["idUserToken"]
                                    ?: params["token"]
                                    ?: params["access_token"]
                                    ?: params["suite-token"]
                                    ?: params["id_token"]

                                val token = candidate?.let { extractJwtToken(it) }
                                if (!token.isNullOrBlank()) {
                                    hasHandled = true
                                    onTokenReceived(token)
                                }
                            }
                            // Always return true to consume custom scheme and avoid ERR_UNKNOWN_URL_SCHEME
                            return true
                        }
                        return false
                    }

                    private fun extractJwtToken(raw: String): String {
                        val trimmed = raw.trim()
                        val jsonRegex = """\"token\"\s*:\s*\"([^\"]+)\"""".toRegex()

                        // 1. Direct JSON match
                        jsonRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

                        // 2. Base64 decoded check (idUserToken is base64 of {"token":"ey..."})
                        try {
                            val decoded = android.util.Base64.decode(trimmed, android.util.Base64.DEFAULT).decodeToString()
                            jsonRegex.find(decoded)?.groupValues?.get(1)?.let { return it }
                            if (decoded.startsWith("eyJ") && decoded.count { it == '.' } == 2) {
                                return decoded
                            }
                        } catch (_: Exception) {
                        }

                        // 3. Direct JWT check
                        if (trimmed.startsWith("eyJ") && trimmed.count { it == '.' } == 2) {
                            return trimmed
                        }

                        return trimmed
                    }
                }

                loadUrl(authUrl)
            }
        }
    )
}
