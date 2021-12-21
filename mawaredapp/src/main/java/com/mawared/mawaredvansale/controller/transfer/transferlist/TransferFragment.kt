package com.mawared.mawaredvansale.controller.transfer.transferlist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.TransferPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.PdfActivity
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.databinding.TransferFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.interfaces.IPrintNavigator
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.transfer_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.Serializable
import java.util.*


class TransferFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Transfer>, IPrintNavigator<Transfer> {

    override val kodein by kodein()

    private val factory: TransferViewModelFactory by instance()

    private lateinit var binding: TransferFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(TransferViewModel::class.java)
    }

    private lateinit var navController: NavController


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.transfer_fragment, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this
        viewModel.printListener = this
        viewModel.ctx = requireActivity()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        binding.pullToRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { viewModel.refresh() }
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

    // binding recycler view
    private fun bindUI()= GlobalScope.launch(Main) {

        val pagedAdapter = TransferPagedListAdapter(viewModel, requireActivity())
        val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                val viewType = pagedAdapter.getItemViewType(position)
                if(viewType == pagedAdapter.MAIN_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
            }
        }
        rcv_transfer.apply {
            layoutManager = gridLayoutManager// LinearLayoutManager(this@OrdersFragment.context)
            setHasFixedSize(true)
            adapter = pagedAdapter// groupAdapter
        }

        viewModel.baseEoList.observe(viewLifecycleOwner, Observer {
            pagedAdapter.submitList(it)
        })

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

        viewModel.baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null && viewModel.isPrint) {
                //mPrint(it)
                //viewModel.onPrintTicket(it)
            }
            else{
                onFailure("Failure not loaded data from server for print it")
            }
        })

    }

    private fun initRecyclerView(rows: List<TransferRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }

        rcv_transfer.apply {
            layoutManager = LinearLayoutManager(this@TransferFragment.context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Transfer>.toRow(): List<TransferRow>{
        return this.map {
            TransferRow( it, viewModel )
        }
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

    override fun onItemDeleteClick(baseEo: Transfer) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    override fun onItemEditClick(baseEo: Transfer) {
        val action = TransferFragmentDirections.actionTransferFragmentToTransferEntryFragment()
        action.transferId = baseEo.tr_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Transfer) {
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
