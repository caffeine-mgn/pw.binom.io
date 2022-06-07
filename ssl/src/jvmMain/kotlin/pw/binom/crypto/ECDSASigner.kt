package pw.binom.crypto

import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner as BCECDSASigner

actual object ECDSASigner {
    actual fun sign(data: ByteArray, privateKey: ECPrivateKey): ECDSASignature {
        val signer = BCECDSASigner(/*HMacDSAKCalculator(SHA256Digest())*/)
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
        val components = signer.generateSignature(data)
        return ECDSASignature(
            r = components[0].toBigInteger(),
            s = components[1].toBigInteger(),
        )
    }
}
