package pw.binom

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

fun TaskContainer.eachKotlinTest(func: (Task) -> Unit) {
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.tasks.KotlinTest }
        .forEach(func)
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest }
        .forEach(func)
}

fun TaskContainer.eachKotlinCompile(func: (Task) -> Unit) {
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile<*> }
        .forEach(func)
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.tasks.AbstractKotlinNativeCompile<*, *> }
        .forEach(func)
}