package pw.binom.crypto

import pw.binom.ssl.Key

expect class ECPublicKey : Key.Public {
    val q: EcPoint
}
