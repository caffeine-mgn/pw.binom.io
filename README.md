# Binom IO
Kotlin IO Library.<br>

## Actual version
Actual version is `0.1.3`

## Parts of library
Library contains next parts:<br>
* [Common Core IO](core/README.md)
* [Common Socket Library](socket/README.md)
* [Common Async Server](server/README.md)
* [Common Http Client](httpClient/README.md)
* [Common File Utils](file/README.md)
* [Job Executor](job/README.md)

## Plans
### Version 0.2
#### File IO
- [ ] Mechanism for read/write file via one entity

#### Socket IO
- [ ] UDP support socket
- [ ] Opportunity set bind interface when you start your server
- [ ] SSL Sockets

#### Common
- [x] Create common reactor for different events. Not only network<br>
See [Stack](core/src/commonMain/kotlin/pw/binom/Stack.kt) and [FreezedStack](core/src/commonMain/kotlin/pw/binom/FreezedStack.kt)
- [x] Base64 Tools<br>
See [Base64](core/src/commonMain/kotlin/pw/binom/Base64.kt), [Base64EncodeOutputStream](core/src/commonMain/kotlin/pw/binom/Base64EncodeOutputStream.kt) and [Base64DecodeAppendable](core/src/commonMain/kotlin/pw/binom/Base64DecodeAppendable.kt)
- [x] Json Tools <br>
See [JsonWriter](json/src/commonMain/kotlin/pw/binom/json/JsonWriter.kt) and [JsonReader](json/src/commonMain/kotlin/pw/binom/json/JsonReader.kt)
- [ ] JsonB Tools
- [ ] XML Tools
- [ ] SSL

#### HTTP
- [x] Basic Support Http Support
See [HttpServer](httpServer/src/commonMain/kotlin/pw/binom/io/httpServer/HttpServer.kt)
- [ ] HTTP Server: WebSocket Support
- [ ] HTTPS Support