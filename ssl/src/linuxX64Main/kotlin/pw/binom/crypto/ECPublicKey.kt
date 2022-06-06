package pw.binom.crypto

import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.base64.Base64
import pw.binom.checkTrue
import pw.binom.getSslError
import pw.binom.io.IOException
import pw.binom.io.use
import pw.binom.ssl.Bio
import pw.binom.ssl.ECKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import pw.binom.throwError
import kotlin.native.internal.createCleaner

actual class ECPublicKey(val native: CPointer<EC_KEY>/*private val curve: ECCurve, actual val q: EcPoint*/) :
    Key.Public, ECKey {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.ECDSA
    override val data: ByteArray
        get() = Bio.mem().use { bio ->
            PEM_write_bio_EC_PUBKEY(bio.self, native)
                .checkTrue("PEM_write_bio_EC_PUBKEY fails")
            val fullData = bio.toByteArray()
            var str = fullData.decodeToString()
            str = str.replace("\n", "")
            if (!str.startsWith("-----BEGIN PUBLIC KEY-----")) {
                TODO()
            }
            if (!str.endsWith("-----END PUBLIC KEY-----")) {
                TODO()
            }
            str = str.substring(26, str.length - 24)
            Base64.decode(str)
        }
    override val format: String
        get() = "X.509"
    actual val q: EcPoint by lazy {
        val curve = EC_GROUP_dup(EC_KEY_get0_group(native) ?: throwError("Can't get Curve"))
            ?: throwError("Can't duplicate Curve")
        EcPoint(
            curve = ECCurve(curve),
            ptr = EC_POINT_dup(EC_KEY_get0_public_key(native) ?: throwError("Can't get public key"), curve)
                ?: throwError("Can't duplicate public key")
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(native) { native ->
        EC_KEY_free(native)
    }

    actual companion object {
        actual fun load(data: ByteArray): ECPublicKey {
            val pem = "-----BEGIN PUBLIC KEY-----\n${Base64.encode(data)}\n-----END PUBLIC KEY-----\n"
            val ecKey = Bio.mem(pem.encodeToByteArray()).use { priv ->
                PEM_read_bio_EC_PUBKEY(priv.self, null, null, null)
                    ?: throw IOException("Can't load public key: ${getSslError()}")
            }
            EC_KEY_check_key(ecKey).checkTrue("EC_KEY_check_key") {
                EC_KEY_free(ecKey)
            }
            return ECPublicKey(ecKey)
        }
    }
}
