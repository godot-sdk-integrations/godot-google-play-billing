extends Panel

var one_time_product = load("res://OneTimeProduct.tscn")

@onready var store_item_container: HFlowContainer = $TabContainer/Store/ItemContainer
@onready var purchased_item_container: HFlowContainer = $TabContainer/PurchasedItems/ItemContainer
@onready var error_message: Label = $ErrorMessage

var product_list = {
	"godot_blue": load("res://icon.svg"),
	"godot_red": load("res://icon.svg")
}

var billing_client: BillingClient
var owned_products: Dictionary[String, String]

func _init() -> void:
	billing_client = BillingClient.new()
	billing_client.connected.connect(_on_connected) # No params
	billing_client.connect_error.connect(_on_connect_error) # response_code: int, debug_message: String
	billing_client.query_product_details_response.connect(_on_query_product_details_response) # response: Dictionary
	billing_client.query_purchases_response.connect(_on_query_purchases_response) # response: Dictionary
	billing_client.on_purchase_updated.connect(_on_purchases_updated) # response: Dictionary
	
	billing_client.start_connection()

func _on_connected() -> void:
	error_message.text = ""
	billing_client.query_product_details(product_list.keys(), BillingClient.ProductType.INAPP)

func _on_connect_error(response_code: int, debug_message: String) -> void:
	error_message.text = str("Connection error: ",response_code,"\n",debug_message)

func _on_query_product_details_response(query_result: Dictionary):
	if query_result.response_code == BillingClient.BillingResponseCode.OK:
		print_verbose("Product details query success")
		for available_product in query_result.product_details:
			create_one_time_product(available_product)
	else:
		print_verbose("Product details query failed")
		error_message.text = str("Product details query: Error: ", query_result.response_code, "\n", query_result.debug_message)

func create_one_time_product(product: Dictionary):
	var product_id = product["product_id"]
	var item = one_time_product.instantiate()
	var purchase = func():
		var result = billing_client.purchase(product_id)
		if result.response_code == BillingClient.BillingResponseCode.OK:
			print_verbose("Billing flow launch success")
		else:
			print_verbose("Billing flow launch failed")
			error_message.text = str("Billing flow launch: Error: ", result.response_code, "\n", result.debug_message)
	
	var consume = func():
		billing_client.consume_purchase(owned_products[product_id])
		var result = await billing_client.consume_purchase_response
		if result.response_code == BillingClient.BillingResponseCode.OK:
			print_verbose("Consume purchase success")
			owned_products.erase(product_id)
			item.already_owned = false
			item._ready()
		else:
			print_verbose("Consume purchase failed")
			error_message.text = str("Consume purchase: Error: ", result.response_code, "debug_message: ", result.debug_message)
	
	item.price = product["one_time_purchase_offer_details"]["formatted_price"]
	
	if owned_products.has(product_id):
		item.already_owned = true
		item.consume.connect(consume)
		purchased_item_container.add_child(item)
	else:
		item.buy_product.connect(purchase)
		store_item_container.add_child(item)

func _on_query_purchases_response(query_result: Dictionary):
	if query_result.response_code == BillingClient.BillingResponseCode.OK:
		print("Purchase query success")
		for purchase in query_result.purchases:
			_process_purchase(purchase)
	else:
		print("Purchase query failed")
		error_message.text = str("Error: ", query_result.response_code, "\n", query_result.debug_message)

func _on_purchases_updated(result: Dictionary):
	if result.response_code == BillingClient.BillingResponseCode.OK:
		print("Purchase update received")
		for purchase in result.purchases:
			_process_purchase(purchase)
	else:
		print("Purchase update error")
		error_message.text = str("Error: ", result.response_code, "\n", result.debug_message)

func _process_purchase(purchase):
	print("Processing purchase...")
	print_verbose(purchase)
	if purchase.is_acknowledged:
		owned_products[purchase.product_ids[0]] = purchase.purchase_token
	else:
		billing_client.acknowledge_purchase(purchase.purchase_token)
		var result = await billing_client.acknowledge_purchase_response
		if result.response_code == BillingClient.BillingResponseCode.OK:
			print("Acknowledge purchase success")
			owned_products[purchase.product_ids[0]] = purchase.purchase_token
		else:
			print("Acknowledge purchase failed")
			print("response_code: ", result.response_code, "debug_message: ", result.debug_message, "purchase_token: ", result.token)

func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_CLOSE_REQUEST:
		billing_client.end_connection()
