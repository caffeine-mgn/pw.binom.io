package pw.binom

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.concurrent.atomic.AtomicBoolean

internal object BouncycastleUtils {
    private val checked = AtomicBoolean()
    private var _provider: BouncyCastleProvider? = null
    val provider: BouncyCastleProvider
        get() {
            if (_provider == null) {
                _provider = BouncyCastleProvider()
            }
            return _provider!!
        }

    fun check() {
        if (!checked.compareAndSet(false, true)) {
            return
        }
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(provider)
        }
    }
}
