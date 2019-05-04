package pw.binom.io.examples.httpServer

import pw.binom.io.IOException
import pw.binom.io.readln
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.write
import pw.binom.io.writeln

class HttpServer : ConnectionManager() {
    override fun connected(connection: Connection) {
        connection {
            try {
                val header = it.input.readln().split(' ')
                println("Request ${header[0]} ${header[1]}")

                //skip all request headers
                while (true) {
                    if (it.input.readln().isEmpty())
                        break
                }

                val txt = """<html>
                |<title>Binom Example Web Server</title>
                |<body>
                |  Hello from Simple server based on <b>Binom IO</b>
                |</body>
                |</html>""".trimMargin()
                it.output.writeln("HTTP/1.1 200 OK")
                it.output.writeln("Server: Binom Example Server")
                it.output.writeln("Content-Type: text/html; charset=utf-8")
                it.output.writeln("Content-Length: ${txt.length}")
                it.output.writeln("Connection: close")
                it.output.writeln("")
                it.output.writeln("")
                it.output.write(txt)
            } catch (e: IOException) {
                //NOP
            }
        }
    }
}

fun main(args: Array<String>) {
    val server = HttpServer()
    server.bind(8899)
    while (true) {
        server.update()
    }
}