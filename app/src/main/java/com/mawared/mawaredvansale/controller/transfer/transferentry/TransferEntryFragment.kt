package com.mawared.mawaredvansale.controller.transfer.transferentry

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.ProductSearchAdapter
import com.mawared.mawaredvansale.controller.adapters.atc_Whs_Adapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.md.Warehouse
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items
import com.mawared.mawaredvansale.databinding.TransferEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.transfer_entry_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class TransferEntryFragment : ScopedFragment(), KodeinAware, IAddNavigator<Transfer_Items>, IMessageListener {

    override val kodein by kodein()

    private val factory: TransferEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(TransferEntryViewModel::class.java)
    }

    lateinit var binding: TransferEntryFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_transfer_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.transfer_entry_fragment, container, false)
        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.docDate.value = "${LocalDateTime.now()}"
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

        val bc = binding.root.findViewById<EditText>(R.id.edtxt_barcode)
        bc.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){
                    val b = viewModel.searchBarcode.value
                    return@OnKeyListener true
                }
                false
            })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            val args = TransferEntryFragmentArgs.fromBundle(arguments!!)
            viewModel.mode = args.mode
            if(viewModel.mode != "Add"){
                viewModel.setId(args.transferId)
            }
        }
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        super.onCreate(savedInstanceState)

    }
    fun initBarcode() {
        // this for activity
        // val scanner = IntentIntegrator(activity)
        //scanner.initiateScan()

        // this for fragment
        val scanner = IntentIntegrator.forSupportFragment(this)
        scanner.setDesiredBarcodeFormats(IntentIntegrator.CODE_128)
        scanner.setBeepEnabled(false)
        scanner.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if(result != null){
                if(result.contents == null){
                    add_layout.snackbar("")
                }else{
                    // get barcode
                    val barcode = result.contents
                }
            }else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
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
                if(!viewModel.isRunning){
                    hideKeyboard()
                    showDialog(context!!, getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ){
                        onStarted()
                        viewModel.onSave()
                    }
                }
            }
            R.id.close_btn -> {
                hideKeyboard()
                activity!!.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Main) {

        viewModel._baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                activity!!.onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }

        })

        viewModel.entityEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.docNo.value = it.tr_ref_no
                viewModel.docDate.value = viewModel.returnDateString(it.tr_doc_date!!)

                viewModel.selectToWarehouse?.wr_Id = it.tr_wr_Id!!
                viewModel.selectToWarehouse?.wr_description = it.tr_wr_name
                binding.atcToWarehouse.setText("${it.tr_wr_name}", true)
                viewModel.setItems(it.items)
            }
        })

        viewModel.items.observe(viewLifecycleOwner, Observer {
            pb_transfer.visibility = View.GONE
            if(it == null) return@Observer
            initRecyclerView(it.toRow())

        })

        // bind customer to autocomplete
        val whsList = viewModel.warEoList
        whsList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initToWhsAutocomplete(it)

        })

        // bind products to autocomplete
        viewModel.productList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initProductAutocomplete(it)
        })

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.setTerm("")
        viewModel.setVoucherCode("Transfer")
        viewModel.setItems(null)

    }

    // init invoices items
    private fun initRecyclerView(rows: List<TransferItemRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }

        rcv_items.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    // convert invoice items to invoice items row
    private fun List<Transfer_Items>.toRow(): List<TransferItemRow>{
        return this.map {
            TransferItemRow(it, viewModel)
        }
    }


    private fun initToWhsAutocomplete(list: List<Warehouse>){
        val adapter = atc_Whs_Adapter(context!!.applicationContext, R.layout.support_simple_spinner_dropdown_item, ArrayList<Warehouse>(list))
        binding.atcToWarehouse.threshold = 0
        binding.atcToWarehouse.setAdapter(adapter)
        binding.atcToWarehouse.setOnFocusChangeListener { _, b ->
            if(b) binding.atcToWarehouse.showDropDown()
        }
        binding.atcToWarehouse.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectToWarehouse = adapter.getItem(position)
        }

        val war = list.find { it.wr_Id == App.prefs.savedSalesman!!.sm_warehouse_id }
        if(war != null){
            if(App.prefs.savedSalesman!!.sm_warehouse_id != null){
                viewModel.selectToWarehouse = Warehouse(war.wr_description, war.wr_description_ar, war.wr_code)
                viewModel.selectToWarehouse?.wr_Id = App.prefs.savedSalesman!!.sm_warehouse_id!!
                viewModel.selectToWarehouse?.wr_description = App.prefs.savedSalesman!!.sm_warehouse_name
                binding.atcToWarehouse.setText("${App.prefs.savedSalesman!!.sm_warehouse_name}", true)
            }
        }
    }
    // init product autocomplete view
    private fun initProductAutocomplete(products: List<Product>){
        val adapter = ProductSearchAdapter(activity!!,
            R.layout.support_simple_spinner_dropdown_item,
            products
        )
        binding.atcProduct.threshold = 0
        binding.atcProduct.dropDownWidth = resources.displayMetrics.widthPixels
        binding.atcProduct.setAdapter(adapter)
        binding.atcProduct.setOnFocusChangeListener { _, b ->
            if(b) binding.atcProduct.showDropDown()
        }
        binding.atcProduct.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedProduct = adapter.getItem(position)
        }

    }

    // clear
    override fun clear(code: String) {
        when(code) {
           // "to_wr" -> binding.atcToWarehouse.setText("", true)
            "prod" -> binding.atcProduct.setText("", true)
        }

    }

    override fun onDelete(baseEo: Transfer_Items) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            viewModel.deleteItem(it)
        }
    }

    override fun onShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->
            viewModel.docDate.value = "${dayOfMonth}-${monthOfYear + 1}-${yr}"

        }, year, month, day)
        dpd.show()
    }

    override fun onStarted() {
        pb_transfer.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        pb_transfer.visibility = View.GONE
        add_layout.snackbar(message)
    }

    override fun onFailure(message: String) {
        pb_transfer.visibility = View.VISIBLE
        add_layout.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
