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
		ArrayList<String> skus = purchase.getSkus();
		dictionary.put("sku", skus.get(0));
		String[] skusArray = skus.toArray(new String[0]);
		dictionary.put("skus", skusArray);
		dictionary.put("is_acknowledged", purchase.isAcknowledged());
		dictionary.put("is_auto_renewing", purchase.isAutoRenewing());
		return dictionary;
	}

	public static Dictionary convertSkuDetailsToDictionary(SkuDetails details) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("sku", details.getSku());
		dictionary.put("title", details.getTitle());
		dictionary.put("description", details.getDescription());
		dictionary.put("price", details.getPrice());
		dictionary.put("price_currency_code", details.getPriceCurrencyCode());
		dictionary.put("price_amount_micros", details.getPriceAmountMicros());
		dictionary.put("free_trial_period", details.getFreeTrialPeriod());
		dictionary.put("icon_url", details.getIconUrl());
		dictionary.put("introductory_price", details.getIntroductoryPrice());
		dictionary.put("introductory_price_amount_micros", details.getIntroductoryPriceAmountMicros());
		dictionary.put("introductory_price_cycles", details.getIntroductoryPriceCycles());
		dictionary.put("introductory_price_period", details.getIntroductoryPricePeriod());
		dictionary.put("original_price", details.getOriginalPrice());
		dictionary.put("original_price_amount_micros", details.getOriginalPriceAmountMicros());
		dictionary.put("subscription_period", details.getSubscriptionPeriod());
		dictionary.put("type", details.getType());
		return dictionary;
	}

	public static Object[] convertPurchaseListToDictionaryObjectArray(List<Purchase> purchases) {
		Object[] purchaseDictionaries = new Object[purchases.size()];

		for (int i = 0; i < purchases.size(); i++) {
			purchaseDictionaries[i] = GooglePlayBillingUtils.convertPurchaseToDictionary(purchases.get(i));
		}

		return purchaseDictionaries;
	}

	public static Object[] convertSkuDetailsListToDictionaryObjectArray(List<SkuDetails> skuDetails) {
		Object[] skuDetailsDictionaries = new Object[skuDetails.size()];

		for (int i = 0; i < skuDetails.size(); i++) {
			skuDetailsDictionaries[i] = GooglePlayBillingUtils.convertSkuDetailsToDictionary(skuDetails.get(i));
		}

		return skuDetailsDictionaries;
	}
}
