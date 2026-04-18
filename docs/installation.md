---
title: Installation
icon: fontawesome/solid/download
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

You can install the plugin in two ways: directly from the Godot Editor using AssetLib, or manually from the [Godot Asset Store](https://store-beta.godotengine.org/asset/godot-foundation/godot-google-play-billing) or [GitHub releases](https://github.com/godot-sdk-integrations/godot-google-play-billing/releases).

!!! warning
    Before installing: Remove any previous version of this plugin from your project to avoid conflicts.

### Option 1: Using the Godot Editor (AssetLib)

1. Open the AssetLib tab in the Godot Editor and search for `Godot Google Play Billing`.
2. Click Download.
3. In the installation dialog:
    - Make sure `Ignore asset root` is **unchecked**.
    - Click Install.
4. In main menu, go to `Project > Project Settings > Plugins`, and enable **GodotGooglePlayBilling**.

The plugin is now active in your project.

### Option 2: Manual Installation

1. Download the latest release from:
    - [Godot Asset Store](https://store-beta.godotengine.org/asset/godot-foundation/godot-google-play-billing)
    - [GitHub releases](https://github.com/godot-sdk-integrations/godot-google-play-billing/releases).
2. Extract and copy the plugin to your project's `addons` folder:
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
