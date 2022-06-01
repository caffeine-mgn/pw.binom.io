package pw.binom.crypto

import org.bouncycastle.crypto.ec.CustomNamedCurves

actual object NamedCurves {
    actual fun getByName(name: String): X9ECParameters =
        X9ECParameters(CustomNamedCurves.getByName(name))
}
