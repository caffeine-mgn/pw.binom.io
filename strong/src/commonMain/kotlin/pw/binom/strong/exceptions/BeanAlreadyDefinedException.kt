package pw.binom.strong.exceptions

class BeanAlreadyDefinedException : StrongException {
    val beanName: String

    constructor(beanName: String) : super() {
        this.beanName = beanName
    }

    constructor(beanName: String, cause: Throwable?) : super(cause) {
        this.beanName = beanName
    }

    override val message: String
        get() = "Bean \"$beanName\" already defined"
}
