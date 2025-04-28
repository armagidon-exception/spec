import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = "io.github.revxrsal"
version = "1.4"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.3.1")
    compileOnly("org.yaml:snakeyaml:1.15")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation("com.google.code.gson:gson:2.3.1")
    testImplementation("org.yaml:snakeyaml:1.15")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.jetbrains:annotations:24.1.0")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

mavenPublishing {
    coordinates(
        groupId = group as String,
        artifactId = "spec",
        version = version as String
    )
    pom {
        name.set("spec")
        description.set("A tiny library for generating beautiful, commented, type-safe YML through interfaces")
        inceptionYear.set("2024")
        url.set("https://github.com/Revxrsal/spec/")
        licenses {
            license {
                name.set("MIT")
                url.set("https://mit-license.org/")
                distribution.set("https://mit-license.org/")
            }
        }
        developers {
            developer {
                id.set("revxrsal")
                name.set("Revxrsal")
                url.set("https://github.com/Revxrsal/")
            }
        }
        scm {
            url.set("https://github.com/Revxrsal/spec/")
            connection.set("scm:git:git://github.com/Revxrsal/spec.git")
            developerConnection.set("scm:git:ssh://git@github.com/Revxrsal/spec.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

}
