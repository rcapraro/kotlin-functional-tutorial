plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("org.jetbrains.kotlin.jvm").version("1.3.41")
}

val arrowVersion = "0.9.1-SNAPSHOT"

repositories {
    jcenter()
    maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.arrow-kt:arrow-core-data:$arrowVersion")
    implementation("io.arrow-kt:arrow-core-extensions:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.arrow-kt:arrow-typeclasses:$arrowVersion")
    implementation("io.arrow-kt:arrow-extras-data:$arrowVersion")
    implementation("io.arrow-kt:arrow-extras-extensions:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-effects-data:$arrowVersion")
    implementation("io.arrow-kt:arrow-effects-extensions:$arrowVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
