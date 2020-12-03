package pw.binom.ssl

import java.util.*
import kotlin.collections.ArrayList

internal fun filterArray(items: Array<String>?, includedItems: List<String>?, excludedItems: List<String>?): Array<String> {
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