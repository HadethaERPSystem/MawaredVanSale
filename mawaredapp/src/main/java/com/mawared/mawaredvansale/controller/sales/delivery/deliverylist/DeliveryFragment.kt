package com.mawared.mawaredvansale.controller.sales.delivery.deliverylist

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.DeliveryAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist.InvoicesFragmentDirections
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.databinding.DeliveryFragmentBinding
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.delivery_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class DeliveryFragment : ScopedFragment(), KodeinAware, SearchView.OnQueryTextListener  {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("Delivery")
    private val factory: DeliveryViewModelFactory by instance()

    private lateinit var binding: DeliveryFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(DeliveryViewModel::class.java)
    }

    private var adapter = DeliveryAdapter(R.layout.delivery_row, permission){ e, t->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
            "P" -> viewModel.onPrient(e.dl_Id)
        }
    }

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.delivery_fragment, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        binding.pullToRefresh.setOnRefreshListener {
            loadList(viewModel.term ?: "")
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { loadList(viewModel.term ?: "") }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var cols = 1
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 2
        }
        @Suppress("UNCHECKED_CAST")
        rcv_delivery.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_delivery.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")
        navController = Navigation.findNavController(view)
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val search = menu?.findItem(R.id.app_bar_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_search -> {

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

    // binding recycler view
    private fun bindUI()= GlobalScope.launch(Dispatchers.Main) {

        viewModel.baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                mPrint(it)
            }
        })

    }


    fun onStarted() {
        progress_bar.visibility = View.VISIBLE
    }

    fun onSuccess(message: String) {
        progress_bar.visibility = View.GONE
        delivery_list_lc.snackbar(message)
    }

    fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        delivery_list_lc.snackbar(message)
    }

    fun onItemDeleteClick(baseEo: Delivery) {
    }

    fun onItemEditClick(baseEo: Delivery) {
        val action = DeliveryFragmentDirections.actionDeliveryFragmentToDeliveryEntryFragment()
        action.deliveryId = baseEo.dl_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Delivery) {
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToAddInvoiceFragment()
        action.saleId = baseEo.dl_Id
        action.mode = "View"
        navController.navigate(action)
    }

    fun mPrint(baseEo: Delivery){

    }

    private fun loadList(term : String){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            onStarted()
            viewModel.loadData(list, term,adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Delivery>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
