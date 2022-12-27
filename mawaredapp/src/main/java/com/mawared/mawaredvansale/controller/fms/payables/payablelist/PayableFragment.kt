package com.mawared.mawaredvansale.controller.fms.payables.payablelist

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
import com.mawared.mawaredvansale.controller.adapters.PayableAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.databinding.PayableFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.payable_fragment.*
import kotlinx.android.synthetic.main.payable_fragment.progress_bar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PayableFragment : ScopedFragment(), KodeinAware, IMessageListener, SearchView.OnQueryTextListener  {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("Payment")
    val factory: PayableViewModelFactory by instance()
    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(PayableViewModel::class.java)
    }
    private lateinit var binding: PayableFragmentBinding

    lateinit var navController: NavController

    private var adapter = PayableAdapter(R.layout.payable_row, permission){ e, t->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
            "P" -> viewModel.onPrint(e.py_Id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.payable_fragment, container, false)

        viewModel.msgListener = this
        viewModel.ctx = context

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
        rcv_payable.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_payable.setLoadMoreFunction { loadList(viewModel.term ?: "") }
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
                navController.navigate(R.id.action_payableFragment_to_payableEntryFragment)
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
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main){
        viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
            if(it == "Successful"){
                onSuccess(getString(R.string.msg_success_delete))
                loadList(viewModel.term ?: "")
            }
            else{
                onFailure(getString(R.string.msg_failure_delete))
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    fun onItemDeleteClick(baseEo: Payable) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    fun onItemEditClick(baseEo: Payable) {
        val action = PayableFragmentDirections.actionPayableFragmentToPayableEntryFragment()
        action.pyId = baseEo.py_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Payable) {
        val action = PayableFragmentDirections.actionPayableFragmentToPayableEntryFragment()
        action.pyId = baseEo.py_Id
        action.mode ="View"
        navController.navigate(action)
    }

    override fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        payable_list_cl?.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        payable_list_cl?.snackbar(message)
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

    fun showResult(list: List<Payable>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }
}
