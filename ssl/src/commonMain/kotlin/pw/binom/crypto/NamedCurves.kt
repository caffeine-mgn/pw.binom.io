package pw.binom.crypto

import pw.binom.ssl.Nid

expect object NamedCurves {
    fun getByName(nid: Nid): X9ECParameters
}
