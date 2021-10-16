# Binom SMTP
Kotlin Library for send email using SMTP

## Using
### Gradle
You must add repository. See [README](../README.md)
```groovy
dependencies {
    api "pw.binom.io:smtp:<version>"
}
```

## Example
```kotlin
val networkDispatcher = NetworkDispatcher()

val feature = async2 {
    // create tls smtp client
    val client = SMTPClient.tls(
        dispatcher = networkDispatcher,
        address = NetworkAddress.Immutable("smtp.yandex.ru", 465),
        keyManager = EmptyKeyManager,
        trustManager = TrustManager.TRUST_ALL,// trust to all domains
        fromEmail = "test@example.com",
        login = "test@test.org",
        password = "test_password"
    )

    // simple message with html body
    client.multipart(
        from = "test@example.com",
        fromAlias = "Journal",
        to = "example@gmail.com",
        toAlias = "Anton",
        subject = "Test Message"
    ) {
        it.appendText("text/html").use {
            it.append("<html><s>Second email! Without attachment!</s>")
        }
    }
    
    // message with html body and attachment
    client.multipart(
        from = "test@example.com",
        fromAlias = "Test Binom Client",
        to = "test2@test.org",
        toAlias = "Anton",
        subject = "Test Message"
    ) {
        it.appendText("text/html").use {
            it.append("<html>Hello from <b>Kotln</b><br><br><i>This</i> is an example HTML with attachment!<br><s>Зачёрктнутый</s>")
        }

        it.attach(name = "my_text.txt").use {
            it.write(ByteBuffer.wrap("MyData in TXT file".encodeToByteArray()))
        }
    }

    // close tls smtp client
    client.asyncClose()
}

while (!feature.isDone) {
    networkDispatcher.select()
}
feature.joinAndGetOrThrow()
```