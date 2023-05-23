pluginManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo.spring.io/milestone/")
        }
        maven {
            url = uri("https://repo.spring.io/snapshot/")
        }
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.springframework.boot") {
                useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "samples-spring-sse-redis"

includeBuild("sample-build-conventions")

include("sample-mvc-imperative")
include("sample-mvc-reactive")
include("sample-webflux")
