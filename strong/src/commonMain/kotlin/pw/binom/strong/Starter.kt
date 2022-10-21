package pw.binom.strong

import pw.binom.collections.defaultArrayList
import pw.binom.logger.Logger
import pw.binom.logger.debug
import pw.binom.strong.exceptions.*
import kotlin.reflect.KClass

class ClassDependency(val clazz: KClass<out Any>, val name: String?, val require: Boolean)

internal class StrongWithDependenciesSpy(val strong: Strong) : Strong by strong {
    private val dependencies = defaultArrayList<ClassDependency>()
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

    override suspend fun destroy() {
        strong.destroy()
    }

    fun getLastDependencies(): List<ClassDependency> {
        checkStatus()
        val r = defaultArrayList(dependencies)
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

    internal class BeanConfig(
        override val bean: Any,
        override val name: String,
        val primary: Boolean,
        val deps: List<ClassDependency>
    ) : BeanDescription {

        var inited = false
        var linked = false
        var initing = false
        var linking = false

        fun isReadyForInit() = !nodes.any { !it.inited }
        fun isReadyForLink() = !nodes.any { !it.linked }

        val nodes = HashSet<BeanConfig>()
        override val beanClass: KClass<out Any>
            get() {
                if (bean is Strong.BeanFactory<out Any>) {
                    return bean.type
                }
                return bean::class
            }

        fun isMatch(clazz: KClass<out Any>): Boolean {
            if (clazz.isInstance(bean)) {
                return true
            }
            if (bean is Strong.BeanFactory<out Any>) {
                if (bean.type == clazz) {
                    return true
                }
            }
            return false
        }

        override fun toString() = "BeanConfig(name=\"$name\", beanClass=$beanClass)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as BeanConfig

            if (name != other.name) return false
            if (beanClass != other.beanClass) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }

    private val createdBeans = defaultArrayList<BeanConfig>()

    private fun init() {
        val beanFromConfig = dd.getLastDefinitions().map {
            val bean = it.init(strongWithDeps)
            val deps = strongWithDeps.getLastDependencies()
            BeanConfig(
                bean = bean,
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
//            if (b.obj !is Strong.DestroyableBean) {
//                return
//            }
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
                    val bean = createdBeans.find { it.name == dep.name }
                    if (bean != null && !bean.isMatch(dep.clazz)) {
                        throw StrongException("Can't cast \"${bean.name}\" (${bean.beanClass::class.getClassName()}) to ${dep.clazz::getClassName}")
                    }
                    bean
                } else {
                    val beans = createdBeans.filter { it.isMatch(dep.clazz) }
                    when {
                        beans.isEmpty() -> null
                        beans.size == 1 -> beans.first()
                        else -> {
                            val primary = beans.filter { it.primary }
                            when {
                                primary.size == 1 -> primary.first()
                                else -> throw SeveralBeanException(klazz = dep.clazz, name = null)
                            }
                        }
                    }
                }
                if (foundBean == null) {
                    if (dep.require) {
                        throw BeanCreateException(
                            clazz = node.bean::class,
                            name = node.name,
                            cause = NoSuchBeanException(klazz = dep.clazz, name = dep.name),
                        )
                    } else {
                        return@forEach
                    }
                }

                if (!foundBean.isMatch(dep.clazz)) {
                    throw BeanCreateException(
                        clazz = node.bean::class,
                        name = node.name,
                        cause = StrongException("Found invalid bean type. Except ${dep.clazz}, actual ${foundBean.beanClass}"), // TODO сделать нормальное описание ошибки
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
            throw StrongException("Bean ${o.bean::class} in initialization. Requested by ${from?.let { it.bean::class }}")
        }
        o.initing = true
        o.nodes.forEach {
            callInit(it, o)
        }
        if (o.bean is Strong.InitializingBean) {
            o.bean.init(strongImpl)
        }
        o.initing = false
        o.inited = true
    }

    private suspend fun callLink(o: BeanConfig, from: BeanConfig?) {
        if (o.linked) {
            return
        }
        if (o.linking) {
            throw StrongException("Bean ${o::class} in linking. Requested by ${from?.let { it.bean::class }}")
        }
        o.linking = true
        o.nodes.forEach {
            callLink(it, o)
        }
        if (o.bean is Strong.LinkingBean) {
            o.bean.link(strongImpl)
        }
        if (o.bean is Strong.BeanFactory<out Any>) {
            val newBean = o.bean.provide(strongWithDeps)
            if (newBean != null) {
                createdBeans += BeanConfig(
                    bean = newBean,
                    name = o.bean.name,
                    deps = strongWithDeps.getLastDependencies(),
                    primary = o.primary,
                )
            }
        }
        o.linking = false
        o.linked = true
        strongImpl.beans[o.name] = BeanEntity(o.bean, o.primary)
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

    private fun buildInitList(full: List<BeanConfig>) {
        val listForInit = full.filter { it.bean is Strong.InitializingBean }

        fun init(c: BeanConfig) {
            if (c.inited) {
                TODO("Бин уже инициализирован")
            }
        }

        listForInit.forEach {
            it.nodes.find { !it.inited }
        }
    }

    private val logger = Logger.getLogger("Strong.Starter")

    @Suppress("UNCHECKED_CAST")
    private inline fun <T> wrapDependenciesException(func: () -> T): T =
        try {
            func()
        } catch (e: GraphUtils.CycleException) {
            e.printStackTrace()
            throw CycleDependencyException(e.dependenciesPath as List<BeanDescription>)
        }

    internal suspend fun start() {
        STRONG_LOCAL = strongWithDeps
        try {
            logger.debug("Init beans")
            init()
            logger.debug("Resolve bean dependencies")
            makeTree()
            strongWithDeps.initFinish()

            val iniList =
                wrapDependenciesException {
                    GraphUtils.buildDependencyGraph(createdBeans.filter { it.bean is Strong.InitializingBean }) { it.nodes.filter { it.bean is Strong.InitializingBean } }
                }

            val lList =
                wrapDependenciesException {
                    GraphUtils.buildDependencyGraph(createdBeans.filter { it.bean is Strong.LinkingBean }) { it.nodes.filter { it.bean is Strong.LinkingBean } }
                }

            strongImpl.destroyOrder =
                wrapDependenciesException {
                    GraphUtils.buildDependencyGraph(createdBeans.filter { it.bean is Strong.DestroyableBean }) { it.nodes.filter { it.bean is Strong.DestroyableBean } }
                        .map { it.bean as Strong.DestroyableBean }
                }

            createdBeans.forEach {
                strongImpl.beans[it.name] = BeanEntity(bean = it.bean, primary = it.primary)
            }
            STRONG_LOCAL = null
            logger.debug("Bean Graph made")
            iniList.forEach {
                try {
                    val bean = it.bean as Strong.InitializingBean
                    logger.debug("Initializing ${it.name} (${it.bean::class.getClassName()})")
                    bean.init(strongImpl)
                } catch (e: Throwable) {
                    throw StrongException("Can't init bean ${it.name} (${it.bean::class.getClassName()})", e)
                }
            }

            lList.forEach {
                try {
                    val bean = it.bean as Strong.LinkingBean
                    logger.debug("Linking ${it.name} (${it.bean::class.getClassName()})")
                    bean.link(strongImpl)
                    logger.debug("Bean ${it.name} (${it.bean::class.getClassName()}) linked success")
                } catch (e: Throwable) {
                    throw StrongException("Can't link bean ${it.name} (${it.bean::class.getClassName()})", e)
                }
            }

/*
            logger.debug("Check cycle bean init")
            checkCycle()
            val deps = makeDestroysTree()
            strongImpl.beanOrder = deps.map { it.name to it.bean }


            logger.debug("Init beans. Beans for init [${createdBeans.size}]:")
            deps.forEach {
                logger.debug("  * ${it.name} (${it.beanClass})")
            }

            deps.forEach { bean ->
                bean.nodes.forEach {
                    if (!it.inited) {
                        throw RuntimeException("Can't init ${bean.name} (${bean.beanClass}). Bean ${it.name} (${it.beanClass}) not inited!")
                    }
                }
                bean.inited = true
            }

            deps.forEach { bean ->
                bean.nodes.forEach {
                    if (!it.linked) {
                        throw RuntimeException("Can't init ${bean.name} (${bean.beanClass}). Bean ${it.name} (${it.beanClass}) not linked!")
                    }
                }
                bean.linked = true
            }
            deps.forEach { bean ->
                bean.inited = false
                bean.linked = false
            }

            var listForStart = ArrayList(createdBeans)
            LOOP@ do {
                for (it in listForStart) {
                    logger.debug("#1 ${listForStart.map { "${it.name} ${it.inited} ${it.linked}" }}")
                    if (!it.inited) {
                        if (it.bean is Strong.InitializingBean) {
                            val notInitedBeans = it.nodes.filter { !it.inited }
                            if (notInitedBeans.isNotEmpty()) {
                                listForStart.removeAll(notInitedBeans)
                                listForStart.addAll(0, notInitedBeans)
                                continue@LOOP
                            }
                            logger.debug("Bean ${it.name} (${it.beanClass}) initing")
                            it.bean.init(strongImpl)
                            it.inited = true
                        } else {
                            it.inited = true
                        }
                        logger.debug("Bean ${it.name} (${it.beanClass}) inited")
                        strongImpl.beans[it.name] = BeanEntity(bean = it.bean, primary = it.primary)
                        continue@LOOP
                    }
                    if (!it.linked) {
                        if (it.bean is Strong.LinkingBean) {
                            val notInitedBeans = it.nodes.filter { !it.linked || !it.inited }
                            if (notInitedBeans.isNotEmpty()) {
                                listForStart.removeAll(notInitedBeans)
                                listForStart.addAll(0, notInitedBeans)
                                continue@LOOP
                            }
                            logger.debug("Linking ${it.name} (${it.beanClass})")
                            it.bean.link(strongImpl)
                            it.linked = true
                        } else {
                            it.linked = true
                        }
                        logger.debug("Bean ${it.name} (${it.beanClass}) linked")
                        listForStart.remove(it)
                        continue@LOOP
                    }
                }
                if (listForStart.isEmpty()) {
                    break
                }
                logger.debug("Init list size: ${listForStart.size}")
            } while (listForStart.isNotEmpty())
            */
            logger.debug("Strong bean initialized successful")
        } catch (e: Throwable) {
            logger.debug("Strong bean initialized fail", e)
            throw e
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
