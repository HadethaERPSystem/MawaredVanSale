package com.mawared.mawaredvansale.controller.md.customerlist

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CustomersListAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.CustomerFragmentBinding
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.customer_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CustomerFragment : ScopedFragment(), KodeinAware, SearchView.OnQueryTextListener {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("Customer")
    private val factory: CustomerViewModelFactory by instance()

    private lateinit var binding: CustomerFragmentBinding

    val viewModel by lazy {
      ViewModelProviders.of(this, factory).get(CustomerViewModel::class.java)
    }

    private lateinit var navController: NavController

    val adapter = CustomersListAdapter(R.layout.customer_row, permission){ customer, mode ->
        if(mode == "edit"){
            onItemEditClick(customer)
        }else{
            onItemViewClick(customer)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.customer_fragment, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        binding.pullToRefresh.setOnRefreshListener {
            adapter.setList(null, 0)
            loadList(viewModel.term ?: "")
            binding.pullToRefresh.isRefreshing = false
        }
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        @Suppress("UNCHECKED_CAST")
        rcv_customers.setupGrid(requireActivity(), adapter as BaseAdapter<Any>, 1)
        rcv_customers.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")

    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val perm = permission.split("|")
        if(perm.count() > 0 && perm[0] == "1"){
            inflater.inflate(R.menu.list_menu, menu)
        }else{
            inflater.inflate(R.menu.search_menu, menu)
        }
        val search = menu?.findItem(R.id.app_bar_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addBtn -> {
                navController.navigate(R.id.action_customerFragment_to_customerEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.term = newText
        adapter.setList(null, 0)
        loadList(viewModel.term ?: "")
        return true
    }

    fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        customer_list_lc?.snackbar(message)
    }

    fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        customer_list_lc?.snackbar(message)
    }

    fun onItemDeleteClick(baseEo: Customer) {

    }

    fun onItemEditClick(baseEo: Customer) {
        val action = CustomerFragmentDirections.actionCustomerFragmentToCustomerEntryFragment()
        action.customerId = baseEo.cu_ref_Id!!
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Customer) {
        val action = CustomerFragmentDirections.actionCustomerFragmentToCustomerEntryFragment()
        action.customerId = baseEo.cu_ref_Id!!
        action.mode = "View"
        navController.navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    private fun loadList(term : String){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            onStarted()
            viewModel.loadData(list, term, adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    private fun showResult(list: List<Customer>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }
}
