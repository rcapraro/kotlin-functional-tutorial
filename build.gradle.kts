plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
}

val arrowVersion = "0.10.4"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")

    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-mtl:$arrowVersion")


    implementation("io.arrow-kt:arrow-fx-rx2:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-reactor:$arrowVersion")

    implementation("com.squareup.retrofit2:retrofit:2.7.0")
    implementation("com.squareup.moshi:moshi:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    implementation("com.squareup.retrofit2:converter-moshi:2.7.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")

    implementation("com.typesafe:config:1.4.0")
    implementation("com.google.guava:guava:28.1-jre")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}