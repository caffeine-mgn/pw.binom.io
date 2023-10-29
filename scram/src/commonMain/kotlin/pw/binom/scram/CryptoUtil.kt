package pw.binom.scram

import pw.binom.crypto.HMac

object CryptoUtil {
    private const val MIN_ASCII_PRINTABLE_RANGE = 0x21
    private const val MAX_ASCII_PRINTABLE_RANGE = 0x7e
    private const val EXCLUDED_CHAR = ','.code // 0x2c

//    private object SecureRandomHolder {
//        private val INSTANCE: java.security.SecureRandom = java.security.SecureRandom()
//    }

//    /**
//     * Generates a random string (called a 'nonce'), composed of ASCII printable characters, except comma (',').
//     * @param size The length of the nonce, in characters/bytes
//     * @param random The SecureRandom to use
//     * @return The String representing the nonce
//     */
//    fun nonce(size: Int, random: java.security.SecureRandom): String? {
//        if (size <= 0) {
//            throw java.lang.IllegalArgumentException("Size must be positive")
//        }
//        val chars = CharArray(size)
//        var r: Int
//        var i = 0
//        while (i < size) {
//            r = random.nextInt(MAX_ASCII_PRINTABLE_RANGE - MIN_ASCII_PRINTABLE_RANGE + 1) + MIN_ASCII_PRINTABLE_RANGE
//            if (r != EXCLUDED_CHAR) {
//                chars[i++] = r.toChar()
//            }
//        }
//        return chars.concatToString()
//    }

    /**
     * Generates a random string (called a 'nonce'), composed of ASCII printable characters, except comma (',').
     * It uses a default SecureRandom instance.
     * @param size The length of the nonce, in characters/bytes
     * @return The String representing the nonce
     */
    fun nonce(size: Int): String? {
        TODO()
//        return nonce(size, SecureRandomHolder.INSTANCE)
    }

//    /**
//     * Compute the "Hi" function for SCRAM.
//     *
//     * `Hi(str, salt, i):
//     *
//     * U1   := HMAC(str, salt + INT(1))
//     * U2   := HMAC(str, U1)
//     * ...
//     * Ui-1 := HMAC(str, Ui-2)
//     * Ui   := HMAC(str, Ui-1)
//     *
//     * Hi := U1 XOR U2 XOR ... XOR Ui
//     *
//     * where "i" is the iteration count, "+" is the string concatenation
//     * operator, and INT(g) is a 4-octet encoding of the integer g, most
//     * significant octet first.
//     *
//     * Hi() is, essentially, PBKDF2 [RFC2898] with HMAC() as the
//     * pseudorandom function (PRF) and with dkLen == output length of
//     * HMAC() == output length of H().
//     ` *
//     *
//     * @param secretKeyFactory The SecretKeyFactory to generate the SecretKey
//     * @param keyLength The length of the key (in bits)
//     * @param value The char array to compute the Hi function
//     * @param salt The salt
//     * @param iterations The number of iterations
//     * @return The bytes of the computed Hi value
//     */
//    fun hi(
//        secretKeyFactory: javax.crypto.SecretKeyFactory,
//        keyLength: Int,
//        value: CharArray?,
//        salt: ByteArray?,
//        iterations: Int
//    ): ByteArray? {
//        return try {
//            val spec: javax.crypto.spec.PBEKeySpec = javax.crypto.spec.PBEKeySpec(value, salt, iterations, keyLength)
//            val key: javax.crypto.SecretKey = secretKeyFactory.generateSecret(spec)
//            key.getEncoded()
//        } catch (e: java.security.spec.InvalidKeySpecException) {
//            throw java.lang.RuntimeException("Platform error: unsupported PBEKeySpec")
//        }
//    }

    /**
     * Computes the HMAC of a given message.
     *
     * `HMAC(key, str): Apply the HMAC keyed hash algorithm (defined in
     * [RFC2104]) using the octet string represented by "key" as the key
     * and the octet string "str" as the input string.  The size of the
     * result is the hash result size for the hash function in use.  For
     * example, it is 20 octets for SHA-1 (see [RFC3174]).
     ` *
     *
     * @param secretKeySpec A key of the given algorithm
     * @param mac A MAC instance of the given algorithm
     * @param message The message to compute the HMAC
     * @return The bytes of the computed HMAC value
     */
    fun hmac(secretKeySpec: ByteArray, algorithm: HMac.AlgorithmType, message: ByteArray): ByteArray? {
        val mac = HMac(
            algorithm = algorithm,
            key = secretKeySpec
        )
        mac.update(message)
        return mac.finish()
    }

    /**
     * Computes a byte-by-byte xor operation.
     *
     * `XOR: Apply the exclusive-or operation to combine the octet string
     * on the left of this operator with the octet string on the right of
     * this operator.  The length of the output and each of the two
     * inputs will be the same for this use.
     ` *
     *
     * @param value1
     * @param value2
     * @return
     * @throws IllegalArgumentException
     */
    fun xor(value1: ByteArray, value2: ByteArray): ByteArray? {
        require(value1.size == value2.size) { "Both values must have the same length" }
        val result = ByteArray(value1.size)
        for (i in value1.indices) {
            result[i] = (value1[i].toInt() xor value2[i].toInt()).toByte()
        }
        return result
    }
}
