package pw.binom.route

import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextInterface
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.descriptors.IrAbstractDescriptorBasedFunctionFactory
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(FirIncompatiblePluginAPI::class)
fun findOrCreateHandleFunction(pluginContext: IrPluginContext, clazz: IrClass): IrFunction {
  val handleFunction = clazz.declarations.asSequence()
    .mapNotNull { it as IrFunction }
    .filter { it.name.toString() == "handle" }
    .filter {
      it.valueParameters.size == 1 &&
        it.valueParameters[0].type.classFqName?.toString() == ExternalClasses.HttpServerExchange.name
    }
    .firstOrNull()
  if (handleFunction != null) {
    return handleFunction
  }
  val type = pluginContext.referenceClass(FqName(ExternalClasses.HttpServerExchange.name))!!.defaultType
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
    name = Name.identifier("handle"),
    visibility = DescriptorVisibilities.LOCAL,
    symbol = IrSimpleFunctionSymbolImpl(),
  )

  beanAllocFunction.addValueParameter(name = "exchange", type = type)
  return beanAllocFunction
}

fun newIrCall(
  callee: IrSimpleFunctionSymbol,
  type: IrType,
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

fun IrGeneratorContextInterface.irString(value: String) =
  IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.stringType, value)

fun IrExpression.not(pluginContext: IrPluginContext): IrExpression {
  val not = newIrCall(pluginContext.irBuiltIns.booleanNotSymbol)
  not.also {
    it.dispatchReceiver = this
  }
  return not
}

fun createNot(pluginContext: IrPluginContext, e: IrExpression): IrCall {
  val not = newIrCall(pluginContext.irBuiltIns.booleanNotSymbol)
  not.also {
    it.dispatchReceiver = e
  }
  return not
}

fun IrPluginContext.createAnd(first: IrExpression, second: IrExpression): IrWhen =
  IrIfThenElseImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = this.irBuiltIns.booleanType,
    origin = IrStatementOrigin.ANDAND,
  ).also {
    it.branches += IrBranchImpl(
      condition = first,
      result = second,
    )
    it.branches += IrElseBranchImpl(
      condition = this.irBoolean(true),
      result = this.irBoolean(false),
    )
  }

fun IrPluginContext.createIf(
  condition: IrExpression,
  then: IrExpression,
) {
  IrIfThenElseImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = this.irBuiltIns.unitType,
    origin = IrStatementOrigin.IF,
  ).also {
    it.branches += IrBranchImpl(
      condition = condition,
      result = then,
    )
  }
}

fun IrPluginContext.checkNotNull(exp: IrExpression) =
  newIrCall(irBuiltIns.checkNotNullSymbol, origin = IrStatementOrigin.EXCLEXCL).also {
    it.putValueArgument(0, exp)
  }

fun IrPluginContext.createIf(
  condition: IrExpression,
  then: IrExpression,
  other: IrExpression,
) =
  IrIfThenElseImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = this.irBuiltIns.unitType,
    origin = IrStatementOrigin.IF,
  ).also {
    it.branches += IrBranchImpl(
      condition = condition,
      result = then,
    )
    it.branches += IrBranchImpl(
      condition = irBoolean(true),
      result = other,
    )
  }

fun IrPluginContext.createOr(first: IrExpression, second: IrExpression): IrWhen =
  IrIfThenElseImpl(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = this.irBuiltIns.booleanType,
    origin = IrStatementOrigin.OROR,
  ).also {
    it.branches += IrBranchImpl(
      condition = first,
      result = this.irBoolean(true),
    )
    it.branches += IrBranchImpl(
      condition = second,
      result = this.irBoolean(true),
    )
    it.branches += IrElseBranchImpl(
      condition = this.irBoolean(true),
      result = this.irBoolean(false),
    )
  }

/*
fun generateBeanRegistry(
  define: ComponentDefine,
  beanDefinitionClass: IrClassSymbol,
  strongClass: IrClassSymbol,
  defineBeanFunction: IrSimpleFunctionSymbol,
  lambdaType: IrType,
  pluginContext: IrPluginContext,
): IrCall {
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
  ).also {
    it.addValueParameter(name = "it", type = strongClass.defaultType)
    it.parent = define.configClass
    it.body = DeclarationIrBuilder(pluginContext, it.symbol).irBlockBody {
      +irReturn(irCallConstructor(define.beanClassReference.defaultConstructor?.symbol ?: TODO(), emptyList()))
    }
  }

  val exp = IrFunctionExpressionImpl(
    UNDEFINED_OFFSET,
    UNDEFINED_OFFSET,
    lambdaType,
    beanAllocFunction,
    IrStatementOrigin.LAMBDA,
  )
  val sam = newIrSamConversion(type = beanDefinitionClass.defaultType, argument = exp)
  return newIrCall(defineBeanFunction).also {
    it.type = pluginContext.symbols.unit.defaultType
    it.putValueArgument(0, pluginContext.irString(define.beanClassReference.kotlinFqName.toString()))
    it.putValueArgument(1, define.beanClassReference.toKClass(pluginContext))
    it.putValueArgument(2, pluginContext.irBoolean(define.primary))
    it.putValueArgument(3, define.name?.let { pluginContext.irString(define.name) } ?: pluginContext.irNull())
    it.putValueArgument(4, pluginContext.irBoolean(define.ifNotExist))
    it.putValueArgument(5, sam)
    it.dispatchReceiver = irGet(define.configClass.thisReceiver!!)
  }
}
*/
fun IrGeneratorContextInterface.irNull() =
  irNull(irBuiltIns.nothingNType)

fun irGet(variable: IrValueDeclaration) = irGet(variable.type, variable.symbol)

fun irGet(type: IrType, variable: IrValueSymbol) =
  IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, variable)

fun irNull(irType: IrType) =
  IrConstImpl.constNull(UNDEFINED_OFFSET, UNDEFINED_OFFSET, irType)

fun newIrSamConversion(argument: IrExpression, type: IrType) =
  newTypeOperator(type, argument, IrTypeOperator.SAM_CONVERSION, type)

fun newTypeOperator(
  resultType: IrType,
  argument: IrExpression,
  typeOperator: IrTypeOperator,
  typeOperand: IrType,
) =
  IrTypeOperatorCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, resultType, typeOperator, typeOperand, argument)

fun IrClass.toKClass(generatorContext: IrGeneratorContext) =
  IrClassReferenceImpl(
    UNDEFINED_OFFSET,
    UNDEFINED_OFFSET,
    generatorContext.irBuiltIns.kClassClass.starProjectedType,
    this.symbol,
    this.defaultType,
  )

fun IrType.toKClass(generatorContext: IrGeneratorContext) =
  this.getClass()!!.toKClass(generatorContext)

fun IrClassSymbol.toKClass(generatorContext: IrGeneratorContext) =
  IrClassReferenceImpl(
    UNDEFINED_OFFSET,
    UNDEFINED_OFFSET,
    generatorContext.irBuiltIns.kClassClass.starProjectedType,
    this,
    this.defaultType,
  )

fun IrClass.addInitBlock(
  generatorContext: IrGeneratorContext,
  static: Boolean = false,
  body: IrBlockBodyBuilder.() -> Unit,
) {
  val initBlock = generatorContext.irFactory.createAnonymousInitializer(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    origin = IrAbstractDescriptorBasedFunctionFactory.classOrigin,
    symbol = IrAnonymousInitializerSymbolImpl(this.symbol),
    isStatic = static,
  ).also {
    it.parent = this
  }
  this.declarations += initBlock
  initBlock.body = DeclarationIrBuilder(generatorContext, initBlock.symbol).irBlockBody {
    body()
  }
}

@OptIn(FirIncompatiblePluginAPI::class)
fun IrPluginContext.getClassReference(name: String): IrClassReference {
  val clazz = referenceClass(FqName(name))
    ?: throw IllegalArgumentException("Can't find class \"$name\"")
  return clazz.toKClass(this)
}
