package pw.binom.io.db.kmigrator

class KMigrationException : RuntimeException {
    constructor(stepId: String) : super() {
        this.stepId = stepId
    }

    constructor(stepId: String, message: String?) : super(message) {
        this.stepId = stepId
    }

    constructor(stepId: String, message: String?, cause: Throwable?) : super(message, cause) {
        this.stepId = stepId
    }

    constructor(stepId: String, cause: Throwable?) : super(cause) {
        this.stepId = stepId
    }

    val stepId: String

    override val message: String
        get() = "Exception on step $stepId${super.message?.let { ": $it" } ?: ""}"
}
