package pw.binom.route

import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.name.FqName

@OptIn(FirIncompatiblePluginAPI::class)
class BinomContext(val pluginContext: IrPluginContext) {
  val httpServerExchange =
    HttpServerExchangeClass(pluginContext.referenceClass(FqName(ExternalClasses.HttpServerExchange.name))!!)
  val uri = UriClass(pluginContext.referenceClass(FqName("pw.binom.url.URI"))!!)
  val path = Path(pluginContext.referenceClass(FqName("pw.binom.url.Path"))!!)

  class HttpServerExchangeClass(val sym: IrClassSymbol) : ClassWrapper {
    val getResponseStarted = run {
      Func(sym = sym.getPropertyGetter("responseStarted")!!, origin = IrStatementOrigin.GET_PROPERTY)
    }

    val getRequestURI = Func(sym = sym.getPropertyGetter("requestURI")!!, origin = IrStatementOrigin.GET_PROPERTY)
    val isQueryParamExist = Func(sym = sym.functionByName("isQueryParamExist"), origin = null)
    val getQueryParam = Func(sym = sym.functionByName("getQueryParam"), origin = null)
    val requestMethod = Func(sym = sym.getPropertyGetter("requestMethod")!!, origin = IrStatementOrigin.GET_PROPERTY)
  }

  class UriClass(val sym: IrClassSymbol) : ClassWrapper {
    val getPath = Func(sym = sym.getPropertyGetter("path")!!, origin = IrStatementOrigin.GET_PROPERTY)
  }

  inner class Path(val sym: IrClassSymbol) {
    val isMatch = run {
      Func(
        sym.functions
          .single {
            it.owner.name.toString() == "isMatch" &&
              it.owner.valueParameters.getOrNull(0)?.type == pluginContext.irBuiltIns.stringType
          },
        origin = null,
      )
//      Func(sym = sym.functionByName("isMatch"), origin = null)
    }
  }
}

interface ClassWrapper

class Func(val sym: IrSimpleFunctionSymbol, val origin: IrStatementOrigin?) {
  operator fun invoke(exp: Array<IrExpression> = emptyArray(), receiver: IrExpression? = null): IrCall =
    newIrCall(sym, origin = origin).also {
      it.dispatchReceiver = receiver
      exp.forEachIndexed { index, arg ->
        it.putValueArgument(index = index, valueArgument = arg)
      }
    }
}
