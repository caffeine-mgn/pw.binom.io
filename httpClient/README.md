# Binom Async HTTP Client
Kotlin Library for create Async HTTP Client

## Using
### Gradle
You must add repository. See [README](../README.md)
```groovy
dependencies {
    api "pw.binom.io:httpClient:<version>"
}
```

## Features
* Async client
* Can be attach to common Connection Manager
* Supports `Connection: keep-alive`
* Supports `Transfer-Encoding: chunked`

Что нужно поддержать:
* https, wss
* переиспользование соединения
* * на http 1.1 это пул
* * на http 2 это мультиплексирование
* http/https proxy