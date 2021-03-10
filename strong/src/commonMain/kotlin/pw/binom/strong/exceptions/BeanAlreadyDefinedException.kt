package pw.binom.strong.exceptions

class BeanAlreadyDefinedException(val beanName: String) : RuntimeException() {
    override val message: String
        get() = "Bean \"$beanName\" already defined"
}