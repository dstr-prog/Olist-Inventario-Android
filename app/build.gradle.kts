import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val buildDate = providers.gradleProperty("inventoryBuildDate").orNull?.also {
    require(it.matches(Regex("\\d{6}"))) { "inventoryBuildDate deve usar o formato aammdd." }
} ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd", Locale.ROOT))
val counterFile = File(System.getProperty("user.home"), ".gradle/inventario-olist-build-counter.properties")
val properties = Properties()
if (counterFile.isFile) {
    FileInputStream(counterFile).use(properties::load)
}

val createsApk = gradle.startParameter.taskNames.any { taskPath ->
    val taskName = taskPath.substringAfterLast(':').lowercase(Locale.ROOT)
    taskName == "build" || taskName.startsWith("assemble") ||
        taskName.startsWith("bundle") || taskName.startsWith("install")
}
val previousCount = properties.getProperty("date")
    .takeIf { it == buildDate }
    ?.let { properties.getProperty("count")?.toIntOrNull() }
    ?: 0
val dailyBuildNumber = if (createsApk) previousCount + 1 else previousCount.coerceAtLeast(1)
if (dailyBuildNumber > 999) {
    throw GradleException("O contador diário de compilações chegou a 999.")
}
if (createsApk) {
    properties.setProperty("date", buildDate)
    properties.setProperty("count", dailyBuildNumber.toString())
    counterFile.parentFile.mkdirs()
    counterFile.outputStream().use { properties.store(it, "Inventario-Olist daily APK build counter") }
}

val dailyBuildText = dailyBuildNumber.toString().padStart(3, '0')
val generatedVersionName = "1.$buildDate$dailyBuildText"
val generatedVersionCode = "1$buildDate$dailyBuildText".toInt()

android {
    namespace = "br.com.inventario.olist"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.inventario.olist"
        minSdk = 26
        targetSdk = 36
        versionCode = generatedVersionCode
        versionName = generatedVersionName
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.configureEach {
    val variantSuffix = name.removePrefix("assemble")
    if (name.startsWith("assemble") && variantSuffix.lowercase(Locale.ROOT) in setOf("debug", "release")) {
        doLast {
            val variantName = variantSuffix.replaceFirstChar { it.lowercase(Locale.ROOT) }
            val variantDirectory = layout.buildDirectory.dir("outputs/apk/$variantName").get().asFile
            val sourceApk = File(variantDirectory, "app-$variantName.apk")
            if (sourceApk.isFile) {
                val namedApk = File(variantDirectory, "Inventario-Olist-$generatedVersionName.apk")
                sourceApk.copyTo(namedApk, overwrite = true)
                logger.lifecycle("APK gerado: ${namedApk.absolutePath}")
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui:1.11.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.11.4")
    implementation("androidx.compose.foundation:foundation:1.11.4")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Supports .xls catalog imports and inventory exports on Android.
    implementation("com.github.SUPERCILEX.poi-android:poi:3.17")
}
