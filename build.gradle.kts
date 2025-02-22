plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "net.qilla"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:1_21_4-6490538291")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("net.kyori:adventure-text-minimessage:4.19.0")
    implementation("de.articdive:jnoise-pipeline:4.1.0")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "net.qilla.QServer"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
        destinationDirectory.set(file("C:\\Users\\Richard\\Development\\Servers\\Minestom"))
    }
}