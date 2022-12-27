package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.InvoiceItemsAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.common.dialog.GenericDialog.showDialog
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDocLines
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.select_invoice_items_fragment.*
import kotlinx.android.synthetic.main.select_invoice_items_fragment.search_view
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SelectInvoiceItemsFragment: Fragment(), KodeinAware, IMessageListener {

        override val kodein by kodein()

        private val factory: SelectInvoiceItemsViewModelFactory by instance()

        private var adapter = InvoiceItemsAdapter(R.layout.item_rv_invoice_item,{item, loc, qty , Success ->
            viewModel.removeLine(item, loc, qty, Success)
        },  { item, loc, qty, isadap ->
            viewModel.addLine(item, loc, qty, isadap)
        })


    val viewModel by lazy {
            ViewModelProviders.of(this, factory).get(SelectInvoiceItemsViewModel::class.java)
        }

        lateinit var navController: NavController

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.select_invoice_items_fragment, container, false)
            viewModel.ctx = requireContext()
            viewModel.msgListener = this
            bindUI()
            return view
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            var cols = 1
            val currentOrientation = resources.configuration.orientation
            if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
                cols = 2
            }
            @Suppress("UNCHECKED_CAST")
            rv_selectInvoiceItems.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
            rv_selectInvoiceItems.setLoadMoreFunction { loadList(viewModel.term ?: "") }

            if(arguments != null){
                val args = SelectInvoiceItemsFragmentArgs.fromBundle(requireArguments())
                viewModel.baseEo = args.sale
                viewModel.doc_id = viewModel.baseEo!!.docEntry
                viewModel.stockType = args.sotType

                loadList(viewModel.term ?: "")
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
                onStart()
                viewModel.loadData(list, viewModel.doc_id, term,adapter.pageCount + 1){data, pageCount ->
                    showResult(data!!, pageCount)
                }
            }
        }

        fun showResult(list: List<InventoryDocLines>, pageCount: Int) = HandlerUtils.runOnUiThread {
            viewModel.setLoc(list)
            adapter.setList(list, pageCount)
            progress_bar?.visibility = View.GONE
        }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        super.onCreate(savedInstanceState)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when(item.itemId){
            R.id.save_btn ->{
                if(!viewModel.isRunning) {
                    viewModel.isRunning = true
                    hideKeyboard()

                    showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null, {
                        onStarted()
                        //viewModel.location = getLocationData()
                        viewModel.onSave({// On Success
                            viewModel.isRunning = false
                            onSuccess(getString(R.string.msg_success_saved))
                            requireActivity().onBackPressed()
                        },{// On Fail
                            viewModel.isRunning = false
                        })
                    })
                }
            }
            R.id.close_btn -> {
                hideKeyboard()
                requireActivity().onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main){

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })
        viewModel.setVoucherCode("StockOut")
    }

    override fun onStarted() {
        progress_bar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar?.visibility = View.GONE
        ll_selectedItems?.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar?.visibility = View.GONE
        ll_selectedItems?.snackbar(message)
    }

    fun hideKeyboard(){
        val view = requireActivity().currentFocus
        if(view != null){
            val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if(inputManager.isAcceptingText){
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}