/*************************************************************************/
/*  GooglePlayBillingUtils.java                                                    */
/*************************************************************************/
/*                       This file is part of:                           */
/*                           GODOT ENGINE                                */
/*                      https://godotengine.org                          */
/*************************************************************************/
/* Copyright (c) 2007-2020 Juan Linietsky, Ariel Manzur.                 */
/* Copyright (c) 2014-2020 Godot Engine contributors (cf. AUTHORS.md).   */
/*                                                                       */
/* Permission is hereby granted, free of charge, to any person obtaining */
/* a copy of this software and associated documentation files (the       */
/* "Software"), to deal in the Software without restriction, including   */
/* without limitation the rights to use, copy, modify, merge, publish,   */
/* distribute, sublicense, and/or sell copies of the Software, and to    */
/* permit persons to whom the Software is furnished to do so, subject to */
/* the following conditions:                                             */
/*                                                                       */
/* The above copyright notice and this permission notice shall be        */
/* included in all copies or substantial portions of the Software.       */
/*                                                                       */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */
/*************************************************************************/

package org.godotengine.godot.plugin.googleplaybilling.utils;

import org.godotengine.godot.Dictionary;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import java.util.ArrayList;
import java.util.List;

public class GooglePlayBillingUtils {
	public static Dictionary convertPurchaseToDictionary(Purchase purchase) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("original_json", purchase.getOriginalJson());
		dictionary.put("order_id", purchase.getOrderId());
		dictionary.put("package_name", purchase.getPackageName());
		dictionary.put("purchase_state", purchase.getPurchaseState());
		dictionary.put("purchase_time", purchase.getPurchaseTime());
		dictionary.put("purchase_token", purchase.getPurchaseToken());
		dictionary.put("quantity", purchase.getQuantity());
		dictionary.put("signature", purchase.getSignature());
		// PBL V4 replaced getSku with getSkus to support multi-sku purchases,
		// use the first entry for "sku" and generate an array for "skus"
		String[] products = purchase.getProducts().toArray(new String[0]);
		dictionary.put("products", products);
		dictionary.put("is_acknowledged", purchase.isAcknowledged());
		dictionary.put("is_auto_renewing", purchase.isAutoRenewing());
		return dictionary;
	}

	public static Dictionary convertProductDetailsToDictionary(ProductDetails details) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("id", details.getProductId());
		dictionary.put("title", details.getTitle());
		dictionary.put("description", details.getDescription());
		dictionary.put("type", details.getProductType());

		ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails = details.getOneTimePurchaseOfferDetails();
		if (oneTimePurchaseOfferDetails != null) {
			Dictionary oneTimePurchaseDetailsDictionary = new Dictionary();
			oneTimePurchaseDetailsDictionary.put("formatted_price", oneTimePurchaseOfferDetails.getFormattedPrice());
			oneTimePurchaseDetailsDictionary.put("price_amount_micros", oneTimePurchaseOfferDetails.getPriceAmountMicros());
			oneTimePurchaseDetailsDictionary.put("price_currency_code", oneTimePurchaseOfferDetails.getPriceCurrencyCode());
			dictionary.put("one_time_purchase_details", oneTimePurchaseDetailsDictionary);
		}

		List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList = details.getSubscriptionOfferDetails();

		if (subscriptionOfferDetailsList != null && !subscriptionOfferDetailsList.isEmpty()) {
			ArrayList<Dictionary> subscriptionOfferDetailsDictionaryList = new ArrayList<>();

			for (ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails : subscriptionOfferDetailsList) {
				Dictionary subscriptionOfferDetailsDictionary = new Dictionary();
				subscriptionOfferDetailsDictionary.put("offer_token", subscriptionOfferDetails.getOfferToken());
				subscriptionOfferDetailsDictionary.put("offer_tags", subscriptionOfferDetails.getOfferTags());

				ArrayList<Dictionary> pricingPhasesDictionaryList = new ArrayList<>();
				for (ProductDetails.PricingPhase pricingPhase : subscriptionOfferDetails.getPricingPhases().getPricingPhaseList()) {
					Dictionary pricingPhasesDictionary = new Dictionary();
					pricingPhasesDictionary.put("billing_cycle_count", pricingPhase.getBillingCycleCount());
					pricingPhasesDictionary.put("billing_period", pricingPhase.getBillingPeriod());
					pricingPhasesDictionary.put("formatted_price", pricingPhase.getFormattedPrice());
					pricingPhasesDictionary.put("price_amount_micros", pricingPhase.getPriceAmountMicros());
					pricingPhasesDictionary.put("price_currency_code", pricingPhase.getPriceCurrencyCode());
					pricingPhasesDictionary.put("recurrence_mode", pricingPhase.getRecurrenceMode());
					pricingPhasesDictionaryList.add(pricingPhasesDictionary);
				}

				subscriptionOfferDetailsDictionary.put("pricing_phases", pricingPhasesDictionaryList);

				subscriptionOfferDetailsDictionaryList.add(subscriptionOfferDetailsDictionary);
			}

			dictionary.put("subscription_offer_details", subscriptionOfferDetailsDictionaryList);
		}

		return dictionary;
	}

	public static Object[] convertPurchaseListToDictionaryObjectArray(List<Purchase> purchases) {
		Object[] purchaseDictionaries = new Object[purchases.size()];

		for (int i = 0; i < purchases.size(); i++) {
			purchaseDictionaries[i] = GooglePlayBillingUtils.convertPurchaseToDictionary(purchases.get(i));
		}

		return purchaseDictionaries;
	}

	public static Object[] convertProductDetailsListToDictionaryObjectArray(List<ProductDetails> productDetails) {
		Object[] skuDetailsDictionaries = new Object[productDetails.size()];

		for (int i = 0; i < productDetails.size(); i++) {
			skuDetailsDictionaries[i] = GooglePlayBillingUtils.convertProductDetailsToDictionary(productDetails.get(i));
		}

		return skuDetailsDictionaries;
	}
}
