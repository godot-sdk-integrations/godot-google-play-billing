/*************************************************************************/
/*  GodotGooglePlayBilling.java                                          */
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

package org.godotengine.godot.plugin.googleplaybilling;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.googleplaybilling.utils.GooglePlayBillingUtils;
import org.godotengine.godot.plugin.UsedByGodot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GodotGooglePlayBilling extends GodotPlugin implements PurchasesUpdatedListener, BillingClientStateListener {

	private final BillingClient billingClient;
	// Billing 8 removed SkuDetails. Keep the legacy cache/API names, but store ProductDetails keyed by sku.
	private final HashMap<String, ProductDetails> skuDetailsCache = new HashMap<>(); // sku -> ProductDetails
	private boolean calledStartConnection;
	private String obfuscatedAccountId;
	private String obfuscatedProfileId;

	public GodotGooglePlayBilling(Godot godot) {
		super(godot);

		PendingPurchasesParams pendingPurchasesParams =
			PendingPurchasesParams.newBuilder().enableOneTimeProducts().build();

		billingClient = BillingClient
								.newBuilder(getActivity())
								.enablePendingPurchases(pendingPurchasesParams)
								.enableAutoServiceReconnection()
								.setListener(this)
								.build();
		calledStartConnection = false;
		obfuscatedAccountId = "";
		obfuscatedProfileId = "";
	}

	public void startConnection() {
		calledStartConnection = true;
		billingClient.startConnection(this);
	}

	public void endConnection() {
		billingClient.endConnection();
	}
	@UsedByGodot
	public boolean isReady() {
		return this.billingClient.isReady();
	}
	@UsedByGodot
	public int getConnectionState() {
		return billingClient.getConnectionState();
	}

	@UsedByGodot
	public void queryPurchases(String type) {
		QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
				.setProductType(type)
				.build();

		billingClient.queryPurchasesAsync(params, new PurchasesResponseListener() {
			@Override
			public void onQueryPurchasesResponse(BillingResult billingResult,
					List<Purchase> purchaseList) {
				Dictionary returnValue = new Dictionary();
				if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
					returnValue.put("status", 0); // OK = 0
					returnValue.put("purchases", GooglePlayBillingUtils.convertPurchaseListToDictionaryObjectArray(purchaseList));
				} else {
					returnValue.put("status", 1); // FAILED = 1
					returnValue.put("response_code", billingResult.getResponseCode());
					returnValue.put("debug_message", billingResult.getDebugMessage());
				}
				emitSignal("query_purchases_response", (Object)returnValue);
			}
		});
	}
	@UsedByGodot
	public void querySkuDetails(final String[] list, String type) {
		// Billing 8 queries ProductDetails. The Godot 3 API still exposes these ids as skus.
		List<QueryProductDetailsParams.Product> skuList = new ArrayList<>();
		for (String sku : list) {
			skuList.add(QueryProductDetailsParams.Product.newBuilder()
					.setProductId(sku)
					.setProductType(type)
					.build());
		}

		QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
				.setProductList(skuList)
				.build();

		billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
			@Override
			public void onProductDetailsResponse(BillingResult billingResult,
					QueryProductDetailsResult queryProductDetailsResult) {
				if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
					List<ProductDetails> skuDetailsList = queryProductDetailsResult.getProductDetailsList();
					for (ProductDetails skuDetails : skuDetailsList) {
						skuDetailsCache.put(skuDetails.getProductId(), skuDetails);
					}
					emitSignal("sku_details_query_completed", (Object)GooglePlayBillingUtils.convertSkuDetailsListToDictionaryObjectArray(skuDetailsList));
				} else {
					emitSignal("sku_details_query_error", billingResult.getResponseCode(), billingResult.getDebugMessage(), list);
				}
			}
		});
	}
	@UsedByGodot
	public void acknowledgePurchase(final String purchaseToken) {
		AcknowledgePurchaseParams acknowledgePurchaseParams =
				AcknowledgePurchaseParams.newBuilder()
						.setPurchaseToken(purchaseToken)
						.build();
		billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
			@Override
			public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
				if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
					emitSignal("purchase_acknowledged", purchaseToken);
				} else {
					emitSignal("purchase_acknowledgement_error", billingResult.getResponseCode(), billingResult.getDebugMessage(), purchaseToken);
				}
			}
		});
	}

	@UsedByGodot
	public void consumePurchase(String purchaseToken) {
		ConsumeParams consumeParams = ConsumeParams.newBuilder()
											  .setPurchaseToken(purchaseToken)
											  .build();

		billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
			@Override
			public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
				if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
					emitSignal("purchase_consumed", purchaseToken);
				} else {
					emitSignal("purchase_consumption_error", billingResult.getResponseCode(), billingResult.getDebugMessage(), purchaseToken);
				}
			}
		});
	}

	@Override
	public void onBillingSetupFinished(BillingResult billingResult) {
		if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
			emitSignal("connected");
		} else {
			emitSignal("connect_error", billingResult.getResponseCode(), billingResult.getDebugMessage());
		}
	}

	@Override
	public void onBillingServiceDisconnected() {
		emitSignal("disconnected");
	}
	@UsedByGodot
	public Dictionary confirmPriceChange(String sku) {
		if (!skuDetailsCache.containsKey(sku)) {
			Dictionary returnValue = new Dictionary();
			returnValue.put("status", 1); // FAILED = 1
			returnValue.put("response_code", null); // Null since there is no ResponseCode to return but to keep the interface (status, response_code, debug_message)
			returnValue.put("debug_message", "You must query the sku details and wait for the result before confirming a price change!");
			return returnValue;
		}

		Dictionary returnValue = new Dictionary();
		returnValue.put("status", 0); // OK = 0
		return returnValue;
	}
	@UsedByGodot
	public Dictionary purchase(String sku) {
		return purchaseInternal("", sku, 
			BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.UNKNOWN_REPLACEMENT_MODE);
	}
	@UsedByGodot
	public Dictionary updateSubscription(String oldToken, String sku, int replacementMode) {
		return purchaseInternal(oldToken, sku, replacementMode);
	}

	private Dictionary purchaseInternal(String oldToken, String sku, int replacementMode) {
		if (!skuDetailsCache.containsKey(sku)) {
			Dictionary returnValue = new Dictionary();
			returnValue.put("status", 1); // FAILED = 1
			returnValue.put("response_code", null); // Null since there is no ResponseCode to return but to keep the interface (status, response_code, debug_message)
			returnValue.put("debug_message", "You must query the sku details and wait for the result before purchasing!");
			return returnValue;
		}

		ProductDetails skuDetails = skuDetailsCache.get(sku);
		// Billing 8 launches purchases with ProductDetailsParams instead of setSkuDetails.
		// Pick the default offer token so the legacy purchase(sku) method still works.
		BillingFlowParams.ProductDetailsParams.Builder skuDetailsParamsBuilder =
				BillingFlowParams.ProductDetailsParams.newBuilder()
						.setProductDetails(skuDetails);

		if (BillingClient.ProductType.SUBS.equals(skuDetails.getProductType())) {
			List<ProductDetails.SubscriptionOfferDetails> offers = skuDetails.getSubscriptionOfferDetails();
			if (offers != null && !offers.isEmpty()) {
				ProductDetails.SubscriptionOfferDetails selectedOffer = offers.get(0);
				// Billing 8 uses a null offer id for the regular base plan. Prefer it for the legacy purchase(sku) path.
				for (ProductDetails.SubscriptionOfferDetails offer : offers) {
					if (offer.getOfferId() == null) {
						selectedOffer = offer;
						break;
					}
				}
				String offerToken = selectedOffer.getOfferToken();
				if (offerToken != null && !offerToken.isEmpty()) {
					skuDetailsParamsBuilder.setOfferToken(offerToken);
				}
			}
		} else {
			ProductDetails.OneTimePurchaseOfferDetails offer = skuDetails.getOneTimePurchaseOfferDetails();
			if (offer != null) {
				String offerToken = offer.getOfferToken();
				if (offerToken != null && !offerToken.isEmpty()) {
					skuDetailsParamsBuilder.setOfferToken(offerToken);
				}
			}
		}

		BillingFlowParams.Builder purchaseParamsBuilder = BillingFlowParams.newBuilder();
		purchaseParamsBuilder.setProductDetailsParamsList(Arrays.asList(skuDetailsParamsBuilder.build()));
		if (!obfuscatedAccountId.isEmpty()) {
			purchaseParamsBuilder.setObfuscatedAccountId(obfuscatedAccountId);
		}
		if (!obfuscatedProfileId.isEmpty()) {
			purchaseParamsBuilder.setObfuscatedProfileId(obfuscatedProfileId);
		}
		if (!oldToken.isEmpty() && replacementMode != BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.UNKNOWN_REPLACEMENT_MODE) {
			BillingFlowParams.SubscriptionUpdateParams updateParams =
				BillingFlowParams.SubscriptionUpdateParams.newBuilder()
					.setOldPurchaseToken(oldToken)
					.setSubscriptionReplacementMode(replacementMode)
					.build();
			purchaseParamsBuilder.setSubscriptionUpdateParams(updateParams);
		}
		BillingResult result = billingClient.launchBillingFlow(getActivity(), purchaseParamsBuilder.build());

		Dictionary returnValue = new Dictionary();
		if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
			returnValue.put("status", 0); // OK = 0
		} else {
			returnValue.put("status", 1); // FAILED = 1
			returnValue.put("response_code", result.getResponseCode());
			returnValue.put("debug_message", result.getDebugMessage());
		}

		return returnValue;
	}
	@UsedByGodot
	public void setObfuscatedAccountId(String accountId) {
		obfuscatedAccountId = accountId;
	}
	@UsedByGodot
	public void setObfuscatedProfileId(String profileId) {
		obfuscatedProfileId = profileId;
	}

	@Override
	public void onPurchasesUpdated(final BillingResult billingResult, @Nullable final List<Purchase> list) {
		if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
			emitSignal("purchases_updated", (Object)GooglePlayBillingUtils.convertPurchaseListToDictionaryObjectArray(list));
		} else {
			emitSignal("purchase_error", billingResult.getResponseCode(), billingResult.getDebugMessage());
		}
	}

	@Override
	public void onMainResume() {
		if (calledStartConnection) {
			emitSignal("billing_resume");
		}
	}

	@NonNull
	@Override
	public String getPluginName() {
		return "GodotGooglePlayBilling";
	}

	@NonNull
	@Override
	public List<String> getPluginMethods() {
		return Arrays.asList("startConnection", "endConnection", "confirmPriceChange", "purchase", "updateSubscription", "querySkuDetails", "isReady", "getConnectionState", "queryPurchases", "acknowledgePurchase", "consumePurchase", "setObfuscatedAccountId", "setObfuscatedProfileId");
	}

	@NonNull
	@Override
	public Set<SignalInfo> getPluginSignals() {
		Set<SignalInfo> signals = new ArraySet<>();

		signals.add(new SignalInfo("connected"));
		signals.add(new SignalInfo("disconnected"));
		signals.add(new SignalInfo("billing_resume"));
		signals.add(new SignalInfo("connect_error", Integer.class, String.class));
		signals.add(new SignalInfo("purchases_updated", Object[].class));
		signals.add(new SignalInfo("query_purchases_response", Object.class));
		signals.add(new SignalInfo("purchase_error", Integer.class, String.class));
		signals.add(new SignalInfo("sku_details_query_completed", Object[].class));
		signals.add(new SignalInfo("sku_details_query_error", Integer.class, String.class, String[].class));
		signals.add(new SignalInfo("price_change_acknowledged", Integer.class));
		signals.add(new SignalInfo("purchase_acknowledged", String.class));
		signals.add(new SignalInfo("purchase_acknowledgement_error", Integer.class, String.class, String.class));
		signals.add(new SignalInfo("purchase_consumed", String.class));
		signals.add(new SignalInfo("purchase_consumption_error", Integer.class, String.class, String.class));

		return signals;
	}
}
