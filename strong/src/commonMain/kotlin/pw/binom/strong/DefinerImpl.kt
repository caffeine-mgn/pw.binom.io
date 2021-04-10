package pw.binom.strong

import pw.binom.strong.exceptions.BeanAlreadyDefinedException
import pw.binom.strong.exceptions.NoSuchBeanException
import kotlin.reflect.KClass

internal class DefinerImpl : Definer {

    private var internalDefinitions = HashMap<String, Any>()
    private val internalBeans = HashMap<KClass<Any>, HashMap<String, (Strong) -> Any>>()

    val beans: Map<KClass<Any>, Map<String, (Strong) -> Any>>
        get() = internalBeans

    override suspend fun define(bean: Any, name: String?, ifNotExist: Boolean) {
        val name = name ?: bean::class.genDefaultName()
        if (internalDefinitions.containsKey(name)) {
            throw BeanAlreadyDefinedException(name)
        }
        internalDefinitions[name] = bean
    }

    override fun <T : Any> findDefine(clazz: KClass<T>, name: String?): T {
        val bean = internalDefinitions.entries.find { clazz.isInstance(it.value) && (name == null || name == it.key) }
            ?: throw NoSuchBeanException(clazz, name)
        return bean.value as T
    }

    override fun <T : Any> bean(clazz: KClass<T>, name: String?, ifNotExist: Boolean, bean: (Strong) -> T) {
        val map = internalBeans.getOrPut(bean::class as KClass<Any>) { HashMap() }
        val name = name ?: bean::class.genDefaultName()
        if (map.containsKey(name)) {
            throw BeanAlreadyDefinedException(name)
        }
        map[name] = bean
    }

    suspend fun createBeans(): List<Pair<String, Any>> {
        val needInit = HashMap<String, BeanEntity>()
        val strong = StrongImpl()

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

        while (beans.isNotEmpty() || internalDefinitions.isNotEmpty()) {
            internalDefinitions.forEach { e ->
                foundDefinitions[e.key] = e.value
            }
            internalBeans.forEach {
                it.value.forEach {
                    initBean(it)
                }
            }
            internalDefinitions = HashMap()
            internalBeans.clear()
            internalDefinitions = foundDefinitions
            needInit.values.forEach {
                if (it.obj is Strong.ServiceProvider) {
                    it.obj.provide(this)
                }
            }
        }

        needInit.forEach { entity ->
            entity.value.deps.forEach { dep ->
                when (dep) {
                    is StrongImpl.Dependency.ClassDependency -> {
                        if (dep.name != null) {
                            if (internalDefinitions[dep.name]?.let { dep.clazz.isInstance(it) } == true) {
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
                            if (internalDefinitions.values.any { dep.clazz.isInstance(it) }) {
                                return@forEach
                            }
                            val depBean = needInit.values.find { dep.clazz.isInstance(it.obj) }
                            if (depBean == null && dep.require) {
                                throw NoSuchBeanException(dep.clazz, null)
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