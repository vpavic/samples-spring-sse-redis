rootProject.name = "plugins"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }

    versionCatalogs.create("libs") {
        from(files("../libs.versions.toml"))
    }
}

include("conventions")
