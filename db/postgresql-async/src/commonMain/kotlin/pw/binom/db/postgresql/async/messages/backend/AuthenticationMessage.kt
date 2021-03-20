package pw.binom.db.postgresql.async.messages.backend

import pw.binom.ByteBuffer
import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.*

const val AuthenticationOk = 0
const val AuthenticationKerberosV5 = 2
const val AuthenticationCleartextPassword = 3
const val AuthenticationMD5Password = 5
const val AuthenticationSCMCredential = 6
const val AuthenticationGSS = 7
const val AuthenticationGSSContinue = 8
const val AuthenticationSSPI = 9

sealed class AuthenticationMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Authentication

    object AuthenticationOkMessage : AuthenticationMessage() {
        override fun write(writer: PackageWriter) {
            TODO("Not yet implemented")
        }
    }

    object AuthenticationChallengeCleartextMessage : AuthenticationMessage() {
        override fun write(writer: PackageWriter) {
            TODO("Not yet implemented")
        }
    }

    class AuthenticationChallengeMessage : AuthenticationMessage() {

        enum class AuthenticationResponseType {
            MD5,
            Cleartext,
            Ok
        }


        var challengeType: AuthenticationResponseType = AuthenticationResponseType.Ok
        var salt: ByteArray? = null

        override fun write(writer: PackageWriter) {
            TODO("Not yet implemented")
        }
    }

    companion object {
        suspend fun read(ctx: PackageReader): AuthenticationMessage {
            val buf = ctx.buf16
            val authenticationType = ctx.input.readInt(buf)
            val result = when (authenticationType) {
                AuthenticationOk -> AuthenticationOkMessage
                AuthenticationCleartextPassword -> AuthenticationChallengeCleartextMessage
                AuthenticationMD5Password -> {
                    val buf2 = ByteArray(ctx.length - Int.SIZE_BYTES)
                    ByteBuffer.alloc(buf2.size) { tmp2 ->
                        while (true) {
                            ctx.input.read(tmp2)
                            if (tmp2.remaining == 0) {
                                break
                            }
                        }
                        tmp2.flip()
                        tmp2.get(buf2)
                    }
//                    var l = buf2.size
//                    while (l > 0) {
//                        buf.position = 0
//                        buf.limit = minOf(l, buf.capacity)
//                        val read = ctx.input.read(buf)
//                        buf.get(buf2, buf2.size - l)
//                        l -= read
//                    }
                    val msg = ctx.authenticationChallengeMessage
                    msg.challengeType = AuthenticationChallengeMessage.AuthenticationResponseType.MD5
                    msg.salt = buf2
                    msg
                }
                else -> TODO()
            }
            ctx.end()
            return result
        }
    }
}