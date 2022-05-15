package com.mawared.mawaredvansale.controller.md.customerlist

import android.app.SearchManager
import android.content.Context
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
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.customer_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CustomerFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Customer> {

    override val kodein by kodein()
    private val factory: CustomerViewModelFactory by instance()

    private lateinit var binding: CustomerFragmentBinding

    val viewModel by lazy {
      ViewModelProviders.of(this, factory).get(CustomerViewModel::class.java)
    }

    private lateinit var navController: NavController
    private var isInSearchMode: Boolean = false

    val adapter = CustomersListAdapter(R.layout.customer_row){ customer, mode ->
        if(mode == "edit"){
            onItemEditClick(customer)
        }else{
            onItemViewClick(customer)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.customer_fragment, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
//                viewModel.term = p0
//                adapter.setList(null, 0)
//                loadList(viewModel.term ?: "")
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.term = p0
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "")
                return false
            }
        })

        binding.pullToRefresh.setOnRefreshListener {
            //viewModel.userId.value = App.prefs.saveUser!!.id
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
        inflater.inflate(R.menu.addonly_menu, menu)

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

    // binding recycler view
//    private fun bindUI()= GlobalScope.launch(Dispatchers.Main) {
//
////        viewModel.baseEoList.observe(viewLifecycleOwner, Observer {
////            adapter.setList(it)
////        })
////
////        viewModel.doSearch(null, "")
//
////        viewModel.networkStateRV.observe(viewLifecycleOwner, Observer {
////            progress_bar.visibility =  if(viewModel.listIsEmpty() && it.status == Status.RUNNING) View.VISIBLE else View.GONE
////            if (viewModel.listIsEmpty() && (it.status == Status.FAILED)) {
////                val pack = requireContext().packageName
////                val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
////                viewModel.errorMessage.value = resources.getString(id)
////                ll_error.visibility = View.VISIBLE
////            } else {
////                ll_error.visibility = View.GONE
////            }
////
////            if(!viewModel.listIsEmpty()){
////                pagedAdapter.setNetworkState(it)
////            }
////        })
//
//    }


    override fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        customer_list_lc?.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        customer_list_lc?.snackbar(message)
    }

    override fun onItemDeleteClick(baseEo: Customer) {

    }

    override fun onItemEditClick(baseEo: Customer) {
        val action = CustomerFragmentDirections.actionCustomerFragmentToCustomerEntryFragment()
        action.customerId = baseEo.cu_ref_Id!!
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Customer) {
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
            viewModel.loadData(list, term, adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    private fun showResult(list: List<Customer>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
    }
}
