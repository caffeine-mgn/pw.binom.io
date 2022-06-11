package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.base63.toJavaBigInteger
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner as BCECDSASigner

actual class ECDSASigner {
    val signer = BCECDSASigner()

    actual constructor(privateKey: ECPrivateKey) {
        val params = ECDomainParameters(
            privateKey.native.parameters.curve,
            privateKey.native.parameters.g,
            privateKey.native.parameters.n,
            privateKey.native.parameters.h,
            privateKey.native.parameters.seed,
        )
        signer.init(
            true,
            ECPrivateKeyParameters(privateKey.native.d, params),
        )
    }

    actual constructor(publicKey: ECPublicKey) {
        val params = ECDomainParameters(
            publicKey.native.parameters.curve,
            publicKey.native.parameters.g,
            publicKey.native.parameters.n,
            publicKey.native.parameters.h,
            publicKey.native.parameters.seed,
        )
        signer.init(
            false,
            ECPublicKeyParameters(publicKey.native.q, params),
        )
    }

    actual fun sign(data: ByteArray): ECDSASignature {
        val components = signer.generateSignature(data)
        return ECDSASignature(
            r = components[0].toBigInteger(),
            s = components[1].toBigInteger(),
        )
    }

    actual fun verify(data: ByteArray, r: BigInteger, s: BigInteger): Boolean =
        signer.verifySignature(data, r.toJavaBigInteger(), s.toJavaBigInteger())

    actual fun verify(data: ByteArray, signature: ECDSASignature) = verify(data, signature.r, signature.s)
}
