### Example

```kotlin

val input = PipeInput()
val output = PipeOutput(input)

ByteBuffer.wrap("Hello world".encodeToByteArray()).use { buffer ->
    input.write(buffer)
}
```