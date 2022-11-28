package pw.binom.scram

import pw.binom.base64.Base64
import pw.binom.crypto.MD5MessageDigest
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.crypto.Sha256MessageDigest
import pw.binom.crypto.Sha512MessageDigest
import pw.binom.security.NoSuchAlgorithmException
import pw.binom.uuid.nextUuid
import kotlin.random.Random

/**
 * Provides building blocks for creating SCRAM authentication client
 */
class ScramClientFunctionalityImpl(
    private var mDigestName: String,
    private var mHmacName: String,
    private var mClientNonce: String,
) : ScramClientFunctionality {
    companion object {
        private val SERVER_FIRST_MESSAGE = "r=([^,]*),s=([^,]*),i=(.*)$".toRegex()
        private val SERVER_FINAL_MESSAGE = "v=([^,]*)\$".toRegex()
        private const val GS2_HEADER = "n,,"
    }

    init {
        require(mDigestName.isNotEmpty()) { "digestName cannot be null or empty" }
        require(mHmacName.isNotEmpty()) { "hmacName cannot be null or empty" }
        require(mClientNonce.isNotEmpty()) { "clientNonce cannot be null or empty" }
    }

    private var mClientFirstMessageBare: String? = null

    private var mIsSuccessful = false
    private var mSaltedPassword: ByteArray? = null
    private var mAuthMessage: String? = null

    private var mState = ScramClientFunctionality.State.INITIAL

    /**
     * Create new ScramClientFunctionalityImpl
     * @param digestName Digest to be used
     * @param hmacName HMAC to be used
     */
    constructor(digestName: String, hmacName: String) : this(
        mDigestName = digestName,
        mHmacName = hmacName,
        mClientNonce = Random.nextUuid().toString(),
    )

    /**
     * Prepares first client message
     *
     * You may want to use [StringPrep.isContainingProhibitedCharacters] in order to check if the
     * username contains only valid characters
     * @param username Username
     * @return prepared first message
     * @throws ScramException if `username` contains prohibited characters
     */
    override fun prepareFirstMessage(username: String): String {
        check(mState == ScramClientFunctionality.State.INITIAL) { "You can call this method only once" }
        return try {
            mClientFirstMessageBare = "n=" + StringPrep.prepAsQueryString(username) + ",r=" + mClientNonce
            mState = ScramClientFunctionality.State.FIRST_PREPARED
            GS2_HEADER + mClientFirstMessageBare
        } catch (e: StringPrep.StringPrepError) {
            mState = ScramClientFunctionality.State.ENDED
            throw ScramException("Username contains prohibited character", e)
        }
    }

    override fun prepareFinalMessage(password: String?, serverFirstMessage: String): String? {
        check(mState == ScramClientFunctionality.State.FIRST_PREPARED) { "You can call this method once only after calling prepareFirstMessage()" }
        val m = SERVER_FIRST_MESSAGE.matchEntire(serverFirstMessage)
        if (m == null) {
            mState = ScramClientFunctionality.State.ENDED
            throw SaslException("Invalid first first server message. \"$serverFirstMessage\" not match to \"$SERVER_FIRST_MESSAGE\"")
        }
        val nonce: String = m.groupValues[1]
        if (!nonce.startsWith(mClientNonce)) {
            mState = ScramClientFunctionality.State.ENDED
            throw SaslException("Invalid first first server message. Invalid nonce. Excepted start with \"$mClientNonce\", but got \"$nonce\"")
        }
        val salt: String = m.groupValues[2]
        val iterationCountString: String = m.groupValues[3]
        val iterations = iterationCountString.toInt()
        if (iterations <= 0) {
            mState = ScramClientFunctionality.State.ENDED
            throw SaslException("Invalid first first server message. Invalid iteration times $iterations")
        }
        return try {
            mSaltedPassword = ScramUtils.generateSaltedPassword(
                password!!,
                Base64.decode(salt),
                iterations,
                mHmacName
            )
            val clientFinalMessageWithoutProof = "c=" + Base64.encode(GS2_HEADER.encodeToByteArray()) + ",r=" + nonce
            mAuthMessage = "$mClientFirstMessageBare,$serverFirstMessage,$clientFinalMessageWithoutProof"
            val clientKey = ScramUtils.computeHmac(mSaltedPassword!!, mHmacName, "Client Key")
            val md = when (mDigestName) {
                "SHA-1" -> Sha1MessageDigest()
                "SHA-256" -> Sha256MessageDigest()
                "SHA-512" -> Sha512MessageDigest()
                "MD5" -> MD5MessageDigest()
                else -> throw NoSuchAlgorithmException(mDigestName)
            }
            md.update(clientKey)
//            val storedKey: ByteArray = java.security.MessageDigest.getInstance(mDigestName).digest(clientKey)
            val storedKey = md.finish()
            val clientSignature = ScramUtils.computeHmac(storedKey, mHmacName, mAuthMessage!!)
            val clientProof: ByteArray = clientKey.copyOf()
            for (i in clientProof.indices) {
                clientProof[i] = (clientProof[i].toInt() xor clientSignature[i].toInt()).toByte()
            }
            mState = ScramClientFunctionality.State.FINAL_PREPARED
            clientFinalMessageWithoutProof + ",p=" + Base64.encode(clientProof)
        } catch (e: NoSuchAlgorithmException) {
            mState = ScramClientFunctionality.State.ENDED
            throw ScramException(e)
        } catch (e: Throwable) {
            mState = ScramClientFunctionality.State.ENDED
            throw ScramException(e)
        }
    }

    @Throws(ScramException::class)
    override fun checkServerFinalMessage(serverFinalMessage: String): Boolean {
        if (mState !== ScramClientFunctionality.State.FINAL_PREPARED) {
            throw IllegalStateException(
                "You can call this method only once after " +
                    "calling prepareFinalMessage()"
            )
        }
        val m = SERVER_FINAL_MESSAGE.matchEntire(serverFinalMessage)
//        val m: java.util.regex.Matcher = SERVER_FINAL_MESSAGE.matcher(serverFinalMessage)
//        if (!m.matches()) {
        if (m == null) {
            mState = ScramClientFunctionality.State.ENDED
            return false
        }
        val serverSignature: ByteArray = Base64.decode(m.groupValues[1])
        mState = ScramClientFunctionality.State.ENDED
        mIsSuccessful = serverSignature.contentEquals(getExpectedServerSignature())
        return mIsSuccessful
    }

    override fun isSuccessful(): Boolean {
        return if (mState === ScramClientFunctionality.State.ENDED) {
            mIsSuccessful
        } else {
            throw IllegalStateException(
                "You cannot call this method before authentication is ended. " +
                    "Use isEnded() to check that"
            )
        }
    }

    override fun isEnded(): Boolean {
        return mState === ScramClientFunctionality.State.ENDED
    }

    override fun getState(): ScramClientFunctionality.State {
        return mState
    }

    @Throws(ScramException::class)
    private fun getExpectedServerSignature(): ByteArray? {
        return try {
            val serverKey = ScramUtils.computeHmac(mSaltedPassword!!, mHmacName, "Server Key")
            ScramUtils.computeHmac(serverKey, mHmacName, mAuthMessage!!)
        } catch (e: Throwable) {
            mState = ScramClientFunctionality.State.ENDED
            throw ScramException(e)
        }
    }
}
