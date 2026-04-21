---
title: Billing Client
---
# BillingClient

`BillingClient` is the main class used to interact with the Google Play Billing service.

It provides functionality for:

- Connecting to Google Play Billing
- Querying product details
- Querying previously owned purchases
- Starting purchase flows
- Consuming purchases
- Acknowledging purchases
- Managing subscriptions

All billing operations should be performed through an instance of this class.

---

## Creating the Client

Create the billing client before performing any billing operations.

```gdscript
var billing_client := BillingClient.new()
```

The BillingClient internally communicates with the Android plugin through a JNI singleton.

---

## Typical Billing Flow

A typical purchase flow looks like this:

1. Create `BillingClient`
2. Connect to [signals](signals.md#signals)
3. Call `start_connection()`
4. Wait for the [`connected`](signals.md#connected) signal
5. Query products with `query_product_details()`
6. Start purchase with `purchase()`
7. Handle [`on_purchase_updated`](signals.md#on_purchase_updated)
8. Acknowledge or consume the purchase

**Example**

```gdscript
var billing_client := BillingClient.new()

func _ready():
    billing_client.connected.connect(_on_connected)
    billing_client.start_connection()

func _on_connected():
    var productIds = ["coins_100"]
    billing_client.query_product_details(productIds, BillingClient.ProductType.INAPP)
```
