plugins {
    alias(libs.plugins.blossom)
    java
}

group = project.properties["maven_group"]!!
version = project.properties["maven_version"]!!

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.minestom)
    implementation(libs.tntloader)
}

tasks {
    blossom {
        val server = "src/main/java/com/github/selfcrafted/customlobby/Server.java"

        replaceToken("&version", version, server)
        replaceToken("&minestomVersion", libs.versions.minestom.get(), server)
    }

    test {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}