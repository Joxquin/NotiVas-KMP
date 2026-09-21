package me.joxquin.notivas.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

private object ImageCacheManager {
    val cache = mutableMapOf<String, ImageBitmap>()
    val imageClient by lazy {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
            }
            followRedirects = true
        }
    }
}

@Composable
fun AsyncAvatarImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit
) {
    if (url.isNullOrBlank()) {
        fallback()
        return
    }

    var bitmap by remember(url) {
        mutableStateOf(ImageCacheManager.cache[url])
    }

    LaunchedEffect(url) {
        if (bitmap == null) {
            try {
                println("AsyncAvatarImage: Loading avatar from $url")
                val bytes = withContext(Dispatchers.Default) {
                    ImageCacheManager.imageClient.get(url).body<ByteArray>()
                }
                println("AsyncAvatarImage: Received ${bytes.size} bytes for $url")
                if (bytes.isNotEmpty()) {
                    val decoded = bytes.decodeToImageBitmap()
                    ImageCacheManager.cache[url] = decoded
                    bitmap = decoded
                    println("AsyncAvatarImage: Successfully decoded bitmap (${decoded.width}x${decoded.height})")
                }
            } catch (e: Exception) {
                println("AsyncAvatarImage error loading $url: ${e::class.simpleName} - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    val currentBitmap = bitmap
    if (currentBitmap != null) {
        val imageModifier = if (shape != null) {
            modifier.clip(shape)
        } else {
            modifier
        }
        Image(
            bitmap = currentBitmap,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = contentScale
        )
    } else {
        fallback()
    }
}
