package pw.binom.scram

import pw.binom.security.NoSuchAlgorithmException
import pw.binom.uuid.nextUuid
import kotlin.random.Random

class ScramSaslClient private constructor(
    digestName: String,
    hmacName: String,
    clientNonce: String?,
    val mechanism: String,
) {
    companion object {

        fun create(algorithms: Collection<String>, clientNonce: String? = Random.nextUuid().toString()) = when {
            "SCRAM-SHA-512" in algorithms -> sha512()
            "SCRAM-SHA-256" in algorithms -> sha256()
            "SCRAM-SHA-1" in algorithms -> sha1()
            "SCRAM-MD5" in algorithms -> md5()
            else -> throw NoSuchAlgorithmException("Supported SCRAM mechanism not found. Full server list: $algorithms")
        }

        fun create(algorithm: String, clientNonce: String? = Random.nextUuid().toString()) =
            when (algorithm.lowercase().replace("-", "").replace(" ", "").trim()) {
                "sha1", "scramsha1" -> sha1(clientNonce = clientNonce)
                "sha256", "scramsha256" -> sha256(clientNonce = clientNonce)
                "sha512", "scramsha512" -> sha512(clientNonce = clientNonce)
                "md5", "scrammd5" -> md5(clientNonce = clientNonce)
                else -> NoSuchAlgorithmException(algorithm)
            }

        fun sha1(clientNonce: String? = Random.nextUuid().toString()) = ScramSaslClient(
            digestName = "SHA-1",
            hmacName = "HmacSHA1",
            clientNonce = clientNonce,
            mechanism = "SCRAM-SHA-1",
        )

        fun sha256(clientNonce: String? = Random.nextUuid().toString()) = ScramSaslClient(
            digestName = "SHA-256",
            hmacName = "HmacSHA256",
            clientNonce = clientNonce,
            mechanism = "SCRAM-SHA-256",
        )

        fun sha512(clientNonce: String? = Random.nextUuid().toString()) = ScramSaslClient(
            digestName = "SHA-512",
            hmacName = "HmacSHA512",
            clientNonce = clientNonce,
            mechanism = "SCRAM-SHA-512",
        )

        fun md5(clientNonce: String? = Random.nextUuid().toString()) = ScramSaslClient(
            digestName = "MD5",
            hmacName = "HmacMD5",
            clientNonce = clientNonce,
            mechanism = "SCRAM-MD5",
        )
    }

    private var mState = State.INITIAL
    val state
        get() = mState
    private var mPassword: String? = null
    private val mScramClientFunctionality = ScramClientFunctionalityImpl(digestName, hmacName, clientNonce!!)

    fun start(username: String, password: String?): String {
        mPassword = password
        mState = State.CLIENT_FIRST_SENT
        return mScramClientFunctionality.prepareFirstMessage(username)
    }

    private fun handleServerFinal(message: String): Boolean {
        return mScramClientFunctionality.checkServerFinalMessage(message)
    }

    private fun handleServerFirst(message: String): String? {
        return mScramClientFunctionality.prepareFinalMessage(mPassword, message)
    }

    fun exchange(message: String): String? {
        if (mState != State.ENDED) {
            when (mState) {
                State.INITIAL -> {
                    val msg = handleServerFirst(message)
                    if (msg != null) {
                        mState = State.CLIENT_FINAL_SENT
                        return msg
                    } else {
                        mState = State.ENDED
                        throw ScramException("Some thing wrong 1")
                    }
                }

                State.CLIENT_FIRST_SENT -> {
                    val msg = handleServerFirst(message)
                    if (msg != null) {
                        mState = State.CLIENT_FINAL_SENT
                        return msg
                    } else {
                        mState = State.ENDED
                        throw ScramException("Some thing wrong 2")
                    }
                }

                State.CLIENT_FINAL_SENT -> {
                    if (handleServerFinal(message)) {
//                        mIsSuccess = true
//                        notifySuccess()
                        mState = State.ENDED
                        return null
                    } else {
                        mState = State.ENDED
                        throw ScramException("Some thing wrong 3")
                    }
                }

                State.ENDED -> TODO("mState=$mState")
            }
        } else {
            TODO()
        }
    }

    enum class State {
        INITIAL, CLIENT_FIRST_SENT, CLIENT_FINAL_SENT, ENDED
    }
}
