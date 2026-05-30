plugins {
    alias(libs.plugins.nia.compose.bridge.feature.impl)
}

android {
    namespace = "com.nia.compose.bridge.feature.home.impl"
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(projects.feature.home.api)
    implementation(projects.feature.settings.api)
    implementation(projects.core.viewbinding)
}
