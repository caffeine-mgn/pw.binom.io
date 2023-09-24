package com.bnorm.template

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextInterface
import org.jetbrains.kotlin.ir.builders.constNull
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrPropertySymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.name.Name

val IrExpression.constString
  get() = (this as IrConst<String>).value

fun <T : IrDeclaration> T.appendTo(parent: IrDeclarationContainer): T {
  this.parent = parent
  parent.declarations += this
  return this
}

fun IrExpression.doReturn(func: IrSimpleFunction) =
  IrReturnImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = func.returnType,
    returnTargetSymbol = func.symbol,
    value = this,
  )

fun IrClass.createProperty(
  name: String,
  type: IrType,
  body: IrBlockBody.(IrProperty, IrSimpleFunction) -> Unit,
): IrProperty {
  val parentProperty =
    this.superTypes.asSequence().mapNotNull {
      it.getClass()?.declarations?.find { it is IrProperty && it.name.identifier == name }
    }.firstOrNull() as IrProperty?
  val property = IrFactoryImpl.createProperty(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    origin = IrDeclarationOrigin.DEFINED,
    symbol = IrPropertySymbolImpl(),
    name = Name.identifier(name),
    visibility = DescriptorVisibilities.PUBLIC,
    modality = Modality.OPEN,
    isVar = false,
    isConst = false,
    isLateinit = false,
    isDelegated = false,
    isExternal = false,
    isExpect = false,
    isFakeOverride = false,
    containerSource = null
  ).also {
    if (parentProperty != null) {
      it.overriddenSymbols = listOf(parentProperty.symbol)
    }
    it.parent = this

    it.getter = IrFactoryImpl.createFunction(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      origin = IrDeclarationOrigin.DEFINED,
      symbol = IrSimpleFunctionSymbolImpl(),
      name = Name.special("<get-$name>"),
      visibility = DescriptorVisibilities.PUBLIC,
      modality = Modality.OPEN,
      returnType = type,
      isInline = false,
      isExternal = false,
      isTailrec = false,
      isSuspend = false,
      isOperator = false,
      isInfix = false,
      isExpect = false,
      isFakeOverride = false,
      containerSource = null,
    ).also { getter1 ->
      if (parentProperty != null) {
        getter1.overriddenSymbols = listOf(parentProperty.getter!!.symbol)
      }

      val getter = IrFactoryImpl.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
        body(it, getter1)
      }

      getter1.parent = this
      getter1.correspondingPropertySymbol = it.symbol
      getter1.dispatchReceiverParameter = this.thisReceiver
      getter1.body = getter
    }
  }
  declarations += property
  return property
}

context (BinomContext)
fun IrExpression.ifNotNull(exp: IrExpression) =
  createIf(
    condition = this eq pluginContext.constNull(UNDEFINED_OFFSET, UNDEFINED_OFFSET),
    then = exp
  )

fun BinomContext.createIf(
  condition: IrExpression,
  then: IrExpression,
) = IrIfThenElseImpl(
  startOffset = UNDEFINED_OFFSET,
  endOffset = UNDEFINED_OFFSET,
  type = pluginContext.irBuiltIns.unitType,
  origin = IrStatementOrigin.IF,
).also {
  it.branches += IrBranchImpl(
    condition = condition,
    result = then,
  )
}

context(BinomContext)
fun String.irString() =
  IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, pluginContext.irBuiltIns.stringType, this)

context(BinomContext)
fun IrClass.toKClass() = this.toKClass(pluginContext)

fun BinomContext.concatString(first: IrExpression, second: IrExpression) =
  pluginContext.irBuiltIns.extensionStringPlus.invoke(
    type = pluginContext.irBuiltIns.stringType,
    self = first,
    second,
  )

fun IrSimpleFunctionSymbol.invoke(
  type: IrType,
  self: IrExpression? = null,
  extensionSelf: IrExpression? = null,
  vararg args: IrExpression,
  types: List<IrType> = emptyList(),
): IrCall {
  val call = IrCallImpl(
    UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this,
    typeArgumentsCount = types.size,
    valueArgumentsCount = args.size,
    origin = IrStatementOrigin.INVOKE
  )
  call.extensionReceiver = extensionSelf
  call.dispatchReceiver = self
  types.forEachIndexed { index, type ->
    call.putTypeArgument(index, type)
  }
  args.forEachIndexed { index, expression ->
    call.putValueArgument(index, expression)
  }
  return call
}

fun IrValueDeclaration.get() = irGet(this)

fun IrField.get(self: IrExpression? = (parent as? IrClass?)?.thisReceiver?.get()) = IrGetFieldImpl(
  startOffset = UNDEFINED_OFFSET,
  endOffset = UNDEFINED_OFFSET,
  symbol = symbol,
  type = this.type,
).also {
  it.receiver = self
}

fun irGet(variable: IrValueDeclaration) = irGet(variable.type, variable.symbol)

fun irGet(type: IrType, variable: IrValueSymbol) =
  IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, variable)

fun newTypeOperator(
  resultType: IrType,
  argument: IrExpression,
  typeOperator: IrTypeOperator,
  typeOperand: IrType,
) =
  IrTypeOperatorCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, resultType, typeOperator, typeOperand, argument)

fun newIrSamConversion(argument: IrExpression, type: IrType) =
  newTypeOperator(type, argument, IrTypeOperator.SAM_CONVERSION, type)

fun newIrCall(
  callee: IrSimpleFunctionSymbol,
  type: IrType = callee.owner.returnType,
  valueArgumentsCount: Int = callee.owner.valueParameters.size,
  typeArgumentsCount: Int = callee.owner.typeParameters.size,
  origin: IrStatementOrigin? = null,
): IrCall =
  IrCallImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = type,
    symbol = callee,
    typeArgumentsCount = typeArgumentsCount,
    valueArgumentsCount = valueArgumentsCount,
    origin = origin,
  )

fun newIrCall(callee: IrSimpleFunctionSymbol, origin: IrStatementOrigin? = null): IrCall =
  newIrCall(callee = callee, type = callee.owner.returnType, origin = origin)

fun IrGeneratorContextInterface.irBoolean(value: Boolean) =
  IrConstImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = irBuiltIns.booleanType,
    kind = IrConstKind.Boolean,
    value = value,
  )

context (BinomContext)
fun IrExpression.not(): IrExpression {
  val not = newIrCall(pluginContext.irBuiltIns.booleanNotSymbol)
  not.also {
    it.dispatchReceiver = this
  }
  return not
}

context (BinomContext)
infix fun IrExpression.eq(second: IrExpression): IrExpression {
  pluginContext.irBuiltIns.eqeqSymbol
  val eqeq = newIrCall(callee = pluginContext.irBuiltIns.eqeqSymbol, valueArgumentsCount = 2)
  eqeq.putValueArgument(0, this)
  eqeq.putValueArgument(1, second)
  return eqeq
}

context (BinomContext)
infix fun IrExpression.or(second: IrExpression): IrWhen =
  IrIfThenElseImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = pluginContext.irBuiltIns.booleanType,
    origin = IrStatementOrigin.OROR,
  ).also {
    it.branches += IrBranchImpl(
      condition = this,
      result = pluginContext.irBoolean(true),
    )
    it.branches += IrBranchImpl(
      condition = second,
      result = pluginContext.irBoolean(true),
    )
    it.branches += IrElseBranchImpl(
      condition = pluginContext.irBoolean(true),
      result = pluginContext.irBoolean(false),
    )
  }

context (BinomContext)
infix fun IrExpression.and(second: IrExpression): IrWhen =
  IrIfThenElseImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = pluginContext.irBuiltIns.booleanType,
    origin = IrStatementOrigin.ANDAND,
  ).also {
    it.branches += IrBranchImpl(
      condition = this,
      result = second,
    )
    it.branches += IrElseBranchImpl(
      condition = pluginContext.irBoolean(true),
      result = pluginContext.irBoolean(false),
    )
  }
