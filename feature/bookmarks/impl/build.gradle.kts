plugins {
    alias(libs.plugins.nia.compose.bridge.feature.impl)
}

android {
    namespace = "com.nia.compose.nia.bookmarks.impl"
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(projects.feature.bookmarks.api)
    implementation(projects.core.viewbinding)
    implementation(projects.core.common)
}
