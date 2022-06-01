package pw.binom.crypto

expect object NamedCurves {
    fun getByName(name: String): X9ECParameters
}
