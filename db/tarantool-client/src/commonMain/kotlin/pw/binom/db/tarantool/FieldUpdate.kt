package pw.binom.db.tarantool

class FieldUpdate(val fieldId: Int, val value: Any?, val operator: UpdateOperator)

/**
 * Update operators
 * @see <a href="https://www.tarantool.io/en/doc/latest/reference/reference_lua/box_space/#box-space-update">Documentation</a>
 */
enum class UpdateOperator(val code: String) {
    /**
     * for addition. values must be numeric, e.g. unsigned or decimal
     */
    ADD("+"),

    /**
     * for subtraction. values must be numeric
     */
    MINUS("-"),

    /**
     * for bitwise AND. values must be unsigned numeric
     */
    AND("&"),

    /**
     * for bitwise OR. values must be unsigned numeric
     */
    OR("|"),

    /**
     * for bitwise XOR. values must be unsigned numeric
     */
    XOR("^"),

    /**
     * for string splice.
     */
    SPLIT(":"),

    /**
     * for insertion of a new field
     */
    INSERT("!"),

    /**
     * for deletion
     */
    DELETE("#"),

    /**
     * for assignment
     */
    ASSIGN("="),
}