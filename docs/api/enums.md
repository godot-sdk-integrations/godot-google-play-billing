---
title: Enums
---

# Enums

Enums used by the billing API.

---

## BillingResponseCode

Response codes returned by billing operations.

```gdscript
enum BillingResponseCode {
    OK = 0, # Success.
    USER_CANCELED = 1, # User cancelled the purchase flow
    SERVICE_UNAVAILABLE = 2, # Network error or no connection
    BILLING_UNAVAILABLE = 3, # A user billing error occurred during processing.
    ITEM_UNAVAILABLE = 4, # The requested product is not available for purchase.
    DEVELOPER_ERROR = 5, # Error resulting from incorrect usage of the API.
    ERROR = 6, # Fatal error during the API action.
    ITEM_ALREADY_OWNED = 7, # The purchase failed because the item is already owned.
    ITEM_NOT_OWNED = 8, # Requested action on the item failed since it is not owned by the user.
    NETWORK_ERROR = 12, # A network error occurred during the operation.
    SERVICE_DISCONNECTED = -1, # The app is not connected to the Play Store service via the Google Play Billing Library.
    FEATURE_NOT_SUPPORTED = -2, # The requested feature is not supported by the Play Store on the current device.
    SERVICE_TIMEOUT = -3 # Request timed out. It's deprecated, SERVICE_UNAVAILABLE which will be used instead of this code.
}
```

---

## ConnectionState

Represents the billing connection state.

```gdscript
enum ConnectionState {
    DISCONNECTED, # This client was not yet connected to billing service or was already closed.
    CONNECTING, # This client is currently in process of connecting to billing service.
    CONNECTED, # This client is currently connected to billing service.
    CLOSED, # This client was already closed and shouldn't be used again.
}
```

---

## ProductType

Defines the type of product.

```gdscript
enum ProductType {
    INAPP, # A Product type for Android apps in-app products.
    SUBS # A Product type for Android apps subscriptions.
}
```

---

## PurchaseState

Represents the state of a purchase.

```gdscript
enum PurchaseState {
    UNSPECIFIED_STATE, # Purchase with unknown state.
    PURCHASED, # Purchase is completed.
    PENDING, # Purchase is pending and not yet completed to be processed by your app.
}
```

---

## ReplacementMode

Defines how subscription replacements behave.

```gdscript
enum ReplacementMode {
    # Unknown...
    UNKNOWN_REPLACEMENT_MODE = 0,

    # The new plan takes effect immediately, and the remaining time will be prorated and credited to the user.
    # Note: This is the default behavior.
    WITH_TIME_PRORATION = 1,

    # The new plan takes effect immediately, and the billing cycle remains the same.
    CHARGE_PRORATED_PRICE = 2,

    # The new plan takes effect immediately, and the new price will be charged on next recurrence time.
    WITHOUT_PRORATION = 3,

    # Replacement takes effect immediately, and the user is charged full price of new plan and
    # is given a full billing cycle of subscription, plus remaining prorated time from the old plan.
    CHARGE_FULL_PRICE = 4,

    # The new purchase takes effect immediately, the new plan will take effect when the old item expires.
    DEFERRED = 5,

    # Indicates that this plan should remain unchanged in the new purchase.
    KEEP_EXISTING = 6
}
```
