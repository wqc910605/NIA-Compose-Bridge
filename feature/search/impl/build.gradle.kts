plugins {
    alias(libs.plugins.nia.compose.bridge.feature.impl)
}

android {
    namespace = "com.nia.compose.nia.search.impl"
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(projects.feature.search.api)
    implementation(projects.core.viewbinding)
    implementation(projects.core.data)
}
