package pw.binom.ssl

import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

internal fun filterArray(
    items: Array<String>?,
    includedItems: List<String>?,
    excludedItems: List<String>?
): Array<String> {
    val filteredItems = if (items == null) ArrayList() else Arrays.asList(*items)
    if (includedItems != null) {
        for (i in filteredItems.indices.reversed()) {
            if (!includedItems.contains(filteredItems[i])) {
                filteredItems.removeAt(i)
            }
        }

        for (includedProtocol in includedItems) {
            if (!filteredItems.contains(includedProtocol)) {
                filteredItems.add(includedProtocol)
            }
        }
    }

    if (excludedItems != null) {
        for (i in filteredItems.indices.reversed()) {
            if (excludedItems.contains(filteredItems[i])) {
                filteredItems.removeAt(i)
            }
        }
    }

    return filteredItems.toTypedArray()
}

fun loadPublicKey(algorithm: KeyAlgorithm, data: ByteArray) =
    when (algorithm) {
        KeyAlgorithm.RSA -> {
            val c = KeyFactory.getInstance("RSA")
            c.generatePublic(X509EncodedKeySpec(data))
        }
        KeyAlgorithm.ECDSA -> {
            val c = KeyFactory.getInstance("EC")
            c.generatePublic(X509EncodedKeySpec(data))
        }
    }

fun loadPrivateKey(algorithm: KeyAlgorithm, data: ByteArray) =
    when (algorithm) {
        KeyAlgorithm.RSA -> {
            val c = KeyFactory.getInstance("RSA")
            c.generatePrivate(PKCS8EncodedKeySpec(data))
        }
        KeyAlgorithm.ECDSA -> {
            val c = KeyFactory.getInstance("EC")
            c.generatePrivate(PKCS8EncodedKeySpec(data))
        }
    }
