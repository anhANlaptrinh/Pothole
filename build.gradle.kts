// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.android.library") version "8.1.1" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}