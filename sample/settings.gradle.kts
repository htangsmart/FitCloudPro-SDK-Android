pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon
        maven {
            url = uri("https://maven.topstepht.com/repository/maven-public/")
        }
        //sdk-fitcloud中com.github.artillerymans.Core:paycertification:leadingSmart_1.0.50依赖需要此配置
        maven {
            credentials {
                username = "5ff28ca9ed01613630f9d551"
                password = "cxN-HwJ]yzST"
            }
            url= uri("https://packages.aliyun.com/6718aa5c2c78927f26d82a35/maven/mltcloudai")
        }
        flatDir {
            dir("libs")
        }
    }
}

rootProject.name = "sample"
include(":app")

if (!isDeveloperEnvironment()) {//Developers ignored this
    include(":sdk-base")
    include(":sdk-fitcloud")
    project(":sdk-base").projectDir = file("../../sdk-base")
    project(":sdk-fitcloud").projectDir = file("../../sdk-fitcloud")
}

/**
 * Developers and authors may use different dependencies
 */
fun isDeveloperEnvironment(): Boolean {
    return !rootProject.projectDir.path.toString().contains("android-sdk-wearkit")
}