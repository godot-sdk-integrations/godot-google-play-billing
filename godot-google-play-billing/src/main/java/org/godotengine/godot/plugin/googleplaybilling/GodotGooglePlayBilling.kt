/*************************************************************************/
/*  GodotGooglePlayBilling.kt                                                    */
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

package org.godotengine.godot.plugin.googleplaybilling

import androidx.collection.ArraySet
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import org.godotengine.godot.plugin.googleplaybilling.utils.GooglePlayBillingUtils.convertPurchaseListToDictionaryObjectArray
import org.godotengine.godot.plugin.googleplaybilling.utils.GooglePlayBillingUtils.convertSkuDetailsListToDictionaryObjectArray
import java.util.*

class GodotGooglePlayBilling(godot: Godot?) : GodotPlugin(godot), PurchasesUpdatedListener,
    BillingClientStateListener, PriceChangeConfirmationListener {
    private val billingClient: BillingClient = BillingClient
        .newBuilder(activity!!)
        .enablePendingPurchases()
        .setListener(this)
        .build()
    private val skuDetailsCache = HashMap<String, SkuDetails>() // sku → SkuDetails
    private var obfuscatedAccountId: String = ""
    private var obfuscatedProfileId: String = ""

    @UsedByGodot
    fun startConnection() {
        billingClient.startConnection(this)
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
    fun queryPurchases(type: String?) {
        billingClient.queryPurchasesAsync(type!!) { billingResult, purchaseList ->
            val returnValue = Dictionary()
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                returnValue["status"] = 0 // OK = 0
                returnValue["purchases"] = convertPurchaseListToDictionaryObjectArray(purchaseList)
            } else {
                returnValue["status"] = 1 // FAILED = 1
                returnValue["response_code"] = billingResult.responseCode
                returnValue["debug_message"] = billingResult.debugMessage
            }
            emitSignal("query_purchases_response", returnValue as Any)
        }
    }

    @UsedByGodot
    fun querySkuDetails(list: Array<String?>, type: String?) {
        val skuList = listOf(*list)
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(type!!)
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (skuDetails in skuDetailsList!!) {
                    skuDetailsCache[skuDetails.sku] = skuDetails
                }
                emitSignal(
                    "sku_details_query_completed", convertSkuDetailsListToDictionaryObjectArray(
                        skuDetailsList
                    ) as Any
                )
            } else {
                emitSignal(
                    "sku_details_query_error",
                    billingResult.responseCode,
                    billingResult.debugMessage,
                    list
                )
            }
        }
    }

    @UsedByGodot
    fun acknowledgePurchase(purchaseToken: String?) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken!!)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                emitSignal("purchase_acknowledged", purchaseToken)
            } else {
                emitSignal(
                    "purchase_acknowledgement_error",
                    billingResult.responseCode,
                    billingResult.debugMessage,
                    purchaseToken
                )
            }
        }
    }

    @UsedByGodot
    fun consumePurchase(purchaseToken: String) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.consumeAsync(consumeParams) { billingResult, token ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                emitSignal("purchase_consumed", token)
            } else {
                emitSignal(
                    "purchase_consumption_error",
                    billingResult.responseCode,
                    billingResult.debugMessage,
                    token
                )
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            emitSignal("connected")
        } else {
            emitSignal("connect_error", billingResult.responseCode, billingResult.debugMessage)
        }
    }

    override fun onBillingServiceDisconnected() {
        emitSignal("disconnected")
    }

    @UsedByGodot
    fun confirmPriceChange(sku: String): Dictionary {
        if (!skuDetailsCache.containsKey(sku)) {
            val returnValue = Dictionary()
            returnValue["status"] = 1 // FAILED = 1
            returnValue["response_code"] =
                null // Null since there is no ResponseCode to return but to keep the interface (status, response_code, debug_message)
            returnValue["debug_message"] =
                "You must query the sku details and wait for the result before confirming a price change!"
            return returnValue
        }
        val skuDetails = skuDetailsCache[sku]
        val priceChangeFlowParams = PriceChangeFlowParams.newBuilder().setSkuDetails(
            skuDetails!!
        ).build()
        billingClient.launchPriceChangeConfirmationFlow(activity!!, priceChangeFlowParams, this)
        val returnValue = Dictionary()
        returnValue["status"] = 0 // OK = 0
        return returnValue
    }

    @UsedByGodot
    fun purchase(sku: String): Dictionary {
        return purchaseInternal(
            "", sku,
            BillingFlowParams.ProrationMode.UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY
        )
    }

    @UsedByGodot
    fun updateSubscription(oldToken: String, sku: String, prorationMode: Int): Dictionary {
        return purchaseInternal(oldToken, sku, prorationMode)
    }

    private fun purchaseInternal(oldToken: String, sku: String, prorationMode: Int): Dictionary {
        if (!skuDetailsCache.containsKey(sku)) {
            val returnValue = Dictionary()
            returnValue["status"] = 1 // FAILED = 1
            returnValue["response_code"] =
                null // Null since there is no ResponseCode to return but to keep the interface (status, response_code, debug_message)
            returnValue["debug_message"] =
                "You must query the sku details and wait for the result before purchasing!"
            return returnValue
        }
        val skuDetails = skuDetailsCache[sku]
        val purchaseParamsBuilder = BillingFlowParams.newBuilder()
        purchaseParamsBuilder.setSkuDetails(skuDetails!!)
        if (obfuscatedAccountId.isNotEmpty()) {
            purchaseParamsBuilder.setObfuscatedAccountId(obfuscatedAccountId)
        }
        if (obfuscatedProfileId.isNotEmpty()) {
            purchaseParamsBuilder.setObfuscatedProfileId(obfuscatedProfileId)
        }
        if (oldToken.isNotEmpty() && prorationMode != BillingFlowParams.ProrationMode.UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY) {
            val updateParams = SubscriptionUpdateParams.newBuilder()
                .setOldSkuPurchaseToken(oldToken)
                .setReplaceSkusProrationMode(prorationMode)
                .build()
            purchaseParamsBuilder.setSubscriptionUpdateParams(updateParams)
        }
        val result = billingClient.launchBillingFlow(activity!!, purchaseParamsBuilder.build())
        val returnValue = Dictionary()
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            returnValue["status"] = 0 // OK = 0
        } else {
            returnValue["status"] = 1 // FAILED = 1
            returnValue["response_code"] = result.responseCode
            returnValue["debug_message"] = result.debugMessage
        }
        return returnValue
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
            emitSignal("purchases_updated", convertPurchaseListToDictionaryObjectArray(list) as Any)
        } else {
            emitSignal("purchase_error", billingResult.responseCode, billingResult.debugMessage)
        }
    }

    override fun onPriceChangeConfirmationResult(billingResult: BillingResult) {
        emitSignal("price_change_acknowledged", billingResult.responseCode)
    }

    override fun onMainResume() {
        emitSignal("billing_resume")
    }

    override fun getPluginName(): String {
        return "GodotGooglePlayBilling"
    }

    override fun getPluginSignals(): Set<SignalInfo> {
        val signals: MutableSet<SignalInfo> = ArraySet()
        signals.add(SignalInfo("connected"))
        signals.add(SignalInfo("disconnected"))
        signals.add(SignalInfo("billing_resume"))
        signals.add(SignalInfo("connect_error", Int::class.java, String::class.java))
        signals.add(SignalInfo("purchases_updated", Array<Any>::class.java))
        signals.add(SignalInfo("query_purchases_response", Any::class.java))
        signals.add(SignalInfo("purchase_error", Int::class.java, String::class.java))
        signals.add(SignalInfo("sku_details_query_completed", Array<Any>::class.java))
        signals.add(
            SignalInfo(
                "sku_details_query_error",
                Int::class.java,
                String::class.java,
                Array<String>::class.java
            )
        )
        signals.add(SignalInfo("price_change_acknowledged", Int::class.java))
        signals.add(SignalInfo("purchase_acknowledged", String::class.java))
        signals.add(
            SignalInfo(
                "purchase_acknowledgement_error",
                Int::class.java,
                String::class.java,
                String::class.java
            )
        )
        signals.add(SignalInfo("purchase_consumed", String::class.java))
        signals.add(
            SignalInfo(
                "purchase_consumption_error",
                Int::class.java,
                String::class.java,
                String::class.java
            )
        )
        return signals
    }

}