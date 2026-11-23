import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://aliucord.com")
        maven("https://aliucord.com")
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("com.aliucord:gradle:main-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://aliucord.com")
        maven("https://aliucord.com")
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.aliucord.gradle")
    apply(plugin = "kotlin-android")

    configure<AliucordExtension> {
        author("entityshadowfound", 0L)
        updateUrl.set("https://githubusercontent.com")
        buildUrl.set("https://githubusercontent.com")
    }

    configure<BaseExtension> {
        compileSdkVersion(30)

        defaultConfig {
            minSdk = 21
            targetSdk = 30
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    dependencies {
        "compileOnly"("com.aliucord:Aliucord:main-SNAPSHOT")
        "compileOnly"("com.discord:discord:126021")
        "implementation"("androidx.appcompat:appcompat:1.4.1")
        "implementation"("com.google.android.material:material:1.5.0")
        "implementation"("androidx.constraintlayout:constraintlayout:2.1.3")
    }
}
