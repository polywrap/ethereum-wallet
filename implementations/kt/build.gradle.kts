import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
    id("org.jetbrains.dokka") version "1.8.20"
    id("convention.publication")
}

group = "io.polywrap"
version = "0.10.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://jitpack.io") }
}

val kethereumVersion = "0.85.7"

dependencies {
    implementation("io.polywrap:polywrap-client:0.10.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
//    implementation("com.github.walleth.kethereum:eip155:${kethereumVersion}")
//    implementation("com.github.walleth.kethereum:erc681:${kethereumVersion}")
//    implementation("com.github.walleth.kethereum:erc1450:${kethereumVersion}")
//    implementation("com.github.walleth.kethereum:flows:${kethereumVersion}")
    implementation("com.github.walleth.kethereum:extensions_transactions:${kethereumVersion}")
    implementation("com.github.walleth.kethereum:rpc:${kethereumVersion}")
    implementation("com.github.walleth.kethereum:model:${kethereumVersion}")
    implementation("com.github.walleth.kethereum:crypto:${kethereumVersion}")
    implementation("com.github.walleth.kethereum:erc712:${kethereumVersion}")
    implementation("com.github.walleth.kethereum:rlp:${kethereumVersion}")
    implementation("com.github.walleth.kethereum:eip1559_signer:${kethereumVersion}")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

// javadoc generation for Maven repository publication
tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

// print stdout during tests
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

// lint configuration
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    disabledRules.set(setOf("no-wildcard-imports"))
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude("**/resources/**")
        exclude("**/wrap/**")
        exclude("**/build.gradle.kts")
    }
}

// dokka configuration
tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            documentedVisibilities.set(
                setOf(
                    DokkaConfiguration.Visibility.PUBLIC,
                    DokkaConfiguration.Visibility.PROTECTED
                )
            )
        }
    }
}
