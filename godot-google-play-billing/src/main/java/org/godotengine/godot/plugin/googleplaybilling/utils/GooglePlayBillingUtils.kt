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

package org.godotengine.godot.plugin.googleplaybilling.utils

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import org.godotengine.godot.Dictionary

object GooglePlayBillingUtils {
    private fun convertPurchaseToDictionary(purchase: Purchase): Dictionary {
        val dictionary = Dictionary()
        dictionary["order_id"] = purchase.orderId
        dictionary["package_name"] = purchase.packageName
        dictionary["purchase_state"] = purchase.purchaseState
        dictionary["purchase_time"] = purchase.purchaseTime
        dictionary["purchase_token"] = purchase.purchaseToken
        dictionary["quantity"] = purchase.quantity
        dictionary["signature"] = purchase.signature
        // PBL V4 replaced getSku with getSkus to support multi-sku purchases,
        // use the first entry for "sku" and generate an array for "skus"
        val skus = purchase.skus
        dictionary["sku"] = skus[0]
        val skusArray = skus.toTypedArray()
        dictionary["skus"] = skusArray
        dictionary["is_acknowledged"] = purchase.isAcknowledged
        dictionary["is_auto_renewing"] = purchase.isAutoRenewing
        return dictionary
    }

    private fun convertSkuDetailsToDictionary(details: SkuDetails): Dictionary {
        val dictionary = Dictionary()
        dictionary["sku"] = details.sku
        dictionary["title"] = details.title
        dictionary["description"] = details.description
        dictionary["price"] = details.price
        dictionary["price_currency_code"] = details.priceCurrencyCode
        dictionary["price_amount_micros"] = details.priceAmountMicros
        dictionary["free_trial_period"] = details.freeTrialPeriod
        dictionary["icon_url"] = details.iconUrl
        dictionary["introductory_price"] = details.introductoryPrice
        dictionary["introductory_price_amount_micros"] = details.introductoryPriceAmountMicros
        dictionary["introductory_price_cycles"] = details.introductoryPriceCycles
        dictionary["introductory_price_period"] = details.introductoryPricePeriod
        dictionary["original_price"] = details.originalPrice
        dictionary["original_price_amount_micros"] = details.originalPriceAmountMicros
        dictionary["subscription_period"] = details.subscriptionPeriod
        dictionary["type"] = details.type
        return dictionary
    }

    @JvmStatic
    fun convertPurchaseListToDictionaryObjectArray(purchases: List<Purchase>): Array<Any?> {
        val purchaseDictionaries = arrayOfNulls<Any>(purchases.size)
        for (i in purchases.indices) {
            purchaseDictionaries[i] = convertPurchaseToDictionary(purchases[i])
        }
        return purchaseDictionaries
    }

    @JvmStatic
    fun convertSkuDetailsListToDictionaryObjectArray(skuDetails: List<SkuDetails>): Array<Any?> {
        val skuDetailsDictionaries = arrayOfNulls<Any>(skuDetails.size)
        for (i in skuDetails.indices) {
            skuDetailsDictionaries[i] = convertSkuDetailsToDictionary(
                skuDetails[i]
            )
        }
        return skuDetailsDictionaries
    }
}