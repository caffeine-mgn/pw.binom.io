package pw.binom

import pw.binom.io.file.openWrite
import pw.binom.io.file.relative
import pw.binom.io.file.workDirectoryFile
import pw.binom.io.socket.ssl.writeAllBytes
import pw.binom.io.use
import pw.binom.ssl.*

object Utils {
    fun findPair(name: String, algorithm: KeyAlgorithm): Key.Pair<out Key.Public, out Key.Private> {
        val publicFile = Environment.workDirectoryFile.relative("$name.pub")
        val privateFile = Environment.workDirectoryFile.relative(name)
        println("public-file: $publicFile")

//        if (publicFile.isFile && privateFile.isFile) {
//            return Key.Pair(
//                public = Key.Public(algorithm = algorithm, publicFile.openRead().use { it.readAllBytes() }),
//                private = Key.Private(algorithm = algorithm, privateFile.openRead().use { it.readAllBytes() }),
//            )
//        }
        val pair = when (algorithm) {
            KeyAlgorithm.RSA -> Key.generateRsa(512)
            KeyAlgorithm.ECDSA -> Key.generateEcdsa(Nid.secp256k1)
        }
        publicFile.openWrite().use { it.writeAllBytes(pair.public.data) }
        privateFile.openWrite().use { it.writeAllBytes(pair.private.data) }
        return pair
    }
}
