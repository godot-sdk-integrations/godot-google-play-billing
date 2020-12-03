# godot-google-play-billing
Godot Android plugin for the Google Play Billing library


## Usage & Docs

You can find the docs for this first-party plugin in the [official Godot docs](https://docs.godotengine.org/en/stable/tutorials/platform/android_in_app_purchases.html).


## Compiling

Prerequisites:

- Android SDK (platform version 29)
- the Godot Android library (`godot-lib.***.release.aar`) for your version of Godot from the [downloads page](https://godotengine.org/download).

Steps to build:

1. Clone this Git repository
2. Put `godot-lib.***.release.aar` in `./godot-google-play-billing/libs/`
3. Run `./gradlew build` in the cloned repository

If the build succeeds, you can find the resulting `.aar` files in `./godot-google-play-billing/build/outputs/aar/`.
