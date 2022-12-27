package com.mawared.mawaredvansale.controller.transfer.transferlist

import android.content.Intent
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
import com.mawared.mawaredvansale.controller.adapters.TransferAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.PdfActivity
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.databinding.TransferFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.interfaces.IPrintNavigator
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.transfer_fragment.*
import kotlinx.android.synthetic.main.transfer_fragment.progress_bar
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.Serializable
import java.util.*


class TransferFragment : ScopedFragment(), KodeinAware, IMessageListener, IPrintNavigator<Transfer>, SearchView.OnQueryTextListener {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("Transfer")
    private val factory: TransferViewModelFactory by instance()

    private lateinit var binding: TransferFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(TransferViewModel::class.java)
    }

    private lateinit var navController: NavController

    private var adapter = TransferAdapter(R.layout.transfer_row, permission){ e, t->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
            "P" -> viewModel.onPrint(e.tr_Id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.transfer_fragment, container, false)


        viewModel.msgListener = this
        viewModel.ctx = requireActivity()
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
        rcv_transfer.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_transfer.setLoadMoreFunction { loadList(viewModel.term ?: "") }
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
        inflater.inflate(R.menu.list_menu, menu)
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
                navController.navigate(R.id.action_transferFragment_to_transferEntryFragment)
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

        viewModel.baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null && viewModel.isPrint) {
                doPrint(it)
                //viewModel.onPrintTicket(it)
            }
            else{
                onFailure("Failure not loaded data from server for print it")
            }
        })

    }


    override fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        transfer_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        transfer_list_lc.snackbar(message)
    }

    fun onItemDeleteClick(baseEo: Transfer) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    fun onItemEditClick(baseEo: Transfer) {
        val action = TransferFragmentDirections.actionTransferFragmentToTransferEntryFragment()
        action.transferId = baseEo.tr_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Transfer) {
        val action = TransferFragmentDirections.actionTransferFragmentToTransferEntryFragment()
        action.transferId = baseEo.tr_Id
        action.mode = "View"
        navController.navigate(action)
    }



    override fun doPrint(baseEo: Transfer) {
        val intent = Intent(requireActivity(), PdfActivity::class.java)
        //intent.putExtra("TRS_PDF_TICKET",lines as Serializable)
        val lang = Locale.getDefault().toString().toLowerCase()
        val template = GenerateTicket(requireActivity(), lang).createPdf(viewModel.baseEo.value!!,
            R.drawable.ic_logo_black, "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)
        val bundle = Bundle()
        val data = template as Serializable
        bundle.putSerializable("TRS_PDF_TICKET", data)
        intent.putExtras(bundle)
        //intent.putExtraJson(lines)
        startActivity(intent)
        viewModel.isPrint = false
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

    fun showResult(list: List<Transfer>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
