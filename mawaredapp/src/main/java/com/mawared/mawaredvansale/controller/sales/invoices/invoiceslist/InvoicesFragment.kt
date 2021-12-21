package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.SalesPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.databinding.FragmentInvoicesBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.fragment_invoices.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.InputStream
import java.net.URL


class InvoicesFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Sale> {

    override val kodein by kodein()

    private val factory: InvoicesViewModelFactory by instance()

    private lateinit var binding: FragmentInvoicesBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(InvoicesViewModel::class.java)
    }

    private lateinit var navController: NavController


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //loadLocale()
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_invoices, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this
        viewModel.ctx = requireActivity()
        viewModel.activity = activity as AppCompatActivity
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        removeObservers()
        bindUI()
        binding.pullToRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { viewModel.refresh() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        removeObservers()
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

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
        viewModel.sales.removeObservers(this)
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
                navController.navigate(R.id.action_invoicesFragment_to_addInvoiceFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI()= GlobalScope.launch(Main) {
        try {

            val pagedAdapter = SalesPagedListAdapter(viewModel, requireActivity())
            val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val viewType = pagedAdapter.getItemViewType(position)
                    if (viewType == pagedAdapter.MAIN_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                    else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
                }
            }
            rcv_invoices.apply {
                layoutManager = gridLayoutManager
                setHasFixedSize(true)
                adapter = pagedAdapter
            }

            viewModel.sales.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    pagedAdapter.submitList(it)
                }
            })
            viewModel.setCustomer(null)

            viewModel.networkStateRV.observe(viewLifecycleOwner, Observer {
                progress_bar.visibility =
                    if (viewModel.listIsEmpty() && it.status == Status.RUNNING) View.VISIBLE else View.GONE
                if (viewModel.listIsEmpty() && (it.status == Status.FAILED)) {
                    val pack = requireContext().packageName
                    val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
                    viewModel.errorMessage.value = resources.getString(id)
                    ll_error.visibility = View.VISIBLE
                } else {
                    ll_error.visibility = View.GONE
                }

                if (!viewModel.listIsEmpty()) {
                    pagedAdapter.setNetworkState(it)
                }
            })

            viewModel.networkState.observe(viewLifecycleOwner, Observer {
                progress_bar.visibility =
                    if (viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
            })

            viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
                if (it == "Successful") {
                    onSuccess(getString(R.string.msg_success_delete))
                    viewModel.setCustomer(null)
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
        progress_bar.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar.visibility = View.GONE
        inv_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        inv_list_lc.snackbar(message)
    }

    override fun onItemDeleteClick(baseEo: Sale) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    override fun onItemEditClick(baseEo: Sale) {
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToAddInvoiceFragment()
        action.saleId = baseEo.sl_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Sale) {

        val action = InvoicesFragmentDirections.actionInvoicesFragmentToAddInvoiceFragment()
        action.saleId = baseEo.sl_Id
        action.mode = "View"
        navController.navigate(action)
    }

//    fun mPrint(baseEo: Sale){
//        //viewModel.onPrintTicket(baseEo)
////        val lang = Locale.getDefault().toString().toLowerCase()
////        val tickets = GenerateTicket(activity!!, lang).Create(baseEo,R.drawable.ic_logo_black, "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)
////
//////        val intent = Intent(activity, PrintingActivity::class.java)
//////        val bundle = Bundle()
//////        bundle.putSerializable("tickets", tickets as Serializable)
//////        intent.putExtras(bundle)
//////        startActivity(intent)
////        TicketPrinting(activity!!, tickets).run(){complete ->
////            if(complete){
////                onSuccess("Print Successfully")
////            }else{
////                onFailure("Print Failure")
////            }
////        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
