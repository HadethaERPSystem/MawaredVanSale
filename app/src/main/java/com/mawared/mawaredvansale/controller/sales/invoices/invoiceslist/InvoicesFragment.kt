package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.databinding.FragmentInvoicesBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_invoices.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class InvoicesFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Sale> {

    override val kodein by kodein()

    private val factory: InvoicesViewModelFactory by instance()

    private lateinit var binding: FragmentInvoicesBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(InvoicesViewModel::class.java)
    }

    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_invoices, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this
        viewModel.ctx = activity!!
        viewModel.activity = activity as AppCompatActivity
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_invoice_list_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = ""
        removeObservers()
        bindUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        removeObservers()
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

    }

    override fun onResume() {
        removeObservers()
        super.onResume()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun removeObservers(){
        viewModel.baseEo.removeObservers(this@InvoicesFragment)
        viewModel.sales.removeObservers(this@InvoicesFragment)
        viewModel.deleteRecord.removeObservers(this@InvoicesFragment)
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
        viewModel.sales.observe(this@InvoicesFragment, Observer { sl ->
            group_loading_invs.hide()
            if(sl == null) return@Observer
            initRecyclerView(sl.sortedByDescending { it.sl_doc_date }.toInvoiceRow())
        })

        viewModel.deleteRecord.observe(this@InvoicesFragment, Observer {
            group_loading_invs.hide()
            if(it == "Successful"){
                onSuccess(getString(R.string.msg_success_delete))
                viewModel.setCustomer(null)
            }
            else{
                onFailure(getString(R.string.msg_failure_delete))
            }
        })

        viewModel.baseEo.observe(this@InvoicesFragment, Observer {
            if(it != null && viewModel.isPrint) {
                viewModel.onPrintTicket(it)
            }

        })
        viewModel.setCustomer(null)
    }

    private fun initRecyclerView(saleItem: List<InvoiceRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(saleItem)
        }

        rcv_invoices.apply {
            layoutManager = LinearLayoutManager(this@InvoicesFragment.context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Sale>.toInvoiceRow(): List<InvoiceRow>{
        return this.map {
            InvoiceRow( it, viewModel )
        }
    }

    override fun onStarted() {
        group_loading_invs.show()
    }

    override fun onSuccess(message: String) {
        group_loading_invs.hide()
        inv_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading_invs.hide()
        inv_list_lc.snackbar(message)
    }

    override fun onItemDeleteClick(baseEo: Sale) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            onStarted()
            viewModel.confirmDelete(it)
        }
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
