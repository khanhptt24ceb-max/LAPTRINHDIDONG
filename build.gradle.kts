// Top-level build file
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// Tự động chuyển thư mục build sang đường dẫn ASCII nếu thư mục chứa project có dấu tiếng Việt trên Windows
allprojects {
    if (rootDir.absolutePath.any { it.code > 127 }) {
        layout.buildDirectory.set(
            file("${System.getProperty("user.home")}/.gradle/ecomer-build/${project.name}")
        )
    }
}
