package pw.binom.scram

/**
 * Definition of the functionality to be provided by every ScramMechanism.
 *
 * Every ScramMechanism implemented must provide implementations of their respective digest and hmac
 * function that will not throw a RuntimeException on any JVM, to guarantee true portability of this library.
 */
interface ScramMechanism {
    /**
     * The name of the mechanism, which must be a value registered under IANA:
     * [
 * SASL SCRAM Family Mechanisms](https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml#scram)
     * @return The mechanism name
     */
    fun getName(): String?

    /**
     * Calculate a message digest, according to the algorithm of the SCRAM mechanism.
     * @param message the message
     * @return The calculated message digest
     * @throws RuntimeException If the algorithm is not provided by current JVM or any included implementations
     */
    fun digest(message: ByteArray?): ByteArray?

    /**
     * Calculate the hmac of a key and a message, according to the algorithm of the SCRAM mechanism.
     * @param key the key
     * @param message the message
     * @return The calculated message hmac instance
     * @throws RuntimeException If the algorithm is not provided by current JVM or any included implementations
     */
    fun hmac(key: ByteArray?, message: ByteArray?): ByteArray?

    /**
     * Returns the length of the key length  of the algorithm.
     * @return The length (in bits)
     */
    fun algorithmKeyLength(): Int

    /**
     * Whether this mechanism supports channel binding
     * @return True if it supports channel binding, false otherwise
     */
    fun supportsChannelBinding(): Boolean

    /**
     * Compute the salted password
     * @return The salted password
     */
    fun saltedPassword(
        stringPreparation: StringPreparation?,
        password: String?,
        salt: ByteArray?,
        iteration: Int
    ): ByteArray?
}
