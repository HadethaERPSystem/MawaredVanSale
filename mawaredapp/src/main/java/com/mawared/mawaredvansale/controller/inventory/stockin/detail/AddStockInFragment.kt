package com.mawared.mawaredvansale.controller.inventory.stockin.detail

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.StockInLinesAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.databinding.AddStockInFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.add_stock_in_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate

class AddStockInFragment : Fragment(), KodeinAware, IMessageListener {

    override val kodein by kodein()

    val factory: AddStockInViewModelFactory by instance()

    lateinit var binding: AddStockInFragmentBinding

   val viewModel by lazy {
       ViewModelProviders.of(this, factory).get(AddStockInViewModel::class.java)
   }

    private var adapter = StockInLinesAdapter(R.layout.stockin_item_row)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.add_stock_in_fragment, container, false)


        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.docDate.value = "${LocalDate.now()}"

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
        rcv_stockin_items.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)

        if(arguments != null) {
            val args = AddStockInFragmentArgs.fromBundle(
                    requireArguments()
                )
            loadList(args.docId)
        }

    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_btn ->{
                //viewModel.saveInvoice()
            }
            R.id.close_btn -> {
                requireActivity().onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onStarted() {
        //progressBar_loading.show()
    }

    override fun onSuccess(message: String) {
        //addInvoice_layout.snackbar(message)
        //progressBar_loading.hide()
    }

    override fun onFailure(message: String) {

        //progressBar_loading.hide()
        //addInvoice_layout.snackbar(message)
        //toast(message)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun loadList(doc_id: Int){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            viewModel.loadLines(list, doc_id,adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Stockin_Items>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
    }
}
