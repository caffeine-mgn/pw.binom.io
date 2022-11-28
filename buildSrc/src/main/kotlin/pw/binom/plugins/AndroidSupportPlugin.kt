package pw.binom.plugins

import com.android.build.gradle.internal.tasks.factory.registerTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class AndroidSupportPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val generateTask = target.tasks.registerTask("GenerateManifest", GenerateManifestTask::class.java)
        generateTask.configure {
            it.manifestFile.set(target.buildDir.resolve("androidManifest/AndroidManifest.xml"))
        }
        target.tasks.getByName("preBuild").dependsOn(generateTask)
        val android = target.extensions.getByType(com.android.build.gradle.LibraryExtension::class.java)
        android.apply {
            compileSdkVersion("android-30")
            buildToolsVersion("30.0.2")

            defaultConfig {
                it.testInstrumentationRunner("android.support.test.runner.AndroidJUnitRunner")
                it.minSdkVersion(28)
                it.targetSdkVersion(30)
//            versionCode(1)
//            versionName("1.0.0")
            }
            sourceSets {
                getByName("main") {
                    it.manifest.srcFile(generateTask.get().manifestFile)
                }
            }
        }
    }
}

abstract class GenerateManifestTask : DefaultTask() {

    @get:OutputFile
    abstract val manifestFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val manifestFile = manifestFile.get().asFile
        manifestFile.parentFile.mkdirs()
        val projectName = project.name.replace('-', '_')
        manifestFile.writeText(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest package=\"pw.binom.io.$projectName\" xmlns:android=\"http://schemas.android.com/apk/res/android\">" +
                """
                       <uses-permission
        android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
        android:minSdkVersion="30" /> 
                    """ +
                "</manifest>"
        )
    }
}
