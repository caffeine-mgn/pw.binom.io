package pw.binom.concurrency

import kotlin.test.Test

class ReferenceTest {

    class A {
        fun doSomething() {

        }
    }

    @Test
    fun closeReferenceFromOtherThread() {
        val w = WorkerPool(10)
        val a = A()
        val aRef = a.asReference()
        val f = w.submit {
            aRef.close()
        }
        while (!f.isDone) {
            Worker.sleep(50)
        }
        a.doSomething()
        if (f.isFailure) {
            throw f.exceptionOrNull!!
        }
    }
}