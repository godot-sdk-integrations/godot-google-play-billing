---
icon: lucide/package
---

# Installation

This guide walks you through installing the Godot Google Play Billing Plugin and preparing your project for Android in-app purchases.

## Requirements

Before installing, make sure you have:

- A project using Godot Engine 4.2+
- Android export configured in Godot
- An application in Google Play Console
- An Android device for testing

## Install the Plugin

1. Download the latest release from the [Releases](https://github.com/godot-sdk-integrations/godot-google-play-billing/releases) page.
2. Unzip and copy the plugin to your project's `addons` folder:
```
[Project root]/addons/GodotGooglePlayBilling/
```
3. Open your project in Godot.
4. In main menu, go to `Project > Project Settings > Plugins`, and enable **GodotGooglePlayBilling**.

The plugin is now active in your project.

## Configure Android Export

1. In main menu, go to `Project > Export`.
2. Add an Android export preset if you dont have one.
3. Ensure:
    - `gradle/use_gradle_build` is checked.
    - Package name matches your Google Play Console app
    - Internet permission is enabled
    - The project is signed with a release keystore

Export settings must match your Google Play configuration for billing to work correctly.

## Upload a Test Build

Google Play Billing only works with builds uploaded to Google Play.

1. Export a signed Android AAB build.
2. Upload it to:
    - Internal testing track (recommended)
    - Closed testing track
3. Add your Google account as a tester.
4. Install the app from the Play Store testing link.
