---
title: Signals
icon: fontawesome/solid/signal
---

# Signals

This page documents all signals emitted by the `BillingClient`.

Signals are used to receive asynchronous results from the Google Play Billing system.

Most billing operations (queries, purchases, acknowledgements) return their final results through signals.

---

## acknowledge_purchase_response

```gdscript
signal acknowledge_purchase_response(response: Dictionary)
```

Emitted when an acknowledge purchase request finishes.

`#!gdscript response: Dictionary` contains the result of this request:

- `#!gdscript response_code: int` - A value from [`BillingResponseCode`](enums.md#billingresponsecode).
- `#!gdscript debug_message: String` - Debug message returned by Google Play billing library.
- `#!gdscript token: String` - The purchase token associated with the request.

---

## connect_error

```gdscript
signal connect_error(response_code: int, debug_message: String)
```

Emitted when the billing client fails to connect to the Google Play Billing service.

- `#!gdscript response_code: int` - Error code from [`BillingResponseCode`](enums.md#billingresponsecode).
- `#!gdscript debug_message: String` - Debug message returned by Google Play billing library.

---

## connected

```gdscript
signal connected
```

Emitted when the billing client successfully connects to the Google Play Billing service.

Once this signal is received, billing operations such as product queries and purchases can be performed.

---

## consume_purchase_response

```gdscript
signal consume_purchase_response(response: Dictionary)
```

Emitted when a consumable purchase has been successfully consumed or if the request failed.

`#!gdscript response: Dictionary` contains the result of the consume request:

- `#!gdscript response_code: int` - A value from [`BillingResponseCode`](enums.md#billingresponsecode).
- `#!gdscript debug_message: String` - Debug message returned by Google Play billing library.
- `#!gdscript token: String` - The purchase token that was consumed.

---

## disconnected

```gdscript
signal disconnected
```

Emitted when the billing client disconnects from the Google Play Billing service.

This can happen due to network interruptions or if the service connection is lost.

---

## on_purchase_updated

```gdscript
signal on_purchase_updated(response: Dictionary)
```

Emitted when the purchase state changes.

This signal is triggered after a purchase flow completes or when a pending purchase updates.

`#!gdscript response: Dictionary` contains purchase update information:

- `#!gdscript response_code: int` - A value from [`BillingResponseCode`](enums.md#billingresponsecode).
- `#!gdscript debug_message: String` - Debug message returned by Google Play billing library.
- `#!gdscript purchases: Array` - Array of purchase dictionaries owned by the user.

### purchase dictionary

Each purchase dictionary contains:

- `#!gdscript order_id: String`
- `#!gdscript purchase_token: String`
- `#!gdscript package_name: String`
- `#!gdscript purchase_state: int` (see [`PurchaseState`](enums.md#purchasestate))
- `#!gdscript purchase_time: int` (milliseconds since the epoch (Jan 1, 1970))
- `#!gdscript original_json: String`
- `#!gdscript is_acknowledged: bool`
- `#!gdscript is_auto_renewing: bool`
- `#!gdscript quantity: int`
- `#!gdscript signature: String`
- `#!gdscript product_ids: PackedStringArray`

**Example**

```gdscript
func _on_purchase_updated(result):
    if result.response_code == BillingClient.BillingResponseCode.OK:
        for purchase in result.purchases:
            print("Purchased:", purchase.product_ids)
```

---

## query_product_details_response

```gdscript
signal query_product_details_response(response: Dictionary)
```

Emitted after [`query_product_details(...)`](methods.md#query_product_details) finishes.

`#!gdscript response: Dictionary` contains product information or an error.

- `#!gdscript response_code: int` - A value from [`BillingResponseCode`](enums.md#billingresponsecode).
- `#!gdscript debug_message: String` - Debug message returned by Google Play.
- `#!gdscript product_details: Array` - Array of product details dictionaries.

**Example**

```gdscript
func _on_query_product_details_response(result):
    if result.response_code == BillingClient.BillingResponseCode.OK:
        for product in result.product_details:
            print(product)
```

---

## query_purchases_response

```gdscript
signal query_purchases_response(response: Dictionary)
```

Emitted when [`query_purchases(...)`](methods.md#query_purchases) completes.

`#!gdscript response: Dictionary` contains the result of the purchase query.

- `#!gdscript response_code: int` - A value from [`BillingResponseCode`](enums.md#billingresponsecode).
- `#!gdscript debug_message: String` - Debug message returned by Google Play.
- `#!gdscript purchases: Array` - Array of [`purchase dictionaries`](#purchase-dictionary) owned by the user.

**Example**

```gdscript
func _on_query_purchases_response(result):
    if result.response_code == BillingClient.BillingResponseCode.OK:
        for purchase in result.purchases:
            print("Owned:", purchase.product_ids)
```
