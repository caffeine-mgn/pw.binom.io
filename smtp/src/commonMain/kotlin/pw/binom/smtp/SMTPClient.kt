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

class SMTPClient(val connect: AsyncChannel) : AsyncCloseable {

    val pool = ByteBufferPool(10)
    val writer = connect.bufferedAsciiWriter()
    val reader = connect.bufferedAsciiReader()

    private var code = 0
    private var description: String? = null
    private var cmd: String? = null
    private var msgStarted = true

    private suspend fun readCmd2() {
        while (true) {
            val txt = reader.readln() ?: throw IOException("Can't read smtp response")
            if (txt.isBlank()) {
                continue
            }
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
            break
        }
    }

    private suspend fun start(sendFromDomain: String, login: String, password: String) {
        checkResponse(250, 220)
        writer.append("EHLO ").append(sendFromDomain).append("\r\n")


        writer.append("AUTH LOGIN\r\n")
        writer.flush()
        while (true) {
            readCmd2()
            if (code == 334) {
                if (description == "VXNlcm5hbWU6") {
                    writer.append(Base64.encode(login.encodeToByteArray())).append("\r\n")
                    writer.flush()
                    continue
                }
                if (description == "UGFzc3dvcmQ6") {
                    writer.append(Base64.encode(password.encodeToByteArray())).append("\r\n")
                    writer.flush()
                    continue
                }
            }
            if (code == 535) {
                throw IOException(description)
            }
            if (code == 235) {
                break
            }
        }
    }

//    suspend fun send(to: String, from: String?, body: String) {
//        writer.append("MAIL FROM:<")
//        if (from != null) {
//            writer.append(from)
//        }
//        writer.append(">\r\n")
//        writer.flush()
//        checkResponse(250)
//
//        writer.append("RCPT TO:<").append(to).append(">\r\n")
//        writer.flush()
//        checkResponse(250)
//
//        writer.append("DATA\r\n")
//        writer.flush()
//        checkResponse(354)
//
//        writer.append(body.replace("\r\n.\r\n", "\r\n..\r\n")).append("\r\n.\r\n")
//        writer.flush()
//        checkResponse(250)
//    }

    suspend fun multipart(
        from: String,
        fromAlias: String?,
        to: String,
        toAlias: String?,
        subject: String?,
        msg: suspend (HtmlMultipartMessage) -> Unit
    ) {
        if (msgStarted) {
            writer.append("RSET\r\n")
            writer.flush()
            checkResponse(250)
        }
        msgStarted = true
        writer.append("MAIL FROM:<")
        writer.append(from)
        writer.append(">\r\n")
        writer.flush()
        checkResponse(250)

        writer.append("RCPT TO:<").append(to).append(">\r\n")
        writer.flush()
        checkResponse(250)
        writer.append("DATA\r\n")
        writer.flush()
        checkResponse(354)

        val m = HtmlMultipartMessage(writer)
        m.start(
            from = from,
            fromAlias = fromAlias,
            to = to,
            toAlias = toAlias,
            subject = subject
        )
        msg(m)
        m.finish()
        writer.append("\r\n.\r\n")
        writer.flush()
        checkResponse(250)
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
        ): SMTPClient {
            val connect = dispatcher.tcpConnect(address)
            val client = SMTPClient(connect)
            return try {
                client.start(sendFromDomain = fromEmail, login = login, password = password)
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
        ): SMTPClient {
            val connect = dispatcher.tcpConnect(address)
            val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
            val clientSession = sslContext.clientSession(tlsHost, tlsPort)
            val sslConnect = clientSession.asyncChannel(connect)
            val client = SMTPClient(sslConnect)
            return try {
                client.start(sendFromDomain = fromEmail, login = login, password = password)
                client
            } catch (e: Throwable) {
                client.asyncClose()
                throw e
            }
        }
    }

    override suspend fun asyncClose() {
        writer.append("QUIT\r\n")
        writer.flush()
        checkResponse(221, 250)
        connect.asyncClose()
    }
}