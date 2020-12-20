package pw.binom.krpc

class Struct(val name: String, val extends: String?) {
    val fields = ArrayList<Definition>()
}

class Definition(val name: String, val type: Type)

class Interface(val name: String) {
    val methods = ArrayList<Method>()
}

class Method(val name: String, val result: Type?) {
    val params = ArrayList<Definition>()
}