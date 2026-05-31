plugins {
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadowJar)
    java
}

var displayName = "custom-lobby"

group = "com.github.selfcrafted"
version = "1.1.0-SNAPSHOT"

dependencies {
    implementation(libs.minestom)
    implementation(libs.slimeloader)
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("Name", displayName.toString())
                property("version", version.toString())
                property("minestomVersion", libs.versions.minestom.get())
            }
            resources {
                property("Name", displayName.toString())
                property("version", version.toString())
            }
        }
    }
}

tasks {
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
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
