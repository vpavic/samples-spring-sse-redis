import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("sample.java-conventions")
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.redis.reactive)

    testImplementation(libs.spring.boot.starter.test)
}
