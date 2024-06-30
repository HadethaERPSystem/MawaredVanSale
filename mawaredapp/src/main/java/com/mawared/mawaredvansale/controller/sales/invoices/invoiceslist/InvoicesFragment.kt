package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.InvoicesAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.common.Common
import com.mawared.mawaredvansale.controller.common.printing.CreatePdf
import com.mawared.mawaredvansale.controller.common.printing.PrintPDFViaBluetooth
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.databinding.FragmentInvoicesBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.PrintingObject
import com.mawared.mawaredvansale.utilities.snackbar
import com.mawared.mawaredvansale.utils.SunmiPrintHelper
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.fragment_invoices.*
import kotlinx.android.synthetic.main.fragment_invoices.progress_bar
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class InvoicesFragment : ScopedFragment(), KodeinAware, IMessageListener, SearchView.OnQueryTextListener {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("Invoice")
    private val factory: InvoicesViewModelFactory by instance()

    private lateinit var binding: FragmentInvoicesBinding

    private var adapter = InvoicesAdapter(R.layout.invoice_row, permission){ e, t->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
            "P" -> {
//                val filePath = Common.getAppPath(requireActivity()) + "test.pdf"
//                CreatePdf().createPdfFromString("علي حسين باوي", filePath )
//                PrintPDFViaBluetooth(requireContext()).printPdfViaBluetooth(filePath ,"DC:0D:30:63:34:1A")
                viewModel.onPrint(e.sl_Id)

             }
        }
    }

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(InvoicesViewModel::class.java)
    }

    private lateinit var navController: NavController


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //loadLocale()
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_invoices, container, false)

        viewModel.msgListener = this
        viewModel.ctx = requireActivity()
        viewModel.activity = activity as AppCompatActivity
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        removeObservers()
        bindUI()

        binding.pullToRefresh.setOnRefreshListener {
            loadList(viewModel.term ?: "")
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { loadList(viewModel.term ?: "") }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        removeObservers()
        super.onViewCreated(view, savedInstanceState)
        var cols = 1
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 2
        }
        @Suppress("UNCHECKED_CAST")
        rcv_invoices.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_invoices.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")
        navController = Navigation.findNavController(view)

    }

    /**
     * Connect print service through interface library
     */
    private fun init() {
        SunmiPrintHelper.getInstance().initSunmiPrinterService(requireContext())
    }
    // enable options menu in this fragment
    override fun onResume() {
        removeObservers()
        super.onResume()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun removeObservers(){
        viewModel.baseEo.removeObservers(this)
        //viewModel.sales.removeObservers(this)
        viewModel.deleteRecord.removeObservers(this)
    }

    override fun onDestroyView() {
        removeObservers()
        onDestroy()
        super.onDestroyView()
    }
    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        init()
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
                navController.navigate(R.id.action_invoicesFragment_to_addInvoiceFragment)
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
    private fun bindUI()= GlobalScope.launch(Main) {
        try {

            viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
                if (it == "Successful") {
                    onSuccess(getString(R.string.msg_success_delete))
                    loadList(viewModel.term ?: "")
                } else {
                    onFailure(getString(R.string.msg_failure_delete))
                }
            })

            viewModel.baseEo.observe(viewLifecycleOwner, Observer {
                if (it != null && viewModel.isPrint) {
                    viewModel.onPrintTicket(it)
                }

            })
        } catch (e: Exception) {
            Log.i("Exc", "Error is ${e.message}")
        }

    }

    override fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        inv_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        inv_list_lc.snackbar(message)
    }

    fun onItemDeleteClick(baseEo: Sale) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    fun onItemEditClick(baseEo: Sale) {
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToAddInvoiceFragment()
        action.saleId = baseEo.sl_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Sale) {
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToAddInvoiceFragment()
        action.saleId = baseEo.sl_Id
        action.mode = "View"
        navController.navigate(action)
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

    fun showResult(list: List<Sale>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
