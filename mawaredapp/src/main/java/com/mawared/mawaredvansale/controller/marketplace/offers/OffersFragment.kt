package com.mawared.mawaredvansale.controller.marketplace.offers

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.OfferAdapter
import com.mawared.mawaredvansale.controller.adapters.UoMAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.controller.marketplace.SharedViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Product_Price_List
import com.mawared.mawaredvansale.data.db.entities.md.UnitConvertion
import com.mawared.mawaredvansale.databinding.OffersFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.offers_fragment.*
import kotlinx.android.synthetic.main.popup_schedule.*
import kotlinx.android.synthetic.main.popup_uom.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class OffersFragment : ScopedFragment(), KodeinAware, IMessageListener {

    override val kodein by kodein()
    private val factory: OffersViewModelFactory by instance()
    private lateinit var binding: OffersFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(OffersViewModel::class.java)
    }

    private val layoutId = R.layout.offers_fragment
    private var mAlertDialog : AlertDialog? = null
    private var adapter = OfferAdapter(R.layout.item_rv_product, {
        // view image
    },{
        // add item
        viewModel.addOrder(it){
            val result = "result"
            // Use the Kotlin extension in the fragment-ktx artifact
            requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))

        }

    },{ p, f ->
        loadUom(p.pr_Id){ units ->
            val uomAdapter = UoMAdapter(R.layout.item_rv_uom) { uc ->
                getPrice(p.pr_Id, viewModel.customer?.cu_price_cat_code ?: "", uc.uom!!){
                    f(uc, it)
                }
                mAlertDialog?.dismiss()
            }
            uomAdapter.setList(units)

            val dialogView = layoutInflater.inflate(R.layout.popup_uom, null)

            val mBuilder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .setCancelable(false)

            // show dialog
            val metrics = DisplayMetrics() //get metrics of screen

            requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

            mAlertDialog = mBuilder.show()
            mAlertDialog?.closeBtn?.setOnClickListener {
                mAlertDialog?.dismiss()
            }

            @Suppress("UNCHECKED_CAST")
            dialogView.rv_uom.setupGrid(requireContext(), uomAdapter as BaseAdapter<Any>, 1)
        }
    })

    // Use the 'by activityViewModels()' Kotlin property delegate
    // from the fragment-ktx artifact
    private val model: SharedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.ctx = requireContext()
        viewModel.msgListener = this

        bindUI()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                viewModel._term.value = p0
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel._term.value = p0
                return false
            }
        })

        (requireActivity() as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_offers_title)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var cols = 2
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 3
        }
        @Suppress("UNCHECKED_CAST")
        rv_product.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)

        model.onlyBrowsing.observe(viewLifecycleOwner, Observer {
            adapter.setExtra(it)
        })
        model.customer.observe(viewLifecycleOwner, Observer {
            viewModel.customer = it
        })
        model.vocode.observe(viewLifecycleOwner, Observer {
            viewModel.vocode = it
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun bindUI()= GlobalScope.launch(Dispatchers.Main){
        try {
            viewModel.loadOrders()

            viewModel.productList.observe(viewLifecycleOwner, Observer {
                if(it != null)
                    adapter.setList(it)
            })
            viewModel._term.value = null
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun loadUom(prod_Id: Int, success: (List<UnitConvertion>?) -> Unit){
        viewModel.loadUom(prod_Id){
            success(it)
        }
    }

    private fun getPrice(prod_Id: Int, priceCode: String, uomId: Int, success: (Product_Price_List?) -> Unit){
        viewModel.getLastPrice(prod_Id, priceCode, uomId){
            success(it)
        }
    }

    override fun onStarted() {
        //progress_bar_sale?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        //progress_bar_sale?.visibility = View.GONE
        ll_offer?.snackbar(message)
    }

    override fun onFailure(message: String) {
        //progress_bar_sale?.visibility = View.GONE
        ll_offer?.snackbar(message)
    }
}