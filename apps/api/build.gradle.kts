plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("org.openapi.generator") version "7.24.0"
}

group = "com.commitquest"
version = "0.3.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
}

openApiValidate {
    inputSpec.set(rootProject.projectDir.resolve("../../docs/api/openapi.yaml").canonicalFile.toURI().toString())
}

openApiGenerate {
    generatorName.set("typescript-angular")
    inputSpec.set(rootProject.projectDir.resolve("../../docs/api/openapi.yaml").canonicalFile.toURI().toString())
    outputDir.set(rootProject.projectDir.resolve("../web/src/app/api/generated").canonicalPath)
    additionalProperties.set(
        mapOf(
            "ngVersion" to "21.2.0",
            "providedInRoot" to "true",
            "serviceSuffix" to "Client",
            "stringEnums" to "true",
            "enumPropertyNaming" to "UPPERCASE"
        )
    )
}

tasks.check {
    dependsOn(tasks.openApiValidate)
}
