---
title: Methods
---

# Methods

This page documents all available methods of the `BillingClient` class.

---

## acknowledge_purchase

```gdscript
func acknowledge_purchase(purchase_token: String)
```

Acknowledges in-app purchases.

All purchases require acknowledgement. Failure to acknowledge a purchase will result in that purchase being refunded.

Consumable purchases do **not** need to be acknowledged manually because calling [`consume_purchase(...)`](#consume_purchase) already acknowledges them.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">purchase_token</span> | Token identifying the purchase to acknowledge. This value is provided in purchase data returned by [`on_purchase_updated`](signals.md#on_purchase_updated) or [`query_purchases_response`](signals.md#query_purchases_response). |

**Emits** the [`acknowledge_purchase_response`](signals.md#acknowledge_purchase_response) signal.

---

## consume_purchase

```gdscript
func consume_purchase(purchase_token: String)
```

Consumes a given in-app product.

Consumable products can be purchased multiple times. Consuming the purchase removes it from the user's owned purchases so it can be bought again.

Calling this method **automatically acknowledges the purchase**.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`purchase_token`</span> | Token identifying the purchase to consume. This token is included in purchase data received from [`on_purchase_updated`](signals.md#on_purchase_updated) or [`query_purchases_response`](signals.md#query_purchases_response). |

**Emits** the [`consume_purchase_response`](signals.md#consume_purchase_response) signal.

---

## end_connection

```gdscript
func end_connection() -> void
```

Ends the connection to the Google Play Billing service.

Once closed, the client should not be used again.

**Emits** the [`disconnected`](signals.md#disconnected) signal.

---

## get_connection_state

```gdscript
func get_connection_state() -> int
```

Returns the current connection state of the billing client.

**Returns** an `int` representing a value from [`ConnectionState`](enums.md#connectionstate).

**Example**

```gdscript
var state = billing_client.get_connection_state()

if state == BillingClient.ConnectionState.CONNECTED:
    print("Billing ready")
```

---

## is_ready

```gdscript
func is_ready() -> bool
```

Returns `true` if the billing client is **connected and ready for use**.

---

## open_subscriptions_page

```gdscript
func open_subscriptions_page(product_id: String = "")
```

Opens the Google Play subscription management page.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`product_id`</span> | Optional subscription product ID. If provided, Google Play will open the management page for that specific subscription. |

**Example**

```gdscript
billing_client.open_subscriptions_page("my_subscription_product_id")
```

---

## purchase

```gdscript
func purchase(product_id: String, purchase_option_id: String = "", offer_id: String = "", is_offer_personalized: bool = false) -> Dictionary
```

Starts the purchase flow for an in-app product.

!!! note
    The product must first be fetched using [query_product_details(...)](#query_product_details) before attempting to purchase.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`product_id`</span> | Identifier of the in-app product configured in the Google Play Console. |
| <span style="white-space:nowrap">`purchase_option_id`</span> | Optional purchase option identifier returned from [`query_product_details_response`](signals.md#query_product_details_response). Used when the product have multiple purchase options, created in Google Play Console. |
| <span style="white-space:nowrap">`offer_id`</span> | Optional offer identifier associated with the selected purchase option. |
| <span style="white-space:nowrap">`is_offer_personalized`</span> | Indicates whether the price is personalized for the user. |

**Returns** a `Dictionary` describing whether the billing flow launched successfully.

Final purchase results are delivered through the [`on_purchase_updated`](signals.md#on_purchase_updated) signal.

**Example**

```gdscript
billing_client.purchase("coins_100", premium_purchase_id, new_login_offer_id)

# billing_client.purchase("coins_100")
```

---

## purchase_subscription

```gdscript
func purchase_subscription(product_id: String, base_plan_id: String, offer_id: String = "", is_offer_personalized: bool = false) -> Dictionary
```

Starts the purchase flow for a subscription product.

!!! note
    The product must first be fetched using [query_product_details(...)](#query_product_details) before attempting to purchase.

Subscriptions require both a **product ID** and a **base plan ID** configured in the Play Console.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`product_id`</span> | Subscription product identifier defined in Google Play Console. |
| <span style="white-space:nowrap">`base_plan_id`</span> | Base plan ID configured for the subscription. |
| <span style="white-space:nowrap">`offer_id`</span> | Optional offer ID defined under the base plan. |
| <span style="white-space:nowrap">`is_offer_personalized`</span> | Indicates whether the price is personalized for the user. |

**Returns** a `Dictionary` describing whether the billing flow launched successfully.

Final purchase results are delivered through the [`on_purchase_updated`](signals.md#on_purchase_updated) signal.

---

## query_product_details

```gdscript
func query_product_details(product_list: PackedStringArray, product_type: ProductType)
```

Queries product information.

This **must** be called before attempting to purchase products.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`product_list`</span> | List of product IDs configured in Google Play Console. |
| <span style="white-space:nowrap">`product_type`</span> | [`ProductType`](enums.md#producttype) enum value indicating the product type being queried. |

**Emits** the [`query_product_details_response`](signals.md#query_product_details_response) signal.

**Example**

```gdscript
var product_ids = ["coins_100", "premium_potion"]
billing_client.query_product_details(products_ids, BillingClient.ProductType.INAPP)
```

---

## query_purchases

```gdscript
func query_purchases(product_type: ProductType, include_suspended_subs: bool = false)
```

Queries the user's currently owned purchases.

By default, this returns **active subscriptions** and **unconsumed in-app purchases**.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`product_type`</span> | [`ProductType`](enums.md#producttype) enum value indicating the product type being queried. |
| <span style="white-space:nowrap">`include_suspended_subs`</span> | If `true`, suspended subscriptions will also be included in the results. |

!!! info
    Suspended subscriptions are still associated with the user but are **not active**. This can happen if the user paused the subscription or if the renewal payment method was declined.

    The [`Purchase dictionary`](signals.md#purchase-dictionary) will contain `is_suspended = true` for suspended subscriptions.

    When a subscription is suspended, you **should not grant access** to the subscription benefits.
    Instead, guide the user to the subscription management page using [`open_subscriptions_page()`](methods.md#open_subscriptions_page) so they can update their payment method or resume the subscription.

**Emits** the [`query_purchases_response`](signals.md#query_purchases_response) signal.

**Example**

```gdscript
billing_client.query_purchases(BillingClient.ProductType.INAPP)

billing_client.query_purchases(BillingClient.ProductType.SUBS, true)
```

---

## set_obfuscated_account_id

```gdscript
func set_obfuscated_account_id(account_id: String)
```

Sets an **obfuscated account identifier** for the current user.

Helps Google Play detect fraud and associate purchases with the correct user account.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`account_id`</span> | Obfuscated identifier representing the user's account in your system. |

!!! info
    See the official Google Play Billing API reference: [setObfuscatedAccountId](https://developer.android.com/reference/com/android/billingclient/api/BillingFlowParams.Builder#setObfuscatedAccountId(java.lang.String)).

---

## set_obfuscated_profile_id

```gdscript
func set_obfuscated_profile_id(profile_id: String)
```

Sets an **obfuscated profile identifier**.

Useful if your application supports multiple profiles under one account.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`profile_id`</span> | Obfuscated identifier representing the user's profile. |

!!! info
    See the official Google Play Billing API reference: [setObfuscatedProfileId](https://developer.android.com/reference/com/android/billingclient/api/BillingFlowParams.Builder#setObfuscatedProfileId(java.lang.String)).

---

## start_connection

```gdscript
func start_connection() -> void
```

Starts the connection to the Google Play Billing service.

The client must be connected before using any billing features.

Connection results are delivered through:

- [`connected`](signals.md#connected) signal
- [`connect_error`](signals.md#connect_error) signal

---

## update_subscription

```gdscript
func update_subscription(old_product_id: String, old_purchase_token: String, replacement_mode: ReplacementMode, new_product_id: String, base_plan_id: String, offer_id: String = "", is_offer_personalized: bool = false) -> Dictionary
```

Updates an **existing subscription** to a new subscription product or plan.

| Parameter | Description |
|-----------|-------------|
| <span style="white-space:nowrap">`old_purchase_token`</span> | Purchase token of the currently active subscription. This token is returned in purchase data from [`query_purchases_response`](signals.md#query_purchases_response). |
| <span style="white-space:nowrap">`replacement_mode`</span> | [`ReplacementMode`](enums.md#replacementmode) enum value to define how the subscription replacement should behave. |
| <span style="white-space:nowrap">`new_product_id`</span> | Product ID of the subscription to switch to. |
| <span style="white-space:nowrap">`base_plan_id`</span> | Base plan ID of the new subscription. |
| <span style="white-space:nowrap">`offer_id`</span> | Optional offer ID configured under the base plan. |
| <span style="white-space:nowrap">`is_offer_personalized`</span> | Indicates whether the price is personalized for the user. |

**Returns** a `Dictionary` describing whether the update flow launched successfully.

**Emits** the [`on_purchase_updated`](signals.md#on_purchase_updated) signal.

**Example**

```gdscript
billing_client.update_subscription("monthly_adfree_sub", old_purchase_token, BillingClient.ReplacementMode.CHARGE_PRORATED_PRICE, "premium_sub", "monthly")
```
