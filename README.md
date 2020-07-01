# Binom IO
Kotlin IO Library.<br>

## Actual version
Actual version is `0.1.17`

## Parts of library
Library contains next parts:<br>
* [Core IO](core)
* [Socket Library](socket)
* [Async NIO](nio)
* [Http Client](httpClient)
* [Http Server](httpServer)
* [File Utils](file)
* [Json Tools](json)
* [RPC Tool Plugin](krpcPlugin)
* [JSON RPC Implementation](rpcJson)
* [XML Tools](xml)
* [WebDav](webdav)
* [Job Executor](job)
* [Process API](process)
* [SSL](ssl)
* [Thread](thread)

## Using
### Gradle
Before using you must add repository:
```groovy
repositories {
    maven {
        url "https://repo.binom.pw/releases"
    }
}
```

For using plugins you must use it:
```groovy
buildscript {
    repositories {
        maven {
            url "https://repo.binom.pw/releases"
        }
    }
}
```

## Plans
### Version 0.2

#### Process API
- [x] Tools for execute external process and get external IO<br>
See [Process](process/src/commonMain/kotlin/pw/binom/process/Process.kt)

#### Socket IO
- [ ] UDP support socket
- [x] Opportunity set bind interface when you start your server
- [x] SSL Sockets
#### WebDav
- [x] WebDav Server Handler<br>
See [AbstractWebDavHandler](webdav/src/commonMain/kotlin/pw/binom/webdav.server/AbstractWebDavHandler.kt)
- [ ] WebDav Client

#### Common
- [x] Create common reactor for different events. Not only network<br>
See [Stack](core/src/commonMain/kotlin/pw/binom/Stack.kt) and [FreezedStack](core/src/commonMain/kotlin/pw/binom/FreezedStack.kt)
- [x] Base64 Tools<br>
See [Base64](core/src/commonMain/kotlin/pw/binom/Base64.kt), [Base64EncodeOutputStream](core/src/commonMain/kotlin/pw/binom/Base64EncodeOutputStream.kt) and [Base64DecodeAppendable](core/src/commonMain/kotlin/pw/binom/Base64DecodeAppendable.kt)
- [x] Json Tools <br>
See [JsonWriter](json/src/commonMain/kotlin/pw/binom/json/JsonWriter.kt) and [JsonReader](json/src/commonMain/kotlin/pw/binom/json/JsonReader.kt)
- [ ] JsonB Tools
- [x] [ObjectPool](core/src/commonMain/kotlin/pw/binom/pool/DefaultPool.kt)
- [x] XML Tools<br>
See SAX Tools:
[XmlVisiter](xml/src/commonMain/kotlin/pw/binom/xml/sax/XmlVisiter.kt),
[XmlReaderVisiter](xml/src/commonMain/kotlin/pw/binom/xml/sax/XmlReaderVisiter.kt),
[XmlWriterVisiter](xml/src/commonMain/kotlin/pw/binom/xml/sax/XmlWriterVisiter.kt),
[XmlRootReaderVisiter](xml/src/commonMain/kotlin/pw/binom/xml/sax/XmlRootReaderVisiter.kt)
and [XmlRootWriterVisiter](xml/src/commonMain/kotlin/pw/binom/xml/sax/XmlRootWriterVisiter.kt)<br>
See DOM Tools:
[XmlElement](xml/src/commonMain/kotlin/pw/binom/xml/dom/XmlElement.kt),
[XmlDomReader](xml/src/commonMain/kotlin/pw/binom/xml/dom/XmlDomReader.kt)
and Method [xml](xml/src/commonMain/kotlin/pw/binom/xml/dom/TagWriteContext.kt)
- [x] SSL
- [x] Logger<br>
See [Logger](logger/src/commonMain/kotlin/pw/binom/logger/Logger.kt)
- [x] RPC Tool Plugin. See [Plugin](krpcPlugin), [RPC Library](krpc)
- [x] JSON RPC [Implementation](rpcJson) 

#### HTTP
- [x] Basic Support Http Support
See [HttpServer](httpServer/src/commonMain/kotlin/pw/binom/io/httpServer/HttpServer.kt)
- [ ] HTTP Server: WebSocket Support
- [x] HTTPS Support

#### Sync DataBase Access
- [x] [Common SQL Interfaces](db/README.md)
- [x] [SQLite Support](sqlite/README.md)




## Using Library in Projects:
[Simple Lightweight Binary Repository](https://github.com/caffeine-mgn/repository)