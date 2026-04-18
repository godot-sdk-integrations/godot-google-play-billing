---
title: Quick start
icon: fontawesome/solid/rocket
---

# Quick Start

This guide walks through the minimal steps required to initialize billing, query products, and handle purchases.

!!! info
	Google Play Billing only works with builds uploaded to the **Google Play Store**.

	You can continue following this guide, but before testing purchases, make sure your Play Console is properly configured.
	See the [Google Play Console Setup](google_play_console_setup.md) guide.

## Initialize the plugin

To use the GodotGooglePlayBilling API:

1. Access the [`BillingClient`](api/billing_client.md) class.
2. Connect to its signals to receive billing results.
3. Call [`start_connection()`](api/methods.md#start_connection).

Initialization example:

```gdscript
var billing_client: BillingClient
func _ready():
	billing_client = BillingClient.new()
	billing_client.connected.connect(_on_connected) # No params
	billing_client.disconnected.connect(_on_disconnected) # No params
	billing_client.connect_error.connect(_on_connect_error) # response_code: int, debug_message: String
	billing_client.query_product_details_response.connect(_on_query_product_details_response) # response: Dictionary
	billing_client.query_purchases_response.connect(_on_query_purchases_response) # response: Dictionary
	billing_client.on_purchase_updated.connect(_on_purchase_updated) # response: Dictionary
	billing_client.consume_purchase_response.connect(_on_consume_purchase_response) # response: Dictionary
	billing_client.acknowledge_purchase_response.connect(_on_acknowledge_purchase_response) # response: Dictionary

	billing_client.start_connection()
```

The API must be in a connected state prior to use. The [`connected`](api/signals.md#connected) signal is sent when the connection process succeeds.
You can also use [`is_ready()`](api/methods.md#is_ready) to determine if the plugin is ready for use.
The [`get_connection_state()`](api/methods.md#get_connection_state) function returns the current connection state of the plugin.

Return values for [`get_connection_state()`](api/methods.md#get_connection_state):

```gdscript
# Matches BillingClient.ConnectionState in the Play Billing Library.
# Access in your script as: BillingClient.ConnectionState.CONNECTED
enum ConnectionState {
	DISCONNECTED, # This client was not yet connected to billing service or was already closed.
	CONNECTING, # This client is currently in process of connecting to billing service.
	CONNECTED, # This client is currently connected to billing service.
	CLOSED, # This client was already closed and shouldn't be used again.
}
```

## Query available items

Once the API has connected, query product IDs using [`query_product_details(...)`](api/methods.md#query_product_details).
You must successfully complete a product details query before calling the
[`purchase(...)`](api/methods.md#purchase), [`purchase_subscription(...)`](api/methods.md#purchase_subscription),
or [`update_subscription(...)](api/methods.md#update_subscription) functions, or they will return an error.

[`query_product_details(...)`](api/methods.md#query_product_details) takes two parameters: an array of product ID strings
and the type of product being queried. The product type should be `BillingClient.ProductType.INAPP` for normal in-app purchases
or `BillingClient.ProductType.SUBS` for subscriptions.

The ID strings in the array should match the product IDs defined in the Google Play Console entry for your app.

**Example**

```gdscript
func _on_connected():
	billing_client.query_product_details(["my_iap_item"], BillingClient.ProductType.INAPP) # BillingClient.ProductType.SUBS for subscriptions.

func _on_query_product_details_response(query_result: Dictionary):
	if query_result.response_code == BillingClient.BillingResponseCode.OK:
		print("Product details query success")
		for available_product in query_result.product_details:
			print(available_product)
	else:
		print("Product details query failed")
		print("response_code: ", query_result.response_code, "debug_message: ", query_result.debug_message)
```

## Query user purchases

To retrieve a user's purchases, call the [`query_purchases(...)`](api/methods.md#query_purchases) function passing a product type to query.
The product type should be `BillingClient.ProductType.INAPP` for normal in-app purchases or `BillingClient.ProductType.SUBS` for subscriptions.

The [`query_purchases_response`](api/signals.md#query_product_details_response) signal is sent with the result.
The signal has a single parameter: a `Dictionary` with a response code andeither an array of purchases or a debug message.

!!! note
	Only active subscriptions and non-consumed one-time purchases are included in the purchase array.

**Example**

```gdscript
func _query_purchases():
	billing_client.query_purchases(BillingClient.ProductType.INAPP) # Or BillingClient.ProductType.SUBS for subscriptions.

func _on_query_purchases_response(query_result: Dictionary):
	if query_result.response_code == BillingClient.BillingResponseCode.OK:
		print("Purchase query success")
		for purchase in query_result.purchases:
			_process_purchase(purchase)
	else:
		print("Purchase query failed")
		print("response_code: ", query_result.response_code, "debug_message: ", query_result.debug_message)
```

## Purchase an item

To launch the billing flow for an item: Use [`purchase(...)`](api/methods.md#purchase) for in-app products, passing the product ID string.
Use [`purchase_subscription(...)`](api/methods.md#purchase_subscription) for subscriptions, passing the product ID and base plan ID.
You may also optionally provide an offer ID.

For both [`purchase(...)`](api/methods.md#purchase) and [`purchase_subscription(...)`](api/methods.md#purchase_subscription),
you can optionally pass a boolean to indicate whether offers are personallised

This method returns a dictionary indicating whether the billing flow was successfully launched.
It includes a response code and either an array of purchases or a debug message.

!!! info
	you *must* query the product details for an item before you can pass it to [purchase(...)](api/methods.md#purchase).

**Example**

```gdscript
var result = billing_client.purchase("my_iap_item")
if result.response_code == BillingClient.BillingResponseCode.OK:
	print("Billing flow launch success")
else:
	print("Billing flow launch failed")
	print("response_code: ", result.response_code, "debug_message: ", result.debug_message)
```

The result of the purchase will be sent through the [`on_purchase_updated`](api/signals.md#on_purchase_updated) signal.

```gdscript
func _on_purchase_updated(result: Dictionary):
	if result.response_code == BillingClient.BillingResponseCode.OK:
		print("Purchase update received")
		for purchase in result.purchases:
			_process_purchase(purchase)
	else:
		print("Purchase update error")
		print("response_code: ", result.response_code, "debug_message: ", result.debug_message)
```

### Processing a purchase item

The [`query_purchases_response`](api/signals.md#query_purchases_response) and [`on_purchase_updated`](api/signals.md#on_purchase_updated) signals
provide an array of purchases in Dictionary format.

Check the `purchase_state` value of a purchase to determine if a purchase was completed or is still pending.

PurchaseState values:

```gdscript
# Matches Purchase.PurchaseState in the Play Billing Library
# Access in your script as: BillingClient.PurchaseState.PURCHASED
enum PurchaseState {
	UNSPECIFIED,
	PURCHASED,
	PENDING,
}
```

If a purchase is in a `PENDING` state, you should not award the contents of the purchase or do any further processing of the purchase
until it reaches the `PURCHASED` state. If you have a store interface, you may wish to display information about pending purchases
needing to be completed in the Google Play Store.

For more details on pending purchases, see [Handling pending transactions](https://developer.android.com/google/play/billing/integrate#pending)
in the Google Play Billing Library documentation.

## Consumables

If your in-app item is not a one-time purchase but a consumable item (e.g. coins) which can be purchased multiple times, you can consume
an item by calling [`consume_purchase(...)`](api/methods.md#consume_purchase) passing the `purchase_token` value from the purchase dictionary.

!!! info
	Calling [consume_purchase(...)](api/methods.md#consume_purchase) automatically acknowledges a purchase.

Consuming a product allows the user to purchase it again, it will no longer appear in subsequent
[`query_purchases(...)`](api/methods.md#query_purchases) calls unless it is repurchased.

**Example**

```gdscript
func _process_purchase(purchase):
	if "my_consumable_iap_item" in purchase.product_ids and purchase.purchase_state == BillingClient.PurchaseState.PURCHASED:
		# Add code to store payment so we can reconcile the purchase token
		# in the completion callback against the original purchase
		billing_client.consume_purchase(purchase.purchase_token)

func _on_consume_purchase_response(result: Dictionary):
	if result.response_code == BillingClient.BillingResponseCode.OK:
		print("Consume purchase success")
		_handle_purchase_token(result.token, true)
	else:
		print("Consume purchase failed")
		print("response_code: ", result.response_code, "debug_message: ", result.debug_message, "purchase_token: ", result.token)

# Find the product associated with the purchase token and award the
# product if successful
func _handle_purchase_token(purchase_token, purchase_successful):
	# check/award logic, remove purchase from tracking list
```

## Acknowledging purchases

If your in-app item is a one-time purchase, you must acknowledge the purchase by calling the
[`acknowledge_purchase(...)`](api/methods.md#acknowledge_purchase) function, passing the `purchase_token` value from the purchase dictionary.
If you do not acknowledge a purchase, the user automatically receives a refund, and Google Play revokes the purchase.

If you are calling [`consume_purchase(...)`](api/methods.md#consume_purchase) it automatically acknowledges the purchase and
you do not need to call [`acknowledge_purchase(...)`](api/methods.md#acknowledge_purchase).

**Example**

```gdscript
func _process_purchase(purchase):
	if "my_one_time_iap_item" in purchase.product_ids and \
			purchase.purchase_state == BillingClient.PurchaseState.PURCHASED and \
			not purchase.is_acknowledged:
		# Add code to store payment so we can reconcile the purchase token
		# in the completion callback against the original purchase
		billing_client.acknowledge_purchase(purchase.purchase_token)

func _on_acknowledge_purchase_response(result: Dictionary):
	if result.response_code == BillingClient.BillingResponseCode.OK:
		print("Acknowledge purchase success")
		_handle_purchase_token(result.token, true)
	else:
		print("Acknowledge purchase failed")
		print("response_code: ", result.response_code, "debug_message: ", result.debug_message, "purchase_token: ", result.token)

# Find the product associated with the purchase token and award the
# product if successful
func _handle_purchase_token(purchase_token, purchase_successful):
	# check/award logic, remove purchase from tracking list
```

## Subscriptions

Subscriptions work mostly like regular in-app items. Use `BillingClient.ProductType.SUBS` as the
second argument to [`query_product_details(...)`](api/methods.md#query_product_details) to get subscription details.
Pass `BillingClient.ProductType.SUBS` to [`query_purchases(...)`](api/methods.md#query_purchases) to get subscription purchase details.

You can check `is_auto_renewing` in the a subscription purchase returned from [`query_purchases(...)`](api/methods.md#query_purchases) to see
if a user has cancelled an auto-renewing subscription.

!!! note
	When a user's subscription is paused, the Play Billing Library doesn't return the subscription unless
	the `include_suspended_subs` parameter is set to **true** in [`query_purchases(...)`](api/methods.md#query_purchases).

You need to acknowledge new subscription purchases, but not automatic subscription renewals.

If you support upgrading or downgrading between different subscription levels, you need to use
[`update_subscription(...)`](api/methods.md#update_subscription) to call the subscription update flow to change an active subscription.

Like [`purchase(...)`](api/methods.md#purchase), results are returned by the [`on_purchase_updated`](api/signals.md#on_purchase_updated) signal.

**Example**

```gdscript
billing_client.update_subscription(active_sub_product_id, active_sub_purchase_token, BillingClient.ReplacementMode.CHARGE_PRORATED_PRICE, "new_sub_product_id", "base_plan_id", "new_user_offer_id", false)
```
