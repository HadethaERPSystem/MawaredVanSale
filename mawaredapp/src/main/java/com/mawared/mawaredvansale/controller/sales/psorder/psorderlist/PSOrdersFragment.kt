package com.mawared.mawaredvansale.controller.sales.psorder.psorderlist

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
import com.mawared.mawaredvansale.controller.adapters.SalesOrderAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.databinding.PsordersFragmentBinding
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.psorders_fragment.*
import kotlinx.android.synthetic.main.psorders_fragment.progress_bar
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PSOrdersFragment : ScopedFragment(), KodeinAware, SearchView.OnQueryTextListener{

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("PSOrder")
    private val factory: PSOrdersViewModelFactory by instance()

    private lateinit var viewModel: PSOrdersViewModel
    private lateinit var binding: PsordersFragmentBinding

    private lateinit var navController: NavController

    private var adapter = SalesOrderAdapter(R.layout.order_row, permission){e, t ->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle?): View {

        viewModel = ViewModelProviders.of(this, factory).get(PSOrdersViewModel::class.java)

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.psorders_fragment, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        bindUI()
        binding.pullToRefresh.setOnRefreshListener {
            loadList(viewModel.term ?: "", viewModel.cu_id)
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { loadList(viewModel.term ?: "", viewModel.cu_id) }
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var cols = 1
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 2
        }
        @Suppress("UNCHECKED_CAST")
        rcv_psorders.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_psorders.setLoadMoreFunction { loadList(viewModel.term ?: "", viewModel.cu_id) }
        loadList(viewModel.term ?: "", viewModel.cu_id)

        navController = Navigation.findNavController(view)
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
            R.id.app_bar_search -> {

            }
            R.id.addBtn -> {
                navController.navigate(R.id.action_PSOrdersFragment_to_PSOrderEntryFragment)
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
        loadList(viewModel.term ?: "", viewModel.cu_id)
        return true
    }

    private fun bindUI() = GlobalScope.launch(Main) {

        viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
            if(it == "Successful"){
                onSuccess(getString(R.string.msg_success_delete))
                loadList(viewModel.term ?: "", viewModel.cu_id)
            }
            else{
                onFailure(getString(R.string.msg_failure_delete))
            }
        })

    }

    fun onItemDeleteClick(baseEo: Sale_Order) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    fun onItemEditClick(baseEo: Sale_Order) {
        val action = PSOrdersFragmentDirections.actionPSOrdersFragmentToPSOrderEntryFragment()
        action.orderId = baseEo.so_id
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Sale_Order) {
        val action = PSOrdersFragmentDirections.actionPSOrdersFragmentToPSOrderEntryFragment()
        action.orderId = baseEo.so_id
        action.mode ="View"
        navController.navigate(action)
    }

    fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        psorder_list_cl.snackbar(message)
    }

    fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        psorder_list_cl.snackbar(message)
    }

    private fun loadList(term : String, cu_id: Int?){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            onStarted()
            viewModel.loadData(list, term, cu_id, adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Sale_Order>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
