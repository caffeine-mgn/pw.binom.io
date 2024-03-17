package pw.binom

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginAware
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import pw.binom.plugins.AndroidSupportPlugin

fun Project.getKotlin() = extensions.getByType(KotlinMultiplatformExtension::class.java)

class KotlinConfigPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    (target as PluginAware).apply {
      it.plugin(pw.binom.publish.plugins.RequiresOptInAllowPlugin::class.java)
    }

    if (Target.ANDROID_JVM_SUPPORT) {
      target.plugins.apply("com.android.library")
      target.plugins.apply(AndroidSupportPlugin::class.java)
    }
  }
}
