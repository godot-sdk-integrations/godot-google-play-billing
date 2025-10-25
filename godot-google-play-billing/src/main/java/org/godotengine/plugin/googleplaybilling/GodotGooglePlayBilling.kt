package org.godotengine.plugin.googleplaybilling

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
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

import android.content.Intent
import androidx.core.net.toUri


class GodotGooglePlayBilling(godot: Godot): GodotPlugin(godot), PurchasesUpdatedListener {
	private lateinit var billingClient: BillingClient
	private var productDetailsMapInapp: Map<String, ProductDetails> = emptyMap()
	private var productDetailsMapSubs: Map<String, ProductDetails> = emptyMap()
	private var obfuscatedAccountId: String = ""
	private var obfuscatedProfileId: String = ""

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
			.enableAutoServiceReconnection()
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

		billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
			if (billingResult.responseCode == BillingResponseCode.OK) {
				var productDetailsList = queryProductDetailsResult.productDetailsList
				if (productType == BillingClient.ProductType.INAPP) {
					productDetailsMapInapp = productDetailsList.associateBy { it.productId }
				} else if (productType == BillingClient.ProductType.SUBS) {
					productDetailsMapSubs = productDetailsList.associateBy { it.productId }
				}
				val productDetailsArray = Utils.convertProductDetailsListToArray(productDetailsList)
				val unfetchedProductArray = Utils.convertUnfetchedProductListToArray(queryProductDetailsResult.unfetchedProductList)
				var resultDict = Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage)
				resultDict["product_details"] = productDetailsArray
				resultDict["unfetched_products"] = unfetchedProductArray
				emitSignal("query_product_details_response", resultDict)
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
				val purchasesArray = Utils.convertPurchaseListToArray(purchaseList)
				var resultDict = Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage)
				resultDict["purchases"] = purchasesArray
				emitSignal("query_purchases_response", resultDict)
			} else {
				emitSignal("query_purchases_response", Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage))
			}
		}
	}

	private fun launchBillingFlow(
		productType: String,
		productId: String,
		basePlanId: String = "",
		offerId: String = "",
		oldPurchaseToken: String = "",
		replacementMode: Int = BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.UNKNOWN_REPLACEMENT_MODE,
		isOfferPersonalized: Boolean = false
	): Dictionary {
		val productDetailsMap = when (productType) {
			BillingClient.ProductType.INAPP -> productDetailsMapInapp
			BillingClient.ProductType.SUBS -> productDetailsMapSubs
			else -> error("Unsupported product type: $productType")
		}
		if (!productDetailsMap.containsKey(productId)) {
			val debugMessage = "productId not found! You must query the product details and wait for the result before purchasing."
			return Utils.createResultDict(BillingResponseCode.DEVELOPER_ERROR, debugMessage)
		}

		val productDetails = productDetailsMap.getValue(productId)

		val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
			.setProductDetails(productDetails)

		if (productDetails.productType == BillingClient.ProductType.SUBS) {
			val offer = productDetails.subscriptionOfferDetails?.let { offers ->
				if (offerId.isBlank()) {
					offers.firstOrNull { it.basePlanId == basePlanId && it.offerId == null }
				} else {
					offers.firstOrNull { it.basePlanId == basePlanId && it.offerId == offerId }
				}
			}

			if (offer != null) {
				productParamsBuilder.setOfferToken(offer.offerToken)
			} else {
				val debugMessage = "Invalid base_plan_id or offer_id. Make sure base_plan_id exists, and offer_id is correct if provided."
				return Utils.createResultDict(BillingResponseCode.DEVELOPER_ERROR, debugMessage)
			}
		}

		val flowParamsBuilder = BillingFlowParams.newBuilder()
			.setProductDetailsParamsList(listOf(productParamsBuilder.build()))
			.setIsOfferPersonalized(isOfferPersonalized)

		if (!obfuscatedAccountId.isEmpty()) {
			flowParamsBuilder.setObfuscatedAccountId(obfuscatedAccountId)
		}
		if (!obfuscatedProfileId.isEmpty()) {
			flowParamsBuilder.setObfuscatedProfileId(obfuscatedProfileId)
		}

		if (!oldPurchaseToken.isEmpty() && replacementMode != SubscriptionUpdateParams.ReplacementMode.UNKNOWN_REPLACEMENT_MODE) {
			val updateParams = SubscriptionUpdateParams.newBuilder()
				.setOldPurchaseToken(oldPurchaseToken)
				.setSubscriptionReplacementMode(replacementMode)
				.build()
			flowParamsBuilder.setSubscriptionUpdateParams(updateParams)
		}

		val billingResult = billingClient.launchBillingFlow(activity!!, flowParamsBuilder.build())

		return Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage)
	}

	override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
		if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
			val purchasesArray = Utils.convertPurchaseListToArray(purchases)
			var resultDict = Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage)
			resultDict["purchases"] = purchasesArray
			emitSignal("on_purchase_updated", resultDict)
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
			var resultDict = Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage)
			resultDict["token"] = pToken
			emitSignal("consume_purchase_response", resultDict)
		}
	}

	@UsedByGodot
	fun acknowledgePurchase(purchaseToken: String) {
		val params = AcknowledgePurchaseParams.newBuilder()
			.setPurchaseToken(purchaseToken)
			.build()

		billingClient.acknowledgePurchase(params) { billingResult ->
			var resultDict = Utils.createResultDict(billingResult.responseCode, billingResult.debugMessage)
			resultDict["token"] = purchaseToken
			emitSignal("acknowledge_purchase_response", resultDict)
		}
	}

	@UsedByGodot
	fun setObfuscatedAccountId(accountId: String) {
		obfuscatedAccountId = accountId
	}

	@UsedByGodot
	fun setObfuscatedProfileId(profileId: String) {
		obfuscatedProfileId = profileId
	}

	@UsedByGodot
	fun purchase(productId: String, isOfferPersonalized: Boolean = false): Dictionary {
		return launchBillingFlow(BillingClient.ProductType.INAPP, productId, isOfferPersonalized = isOfferPersonalized)
	}

	@UsedByGodot
	fun purchaseSubscription(productId: String, basePlanId: String, offerId: String, isOfferPersonalized: Boolean = false): Dictionary {
		return launchBillingFlow(BillingClient.ProductType.SUBS, productId, basePlanId, offerId, isOfferPersonalized = isOfferPersonalized)
	}

	@UsedByGodot
	fun updateSubscription(productId: String, basePlanId: String, offerId: String, oldPurchaseToken: String, replacementMode: Int, isOfferPersonalized: Boolean = false): Dictionary {
		return launchBillingFlow(BillingClient.ProductType.SUBS,productId, basePlanId, offerId, oldPurchaseToken, replacementMode, isOfferPersonalized)
	}

	@UsedByGodot
	fun openSubscriptions(productId: String) {
		if (activity == null) {
			return
		}
		val uri = if (productId.isEmpty()) {
			"https://play.google.com/store/account/subscriptions".toUri()
		} else {
			"https://play.google.com/store/account/subscriptions?sku=$productId&package=${activity!!.packageName}".toUri()
		}
		val intent = Intent(Intent.ACTION_VIEW, uri)
		activity!!.startActivity(intent)
	}
}
