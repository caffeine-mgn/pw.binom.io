@file:Suppress("DEPRECATION_ERROR")

package pw.binom.http.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind

import pw.binom.bitarray.BitArray64
import pw.binom.http.rest.annotations.*
import pw.binom.url.PathMask
import pw.binom.url.toPathMask

class EndpointDescription<T> private constructor(
  val method: String,
  val path: PathMask,
  val nameByIndex: Map<Int, String>,
  val pathParamNames: Map<String, Int>,
  val headerParamNames: Map<String, Int>,
  val getParamNames: Map<String, Int>,
  val pathParam: BitArray64,
  val headerParam: BitArray64,
  val getParam: BitArray64,
  val serializer: KSerializer<T>,
  val bodyIndex: Int,
  val responseCodeIndex: Int,
) {
  val bodyDescription = if (bodyIndex == -1) null else serializer.descriptor.getElementDescriptor(bodyIndex)

  companion object {
    @Suppress("UNCHECKED_CAST")
    fun <T> create(serializer: KSerializer<T>): EndpointDescription<T> {
      val endpoint = serializer.descriptor.annotations.find { it is EndpointMapping } as EndpointMapping?
        ?: TODO("${serializer.descriptor.serialName} is not endpoint")
      val nameByIndex = HashMap<Int, String>()
      val pathParamNames = HashMap<String, Int>()
      val headerParamNames = HashMap<String, Int>()
      val getParamNames = HashMap<String, Int>()
      var pathParam = BitArray64()
      var headerParam = BitArray64()
      var getParam = BitArray64()
      var bodyIndex = -1
      var responseCodeIndex = -1
      for (i in 0 until serializer.descriptor.elementsCount) {
        val itemAnnotations = serializer.descriptor.getElementAnnotations(i)
        val path = itemAnnotations.find { it is PathParam } as PathParam?
        val header = itemAnnotations.find { it is HeaderParam } as HeaderParam?
        val get = itemAnnotations.find { it is GetParam } as GetParam?
        val bodyExist = itemAnnotations.find { it is BodyParam } != null
        val responseCodeExist = itemAnnotations.find { it is ResponseCode } != null
        if (bodyExist) {
          if (bodyIndex != -1) {
            throw IllegalArgumentException("Only one field can be marked as @BodyParam")
          }
          bodyIndex = i
        }
        if (responseCodeExist) {
          if (responseCodeIndex != -1) {
            throw IllegalArgumentException("Only one field can be marked as @ResponseCode")
          }
          when (serializer.descriptor.kind) {
            PrimitiveKind.STRING,
            PrimitiveKind.INT,
            PrimitiveKind.LONG,
            PrimitiveKind.SHORT,
            -> responseCodeIndex = i

            else -> throw IllegalArgumentException("Can't use ${serializer.descriptor.kind} is ResponseCode")
          }
        }
        if (path != null) {
          val pathName = path.name.ifEmpty { serializer.descriptor.getElementName(i) }
          pathParamNames[pathName] = i
          nameByIndex[i] = pathName
          pathParam = pathParam.update(i, true)
        }
        if (header != null) {
          val headerName = header.name.ifEmpty { serializer.descriptor.getElementName(i) }
          headerParamNames[headerName] = i
          nameByIndex[i] = headerName
          headerParam = headerParam.update(i, true)
        }
        if (get != null) {
          val headerName = get.name.ifEmpty { serializer.descriptor.getElementName(i) }
          getParamNames[headerName] = i
          nameByIndex[i] = headerName
          getParam = getParam.update(i, true)
        }
      }
      return EndpointDescription(
        method = endpoint.method,
        path = endpoint.path.toPathMask(),
        nameByIndex = nameByIndex,
        pathParamNames = pathParamNames,
        headerParamNames = headerParamNames,
        getParamNames = getParamNames,
        pathParam = pathParam,
        headerParam = headerParam,
        getParam = getParam,
        serializer = serializer,
        bodyIndex = bodyIndex,
        responseCodeIndex = responseCodeIndex,
      )
    }
  }
}
