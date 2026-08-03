// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.dokka)
}

val extVersionMajor by extra { 2 }
val extVersionMinor by extra { 1 }
val extVersionPatch by extra { 1 }
val extVersionBuild by extra { 0 }
val extVersionCode: Int by extra { extVersionMajor * 1_000_000 + extVersionMinor * 10_000 + extVersionPatch * 100 + extVersionBuild }
val extVersionNamePrefix: String by extra { "$extVersionMajor.$extVersionMinor.$extVersionPatch" }
