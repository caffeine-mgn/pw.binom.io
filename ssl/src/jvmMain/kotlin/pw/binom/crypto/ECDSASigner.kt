package pw.binom.crypto

import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner as BCECDSASigner

actual class ECDSASigner actual constructor(privateKey: ECPrivateKey) {
    val signer = BCECDSASigner()

    init {
        val params = ECDomainParameters(
            privateKey.native.parameters.curve,
            privateKey.native.parameters.g,
            privateKey.native.parameters.n,
            privateKey.native.parameters.h
        )
        signer.init(
            true,
            ECPrivateKeyParameters(privateKey.native.d, params),
        )
    }

    actual fun sign(data: ByteArray): ECDSASignature {
        val components = signer.generateSignature(data)
        return ECDSASignature(
            r = components[0].toBigInteger(),
            s = components[1].toBigInteger(),
        )
    }
}
