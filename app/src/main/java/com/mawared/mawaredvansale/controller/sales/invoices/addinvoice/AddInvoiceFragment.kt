package com.mawared.mawaredvansale.controller.sales.invoices.addinvoice

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter
import com.mawared.mawaredvansale.controller.adapters.atc_prod_expiry_Adapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.databinding.AddInvoiceFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
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
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
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
        loadLocale()
        val view = inflater.inflate(R.layout.add_invoice_fragment, container, false)

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.add_invoice_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.docDate.value = "${LocalDate.now()}"
        viewModel.ctx = activity!!
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

    override fun onResume() {
        removeObservers()
        super.onResume()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun removeObservers(){
        viewModel._baseEo.removeObservers(this)
        viewModel.entityEo.removeObservers(this)
        viewModel.invoiceItems.removeObservers(this)
        viewModel.productList.removeObservers(this)
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


    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Main) {

        viewModel._baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                doPrint(it)
                activity!!.onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }

        })

        viewModel.entityEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.docNo.value = it.sl_doc_no?.toString()
                viewModel.docDate.value = viewModel.returnDateString(it.sl_doc_date!!)
                viewModel.selectedCustomer?.cu_ref_Id = it.sl_customerId!!
                viewModel.selectedCustomer?.cu_name = it.sl_customer_name
                binding.atcCustomer.setText("${it.sl_customer_name}", true)
                viewModel.setItems(it.items)
            }
        })

        viewModel.invoiceItems.observe(viewLifecycleOwner, Observer {
            llProgressBar?.visibility = View.GONE
            if(it == null) return@Observer
            initRecyclerView(it.toInvoiceItemRow())
            viewModel.setTotals()
        })

        // bind customer to autocomplete
        val customerList = viewModel.customerList.await()
        customerList.observe(viewLifecycleOwner, Observer { cu ->
            if(cu == null) return@Observer
            initCustomerAutocomplete(cu)

        })

        // bind products to autocomplete
        viewModel.productList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initProductAutocomplete(it)
        })

        viewModel.mProductPrice.observe(viewLifecycleOwner, Observer {
            viewModel.unitPrice = if(it.pl_unitPirce == null) 0.00 else it.pl_unitPirce!!
        })

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(viewLifecycleOwner, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.00
        })

        viewModel.mDiscount.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel.allowed_discount.value = false
                viewModel.discount = it
            }else{
                viewModel.allowed_discount.value = true
            }

        })
        viewModel.setVoucherCode("SaleInvoice")
        viewModel.setCurrencyId(App.prefs.saveUser!!.sl_cr_Id!!)
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
            viewModel.allowed_select_prod.value = true
            viewModel.selectedCustomer = adapter.getItem(position)
            viewModel.setPriceCategory()
            viewModel.setTerm("")
        }

    }

    // init product autocomplete view
    private fun initProductAutocomplete(products: List<Product>){
        val adapter = atc_prod_expiry_Adapter(context!!,
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
            viewModel.unitPrice = viewModel.selectedProduct!!.pr_unit_price ?: 0.00
            //viewModel.setProductId(viewModel.selectedProduct!!.pr_Id)
        }

    }

    // clear
    override fun clear(code: String) {
        when(code) {
            "cu"-> {
                viewModel.allowed_select_prod.value = false
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
        llProgressBar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        llProgressBar?.visibility = View.GONE
        addInvoice_layout?.snackbar(message)
    }

    override fun onFailure(message: String) {
        llProgressBar?.visibility = View.GONE
        addInvoice_layout?.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    private fun doPrint(entityEo: Sale){
        //val lang = Locale.getDefault().toString().toLowerCase()
        val config = activity!!.resources.configuration
        val isRTL = if(config.layoutDirection == View.LAYOUT_DIRECTION_LTR) false else true
        var bmp: Bitmap? = null // BitmapFactory.decodeResource(ctx!!.resources, R.drawable.ic_logo_black)

        val mngr: AssetManager = context!!.getAssets()
        var `is`: InputStream? = null
        try {
            `is` = mngr.open("images/co_logo.bmp")
            bmp = BitmapFactory.decodeStream(`is`)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        val fontNameEn = "assets/fonts/arial.ttf"
        val fontNameAr = "assets/fonts/arial.ttf"// "assets/fonts/droid_kufi_regular.ttf"
        try {

            val imgLogo = RepLogo(bmp, 10F, 800F)
            val header: ArrayList<HeaderFooterRow> = arrayListOf()
            var tbl: HashMap<Int, TCell> = hashMapOf()
            val rws: ArrayList<CTable> = arrayListOf()
            val phones = if(entityEo.sl_org_phone != null) entityEo.sl_org_phone!!.replace("|", "\n\r") else ""

            header.add(HeaderFooterRow(0, null, "Al-Nadir Trading Company",  14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(1, null, "${entityEo.sl_org_name}", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(2, null, phones, 11F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            //header.add(HeaderFooterRow(3, null, "Asia: 0770-6502228", 20F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(3, null, "شركة النادر التجارية", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameAr ))
            header.add(HeaderFooterRow(4, null, "", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(5, null, "", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))

            tbl.put(0,TCell("", 9F, false, 2f, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(1,TCell(activity!!.resources!!.getString(R.string.rpt_list_name), 9f, false, 15F, "", Element.ALIGN_RIGHT, 0))
            tbl.put(2,TCell(entityEo.sl_vo_name!!,9F, false, 30F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

            tbl.put(3,TCell(activity!!.resources!!.getString(R.string.rpt_invoice_no), 9F, false, 15F, "", Element.ALIGN_RIGHT, 0))
            tbl.put(4,TCell(entityEo.sl_refNo!!,9F, false, 30F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

            tbl.put(5,TCell(activity!!.resources!!.getString(R.string.rpt_invoice_date) ,9F, false, 10F, "", com.itextpdf.text.Element.ALIGN_RIGHT,0,fontName = fontNameAr))
            tbl.put(6,TCell( viewModel.returnDateString(entityEo.sl_doc_date!!),9F, false, 25F, "", com.itextpdf.text.Element.ALIGN_RIGHT,0,fontName = fontNameAr))

            tbl.put(7,TCell("", 9F, false, 15F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(8,TCell("", 9F, false, 10F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(9,TCell("", 9F, false, 2F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            rws.add(CTable(tbl))
            tbl = hashMapOf()

            tbl.put(0,TCell("", 9F, false, 12F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(1,TCell(activity!!.resources!!.getString(R.string.rpt_customer) ,9F, false, 12F,"", Element.ALIGN_RIGHT,0,fontName = fontNameAr) )
            tbl.put(2,TCell(entityEo.sl_customer_name!!,9F, false, 12F,"", Element.ALIGN_RIGHT,0,fontName = fontNameAr) )

            tbl.put(3,TCell(activity!!.resources!!.getString(R.string.rpt_contact_name),9F,false,12F,"", Element.ALIGN_RIGHT,0,fontNameAr))
            tbl.put(4,TCell("${entityEo.sl_contact_name}",9F,false,12F,"", Element.ALIGN_RIGHT,0,fontNameAr))

            tbl.put(5,TCell(activity!!.resources!!.getString(R.string.rpt_customer_phone), 9F, false, 12F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))
            tbl.put(6,TCell(if(entityEo.sl_customer_phone == null) "" else entityEo.sl_customer_phone!!, 9F, false, 12F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

            tbl.put(7,TCell(activity!!.resources!!.getString(R.string.rpt_cr_name) , 9F, false, 18F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))
            tbl.put(8,TCell(if(entityEo.sl_cr_name == null) "" else entityEo.sl_cr_name!! , 9F, false, 18F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

            tbl.put(9,TCell("", 9F, false, 12F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            rws.add(CTable(tbl))

            val cw: ArrayList<Int> = arrayListOf(5, 15, 25, 10, 30, 25, 15, 15, 10, 5)
            header.add(HeaderFooterRow(8, rws, null, cellsWidth = cw))

            val footer: ArrayList<HeaderFooterRow> = arrayListOf()
            footer.add(HeaderFooterRow(0,null,"موارد",fontSize = 9F,align = Element.ALIGN_LEFT,fontName = fontNameAr))
            footer.add(HeaderFooterRow(1,null,"الشركة الحديثة للبرامجيات الاتمتة المحدودة",fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
            footer.add(HeaderFooterRow(2,null,activity!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
            val rowHeader: HashMap<Int, RowHeader> = hashMapOf()
            rowHeader.put(0, RowHeader("#", 9.0F, false, 4, "", 0, 0F))
            rowHeader.put(1, RowHeader(activity!!.resources!!.getString(R.string.rpt_barcode), 9.0F, false, 15, "", 0, 0F))
            rowHeader.put(2, RowHeader(activity!!.resources!!.getString(R.string.rpt_prod_name), 9.0F, false, 30, "", 0, 0F))
            rowHeader.put(3, RowHeader(activity!!.resources!!.getString(R.string.rpt_qty), 9.0F, false, 5, "", 0, 0F))
            rowHeader.put(4, RowHeader(activity!!.resources!!.getString(R.string.rpt_gift), 9.0F, false, 5, "", 0, 0F))
            rowHeader.put(5, RowHeader(activity!!.resources!!.getString(R.string.rpt_unit_price), 9.0F, false, 11, "", 0, 0F))
            rowHeader.put(6, RowHeader(activity!!.resources!!.getString(R.string.rpt_dis_value), 9.0F, false, 7, "", 0, 0F))
            rowHeader.put(7, RowHeader(activity!!.resources!!.getString(R.string.rpt_net_total), 9.0F, false, 11, "", 0, 0F))
            rowHeader.put(8, RowHeader(activity!!.resources!!.getString(R.string.rpt_notes), 9.0F, false, 13, "Total", 0, 0F))

            // Summary part
            val df1 = DecimalFormat("#,###")
            val df2 = DecimalFormat("#,###,###.#")
            val summary: ArrayList<HeaderFooterRow> = arrayListOf()
            tbl = hashMapOf()
            var srows: ArrayList<CTable> = arrayListOf()
            val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
            tbl.put(0, TCell(activity!!.resources!!.getString(R.string.rpt_total_qty),9F,false,25F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr))
            tbl.put(1, TCell("${df1.format(tQty)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
            srows.add(CTable(tbl))

            tbl = hashMapOf()
            val tweight = entityEo.items.sumByDouble {if(it.sld_total_weight == null) 0.00 else it.sld_total_weight!! }
            tbl.put(0, TCell(activity!!.resources!!.getString(R.string.rpt_total_weight),9F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr))
            tbl.put(1, TCell("${df2.format(tweight)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
            srows.add(CTable(tbl))
            // row 2
            tbl = hashMapOf()
            tbl.put(0,TCell(activity!!.resources!!.getString(R.string.rpt_total_amount),9F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr))
            tbl.put(1,TCell("${df2.format(entityEo.sl_total_amount)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
            srows.add(CTable(tbl))
            // row 3
            val tDiscount = if(entityEo.sl_total_discount == null) 0.00 else entityEo.sl_total_discount
            tbl = hashMapOf()
            tbl.put(0,TCell(activity!!.resources!!.getString(R.string.rpt_total_discount),9F,false,12F,"",Element.ALIGN_RIGHT,1, fontName = fontNameAr))
            tbl.put(1,TCell("${df2.format(tDiscount)}",9F,false,80F,"", Element.ALIGN_RIGHT,1))
            srows.add(CTable(tbl))
            // row 4
            tbl = hashMapOf()
            tbl.put(0,TCell(activity!!.resources!!.getString(R.string.rpt_net_amount),9F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr ) )
            tbl.put(1,TCell("${df2.format(entityEo.sl_net_amount)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1) )
            srows.add(CTable(tbl))

            //sl_customer_balance
            var balance: Double = 0.00
            if(entityEo.sl_customer_balance != null) balance = entityEo.sl_customer_balance!!
            tbl = hashMapOf()
            tbl.put(0,TCell(activity!!.resources!!.getString(R.string.rpt_cu_balance),9F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr ) )
            tbl.put(1,TCell("${df2.format(balance)}  ${entityEo.sl_cr_name}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1) )
            srows.add(CTable(tbl))

            val scw: java.util.ArrayList<Int> = arrayListOf(80, 20)
            summary.add(HeaderFooterRow(0, srows, null, cellsWidth = scw))

            summary.add(HeaderFooterRow(1, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
            summary.add(HeaderFooterRow(2, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
            summary.add(HeaderFooterRow(3, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
            summary.add(HeaderFooterRow(4, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
            srows = arrayListOf()
            tbl = hashMapOf()
            tbl.put(0, TCell(activity!!.resources.getString(R.string.rpt_person_reciever_sig),10F,false,12F,"", Element.ALIGN_CENTER,0, fontName = fontNameAr ))
            tbl.put(1, TCell(activity!!.resources.getString(R.string.rpt_storekeeper_sig),10F,false,12F, "",Element.ALIGN_CENTER,0, fontName = fontNameAr ))
            tbl.put(2, TCell(activity!!.resources.getString(R.string.rpt_sales_manager_sig),10F,false,12F,"",  Element.ALIGN_CENTER,0, fontName = fontNameAr))
            srows.add(CTable(tbl))

            summary.add(HeaderFooterRow(5, srows, null, cellsWidth = arrayListOf(35, 35, 34)))
            val act = activity!!
            GeneratePdf().createPdf(act,imgLogo, entityEo.items, rowHeader, header, footer,null, summary, isRTL) { _, path ->
               onSuccess("Pdf Created Successfully")
                GeneratePdf().printPDF(act, path)
            }
        } catch (e: Exception) {
            onFailure("Error Exception ${e.message}")
            e.printStackTrace()
        }
    }
}
