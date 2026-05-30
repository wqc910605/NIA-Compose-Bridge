plugins {
    alias(libs.plugins.nia.compose.bridge.library)
    alias(libs.plugins.nia.compose.bridge.library.compose)
}

android {
    namespace = "com.nia.compose.bridge.core.designsystem"
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.coil.kt.compose)
}
