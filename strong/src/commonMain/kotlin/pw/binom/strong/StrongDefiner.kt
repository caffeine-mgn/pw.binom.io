package pw.binom.strong

import pw.binom.strong.exceptions.BeanAlreadyDefinedException

interface StrongDefiner : Strong {
    /**
     * Define [bean]. Default [name] is `[bean]::class + "_" + [bean].class.hashCode()`
     *
     * @param bean object for define
     * @param name name of [bean] for define. See description of method for get default value
     * @param ifNotExist if false on duplicate will throw [BeanAlreadyDefinedException]. If true will ignore redefine
     */
    fun define(bean: Any, name: String = "${bean::class}_${bean::class.hashCode()}", ifNotExist: Boolean = false)
}