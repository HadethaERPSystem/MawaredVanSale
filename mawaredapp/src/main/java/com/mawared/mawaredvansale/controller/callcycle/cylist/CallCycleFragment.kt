package com.mawared.mawaredvansale.controller.callcycle.cylist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.CallCyclePagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.callcycle.cyentry.CyDialogFragment
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.databinding.CallCycleFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.call_cycle_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CallCycleFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Call_Cycle> {

    override val kodein by kodein()

    private val factory: CallCycleViewModelFactory by instance()

    private lateinit var binding: CallCycleFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CallCycleViewModel::class.java)
    }

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.call_cycle_fragment, container, false)
        viewModel.navigator = this
        viewModel.msgListener = this
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

    override fun onResume() {
        removeObservers()
        super.onResume()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun removeObservers(){
        viewModel.baseEoList.removeObservers(this)
        //viewModel.networkStateRV.removeObservers(this)
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

    private fun bindUI()= GlobalScope.launch(Main){
        try {
            val pagedAdapter = CallCyclePagedListAdapter(viewModel, requireActivity())
            val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val viewType = pagedAdapter.getItemViewType(position)
                    if (viewType == pagedAdapter.MAIN_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                    else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
                }
            }

            rcv_cy.apply {
                layoutManager = gridLayoutManager
                setHasFixedSize(true)
                adapter = pagedAdapter
            }

            viewModel.baseEoList.observe(viewLifecycleOwner, Observer {
                pagedAdapter.submitList(it)
            })

            viewModel.setCustomer(null)

            viewModel.networkStateRV.observe(viewLifecycleOwner, Observer{
                progress_bar.visibility = if (viewModel.listIsEmpty() && it.status == Status.RUNNING) View.VISIBLE else View.GONE
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


        }catch (e: Exception){
            Log.i("Exc", "Error is ${e.message}")
        }
    }

    override fun onStarted() {
        progress_bar.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar.visibility = View.GONE
        cy_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        cy_list_lc.snackbar(message)
    }

    override fun onItemDeleteClick(baseEo: Call_Cycle) {

    }

    override fun onItemEditClick(baseEo: Call_Cycle) {
        val action = CallCycleFragmentDirections.actionCallCycleFragmentToCallCycleEntryFragment()
        action.baseBO = baseEo

        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Call_Cycle) {

    }
}
