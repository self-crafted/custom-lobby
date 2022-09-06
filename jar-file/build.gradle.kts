plugins {
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadowJar)
    java
}

group = project.properties["maven_group"]!!
version = project.properties["maven_version"]!!

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.minestom)
    implementation(libs.tntloader)

    implementation(projects.common)
}

tasks {
    blossom {
        val server = "src/main/java/com/github/selfcrafted/customlobby/Server.java"

        replaceToken("&Name", rootProject.name, server)
        replaceToken("&version", version, server)
        replaceToken("&minestomVersion", libs.versions.minestom.get(), server)
    }

    processResources {
        filesMatching("start.sh") {
            expand(
                mapOf(
                    "Name" to rootProject.name,
                    "version" to version
                )
            )
        }
    }

    shadowJar {
        manifest {
            attributes("Main-Class" to "com.github.selfcrafted.customlobby.preboot.JarPreboot")
        }
        archiveBaseName.set(rootProject.name)
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