package com.mawared.mawaredvansale.controller.marketplace.items

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import androidx.navigation.NavController
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.Barcode.ContinuousActivity
import com.mawared.mawaredvansale.controller.adapters.ItemAdapter
import com.mawared.mawaredvansale.controller.adapters.UoMAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.controller.marketplace.SharedViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.md.Product_Price_List
import com.mawared.mawaredvansale.data.db.entities.md.UnitConvertion
import com.mawared.mawaredvansale.databinding.ItemsFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.items_fragment.*
import kotlinx.android.synthetic.main.items_fragment.ll_error
import kotlinx.android.synthetic.main.items_fragment.progress_bar
import kotlinx.android.synthetic.main.popup_disc.*
import kotlinx.android.synthetic.main.popup_uom.view.*
import kotlinx.android.synthetic.main.select_customer_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class ItemsFragment : ScopedFragment(), KodeinAware, IMessageListener {

    override val kodein by kodein()
    private val factory: ItemsViewModelFactory by instance()
    private lateinit var binding: ItemsFragmentBinding
    //private var isFilter: String = "N"
    private var onlyBrowsing: String = "N"
    private var subtitle: String = ""
    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(ItemsViewModel::class.java)
    }
    private val layoutId = R.layout.items_fragment
    private var mAlertDialog : AlertDialog? = null
    private var adapter = ItemAdapter(R.layout.item_rv_product, {
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
    },{ p, f ->

        val dialogView = layoutInflater.inflate(R.layout.popup_disc, null)

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
        mAlertDialog?.applyBtn?.setOnClickListener{
            val discPrcnt = mAlertDialog?.addDisc?.text
            val discAmnt = mAlertDialog?.addDiscAmnt?.text
            if(!discPrcnt.isNullOrEmpty() || !discAmnt.isNullOrEmpty()){
                val disc = if(discPrcnt.isNullOrEmpty()) 0.0 else discPrcnt.toString().toDouble()
                val discAmnt = if(discAmnt.isNullOrEmpty()) 0.0 else discAmnt.toString().toDouble()
                val discAmntPrcnt = ((discAmnt / (p.pr_unit_price ?: 1.0)) * 100)
                val limitDisc = App.prefs.saveUser?.iDiscPrcnt ?: 0.0
                if((disc + discAmntPrcnt) <= limitDisc){
                    p.pr_user_discPrcnt = disc
                    p.pr_user_disc_amnt = discAmnt
                    p.pr_price_AfD = (p.pr_unit_price!! * (1-(p.pr_user_discPrcnt / 100))) - discAmnt
                    f(p)
                }else{
                    val msg = requireContext().resources.getString(R.string.msg_error_disc_limit)
                    onFailure(String.format(msg, limitDisc.toString()))
                }
            }

            mAlertDialog?.dismiss()
        }
    })


    // Use the 'by activityViewModels()' Kotlin property delegate
    // from the fragment-ktx artifact
    private val model: SharedViewModel by activityViewModels()

    private lateinit var navController: NavController

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.ctx = requireContext()
        viewModel.msgListener = this

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.term = p0
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "", viewModel.cat_id, viewModel.br_id)

                return true
            }
        })

        binding.btnScan.setOnClickListener {
            val scanner = Intent(requireContext(), ContinuousActivity::class.java)
            startActivityForResult(scanner, 12)
        }
        //(requireActivity() as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_items_title)
        bindUI()
        binding.pullToRefresh.setOnRefreshListener {
            adapter.setList(null, 0)
            loadList(viewModel.term ?: "", viewModel.cat_id, viewModel.br_id)
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener {
            adapter.setList(null, 0)
            loadList(viewModel.term ?: "", viewModel.cat_id, viewModel.br_id)
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data != null){
            if(data.hasExtra("return_barcode")){
                binding.searchView.setQuery(data.getExtras()!!.getString("return_barcode"), false)
            }
        }
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
        rv_product.setLoadMoreFunction { loadList(viewModel.term ?: "", viewModel.cat_id, viewModel.br_id) }
        subtitle = getString(R.string.layout_items_title)
        if(arguments != null){
            val args = ItemsFragmentArgs.fromBundle(requireArguments())
            if(args.categoryId != 0){
                viewModel.cat_id = args.categoryId
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "", args.categoryId, null)
                //isFilter = "Y"
                subtitle = args.categoryName
            }else if(args.brandId != 0){
                viewModel.br_id = args.brandId
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "", null, args.brandId)
                //isFilter = "Y"
                subtitle = args.brandName
            }else{
                loadList(viewModel.term ?: "", viewModel.cat_id, viewModel.br_id)
            }
        }

        model.onlyBrowsing.observe(viewLifecycleOwner, Observer {
            adapter.setExtra(it)
        })
        model.customer.observe(viewLifecycleOwner, Observer {
            viewModel.customer = it
            viewModel.price_cat = it.cu_price_cat_code ?: App.prefs.savedSalesman?.price_cat ?: "POS"
        })
        model.vocode.observe(viewLifecycleOwner, Observer {
            viewModel.vocode = it
        })
        viewModel.loadOrders()
    }

    private fun bindUI() = Coroutines.main {
        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.FAILED) {
                val pack = requireContext().packageName
                val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
                viewModel.errorMessage.value = resources.getString(id)
                ll_error.visibility = View.VISIBLE
                rv_product.visibility = View.GONE
            } else {
                rv_product.visibility = View.VISIBLE
                ll_error.visibility = View.GONE
            }

        })
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar!!.subtitle = subtitle
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

    private fun loadList(term : String, cat_id: Int?, br_id: Int? ){
        try {
            if(rv_product.visibility == View.GONE){
                rv_product.visibility = View.VISIBLE
            }
            val list = adapter.getList().toMutableList()
            if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
                onStarted()
                viewModel.loadData(list, term, cat_id, br_id,adapter.pageCount + 1){data, pageCount ->
                    showResult(data!!, pageCount)
                }
            }
        }catch (e: Exception){
            onFailure(getString(R.string.lbl_error))
        }
    }

    fun showResult(list: List<Product>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }

    override fun onStarted() {
        ll_error.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        ll_items?.snackbar(message)
        progress_bar?.visibility = View.GONE
    }

    override fun onFailure(message: String) {
        ll_items?.snackbar(message)
        progress_bar?.visibility = View.GONE
    }
}