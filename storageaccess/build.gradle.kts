plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.planck.storageaccess"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

// Explicit API mode guards the published surface only; test sources have no consumers and would
// otherwise need a visibility modifier on every test class.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    if (!name.contains("Test")) {
        compilerOptions.freeCompilerArgs.add("-Xexplicit-api=strict")
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}