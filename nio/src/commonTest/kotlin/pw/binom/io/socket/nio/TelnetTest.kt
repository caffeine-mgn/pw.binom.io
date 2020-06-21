package pw.binom.io.socket.nio

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.Input
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.readln
import pw.binom.io.socket.ServerSocketChannel
import pw.binom.io.socket.SocketFactory
import pw.binom.io.socket.SocketSelector
import pw.binom.io.socket.rawSocketFactory
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.pool.DefaultPool
import pw.binom.thread.FixedThreadPool
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class TelnetTest {
    /*
        sealed class IOOperation {

            class Read : IOOperation() {
                lateinit var data: ByteArray
                var offset: Int = 0
                var length: Int = 0
                var out: InputStream? = null
                lateinit var cc: Continuation<Int>

            }

            class Open : IOOperation() {
                lateinit var file: File
                lateinit var cc: Continuation<InputStream?>
            }

            class Close : IOOperation() {
                var out: InputStream? = null
                lateinit var cc: Continuation<Unit>
            }
        }


            class AsyncFS : Closeable {
                val readPool = DefaultPool<IOOperation.Read>(10) {
                    IOOperation.Read()
                }
                val openPool = DefaultPool<IOOperation.Open>(10) {
                    IOOperation.Open()
                }
                val closePool = DefaultPool<IOOperation.Close>(10) {
                    IOOperation.Close()
                }
                val readStack = Stack<IOOperation>().asFiFoQueue()
                suspend fun open(file: File): AsyncInputStream? {
                    val stream = suspendCoroutine<InputStream?> { con ->
                        readStack.push(openPool.borrow {
                            it.file = file
                            it.cc = con
                        })
                    } ?: return null


                    return object : AsyncInputStream {
                        override suspend fun read(): Byte {
                            TODO("Not yet implemented")
                        }

                        override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
                            return suspendCoroutine { con ->
                                readStack.push(readPool.borrow {
                                    it.cc = con
                                    it.data = data
                                    it.offset = offset
                                    it.length = length
                                    it.out = stream
                                })
                            }
                        }

                        override suspend fun close() {
                            suspendCoroutine<Unit> { con ->
                                readStack.push(closePool.borrow {
                                    it.cc = con
                                    it.out = stream
                                })
                            }
                        }

                    }
                }

                private var closed = AtomicBoolean(false)


                private val thread = Thread(Runnable {
                    val rr = PopResult<IOOperation>()
                    while (!closed.value) {
                        readStack.pop(rr)
                        if (rr.isEmpty) {
                            continue
                        }

                        when (val r = rr.value) {
                            is IOOperation.Close -> {
                                r.cc.resumeWith(kotlin.runCatching {
                                    r.out!!.close()
                                })
                                closePool.recycle(r)
                            }

                            is IOOperation.Open -> {
                                r.cc.resumeWith(kotlin.runCatching {
                                    r.file.inputStream
                                })
                                openPool.recycle(r)
                            }

                            is IOOperation.Read -> {
                                r.cc.resumeWith(kotlin.runCatching {
                                    r.out!!.read(
                                            data = r.data,
                                            offset = r.offset,
                                            length = r.length
                                    )
                                })
                                readPool.recycle(r)
                            }
                        }
                    }
                })

                override fun close() {
                    closed.value = true
                }

                init {
                    thread.start()
                }
            }
        */
    class MyHandler : SocketNIOManager.ConnectHandler {

        val done = AtomicBoolean(false)
//        val asyncFs = AsyncFS()

        val pool = DefaultPool(10) {
            ByteDataBuffer.alloc(1024 * 1024)
        }

        lateinit var manager: SocketNIOManager

        @ExperimentalTime
        override fun clientConnected(connection: SocketNIOManager.ConnectionRaw, manager: SocketNIOManager) {
            try {
                connection { con ->

                    var readFrom = Duration.ZERO
                    var writeTo = Duration.ZERO

                    val reader = con.utf8Reader()
                    val q = reader.readln()
                    println("Query: $q")
                    while (true) {
                        val s = reader.readln()
                        if (s == null || s.isEmpty())
                            break
                    }

                    suspend fun Input.copyTo2(outputStream: AsyncOutput): ULong {
                        var out = 0uL
                        val buffer = pool.borrow()
                        val total = try {
                            measureTime {
                                while (true) {
                                    val len: Int
                                    readFrom += measureTime {
                                        len = read(buffer)
                                    }
                                    if (len <= 0) {
                                        break
                                    }
                                    writeTo += measureTime {
                                        out += outputStream.write(buffer, 0, len).toULong()
                                    }
                                }
                            }
                        } finally {
                            pool.recycle(buffer)
                        }
                        outputStream.flush()
                        println("Done. Read: $readFrom, Write: $writeTo, Total: $total, Wrote Bytes: $out bytes. Update time: [${manager.updateTime}]")
                        manager.updateTime = Duration.ZERO
//                        con.sendTime = Duration.ZERO
//                        con.waitWrite = Duration.ZERO
                        return out
                    }

                    File("D:\\WORK\\WebTest\\static\\33.stl").channel(AccessType.READ).use {
                        it.copyTo2(con)
                    }
                    con
//                    con.output.write("Hello from server!\n".encodeToByteArray())
                    con.flush()
                    con.close()
                }
            } catch (e: Throwable) {
                println("ERRORRRR! $e")
                throw e
            }
        }

    }

    @Test
    fun test() {
        val pool = FixedThreadPool(10)
        val v = PoolThreadNioManager(pool)
        val handler = MyHandler()
        handler.manager = v
        v.bind(port = 2300, handler = handler)

        while (true) {
            v.update(1000)
        }
        handler.done.value = true
    }

    @Test
    fun fff() {
        val ss = SocketSelector(10)
        val server = SocketFactory.rawSocketFactory.createSocketServerChannel()
        server.blocking = false
        ss.reg(server)
        server.bind(port = 2300)

        while (true) {
            ss.process {
                if (it.channel is ServerSocketChannel) {
                    val client = (it.channel as ServerSocketChannel).accept()!!
                    client.blocking = false
                    ss.reg(client)
                } else
                    println("!!")
            }
        }
    }
}