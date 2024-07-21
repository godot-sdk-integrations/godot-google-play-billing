# godot-google-play-billing

Godot Android plugin for the Google Play Billing Library

This currently uses version 7.0.0 of the Google Play Billing Library. This is currently the newest version (as of 2024) and will be [deprecated](https://developer.android.com/google/play/billing/deprecation-faq) starting on August 31, 2026. 

## Usage & Docs

You can find the docs for this first-party plugin in the [official Godot docs](https://docs.godotengine.org/en/stable/tutorials/platform/android_in_app_purchases.html).

## Compiling

-- If you don't want to compile yourself in `output` the `.aar` has been pre-compiled with Godot 4.2.2.

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
- For Godot versions below 4.2 use the `gdap` file located in `editor_export_plugin/legacy.`
- Choose your preferred language and place the directory `google_play_billing` which is inside the directory `editor_export_plugin` into the `addons` folder of your Godot project.
- Take the compiled `.aar` files from the plugin and place them in the `libs` folder.
- Enable the plugin from the dedicated project settings tab. (For C# users, press the build button first)
- Make sure to enable Android Gradle builds when exporting.

## API
- Official class [reference](https://developer.android.com/reference/com/android/billingclient/api/package-summary).
- Google guidelines for [integrating](https://developer.android.com/google/play/billing/integrate) the API into your Android application.

## Example
1. Use `startConnection()` on `_Ready()` to connect `BillingClient` with Google servers.
2. Call `queryProductDetails` to show what products are available for purchase and to cache product IDs.
3. If you have a one time purchase, (like a remove ads purchase or a level that unlocks once) call `purchaseNonConsumable.` Alternatively for a consumable purchase (like a virtual currency or an item that you can have multiples of) call `purchaseConsumable.` Subscription can be called with `purchaseSubscription.`
4. If the request is successfully completed, you'll get a result with the `purchasesUpdated` signal. However this is not guaranteed such as if the user loses internet connection. To make sure, call `queryPurchases.` This is needed to verify the user successfully paid for the purchase so the developer can grant entitlement (the purchased item).
5. Google requires the developer to acknowledge the purchase, which can be done with `acknowledgePurchase.` This is done after a successful purchase has occurred and demonstrates the user received entitlement. If this isn't done after a period of time, Google will automatically refund it to the user.

## API Bindings
- This can optionally be added to your Godot project to get a headstart on using the API. 
- Choose the C# or GDscript bindings which can be used in your Godot project. Since the Godot plugin system uses engine-level reflection, the API singleton has to be registered along with corresponding signals and methods.
- That's where these bindings come in. The Google API has native classes in Java or Kotlin that currently are not able to be marshalled by Godot so they're converted into Godot `Dictionary.` These bindings parse the dictionaries into classes that Godot works with natively such as C# and GDscript.
- An example of how this is done is with `GodotObject billing = Engine.GetSingleton(NAME_OF_PLUGIN).` Through that `GodotObject` the API can be accessed.