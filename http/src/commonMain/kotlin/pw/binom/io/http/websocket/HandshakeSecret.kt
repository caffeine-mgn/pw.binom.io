package pw.binom.io.http.websocket

import pw.binom.base64.Base64
import pw.binom.io.use
import pw.binom.security.MessageDigest
import pw.binom.toByteBufferUTF8
import kotlin.random.Random

object HandshakeSecret {
    fun generateResponse(sha1: MessageDigest, request: String): String {
        sha1.init()
        (request + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteBufferUTF8().use {
            sha1.update(it)
        }
        return Base64.encode(sha1.finish())
    }

    fun generateRequestKey(): String {
        val arr = ByteArray(16)
        Random.nextBytes(arr)
        return Base64.encode(arr)
    }
}
