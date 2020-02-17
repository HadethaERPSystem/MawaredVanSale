package com.mawared.mawaredvansale.controller.fms.payables.payablelist

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.PagedListAdapter.PayablePagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.databinding.PayableFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.payable_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PayableFragment : ScopedFragment(), KodeinAware, IMainNavigator<Payable>, IMessageListener {

    override val kodein by kodein()
    val factory: PayableViewModelFactory by instance()
    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(PayableViewModel::class.java)
    }
    private lateinit var binding: PayableFragmentBinding

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.payable_fragment, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_payable_list_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = ""
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
                navController.navigate(R.id.action_payableFragment_to_payableEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI() {

        val pagedAdapter = PayablePagedListAdapter(viewModel, activity!!)
        val gridLayoutManager = GridLayoutManager(activity!!, 1)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                val viewType = pagedAdapter.getItemViewType(position)
                if(viewType == pagedAdapter.MAIN_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
            }
        }
        rcv_payable.apply {
            layoutManager = gridLayoutManager// LinearLayoutManager(this@OrdersFragment.context)
            setHasFixedSize(true)
            adapter = pagedAdapter// groupAdapter
        }
        viewModel.baseEoList.observe(viewLifecycleOwner, Observer {
            it.sortByDescending { it.py_doc_date }
            pagedAdapter.submitList(it)
        })

        viewModel.networkStateRV.observe(viewLifecycleOwner, Observer {
            progress_bar_payable.visibility =  if(viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
            txt_error_payable.visibility = if(viewModel.listIsEmpty() && it == NetworkState.ERROR) View.VISIBLE else View.GONE

            if(!viewModel.listIsEmpty()){
                pagedAdapter.setNetworkState(it)
            }
        })

        viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    override fun onItemDeleteClick(baseEo: Payable) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            onStarted()
            viewModel.confirmDelete(it)
        }
    }

    override fun onItemEditClick(baseEo: Payable) {
        val action = PayableFragmentDirections.actionPayableFragmentToPayableEntryFragment()
        action.pyId = baseEo.py_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Payable) {
        val action = PayableFragmentDirections.actionPayableFragmentToPayableEntryFragment()
        action.pyId = baseEo.py_Id
        action.mode ="View"
        navController.navigate(action)
    }

    override fun onStarted() {
        progress_bar_payable?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar_payable?.visibility = View.GONE
        payable_list_cl?.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar_payable?.visibility = View.GONE
        payable_list_cl?.snackbar(message)
    }
}
