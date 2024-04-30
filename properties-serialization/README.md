### Example
```kotlin
@Serializable
@PropertiesPrefix("app.auth")
data class Auth(
    val name:String,
    val password:String,
)

@Serializable
@PropertiesPrefix("app.storage")
data class Storage(
    val path:String,
    val lifetime:Duration,
)

fun loadConfig(){
    // parse root value
    val root = IniParser.parseMap(mapOf(/*data*/))
    
    // decode auth properties
    val auth = PropertiesDecoder.parse(
        serializer = Auth.serializer(),
        root = root,
    )

    // decode storage properties
    val storage = PropertiesDecoder.parse(
        serializer = Storage.serializer(),
        root = root,
    )
}
```
