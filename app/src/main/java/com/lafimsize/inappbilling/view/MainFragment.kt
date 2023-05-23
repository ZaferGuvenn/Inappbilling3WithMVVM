package com.lafimsize.inappbilling.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.lafimsize.inappbilling.R
import com.lafimsize.inappbilling.databinding.FragmentMainBinding

class MainFragment :Fragment(R.layout.fragment_main){

    private lateinit var fragmentBinding:FragmentMainBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentBinding= FragmentMainBinding.bind(view)
        val binding=fragmentBinding


        binding.goBuy.setOnClickListener {

            Navigation.findNavController(it)
                .navigate(MainFragmentDirections.actionMainFragmentToShopFragment())

        }
    }



}