package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.io.http.headersOf
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.useAsync

abstract class AbstractCommonFluxSerialization : FluxSerialization {
  protected abstract fun getSerializationList(): Collection<FluxSerialization>

  protected open fun getDefaultMimeType(): String? = null

  override fun isBodySupported(mimeType: String): Boolean = getSerializationList().any { it.isBodySupported(mimeType) }

  override fun getDefaultMimeType(inputMimeType: String): String {
    getSerializationList().forEach {
      if (it.isBodySupported(inputMimeType)) {
        return it.getDefaultMimeType(inputMimeType)
      }
    }
    throw FluxSerialization.SerializationNotSupported()
  }

  override fun isStringMapSupported(): Boolean = getSerializationList().any { isStringMapSupported() }

  open fun findSupportedSerializer(mimeType: String) = getSerializationList().find { it.isBodySupported(mimeType) }

  open fun getSupportedSerializer(mimeType: String) =
    findSupportedSerializer(mimeType)
      ?: throw FluxSerialization.SerializationNotSupported("Unknown type \"$mimeType\"")

  override fun <T> encodeMap(
    value: T,
    serializer: KSerializer<T>,
    output: MutableMap<String, String>,
  ) {
    getSerializationList().forEach { flexSerializer ->
      if (!flexSerializer.isStringMapSupported()) {
        return@forEach
      }
      try {
        return flexSerializer.encodeMap(
          serializer = serializer,
          output = output,
          value = value,
        )
      } catch (e: FluxSerialization.SerializationNotSupported) {
        // Do nothing
      }
    }
    throw FluxSerialization.SerializationNotSupported()
  }

  override fun <T> decodeMap(
    serializer: KSerializer<T>,
    input: Map<String, String>,
  ): T {
    getSerializationList().forEach { flexSerializer ->
      if (!flexSerializer.isStringMapSupported()) {
        return@forEach
      }
      try {
        return flexSerializer.decodeMap(
          serializer = serializer,
          input = input,
        )
      } catch (e: FluxSerialization.SerializationNotSupported) {
        // Do nothing
      }
    }
    throw FluxSerialization.SerializationNotSupported()
  }

  override suspend fun <T> encodeBody(
    mimeType: String,
    value: T,
    serializer: KSerializer<T>,
    output: AsyncOutput,
  ) {
    getSerializationList().forEach { flexSerializer ->
      if (!flexSerializer.isBodySupported(mimeType)) {
        return@forEach
      }
      try {
        return flexSerializer.encodeBody(
          mimeType = mimeType,
          serializer = serializer,
          output = output,
          value = value,
        )
      } catch (e: FluxSerialization.SerializationNotSupported) {
        // Do nothing
      }
    }
    throw FluxSerialization.SerializationNotSupported(mimeType)
  }

  override suspend fun <T> decodeBody(
    mimeType: String,
    serializer: KSerializer<T>,
    input: AsyncInput,
  ): T {
    getSerializationList().forEach { flexSerializer ->
      if (!flexSerializer.isBodySupported(mimeType)) {
        return@forEach
      }
      try {
        return flexSerializer.decodeBody(
          mimeType = mimeType,
          serializer = serializer,
          input = input,
        )
      } catch (e: FluxSerialization.SerializationNotSupported) {
        // Do nothing
      }
    }
    throw FluxSerialization.SerializationNotSupported(mimeType)
  }

  suspend fun <T> read(
    exchange: HttpServerExchange,
    serializer: KSerializer<T>,
  ): T {
    val type =
      exchange.requestHeaders.mimeType?.lowercase() ?: getDefaultMimeType()
        ?: throw FluxSerialization.SerializationNotSupported()
    type.split(',').forEach {
      val mime = it.trim()
      val serializerModule = getSupportedSerializer(mime)
      return serializerModule.decodeBody(
        mimeType = mime,
        serializer = serializer,
        input = exchange.input,
      )
    }
    throw FluxSerialization.SerializationNotSupported()
  }

  private suspend fun <T> response(
    status: Int = 200,
    headers: Headers = emptyHeaders(),
    exchange: HttpServerExchange,
    value: T,
    serializer: KSerializer<T>,
    serializerModule: FluxSerialization,
    mime: String,
  ) {
    val resultMemType = serializerModule.getDefaultMimeType(mime)
    val headerForSend =
      if (headers.isEmpty()) {
        headersOf(Headers.CONTENT_TYPE to resultMemType)
      } else {
        val newHeaders = HashHeaders2()
        newHeaders.add(headers)
        newHeaders[Headers.CONTENT_TYPE] = resultMemType
        newHeaders
      }
    exchange.startResponse(statusCode = status, headerForSend)
    exchange.output.useAsync { output ->
      serializerModule.encodeBody(
        mimeType = resultMemType,
        value = value,
        serializer = serializer,
        output = output,
      )
    }
  }

  suspend fun <T> response(
    status: Int = 200,
    headers: Headers = emptyHeaders(),
    exchange: HttpServerExchange,
    value: T,
    serializer: KSerializer<T>,
  ) {
    exchange.requestHeaders.getSingleOrNull(Headers.ACCEPT)?.lowercase()?.split(',')?.forEach {
      val mime = it.trim()
      val serializerModule = findSupportedSerializer(mime) ?: return@forEach
      response(
        status = status,
        headers = headers,
        exchange = exchange,
        value = value,
        serializer = serializer,
        serializerModule = serializerModule,
        mime = mime,
      )
    }
    val defaultMimeType =
      getDefaultMimeType() ?: throw FluxSerialization.SerializationNotSupported("No default mimetype")
    val serializerModule = getSupportedSerializer(defaultMimeType)
    response(
      status = status,
      headers = headers,
      exchange = exchange,
      value = value,
      serializer = serializer,
      serializerModule = serializerModule,
      mime = defaultMimeType,
    )
  }
}
