package pw.binom.strong

import pw.binom.strong.exceptions.BeanCreateException
import pw.binom.strong.exceptions.NoSuchBeanException
import pw.binom.strong.exceptions.StrongException
import kotlin.reflect.KClass


class ClassDependency(val clazz: KClass<out Any>, val name: String?, val require: Boolean)

internal class StrongWithDependenciesSpy(val strong: Strong) : Strong by strong {
    private val dependencies = ArrayList<ClassDependency>()
    private var inited = false
    private fun checkStatus() {
        if (inited) {
            throw IllegalStateException("Strong already inited")
        }
    }

    fun initFinish() {
        checkStatus()
        dependencies.clear()
    }

    fun getLastDependencies(): List<ClassDependency> {
        checkStatus()
        val r = ArrayList(dependencies)
        dependencies.clear()
        return r
    }

    override fun <T : Any> serviceOrNull(beanClass: KClass<T>, name: String?): ServiceProvider<T?> {
        if (!inited) {
            dependencies += ClassDependency(clazz = beanClass, name = name, require = false)
        }
        return strong.serviceOrNull(beanClass, name)
    }

    override fun <T : Any> service(beanClass: KClass<T>, name: String?): ServiceProvider<T> {
        if (!inited) {
            dependencies += ClassDependency(clazz = beanClass, name = name, require = true)
        }
        return strong.service(beanClass, name)
    }
}

internal class Starter(
    val strongImpl: StrongImpl,
    startDefiner: InternalDefiner,
) {

    private val dd = startDefiner
    private val strongWithDeps = StrongWithDependenciesSpy(strongImpl)

    private class BeanConfig(val obj: Any, val name: String, val primary: Boolean, val deps: List<ClassDependency>) {
        var inited = false
        var linked = false
        var initing = false
        var linking = false

        fun isReadyForInit() = !nodes.any { !it.inited }
        fun isReadyForLink() = !nodes.any { !it.linked }

        val nodes = HashSet<BeanConfig>()
        val resultBeanClass: KClass<out Any>
            get() {
                if (obj is Strong.BeanFactory<out Any>) {
                    return obj.type
                }
                return obj::class
            }

        fun isMatch(clazz: KClass<out Any>): Boolean {
            if (clazz.isInstance(obj)) {
                return true
            }
            if (obj is Strong.BeanFactory<out Any>) {
                if (obj.type === clazz) {
                    return true
                }
            }
            return false
        }
    }

    private val createdBeans = ArrayList<BeanConfig>()

    private fun init() {
        val beanFromConfig = dd.getLastDefinitions().map {
            val bean = it.init(strongWithDeps)
            val deps = strongWithDeps.getLastDependencies()
            BeanConfig(
                obj = bean,
                name = it.name,
                deps = deps,
                primary = it.primary,
            )
        }
        createdBeans.addAll(beanFromConfig)
    }

    private fun makeDestroysTree(): List<BeanConfig> {
        val dd = LinkedHashSet<BeanConfig>()
        fun ff(b: BeanConfig) {
            if (b in dd) {
                return
            }
            if (b.obj !is Strong.DestroyableBean) {
                return
            }
            b.nodes.forEach {
                ff(it)
            }
            dd += b
        }

        createdBeans.forEach {
            ff(it)
        }
        return dd.toList()
    }

    private fun makeTree() {
        createdBeans.forEach { node ->
            node.deps.forEach { dep ->
                val foundBean = if (dep.name != null) {
                    createdBeans.find { it.name == dep.name }
                } else {
                    createdBeans.find { it.isMatch(dep.clazz) }
                }
                if (foundBean == null) {
                    if (dep.require) {
                        throw BeanCreateException(
                            clazz = node.obj::class,
                            name = node.name,
                            cause = NoSuchBeanException(klazz = dep.clazz, name = dep.name),
                        )

                    } else {
                        return@forEach
                    }
                }

                if (!foundBean.isMatch(dep.clazz)) {
                    throw BeanCreateException(
                        clazz = node.obj::class,
                        name = node.name,
                        cause = StrongException("Found invalid bean type. Except ${dep.clazz}, actual ${foundBean.resultBeanClass}"),//TODO сделать нормальное описание ошибки
                    )

                }
                if (node !== foundBean) {
                    node.nodes += foundBean
                }
            }
        }
    }

    private suspend fun callInit(o: BeanConfig, from: BeanConfig?) {
        if (o.inited) {
            return
        }
        if (o.initing) {
            throw StrongException("Bean ${o.obj::class} in initialization. Requested by ${from?.let { it.obj::class }}")
        }
        o.initing = true
        o.nodes.forEach {
            callInit(it, o)
        }
        if (o.obj is Strong.InitializingBean) {
            o.obj.init(strongImpl)
        }
        o.initing = false
        o.inited = true
    }

    private suspend fun callLink(o: BeanConfig, from: BeanConfig?) {
        if (o.linked) {
            return
        }
        if (o.linking) {
            throw StrongException("Bean ${o::class} in linking. Requested by ${from?.let { it.obj::class }}")
        }
        o.linking = true
        o.nodes.forEach {
            callLink(it, o)
        }
        if (o.obj is Strong.LinkingBean) {
            o.obj.link(strongImpl)
        }
        if (o.obj is Strong.BeanFactory<out Any>) {
            val newBean = o.obj.provide(strongWithDeps)
            if (newBean != null) {
                createdBeans += BeanConfig(
                    obj = newBean,
                    name = o.obj.name,
                    deps = strongWithDeps.getLastDependencies(),
                    primary = o.primary,
                )
            }
        }
        o.linking = false
        o.linked = true
        strongImpl.beans[o.name] = BeanEntity(o.obj, o.primary)
    }

    fun checkCycle() {
        if (createdBeans.isEmpty()) {
            return
        }
        val objs = LinkedHashSet<BeanConfig>()

        fun ff(beanConfig: BeanConfig) {
            if (beanConfig in objs) {
                return
            }
            beanConfig.nodes.forEach {
                objs += it
                ff(it)
            }
        }

        createdBeans.forEach {
            objs.clear()
            ff(it)
            if (it in objs) {
                TODO("Циклическая завиисмость")
            }
        }
    }

    internal suspend fun start() {
        STRONG_LOCAL = strongWithDeps
        try {
            init()
            makeTree()
            checkCycle()
            strongImpl.beanOrder = makeDestroysTree().map { it.name to it.obj }

            strongWithDeps.initFinish()
            var listForStart = ArrayList(createdBeans)
            LOOP@ do {
                for (it in listForStart) {
                    if (!it.inited) {
                        if (it.obj is Strong.InitializingBean) {
                            val notInitedBeans = it.nodes.filter { !it.inited }
                            if (notInitedBeans.isNotEmpty()) {
                                listForStart.removeAll(notInitedBeans)
                                listForStart.addAll(0, notInitedBeans)
                                continue@LOOP
                            }
                            it.obj.init(strongImpl)
                            it.inited = true
                        } else {
                            it.inited = true
                        }
                        it.obj
                        strongImpl.beans[it.name] = BeanEntity(bean = it.obj, primary = it.primary)
                        continue@LOOP
                    }
                    if (!it.linked) {
                        if (it.obj is Strong.LinkingBean) {
                            val notInitedBeans = it.nodes.filter { !it.linked || !it.inited }
                            if (notInitedBeans.isNotEmpty()) {
                                listForStart.removeAll(notInitedBeans)
                                listForStart.addAll(0, notInitedBeans)
                                continue@LOOP
                            }
                            it.obj.link(strongImpl)
                            it.linked = true
                        } else {
                            it.linked = true
                        }
                        listForStart.remove(it)
                        continue@LOOP
                    }
                }
                if (listForStart.isEmpty()) {
                    break
                }
            } while (true)

        } finally {
            strongWithDeps.initFinish()
            STRONG_LOCAL = null
        }
    }

//    internal suspend fun start() {
//        STRONG_LOCAL = strongWithDeps
//        try {
//            init()
//            makeTree()
//            createdBeans.forEach {
//                if (it.obj is Strong.BeanFactory<out Any>) {
//                    callInit(it, null)
//                }
//            }
//            do {
//                val initSize = createdBeans.size
//                ArrayList(createdBeans).forEach {
//                    if (it.obj is Strong.BeanFactory<out Any>) {
//                        callLink(it, null)
//                    }
//                }
//                if (createdBeans.size != initSize) {
//                    continue
//                }
//            } while (false)
//            createdBeans.forEach {
//                callInit(it, null)
//            }
//            createdBeans.forEach {
//                callLink(it, null)
//            }
//        } finally {
//            strongWithDeps.initFinish()
//            STRONG_LOCAL = null
//        }
//    }
}