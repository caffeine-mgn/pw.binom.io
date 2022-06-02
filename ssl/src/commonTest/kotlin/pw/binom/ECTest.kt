package pw.binom

import pw.binom.base64.Base64
import pw.binom.crypto.ECPrivateKey
import pw.binom.crypto.ECPublicKey
import pw.binom.ssl.Key
import pw.binom.ssl.Nid
import pw.binom.ssl.generateEcdsa
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ECTest {

    val NATIVE_PUBLIC =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAET15MuvC6U0fsRJq1W6LfSqwNYNes6ZPmrzWcQ6WDxMpDyqLh29SOAMPn485QMAlzkEebEzwNXqWfsHPhHzYtUA=="
    val NATIVE_PRIVATE =
        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgiLg2bUzZIn6hjkIhjOu2c1aFQ/L0eQc7Nb9Ov+UkXzehRANCAARPXky68LpTR+xEmrVbot9KrA1g16zpk+avNZxDpYPEykPKouHb1I4Aw+fjzlAwCXOQR5sTPA1epZ+wc+EfNi1Q"
    val JVM_PUBLIC =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE0eDKK+n2g3szwU5yxKLs5JfDqzhva7h1JFUpkqnBYTJjtTgxtQCHO9cGbcHK2S6QU/IynLnxvHda3atDUKtKJA=="
    val JVM_PRIVATE =
        "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgoSF+Y/0VQS0uMGTxFfIHnVSS5uaa1VZ83U3RgsIZFhWgCgYIKoZIzj0DAQehRANCAATR4Mor6faDezPBTnLEouzkl8OrOG9ruHUkVSmSqcFhMmO1ODG1AIc71wZtwcrZLpBT8jKcufG8d1rdq0NQq0ok"

    @Test
    fun nativeLoadTest() {
        val public = ECPublicKey.load(Base64.decode(NATIVE_PUBLIC))
        assertEquals(NATIVE_PUBLIC, Base64.encode(public.data))
        val private = ECPrivateKey.load(Base64.decode(NATIVE_PRIVATE))
//        assertEquals(NATIVE_PRIVATE, Base64.encode(private.data))
    }

    @Test
    fun jvmLoadTest() {
        val public = ECPublicKey.load(Base64.decode(JVM_PUBLIC))
        assertEquals(JVM_PUBLIC, Base64.encode(public.data), "Public key faild")
        val private = ECPrivateKey.load(Base64.decode(JVM_PRIVATE))
//        assertEquals(JVM_PRIVATE, Base64.encode(private.data), "Private Key faild")
    }

    @Ignore
    @Test
    fun loadTest() {
        val e = when (Environment.platform) {
            Platform.JVM -> "JVM"
            else -> "NATIVE"
        }
        val pair = Key.generateEcdsa(Nid.secp256r1)
        println("val ${e}_PUBLIC = \"${Base64.encode(pair.public.data)}\"")
        println("val ${e}_PRIVATE = \"${Base64.encode(pair.private.data)}\"")
    }
}
