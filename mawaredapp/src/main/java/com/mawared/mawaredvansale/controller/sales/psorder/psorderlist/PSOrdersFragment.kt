package com.mawared.mawaredvansale.controller.sales.psorder.psorderlist

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.PSOrderPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.databinding.PsordersFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.psorders_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PSOrdersFragment : ScopedFragment(), KodeinAware, IMainNavigator<Sale_Order>, IMessageListener {

    override val kodein by kodein()

    private val factory: PSOrdersViewModelFactory by instance()

    private lateinit var viewModel: PSOrdersViewModel
    private lateinit var binding: PsordersFragmentBinding

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle?): View {

        viewModel = ViewModelProviders.of(this, factory).get(PSOrdersViewModel::class.java)

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.psorders_fragment, container, false)

        viewModel.navigator = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        bindUI()
        binding.pullToRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { viewModel.refresh() }
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
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
                navController.navigate(R.id.action_PSOrdersFragment_to_PSOrderEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindUI() = GlobalScope.launch(Main) {
        val pagedAdapter = PSOrderPagedListAdapter(viewModel, requireActivity())
        val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                val viewType = pagedAdapter.getItemViewType(position)
                if(viewType == pagedAdapter.ORDER_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
            }
        }
        rcv_psorders.apply {
            layoutManager = gridLayoutManager// LinearLayoutManager(this@OrdersFragment.context)
            setHasFixedSize(true)
            adapter = pagedAdapter// groupAdapter
        }

        viewModel.orders.observe(viewLifecycleOwner, Observer {
            pagedAdapter.submitList(it)
        })
        viewModel.setCustomer(null)

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

    override fun onItemDeleteClick(baseEo: Sale_Order) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            onStarted()
            viewModel.confirmDelete(it)
        })
    }

    override fun onItemEditClick(baseEo: Sale_Order) {
        val action = PSOrdersFragmentDirections.actionPSOrdersFragmentToPSOrderEntryFragment()
        action.orderId = baseEo.so_id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Sale_Order) {
        val action = PSOrdersFragmentDirections.actionPSOrdersFragmentToPSOrderEntryFragment()
        action.orderId = baseEo.so_id
        action.mode ="View"
        navController.navigate(action)
    }

    override fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        psorder_list_cl.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        psorder_list_cl.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
