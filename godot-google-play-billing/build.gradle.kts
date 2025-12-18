import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val pluginName = "GodotGooglePlayBilling"

val pluginPackageName = "org.godotengine.plugin.googleplaybilling"

android {
    namespace = pluginPackageName
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 21
        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
        setProperty("archivesBaseName", pluginName)
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
    implementation("org.godotengine:godot:4.5.1.stable")
    implementation("com.android.billingclient:billing-ktx:8.0.0")
}

// BUILD TASKS DEFINITION
val copyDebugAARToAddons by tasks.registering(Copy::class) {
    description = "Copies the generated debug AAR binary to the addons directory"
    from("build/outputs/aar")
    include("$pluginName-debug.aar")
    into("../demo/addons/$pluginName/bin/debug")
}

val copyReleaseAARToAddons by tasks.registering(Copy::class) {
    description = "Copies the generated release AAR binary to the addons directory"
    from("build/outputs/aar")
    include("$pluginName-release.aar")
    into("../demo/addons/$pluginName/bin/release")
}

val cleanAddons by tasks.registering(Delete::class) {
    delete("../demo/addons/$pluginName")
}

val copyPluginToAddons by tasks.registering(Copy::class) {
    description = "Copies the export scripts to the addons directory"

    dependsOn(cleanAddons)
    finalizedBy(copyDebugAARToAddons)
    finalizedBy(copyReleaseAARToAddons)

    from("export_scripts")
    into("../demo/addons/$pluginName")
}

tasks.named("assemble").configure {
    finalizedBy(copyPluginToAddons)
}

tasks.named<Delete>("clean").apply {
    dependsOn(cleanAddons)
}
