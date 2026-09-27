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
actual fun CanvasLoginWebView(
    url: String,
    onLoginSuccess: (cookies: String, csrfToken: String?) -> Unit,
    modifier: Modifier,
    onPageStarted: (() -> Unit)?,
    onPageFinished: (() -> Unit)?
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
                    userAgentString = userAgentString.replace("; wv", "") // Browser look for SSO compatibility
                }

                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    private var hasReportedSuccess = false

                    override fun onPageStarted(view: WebView?, urlStr: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, urlStr, favicon)
                        onPageStarted?.invoke()
                    }

                    override fun onPageFinished(view: WebView?, urlStr: String?) {
                        super.onPageFinished(view, urlStr)
                        onPageFinished?.invoke()

                        if (urlStr != null && !hasReportedSuccess) {
                            checkAndExtractSession(urlStr)
                        }
                    }

                    override fun doUpdateVisitedHistory(view: WebView?, urlStr: String?, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, urlStr, isReload)
                        if (urlStr != null && !hasReportedSuccess) {
                            checkAndExtractSession(urlStr)
                        }
                    }

                    private fun checkAndExtractSession(currentUrl: String) {
                        val isSuccessRedirect = currentUrl.contains("/dashboard") ||
                                currentUrl.contains("login_success=1") ||
                                currentUrl.matches(Regex(".+/[?]?")) ||
                                (currentUrl.contains("/courses") && !currentUrl.contains("/login"))

                        val cookies = cookieManager.getCookie(currentUrl) ?: ""
                        val hasSessionCookie = cookies.contains("canvas_session") || cookies.contains("log_session_id")

                        if (hasSessionCookie && (isSuccessRedirect || !currentUrl.contains("/login"))) {
                            hasReportedSuccess = true
                            val csrfToken = extractCsrfToken(cookies)
                            onLoginSuccess(cookies, csrfToken)
                        }
                    }

                    private fun extractCsrfToken(cookies: String): String? {
                        return cookies.split(";")
                            .map { it.trim() }
                            .firstOrNull { it.startsWith("_csrf_token=") }
                            ?.substringAfter("_csrf_token=")
                            ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                    }
                }

                loadUrl(url)
            }
        },
        update = { webView ->
            // Si la url cambia externamente, recargar
            if (webView.url != url && !webView.url.orEmpty().startsWith(url.trimEnd('/'))) {
                webView.loadUrl(url)
            }
        }
    )
}
