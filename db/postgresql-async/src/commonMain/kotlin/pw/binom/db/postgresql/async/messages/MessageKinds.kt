package pw.binom.db.postgresql.async.messages

/**
 * [PostgreSQL Message Formats](https://www.postgresql.org/docs/10/protocol-message-formats.html)
 */
object MessageKinds{
    const val Authentication = 'R'.toByte()
    const val BackendKeyData = 'K'.toByte()
    const val Bind = 'B'.toByte()
    const val BindComplete = '2'.toByte()
    const val CommandComplete = 'C'.toByte()
    const val Close = 'X'.toByte()
    const val CloseStatementOrPortal = 'C'.toByte()
    const val CloseComplete = '3'.toByte()
    const val DataRow = 'D'.toByte()
    const val Describe = 'D'.toByte()
    const val Error = 'E'.toByte()
    const val Execute = 'E'.toByte()
    const val EmptyQueryString = 'I'.toByte()
    const val NoData = 'n'.toByte()
    const val Notice = 'N'.toByte()
    const val NotificationResponse = 'A'.toByte()
    const val ParameterStatus = 'S'.toByte()
    const val Parse = 'P'.toByte()
    const val ParseComplete = '1'.toByte()
    const val PasswordMessage = 'p'.toByte()
    const val PortalSuspended = 's'.toByte()
    const val Query = 'Q'.toByte()
    const val RowDescription = 'T'.toByte()
    const val ReadyForQuery = 'Z'.toByte()
    const val Sync = 'S'.toByte()
}