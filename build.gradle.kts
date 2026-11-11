plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "kr.foundcake"
version = "0.0.1-SNAPSHOT"
description = "secu_qna"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

configurations.configureEach {
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    resolutionStrategy.force(
        "org.apache.logging.log4j:log4j-api:2.14.1",
        "org.apache.logging.log4j:log4j-core:2.14.1"
    )
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.apache.logging.log4j:log4j-core:2.14.1")
    testAnnotationProcessor("org.projectlombok:lombok")
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.14.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val verifyLog4jRuntime = tasks.register("verifyLog4jRuntime") {
    group = "verification"
    description = "Checks the isolated audit logger dependency versions and bridge exclusion."
    doLast {
        val modules = configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
            .map { it.moduleVersion.id }
        for (name in listOf("log4j-api", "log4j-core")) {
            check(modules.count { it.group == "org.apache.logging.log4j" && it.name == name && it.version == "2.14.1" } == 1) {
                "$name must resolve exactly once at 2.14.1"
            }
        }
        check(modules.none { it.group == "org.apache.logging.log4j" && it.name == "log4j-to-slf4j" }) {
            "log4j-to-slf4j would divert ticket audit events to Logback"
        }
    }
}

tasks.named("check") {
    dependsOn(verifyLog4jRuntime)
}
