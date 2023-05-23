package com.lafimsize.inappbilling.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.lafimsize.inappbilling.util.Security
import kotlinx.coroutines.launch

class ShopViewModel:ViewModel() {

    val isPurchased=MutableLiveData<Int>()

    val productDetailsList=MutableLiveData<List<ProductDetails>>()
    val loading=MutableLiveData<Boolean>()
    val billingResult=MutableLiveData<BillingResult>()

    val connectionStatus=MutableLiveData<Boolean>()
    lateinit var billingClient:BillingClient

    fun initializeBillingClient(context: Context){

        val pUL=PurchasesUpdatedListener{billingResult,purchases->

            println("pullll tetiklenddiii")

            if(billingResult.responseCode==BillingResponseCode.OK && purchases!=null){

                for (i in purchases){

                    handlePurchaseConsumableProduct(i)

                }

            }

        }

        billingClient=BillingClient.newBuilder(context).enablePendingPurchases()
            .setListener(pUL).build()

        startConnection()


    }

    private fun startConnection(){

        billingClient.startConnection(object :BillingClientStateListener{
            override fun onBillingServiceDisconnected() {
                viewModelScope.launch {
                    connectionStatus.value=false
                }
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                viewModelScope.launch{
                    connectionStatus.value=true
                    initializeAllProducts()
                }
            }
        })

    }

    private fun initializeAllProducts(){


        loading.value=true

        val elmas30=
            QueryProductDetailsParams.Product.newBuilder().setProductId("elmas_30")
                .setProductType(ProductType.INAPP).build()
        val elmas50=
            QueryProductDetailsParams.Product.newBuilder().setProductId("elmas_50")
                .setProductType(ProductType.INAPP).build()

        val productList= listOf(elmas30,elmas50)

        val queryProductDetailsParams=
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList).build()



        viewModelScope.launch {

            val queryResponse=billingClient.queryProductDetails(queryProductDetailsParams)
            billingResult.value=queryResponse.billingResult

            if (billingResult.value?.responseCode==BillingResponseCode.OK){

                queryResponse.productDetailsList?.let {

                    productDetailsList.value=it


                    //loading finish with success
                    loading.value=false

                }
            }else{

                //loading finish with error
                loading.value=false
            }

        }




    }

    fun buyProduct(which:Int,activity:Activity){

        productDetailsList.value?.get(which)?.let {
            val productDetailsParamsList=
                listOf(

                    BillingFlowParams.ProductDetailsParams
                        .newBuilder().setProductDetails(it).build()

                )

            val billingFlowParams=
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

            val result=billingClient.launchBillingFlow(activity,billingFlowParams)



        }



    }

    private fun handlePurchaseConsumableProduct(purchase: Purchase){


        val consumeParams=ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken).build()

        viewModelScope.launch {

            val consumeResult=billingClient.consumePurchase(consumeParams)

            verification(purchase)

        }

    }

    private fun verification(purchase: Purchase){


        if (purchase.purchaseState==Purchase.PurchaseState.PURCHASED){

            //security verify işlemi
            if(!verifyValidSignature(purchase.originalJson,purchase.signature)){
                //doğrulama başarısız..
                println("doğrulama başarısız")
                return
            }



            println("doğrulama başarılı")
        }

    }

    private fun verifyValidSignature(signedData:String, signature:String):Boolean{

        return try{
            val base64Key="Your app license code get in Google play account monetizing section"
            val security= Security()

            security.verifyPurchase(base64Key,signedData,signature)
        } catch(e: Exception){
            false
        }

    }

    override fun onCleared() {
        super.onCleared()

        billingClient.endConnection()
    }

}