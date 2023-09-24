package com.bnorm.template

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFieldImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.Name

class RouteClientGenerator(val ctx: BinomContext) {

  context(BinomContext)
  fun ControllerDescription.ArgumentDescription.getSerializer() =
    ctx.serializerFunc.invoke(
      extensionSelf = value.type.getClass()!!.toKClass(),
      type = ctx.kSerializerClass.defaultType,
    )

  private fun generate(
    clazz: IrClass,
    property: IrProperty,
    method: ControllerDescription.MethodDescription,
  ): IrSimpleFunction {
    val bodyArg = method.arguments.indexOfFirst { it is ControllerDescription.ArgumentDescription.BodyArgument }
    val configField = property.backingField!!
    val func = ctx.pluginContext.irFactory.buildFun {
      this.name = method.function.name
      this.returnType = method.function.returnType
      this.isSuspend = method.function.isSuspend
    }
    func.dispatchReceiverParameter = clazz.thisReceiver
    func.overriddenSymbols = listOf(method.function.symbol)

    val args = method.arguments.mapIndexed { index, arg ->
      IrFactoryImpl.createValueParameter(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        origin = IrDeclarationOrigin.DEFINED,
        symbol = IrValueParameterSymbolImpl(),
        name = arg.value.name,
        index = index,
        type = arg.value.type,
        varargElementType = null,
        isCrossinline = false,
        isNoinline = false,
        isHidden = false,
        isAssignable = false
      )
    }

    args.forEach { arg ->
      arg.parent = func
    }
    func.valueParameters = args

    val pathExp = with(ctx) {
      var root: IrExpression = "".irString()
      method.path.splitOnElements(const = {
        root = concatString(root, it.irString())
      }, variable = { variableName ->
        val argIndex =
          method.arguments.indexOfFirst { it is ControllerDescription.ArgumentDescription.PathArgument && it.name == variableName }
        if (argIndex == -1) {
          TODO()
        }
        val arg = method.arguments[argIndex]
        root = concatString(
          root, ctx.encodeValueFunc.invoke(
            type = ctx.pluginContext.irBuiltIns.stringType,
            self = configField.get(),
            types = listOf(arg.value.type),
            args = arrayOf(arg.getSerializer(), args[argIndex].get())
          )
        )
      })
      root
    }
    var urlExp: IrExpression = ctx.createUrlConfigFunc.invoke(
      type = ctx.urlClass.defaultType,
      self = configField.get(),
      args = arrayOf(pathExp)
    )
    method.arguments.forEachIndexed { index, parameter ->
      with(ctx) {
        val p = (parameter as? ControllerDescription.ArgumentDescription.GetArgument) ?: return@forEachIndexed
        val getName = p.name.takeIf { it.isNotEmpty() } ?: p.value.name.identifier
        urlExp = ctx.appendQueryUrlFunc.invoke(
          type = ctx.urlClass.defaultType,
          self = urlExp,
          args = arrayOf(
            getName.irString(),
            ctx.encodeValueFunc.invoke(
              type = ctx.pluginContext.irBuiltIns.stringType,
              self = configField.get(),
              types = listOf(parameter.value.type),
              args = arrayOf(parameter.getSerializer(), args[index].get())
            )
          )
        )
      }
    }
    val connectExp = with(ctx) {
      ctx.connectConfigFunc.invoke(
        type = ctx.httpRequestClass.defaultType,
        self = configField.get(),
        args = arrayOf(method.method.irString(), urlExp)
      )
    }
    ctx.useAsync(value = connectExp, parent = func, resultType = connectExp.type) { requestValue, lambda ->
      with(ctx) {
        method.cookieArgs { index, parameter ->
          +args[index].get().ifNotNull(
            ctx.addCookieFunc.invoke(
              type = requestValue.type,
              extensionSelf = requestValue.get(),
              types = listOf(requestValue.type),
              args = arrayOf(
                parameter.name.irString(),
                ctx.encodeValueFunc.invoke(
                  type = ctx.pluginContext.irBuiltIns.stringType,
                  self = configField.get(),
                  types = listOf(parameter.value.type),
                  args = arrayOf(parameter.getSerializer(), args[index].get())
                )
              )
            )
          )
        }
        method.headerArgs { index, parameter ->
          +requestValue.get().ifNotNull(
            ctx.addHeaderFunc.invoke(
              type = requestValue.type,
              extensionSelf = requestValue.get(),
              types = listOf(requestValue.type),
              args = arrayOf(
                parameter.name.irString(),
                ctx.encodeValueFunc.invoke(
                  type = ctx.pluginContext.irBuiltIns.stringType,
                  self = configField.get(),
                  types = listOf(parameter.value.type),
                  args = arrayOf(parameter.getSerializer(), args[index].get())
                )
              )
            )
          )
        }
        if (bodyArg != -1) {
          args[bodyArg].get().ifNotNull(
            ctx.encodeBodyFunc.invoke(
              type = ctx.pluginContext.irBuiltIns.unitType,
              self = configField.get(),
              types = listOf(method.arguments[bodyArg].value.type),
              args = arrayOf(method.arguments[bodyArg].getSerializer(), args[bodyArg].get(), requestValue.get())
            )
          )
        }

        val responseExp = ctx.getResponseFunc.invoke(
          type = ctx.httpResponseClass.defaultType,
          self = requestValue.get(),
          args = emptyArray(),
        )
        +ctx.useAsync(value = responseExp, parent = func, resultType = func.returnType) { requestValue, lambda ->
          ctx.decodeBodyFunc.invoke(
            type = func.returnType,
            self = configField.get(),
            types = listOf(func.returnType),
            args = arrayOf(func.returnType.getClass()!!.toKClass(), requestValue.get())
          ).doReturn(lambda)
        }.doReturn(func)
      }
    }
    func.body = IrFactoryImpl.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
//      val v = IrVariableImpl(
//        startOffset = UNDEFINED_OFFSET,
//        endOffset = UNDEFINED_OFFSET,
//        origin = IrDeclarationOrigin.DEFINED,
//        symbol = IrVariableSymbolImpl(),
//        name = Name.identifier("path"),
//        type = ctx.pluginContext.irBuiltIns.stringType,
//        isVar = false,
//        isConst = false,
//        isLateinit = false,
//      ).also {
//        it.initializer = urlExp
//        it.parent = func
//      }
      statements += connectExp
      statements += IrConstImpl.constNull(
        type = func.returnType,
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
      ).doReturn(func)
    }
    return func
  }

  fun generate(description: ControllerDescription): IrClass {
    val configArg = IrFactoryImpl.createValueParameter(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      origin = IrDeclarationOrigin.DEFINED,
      symbol = IrValueParameterSymbolImpl(),
      name = Name.identifier("config"),
      index = 0,
      type = ctx.clientRouteConfigClass.defaultType,
      varargElementType = null,
      isCrossinline = false,
      isNoinline = false,
      isHidden = false,
      isAssignable = false,
    )

    val clazz = IrFactoryImpl.buildClass {
      name = Name.identifier("RouteClient")
      kind = ClassKind.CLASS
      modality = Modality.FINAL
    }.also { clazz ->
      clazz.superTypes = listOf(ctx.pluginContext.symbols.any.defaultType, description.controllerInterface.defaultType)
      clazz.createImplicitParameterDeclarationWithWrappedDescriptor()
    }

    val property = clazz.createProperty("config", type = ctx.clientRouteConfigClass.defaultType) { property, getter ->
      property.backingField = IrFieldImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        origin = IrDeclarationOrigin.DEFINED,
        symbol = IrFieldSymbolImpl(),
        name = Name.identifier("config"),
        type = ctx.clientRouteConfigClass.defaultType,
        visibility = DescriptorVisibilities.PRIVATE,
        isFinal = false,
        isExternal = false,
        isStatic = false,
      ).also { field ->
        field.parent = clazz
        field.initializer = IrExpressionBodyImpl(configArg.get())
      }
      statements += property.backingField!!.get()
//        .also { it.receiver = configArg.get() }
        .doReturn(getter)
    }

//    clazz.createProperty("config"){
//
//    }

    val routeClientConstructor = IrFactoryImpl.buildConstructor {
      returnType = clazz.defaultType
      isPrimary = true
    }.also { constructor ->
      configArg.parent = constructor
//      constructor.valueParameters += configArg
      constructor.valueParameters = listOf(configArg)
      constructor.appendTo(clazz)
      constructor.body = IrFactoryImpl.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
        statements += IrDelegatingConstructorCallImpl(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          type = ctx.pluginContext.symbols.any.defaultType,
          symbol = ctx.pluginContext.symbols.any.constructors.single(),
          typeArgumentsCount = 0,
          valueArgumentsCount = 0,
        )
        statements += IrInstanceInitializerCallImpl(
          startOffset = UNDEFINED_OFFSET,
          endOffset = UNDEFINED_OFFSET,
          classSymbol = clazz.symbol,
          type = ctx.pluginContext.symbols.unit.defaultType
        )
      }
    }
    description.methods.forEach {
      val func = generate(
        clazz = clazz,
        method = it,
        property = property,
      )
      func.appendTo(clazz)
    }
    return clazz
  }
}
