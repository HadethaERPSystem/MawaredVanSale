package com.mawared.mawaredvansale.controller.inventory.stockin.addstockin

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.SelectDocForStockinAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDoc
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.select_doc_for_stockin_fragment.*
import kotlinx.android.synthetic.main.select_doc_for_stockin_fragment.progress_bar
import kotlinx.android.synthetic.main.select_doc_for_stockin_fragment.search_view
import kotlinx.android.synthetic.main.select_invoice_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SelectDocForStockinFragment : Fragment(), KodeinAware {
    override val kodein by kodein()

    private val factory: SelectDocForStockinViewModelFactory by instance()

    private var adapter = SelectDocForStockinAdapter(R.layout.item_rv_stockin_doc){
        viewModel.baseEo = it
        val action = SelectDocForStockinFragmentDirections.actionSelectDocForStockinFragmentToSelectDocForStockinItemsFragment()
        action.sale = it
        action.sinType = viewModel.stockType
        navController.navigate(action)
    }

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(SelectDocForStockinViewModel::class.java)
    }

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.select_doc_for_stockin_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var cols = 1
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 2
        }
        @Suppress("UNCHECKED_CAST")
        rv_selectDocs.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rv_selectDocs.setLoadMoreFunction {
            var term = if(viewModel.term.isNullOrEmpty()) "" else viewModel.term
            loadList(viewModel.term ?: "")
        }
        loadList("")

        if(arguments != null){
            val args = SelectDocForStockinFragmentArgs.fromBundle(requireArguments())
            viewModel.stockType = args.sinType

        }

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                viewModel.term = p0 ?: ""
                adapter.setList(null, 0)
                loadList(viewModel.term!!)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.term = p0 ?: ""
                adapter.setList(null, 0)
                loadList(viewModel.term!!)
                return false
            }
        })

        navController = Navigation.findNavController(view)
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

    fun showResult(list: List<InventoryDoc>, pageCount: Int) = HandlerUtils.runOnUiThread {
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