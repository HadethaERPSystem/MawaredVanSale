package com.mawared.mawaredvansale.controller.sales.invoices.addinvoice

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.AutoCompleteCustomerAdapter
import com.mawared.mawaredvansale.controller.adapters.AutoCompleteProductAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.databinding.AddInvoiceFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_invoice_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*

class AddInvoiceFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Sale_Items>,
    IMessageListener {

    override val kodein by kodein()

    private val factory: AddInvoiceViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(AddInvoiceViewModel::class.java)
    }

    lateinit var binding: AddInvoiceFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.add_invoice_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.docDate.value = "${LocalDate.now()}"
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_invoice_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            val args = AddInvoiceFragmentArgs.fromBundle(arguments!!)
            viewModel.mode = args.mode
            if(viewModel.mode != "Add"){
                viewModel.setInvoiceId(args.saleId)
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
                    addInvoice_layout.snackbar("")
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
                showDialog(context!!, getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ){
                    onStarted()
                    viewModel.location = getLocationData()
                    viewModel.onSave()
                }
            }
            R.id.close_btn -> {
                activity!!.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Main) {

        viewModel.savedEntity.observe(this@AddInvoiceFragment, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                activity!!.onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }

        })

        viewModel.entityEo.observe(this@AddInvoiceFragment, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.docNo.value = it.sl_doc_no?.toString()
                viewModel.docDate.value = viewModel.returnDateString(it.sl_doc_date!!)
                viewModel.selectedCustomer?.cu_Id = it.sl_customerId!!
                viewModel.selectedCustomer?.cu_name = it.sl_customer_name
                binding.atcCustomer.setText("${it.sl_customer_name}", true)
                viewModel.setItems(it.items)
            }
        })

        viewModel.invoiceItems.observe(this@AddInvoiceFragment, Observer {
            group_loading.hide()
            if(it == null) return@Observer
            initRecyclerView(it.toInvoiceItemRow())

        })

        // bind customer to autocomplete
        val customerList = viewModel.customerList.await()
        customerList.observe(this@AddInvoiceFragment, Observer { cu ->
            if(cu == null) return@Observer
            initCustomerAutocomplete(cu)

        })

        // bind products to autocomplete
        viewModel.productList.observe(this@AddInvoiceFragment, Observer {
            if(it == null) return@Observer
            initProductAutocomplete(it)
        })

        viewModel.mProductPrice.observe(this@AddInvoiceFragment, Observer {
            viewModel.unitPrice = if(it.pl_unitPirce == null) 0.00 else it.pl_unitPirce!!
        })

        viewModel.mVoucher.observe(this@AddInvoiceFragment, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(this@AddInvoiceFragment, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.00
        })

        viewModel.saleCurrency.observe(this@AddInvoiceFragment, Observer {
            viewModel.bcCurrency = it
        })

        viewModel.lCurrency.observe(this@AddInvoiceFragment, Observer {
            viewModel.lcCurrency = it
        })

        viewModel.setTerm("")
        viewModel.setVoucherCode("SaleInvoice")
        viewModel.setSaleCurrency("$")
        viewModel.setLCurrency("IQD")
        viewModel.setCurrencyId(App.prefs.saveUser!!.cr_Id!!)
        viewModel.setItems(null)
    }

    // init invoices items
    private fun initRecyclerView(rows: List<InvoiceItemRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }

        rcv_invoice_items.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    // convert invoice items to invoice items row
    private fun List<Sale_Items>.toInvoiceItemRow(): List<InvoiceItemRow>{
        return this.map {
            InvoiceItemRow(it, viewModel)
        }
    }

    // init customer autocomplete view
    private fun initCustomerAutocomplete(customers: List<Customer>){
        val adapter = AutoCompleteCustomerAdapter(context!!.applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            customers
        )
        binding.atcCustomer.threshold = 0
        binding.atcCustomer.dropDownWidth = resources.displayMetrics.widthPixels
        binding.atcCustomer.setAdapter(adapter)
        binding.atcCustomer.setOnFocusChangeListener { _, b ->
            if(b) binding.atcCustomer.showDropDown()
        }
        binding.atcCustomer.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomer = adapter.getItem(position)
        }

    }

    // init product autocomplete view
    private fun initProductAutocomplete(products: List<Product>){
        val adapter = AutoCompleteProductAdapter(context!!.applicationContext,
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
            viewModel.setProductId(viewModel.selectedProduct!!.pr_Id)
        }

    }

    // clear
    override fun clear(code: String) {
        when(code) {
            "cu"-> {
                binding.atcCustomer.setText("", true)
            }
            "prod"-> {
                binding.atcProduct.setText("", true)
            }
        }

    }

    override fun onDelete(baseEo: Sale_Items) {
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
        group_loading.show()
    }

    override fun onSuccess(message: String) {
        group_loading.hide()
        addInvoice_layout.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading.hide()
        addInvoice_layout.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
