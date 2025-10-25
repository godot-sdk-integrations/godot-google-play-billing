package org.godotengine.plugin.googleplaybilling

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.UnfetchedProduct

import org.godotengine.godot.Dictionary


object Utils {
	fun convertPurchaseListToArray(purchases: MutableList<Purchase>): Array<Any?> {
		val array = arrayOfNulls<Any>(purchases.size)
		for (i in purchases.indices) {
			array[i] = purchaseToDictionary(purchases[i])
		}

		return array
	}

	fun purchaseToDictionary(purchase: Purchase): Dictionary {
		val dict = Dictionary()
		dict["order_id"] = purchase.orderId
		dict["purchase_token"] = purchase.purchaseToken
		dict["package_name"] = purchase.packageName
		dict["purchase_state"] = purchase.purchaseState
		dict["purchase_time"] = purchase.purchaseTime
		dict["original_json"] = purchase.originalJson
		dict["is_acknowledged"] = purchase.isAcknowledged
		dict["is_auto_renewing"] = purchase.isAutoRenewing
		dict["quantity"] = purchase.quantity
		dict["signature"] = purchase.signature
		dict["product_ids"] = purchase.products.toTypedArray()
		return dict
	}

	fun convertUnfetchedProductListToArray(productList: MutableList<UnfetchedProduct>): Array<Any?> {
		val array = arrayOfNulls<Any>(productList.size)
		for (i in productList.indices) {
			array[i] = unfetchedProductToDictionary(productList[i])
		}

		return array
	}

	fun unfetchedProductToDictionary(product: UnfetchedProduct): Dictionary {
		val dict = Dictionary()
		dict["product_id"] = product.productId
		dict["product_type"] = product.productType
		dict["status_code"] = product.statusCode
		return dict
	}

	fun convertProductDetailsListToArray(detailsList: MutableList<ProductDetails>): Array<Any?> {
		val array = arrayOfNulls<Any>(detailsList.size)
		for (i in detailsList.indices) {
			array[i] = productDetailsToDictionary(detailsList[i])
		}

		return array
	}

	fun productDetailsToDictionary(details: ProductDetails): Dictionary {
		val dict = Dictionary()
		dict["product_id"] = details.productId
		dict["title"] = details.title
		dict["name"] = details.name
		dict["description"] = details.description
		dict["product_type"] = details.productType

		dict["one_time_purchase_offer_details"] = details.oneTimePurchaseOfferDetails?.let { oneTimeOfferToDict(it) }

		val subOffers = details.subscriptionOfferDetails
		if (!subOffers.isNullOrEmpty()) {
			val array = arrayOfNulls<Any>(subOffers.size)
			for (i in subOffers.indices) {
				array[i] = subscriptionOfferToDict(subOffers[i])
			}
			dict["subscription_offer_details"] = array
		} else {
			dict["subscription_offer_details"] = null
		}

		return dict
	}

	private fun oneTimeOfferToDict(offer: ProductDetails.OneTimePurchaseOfferDetails): Dictionary {
		val dict = Dictionary()
		dict["price_amount_micros"] = offer.priceAmountMicros
		dict["price_currency_code"] = offer.priceCurrencyCode
		dict["formatted_price"] = offer.formattedPrice
		return dict
	}

	private fun subscriptionOfferToDict(offer: ProductDetails.SubscriptionOfferDetails): Dictionary {
		val dict = Dictionary()
		dict["base_plan_id"] = offer.basePlanId
		dict["offer_id"] = offer.offerId
		dict["offer_token"] = offer.offerToken

		dict["installment_plan_details"] = offer.installmentPlanDetails?.let { installment ->
			Dictionary().apply {
				this["installment_plan_commitment_payments_count"] = installment.installmentPlanCommitmentPaymentsCount
				this["subsequent_installment_plan_commitment_payments_count"] = installment.subsequentInstallmentPlanCommitmentPaymentsCount
			}
		}

		val pricingPhaseList = offer.pricingPhases.pricingPhaseList
		val phasesArray = arrayOfNulls<Any>(pricingPhaseList.size)
		for (i in pricingPhaseList.indices) {
			val phase = pricingPhaseList[i]
			val dict = Dictionary()
			dict["price_amount_micros"] = phase.priceAmountMicros
			dict["price_currency_code"] = phase.priceCurrencyCode
			dict["formatted_price"] = phase.formattedPrice
			dict["billing_period"] = phase.billingPeriod
			dict["recurrence_mode"] = phase.recurrenceMode
			dict["billing_cycle_count"] = phase.billingCycleCount
			phasesArray[i] = dict
		}

		dict["pricing_phases"] = phasesArray
		dict["offer_tags"] = offer.offerTags.toTypedArray()

		return dict
	}

	fun createResultDict(responseCode: Int, debugMessage: String): Dictionary {
		val result = Dictionary()
		result["response_code"] = responseCode
		result["debug_message"] = debugMessage
		return result
	}
}
