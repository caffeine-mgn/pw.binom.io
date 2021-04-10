package pw.binom.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class DocsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(org.jetbrains.dokka.gradle.DokkaPlugin::class.java)
    }
}