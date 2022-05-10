package pw.binom.strong

// interface StrongDefiner : Strong, Definer

// inline fun <reified T : Closeable> StrongDefiner.lazyDefineClosable(
//    name: String = "${T::class}_${T::class.hashCode()}",
//    ifNotExist: Boolean = false,
//    noinline func: suspend (StrongDefiner) -> T
// ) {
//    define(
//        bean = object : Strong.ServiceProvider {
//            override suspend fun provide(strong: StrongDefiner) {
//                defineClosable(func(strong))
//            }
//        },
//        name = name,
//        ifNotExist = ifNotExist,
//    )
// }

// @JvmName("lazyDefineAsyncCloseable")
// inline fun <reified T : AsyncCloseable> StrongDefiner.lazyDefineClosable(
//    name: String = "${T::class}_${T::class.hashCode()}",
//    ifNotExist: Boolean = false,
//    noinline func: suspend (StrongDefiner) -> T
// ) {
//    define(
//        bean = object : Strong.ServiceProvider {
//            override suspend fun provide(strong: StrongDefiner) {
//                defineClosable(func(strong))
//            }
//        },
//        name = name,
//        ifNotExist = ifNotExist,
//    )
// }

// inline fun <reified T : Any> StrongDefiner.lazyDefine(
//    name: String = "${T::class}_${T::class.hashCode()}",
//    ifNotExist: Boolean = false,
//    noinline func: suspend (StrongDefiner) -> T
// ) {
//    define(
//        bean = object : Strong.ServiceProvider {
//            override suspend fun provide(strong: StrongDefiner) {
//                func(strong)
//            }
//        },
//        name = name,
//        ifNotExist = ifNotExist
//    )
// }
//
// inline fun<reified T:AsyncCloseable> Definer.defineClosable(
//    name: String?=null,
//    ifNotExist: Boolean = false,
//    noinline bean: (Strong) -> T,
// ) {
//    bean(name=name,ifNotExist = ifNotExist,bean = bean)
//    executeOnDestroy {
//        bean.asyncClose()
//    }
// }
//
// fun StrongDefiner.defineClosable(
//    bean: Closeable,
//    name: String = "${bean::class}_${bean::class.hashCode()}",
//    ifNotExist: Boolean = false
// ) {
//    define(
//        bean = bean,
//        name = name,
//        ifNotExist = ifNotExist,
//    )
//    executeOnDestroy {
//        bean.close()
//    }
// }
//
// fun StrongDefiner.executeOnDestroy(func: suspend () -> Unit) {
//    define(
//        bean = object : Strong.DestroyableBean {
//            override suspend fun destroy(strong: Strong) {
//                func()
//            }
//        },
//        name = Random.uuid().toString()
//    )
// }
