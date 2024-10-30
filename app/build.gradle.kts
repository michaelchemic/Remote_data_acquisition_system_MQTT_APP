plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.example.myhome"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myhome"
        minSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

    //MQTT
    implementation(libs.org.eclipse.paho.client.mqttv3)
    implementation(libs.paho.org.eclipse.paho.android.service)
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    //图形view
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


}


