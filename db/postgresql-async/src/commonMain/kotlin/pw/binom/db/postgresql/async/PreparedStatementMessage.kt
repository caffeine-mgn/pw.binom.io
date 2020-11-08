package pw.binom.db.postgresql.async

import pw.binom.UUID

abstract class PreparedStatementMessage(
    val statementId: String,
    val query: String,
    val values: List<Any?>,
):KindedMessage