package pw.binom

import pw.binom.uuid.UUID
import java.util.UUID as JUUID

val UUID.java: JUUID
    get() = JUUID.fromString(toString())

val JUUID.binom: UUID
    get() = UUID.fromString(toString())
