package pw.binom.scram

object ScramFunctions {
    private val CLIENT_KEY_HMAC_KEY = "Client Key".encodeToByteArray()
    private val SERVER_KEY_HMAC_KEY = "Server Key".encodeToByteArray()

    /**
     * Compute the salted password, based on the given SCRAM mechanism, the String preparation algorithm,
     * the provided salt and the number of iterations.
     *
     * `SaltedPassword  := Hi(Normalize(password), salt, i)
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param stringPreparation The String preparation
     * @param password The non-salted password
     * @param salt The bytes representing the salt
     * @param iteration The number of iterations
     * @return The salted password
     */
    fun saltedPassword(
        scramMechanism: ScramMechanism,
        stringPreparation: StringPreparation?,
        password: String?,
        salt: ByteArray?,
        iteration: Int
    ): ByteArray? {
        return scramMechanism.saltedPassword(stringPreparation, password, salt, iteration)
    }

    /**
     * Computes the HMAC of the message and key, using the given SCRAM mechanism.
     * @param scramMechanism The SCRAM mechanism
     * @param message The message to compute the HMAC
     * @param key The key used to initialize the MAC
     * @return The computed HMAC
     */
    fun hmac(scramMechanism: ScramMechanism, message: ByteArray?, key: ByteArray?): ByteArray? {
        return scramMechanism.hmac(key, message)
    }

    /**
     * Generates a client key, from the salted password.
     *
     * `ClientKey       := HMAC(SaltedPassword, "Client Key")
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param saltedPassword The salted password
     * @return The client key
     */
    fun clientKey(scramMechanism: ScramMechanism?, saltedPassword: ByteArray?): ByteArray? {
        return hmac(scramMechanism!!, CLIENT_KEY_HMAC_KEY, saltedPassword)
    }

    /**
     * Generates a client key from the password and salt.
     *
     * `SaltedPassword  := Hi(Normalize(password), salt, i)
     * ClientKey       := HMAC(SaltedPassword, "Client Key")
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param stringPreparation The String preparation
     * @param password The non-salted password
     * @param salt The bytes representing the salt
     * @param iteration The number of iterations
     * @return The client key
     */
    fun clientKey(
        scramMechanism: ScramMechanism?,
        stringPreparation: StringPreparation?,
        password: String?,
        salt: ByteArray?,
        iteration: Int
    ): ByteArray? {
        return clientKey(scramMechanism, saltedPassword(scramMechanism!!, stringPreparation, password, salt, iteration))
    }

    /**
     * Generates a server key, from the salted password.
     *
     * `ServerKey       := HMAC(SaltedPassword, "Server Key")
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param saltedPassword The salted password
     * @return The server key
     */
    fun serverKey(scramMechanism: ScramMechanism?, saltedPassword: ByteArray?): ByteArray? {
        return hmac(scramMechanism!!, SERVER_KEY_HMAC_KEY, saltedPassword)
    }

    /**
     * Generates a server key from the password and salt.
     *
     * `SaltedPassword  := Hi(Normalize(password), salt, i)
     * ServerKey       := HMAC(SaltedPassword, "Server Key")
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param stringPreparation The String preparation
     * @param password The non-salted password
     * @param salt The bytes representing the salt
     * @param iteration The number of iterations
     * @return The server key
     */
    fun serverKey(
        scramMechanism: ScramMechanism?,
        stringPreparation: StringPreparation?,
        password: String?,
        salt: ByteArray?,
        iteration: Int
    ): ByteArray? {
        return serverKey(scramMechanism, saltedPassword(scramMechanism!!, stringPreparation, password, salt, iteration))
    }

    /**
     * Computes the hash function of a given value, based on the SCRAM mechanism hash function.
     * @param scramMechanism The SCRAM mechanism
     * @param value The value to hash
     * @return The hashed value
     */
    fun hash(scramMechanism: ScramMechanism, value: ByteArray?): ByteArray? {
        return scramMechanism.digest(value)
    }

    /**
     * Generates a stored key, from the salted password.
     *
     * `StoredKey       := H(ClientKey)
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param clientKey The client key
     * @return The stored key
     */
    fun storedKey(scramMechanism: ScramMechanism, clientKey: ByteArray?): ByteArray? {
        return hash(scramMechanism, clientKey)
    }

    /**
     * Computes the SCRAM client signature.
     *
     * `ClientSignature := HMAC(StoredKey, AuthMessage)
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param storedKey The stored key
     * @param authMessage The auth message
     * @return The client signature
     */
    fun clientSignature(scramMechanism: ScramMechanism?, storedKey: ByteArray?, authMessage: String): ByteArray? {
        return hmac(scramMechanism!!, authMessage.encodeToByteArray(), storedKey)
    }

    /**
     * Computes the SCRAM client proof to be sent to the server on the client-final-message.
     *
     * `ClientProof     := ClientKey XOR ClientSignature
     ` *
     *
     * @param clientKey The client key
     * @param clientSignature The client signature
     * @return The client proof
     */
    fun clientProof(clientKey: ByteArray?, clientSignature: ByteArray?): ByteArray? {
        TODO()
//        return CryptoUtil.xor(clientKey, clientSignature)
    }

    /**
     * Compute the SCRAM server signature.
     *
     * `ServerSignature := HMAC(ServerKey, AuthMessage)
     ` *
     *
     * @param scramMechanism The SCRAM mechanism
     * @param serverKey The server key
     * @param authMessage The auth message
     * @return The server signature
     */
    fun serverSignature(scramMechanism: ScramMechanism?, serverKey: ByteArray?, authMessage: String): ByteArray? {
        return clientSignature(scramMechanism, serverKey, authMessage)
    }

    /**
     * Verifies that a provided client proof is correct.
     * @param scramMechanism The SCRAM mechanism
     * @param clientProof The provided client proof
     * @param storedKey The stored key
     * @param authMessage The auth message
     * @return True if the client proof is correct
     */
    fun verifyClientProof(
        scramMechanism: ScramMechanism,
        clientProof: ByteArray?,
        storedKey: ByteArray?,
        authMessage: String
    ): Boolean {
        TODO()
//        val clientSignature = clientSignature(scramMechanism, storedKey, authMessage)
//        val clientKey: ByteArray = CryptoUtil.xor(clientSignature, clientProof)
//        val computedStoredKey = hash(scramMechanism, clientKey)
//        return storedKey.contentEquals(computedStoredKey)
    }

    /**
     * Verifies that a provided server proof is correct.
     * @param scramMechanism The SCRAM mechanism
     * @param serverKey The server key
     * @param authMessage The auth message
     * @param serverSignature The provided server signature
     * @return True if the server signature is correct
     */
    fun verifyServerSignature(
        scramMechanism: ScramMechanism?,
        serverKey: ByteArray?,
        authMessage: String,
        serverSignature: ByteArray?
    ): Boolean {
        return serverSignature(scramMechanism, serverKey, authMessage)!!.contentEquals(serverSignature)
//        return java.util.Arrays.equals(serverSignature(scramMechanism, serverKey, authMessage), serverSignature)
    }
}
