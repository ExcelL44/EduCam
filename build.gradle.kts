// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Add JavaPoet to the buildscript classpath to ensure the plugin worker sees a
// compatible version at runtime (fixes NoSuchMethodError during Hilt aggregation).
buildscript {
    dependencies {
        classpath("com.squareup:javapoet:1.13.0")
    }
}

// Force a compatible JavaPoet version to avoid Hilt runtime NoSuchMethodError
configurations.all {
    resolutionStrategy {
        // Hilt's processors require a JavaPoet version that provides `canonicalName()`
        // Use a JavaPoet version available on Maven Central that provides the
        // needed API for Hilt's processors.
        force("com.squareup:javapoet:1.13.0")
    }
}