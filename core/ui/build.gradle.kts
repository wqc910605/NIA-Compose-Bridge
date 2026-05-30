plugins {
    alias(libs.plugins.nia.compose.bridge.library)
    alias(libs.plugins.nia.compose.bridge.library.compose)
}

android {
    namespace = "com.nia.compose.bridge.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.kt.compose)
}
