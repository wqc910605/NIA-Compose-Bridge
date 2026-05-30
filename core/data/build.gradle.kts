plugins {
    alias(libs.plugins.nia.compose.bridge.library)
    alias(libs.plugins.nia.compose.bridge.hilt)
}

android {
    namespace = "com.nia.compose.bridge.core.data"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(projects.core.database)
    api(projects.core.datastore)
    api(projects.core.network)

    implementation(libs.kotlinx.coroutines.android)
}
