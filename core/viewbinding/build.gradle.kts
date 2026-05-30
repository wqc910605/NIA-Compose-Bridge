plugins {
    alias(libs.plugins.nia.compose.bridge.library)
}

android {
    namespace = "com.nia.compose.bridge.core.viewbinding"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)
    implementation(libs.fragment.ktx)
    implementation(libs.timber)
}
