package pw.binom.crypto

import pw.binom.ssl.Nid

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object NamedCurves {
    fun getByName(nid: Nid): X9ECParameters
}
