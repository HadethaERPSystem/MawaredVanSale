package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnentry

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter1
import com.mawared.mawaredvansale.controller.adapters.ProductSearchAdapter
import com.mawared.mawaredvansale.controller.adapters.atc_invoices_Adapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items
import com.mawared.mawaredvansale.databinding.SaleReturnEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.sale_return_entry_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*

class SaleReturnEntryFragment : ScopedFragmentLocation(), KodeinAware, IMessageListener, IAddNavigator<Sale_Return_Items> {

    override val kodein by kodein()
    private val factory: SaleReturnEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(SaleReturnEntryViewModel::class.java)
    }

    private lateinit var binding: SaleReturnEntryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.sale_return_entry_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.resources = resources

        viewModel.doc_date.value = "${LocalDate.now()}"
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_salereturn_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)
        if(arguments != null){
            val args = SaleReturnEntryFragmentArgs.fromBundle(requireArguments())
            viewModel.mode = args.mode
            if(viewModel.mode == "View") viewModel.visible = View.GONE else viewModel.visible = View.VISIBLE
            if(viewModel.mode != "Add"){
                viewModel.setId(args.returnId)
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
        if(viewModel.mode == "View"){
            inflater.inflate(R.menu.view_menu, menu)
        }else{
            inflater.inflate(R.menu.add_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_btn ->{
                if(!viewModel.isRunning){
                    viewModel.isRunning = true
                    hideKeyboard()
                    showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ,{
                        onStarted()
                        viewModel.location = getLocationData()
                        viewModel.onSave()
                    },{
                        viewModel.isRunning = false
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
    fun bindUI() = GlobalScope.launch(Main){

        viewModel._baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                doPrint(it)
                requireActivity().onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }
        })

        viewModel.entityEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.doc_no.value = it.sr_doc_no?.toString()
                viewModel.doc_date.value = viewModel.returnDateString(it.sr_doc_date!!)
                viewModel.oCu_Id = it.sr_customerId!!
                viewModel.allowed_select_prod.value = true
                viewModel.rowNo = it.items.maxByOrNull { it.srd_rowNo!! }?.srd_rowNo ?: 0
                binding.atcCustomer.setText("${it.sr_customer_name}", true)
                viewModel.setItems(it.items)
            }
        })

        viewModel.srItems.observe(viewLifecycleOwner, Observer {
            progress_bar_entry_return?.visibility = View.GONE
            if(it == null) return@Observer
            initRecyclerView(it.toSRItemRow())
            viewModel.setTotals()
        })

        // Customer autocomplete settings
        val adapter = CustomerAdapter1(requireContext(), R.layout.support_simple_spinner_dropdown_item )

        binding.atcCustomer.threshold = 0
        binding.atcCustomer.dropDownWidth = resources.displayMetrics.widthPixels - 10
        binding.atcCustomer.setAdapter(adapter)
        binding.btnOpenCustomer.setOnClickListener {
            binding.atcCustomer.showDropDown()
        }
        binding.atcCustomer.setOnItemClickListener { _, _, position, _ ->
            viewModel.allowed_select_prod.value = true
            viewModel.selectedCustomer = adapter.getItem(position)
            if(viewModel.oCu_Id != viewModel.selectedCustomer?.cu_ref_Id){
                viewModel.clearItems()
            }
            viewModel.setPriceCategory()
            binding.atcCustomer.dismissDropDown()
            viewModel.setTerm("")
            binding.atcProduct.requestFocus()
        }

        binding.atcCustomer.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.selectedCustomer == null) viewModel.term.value = s.toString() else binding.atcCustomer.dismissDropDown()
            }
        })
        // bind customer to autocomplete
        viewModel.customerList.observe(viewLifecycleOwner, Observer { cu ->
            if(cu != null){
                adapter.setCustomers(cu)
                if(viewModel.mode != "Add" && cu.isNotEmpty() && viewModel._entityEo != null){
                    viewModel.selectedCustomer = cu.find { it.cu_ref_Id == viewModel._entityEo?.sr_customerId}
                }else{
                    binding.atcCustomer.showDropDown()
                }
            }
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            progress_bar_entry_return.visibility =  if(it == NetworkState.LOADING) View.VISIBLE else View.GONE
        })

        // bind products to autocomplete
        viewModel.productList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initProductAutocomplete(it)
        })

        viewModel.InvoicesList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initInvoices(it)
        })

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(viewLifecycleOwner, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.0
        })

        viewModel.setVoucherCode("SaleReturn")
        viewModel.setCurrencyId(App.prefs.saveUser!!.sf_cr_Id!!)
        viewModel.setItems(null)

        if(viewModel.mode != "Add") {
            viewModel.setTerm("")
        }else{
            viewModel.term.value = ""
        }
    }

    // init invoices items
    private fun initRecyclerView(rows: List<SaleReturnItemRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }

        rcv_sale_return_items.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    // convert invoice items to invoice items row
    private fun List<Sale_Return_Items>.toSRItemRow(): List<SaleReturnItemRow>{
        return this.map {
            SaleReturnItemRow(it, viewModel)
        }
    }

    // init product autocomplete view
    private fun initProductAutocomplete(products: List<Product>){
        val adapter = ProductSearchAdapter(requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            products
        )
        binding.atcProduct.threshold = 0
        binding.atcProduct.dropDownWidth = resources.displayMetrics.widthPixels - 10
        binding.atcProduct.setAdapter(adapter)
        binding.btnOpenItems.setOnClickListener {
            binding.atcProduct.showDropDown()
        }

        binding.atcProduct.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedProduct = adapter.getItem(position)
            viewModel.doc_unit_price.value = ""
            viewModel.doc_expiry.value = ""
            viewModel.selectedInvoice = null
            viewModel.unitPrice = 0.0
            viewModel.setInvoices("")

            binding.atcInvoices.showDropDown()
        }
    }

    private fun initInvoices(invoices: List<Product>){
        val adapter = atc_invoices_Adapter(requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            invoices
        )
        binding.atcInvoices.threshold = 0
        binding.atcInvoices.dropDownWidth = resources.displayMetrics.widthPixels - 10
        binding.atcInvoices.setAdapter(adapter)
        binding.btnOpenInvoices.setOnClickListener {
            binding.atcInvoices.showDropDown()
        }
        binding.atcInvoices.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedInvoice = adapter.getItem(position)
            viewModel.invQty = viewModel.selectedInvoice!!.pr_qty ?: 0.0
            viewModel.unitPrice = viewModel.selectedInvoice!!.pr_unit_price ?: 0.0
            viewModel.doc_unit_price.value = viewModel.selectedInvoice!!.pr_unit_price.toString() ?: "0"
            viewModel.ref_rowNo = viewModel.selectedInvoice!!.ref_rowNo
            viewModel.ref_Id = viewModel.selectedInvoice!!.ref_Id
            viewModel.ref_no = viewModel.selectedInvoice!!.ref_no
            //viewModel.doc_expiry.value =viewModel.returnDateString(viewModel.selectedInvoice!!.pr_expiry_date ?: "")
            //viewModel.doc_unit_price.value = viewModel.numberFormat(viewModel.selectedInvoice!!.pr_unit_price ?: 0.0)
            //viewModel.setProductId(viewModel.selectedProduct!!.pr_Id)
        }
    }

    override fun onDelete(baseEo: Sale_Return_Items) {
        showDialog(requireContext(), getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo,{
            viewModel.deleteItem(it)
        })
    }

    override fun onShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->

            viewModel.doc_date.value =  "${yr}-${monthOfYear + 1}-${dayOfMonth}"//"${dayOfMonth}-${monthOfYear + 1}-${yr}"

        }, year, month, day)
        dpd.show()
    }

    override fun onStarted() {
        progress_bar_entry_return?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar_entry_return?.visibility = View.GONE
        sale_return_entry_layout?.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar_entry_return?.visibility = View.GONE
        sale_return_entry_layout?.snackbar(message)
    }

    override fun clear(code: String) {
        when(code) {
            "cu"-> {
                viewModel.oCu_Id = viewModel.selectedCustomer?.cu_Id
                viewModel.allowed_select_prod.value = false
                binding.atcCustomer.setText("", true)
            }
            "prod"-> {
                binding.atcProduct.setText("", true)
            }
            "invoices"->{
                binding.atcInvoices.setText("", true)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    private fun doPrint(entityEo: Sale_Return) {
        if (App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                val tickets = GenerateTicket(requireContext(), lang).create(
                    entityEo,
                    URL_LOGO + "co_black_logo.png",//R.drawable.ic_logo_black,
                    "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION",
                    null,
                    null
                )

                TicketPrinting(requireContext(), tickets).run()
            } catch (e: Exception) {
                onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        }
    }
}
