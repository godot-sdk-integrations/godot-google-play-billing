/*************************************************************************/
/*  GooglePlayBillingUtils.java                                          */
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

		List<String> products = purchase.getProducts();
		dictionary.put("sku", products.get(0));
		String[] productsArray = products.toArray(new String[0]);
		dictionary.put("skus", productsArray);
		dictionary.put("is_acknowledged", purchase.isAcknowledged());
		dictionary.put("is_auto_renewing", purchase.isAutoRenewing());
		return dictionary;
	}

	public static Dictionary convertProductDetailsToDictionary(ProductDetails details) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("sku", details.getProductId());
		dictionary.put("title", details.getTitle());
		dictionary.put("description", details.getDescription());
		dictionary.put("type", details.getProductType());

		ProductDetails.OneTimePurchaseOfferDetails oneTimeOfferDetails = details.getOneTimePurchaseOfferDetails();
		List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = details.getSubscriptionOfferDetails();
		if (oneTimeOfferDetails != null) {
			dictionary.put("price", oneTimeOfferDetails.getFormattedPrice());
			dictionary.put("price_currency_code", oneTimeOfferDetails.getPriceCurrencyCode());
			dictionary.put("price_amount_micros", oneTimeOfferDetails.getPriceAmountMicros());
		} else if (subscriptionOfferDetails != null && !subscriptionOfferDetails.isEmpty()) {
			// Defaulting to first pricing phase of the primary subscription offer.
			ProductDetails.SubscriptionOfferDetails subDetails = subscriptionOfferDetails.get(0);
			if (!subDetails.getPricingPhases().getPricingPhaseList().isEmpty()) {
				ProductDetails.PricingPhase phase = subDetails.getPricingPhases().getPricingPhaseList().get(0);
				dictionary.put("price", phase.getFormattedPrice());
				dictionary.put("price_currency_code", phase.getPriceCurrencyCode());
				dictionary.put("price_amount_micros", phase.getPriceAmountMicros());
				dictionary.put("subscription_period", phase.getBillingPeriod());
			}
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

	public static Object[] convertProductDetailsListToDictionaryObjectArray(List<ProductDetails> productDetailsList) {
		Object[] productDetailsDictionaries = new Object[productDetailsList.size()];

		for (int i = 0; i < productDetailsList.size(); i++) {
			productDetailsDictionaries[i] = GooglePlayBillingUtils.convertProductDetailsToDictionary(productDetailsList.get(i));
		}

		return productDetailsDictionaries;
	}
}
