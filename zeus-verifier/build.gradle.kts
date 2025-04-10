plugins {
    id("java")
    id("com.gradleup.shadow") version ("9.0.0-beta11")
}

group = "zeus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("tools.aqua:z3-turnkey:4.14.0")
    implementation("zeus:zeus-compiler")
}

java {
    sourceCompatibility = JavaVersion.VERSION_23
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "zeus.zeusverifier.Main"
    }
}

tasks.register("start", JavaExec::class) {
    mainClass = "zeus.zeusverifier.Main"
    classpath = sourceSets["main"].runtimeClasspath

    if (!project.hasProperty("type")) {
        throw GradleException("Invalid usage: argument \"type\" is required")
    }

    if (!project.hasProperty("port")) {
        throw GradleException("Invalid usage: argument \"port\" is required")
    }

    args = listOf(project.property("type").toString(), project.property("port").toString())
}

tasks.test {
    useJUnitPlatform()
}