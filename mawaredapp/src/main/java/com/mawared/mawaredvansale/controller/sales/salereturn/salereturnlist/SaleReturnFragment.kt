package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnlist

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.ReturnAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.databinding.SaleReturnFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.sale_return_fragment.*
import kotlinx.android.synthetic.main.sale_return_fragment.progress_bar
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SaleReturnFragment : ScopedFragment(), KodeinAware, IMessageListener, SearchView.OnQueryTextListener {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("SaleReturn")
    private val factory: SaleReturnViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(SaleReturnViewModel::class.java)
    }

    private lateinit var binding: SaleReturnFragmentBinding
    private lateinit var navController: NavController

    private var adapter = ReturnAdapter(R.layout.sale_return_row, permission){ e, t->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
            "P" -> viewModel.onPrint(e.sr_Id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.sale_return_fragment, container, false)

        viewModel.msgListener = this
        viewModel.ctx = requireActivity()

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        binding.pullToRefresh.setOnRefreshListener {
            loadList(viewModel.term ?: "")
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { loadList(viewModel.term ?: "") }

        bindUI()
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
        rcv_sale_return.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_sale_return.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")
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
                navController.navigate(R.id.action_saleReturnFragment_to_saleReturnEntryFragment)
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

    private fun bindUI() = GlobalScope.launch(Main) {

        try {

            viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
                if(it == "Successful"){
                    onSuccess(getString(R.string.msg_success_delete))
                    loadList(viewModel.term ?: "")
                }
                else{
                    onFailure(getString(R.string.msg_failure_delete))
                }
            })

            viewModel.baseEo.observe(viewLifecycleOwner, Observer {
                if (it != null && viewModel.isPrint) {
                    viewModel.onPrintTicket(it)
                }
            })

        }catch (e: Exception){
            Log.i("Exc", "Error is ${e.message}")
        }

    }

    // Items event listener
    fun onItemDeleteClick(baseEo: Sale_Return) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    fun onItemEditClick(baseEo: Sale_Return) {
        val action = SaleReturnFragmentDirections.actionSaleReturnFragmentToSaleReturnEntryFragment()
        action.returnId = baseEo.sr_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Sale_Return) {
        val action = SaleReturnFragmentDirections.actionSaleReturnFragmentToSaleReturnEntryFragment()
        action.returnId = baseEo.sr_Id
        action.mode = "View"
        navController.navigate(action)
    }

    // message listener
    override fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        sale_return_list_cl.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        sale_return_list_cl.snackbar(message)
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

    fun showResult(list: List<Sale_Return>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }

    // fragment event on destoy
    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
