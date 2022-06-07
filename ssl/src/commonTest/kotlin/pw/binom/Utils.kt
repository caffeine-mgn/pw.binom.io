package pw.binom

import pw.binom.base64.Base64
import pw.binom.crypto.ECPrivateKey
import pw.binom.crypto.ECPublicKey
import pw.binom.io.file.openWrite
import pw.binom.io.file.relative
import pw.binom.io.file.workDirectoryFile
import pw.binom.io.socket.ssl.writeAllBytes
import pw.binom.io.use
import pw.binom.ssl.*

object Utils {
    const val PUBLIC_EC_BASE64 =
        "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEDQrri3VDE2cbV3Hs1xhkCOJST2Kh0iH3YxYp7k/z12AZO1KuqSZwDOWBCAgZt28tirmAE83gXsdUqOWXSQWIFA=="
    const val PRIVATE_EC_BASE64 =
        "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQglOJooqMiGAPFBUzhX98KbsFlNTh6gRD7FdA9PVYqzBmgBwYFK4EEAAqhRANCAAQNCuuLdUMTZxtXcezXGGQI4lJPYqHSIfdjFinuT/PXYBk7Uq6pJnAM5YEICBm3by2KuYATzeBex1So5ZdJBYgU"
    const val PUBLIC_COMPONENT_COMPRESSED_BASE64 = "Ag0K64t1QxNnG1dx7NcYZAjiUk9iodIh92MWKe5P89dg"
    const val PUBLIC_UNCOMPONENT_COMPRESSED_BASE64 =
        "BA0K64t1QxNnG1dx7NcYZAjiUk9iodIh92MWKe5P89dgGTtSrqkmcAzlgQgIGbdvLYq5gBPN4F7HVKjll0kFiBQ="

    fun getPair() =
        Key.Pair(
            public = ECPublicKey.load(Base64.decode(PUBLIC_EC_BASE64)),
            private = ECPrivateKey.load(Base64.decode(PRIVATE_EC_BASE64))
        )

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
