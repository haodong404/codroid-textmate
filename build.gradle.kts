import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    `maven-publish`
    application
}

group = "org.codroid"
version = "1.0.0"

tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("docs"))
}


val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml.get())
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml.get().outputDirectory)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.googlecode.plist:dd-plist:1.24")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation(project(":oniguruma-lib"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

publishing {
    publications {
        create<MavenPublication>("codroid-textmate") {
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(javadocJar)
            artifact(tasks.kotlinSourcesJar)
            pom {
                name.set("codroid-textmate")
                description.set("A library written in kotlin that helps tokenize text using Text Mate grammars.")
                url.set("https://github.com/zacharychin233/codroid-textmate")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        name.set("Zacharychin233")
                        email.set("vcoty233@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/zacharychin233/codroid-textmate")
                    connection.set("scm:git:git://zacharychin233/codroid-textmate.git")
                    developerConnection.set("scm:git:ssh://git@github.com:zacharychin233/codroid-textmate.git")
                }
            }
        }
    }
}


nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USER"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}
