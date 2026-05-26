plugins {
    alias(libs.plugins.emptyandroid.android.library)
}

android {
    namespace = "com.empty.android.core.viewbinding"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core:base"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)
    implementation(libs.fragment.ktx)
    implementation(libs.timber)
}
