package pw.binom.io.examples.httpServer

import pw.binom.io.*
import pw.binom.io.socket.nio.SocketNIOManager

class HttpServerHandler : SocketNIOManager.ConnectHandler {
    override fun clientConnected(connection: SocketNIOManager.ConnectionRaw, manager: SocketNIOManager) {
        connection {
            try {
                val header = it.utf8Reader().readln()!!.split(' ')
                println("Request ${header[0]} ${header[1]}")

                //skip all request headers
                while (true) {
                    if (it.utf8Reader().readln()?.isNotEmpty() != false)
                        break
                }

                val txt = """<html>
                |<title>Binom Example Web Server</title>
                |<body>
                |  Hello from Simple server based on <b>Binom IO</b>
                |</body>
                |</html>""".trimMargin()
                val app = it.utf8Appendable()
                app.appendln("HTTP/1.1 200 OK")
                        .appendln("Server: Binom Example Server")
                        .appendln("Content-Type: text/html; charset=utf-8")
                        .appendln("Content-Length: ${txt.length}")
                        .appendln("Connection: close")
                        .appendln("")
                        .appendln("")
                        .appendln(txt)
            } catch (e: IOException) {
                //NOP
            }
        }
    }

}

fun main(args: Array<String>) {
    val nioManager = SocketNIOManager()
    nioManager.bind(port = 8899, handler = HttpServerHandler())
    while (true) {
        nioManager.update()
    }
}