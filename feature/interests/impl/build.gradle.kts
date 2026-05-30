plugins {
    alias(libs.plugins.nia.compose.bridge.feature.impl)
}

android {
    namespace = "com.nia.compose.nia.interests.impl"
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(projects.feature.interests.api)
    implementation(projects.core.viewbinding)
}
