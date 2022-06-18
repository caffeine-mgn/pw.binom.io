package pw.binom.io.socket.ssl

import pw.binom.Environment
import pw.binom.copyTo
import pw.binom.crypto.RSAPrivateKey
import pw.binom.crypto.RSAPublicKey
import pw.binom.io.*
import pw.binom.io.file.openWrite
import pw.binom.io.file.relative
import pw.binom.io.file.workDirectoryFile
import pw.binom.ssl.*
import kotlin.random.Random
import kotlin.test.Test

fun Input.readAllBytes() = ByteArrayOutput().use { output ->
    copyTo(output)
    output.toByteArray()
}

fun Output.writeAllBytes(data: ByteArray) {
    ByteBuffer.wrap(data).use {
        write(it)
    }
}

class ChiperTest {

    private fun findPair(name: String): Key.Pair<out RSAPublicKey, out RSAPrivateKey> {
        val publicFile = Environment.workDirectoryFile.relative("$name.pub")
        val privateFile = Environment.workDirectoryFile.relative(name)
        println("public-file: $publicFile")

//        if (publicFile.isFile && privateFile.isFile) {
//            return Key.Pair(
//                public = Key.Public(algorithm = KeyAlgorithm.RSA, publicFile.openRead().use { it.readAllBytes() }),
//                private = Key.Private(algorithm = KeyAlgorithm.RSA, privateFile.openRead().use { it.readAllBytes() }),
//            )
//        }

        val pair = Key.generateRsa(512)
        publicFile.openWrite().use { it.writeAllBytes(pair.public.data) }
        privateFile.openWrite().use { it.writeAllBytes(pair.private.data) }
        return pair
    }

    @Test
    fun test2() {
        val pair = Key.generateEcdsa(Nid.secp256k1)
        val dd = Cipher.getInstance("ECDSA")
        dd.init(Cipher.Mode.ENCODE, pair.public)
        dd.doFinal("Hello world".encodeToByteArray())
    }

    @Test
    fun test() {
        val pair = findPair("test")
        val public = pair.public.data.toHex()
        val private = pair.private.data.toHex()
        val encideInstance = Cipher.getInstance("RSA")
        encideInstance.init(
            mode = Cipher.Mode.ENCODE,
            key = pair.public,
        )

        val decodeInstance = Cipher.getInstance("RSA")
        decodeInstance.init(
            mode = Cipher.Mode.DECODE,
            key = pair.private,
        )
        val msg = "Hello world" // String.gen(100)
        val encoeded = encideInstance.doFinal(msg.encodeToByteArray())
        println("encoded [${encoeded.size}]: ${encoeded.toHex()}")
        println("decoded: ${decodeInstance.doFinal(encoeded).decodeToString()}")

        println("->$public")
        println("->$private")
    }
}

fun String.Companion.gen(length: Int): String {
    val sb = StringBuilder(length)
    repeat(length) {
        sb.append(Random.nextInt('0'.code, '9'.code).toChar())
    }
    return sb.toString()
}
