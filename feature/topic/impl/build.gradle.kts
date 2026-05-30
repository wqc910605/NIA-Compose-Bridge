plugins {
    alias(libs.plugins.nia.compose.bridge.feature.impl)
}

android {
    namespace = "com.nia.compose.bridge.topic.impl"
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(projects.feature.topic.api)
    implementation(projects.core.viewbinding)
}
