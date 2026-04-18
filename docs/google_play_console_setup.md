---
title: Console Setup
icon: fontawesome/brands/google-play
---

# Google Play Console Setup

Before testing in-app purchases, your application and products must be configured in the Google Play Console.

## 1. Create Your Application

Create your app in the Google Play Console and ensure the **package name matches your Godot Android export**.

For the full step-by-step guide, see the official documentation: [Create and set up your app](https://support.google.com/googleplay/android-developer/answer/9859152)

## 2. Create In-App Products

Products must be created in the Play Console before they can be queried by the billing plugin.

Follow the official documentation: [Create an in-app product](https://support.google.com/googleplay/android-developer/answer/1153481#zippy=%2Ccreate-a-single-in-app-product)

Important notes:

- **Product ID must exactly match the ID used in your project's code**
- Products must be **activated**

Example product IDs:

```
coins_100
remove_ads
premium_upgrade
```

## 3. Upload a Test Build

Google Play Billing only works with builds uploaded to Google Play.

Upload your build to a testing track (recommended: **Internal testing**).

Official documentation: [Set up an open, closed, or internal test](https://support.google.com/googleplay/android-developer/answer/9845334)

## 4. License Testing (Recommended)

To use Google Play test payment methods without being charged, you must add tester accounts in **License Testing**.

Official documentation: [Test in-app billing with application licensing](https://support.google.com/googleplay/android-developer/answer/6062777)

### Remote Debug Testing

You can test purchase flows while running the app via **remote debugging** without downloading it from the Play Store every time.

This works if:

- The device's **primary** Google Play Store account must be added to the **License Testing list** in the Play Console.
- Your local app's package name must exactly match the one configured in the Play Console.
- At least **one build** with the billing plugin must have been uploaded to the Play Console to **activate** billing for that package.
- All In-App Products or Subscriptions must be set to **Active** status in the Play Console.

