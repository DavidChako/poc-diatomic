plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.10"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    groovy
    jacoco
    `maven-publish`
}

group = "com.icosahedron"
version = "1.0.0"

val jacocoMinimumInstructionCoveredRatio = 1.0
val jacocoMinimumComplexityCoveredRatio = 1.0
val jacocoMaximumClassMissedCount = 0.0

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-context")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    implementation("com.datomic:peer:1.0.7277")
    implementation("org.clojure:clojure") {
        // Fix transitive vulnerability:
        // implementation("com.datomic:peer:1.0.7277")
        // --> maven:org.clojure:clojure:1.10.0 is vulnerable
        version {
            strictly("1.12.0-alpha9")
        }
    }

    val spockVersion = "2.4-M5-groovy-4.0"
    testImplementation(platform("org.spockframework:spock-bom:$spockVersion"))
    testImplementation("org.spockframework:spock-core:$spockVersion")
    testImplementation("org.spockframework:spock-spring:$spockVersion")
    testImplementation("org.springframework:spring-test")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }

    finalizedBy(tasks.jacocoTestReport)
    doLast {
        println("View code coverage at:")
        println("file://$buildDir/reports/jacoco/test/html/index.html")
    }
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)

    violationRules {
        rule {
            isEnabled = true
            element = "PACKAGE"

            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = jacocoMinimumInstructionCoveredRatio.toBigDecimal()
            }

            limit {
                counter = "COMPLEXITY"
                value = "COVEREDRATIO"
                minimum = jacocoMinimumComplexityCoveredRatio.toBigDecimal()
            }

            limit {
                counter = "CLASS"
                value = "MISSEDCOUNT"
                minimum = jacocoMaximumClassMissedCount.toBigDecimal()
            }
        }
    }
}

tasks.jacocoTestReport {
    finalizedBy(tasks.jacocoTestCoverageVerification)
    classDirectories.setFrom(
        files(classDirectories.files.map { file ->
            fileTree(file) {
                exclude(
                    "com/icosahedron/example/*.class",
                    "com/icosahedron/stub/*.class",
                    "com/icosahedron/core/*.class",
                    "com/icosahedron/math/*.class",
                )
            }
        })
    )
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

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
