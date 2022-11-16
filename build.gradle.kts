plugins {
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadowJar)
    java
}

var displayName = "custom-lobby"

group = "com.github.selfcrafted"
version = "1.1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.minestom)
    implementation(libs.slimeloader)
    implementation(libs.tntloader)
}

tasks {
    blossom {
        val server = "src/main/java/com/github/selfcrafted/customlobby/Server.java"

        replaceToken("&Name", displayName, server)
        replaceToken("&version", version, server)
        replaceToken("&minestomVersion", libs.versions.minestom.get(), server)
    }

    processResources {
        filesMatching("start.sh") {
            expand(
                mapOf(
                    "Name" to displayName,
                    "version" to version
                )
            )
        }
    }

    shadowJar {
        manifest {
            attributes("Main-Class" to "com.github.selfcrafted.customlobby.Server")
        }
        archiveBaseName.set(displayName)
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}