package pw.binom.krpc

sealed class Type {
    abstract val nullable: Boolean

    class Primitive(val type: Type, override val nullable: Boolean) : Type() {
        enum class Type {
            INT,
            LONG,
            BOOL,
            FLOAT,
            STRING,
            STRUCT
        }
    }

    class Array(val type: Type, override val nullable: Boolean) : Type()
    class Struct(val name: String, override val nullable: Boolean) : Type()
}