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
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.SelectCustomerFragmentBinding
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import kotlinx.android.synthetic.main.popup_schedule.*
import kotlinx.android.synthetic.main.select_customer_fragment.*
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

        bindUI()
        binding.pullToRefresh.setOnRefreshListener {
            adapter.setList(null, 0)
            loadList(viewModel.term ?: "")
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener {
            adapter.setList(null, 0)
            loadList(viewModel.term ?: "")}

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

    private fun bindUI() = Coroutines.main {
        viewModel.networkState.observe(viewLifecycleOwner, Observer {

            if (it.status == Status.FAILED) {
                val pack = requireContext().packageName
                val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
                viewModel.errorMessage.value = resources.getString(id)
                ll_error.visibility = View.VISIBLE
                rv_customer.visibility = View.GONE
            } else {
                rv_customer.visibility = View.VISIBLE
                ll_error.visibility = View.GONE
            }

        })
    }

    private fun schedule(customer: Customer){
        val dialogView = layoutInflater.inflate(R.layout.popup_schedule, null)
        val mBuilder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogView)
            //.setCustomTitle()
            .setCancelable(false)

        // show dialog
        val mAlertDialog = mBuilder.show()
        val menus = MenuSysPrefs.getMenu()
        var menu = menus.find{it.menu_code == "Order"}

        mAlertDialog.orderBtn.visibility = if(menu != null) View.VISIBLE else View.GONE
        mAlertDialog.orderBtn.setOnClickListener{
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToMarketPlaceFragment()
            action.customer = customer
            action.vocode = "SaleOrder"
            navController.navigate(action)
            mAlertDialog.dismiss()
        }
        menu =  menus.find{it.menu_code == "PSOrder"}
        mAlertDialog.psorderBtn.visibility = if(menu != null) View.VISIBLE else View.GONE
        mAlertDialog.psorderBtn.setOnClickListener {
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToMarketPlaceFragment()
            action.customer = customer
            action.vocode = "PSOrder"
            navController.navigate(action)
            mAlertDialog.dismiss()
        }


        menu =  menus.find{it.menu_code == "Invoice"}
        mAlertDialog.invoiceBtn.visibility = if(menu != null) View.VISIBLE else View.GONE
        mAlertDialog.invoiceBtn.setOnClickListener {
           val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToMarketPlaceFragment()
           action.customer = customer
           action.vocode = "SaleInvoice"
           navController.navigate(action)
           mAlertDialog.dismiss()
       }

        menu =  menus.find{it.menu_code == "Delivery"}
        mAlertDialog.deliveryBtn.visibility = if(menu != null) View.VISIBLE else View.GONE
        mAlertDialog.deliveryBtn.setOnClickListener {
            val action = SelectCustomerFragmentDirections.actionSelectCustomerFragmentToDeliveryFragment()
            action.cuId = customer.cu_ref_Id!!
            navController.navigate(action)
            mAlertDialog.dismiss()
        }

        menu =  menus.find{it.menu_code == "CallCycle"}
        mAlertDialog.visitBtn.visibility = if(menu != null) View.VISIBLE else View.GONE
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
        try {
            if(rv_customer.visibility == View.GONE){
                rv_customer.visibility = View.VISIBLE
            }
            val list = adapter.getList().toMutableList()
            if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
                onStarted()
                viewModel.loadData(list, term, adapter.pageCount + 1){data, pageCount ->
                    showResult(data!!, pageCount)
                }
            }
        }catch (e: Exception){
            onFailure(getString(R.string.lbl_error))
        }
    }

    private fun showResult(list: List<Customer>, pageCount: Int) = runOnUiThread {
        adapter.setList(list, pageCount)
        onSuccess()
    }

    private fun deleteAll(){
        viewModel.deleteAll()
    }

    fun onStarted() {
        ll_error?.visibility = View.GONE
        progress_bar?.visibility = View.VISIBLE
    }

    fun onSuccess() {
        progress_bar?.visibility = View.GONE
    }

    fun onFailure(message: String) {
        ll_customer?.snackbar(message)
        progress_bar?.visibility = View.GONE
    }
}