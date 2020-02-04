package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnlist

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
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.databinding.SaleReturnFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.sale_return_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SaleReturnFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Sale_Return> {

    override val kodein by kodein()
    private val factory: SaleReturnViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(SaleReturnViewModel::class.java)
    }

    private lateinit var binding: SaleReturnFragmentBinding
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.sale_return_fragment, container, false)

        viewModel.navigator = this

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        bindUI()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_salereturn_list_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = ""
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
                navController.navigate(R.id.action_saleReturnFragment_to_saleReturnEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindUI() = GlobalScope.launch(Main) {

        viewModel.saleReturns.observe(viewLifecycleOwner, Observer {
            group_loading.hide()
            initRecyclerView(it.sortedByDescending { it.sr_doc_date }.toSaleReturnRow())
        })

        viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
            group_loading.hide()
            if(it == "Successful"){
                onSuccess(getString(R.string.msg_success_delete))
                viewModel.setCustomer(null)
            }
            else{
                onFailure(getString(R.string.msg_failure_delete))
            }
        })

        viewModel.setCustomer(null)
    }

    private fun initRecyclerView(saleItem: List<SaleReturnRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(saleItem)
        }

        rcv_sale_return.apply {
            layoutManager = LinearLayoutManager(this@SaleReturnFragment.context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Sale_Return>.toSaleReturnRow(): List<SaleReturnRow>{
        return this.map {
            SaleReturnRow(it, viewModel)
        }
    }

    // Items event listener
    override fun onItemDeleteClick(baseEo: Sale_Return) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            onStarted()
            viewModel.confirmDelete(it)
        }
    }

    override fun onItemEditClick(baseEo: Sale_Return) {
        val action = SaleReturnFragmentDirections.actionSaleReturnFragmentToSaleReturnEntryFragment()
        action.returnId = baseEo.sr_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Sale_Return) {
        val action = SaleReturnFragmentDirections.actionSaleReturnFragmentToSaleReturnEntryFragment()
        action.returnId = baseEo.sr_Id
        action.mode = "View"
        navController.navigate(action)
    }

    // message listener
    override fun onStarted() {
        group_loading.show()
    }

    override fun onSuccess(message: String) {
        group_loading.hide()
        sale_return_list_cl.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading.hide()
        sale_return_list_cl.snackbar(message)
    }

    // fragment event on destoy
    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
