// 이 파일은 "이 프로젝트에 어떤 모듈이 있는지"와
// "라이브러리를 어디서 받아올지"를 정한다.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Hasenheide"  // 화면에 보이는 앱 이름은 strings.xml에서 정한다
include(":app")
