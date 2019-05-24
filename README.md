# Binom IO
Kotlin IO Library.<br>

## Actual version
Actual version is `0.1.3`

## Parts of library
Library contains next parts:<br>
* [Core IO](core)
* [Socket Library](socket)
* [Async Server](server)
* [Http Client](httpClient)
* [Http Server](httpServer)
* [File Utils](file)
* [Json Tools](json)
* [XML Tools](xml)
* [WebDav](webdav)
* [Job Executor](job)

## Using
### Gradle
Before using you must add repository:
```groovy
repositories {
    maven {
        url "http://repo.binom.pw/releases"
    }
}
```

## Plans
### Version 0.2
#### File IO
- [ ] Mechanism for read/write file via one entity

#### Socket IO
- [ ] UDP support socket
- [ ] Opportunity set bind interface when you start your server
- [ ] SSL Sockets
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
- [ ] SSL
- [x] Logger<br>
See [Logger](logger/src/commonMain/kotlin/pw/binom/logger/Logger.kt)

#### HTTP
- [x] Basic Support Http Support
See [HttpServer](httpServer/src/commonMain/kotlin/pw/binom/io/httpServer/HttpServer.kt)
- [ ] HTTP Server: WebSocket Support
- [ ] HTTPS Support



## Using Library in Projects:
[Simple Lightweight Binary Repository](https://github.com/caffeine-mgn/repository)