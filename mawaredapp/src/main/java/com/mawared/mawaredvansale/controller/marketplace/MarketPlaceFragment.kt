package com.mawared.mawaredvansale.controller.marketplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.databinding.FragmentMarketPlaceBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.lang.Exception


class MarketPlaceFragment : ScopedFragment(), KodeinAware {
    override val kodein by kodein()
    private val factory: MarketPlaceViewModelFactory by instance()

    private lateinit var binding : FragmentMarketPlaceBinding

    val viewModel by lazy {
      ViewModelProviders.of(this, factory).get(MarketPlaceViewModel::class.java)
    }

    // Use the 'by activityViewModels()' Kotlin property delegate
    // from the fragment-ktx artifact
    private val model: SharedViewModel by activityViewModels()

    var bottomNavView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_market_place, container, false)

        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.container_fragment) as? NavHostFragment

        val navController = nestedNavHostFragment?.navController

        bottomNavView = binding.bottomNavigation
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        try {
            if (navController != null) {
                binding.cartCard.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putSerializable("customer", viewModel.customer)
                    bundle.putString("vocode", viewModel.vocode)
                    navController!!.navigate(R.id.cartFragment, bundle)
                }
                //bottomNavigationView.setupWithNavController(navController)
                //val navController = Navigation.findNavController(requireActivity(), R.id.container_fragment)
                NavigationUI.setupWithNavController(bottomNavView!!, navController)
                //NavigationUI.setupActionBarWithNavController(requireActivity() as AppCompatActivity, navController, null)
                bottomNavView!!.setupWithNavController(navController)

                val appBarConfiguration = AppBarConfiguration(setOf(R.id.itemsFragment, R.id.categoryFragment, R.id.brandFragment, R.id.offersFragment, R.id.cartFragment))
                (requireActivity() as AppCompatActivity).setupActionBarWithNavController(navController, appBarConfiguration)

            }



        }catch (e: Exception){
            e.printStackTrace()
        }

        return binding.root
    }

    fun refresh(){
        viewModel.refresh()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Use the Kotlin extension in the fragment-ktx artifact
        requireActivity().supportFragmentManager.setFragmentResultListener("requestKey", viewLifecycleOwner) { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            refresh()
            // Do something with the result
        }

        refresh()

        if(arguments != null){
            val args = MarketPlaceFragmentArgs.fromBundle(requireArguments())
            if(args.customer != null && !args.vocode.isNullOrEmpty()){
                viewModel.customer = args.customer
                viewModel.vocode = args.vocode
                binding.card1.visibility = View.VISIBLE
                model.setBrowsingOnly("N")
                model.setCustomer(viewModel.customer!!)
            }
            else
            {
                viewModel.onlyBrowsing = true
                binding.card1.visibility = View.GONE
                model.setBrowsingOnly("Y")
            }
        }
    }
}