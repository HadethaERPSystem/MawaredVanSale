package com.mawared.mawaredvansale.controller.md.customerlist

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.CustomerPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.CustomerFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.customer_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.customer_fragment, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        bindUI()
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
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
        val manager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.app_bar_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setSearchableInfo(manager.getSearchableInfo(requireActivity().componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchView.setQuery("", false)
                searchItem.collapseActionView()
                isInSearchMode = true
                viewModel.doSearch(null, query.toString())
                //Toast.makeText(activity!!, "Quary is $query", Toast.LENGTH_LONG).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //Toast.makeText(activity!!, "Text is $newText", Toast.LENGTH_LONG).show()
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_search -> {
                if(isInSearchMode) {
                    isInSearchMode = false
                    viewModel.doSearch(null ,"")
                }
            }
            R.id.addBtn -> {
                navController.navigate(R.id.action_customerFragment_to_customerEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI()= GlobalScope.launch(Dispatchers.Main) {
        val pagedAdapter = CustomerPagedListAdapter(viewModel, requireActivity())
        val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                val viewType = pagedAdapter.getItemViewType(position)
                if(viewType == pagedAdapter.MAIN_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
            }
        }
        rcv_customers.apply {
            layoutManager = gridLayoutManager// LinearLayoutManager(this@OrdersFragment.context)
            setHasFixedSize(true)
            adapter = pagedAdapter// groupAdapter
        }

        viewModel.baseEoList.observe(viewLifecycleOwner, Observer {
            pagedAdapter.submitList(it)
        })

        viewModel.doSearch(null, "")

        viewModel.networkStateRV.observe(viewLifecycleOwner, Observer {
            progress_bar.visibility =  if(viewModel.listIsEmpty() && it.status == Status.RUNNING) View.VISIBLE else View.GONE
            if (viewModel.listIsEmpty() && (it.status == Status.FAILED)) {
                val pack = requireContext().packageName
                val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
                viewModel.errorMessage.value = resources.getString(id)
                ll_error.visibility = View.VISIBLE
            } else {
                ll_error.visibility = View.GONE
            }

            if(!viewModel.listIsEmpty()){
                pagedAdapter.setNetworkState(it)
            }
        })

    }

    private fun List<Customer>.toRow(): List<CustomerRow>{
        return this.map {
            CustomerRow( it, viewModel )
        }
    }

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
}
