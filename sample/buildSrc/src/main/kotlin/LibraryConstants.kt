object LibraryConstants {
    const val toolchain = 8
    const val compileSdk = 34
    const val minSdk = 21
    const val groupId = "com.topstep.wearkit"
}

const val mavenReleaseVersion = "1.0.0"

const val mavenSnapshotVersion = true

val mavenPublishVersion: String
    get() = if (mavenSnapshotVersion) {
        "$mavenReleaseVersion-SNAPSHOT"
    } else {
        mavenReleaseVersion
    }