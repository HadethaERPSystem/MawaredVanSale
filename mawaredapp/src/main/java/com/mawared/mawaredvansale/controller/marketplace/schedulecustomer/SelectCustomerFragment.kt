package com.mawared.mawaredvansale.controller.marketplace.schedulecustomer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.ScheduleCustomerAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.callcycle.cylist.CallCycleFragmentDirections
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.SelectCustomerFragmentBinding
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import kotlinx.android.synthetic.main.popup_schedule.*
import kotlinx.android.synthetic.main.select_customer_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SelectCustomerFragment : ScopedFragmentLocation(), KodeinAware {

    override val kodein by kodein()

    private val factory : SelectCustomerViewModelFactory by instance()
    lateinit var binding : SelectCustomerFragmentBinding
    var isLoading: Boolean = false

    val viewModel by lazy{
        ViewModelProviders.of(this, factory).get(SelectCustomerViewModel::class.java)
    }
    private lateinit var navController: NavController
    private val rv_layoutId = R.layout.item_rv_customer
    private val layoutId = R.layout.select_customer_fragment

    private var adapter = ScheduleCustomerAdapter(rv_layoutId){
        schedule(it)
        deleteAll()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?  ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                viewModel.term = p0
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "")
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.term = p0
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "")
                return false
            }
        })
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        @Suppress("UNCHECKED_CAST")
        rv_customer.setupGrid(requireActivity(), adapter as BaseAdapter<Any>, 1)
        rv_customer.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")
    }


    private fun schedule(customer: Customer){
        val dialogView = layoutInflater.inflate(R.layout.popup_schedule, null)
        val mBuilder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogView)
            //.setCustomTitle()
            .setCancelable(false)

        // show dialog
        val mAlertDialog = mBuilder.show()

        mAlertDialog.orderBtn.setOnClickListener{
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToMarketPlaceFragment()
            action.customer = customer
            action.vocode = "SaleOrder"
            navController.navigate(action)
            mAlertDialog.dismiss()
        }

        mAlertDialog.psorderBtn.setOnClickListener {
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToMarketPlaceFragment()
            action.customer = customer
            action.vocode = "PSOrder"
            navController.navigate(action)
            mAlertDialog.dismiss()
        }

        mAlertDialog.invoiceBtn.setOnClickListener {
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToMarketPlaceFragment()
            action.customer = customer
            action.vocode = "SaleInvoice"
            navController.navigate(action)
            mAlertDialog.dismiss()
        }

        mAlertDialog.deliveryBtn.setOnClickListener {
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToDeliveryFragment()
            action.cuId = customer.cu_ref_Id!!
            navController.navigate(action)
            mAlertDialog.dismiss()
        }

        mAlertDialog.visitBtn.setOnClickListener {
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToCallCycleFragment()
            action.cuId = customer.cu_ref_Id!!
            navController.navigate(action)
            mAlertDialog.dismiss()
        }

        mAlertDialog.closeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun loadList(term : String){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            viewModel.loadData(list, term, adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    private fun showResult(list: List<Customer>, pageCount: Int) = runOnUiThread {
        adapter.setList(list, pageCount)
    }

    private fun deleteAll(){
        viewModel.deleteAll()
    }
}