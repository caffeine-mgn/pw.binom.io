package pw.binom.strong

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ServiceProvider<T> : ReadOnlyProperty<Any?, T> {
    val service: T

    companion object {
        fun <T> provide(value: T) = object : ServiceProvider<T> {
            override val service: T
                get() = value
        }

        fun <T> provide(value: () -> T) = object : ServiceProvider<T> {
            private val data by lazy(value)
            override val service: T
                get() = data
        }
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        service
}
