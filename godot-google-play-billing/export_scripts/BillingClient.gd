class_name BillingClient extends Node

signal connected
signal disconnected
signal connect_error(response_code: int, debug_message: String)
signal query_product_details_response(response: Dictionary)
signal query_purchases_response(response: Dictionary)
signal on_purchase_updated(response: Dictionary)
signal consume_purchase_response(response: Dictionary)
signal acknowledge_purchase_response(response: Dictionary)


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

enum ConnectionState {
	DISCONNECTED, # This client was not yet connected to billing service or was already closed.
	CONNECTING, # This client is currently in process of connecting to billing service.
	CONNECTED, # This client is currently connected to billing service.
	CLOSED # This client was already closed and shouldn't be used again.
}

enum ProductType {
	INAPP, # A Product type for Android apps in-app products.
	SUBS # A Product type for Android apps subscriptions.
}

enum PurchaseState {
	UNSPECIFIED_STATE, # Purchase with unknown state.
	PURCHASED, # Purchase is completed.
	PENDING, # Purchase is pending and not yet completed to be processed by your app.
}

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
	CHARGE_FULL_PRICE = 5,
	# The new purchase takes effect immediately, the new plan will take effect when the old item expires.
	DEFERRED = 6
}

var _plugin_singleton: JNISingleton
var _plugin_name: String = "GodotGooglePlayBilling"


func _init() -> void:
	if Engine.has_singleton(_plugin_name):
		_plugin_singleton = Engine.get_singleton(_plugin_name)
		_plugin_singleton.initPlugin()
		_connect_signals()
	elif OS.has_feature("template"):
		printerr(_plugin_name, " singleton not found!")

func _connect_signals() -> void:
	_plugin_singleton.connect("connected", connected.emit)
	_plugin_singleton.connect("disconnected", disconnected.emit)
	_plugin_singleton.connect("connect_error", connect_error.emit)
	_plugin_singleton.connect("query_product_details_response", query_product_details_response.emit)
	_plugin_singleton.connect("query_purchases_response", query_purchases_response.emit)
	_plugin_singleton.connect("on_purchase_updated", on_purchase_updated.emit)
	_plugin_singleton.connect("consume_purchase_response", consume_purchase_response.emit)
	_plugin_singleton.connect("acknowledge_purchase_response", acknowledge_purchase_response.emit)

func start_connection() -> void:
	if _plugin_singleton:
		_plugin_singleton.startConnection()

func end_connection() -> void:
	if _plugin_singleton:
		_plugin_singleton.endConnection()

func is_ready() -> bool:
	if _plugin_singleton:
		return _plugin_singleton.isReady()
	return false

func get_connection_state() -> int:
	if _plugin_singleton:
		return _plugin_singleton.getConnectionState()
	return ConnectionState.DISCONNECTED

func query_product_details(product_list: PackedStringArray, product_type: ProductType) -> void:
	var product_type_str = "inapp"
	if product_type == ProductType.SUBS:
		product_type_str = "subs"

	if _plugin_singleton:
		_plugin_singleton.queryProductDetails(product_list, product_type_str)

func query_purchases(product_type: ProductType) -> void:
	var product_type_str = "inapp"
	if product_type == ProductType.SUBS:
		product_type_str = "subs"
	
	if _plugin_singleton:
		_plugin_singleton.queryPurchases(product_type_str)

func purchase(product_id: String, is_offer_personalized: bool = false) -> Dictionary:
	if _plugin_singleton:
		return _plugin_singleton.purchase(product_id, is_offer_personalized)
	return Dictionary()

func purchase_subscription(product_id: String, base_plan_id: String, offer_id: String = "", is_offer_personalized: bool = false) -> Dictionary:
	if _plugin_singleton:
		return _plugin_singleton.purchaseSubscription(product_id, base_plan_id, offer_id, is_offer_personalized)
	return Dictionary()

func update_subscription(old_purchase_token: String, replacement_mode: ReplacementMode, new_product_id: String, base_plan_id: String, offer_id: String = "", is_offer_personalized: bool = false) -> Dictionary:
	if _plugin_singleton:
		return _plugin_singleton.updateSubscription(new_product_id, base_plan_id, offer_id, old_purchase_token, replacement_mode, is_offer_personalized)
	return Dictionary()

func consume_purchase(purchase_token: String):
	if _plugin_singleton:
		_plugin_singleton.consumePurchase(purchase_token)

func acknowledge_purchase(purchase_token: String):
	if _plugin_singleton:
		_plugin_singleton.acknowledgePurchase(purchase_token)

func set_obfuscated_account_id(account_id: String):
	if _plugin_singleton:
		_plugin_singleton.setObfuscatedAccountId(account_id)

func set_obfuscated_profile_id(profile_id: String):
	if _plugin_singleton:
		_plugin_singleton.setObfuscatedProfileId(profile_id)

func open_subscriptions_page(product_id: String = ""):
	if _plugin_singleton:
		_plugin_singleton.openSubscriptions(product_id)
