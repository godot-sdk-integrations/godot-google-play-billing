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

import com.android.billingclient.api.BillingClient;
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
		// PBL V4 replaced getSku with getSkus to support multi-sku purchases.
		// PBL V7 replaced getSkus with getProducts.
		List<String> skus = purchase.getProducts();
		dictionary.put("sku", skus.isEmpty() ? "" : skus.get(0));
		String[] skusArray = skus.toArray(new String[0]);
		dictionary.put("skus", skusArray);
		dictionary.put("is_acknowledged", purchase.isAcknowledged());
		dictionary.put("is_auto_renewing", purchase.isAutoRenewing());
		return dictionary;
	}

	public static Dictionary convertSkuDetailsToDictionary(ProductDetails details) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("sku", details.getProductId());
		dictionary.put("title", details.getTitle());
		dictionary.put("description", details.getDescription());
		// ProductDetails no longer exposes every flattened SkuDetails field.
		// Keep the legacy dictionary keys and use old-type defaults when there is no direct equivalent.
		dictionary.put("price", "");
		dictionary.put("price_currency_code", "");
		dictionary.put("price_amount_micros", 0L);
		dictionary.put("free_trial_period", "");
		dictionary.put("icon_url", "");
		dictionary.put("introductory_price", "");
		dictionary.put("introductory_price_amount_micros", 0L);
		dictionary.put("introductory_price_cycles", 0);
		dictionary.put("introductory_price_period", "");
		dictionary.put("original_price", "");
		dictionary.put("original_price_amount_micros", 0L);
		dictionary.put("subscription_period", "");
		dictionary.put("type", details.getProductType());

		if (BillingClient.ProductType.INAPP.equals(details.getProductType())) {
			ProductDetails.OneTimePurchaseOfferDetails offer = details.getOneTimePurchaseOfferDetails();
			if (offer != null) {
				dictionary.put("price", offer.getFormattedPrice());
				dictionary.put("price_currency_code", offer.getPriceCurrencyCode());
				dictionary.put("price_amount_micros", offer.getPriceAmountMicros());
				Long fullPriceMicros = offer.getFullPriceMicros();
				if (fullPriceMicros != null) {
					dictionary.put("original_price_amount_micros", fullPriceMicros);
				}
			}
		} else if (BillingClient.ProductType.SUBS.equals(details.getProductType())) {
			List<ProductDetails.SubscriptionOfferDetails> offers = details.getSubscriptionOfferDetails();
			if (offers != null && !offers.isEmpty()) {
				ProductDetails.SubscriptionOfferDetails selectedOffer = offers.get(0);
				// Billing 8 uses a null offer id for the regular base plan. Prefer it for legacy sku details.
				for (ProductDetails.SubscriptionOfferDetails offer : offers) {
					if (offer.getOfferId() == null) {
						selectedOffer = offer;
						break;
					}
				}
				List<ProductDetails.PricingPhase> phases = selectedOffer.getPricingPhases().getPricingPhaseList();
				if (phases != null && !phases.isEmpty()) {
					ProductDetails.PricingPhase pricingPhase = phases.get(0);
					dictionary.put("price", pricingPhase.getFormattedPrice());
					dictionary.put("price_currency_code", pricingPhase.getPriceCurrencyCode());
					dictionary.put("price_amount_micros", pricingPhase.getPriceAmountMicros());
					dictionary.put("subscription_period", pricingPhase.getBillingPeriod());
				}
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

	public static Object[] convertSkuDetailsListToDictionaryObjectArray(List<ProductDetails> skuDetails) {
		Object[] skuDetailsDictionaries = new Object[skuDetails.size()];

		for (int i = 0; i < skuDetails.size(); i++) {
			skuDetailsDictionaries[i] = GooglePlayBillingUtils.convertSkuDetailsToDictionary(skuDetails.get(i));
		}

		return skuDetailsDictionaries;
	}
}
