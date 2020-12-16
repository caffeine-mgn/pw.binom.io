package pw.binom.db.tarantool

class TarantoolException:RuntimeException{
    constructor():super()
    constructor(message:String):super(message)
}