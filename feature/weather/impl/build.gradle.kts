plugins {
    alias(libs.plugins.emptyandroid.android.feature.impl)
}

android {
    namespace = "com.empty.android.feature.weather.impl"
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(projects.core.base)
    implementation(projects.core.viewbinding)
    implementation(projects.feature.weather.api)
}
