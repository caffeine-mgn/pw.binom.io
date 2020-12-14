package pw.binom.smtp

import pw.binom.ByteBufferPool
import pw.binom.base64.Base64
import pw.binom.io.*
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.KeyManager
import pw.binom.ssl.SSLContext
import pw.binom.ssl.SSLMethod
import pw.binom.ssl.TrustManager

class Client(val connect: AsyncChannel) : AsyncCloseable {

    val pool = ByteBufferPool(10)
    val writer = connect.bufferedWriter(pool)
    val reader = connect.bufferedReader(pool)

    private var code = 0
    private var description: String? = null
    private var cmd: String? = null

    private suspend fun readCmd2() {
        val txt = reader.readln() ?: throw IOException("Can't read smtp response")
        println(">>$txt")
        val items = txt.split(' ', limit = 2)
        description = items.getOrNull(1)
        val first = items[0]
        if (first.length > 3) {
            code = first.substring(0, 3).toInt()
            cmd = first.substring(3)
        } else {
            code = first.toInt()
            cmd = null
        }
    }

    private suspend fun start(fromAddr: String, login: String, password: String) {
        checkResponse(250, 220)

        println("\n\nstart")
        val fromAddr = "tlsys.org"
        writer.append("EHLO ").append(fromAddr).append("\r\n")


        writer.append("AUTH LOGIN\r\n")
        writer.flush()
        while (true) {
            readCmd2()
            println("code: $code [$description]")
            if (code == 334) {
                if (description == "VXNlcm5hbWU6") {
                    println("send login.... [$login]")
                    writer.append(Base64.encode(login.encodeToByteArray())).append("\r\n")
                    writer.flush()
                    continue
                }
                if (description == "UGFzc3dvcmQ6") {
                    println("send password....[$password]")
                    writer.append(Base64.encode(password.encodeToByteArray())).append("\r\n")
                    writer.flush()
                    continue
                }
            }
            if (code == 535) {
                throw IOException(description)
            }
            if (code == 235) {
                println("Loggined!")
                break
            }
        }
        println("end\n\n")
    }

    suspend fun send(to: String, from: String?, body: String) {
        println("\n\nsend--start")
        writer.append("MAIL FROM:<")
        if (from != null) {
            writer.append(from)
        }
        writer.append(">\r\n")
        writer.flush()
        checkResponse(250)

        writer.append("RCPT TO:<").append(to).append(">\r\n")
        writer.flush()
        checkResponse(250)
        println("send--end\n\n")

        println("\n\ndata--start")
        writer.append("DATA\r\n")
        writer.flush()
        checkResponse(354)
        println("data--end\n\n")

        println("sending data - start")
        writer.append(body.replace("\r\n.\r\n", "\r\n..\r\n")).append("\r\n.\r\n")
        writer.flush()
        checkResponse(250)
//        while (true) readCmd2()
        println("sending data - end")
    }

    private suspend fun checkResponse(cmd: Int) {
        readCmd2()
        if (code != cmd) {
            throw IOException("Invalid response code. ${code} ${this.cmd ?: ""} ${description}")
        }
    }

    private suspend fun checkResponse(cmd1: Int, cmd2: Int) {
        readCmd2()
        if (code != cmd1 && code != cmd2) {
            throw IOException("Invalid response code. ${code} ${this.cmd ?: ""} ${description}")
        }
    }

    companion object {
        suspend fun tcp(
            dispatcher: NetworkDispatcher,
            login: String,
            password: String,
            fromEmail: String,
            address: NetworkAddress
        ): Client {
            val connect = dispatcher.tcpConnect(address)
            val client = Client(connect)
            return try {
                client.start(fromAddr = fromEmail, login = login, password = password)
                client
            } catch (e: Throwable) {
                client.asyncClose()
                throw e
            }
        }

        suspend fun tls(
            dispatcher: NetworkDispatcher,
            login: String,
            password: String,
            fromEmail: String,
            address: NetworkAddress,
            keyManager: KeyManager,
            trustManager: TrustManager,
            tlsHost: String = address.host,
            tlsPort: Int = address.port,
        ): Client {
            val connect = dispatcher.tcpConnect(address)
            val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
            val clientSession = sslContext.clientSession(tlsHost, tlsPort)
            val sslConnect = clientSession.asyncChannel(connect)
            val client = Client(sslConnect)
            return try {
                client.start(fromAddr = fromEmail, login = login, password = password)
                client
            } catch (e: Throwable) {
                client.asyncClose()
                throw e
            }
        }
    }

    override suspend fun asyncClose() {
        println("Try close...")
        writer.append("QUIT\r\n")
        writer.flush()
        println("wating quit")
        checkResponse(221, 250)
        println("done!")
        connect.asyncClose()
    }
}