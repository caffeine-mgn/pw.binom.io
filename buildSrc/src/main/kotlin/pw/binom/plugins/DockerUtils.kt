package pw.binom.plugins

import com.bmuschko.gradle.docker.tasks.AbstractDockerRemoteApiTask
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.util.*

class DockerTasks(
    val containerId: String,
    val pull: TaskProvider<DockerPullImage>,
    val create: TaskProvider<DockerCreateContainer>,
    val start: TaskProvider<DockerStartContainer>,
    val stop: TaskProvider<DockerStopContainer>,
    val remove: TaskProvider<DockerRemoveContainer>,
) {
    fun dependsOn(task: Task) {
        task.dependsOn(start)
        task.finalizedBy(stop)
        task.finalizedBy(remove)
    }
}

object DockerUtils {
    fun dockerContanier(
        project: Project,
        image: String,
        tcpPorts: List<Pair<Int, Int>>,
        args: List<String>,
        suffix: String,
        envs: Map<String, String> = emptyMap(),
        healthCheck: String? = null
    ): DockerTasks {
        val containerId = UUID.randomUUID().toString()
        val pullTask =
            project.tasks.register("pull$suffix", com.bmuschko.gradle.docker.tasks.image.DockerPullImage::class.java)
        pullTask.configure {
            it.applyUrl()
            it.image.set(image)
        }

        val createTask = project.tasks.register(
            "create$suffix",
            com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer::class.java
        )
        createTask.configure {
            if (healthCheck != null) {
                it.healthCheck.cmd.add(healthCheck)
            }
            it.applyUrl()
            it.dependsOn(pullTask)
            it.image.set(image)
            it.imageId.set(image)
            it.cmd.addAll(args)
            envs.map { env ->
                it.withEnvVar(env.key, env.value)
            }

            it.hostConfig.portBindings.set(
                tcpPorts.map {
                    "127.0.0.1:${it.second}:${it.first}"
                }
            )
            it.containerId.set(containerId)
            it.containerName.set(containerId)
        }

        val startTask = project.tasks.register(
            "start$suffix",
            com.bmuschko.gradle.docker.tasks.container.DockerStartContainer::class.java
        )
        startTask.configure {
            it.applyUrl()
            it.dependsOn(createTask)
            it.targetContainerId(containerId)
        }

        val stopTask = project.tasks.register(
            "stop$suffix",
            com.bmuschko.gradle.docker.tasks.container.DockerStopContainer::class.java
        )
        stopTask.configure {
            it.applyUrl()
            it.targetContainerId(containerId)
        }

        val removeTask = project.tasks.register(
            "remove$suffix",
            com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer::class.java
        )
        removeTask.configure {
            it.applyUrl()
            it.dependsOn(stopTask)
            it.targetContainerId(containerId)
        }
        return DockerTasks(
            containerId = containerId,
            pull = pullTask,
            create = createTask,
            start = startTask,
            stop = stopTask,
            remove = removeTask,
        )
    }
}

fun AbstractDockerRemoteApiTask.applyUrl() {
    val host = System.getenv("DOCKER_HOST")
    if (host != null) {
        url.set(host)
    }
}
