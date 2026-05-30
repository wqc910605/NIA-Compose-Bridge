plugins {
    alias(libs.plugins.nia.compose.bridge.library)
    alias(libs.plugins.nia.compose.bridge.hilt)
}

android {
    namespace = "com.nia.compose.bridge.core.datastore"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)

    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
