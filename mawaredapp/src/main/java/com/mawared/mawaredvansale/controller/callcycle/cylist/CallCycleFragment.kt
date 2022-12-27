package com.mawared.mawaredvansale.controller.callcycle.cylist

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CallCycleAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.databinding.CallCycleFragmentBinding
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.call_cycle_fragment.*
import kotlinx.android.synthetic.main.call_cycle_fragment.progress_bar
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CallCycleFragment : ScopedFragment(), KodeinAware, SearchView.OnQueryTextListener {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("CallCycle")
    private val factory: CallCycleViewModelFactory by instance()

    private lateinit var binding: CallCycleFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CallCycleViewModel::class.java)
    }

    private lateinit var navController: NavController

    private var adapter = CallCycleAdapter(R.layout.call_cycle_row){e ->
        onItemEditClick(e)
    }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.call_cycle_fragment, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

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
        rcv_cy.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_cy.setLoadMoreFunction { loadList(viewModel.term ?: "" )}
        loadList(viewModel.term ?: "")

        navController = Navigation.findNavController(view)

    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        onDestroy()
        super.onDestroyView()
    }


    fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        cy_list_lc.snackbar(message)
    }

    fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        cy_list_lc.snackbar(message)
    }


    fun onItemEditClick(baseEo: Call_Cycle) {
        val action = CallCycleFragmentDirections.actionCallCycleFragmentToCallCycleEntryFragment()
        action.baseBO = baseEo
        navController.navigate(action)
    }

    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val search = menu?.findItem(R.id.app_bar_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.app_bar_search ->{
//
//            }
//            R.id.addBtn -> {
//                navController.navigate(R.id.action_ordersFragment_to_addOrderFragment)
//            }
//        }
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

    private fun loadList(term : String){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            onStarted()
            viewModel.loadData(list, term, adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Call_Cycle>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar?.visibility = View.GONE
    }
}
