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

import com.android.billingclient.api.AccountIdentifiers;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PendingPurchaseUpdate;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails;
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails;
import com.android.billingclient.api.ProductDetails.InstallmentPlanDetails;
import com.android.billingclient.api.ProductDetails.PricingPhases;
import com.android.billingclient.api.ProductDetails.PricingPhase;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GooglePlayBillingUtils {
	public static void addProductDetailsByProductId(List<ProductDetails> allProductDetails, HashMap<String, ProductDetails> allProductDetailsByProductId) {
		if (allProductDetails == null) return;

		for (int i = 0; i < allProductDetails.size(); i++) {
			ProductDetails productDetails = allProductDetails.get(i);
			String productId = productDetails.getProductId();

			allProductDetailsByProductId.put(productId, productDetails);
		}
	}
	public static void addPurchasesByPurchaseToken(List<Purchase> allPurchases, HashMap<String, Purchase> allPurchasesByPurchaseToken) {
		if (allPurchases == null) return;

		for (int i = 0; i < allPurchases.size(); i++) {
			Purchase purchase = allPurchases.get(i);
			String purchaseToken = purchase.getPurchaseToken();

			allPurchasesByPurchaseToken.put(purchaseToken, purchase);
		}
	}
	public static Dictionary convertProductDetailsToGodotDictionary(HashMap<String, ProductDetails> allProductDetailsById) {
		Dictionary dictionary = new Dictionary();

		for (Map.Entry<String, ProductDetails> entry : allProductDetailsById.entrySet()) {
			dictionary.put(entry.getKey(), convertFromProductDetails(entry.getValue()));
		}

		return dictionary;
	}
	public static Object[] convertFromProductDetailsMap(HashMap<String, ProductDetails> allProductDetailsById) {
		Object[] allDictionaries = new Object[allProductDetailsById.size()];
		int i = 0;

		for (Map.Entry<String, ProductDetails> entry : allProductDetailsById.entrySet()) {
			allDictionaries[i] = convertFromProductDetails(entry.getValue());
			i++;
		}

		return allDictionaries;
	}
	public static Object[] convertFromProductDetailsArr(List<ProductDetails> allProductDetails) {
		if (allProductDetails == null) {
			return new Object[] {  };
		}

		Object[] allDictionaries = new Object[allProductDetails.size()];

		for (int i = 0; i < allDictionaries.length; i++) {
			allDictionaries[i] = convertFromProductDetails(allProductDetails.get(i));
		}

		return allDictionaries;
	}
	public static Dictionary convertFromProductDetails(ProductDetails productDetails) {
		Dictionary dictionary = new Dictionary();

		dictionary.put("description", productDetails.getDescription()); // String
		dictionary.put("name", productDetails.getName()); // String
		dictionary.put("one_time_purchase_offer_details", convertFromOneTimePurchaseOfferDetails(productDetails.getOneTimePurchaseOfferDetails())); // Dictionary
		dictionary.put("product_id", productDetails.getProductId()); // String
		dictionary.put("product_type", productDetails.getProductType()); // String
		dictionary.put("subscription_offer_details", convertFromSubscriptionOfferDetailsArr(productDetails.getSubscriptionOfferDetails())); // Godot Array of Dictionaries
		dictionary.put("title", productDetails.getTitle()); // String

		return dictionary;
	}
	public static Dictionary convertFromOneTimePurchaseOfferDetails(OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails) {
		Dictionary dictionary = new Dictionary();

		// Developer docs says it's possible for this to be null.
		if (oneTimePurchaseOfferDetails == null) {
			return dictionary;
		}

		dictionary.put("formatted_price", oneTimePurchaseOfferDetails.getFormattedPrice()); // String
		dictionary.put("price_amount_micros", oneTimePurchaseOfferDetails.getPriceAmountMicros()); // long
		dictionary.put("price_currency_code", oneTimePurchaseOfferDetails.getPriceCurrencyCode()); // String
		return dictionary;
	}
	public static Object[] convertFromSubscriptionOfferDetailsArr(List<SubscriptionOfferDetails> subscriptionOfferDetails) {
		// Developer docs says it's possible for this to be null.
		if (subscriptionOfferDetails == null) {
			return new Object[] {  };
		}

		Object[] allDictionaries = new Object[subscriptionOfferDetails.size()];

		for (int i = 0; i < allDictionaries.length; i++) {
			allDictionaries[i] = convertFromSubscriptionOfferDetails(subscriptionOfferDetails.get(i));
		}

		return allDictionaries;
	}
	public static Dictionary convertFromSubscriptionOfferDetails(SubscriptionOfferDetails subscriptionOfferDetails) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("base_plan_id", subscriptionOfferDetails.getBasePlanId()); // String
		dictionary.put("installment_plan_details", convertFromInstallmentPlanDetails(subscriptionOfferDetails.getInstallmentPlanDetails())); // Dictionary
		dictionary.put("offer_id", subscriptionOfferDetails.getOfferId()); // String
		dictionary.put("offer_tags", subscriptionOfferDetails.getOfferTags().toArray()); // String[]
		dictionary.put("offer_token", subscriptionOfferDetails.getOfferToken()); // String
		dictionary.put("pricing_phases", convertFromPricingPhases(subscriptionOfferDetails.getPricingPhases())); // Dictionary
		return dictionary;
	}
	public static Dictionary convertFromInstallmentPlanDetails(InstallmentPlanDetails installmentPlanDetails) {
		Dictionary dictionary = new Dictionary();

		if (installmentPlanDetails == null) {
			return dictionary;
		}

		dictionary.put("installment_plan_commitment_payments_count", installmentPlanDetails.getInstallmentPlanCommitmentPaymentsCount()); // int
		dictionary.put("subsequent_installment_plan_commitment_payments_count", installmentPlanDetails.getSubsequentInstallmentPlanCommitmentPaymentsCount()); // int
		return dictionary;
	}
	public static Dictionary convertFromPricingPhases(PricingPhases pricingPhases) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("pricing_phase_list", convertFromPricingPhaseArr(pricingPhases.getPricingPhaseList())); // Array of Dictionaries
		return dictionary;
	}
	public static Object[] convertFromPricingPhaseArr(List<PricingPhase> allPricingPhases) {
		if (allPricingPhases == null) {
			return new Object[] {  };
		}

		Object[] allDictionaries = new Object[allPricingPhases.size()];

		for (int i = 0; i < allDictionaries.length; i++) {
			allDictionaries[i] = convertFromPricingPhase(allPricingPhases.get(i));
		}

		return allDictionaries;
	}
	public static Dictionary convertFromPricingPhase(PricingPhase pricingPhase) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("billing_cycle_count", pricingPhase.getBillingCycleCount()); // int
		dictionary.put("billing_period", pricingPhase.getBillingPeriod()); // String
		dictionary.put("formatted_price", pricingPhase.getFormattedPrice()); // String
		dictionary.put("price_amount_micros", pricingPhase.getPriceAmountMicros()); // long
		dictionary.put("price_currency_code", pricingPhase.getPriceCurrencyCode()); // String
		dictionary.put("recurrence_mode", pricingPhase.getRecurrenceMode()); // int
		return dictionary;
	}
	public static Dictionary convertFromBillingResult(BillingResult billingResult) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("debug_message", billingResult.getDebugMessage()); // String
		dictionary.put("response_code", billingResult.getResponseCode()); // int
		return dictionary;
	}
	public static Dictionary convertPurchaseToGodotDictionary(HashMap<String, Purchase> allPurchasesById) {
		Dictionary dictionary = new Dictionary();

		for (Map.Entry<String, Purchase> entry : allPurchasesById.entrySet()) {
			dictionary.put(entry.getKey(), convertFromPurchase(entry.getValue()));
		}

		return dictionary;
	}
	public static Object[] convertFromPurchaseMap(HashMap<String, Purchase> allPurchasesById) {
		Object[] allDictionaries = new Object[allPurchasesById.size()];
		int i = 0;

		for (Map.Entry<String, Purchase> entry : allPurchasesById.entrySet()) {
			allDictionaries[i] = convertFromPurchase(entry.getValue());
			i++;
		}

		return allDictionaries;
	}
	public static Object[] convertFromPurchaseArr(List<Purchase> allPurchases) {
		if (allPurchases == null) {
			return new Object[] {  };
		}

		Object[] allDictionaries = new Object[allPurchases.size()];

		for (int i = 0; i < allDictionaries.length; i++) {
			allDictionaries[i] = convertFromPurchase(allPurchases.get(i));
		}

		return allDictionaries;
	}
	public static Dictionary convertFromPurchase(Purchase purchase) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("account_identifiers", convertFromAccountIdentifiers(purchase.getAccountIdentifiers())); // Dictionary
		dictionary.put("developer_payload", purchase.getDeveloperPayload()); // String
		dictionary.put("order_id", purchase.getOrderId()); // String
		dictionary.put("original_json", purchase.getOriginalJson()); // String
		dictionary.put("package_name", purchase.getPackageName()); // String
		dictionary.put("pending_purchase_update", convertFromPendingPurchaseUpdate(purchase.getPendingPurchaseUpdate())); // Dictionary
		dictionary.put("products", purchase.getProducts().toArray()); // String[]
		dictionary.put("purchase_state", purchase.getPurchaseState()); // int
		dictionary.put("purchase_time", purchase.getPurchaseTime()); // long
		dictionary.put("purchase_token", purchase.getPurchaseToken()); // String
		dictionary.put("quantity", purchase.getQuantity()); // int
		dictionary.put("signature", purchase.getSignature()); // String
		dictionary.put("is_acknowledged", purchase.isAcknowledged()); // boolean
		dictionary.put("is_auto_renewing", purchase.isAutoRenewing()); // boolean
		return dictionary;
	}
	public static Dictionary convertFromAccountIdentifiers(AccountIdentifiers accountIdentifiers) {
		Dictionary dictionary = new Dictionary();

		if (accountIdentifiers == null) {
			return dictionary;
		}

		dictionary.put("obfuscated_account_id", accountIdentifiers.getObfuscatedAccountId()); // String
		dictionary.put("obfuscated_profile_id", accountIdentifiers.getObfuscatedProfileId()); // String
		return dictionary;
	}
	public static Dictionary convertFromPendingPurchaseUpdate(PendingPurchaseUpdate pendingPurchaseUpdate) {
		Dictionary dictionary = new Dictionary();

		if (pendingPurchaseUpdate == null) {
			return dictionary;
		}

		dictionary.put("products", pendingPurchaseUpdate.getProducts().toArray()); // String[]
		dictionary.put("purchase_token", pendingPurchaseUpdate.getPurchaseToken()); // String
		return dictionary;
	}
}