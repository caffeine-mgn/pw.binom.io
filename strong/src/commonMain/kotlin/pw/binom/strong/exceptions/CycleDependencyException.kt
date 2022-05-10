package pw.binom.strong.exceptions

import pw.binom.strong.BeanDescription

class CycleDependencyException(val dependenciesPath: List<BeanDescription>) : StrongException() {
    override val message: String?
        get() {
            val sb = StringBuilder()
            dependenciesPath.forEachIndexed { index, beanDescription ->
                if (index > 0) {
                    sb.append(" depends ")
                }
                sb.append(beanDescription.name)
            }
            return sb.toString()
        }
}
