package pw.binom.smtp

import pw.binom.base64.Base64
import pw.binom.io.AsyncChannel
import pw.binom.io.IOException
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter

class BaseSMTPClient(val connect: AsyncChannel):SMTPClient {
    private val writer = connect.bufferedAsciiWriter()
    private val reader = connect.bufferedAsciiReader()

    private var code = 0
    private var description: String? = null
    private var cmd: String? = null
    private var msgStarted = true
    private var started = false

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

    internal suspend fun start(sendFromDomain: String, login: String, password: String) {
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
        started = true
    }

    override suspend fun multipart(
        from: String,
        fromAlias: String?,
        to: String,
        toAlias: String?,
        subject: String?,
        msg: suspend (HtmlMultipartMessage) -> Unit
    ) {
        if (!started) {
            throw IllegalStateException("Smtp not finished handshake process. Wait until SMTPClient.tcp or SMTPClient.tls is finished")
        }
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

    override suspend fun asyncClose() {
        writer.append("QUIT\r\n")
        writer.flush()
        checkResponse(221, 250)
        connect.asyncClose()
    }
}