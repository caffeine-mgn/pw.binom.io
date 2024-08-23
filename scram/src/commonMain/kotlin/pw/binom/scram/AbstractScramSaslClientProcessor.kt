package pw.binom.scram

import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import pw.binom.uuid.nextUuid
import kotlin.concurrent.Volatile
import kotlin.random.Random

abstract class AbstractScramSaslClientProcessor(
  private var mListener: ScramSaslClientProcessor.Listener,
  private var mSender: ScramSaslClientProcessor.Sender,
  digestName: String,
  hmacName: String,
  clientNonce: String?,
) : ScramSaslClientProcessor {
  init {
    require(digestName.isNotEmpty()) { "digestName cannot be empty" }
    require(hmacName.isNotEmpty()) { "hmacName cannot be empty" }
  }

  private var mPassword: String? = null
  private var mState = State.INITIAL
  private val mScramClientFunctionality = ScramClientFunctionalityImpl(digestName, hmacName, clientNonce!!)

  private val lock = ReentrantLock()

  @Volatile
  private var mIsSuccess = false

  @Volatile
  private var mAborted = false

  /**
   * Creates new AbstractScramSaslClientProcessor
   * @param listener Listener of the client processor (this object)
   * @param sender Sender used to send messages to the server
   * @param digestName Digest to be used
   * @param hmacName HMAC to be used
   */
  constructor(
    listener: ScramSaslClientProcessor.Listener,
    sender: ScramSaslClientProcessor.Sender,
    digestName: String,
    hmacName: String,
  ) : this(
    mListener = listener,
    mSender = sender,
    digestName = digestName,
    hmacName = hmacName,
    clientNonce = Random.nextUuid().toString()
  )

  override fun onMessage(message: String) {
    lock.synchronize {
      if (mState != State.ENDED) {
        when (mState) {
          State.INITIAL -> {
            notifyFail()
            val msg = handleServerFirst(message)
            if (msg != null) {
              mState = State.CLIENT_FINAL_SENT
              mSender.sendMessage(msg)
            } else {
              mState = State.ENDED
              notifyFail()
            }
          }

          State.CLIENT_FIRST_SENT -> {
            val msg = handleServerFirst(message)
            if (msg != null) {
              mState = State.CLIENT_FINAL_SENT
              mSender.sendMessage(msg)
            } else {
              mState = State.ENDED
              notifyFail()
            }
          }

          State.CLIENT_FINAL_SENT -> {
            if (handleServerFinal(message)) {
              mIsSuccess = true
              notifySuccess()
            } else {
              notifyFail()
            }
            mState = State.ENDED
          }

          State.ENDED -> TODO("mState=$mState")
        }
      }
    }
  }

  override fun abort() {
    lock.synchronize {
      mAborted = true
      mState = State.ENDED
    }
  }

  override fun isEnded(): Boolean {
    lock.synchronize {
      return mState == State.ENDED
    }
  }

  override fun isSuccess(): Boolean {
    return mIsSuccess
  }

  @Throws(ScramException::class)
  override fun start(username: String, password: String?) {
    lock.synchronize {
      mPassword = password
      mState = State.CLIENT_FIRST_SENT
      mSender.sendMessage(mScramClientFunctionality.prepareFirstMessage(username))
    }
  }

  override fun isAborted(): Boolean {
    return mAborted
  }

  private fun handleServerFinal(message: String): Boolean {
    return mScramClientFunctionality.checkServerFinalMessage(message)
  }

  private fun handleServerFirst(message: String): String? {
    return mScramClientFunctionality.prepareFinalMessage(mPassword, message)
  }

  private fun notifySuccess() {
    mListener.onSuccess()
  }

  private fun notifyFail() {
    mListener.onFailure()
  }

  internal enum class State {
    INITIAL, CLIENT_FIRST_SENT, CLIENT_FINAL_SENT, ENDED
  }
}
