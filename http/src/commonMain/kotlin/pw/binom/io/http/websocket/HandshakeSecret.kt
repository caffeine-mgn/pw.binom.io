package pw.binom.io.http.websocket

import pw.binom.base64.Base64
import pw.binom.io.MessageDigest
import pw.binom.io.Sha1
import pw.binom.io.use
import pw.binom.toByteBufferUTF8

object HandshakeSecret{
    fun generateResponse(sha1: MessageDigest, request: String): String {
        sha1.init()
        (request + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteBufferUTF8().use {
            sha1.update(it)
        }
        return Base64.encode(sha1.finish())
    }
}