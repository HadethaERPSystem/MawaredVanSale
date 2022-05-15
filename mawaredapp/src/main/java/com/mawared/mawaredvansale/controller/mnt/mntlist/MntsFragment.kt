package com.mawared.mawaredvansale.controller.mnt.mntlist

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.MntsPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.databinding.MntsFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.mnts_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MntsFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Mnts> {

    override val kodein by kodein()
    private val factory: MntsVieModelFactory by instance()

    private lateinit var binding: MntsFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(MntsViewModel::class.java)
    }

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.mnts_fragment, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        removeObservers()
        bindUI()

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
        viewModel.mntsList.removeObservers(this)

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
                navController.navigate(R.id.action_mntsFragment_to_mntEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindUI()= GlobalScope.launch(Dispatchers.Main) {
        try {
        val pagedAdapter = MntsPagedListAdapter(viewModel, requireActivity())
        val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = pagedAdapter.getItemViewType(position)
                if (viewType == pagedAdapter.MAIN_VIEW_TYPE) return 1    // ORDER_VIEW_TYPE will occupy 1 out of 3 span
                else return 1                                            // NETWORK_VIEW_TYPE will occupy all 3 span
            }
        }
        rcv_mnts.apply {
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
            adapter = pagedAdapter
        }

        viewModel.mntsList.observe(viewLifecycleOwner, Observer {
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
        } catch (e: Exception) {
            Log.i("Exc", "Error is ${e.message}")
        }

    }

    override fun onItemDeleteClick(baseEo: Mnts) {
        TODO("Not yet implemented")
    }

    override fun onItemEditClick(baseEo: Mnts) {
        val action = MntsFragmentDirections.actionMntsFragmentToMntEntryFragment()
        action.mntId = baseEo.mntId
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Mnts) {
        val action = MntsFragmentDirections.actionMntsFragmentToMntEntryFragment()
        action.mntId = baseEo.mntId
        action.mode ="View"
        navController.navigate(action)
    }

    override fun onStarted() {
        progress_bar.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar.visibility = View.GONE
        mnts_list_cl.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        mnts_list_cl.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}