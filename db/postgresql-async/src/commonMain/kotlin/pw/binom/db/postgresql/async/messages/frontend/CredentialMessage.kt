package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.crypto.MD5MessageDigest
import pw.binom.db.postgresql.async.messages.backend.AuthenticationMessage
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.writeByte

private val lookup = byteArrayOf(
    '0'.toByte(),
    '1'.toByte(),
    '2'.toByte(),
    '3'.toByte(),
    '4'.toByte(),
    '5'.toByte(),
    '6'.toByte(),
    '7'.toByte(),
    '8'.toByte(),
    '9'.toByte(),
    'a'.toByte(),
    'b'.toByte(),
    'c'.toByte(),
    'd'.toByte(),
    'e'.toByte(),
    'f'.toByte()
)

private fun bytesToHex(bytes: ByteArray, hex: ByteArray, offset: Int) {
    var pos = offset
    var i = 0
    while (i < 16) {
        val c = bytes[i].toInt() and 0xff
        var j = c shr 4
        hex[pos++] = lookup[j]
        j = c and 0xf
        hex[pos++] = lookup[j]
        i += 1
    }
}

class CredentialMessage : KindedMessage {

    var username: String = ""
    var password: String? = null
    var authenticationType = AuthenticationMessage.AuthenticationChallengeMessage.AuthenticationResponseType.Ok
    var salt: ByteArray? = null

    override val kind: Byte
        get() = MessageKinds.PasswordMessage

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.PasswordMessage)
        writer.startBody()

        when (authenticationType) {
            AuthenticationMessage.AuthenticationChallengeMessage.AuthenticationResponseType.Cleartext -> {
                writer.writeCString(password!!)
            }
            AuthenticationMessage.AuthenticationChallengeMessage.AuthenticationResponseType.MD5 -> {
                val md = MD5MessageDigest()

                md.update(writer.connection.charsetUtils.encode(password!!){it.toByteArray()})
                md.update(writer.connection.charsetUtils.encode(username){it.toByteArray()})
                val tempDigest = md.finish()
                val hexDigest = ByteArray(35)
                bytesToHex(tempDigest, hexDigest, 0)
                md.update(hexDigest, 0, 32);
                md.update(salt!!)
                val passDigest = md.finish()

                bytesToHex(passDigest, hexDigest, 3)

                hexDigest[0] = 'm'.toByte()
                hexDigest[1] = 'd'.toByte()
                hexDigest[2] = '5'.toByte()
                writer.write(hexDigest)
                writer.output.writeByte(writer.buf16, 0)
            }
        }
        writer.endBody()
    }
}