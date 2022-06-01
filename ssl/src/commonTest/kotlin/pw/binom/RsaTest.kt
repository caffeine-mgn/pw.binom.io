package pw.binom

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.base64.Base64
import pw.binom.crypto.PKCS8EncodedKeySpec
import pw.binom.crypto.RSAPrivateKey
import pw.binom.crypto.RSAPrivateKeySpec
import pw.binom.io.socket.ssl.toHex
import pw.binom.ssl.Cipher
import pw.binom.ssl.Key
import pw.binom.ssl.generateRsa
import kotlin.test.Test

class RsaTest {
    val PRIVATE_KEY =
        "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEA2z/cnm7+io4zNAkAJMiIIJhvDBKBcW6GvqHBJFJ11nnRJpMzI5ydY6ezxUzUy1xM540fkvZnNjOCE95DZcehLQIDAQABAkAUQa85eBnOZVr3uMkpnNlu4YaJAACzoTmTzVAR8ghvG0BfjDZWvvgnXZ5SjP74uj0S28xLuEaWsYbpZVi6T4NlAiEA7PBNr+FxX5M+QPK1+uvW31Jyz4GGp4qGGY6AeekIBMcCIQDs40C9b7DDXBHGW+X5uJLBHzEYPOASPjY9e+1clz9OawIhAIUySdn32G4sLjEAwIDAl9iPVu+EFxiUbPJtA5iFAfb9AiAw313O94kfdRJRu0oCMFtOrrHBT2XnPaCRJM0+yhGMiwIgFY7RcG3cOEwiq5s4T5FnRyYqgKGzGs/zQxIrtnDnvus="
    val privateExponent =
        BigInteger.parseString("1060923177610613888977131900342146859424935848562316756024188675335686629510850283028768983015925468347904607778223671322465633133521948790215494595478373")
    val module =
        BigInteger.parseString("11483025977054798091146869257262308625290176830260702434278654535999156835384623070672856801352969244981579368158411562631201260238971857103129084256821549")

    @Test
    fun loadPrivateKey() {
        val privateKey = RSAPrivateKey.load(PKCS8EncodedKeySpec(Base64.decode(PRIVATE_KEY)))
        val privateKey2 = RSAPrivateKey.load(
            RSAPrivateKeySpec(
                modulus = module,
                privateExponent = privateExponent,
            )
        )
        val c = Cipher.getInstance("RSA")
        c.init(Cipher.Mode.ENCODE, privateKey)
        val encoede = c.doFinal("Hello".encodeToByteArray())
        println("->OK ${encoede.toHex()}")
    }

    @Test
    fun test() {
        val pair = Key.generateRsa(512)
        val chiper = Cipher.getInstance("RSA")

        val data = (pair.private as RSAPrivateKey).data
        println("Data: ${Base64.encode(data)}")
        RSAPrivateKey.load(PKCS8EncodedKeySpec(data))
        chiper.init(mode = Cipher.Mode.ENCODE, key = pair.public)
        chiper.doFinal("Hello world".encodeToByteArray())
        chiper.init(mode = Cipher.Mode.ENCODE, key = pair.private)
        chiper.doFinal("Hello world".encodeToByteArray())
    }
}
