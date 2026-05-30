plugins {
    alias(libs.plugins.nia.compose.bridge.library)
    alias(libs.plugins.nia.compose.bridge.hilt)
}

android {
    namespace = "com.nia.compose.bridge.core.domain"
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)
    api(projects.core.common)

    implementation(libs.kotlinx.coroutines.android)
}
