package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

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
import com.mawared.mawaredvansale.controller.adapters.SelectInvoiceAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDoc
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.select_invoice_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SelectInvoiceFragment : Fragment(), KodeinAware {
    override val kodein by kodein()

    private val factory: SelectInvoiceViewModelFactory by instance()

    private var adapter = SelectInvoiceAdapter(R.layout.item_rv_invoice){
        viewModel.baseEo = it
        val action = SelectInvoiceFragmentDirections.actionSelectInvoiceFragmentToSelectInvoiceItemsFragment()
        action.sale = it
        action.sotType = viewModel.stockType
        navController.navigate(action)
    }

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(SelectInvoiceViewModel::class.java)
    }

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.select_invoice_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var cols = 1
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 2
        }
        @Suppress("UNCHECKED_CAST")
        rv_selectInvoices.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rv_selectInvoices.setLoadMoreFunction {
            var term = if(viewModel.term.isNullOrEmpty()) "" else viewModel.term
            loadList(viewModel.term ?: "")
        }
        loadList("")

        if(arguments != null){
            val args = SelectInvoiceFragmentArgs.fromBundle(requireArguments())
            viewModel.stockType = args.sotType

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
            viewModel.loadData(list, term,adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<InventoryDoc>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
    }
}