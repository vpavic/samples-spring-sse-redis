import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("sample.java-convention")
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.data.redis.reactive)

    testImplementation(libs.spring.boot.starter.test)
}
