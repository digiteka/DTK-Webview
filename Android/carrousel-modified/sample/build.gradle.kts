import org.gradle.internal.extensions.core.extra
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.Properties
import kotlin.apply

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
}

val localProperties = Properties().apply {
	val localProperties = rootProject.file("local.properties")
	if (localProperties.exists() && localProperties.isFile) {
		InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
			load(reader)
		}
	}
}

val extVersionBuild: Int by rootProject.extra
val extVersionCode: Int by rootProject.extra
val extVersionNamePrefix: String by rootProject.extra

fun generateVersionName(): String {
	val isRelease = gradle.startParameter.taskRequests.toString().lowercase(Locale.getDefault()).contains("release")
	val isProd = gradle.startParameter.taskRequests.toString().lowercase(Locale.getDefault()).contains("prod")

	// display build number except for prod release
	return extVersionNamePrefix
		.let { if (isRelease || isProd) it else "$it-$extVersionBuild" }
}

android {
	namespace = "com.digiteka.videofeed.sample"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.digiteka.videofeed"
		minSdk = 23
		targetSdk = 35
		versionCode = extVersionCode
		versionName = generateVersionName()

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

		setProperty("archivesBaseName", "Digiteka-Videofeed-$versionName")

		buildConfigField("String", "DIGITEKA_VIDEOFEED_MDTK", "\"${localProperties.getProperty("DIGITEKA_VIDEOFEED_MDTK") ?: ""}\"")
		buildConfigField("String", "DIGITEKA_VIDEOFEED_VIDEOID", "\"${localProperties.getProperty("DIGITEKA_VIDEOFEED_VIDEOID") ?: ""}\"")
		buildConfigField("String", "DIGITEKA_VIDEOFEED_ADUNITPATH", "\"${localProperties.getProperty("DIGITEKA_VIDEOFEED_ADUNITPATH") ?: ""}\"")
		buildConfigField("String", "DIGITEKA_VIDEOFEED_ZONEID", "\"${localProperties.getProperty("DIGITEKA_VIDEOFEED_ZONEID") ?: ""}\"")
	}
	buildFeatures {
		compose = true
		buildConfig = true
		viewBinding = true
	}
	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.15"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	implementation(project(":lib"))

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.recyclerview)

	implementation(platform(libs.compose.bom))
	implementation(libs.activity.compose)
	implementation(libs.compose.ui)
	implementation(libs.compose.ui.tooling.preview)
	implementation(libs.compose.foundation)
	debugImplementation(libs.compose.ui.tooling)


	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}
