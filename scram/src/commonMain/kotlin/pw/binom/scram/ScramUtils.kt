package pw.binom.scram

import pw.binom.crypto.HMac

object ScramUtils {

    private val INT_1 = byteArrayOf(0, 0, 0, 1)

    /**
     * Creates HMAC
     *
     * @param keyBytes key
     * @param hmacName HMAC name
     * @return Mac
     * @throws InvalidKeyException      if internal error occur while working with SecretKeySpec
     * @throws NoSuchAlgorithmException if hmacName is not supported by the java
     */
    fun createHmac(keyBytes: ByteArray, hmacName: String): HMac {
        return HMac(
            algorithm = HMac.AlgorithmType.get(hmacName),
            key = keyBytes,
        )
//        val key: javax.crypto.spec.SecretKeySpec = javax.crypto.spec.SecretKeySpec(keyBytes, hmacName)
//        val mac: javax.crypto.Mac = javax.crypto.Mac.getInstance(hmacName)
//        mac.init(key)
//        return mac
    }

    /**
     * Computes HMAC byte array for given string
     *
     * @param key      key
     * @param hmacName HMAC name
     * @param string   string for which HMAC will be computed
     * @return computed HMAC
     * @throws InvalidKeyException      if internal error occur while working with SecretKeySpec
     * @throws NoSuchAlgorithmException if hmacName is not supported by the java
     */
    fun computeHmac(key: ByteArray, hmacName: String, string: String): ByteArray {
        val mac = createHmac(keyBytes = key, hmacName = hmacName)
        mac.update(string.encodeToByteArray())
        return mac.finish()
    }

    /**
     * Generates salted password.
     *
     * @param password        Clear form password, i.e. what user typed
     * @param salt            Salt to be used
     * @param iterationsCount Iterations for 'salting'
     * @param hmacName        HMAC to be used
     * @return salted password
     * @throws InvalidKeyException      if internal error occur while working with SecretKeySpec
     * @throws NoSuchAlgorithmException if hmacName is not supported by the java
     */
    fun generateSaltedPassword(
        password: String,
        salt: ByteArray,
        iterationsCount: Int,
        hmacName: String?
    ): ByteArray? {
        val mac = createHmac(
            password.encodeToByteArray(),
            hmacName!!
        )
        mac.update(salt)
        mac.update(ScramUtils.INT_1)
        val result: ByteArray = mac.finish()
        var previous: ByteArray? = null
        for (i in 1 until iterationsCount) {
            mac.update(previous ?: result)
            previous = mac.finish()
            for (x in result.indices) {
                result[x] = (result[x].toInt() xor previous[x].toInt()).toByte()
            }
        }
        return result
    }
}
