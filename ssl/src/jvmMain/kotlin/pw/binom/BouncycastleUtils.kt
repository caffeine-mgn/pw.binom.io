package pw.binom

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.concurrent.atomic.AtomicBoolean

internal object BouncycastleUtils {
    private val checked = AtomicBoolean()
    fun check() {
        if (!checked.compareAndSet(false, true)) {
            return
        }
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }
}
