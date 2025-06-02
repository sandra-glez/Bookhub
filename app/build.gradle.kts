plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sandraygonzalo.bookhub"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sandraygonzalo.bookhub"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-firestore:24.9.1")
    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    implementation ("com.google.android.material:material:1.11.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.google.firebase:firebase-firestore:24.7.1")
    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.android.material:material:1.12.0")


}