package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class SASLResponse(var slasData: ByteArray) : KindedMessage {
    constructor() : this(ByteArray(0))

    override val kind: Byte
        get() = MessageKinds.PasswordMessage

    override fun write(writer: PackageWriter) {
        writer.writeCmd(kind)
        writer.startBody()
        writer.write(slasData)
        writer.endBody()
    }
}
