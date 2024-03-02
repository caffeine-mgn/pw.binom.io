package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.io.ByteBuffer
import pw.binom.io.use

const val AuthenticationOk = 0
const val AuthenticationKerberosV5 = 2
const val AuthenticationCleartextPassword = 3
const val AuthenticationMD5Password = 5
const val AuthenticationSCMCredential = 6
const val AuthenticationGSS = 7
const val AuthenticationGSSContinue = 8
const val AuthenticationSSPI = 9
const val AuthenticationSASL = 10
const val AuthenticationSASLContinue = 11
const val AuthenticationSASLFinal = 12

/**
 * [Specification](https://www.postgresql.org/docs/current/protocol-message-formats.html)
 */
sealed class AuthenticationMessage : KindedMessage {
  override val kind: Byte
    get() = MessageKinds.Authentication

  object AuthenticationOkMessage : AuthenticationMessage() {
    override fun write(writer: PackageWriter) {
      TODO("Not yet implemented")
    }

    override fun toString(): String = "AuthenticationOkMessage()"
  }

  object AuthenticationChallengeCleartextMessage : AuthenticationMessage() {
    override fun write(writer: PackageWriter) {
      TODO("Not yet implemented")
    }

    override fun toString(): String = "AuthenticationChallengeCleartextMessage()"
  }

  class AuthenticationChallengeMessage : AuthenticationMessage() {
    enum class AuthenticationResponseType {
      MD5,
      Cleartext,
      Ok,
    }

    var challengeType: AuthenticationResponseType = AuthenticationResponseType.Ok
    var salt: ByteArray? = null

    override fun write(writer: PackageWriter) {
      TODO("Not yet implemented")
    }

    override fun toString(): String {
      return "AuthenticationChallengeMessage(challengeType=$challengeType, salt=${salt?.contentToString()})"
    }
  }

  class SaslAuth(val algorithms: List<String>) : AuthenticationMessage() {
    override fun write(writer: PackageWriter) {
      TODO("Not yet implemented")
    }
  }

  class SaslContinue(val data: ByteArray) : AuthenticationMessage() {
    override fun write(writer: PackageWriter) {
      TODO("Not yet implemented")
    }
  }

  class SaslFinal(val data: ByteArray) : AuthenticationMessage() {
    override fun write(writer: PackageWriter) {
      TODO("Not yet implemented")
    }
  }

//    class SASLInitialResponseMessage : AuthenticationMessage() {
//        override val kind: Byte
//            get() = MessageKinds.PasswordMessage
//        var saslAuthMechanism = ""
//        var data = ByteArray(0)
//
//        override fun write(writer: PackageWriter) {
//            writer.writeCmd(MessageKinds.PasswordMessage)
//            writer.startBody()
//            writer.writeCString(saslAuthMechanism)
//            writer.writeInt(data.size)
//            writer.write(data)
//            writer.endBody()
//        }
//    }

  companion object {
    suspend fun read(ctx: PackageReader): AuthenticationMessage {
      val authenticationType = ctx.readInt()
      val result =
        when (authenticationType) {
          AuthenticationOk -> AuthenticationOkMessage
          AuthenticationCleartextPassword -> AuthenticationChallengeCleartextMessage
          AuthenticationMD5Password -> {
            val buf2 = ByteArray(ctx.length - Int.SIZE_BYTES)
            ByteBuffer(buf2.size).use { tmp2 ->
              while (true) {
                ctx.input.read(tmp2)
                if (tmp2.remaining == 0) {
                  break
                }
              }
              tmp2.flip()
              tmp2.readInto(buf2)
            }
            val msg = ctx.authenticationChallengeMessage
            msg.challengeType = AuthenticationChallengeMessage.AuthenticationResponseType.MD5
            msg.salt = buf2
            msg
          }

          AuthenticationSASL -> {
            val algoritmes = ArrayList<String>()
            while (true) {
              val str = ctx.readCString()
              if (str.isEmpty()) {
                break
              }
              algoritmes += str
            }
            SaslAuth(algoritmes)
          }

          AuthenticationSASLContinue -> {
            val salsData = ctx.readByteArray(ctx.length - Int.SIZE_BYTES)
            SaslContinue(salsData)
          }

          AuthenticationSASLFinal -> {
            val salsData = ctx.readByteArray(ctx.length - Int.SIZE_BYTES)
            SaslFinal(salsData)
          }

          else -> TODO("Unknown authenticationType $authenticationType")
        }
      ctx.end()
      return result
    }
  }
}
