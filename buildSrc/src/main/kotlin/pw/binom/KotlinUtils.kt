package pw.binom

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginAware
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.getKotlin() = extensions.getByType(KotlinMultiplatformExtension::class.java)

class KotlinConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        (target as PluginAware).apply {
            it.plugin(pw.binom.publish.plugins.RequiresOptInAllowPlugin::class.java)
        }
    }
}
