# Binom Socket IO
Kotlin Library for work with Socket.<br>
Allows using for blocked and non-blocked sockets. Supports only TCP.
## Using
### Gradle
```groovy
repositories {
    maven {
        url "http://repo.tlsys.su/maven/releases"
    }
}
dependencies {
    api "pw.binom.io:socket:<version>"
}
```

## Simple using
### Client
```kotlin
val client = Socket()
client.connect("example.com", 80)
val data = ByteBuffer(512)
client.read(data)
client.close()
```

### Server via Blocking IO
```kotlin
val server = SocketServer()
val client = server.accept()
val data = ByteBuffer(512)
client.read(data)
client.close()
server.close()
```

### Server via Non-Blocking IO
```kotlin
val server = ServerSocketChannel()
val selector = SocketSelector(512)
server.blocking = false
server.bind(8080)
selector.reg(server)

val buffer = ByteArray(256)
while (true) {
    selector.process {
        if (it.channel === server) {
            val client = server.accept()!!
            client.blocking=false
            selector.reg(client)
            println("Client connected")
        } else {
            try {
                val client = it.channel as SocketChannel
                val len = client.read(buffer)
                client.write(buffer, 0, len)
            } catch (e: SocketClosedException) {
                it.cancel()
                println("Client disconnected")
            }
        }
    }
}
```

## Examples
* [Echo server](../examples/echoServer)