package pw.binom.rpc.json

abstract class RemoteService(val name: String) {
    private var invokator: Invokator? = null
    private val methods = HashMap<String, RemoteMethod>()

    fun invokator(invokator: Invokator) {
        this.invokator = invokator
    }

    suspend fun call(method: String, args: Array<JDTO?>): JDTO? {
        val method = methods[method] ?: throw IllegalArgumentException("Can't find method $name.$method")
        return method.invoke(*args)
    }

    fun method(name: String): RemoteMethod {
        require(!methods.containsKey(name)) { "Method \"$name\" already exist in Remote Service \"${this.name}\"" }
        val rm = RemoteMethod(name)
        methods[name] = rm
        return rm
    }

    inner class RemoteMethod(val name: String) {
        private var invokator: (suspend (Array<out JDTO?>) -> JDTO?)? = null

        fun invokator(invokator: suspend (Array<out JDTO?>) -> JDTO?) {
            this.invokator = invokator
        }

        suspend operator fun invoke(vararg args: JDTO?): JDTO? {
            if (invokator != null)
                return invokator!!(args)
            val invokator = requireNotNull(this@RemoteService.invokator) { "Not set invokator for ${this@RemoteService.name}.$name" }
            val response = invokator.call(JRequest(
                    service = this@RemoteService.name,
                    method = name,
                    args = JsonFactory.writeArray(args)
            ))
            if (response.error)
                throw (response.obj!!.dto<JDTO>() as Throwable)
            return response.obj?.dto()
        }
    }

    interface Invokator {
        suspend fun call(request: JRequest): JResponce
    }
}