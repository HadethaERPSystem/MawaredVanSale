package com.mawared.mawaredvansale.controller.sales.delivery.deliverylist

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.DeliveryPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist.InvoicesFragmentDirections
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.databinding.DeliveryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.delivery_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class DeliveryFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Delivery> {

    override val kodein by kodein()

    private val factory: DeliveryViewModelFactory by instance()

    private lateinit var binding: DeliveryFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(DeliveryViewModel::class.java)
    }

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.delivery_fragment, container, false)

        viewModel.navigator = this
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItem = menu.findItem(R.id.addBtn)
        menuItem.isVisible = false
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
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI()= GlobalScope.launch(Dispatchers.Main) {

        val pagedAdapter = DeliveryPagedListAdapter(viewModel, requireActivity())
        val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = pagedAdapter.getItemViewType(position)
                if (viewType == pagedAdapter.MAIN_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
            }
        }
        rcv_delivery.apply {
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
            adapter = pagedAdapter
        }
        viewModel.entityEoList.observe(viewLifecycleOwner, Observer { dl ->
            pagedAdapter.submitList(dl)
        })

        viewModel.baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                mPrint(it)
            }
        })
        viewModel.setCustomer(null)

        viewModel.networkStateRV.observe(viewLifecycleOwner, Observer {
            progress_bar.visibility = if (viewModel.listIsEmpty() && it.status == Status.RUNNING) View.VISIBLE else View.GONE

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

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            progress_bar.visibility =
                if (viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
        })
    }


    override fun onStarted() {
        progress_bar.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar.visibility = View.GONE
        delivery_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        delivery_list_lc.snackbar(message)
    }

    override fun onItemDeleteClick(baseEo: Delivery) {
    }

    override fun onItemEditClick(baseEo: Delivery) {
        val action = DeliveryFragmentDirections.actionDeliveryFragmentToDeliveryEntryFragment()
        action.deliveryId = baseEo.dl_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Delivery) {
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToAddInvoiceFragment()
        action.saleId = baseEo.dl_Id
        action.mode = "View"
        navController.navigate(action)
    }

    fun mPrint(baseEo: Delivery){

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
