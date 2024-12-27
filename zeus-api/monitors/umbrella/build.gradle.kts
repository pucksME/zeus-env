plugins {
    id("java")
    id("com.gradleup.shadow") version ("9.0.0-beta4")
}

group = "zeus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.11.0")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "zeus.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}