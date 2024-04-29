# godot-google-play-billing
Godot Android plugin for the Google Play Billing library


## Usage & Docs

You can find the docs for this first-party plugin in the [official Godot docs](https://docs.godotengine.org/en/stable/tutorials/platform/android_in_app_purchases.html).


## Compiling

Prerequisites:

- Android SDK (platform version 33)
- the Godot Android library (`godot-lib.***.release.aar`) for your version of Godot from the [downloads page](https://godotengine.org/download).

Steps to build:

1. Clone this Git repository
2. Put `godot-lib.***.release.aar` in `./godot-google-play-billing/libs/`
3. Run `./gradlew build` in the cloned repository

If the build succeeds, you can find the resulting `.aar` files in `./godot-google-play-billing/build/outputs/aar/`.

## Installing
- This now uses v2 of the [Android Plugin](https://docs.godotengine.org/en/stable/tutorials/platform/android/android_plugin.html) architecture. (Compatible with Godot 4.2+)
- Choose your preferred language and place the directory `google_play_billing` which is inside of `editor_export_plugin` into the `addons` folder of your Godot project.
- Take the compiled `.aar` files from the plugin and place them in the `libs` folder.
- Enable the plugin from the dedicated project settings tab. (For C# users, press the build button first)
- Make sure to enable Android Gradle builds when exporting.