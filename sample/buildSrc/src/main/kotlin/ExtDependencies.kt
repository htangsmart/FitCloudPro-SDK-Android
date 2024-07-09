import groovy.util.Node
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.publish.maven.MavenPublication

fun MavenPublication.mavenPublishDependencies(
    configurations: ConfigurationContainer,
    includes: Set<String>? = null,
    excludes: Set<String>? = null,
) {
    //TODO
}

private fun Set<String>?.toDependencies(): Set<Dependency>? {
    TODO()
}

private fun appendDependency(dependenciesNode: Node, dependency: Dependency): Boolean {
    TODO()
}