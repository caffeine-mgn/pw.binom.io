package pw.binom.route

import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.classFqName

abstract class ExternalClasses(val name: String) {
  object HttpHandler : ExternalClasses("pw.binom.io.httpServer.HttpHandler") {
    const val HANDLE = "handle"
  }

  object Mapping : ExternalClasses("pw.binom.io.httpServer.annotations.Mapping")
  object HttpServerExchange : ExternalClasses("pw.binom.io.httpServer.HttpServerExchange")
  object PathVariable : ExternalClasses("pw.binom.io.httpServer.annotations.PathVariable")
  object RequestBody : ExternalClasses("pw.binom.io.httpServer.annotations.RequestBody")
  object QueryVariable : ExternalClasses("pw.binom.io.httpServer.annotations.QueryVariable")
  object Context : ExternalClasses("pw.binom.io.httpServer.annotations.Context")
  object Encode : ExternalClasses("pw.binom.io.httpServer.annotations.Encode")
}

fun IrAnnotationContainer.findAnnotation(clazz: ExternalClasses) =
  annotations.find { it.type.classFqName?.asString() == clazz.name }

fun IrConstructorCall.getString(index: Int) =
  (valueArguments[index] as IrConst<String>?)?.value

fun IrConstructorCall.getClass(index: Int) =
  (valueArguments[index] as IrClassReference)

fun IrClassReference.getClass() = symbol.owner as IrClass
