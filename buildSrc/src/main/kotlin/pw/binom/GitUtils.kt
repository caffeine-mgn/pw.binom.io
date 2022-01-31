package pw.binom

import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.getGitBranch(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        it.commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        it.standardOutput = stdout
    }
    return stdout.toString().trim()
}
