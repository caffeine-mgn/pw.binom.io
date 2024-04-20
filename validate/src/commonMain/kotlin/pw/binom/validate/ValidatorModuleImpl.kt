package pw.binom.validate

import kotlin.reflect.KClass

private class ValidatorModuleImpl(
  val valueValidators: Map<KClass<out Annotation>, Validator.FieldValidator<out Annotation>>,
  val objectValidators: Map<KClass<out Annotation>, Validator.ObjectValidator<out Annotation>>,
  val parents: Set<ValidatorModule>,
) : ValidatorModule {
  @Suppress("UNCHECKED_CAST")
  override fun <T : Annotation> findValueValidator(annotation: T): Validator.FieldValidator<T>? =
    (valueValidators.entries.find { it.key.isInstance(annotation) }?.value as Validator.FieldValidator<T>?)
      ?: parents.asSequence().mapNotNull { it.findValueValidator(annotation) }.firstOrNull()

  @Suppress("UNCHECKED_CAST")
  override fun <T : Annotation> findObjectValidator(annotation: T): Validator.ObjectValidator<T>? =
    (objectValidators.entries.find { it.key.isInstance(annotation) }?.value as Validator.ObjectValidator<T>?)
      ?: parents.asSequence().mapNotNull { it.findObjectValidator(annotation) }.firstOrNull()
}

interface ValidatorModuleContext {
  fun <T : Annotation> define(annotation: KClass<T>, validator: Validator.FieldValidator<T>)
  fun <T : Annotation> define(annotation: KClass<T>, validator: Validator.ObjectValidator<T>)
  fun include(module: ValidatorModule)
}

private class ValidatorModuleContextImpl : ValidatorModuleContext {
  val valueValidators = HashMap<KClass<out Annotation>, Validator.FieldValidator<out Annotation>>()
  val objectValidators = HashMap<KClass<out Annotation>, Validator.ObjectValidator<out Annotation>>()
  val parents = HashSet<ValidatorModule>()
  override fun <T : Annotation> define(annotation: KClass<T>, validator: Validator.FieldValidator<T>) {
    valueValidators[annotation] = validator
  }

  override fun <T : Annotation> define(annotation: KClass<T>, validator: Validator.ObjectValidator<T>) {
    objectValidators[annotation] = validator
  }

  override fun include(module: ValidatorModule) {
    parents += module
  }
}

fun ValidatorModule(func: ValidatorModuleContext.() -> Unit): ValidatorModule {
  val ctx = ValidatorModuleContextImpl()
  func(ctx)
  return ValidatorModuleImpl(
    valueValidators = ctx.valueValidators,
    objectValidators = ctx.objectValidators,
    parents = ctx.parents,
  )
}
