package com.lafimsize.inappbilling.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lafimsize.inappbilling.R
import com.lafimsize.inappbilling.databinding.FragmentMainBinding
import com.lafimsize.inappbilling.databinding.FragmentShopBinding
import com.lafimsize.inappbilling.viewmodel.ShopViewModel

class ShopFragment :Fragment(R.layout.fragment_shop){

    private lateinit var fragmentBinding:FragmentShopBinding

    private lateinit var viewModel: ShopViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentBinding= FragmentShopBinding.bind(view)
        val binding=fragmentBinding


        viewModel=ViewModelProvider(this)[ShopViewModel::class.java]

        viewModel.initializeBillingClient(requireContext())

        observeLiveData()

        binding.firstPackage.setOnClickListener {

            viewModel.buyProduct(0,requireActivity())

        }

        binding.secondPackage.setOnClickListener {

            viewModel.buyProduct(1,requireActivity())

        }


    }


    private fun observeLiveData(){

        viewModel.connectionStatus.observe(viewLifecycleOwner){

            println("connection status: $it")

        }

        viewModel.loading.observe(viewLifecycleOwner){

            println("loading status: $it")

        }

        viewModel.billingResult.observe(viewLifecycleOwner){
            println("billing result: $it")
        }

        viewModel.productDetailsList.observe(viewLifecycleOwner){

            println("----Product----")
            for (i in it.indices){
                println("$i == ${it[i]}")


            }

            fragmentBinding.firstPackageTV.text=it[0].name
            fragmentBinding.firstPackage.text=it[0].oneTimePurchaseOfferDetails?.formattedPrice

            fragmentBinding.secondPackageTV.text=it[1].name
            fragmentBinding.secondPackage.text=it[1].oneTimePurchaseOfferDetails?.formattedPrice
        }


    }


}