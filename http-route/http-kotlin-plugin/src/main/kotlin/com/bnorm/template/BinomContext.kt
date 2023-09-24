package com.bnorm.template

import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(FirIncompatiblePluginAPI::class)
class BinomContext(val pluginContext: IrPluginContext) {
  companion object {
    val mappingFq = FqName("pw.binom.io.http.annotations.Mapping")
    val getParamFq = FqName("pw.binom.io.http.annotations.GetParam")
    val pathParamFq = FqName("pw.binom.io.http.annotations.PathParam")
    val headerParamFq = FqName("pw.binom.io.http.annotations.HeaderParam")
    val bodyParamFq = FqName("pw.binom.io.http.annotations.BodyParam")
    val cookieParamFq = FqName("pw.binom.io.http.annotations.CookieParam")
  }

  val clientRouteConfigClass by lazy {
    pluginContext.referenceClass(FqName("pw.binom.io.http.ClientRouteConfig"))!!
  }
  val routeFactoryClass by lazy {
    pluginContext.referenceClass(FqName("pw.binom.io.http.RouteFactory"))!!
  }
  val httpRequestClass by lazy {
    pluginContext.referenceClass(FqName("pw.binom.io.httpClient.HttpRequest"))!!
  }
  val urlClass by lazy {
    pluginContext.referenceClass(FqName("pw.binom.url.URL"))!!
  }
  val createUrlConfigFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.http.ClientRouteConfig.createUrl")).single()
  }
  val connectConfigFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.http.ClientRouteConfig.connect")).single()
  }
  val appendQueryUrlFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.url.URL.appendQuery"))
      .asSequence()
      .filter {
        val o = it.owner
        val l = it.owner.valueParameters
        println("$o $l")
        it.owner.valueParameters.size == 2
          && it.owner.valueParameters[0].type.classFqName == pluginContext.irBuiltIns.stringType.classFqName
          && it.owner.valueParameters[1].type.classFqName == pluginContext.irBuiltIns.stringType.classFqName
      }
      .single()
  }
  val kSerializerClass by lazy {
    pluginContext.referenceClass(FqName("kotlinx.serialization.KSerializer"))!!
  }
  val httpResponseClass by lazy {
    pluginContext.referenceClass(FqName("pw.binom.io.httpClient.HttpResponse"))!!
  }
  val stringPlus by lazy {
    pluginContext.referenceFunctions(FqName("kotlin.String.plus")).single()
  }
  val encodeValueFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.http.ClientRouteConfig.encodeValue")).single()
  }
  val encodeBodyFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.http.ClientRouteConfig.encodeBody")).single()
  }
  val decodeBodyFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.http.ClientRouteConfig.decodeBody")).single()
  }
  val getResponseFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.httpClient.HttpRequest.getResponse")).single()
  }
  val addHeaderFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.httpClient.addHeader")).single()
  }
  val addCookieFunc by lazy {
    pluginContext.referenceFunctions(FqName("pw.binom.io.httpClient.addCookie")).single()
  }
  val serializerFunc by lazy {
    pluginContext.referenceFunctions(FqName("kotlinx.serialization.serializer"))
      .asSequence()
      .filter {
        it.owner.extensionReceiverParameter?.type?.classFqName?.asString() == "kotlin.reflect.KClass" &&
          it.owner.valueParameters.isEmpty()
      }
      .single()
  }

  fun <T : IrExpression> useAsync(
    value: T,
    parent: IrDeclarationParent,
    resultType: IrType,
    body: IrBlockBodyBuilder.(IrValueParameter, lambda: IrSimpleFunction) -> Unit,
  ): IrTypeOperatorCallImpl {
    val beanAllocFunction = pluginContext.irFactory.createFunction(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
      returnType = pluginContext.symbols.any.defaultType,
      isExpect = false,
      isExternal = false,
      isInfix = false,
      isInline = false,
      isOperator = false,
      isSuspend = false,
      isTailrec = false,
      modality = Modality.FINAL,
      name = Name.special("<anonymous>"),
      visibility = DescriptorVisibilities.LOCAL,
      symbol = IrSimpleFunctionSymbolImpl(),
    ).also { lambda ->
      val valueInLambda = lambda.addValueParameter(name = "it", type = value.type)
      lambda.parent = parent
      lambda.body = DeclarationIrBuilder(pluginContext, lambda.symbol).irBlockBody {
        body(this, valueInLambda, lambda)
      }
    }
    val lambdaType = pluginContext.symbols.functionN(1).typeWith(value.type, resultType)
    val exp = IrFunctionExpressionImpl(
      UNDEFINED_OFFSET,
      UNDEFINED_OFFSET,
      lambdaType,
      beanAllocFunction,
      IrStatementOrigin.LAMBDA,
    )
    return newIrSamConversion(type = resultType, argument = exp)
  }
}

