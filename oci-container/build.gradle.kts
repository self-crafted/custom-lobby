plugins {
    alias(libs.plugins.blossom)
    alias(libs.plugins.oci.container)
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
        replaceToken("&Name", rootProject.name,
            "src/main/java/com/github/selfcrafted/customlobby/oci/Preboot.java")
    }

    jib {
        from {
            image = "gcr.io/distroless/java17-debian11"
        }
        to {
            image = "custom-lobby"
            tags = setOf("latest", project.version.toString())
        }
        container {
            jvmFlags = listOf("-Xms512M", "-Xmx512M")
            mainClass = "com.github.selfcrafted.customlobby.oci.Preboot"
            args = listOf()
            ports = listOf("25565/tcp")
            environment = mapOf(
                "IS_CONTAINER" to "true"
            )
            format = com.google.cloud.tools.jib.api.buildplan.ImageFormat.OCI
        }
        containerizingMode = "exploded"
    }

    test {
        useJUnitPlatform()
    }

    build {
        dependsOn(jibBuildTar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}