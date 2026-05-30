plugins {
    alias(libs.plugins.nia.compose.bridge.feature.impl)
}

android {
    namespace = "com.nia.compose.bridge.weather.impl"
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(projects.core.common)
    implementation(projects.core.viewbinding)
    implementation(projects.feature.weather.api)
}
