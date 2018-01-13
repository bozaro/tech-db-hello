import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val springBootVersion by extra { "1.5.9.RELEASE" }
    extra["kotlinVersion"] = "1.2.10"

    repositories {
        jcenter()
        maven { setUrl("http://repo.spring.io/snapshot") }
        maven { setUrl("http://repo.spring.io/milestone") }
    }

    dependencies {
        classpath("io.spring.gradle:dependency-management-plugin:1.0.4.RELEASE")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra["kotlinVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${extra["kotlinVersion"]}")
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.2.10"
    id("org.detoeuf.swagger-codegen") version "1.6.3"
}

apply {
    plugin("java")
    plugin("idea")
    plugin("kotlin")
    plugin("kotlin-spring")
    plugin("org.springframework.boot")
}


springBoot {
    mainClass = "ru.lionzxy.techDb.service.ApplicationKt"
}


swagger {
    inputSpec = "../common/swagger.yml"

    output = "build/swagger"
    language = "spring"
    additionalProperties = mapOf("modelPackage" to "ru.lionzxy.techDb.hello.model",
            "apiPackage" to "ru.lionzxy.techDb.hello.api",
            "serializableModel" to "true",
            "dateLibrary" to "kotlin")
}

tasks {
    "compileJava" {
        dependsOn("swagger")
    }
    "compileKotlin" {
        dependsOn("swagger")
    }
}

repositories {
    jcenter()

    maven { setUrl("http://repo.spring.io/snapshot") }
    maven { setUrl("http://repo.spring.io/milestone") }
}

java.sourceSets["main"].java {
    srcDir("${project.buildDir.path}/swagger/src/main/java")
}

val kotlinVersion: String by project.extra

dependencies {
    swaggerCompile("org.springframework.boot:spring-boot-starter-web")
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    compile("org.flywaydb:flyway-core")
    compile("io.springfox:springfox-swagger-ui:2.7.0")
    compile("io.springfox:springfox-swagger2:2.7.0")
    compile("org.springframework.boot:spring-boot-starter-web-services")
    compile("io.swagger:swagger-annotations:1.5.8")
    compile("joda-time:joda-time:2.9.9")
    compile("org.springframework.boot:spring-boot-starter-jdbc") {
        exclude(group = "org.apache.tomcat", module = "tomcat-jdbc")
    }

    compile("com.zaxxer:HikariCP:2.7.2")
    compile("org.postgresql:postgresql")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.8.10")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

fun Project.springBoot(configure: org.springframework.boot.gradle.SpringBootPluginExtension.() -> Unit = {}) =
        extensions.getByName<org.springframework.boot.gradle.SpringBootPluginExtension>("springBoot").apply { configure() }