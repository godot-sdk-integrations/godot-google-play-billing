package org.godotengine.plugin.googleplaybilling

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot


class GodotGooglePlayBilling(godot: Godot): GodotPlugin(godot), PurchasesUpdatedListener {
	private lateinit var billingClient: BillingClient
	private var productDetailsMap: Map<String, ProductDetails> = emptyMap()

	override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

	override fun getPluginSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = mutableSetOf()
		signals.add(SignalInfo("connected"))
		signals.add(SignalInfo("disconnected"))
		signals.add(SignalInfo("connect_error", Integer::class.java, String::class.java))

		signals.add(SignalInfo("query_product_details_response", Dictionary::class.java))
		signals.add(SignalInfo("query_purchases_response", Dictionary::class.java))
		signals.add(SignalInfo("on_purchase_updated", Dictionary::class.java))

		signals.add(SignalInfo("consume_purchase_response", Dictionary::class.java))
		signals.add(SignalInfo("acknowledge_purchase_response", Dictionary::class.java))

		return signals
	}

	@UsedByGodot
	fun initPlugin() {
		billingClient = BillingClient.newBuilder(activity!!)
			.setListener(this)
			.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
			.build()
	}

	@UsedByGodot
	fun startConnection() {
		billingClient.startConnection(object : BillingClientStateListener {
			override fun onBillingSetupFinished(billingResult: BillingResult) {
				if (billingResult.responseCode == BillingResponseCode.OK) {
					emitSignal("connected")
				} else {
					emitSignal("connect_error", billingResult.responseCode, billingResult.debugMessage);
				}
			}

			override fun onBillingServiceDisconnected() {
				emitSignal("disconnected");
			}
		})
	}

	@UsedByGodot
	fun endConnection() {
		billingClient.endConnection()
	}

	@UsedByGodot
	fun isReady(): Boolean {
		return billingClient.isReady
	}

	@UsedByGodot
	fun getConnectionState(): Int {
		return billingClient.connectionState
	}

	@UsedByGodot
	fun queryProductDetails(productIds: Array<String>, productType: String) {
		val productList = productIds.map {
			QueryProductDetailsParams.Product.newBuilder()
				.setProductId(it)
				.setProductType(productType)
				.build()
		}

		val params = QueryProductDetailsParams.newBuilder()
			.setProductList(productList)
			.build()

		billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
			if (billingResult.responseCode == BillingResponseCode.OK) {
				productDetailsMap = productDetailsList.associateBy { it.productId }
				val resultArray = Utils.convertProductDetailsListToArray(productDetailsList)
				emitSignal("query_product_details_response", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage, resultArray))
			} else {
				emitSignal("query_product_details_response", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage))
			}
		}
	}

	@UsedByGodot
	fun queryPurchases(productType: String) {
		val params = QueryPurchasesParams.newBuilder()
			.setProductType(productType)
			.build()

		billingClient.queryPurchasesAsync(params) { billingResult, purchaseList ->
			if (billingResult.responseCode == BillingResponseCode.OK) {
				val resultArray = Utils.convertPurchaseListToArray(purchaseList)
				emitSignal("query_purchases_response", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage, resultArray))
			} else {
				emitSignal("query_purchases_response", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage))
			}
		}
	}

	@UsedByGodot
	fun launchBillingFlow(productId: String, offerToken: String = ""): Dictionary {
		if (!productDetailsMap.containsKey(productId)) {
			val debugMessage = "productId not found! You must query the sku details and wait for the result before purchasing."
			return Utils.createResultDict(BillingResponseCode.DEVELOPER_ERROR, debugMessage)
		}

		val productDetails = productDetailsMap.getValue(productId)

		val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
			.setProductDetails(productDetails)

		if (productDetails.productType == BillingClient.ProductType.SUBS) {
			productParamsBuilder.setOfferToken(offerToken)
		}
		val productParams = productParamsBuilder.build()

		val flowParams = BillingFlowParams.newBuilder()
			.setProductDetailsParamsList(listOf(productParams))
			.build()

		val billingResult = billingClient.launchBillingFlow(activity!!, flowParams)

		return Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage)
	}

	override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
		if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
			val resultArray = Utils.convertPurchaseListToArray(purchases)
			emitSignal("on_purchase_updated", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage, resultArray))
		} else {
			emitSignal("on_purchase_updated", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage))
		}
	}

	@UsedByGodot
	fun consumePurchase(purchaseToken: String) {
		val params = ConsumeParams.newBuilder()
			.setPurchaseToken(purchaseToken)
			.build()

		billingClient.consumeAsync(params) { billingResult, pToken ->
			emitSignal("consume_purchase_response", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage, token = pToken))
		}
	}

	@UsedByGodot
	fun acknowledgePurchase(purchaseToken: String) {
		val params = AcknowledgePurchaseParams.newBuilder()
			.setPurchaseToken(purchaseToken)
			.build()

		billingClient.acknowledgePurchase(params) { billingResult ->
			emitSignal("acknowledge_purchase_response", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage, token = purchaseToken))
		}
	}
}
