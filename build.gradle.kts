// 프로젝트 전체에서 쓰는 플러그인의 "버전"만 여기서 선언한다.
// 실제로 적용하는 곳은 app/build.gradle.kts.

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
