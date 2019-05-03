package pw.binom.io.examples.httpServer

import pw.binom.io.AsyncInputStream
import pw.binom.io.OutputStream
import pw.binom.io.readLn
import pw.binom.io.socket.AsyncServer
import pw.binom.io.socket.Socket
import pw.binom.io.write

class HttpServer(port: Int) : AsyncServer(port) {
    override suspend fun client(input: AsyncInputStream, output: OutputStream) {
        //read request method and uri
        val header = input.readLn().split(' ')
        println("Request ${header[0]} ${header[1]}")

        //skip all request headers
        while (true) {
            if (input.readLn().isEmpty())
                break
        }

        val txt = """<html>
            |<title>Binom Example Web Server</title>
            |<body>
            |  Hello from Simple server based on <b>Binom IO</b>
            |</body>
            |</html>""".trimMargin()

        output.write("HTTP/1.1 200 OK\r\n")
        output.write("Server: Binom Example Server\r\n")
        output.write("Content-Type: text/html; charset=utf-8\r\n")
        output.write("Content-Length: ${txt.length}\r\n")
        output.write("Connection: close\r\n")
        output.write("\r\n\r\n")
        output.write(txt)
    }
}

fun main(args: Array<String>) {
    val server = HttpServer(8899)
    server.start()
}