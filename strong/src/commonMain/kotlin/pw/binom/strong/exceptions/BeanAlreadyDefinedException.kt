package pw.binom.strong.exceptions

class BeanAlreadyDefinedException(val beanName: String) : StrongException() {
    override val message: String
        get() = "Bean \"$beanName\" already defined"
}