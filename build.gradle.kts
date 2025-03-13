plugins {
    groovy // Spock
    kotlin("jvm") version "2.1.10"
}

group = "com.icosahedron.poc"
version = "1.0-SNAPSHOT"

val spockVersion = "2.4-M5-groovy-4.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.datomic:peer:1.0.7277")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    testImplementation(platform("org.spockframework:spock-bom:$spockVersion"))
    testImplementation("org.spockframework:spock-core:$spockVersion")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

kotlin {
    jvmToolchain(17)
}