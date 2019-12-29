package com.mawared.mawaredvansale.controller.sales.psorder.psorderentry

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.AutoCompleteProductAdapter
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
import com.mawared.mawaredvansale.databinding.PsorderEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_order_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*

class PSOrderEntryFragment : ScopedFragmentLocation() , KodeinAware, IMessageListener, IAddNavigator<Sale_Order_Items> {

    override val kodein by kodein()

    private val factory: PSOrderEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(PSOrderEntryViewModel::class.java)
    }

    lateinit var binding: PsorderEntryFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.psorder_entry_fragment, container, false)

        viewModel.ctx = activity!!
        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.docDate.value = "${LocalDate.now()}"
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_psorder_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)
        if(arguments != null){
            val args = PSOrderEntryFragmentArgs.fromBundle(arguments!!)
            viewModel.mode = args.mode
            if(viewModel.mode != "Add"){
                viewModel.setOrderId(args.orderId)
            }
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
                hideKeyboard()
                showDialog(context!!, getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ){
                    onStarted()
                    viewModel.location = getLocationData()
                    viewModel.onSave()
                }
            }
            R.id.close_btn -> {
                hideKeyboard()
                activity!!.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun bindUI() = GlobalScope.launch(Main){

        viewModel._baseEo.observe(this@PSOrderEntryFragment, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                activity!!.onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }

        })

        viewModel.entityEo.observe(this@PSOrderEntryFragment, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.docNo.value = it.so_no?.toString()
                viewModel.docDate.value = viewModel.returnDateString(it.so_date!!)
                viewModel.selectedCustomer?.cu_Id = it.so_customerId!!
                viewModel.selectedCustomer?.cu_name = it.so_customer_name
                binding.atcCustomer.setText("${it.so_customer_name}", true)
                viewModel.setItems(it.items)
            }
        })

        viewModel.soItems.observe(this@PSOrderEntryFragment, Observer {
            group_loading_order_entry.hide()
            if(it == null) return@Observer
            initRecyclerView(it.toOrderItemRow())
            viewModel.setTotals()
        })

        // bind customer to autocomplete
        val customerList = viewModel.customerList.await()
        customerList.observe(this@PSOrderEntryFragment, Observer { cu ->
            if(cu == null) return@Observer
            initCustomerAutocomplete(cu)

        })

        // bind products to autocomplete
        viewModel.productList.observe(this@PSOrderEntryFragment, Observer {
            if(it == null) return@Observer
            initProductAutocomplete(it)
        })

        viewModel.mProductPrice.observe(this@PSOrderEntryFragment, Observer {
            viewModel.unitPrice = if(it.pl_unitPirce == null) 0.00 else it.pl_unitPirce!!
        })

        viewModel.mVoucher.observe(this@PSOrderEntryFragment, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(this@PSOrderEntryFragment, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.00
        })

        viewModel.saleCurrency.observe(this@PSOrderEntryFragment, Observer {
            viewModel.bcCurrency = it
        })
        viewModel.setTerm("")
        viewModel.setVoucherCode("PSOrder")
        viewModel.setSaleCurrency("$")
        viewModel.setCurrencyId(App.prefs.saveUser!!.cr_Id!!)
        viewModel.setItems(null)
    }

    // init invoices items
    private fun initRecyclerView(rows: List<PSOrderItemRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }

        rcv_order_items.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    // convert invoice items to invoice items row
    private fun List<Sale_Order_Items>.toOrderItemRow(): List<PSOrderItemRow>{
        return this.map {
            PSOrderItemRow(it, viewModel)
        }
    }

    // init customer autocomplete view
    private fun initCustomerAutocomplete(customers: List<Customer>){
        val adapter = CustomerAdapter(context!!,
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
        val adapter = AutoCompleteProductAdapter(context!!,
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

    override fun onDelete(baseEo: Sale_Order_Items) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            viewModel.deleteItem(it)
        }
    }

    override fun onShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->

            viewModel.docDate.value = "${dayOfMonth}-${monthOfYear + 1}-${yr}"

        }, year, month, day)
        dpd.show()
    }

    // clear selected customer
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

    override fun onStarted() {
       group_loading_order_entry.show()
    }

    override fun onSuccess(message: String) {
        add_order_layout.snackbar(message)
        group_loading_order_entry.hide()
    }

    override fun onFailure(message: String) {
        add_order_layout.snackbar(message)
        group_loading_order_entry.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
