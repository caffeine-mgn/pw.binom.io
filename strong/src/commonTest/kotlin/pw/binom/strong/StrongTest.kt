package pw.binom.strong

import pw.binom.async2
import kotlin.reflect.KClass
import kotlin.test.Ignore
import kotlin.test.Test

class StrongTest {

    interface A
    interface B
    class AImpl : A
    class BImpl : B
    class ABImpl : A, B

//    @Test
//    fun serviceList() {
//        asyncTest {
//            val s = Strong.create(Strong.config {
//                it.define(AImpl())
//                it.define(BImpl())
//                it.define(ABImpl())
//            })
//            val list by s.injectServiceList<A>()
//            assertEquals(2, list.size)
//            assertTrue(list.any { it is AImpl })
//            assertTrue(list.any { it is ABImpl })
//        }
//    }

    interface Client
    class OO : Client
    class VV

    @Ignore
    @Test
    fun aaa() {
        class A
        val s = async2 {
            val factory = object : Strong.BeanFactory<A> {
                override val type: KClass<A>
                    get() = A::class

                override suspend fun provide(strong: Strong): A = A()
            }

            class TestClass:Strong.Bean(){
                val a by strong.inject<A>()
            }

            val s = Strong.create(
                Strong.config {
                    it.bean { factory }
                    it.bean { TestClass() }
                }
            )
            println("->${s.service(TestClass::class).service.a}")
        }
        if (s.isFailure){
            throw s.exceptionOrNull!!
        }
    }

    @Test
    fun aa() {
        val cc = async2 {
            class A : Strong.InitializingBean {
                override suspend fun init(strong: Strong) {
                    println("init A")
                }

            }

            class B : Strong.InitializingBean {
                override suspend fun init(strong: Strong) {
                    println("init B")
                }

            }

            class Prov : Strong.Bean(), Strong.InitializingBean {
                val a by strong.inject<A>()

                override suspend fun init(strong: Strong) {
                    println("init Prov")
                }

            }

            class MyBean4 : Strong.Bean()
            class MyBean2
            class MyBean3 : Strong.InitializingBean, Strong.LinkingBean, Strong.Bean() {
                private val bean4 by strong.inject<MyBean4>()
                override suspend fun init(strong: Strong) {
                    println("init MyBean3")
                }

                override suspend fun link(strong: Strong) {
                    println("link MyBean3")
                }

            }

            class Server(val client: Client)
            class ClientImpl(val m: MyBean3) : Client

            class ClientWraper : Client, Strong.Bean(), Strong.InitializingBean {
                private val lll by strong.inject<MyBean3>()
                override suspend fun init(strong: Strong) {
                    ClientImpl(lll)
                }

            }

            class MyBean : Strong.InitializingBean, Strong.LinkingBean, Strong.Bean() {
                private val bean3 by strong.inject<MyBean3>()

                override suspend fun init(strong: Strong) {
                    println("init MyBean")
                }

                override suspend fun link(strong: Strong) {
                    println("link MyBean")
                }
            }

            val strong = StrongImpl()


            val ss = Strong.create(
                Strong.config { definer ->
                    definer.bean { MyBean() }
                    definer.bean { MyBean3() }
                    definer.bean { MyBean4() }
                    definer.bean { ClientWraper() }
                    definer.wrap<MyBean3,ClientImpl> { bean3 -> ClientImpl(bean3) }
                }
            )

//            val starter = Starter(
//                strong,
//                listOf(
//                    Starter.Definition(name = Random.nextUuid().toString(), Prov::class, { Prov() }),
//                    Starter.Definition(name = Random.nextUuid().toString(), A::class, { A() }),
//                    Starter.Definition(name = Random.nextUuid().toString(), B::class, { B() }),
//                )
//            )
//            starter.start()
        }
        if (cc.isFailure) {
            throw cc.exceptionOrNull!!
        }
    }
}

private inline fun <reified P1 : Any, reified T : Any> Definer.wrap(noinline func: (P1) -> T) {
    bean {
        val o by it.inject<P1>()
        object : Strong.BeanFactory<T> {
            override val type: KClass<T>
                get() = T::class

            override suspend fun provide(strong: Strong): T =
                func(o)
        }
    }
}