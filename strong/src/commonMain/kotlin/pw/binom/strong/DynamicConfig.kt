package pw.binom.strong

import pw.binom.collections.defaultMutableList
import kotlin.reflect.KClass

fun interface BeanDefinition {
    fun define(strong: Strong): Any
}

open class DynamicConfig : Strong.Config {
    private val items = defaultMutableList<Item<out Any>>()
    private val includes = defaultMutableList<Strong.Config>()
    private var applied = false

    private class Item<T : Any>(
        val clazz: KClass<T>,
        val primary: Boolean = false,
        val name: String? = null,
        val ifNotExist: Boolean = false,
        val bean: (Strong) -> T
    )

    fun include(config: Strong.Config) {
        includes += config
    }

    inline fun <reified T : Any> bean(
        name: String? = null,
        primary: Boolean = false,
        ifNotExist: Boolean = false,
        noinline bean: (Strong) -> T
    ) {
        println("call bean inline!!! ${T::class}")
        beanSpecial(
            clazz = T::class,
            name = name,
            ifNotExist = ifNotExist,
            bean = bean,
            primary = primary,
        )
    }

    protected operator fun <T : Any> KClass<T>.unaryPlus() {
        TODO("Not yet implemented")
    }

    open fun define(
        clazz: KClass<out Any>,
        primary: Boolean,
        name: String?,
        ifNotExist: Boolean,
        bean: BeanDefinition
    ) {
        check(!applied) { "Configuration already applied" }
        items += Item(
            clazz = clazz as KClass<Any>,
            primary = primary,
            name = name,
            ifNotExist = ifNotExist,
            bean = { bean.define(it) },
        )
    }

    @PublishedApi
    internal fun <T : Any> beanSpecial(
        clazz: KClass<T>,
        primary: Boolean = false,
        name: String? = null,
        ifNotExist: Boolean = false,
        bean: (Strong) -> T
    ) {
        check(!applied) { "Configuration already applied" }
        items += Item(
            clazz = clazz,
            primary = primary,
            name = name,
            ifNotExist = ifNotExist,
            bean = bean,
        )
    }

    override suspend fun apply(strong: Definer) {
        applied = true
        items.forEach {
            strong.bean(
                name = it.name,
                primary = it.primary,
                clazz = it.clazz as KClass<Any>,
                ifNotExist = it.ifNotExist,
                bean = it.bean,
            )
        }

        includes.forEach {
            it.apply(strong)
        }
    }
}