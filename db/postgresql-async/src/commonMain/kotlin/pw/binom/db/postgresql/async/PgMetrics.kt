package pw.binom.db.postgresql.async

import pw.binom.BinomMetrics
import pw.binom.metric.MutableGauge

internal object PgMetrics {
    val pgConnections = MutableGauge("binom_pg_connection", description = "PostgreSQL Connection count")

    init {
        BinomMetrics.reg(pgConnections)
    }
}
