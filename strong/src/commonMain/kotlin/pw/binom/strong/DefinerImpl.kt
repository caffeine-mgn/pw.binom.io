package pw.binom.strong

import pw.binom.strong.exceptions.BeanAlreadyDefinedException
import pw.binom.strong.exceptions.BeanCreateException
import pw.binom.strong.exceptions.NoSuchBeanException
import kotlin.reflect.KClass
/*
internal class DefinerImpl : Definer {

    private val internalDefinitions = HashMap<String, Any>()
    private val internalBeans = HashMap<KClass<Any>, HashMap<String, (Strong) -> Any>>()
    private var currentDef = HashMap<String, Any>()

    val beans: Map<KClass<Any>, Map<String, (Strong) -> Any>>
        get() = internalBeans

    override suspend fun define(bean: Any, name: String?, ifNotExist: Boolean) {
        val name = name ?: bean::class.genDefaultName()
        if (currentDef.containsKey(name)) {
            throw BeanAlreadyDefinedException(name)
        }
        internalDefinitions[name] = bean
        currentDef[name] = bean
    }

    override fun <T : Any> findDefine(clazz: KClass<T>, name: String?): T {
        val bean = currentDef.entries.find { clazz.isInstance(it.value) && (name == null || name == it.key) }
            ?: throw NoSuchBeanException(clazz, name)
        return bean.value as T
    }

    override fun <T : Any> bean(clazz: KClass<T>, name: String?, ifNotExist: Boolean, bean: (Strong) -> T) {
        val map = internalBeans.getOrPut(bean::class as KClass<Any>) { HashMap() }
        val name = name ?: clazz.genDefaultName()
        if (map.containsKey(name)) {
            throw BeanAlreadyDefinedException(name)
        }
        map[name] = bean
    }

    suspend fun createBeans(strong: StrongImpl): List<Pair<String, Any>> {
        val needInit = HashMap<String, BeanEntity>()

        val foundDefinitions = HashMap<String, Any>()

        fun initBean(it: Map.Entry<String, (Strong) -> Any>) {
            strong.defining()
            val bean = it.value(strong)
            needInit[it.key] = BeanEntity(
                obj = bean,
                name = it.key,
                deps = strong.dependencies,
            )
        }
        val oo = HashSet<Any>()
        do {
            internalDefinitions.forEach { e ->
                foundDefinitions[e.key] = e.value
            }
            internalBeans.forEach {
                it.value.forEach {
                    initBean(it)
                }
            }
            internalBeans.clear()
            internalDefinitions.clear()
            currentDef = HashMap(foundDefinitions)
            needInit.values.forEach {
                if (it.obj in oo) {
                    return@forEach
                }
                if (it.obj is Strong.ServiceProvider) {
                    it.obj.provide(this)
                }
                oo += it.obj
            }
        } while (internalBeans.isNotEmpty() || internalDefinitions.isNotEmpty())

        needInit.forEach { entity ->
            entity.value.deps.forEach { dep ->
                when (dep) {
                    is StrongImpl.Dependency.ClassDependency -> {
                        if (dep.name != null) {
                            if (foundDefinitions[dep.name]?.let { dep.clazz.isInstance(it) } == true) {
                                return@forEach
                            }
                            val depBean = needInit[dep.name]
                            if (depBean == null && dep.require) {
                                throw NoSuchBeanException(dep.clazz, dep.name)
                            }
                            if (depBean != null) {
                                entity.value.depsEntity.add(depBean)
                            }
                        } else {
                            if (foundDefinitions.values.any { dep.clazz.isInstance(it) }) {
                                return@forEach
                            }
                            val depBean = needInit.values.find { dep.clazz.isInstance(it.obj) }
                            if (depBean == null && dep.require) {
                                throw BeanCreateException(
                                    entity.value.obj::class,
                                    entity.key,
                                    NoSuchBeanException(dep.clazz, null)
                                )
                            }
                        }
                    }
                    is StrongImpl.Dependency.ClassSetDependency -> {
                        entity.value.depsEntity.addAll(needInit.values.filter { dep.clazz.isInstance(it.obj) })
                    }
                }
            }
        }

        val initList = ArrayList<BeanEntity>()
        val inited = HashSet<BeanEntity>()
        fun init(bean: BeanEntity) {
            if (bean in inited) {
                return
            }
            //TODO add cycle check
            bean.depsEntity.forEach {
                init(it)
            }
            inited += bean
            initList += bean
        }
        needInit.values.forEach {
            init(it)
        }
        return foundDefinitions.map { it.key to it.value } + initList.map { it.name to it.obj }
    }
}

internal class BeanEntity(val obj: Any, val name: String, val deps: List<StrongImpl.Dependency>) {
    val depsEntity = ArrayList<BeanEntity>()
}
 */