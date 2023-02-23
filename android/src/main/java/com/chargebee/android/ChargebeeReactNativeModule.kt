package com.chargebee.android

import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.GPErrorCode
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.CBProductIDResult
import com.chargebee.android.models.CBProduct
import com.chargebee.android.models.PurchaseResult
import com.chargebee.android.network.ReceiptDetail
import com.chargebee.android.utils.convertArrayToWritableArray
import com.chargebee.android.utils.convertListToWritableArray
import com.chargebee.android.utils.convertPurchaseResultToDictionary
import com.chargebee.android.utils.convertReadableArray
import com.facebook.react.bridge.*

class ChargebeeReactNativeModule internal constructor(context: ReactApplicationContext) :
  ChargebeeReactNativeSpec(context) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  override fun configure(site: String, publishableApiKey: String, sdkKey: String) {
    val packageName = currentActivity?.packageName ?: ""
    Chargebee.configure(site, publishableApiKey, true, sdkKey, packageName)
  }

  @ReactMethod
  override fun retrieveProductIdentifiers(queryParams: ReadableMap, promise: Promise) {
    val formattedQueryParams = getFormattedQueryParams(queryParams)
    CBPurchase.retrieveProductIdentifers(formattedQueryParams) {
      when (it) {
        is CBProductIDResult.ProductIds -> {
          promise.resolve(convertArrayToWritableArray(it.IDs.toArray()))
        }
        is CBProductIDResult.Error -> {
          promise.reject(it.exp.message, it.exp)
        }
      }
    }
  }

  @ReactMethod
  override fun retrieveProducts(productIds: ReadableArray, promise: Promise) {
    val activity = currentActivity
    activity?.let {
      CBPurchase.retrieveProducts(it, convertReadableArray(productIds),
        object : CBCallback.ListProductsCallback<ArrayList<CBProduct>> {
          override fun onSuccess(productDetails: ArrayList<CBProduct>) {
            promise.resolve(convertListToWritableArray(productDetails))
          }

          override fun onError(error: CBException) {
            promise.reject(error.message, error)
          }
        })
    }
  }

  @ReactMethod
  override fun purchaseProduct(productId: String, customerId: String, promise: Promise) {
    val activity = currentActivity
    activity?.let {
      val productIds = arrayListOf(productId)
      CBPurchase.retrieveProducts(it, productIds,
        object : CBCallback.ListProductsCallback<ArrayList<CBProduct>> {
          override fun onSuccess(productDetails: ArrayList<CBProduct>) {
            if(productDetails.size > 0) {
              CBPurchase.purchaseProduct(
                productDetails.first(),
                customerId,
                object : CBCallback.PurchaseCallback<String> {

                  override fun onSuccess(receiptDetail: ReceiptDetail, status: Boolean) {
                    val purchaseResult = PurchaseResult(receiptDetail, status)
                    promise.resolve(convertPurchaseResultToDictionary(purchaseResult, status))
                  }

                  override fun onError(error: CBException) {
                    promise.reject(error.message, error)
                  }
                })
            } else {
              val productNotAvailable = CBException(ErrorDetail(GPErrorCode.ProductUnavailable.errorMsg))
              promise.reject(productNotAvailable.message, productNotAvailable)
            }

          }
          override fun onError(error: CBException) {
            promise.reject(error.message, error)
          }
        })
    }
  }

  private fun getFormattedQueryParams(queryParams: ReadableMap): Array<String> {
    if (queryParams != null)
      return arrayOf(queryParams.getString("limit") ?: "")
    return arrayOf("")
  }

  companion object {
    const val NAME = "ChargebeeReactNative"
  }
}


