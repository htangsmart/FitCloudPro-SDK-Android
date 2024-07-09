import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.TaskContainer

fun PublishingExtension.mavenPublishRepositories(isSnapshot: Boolean) {
    //TODO
}

fun Project.myMappingOutputDir(): String {
    TODO()
}

fun Project.myAAROutputDir(): String {
    TODO()
}

fun TaskContainer.registerCopyAssembleRelease(name: String, project: Project) {
    //TODO
}