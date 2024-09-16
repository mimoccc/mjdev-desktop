package eu.mjdev.desktop.helpers.adb.helpers

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

internal object PKCS8 {
    private const val PREFIX = "-----BEGIN PRIVATE KEY-----"
    private const val SUFFIX = "-----END PRIVATE KEY-----"

    fun parse(bytes: ByteArray): PrivateKey {
        val string = String(bytes).replace(PREFIX, "").replace(SUFFIX, "").replace("\n", "")
        val encoded = Base64.getDecoder().decode(string)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec)
    }
}