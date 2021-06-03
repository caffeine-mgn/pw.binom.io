package pw.binom.strong

interface BeanProvider<T : Any> {
    fun provide(strong: Strong): T
}