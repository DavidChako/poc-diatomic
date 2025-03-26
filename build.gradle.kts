plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.10"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    `maven-publish`
    groovy // Spock
}

group = "com.icosahedron"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context")
    implementation("com.datomic:peer:1.0.7277")
    implementation("ch.qos.logback:logback-classic:1.5.17")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.20")

    val spockVersion = "2.4-M5-groovy-4.0"
    testImplementation(platform("org.spockframework:spock-bom:$spockVersion"))
    testImplementation("org.spockframework:spock-core:$spockVersion")
    testImplementation("org.spockframework:spock-spring:$spockVersion")
    testImplementation("org.springframework:spring-test")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "local"
            url = uri("/Users/icosahedron/.m2/repository") // or mavenLocal() for ~/.m2/repository
        }
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}