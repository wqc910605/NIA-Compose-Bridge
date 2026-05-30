plugins {
    alias(libs.plugins.nia.compose.bridge.library)
    alias(libs.plugins.nia.compose.bridge.hilt)
    alias(libs.plugins.nia.compose.bridge.room)
}

android {
    namespace = "com.nia.compose.bridge.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.android)
}
