plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.bulkagrochemapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wnapp.id1683866209198"
        minSdk = 23
        targetSdk = 35
        versionCode = 11
        versionName = "11.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                // This flag helps with native alignment during the build process
                arguments += "-DANDROID_ALIGNED_16KB=ON"
            }
        }
    }

    packaging {
        jniLibs {
            // Forces the build to not compress JNI libs,
            // allowing them to be page-aligned automatically for 16 KB support.
            useLegacyPackaging = false
        }
    }

    ndkVersion = "27.1.12297006"

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)



    // Retrofit 2.11.0 is good, but check if there's a 2.11.x patch for 16KB
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation(libs.razorpay)
}
