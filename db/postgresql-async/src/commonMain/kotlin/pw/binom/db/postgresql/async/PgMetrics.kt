package pw.binom.db.postgresql.async

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge

internal object PgMetrics {
    val pgConnections = MutableLongGauge("binom_pg_connection", description = "PostgreSQL Connection count")

    init {
        BinomMetrics.reg(pgConnections)
    }
}
