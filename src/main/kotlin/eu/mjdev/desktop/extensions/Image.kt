package eu.mjdev.desktop.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.skia.Image
import java.io.File

object Image {

    @Suppress("RedundantSuspendModifier", "UnusedReceiverParameter")
    suspend fun CoroutineScope.loadPicture(
        src: Any?
    ): Result<ImageBitmap> = try {
        when {
            src == null -> throw (NullPointerException())
            src is String && src.startsWith("http") -> {
                val client = OkHttpClient.Builder().build()
                val request = Request.Builder().url(src).build()
                val image = client.newCall(request).execute().body.bytes()
                Result.success(Image.makeFromEncoded(image).toComposeImageBitmap())
            }

            src is File && src.exists() -> {
                val image = src.readBytes()
                Result.success(Image.makeFromEncoded(image).toComposeImageBitmap())
            }

            else -> throw (IllegalArgumentException())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

}