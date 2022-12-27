package com.mawared.mawaredvansale.controller.inventory.stockout.stockoutlist

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.StockoutAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.databinding.StockOutFragmentBinding
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.stock_out_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class StockOutFragment : Fragment(), KodeinAware, SearchView.OnQueryTextListener  {

    override val kodein by kodein()
    private val permission = MenuSysPrefs.getPermission("DocStock-Out")
    private val factory: StockOutViewModelFactory by instance()

    private lateinit var binding: StockOutFragmentBinding

    private var adapter = StockoutAdapter(R.layout.item_rv_stock_row, permission){ e, t ->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
        }
    }

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(StockOutViewModel::class.java)
    }

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.stock_out_fragment, container, false)

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
        rcv_stockout.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_stockout.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")
        navController = Navigation.findNavController(view)

    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
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
        when (item.itemId) {
            R.id.app_bar_search -> {

            }
            R.id.addBtn -> {
                navController.navigate(R.id.action_stockOutFragment_to_addStockOutFragment)
            }
        }
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

    fun onItemDeleteClick(baseEo: Stockout) {

    }

    fun onItemEditClick(baseEo: Stockout) {

    }

    fun onItemViewClick(baseEo: Stockout) {
        val action = StockOutFragmentDirections.actionStockOutFragmentToAddStockOutFragment()
        action.docId = baseEo.docEntry
        action.mode = "View"
        navController.navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    private fun loadList(term : String){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            onStarted()
            viewModel.loadData(list, term,adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Stockout>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        onSuccess()
    }

    fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    fun onSuccess() {
        progress_bar?.visibility = View.GONE
    }

    fun onFailure() {
        progress_bar?.visibility = View.GONE
    }
}
