package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class SASLInitialResponse : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.PasswordMessage
    var mechanism = ""
    var slasData = ByteArray(0)

    override fun write(writer: PackageWriter) {
        writer.writeCmd(kind)
        writer.startBody()
        writer.writeCString(mechanism)
        writer.writeInt(slasData.size)
        writer.write(slasData)
        writer.endBody()
    }
}
