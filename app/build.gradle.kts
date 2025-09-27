plugins {
    id("java")
    id("java-library")
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    api(libs.org.springframework.boot.spring.boot.starter.actuator)
    api(libs.org.springframework.boot.spring.boot.starter.data.mongodb)
    api(libs.org.springframework.boot.spring.boot.starter.validation)
    api(libs.org.springframework.boot.spring.boot.starter.web)
    api(libs.org.apache.httpcomponents.client5.httpclient5)
    api(libs.com.bucket4j.bucket4j.core)
    api(libs.org.projectlombok.lombok)
    api(libs.org.springdoc.springdoc.openapi.starter.webmvc.ui)
    api(libs.org.springdoc.springdoc.openapi.starter.webmvc.api)

    compileOnly(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)

    testCompileOnly(libs.org.projectlombok.lombok)
    testAnnotationProcessor(libs.org.projectlombok.lombok)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testImplementation(libs.de.flapdoodle.embed.de.flapdoodle.embed.mongo.spring30x)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
}

group = "com.dotashowcase"
version = "0.0.1"
description = "inventory-service"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
