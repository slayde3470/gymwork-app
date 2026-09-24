// 앱 모듈의 설정.
// 여기서 정하는 것: 패키지명, 안드로이드 버전, 사용할 라이브러리.

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.slayde.hasenheide"
    compileSdk = 35

    defaultConfig {
        // ⚠️ applicationId = 앱의 주민등록번호.
        // 플레이스토어에 출시한 뒤에는 절대 바꿀 수 없다.
        applicationId = "com.slayde.hasenheide"

        minSdk = 26        // 안드로이드 8.0 이상에서 동작
        targetSdk = 35
        versionCode = 10    // 새 APK를 낼 때마다 1씩 올린다 (덮어쓰기 설치에 필요)
        versionName = "0.6.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose BOM — 아래 compose 라이브러리들의 버전을 한 번에 맞춰준다
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
