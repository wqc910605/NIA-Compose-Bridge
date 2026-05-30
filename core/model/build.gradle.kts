plugins {
    alias(libs.plugins.nia.compose.bridge.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
