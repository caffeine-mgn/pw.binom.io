package pw.binom.jsonrpc

import kotlinx.serialization.KSerializer

abstract class JsonRpcModule {
  private val internalMethods = HashMap<String, JsonRpcMethod<*, *>>()
  private val internalNotifications = HashMap<String, JsonRpcNotification<*>>()
  val methods: Map<String, JsonRpcMethod<*, *>>
    get() = internalMethods

  val notification: Map<String, JsonRpcNotification<*>>
    get() = internalNotifications

  protected fun <REQUEST, RESPONSE> define(
    name: String = jsonRpcRequest.descriptor.serialName,
    description: String? = null,
    jsonRpcRequest: KSerializer<REQUEST>,
    jsonRpcResponse: KSerializer<RESPONSE>,
  ): JsonRpcMethod<REQUEST, RESPONSE> {
    val method = JsonRpcMethod.create(
      name = name,
      description = description,
      jsonRpcRequest = jsonRpcRequest,
      jsonRpcResponse = jsonRpcResponse,
    )
    check(internalMethods.containsKey(name)) { "Method \"$name\" already defined." }
    check(internalNotifications.containsKey(name)) { "Method \"$name\" already defined." }
    internalMethods[name] = method
    return method
  }

  protected fun <REQUEST> define(
    name: String = jsonRpcRequest.descriptor.serialName,
    description: String? = null,
    jsonRpcRequest: KSerializer<REQUEST>,
  ): JsonRpcNotification<REQUEST> {
    val notifications = JsonRpcNotification.create(
      name = name,
      description = description,
      jsonRpcRequest = jsonRpcRequest,
    )
    check(internalMethods.containsKey(name)) { "Method \"$name\" already defined." }
    check(internalNotifications.containsKey(name)) { "Method \"$name\" already defined." }
    internalNotifications[name] = notifications
    return notifications
  }
}
