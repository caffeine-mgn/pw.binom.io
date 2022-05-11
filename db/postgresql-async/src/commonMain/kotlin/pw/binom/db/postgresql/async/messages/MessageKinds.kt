package pw.binom.db.postgresql.async.messages

/**
 * [PostgreSQL Message Formats](https://www.postgresql.org/docs/10/protocol-message-formats.html)
 */
object MessageKinds {
    const val Authentication = 'R'.code.toByte()
    const val BackendKeyData = 'K'.code.toByte()
    const val Bind = 'B'.code.toByte()
    const val BindComplete = '2'.code.toByte()
    const val CommandComplete = 'C'.code.toByte()
    const val CloseStatementOrPortal = 'C'.code.toByte()
    const val CloseComplete = '3'.code.toByte()
    const val DataRow = 'D'.code.toByte()
    const val Describe = 'D'.code.toByte()
    const val Error = 'E'.code.toByte()
    const val Execute = 'E'.code.toByte()
    const val EmptyQueryString = 'I'.code.toByte()
    const val NoData = 'n'.code.toByte()
    const val Notice = 'N'.code.toByte()
    const val NotificationResponse = 'A'.code.toByte()
    const val ParameterStatus = 'S'.code.toByte()
    const val Parse = 'P'.code.toByte()
    const val ParseComplete = '1'.code.toByte()
    const val PasswordMessage = 'p'.code.toByte()
    const val PortalSuspended = 's'.code.toByte()
    const val Query = 'Q'.code.toByte()
    const val RowDescription = 'T'.code.toByte()
    const val ReadyForQuery = 'Z'.code.toByte()
    const val Sync = 'S'.code.toByte()
    const val Terminate = 'X'.code.toByte()
}
