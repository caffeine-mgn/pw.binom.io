package pw.binom.cleaner

actual class Cleaner {
    actual companion object {
        private class CleanRecord<T : Any>(val value: T, val func: (T) -> Unit)

        private val list = ArrayList<Pair<JSWeakRef, CleanRecord<Any>>>()
        private fun check() {
            val it = list.iterator()
            while (it.hasNext()) {
                val e = it.next()
                if (e.first.deref() == null) {
                    it.remove()
                    e.second.func(e.second.value)
                }
            }
        }

        actual fun <T> create(value: T, func: (T) -> Unit): Cleaner {
            check()
            val c = Cleaner()
            value ?: return c
            val item = CleanRecord(value = value, func = func) as CleanRecord<Any>
            list.add(JSWeakRef(c) to item)
            return c
        }
    }
}
