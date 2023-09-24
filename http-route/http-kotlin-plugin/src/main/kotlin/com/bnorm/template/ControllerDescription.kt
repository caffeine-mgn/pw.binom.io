package com.bnorm.template

import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.functions
import pw.binom.url.PathMask
import pw.binom.url.toPathMask

class ControllerDescription(val controllerInterface: IrClass, val methods: List<MethodDescription>) {
  sealed class ArgumentDescription(val value: IrValueParameter) {
    class GetArgument(val name: String, value: IrValueParameter) : ArgumentDescription(value)
    class PathArgument(val name: String, value: IrValueParameter) : ArgumentDescription(value)
    class HeaderArgument(val name: String, value: IrValueParameter) : ArgumentDescription(value)
    class CookieArgument(val name: String, value: IrValueParameter) : ArgumentDescription(value)
    class BodyArgument(value: IrValueParameter) : ArgumentDescription(value)
  }

  data class MethodDescription(
    val method: String,
    val path: PathMask,
    val function: IrSimpleFunction,
    val arguments: List<ArgumentDescription>,
  ) {
    fun getArgs(func: (Int, ArgumentDescription.GetArgument) -> Unit) {
      arguments.forEachIndexed { index, argumentDescription ->
        val arg = argumentDescription as? ArgumentDescription.GetArgument ?: return@forEachIndexed
        func(index, arg)
      }
    }
    fun headerArgs(func: (Int, ArgumentDescription.HeaderArgument) -> Unit) {
      arguments.forEachIndexed { index, argumentDescription ->
        val arg = argumentDescription as? ArgumentDescription.HeaderArgument ?: return@forEachIndexed
        func(index, arg)
      }
    }
    fun cookieArgs(func: (Int, ArgumentDescription.CookieArgument) -> Unit) {
      arguments.forEachIndexed { index, argumentDescription ->
        val arg = argumentDescription as? ArgumentDescription.CookieArgument ?: return@forEachIndexed
        func(index, arg)
      }
    }
  }

  companion object {
    fun create(routeClass: IrClass): ControllerDescription {
      val methods = ArrayList<MethodDescription>()
      routeClass.functions.forEach functionLoop@{ function ->
        val mapping = function.annotations.findAnnotation(BinomContext.mappingFq) ?: return@functionLoop
        val method = mapping.valueArguments[0]?.constString!!
        val path = mapping.valueArguments[1]?.constString!!
        val variables = ArrayList<ArgumentDescription>(function.valueParameters.size)
        function.valueParameters.forEach variableLoop@{ variable ->
          val getParam = variable.annotations.findAnnotation(BinomContext.getParamFq)
          val pathParam = variable.annotations.findAnnotation(BinomContext.pathParamFq)
          val headerParam = variable.annotations.findAnnotation(BinomContext.headerParamFq)
          val cookieParam = variable.annotations.findAnnotation(BinomContext.cookieParamFq)
          val bodyParam = variable.annotations.findAnnotation(BinomContext.bodyParamFq)
          if (getParam != null) {
            val paramName = getParam.valueArguments[0]?.constString?.takeIf { it.isNotEmpty() }
              ?: variable.name.asString()
            variables += ArgumentDescription.GetArgument(
              name = paramName,
              value = variable,
            )
            return@variableLoop
          }
          if (cookieParam != null) {
            val paramName = cookieParam.valueArguments[0]?.constString?.takeIf { it.isNotEmpty() }
              ?: variable.name.asString()
            variables += ArgumentDescription.CookieArgument(
              name = paramName,
              value = variable,
            )
            return@variableLoop
          }
          if (headerParam != null) {
            val paramName = headerParam.valueArguments[0]?.constString?.takeIf { it.isNotEmpty() }
              ?: variable.name.asString()
            variables += ArgumentDescription.HeaderArgument(
              name = paramName,
              value = variable,
            )
            return@variableLoop
          }
          if (pathParam != null) {
            val paramName = pathParam.valueArguments[0]?.constString?.takeIf { it.isNotEmpty() }
              ?: variable.name.asString()
            variables += ArgumentDescription.PathArgument(
              name = paramName,
              value = variable,
            )
            return@variableLoop
          }
          if (bodyParam != null) {
            variables += ArgumentDescription.BodyArgument(
              value = variable,
            )
            return@variableLoop
          }
          TODO("Unknown argument")
        }
        variables.trimToSize()
        methods += MethodDescription(
          function = function,
          arguments = variables,
          method = method,
          path = path.toPathMask(),
        )
      }
      methods.trimToSize()
      return ControllerDescription(controllerInterface = routeClass, methods = methods)
    }
  }
}
