import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("sample.java-convention")
    alias(libs.plugins.spring.boot)
}

dependencies {
    modules {
        module("io.lettuce:lettuce-core") {
            replacedBy("redis.clients:jedis")
        }
    }

    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.jedis)

    testImplementation(libs.spring.boot.starter.test)
}
