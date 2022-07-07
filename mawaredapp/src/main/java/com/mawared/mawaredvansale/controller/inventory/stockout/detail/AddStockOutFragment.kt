package com.mawared.mawaredvansale.controller.inventory.stockout.detail

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.StockOutLinesAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items
import com.mawared.mawaredvansale.databinding.AddStockOutFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.add_stock_out_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*

class AddStockOutFragment : ScopedFragmentLocation(), KodeinAware, IMessageListener{

    override val kodein by kodein()

    private val factory: AddStockOutViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(AddStockOutViewModel::class.java)
    }

    lateinit var binding : AddStockOutFragmentBinding

    private lateinit var navController: NavController

    private var adapter = StockOutLinesAdapter(R.layout.stockout_item_row)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.add_stock_out_fragment, container, false)
        viewModel.msgListener = this
        viewModel.docDate.value = "${LocalDate.now()}"
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

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
        rcv_stockout_items.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        //rcv_stockout_items.setLoadMoreFunction { loadList(viewModel.term) }

        if(arguments != null) {
            val args =
                com.mawared.mawaredvansale.controller.inventory.stockout.detail.AddStockOutFragmentArgs.fromBundle(
                    requireArguments()
                )
            loadList(args.docId)
        }

        navController = Navigation.findNavController(view)
    }

    fun onShowDatePicker(v: View) {

    }

    override fun onStarted() {

    }

    override fun onSuccess(message: String) {

    }

    override fun onFailure(message: String) {

    }

    private fun loadList(doc_id: Int){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            viewModel.loadLines(list, doc_id,adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Stockout_Items>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
    }
}
