package org.mjdev.desktop.extensions

// import androidx.compose.ui.graphics.ImageBitmap
// import androidx.compose.ui.graphics.toComposeImageBitmap
// import kotlinx.coroutines.CoroutineScope
// import okhttp3.OkHttpClient
// import okhttp3.Request
// import okio.Path
// import org.jetbrains.skia.Image
// import org.mjdev.desktop.extensions.PathExt.bytes
// import org.mjdev.desktop.extensions.PathExt.exists
//
// object Image {
//
//    @Suppress("UnusedReceiverParameter")
//    suspend fun CoroutineScope.loadPicture(
//        src: Any?
//    ): Result<ImageBitmap> = try {
//        when {
//            src == null -> throw (NullPointerException())
//            src is String && src.startsWith("http") -> {
//                val client = OkHttpClient.Builder().build()
//                val request = Request.Builder().url(src).build()
//                val image = client.newCall(request).execute().body?.bytes() ?: byteArrayOf()
//                Result.success(Image.makeFromEncoded(image).toComposeImageBitmap())
//            }
//
//            src is Path && src.exists -> {
//                Result.success(Image.makeFromEncoded(src.bytes).toComposeImageBitmap())
//            }
//
//            else -> throw (IllegalArgumentException("$src"))
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//        Result.failure(e)
//    }
// }
