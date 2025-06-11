package org.mjdev.desktop.components.web

//import java.net.CookieHandler
//import java.net.URI

object CookieManagerCompat {
//    fun setCookie(
//        url: String,
//        headers: HashMap<String, String>
//    ) {
//        LinkedHashMap<String, List<String>>().apply {
//            this["Set-Cookie"] = headers.map { "${it.key}=${it.value}" }
//        }.also { nHeaders ->
//            CookieHandler.getDefault().put(URI.create(url), nHeaders)
//        }
//    }
//
//    fun getCookie(uri: URI): MutableMap<String, String>? {
//        return CookieHandler.getDefault().get(uri, HashMap()).map {
//            it.key to it.value[0]
//        }.toMap().toMutableMap()
//    }
}