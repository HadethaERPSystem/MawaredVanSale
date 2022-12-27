package com.mawared.mawaredvansale.controller.marketplace.cart

import android.app.AlertDialog
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CartAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.databinding.CartFragmentBinding
import com.mawared.mawaredvansale.databinding.CartPaymentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.cart_fragment.*
import kotlinx.android.synthetic.main.invoice_payment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*

class CartFragment : ScopedFragmentLocation(), KodeinAware, IMessageListener {
    override val kodein by kodein()
    private val factory: CartViewModelFactory by instance()
    private lateinit var binding: CartFragmentBinding
    lateinit var binding1: CartPaymentBinding
    lateinit var navController: NavController

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CartViewModel::class.java)
    }
    private val layoutId = R.layout.cart_fragment
    private val rv_layoutId = R.layout.item_rv_cart

    private var adapter = CartAdapter(rv_layoutId){
        delete(it){
            val result = "result"
            // Use the Kotlin extension in the fragment-ktx artifact
            requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.ctx = requireContext()
        viewModel.msgListener = this
        bindUI()

        binding.saveBtn.setOnClickListener {
            when(viewModel.voucher?.vo_code){
                "SaleInvoice"->{ saveInvoice()}
                "SaleOrder" ->{ saveSaleOrder()}
                "PSOrder" ->{ saveSaleOrder()}
            }
        }
        binding.etDiscPrcnt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val discPrcnt : Double = if(s.isNullOrEmpty()) 0.0 else s.toString().toDouble()
                viewModel.discPrcnt = discPrcnt
                viewModel.recalculateTotal({
                    //loadData()
                    viewModel.setTotals()
                    viewModel.updateRemain()
                    adapter.setList(viewModel.orders)
                    //val result = "result"
                    // Use the Kotlin extension in the fragment-ktx artifact
                    //requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                },
                    {
                        binding.etDiscPrcnt.setText(it?.toInt().toString())
                    })
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(arguments != null){
            val args = CartFragmentArgs.fromBundle(requireArguments())
            if(args.customer != null && !args.vocode.isNullOrEmpty()){
                viewModel.customer = args.customer
                viewModel.loadAgeDebit(viewModel.customer!!.cu_ref_Id!!)
                viewModel.setVoucherCode(args.vocode!!)
                viewModel.customer_name.value = args.customer?.cu_name_ar
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        rv_cart.setupGrid(requireContext(), adapter as BaseAdapter<Any>, 1)
        loadData()
        if(arguments != null){
            val args = CartFragmentArgs.fromBundle(requireArguments())
            if(args.customer != null && !args.vocode.isNullOrEmpty()){
                viewModel.customer = args.customer
                viewModel.setVoucherCode(args.vocode!!)
                viewModel.customer_name.value = args.customer?.cu_name_ar
            }
        }
        //binding.etDiscPrcnt.setText(viewModel.discPrcnt.toString())

        navController = Navigation.findNavController(view)
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar!!.subtitle =  getString(R.string.layout_cart_title)
    }

    fun bindUI() = GlobalScope.launch(Dispatchers.Main){
        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(viewLifecycleOwner, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.0
        })
        var cr_id = App.prefs.saveUser!!.sl_cr_Id
        if(App.prefs.saveUser!!.sl_cr_Id == App.prefs.saveUser!!.ss_cr_Id){
            cr_id = App.prefs.saveUser!!.sf_cr_Id
        }
        viewModel.setCurrencyId(cr_id!!)
        viewModel.fcr_symbol.value = if(App.prefs.saveUser!!.sl_cr_code!! == App.prefs.saveUser!!.ss_cr_code!!) App.prefs.saveUser!!.sf_cr_code!! else App.prefs.saveUser!!.ss_cr_code!!
    }

    fun delete(item: OrderItems, Success: () -> Unit) {
        showDialog(
            requireContext(),
            getString(R.string.delete_dialog_title),
            getString(R.string.msg_confirm_delete),
            item,
            {
                viewModel.delete(it, {
                    mcv_invoices?.snackbar(getString(R.string.msg_success_delete))
                    loadData()
                    Success()
                }, {
                    mcv_invoices?.snackbar(getString(R.string.msg_failure_delete))
                })
            })
    }

    fun deleteAll(Success: () -> Unit){
        showDialog(
            requireContext(),
            getString(R.string.delete_dialog_title),
            getString(R.string.msg_confirm_deleteAll),
            null,
            {
                viewModel.deleteAll( {
                    mcv_invoices?.snackbar(getString(R.string.msg_success_delete))
                    loadData()
                    Success()
                }, {
                    mcv_invoices?.snackbar(getString(R.string.msg_failure_delete))
                })
            })
    }

    fun loadData(){
        viewModel.loadOrders(){
            viewModel.setTotals()
            viewModel.updateRemain()
            adapter.setList(viewModel.orders)
        }
    }

    override fun onStarted() {
        progress_bar_cart?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar_cart?.visibility = View.GONE
        mcv_invoices?.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar_cart?.visibility = View.GONE
        mcv_invoices?.snackbar(message)
    }

    // Save Invoice
    private fun saveInvoice(){
        viewModel.setTotals()
        viewModel.updateRemain()
        if(!viewModel.isRunning){
            viewModel.isRunning = true
            hideKeyboard()
            // create data binding
            val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
            binding1 = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.cart_payment, null, false)
            binding1.viewmodel = viewModel
            binding1.lifecycleOwner = this
            // AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(requireContext())
                .setView(binding1.root)
                .setCustomTitle(titleView)
                .setCancelable(false)

            // show dialog
            val mAlertDialog = mBuilder.show()
            mAlertDialog.saveButton.setOnClickListener {
                showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null, {
                    onStarted()
                    viewModel.location = getLocationData()
                    viewModel.onSaveInvoice ({ o ->// On Success
                        mAlertDialog.dismiss()
                        viewModel.isRunning = false
                        if(o != null) {
                            doPrint(o)
                        }
                        viewModel.deleteAll(
                            {
                                loadData()
                                val result = "result"
                                // Use the Kotlin extension in the fragment-ktx artifact
                                requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                            })
                        onSuccess(getString(R.string.saved_successfully))
                    },{
                        // On Fail
                        viewModel.isRunning = false
                        if(!it.isNullOrEmpty())
                            onFailure(it)
                    })
                },{// On Select cancel
                    viewModel.isRunning = false
                })
            }

            mAlertDialog.cancelButton.setOnClickListener {
                viewModel.isRunning = false
                mAlertDialog.dismiss()
            }

            //======== OnTextChanged
            //======== Paid USD
            mAlertDialog.edtxt_amount_sc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            //======== Change USD
            mAlertDialog.edtxt_change_sc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            //======== Paid IQD
            mAlertDialog.edtxt_amount_fc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            //======== Change USD
            mAlertDialog.edtxt_change_fc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            //====================
        }
    }

    // Save Sale Order or Presale order
    private fun saveSaleOrder(){
        if(!viewModel.isRunning){
            viewModel.isRunning = true
            hideKeyboard()
            showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ,{
                onStarted()
                viewModel.location = getLocationData()
                viewModel.onSaveOrder({// On Success
                    viewModel.isRunning = false
                    viewModel.deleteAll(
                        {
                            loadData()
                            val result = "result"
                            // Use the Kotlin extension in the fragment-ktx artifact
                            requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                        })
                    onSuccess(getString(R.string.saved_successfully))
                },{// On Fail
                    viewModel.isRunning = false
                    onFailure(getString(R.string.msg_failure_saved))
                })
            },{
                viewModel.isRunning = false
            })
        }
    }

    // Print
    private fun doPrint(entityEo: Sale){
        if(App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                entityEo.sl_salesman_phone = App.prefs.savedSalesman?.sm_phone_no ?: ""
                val tickets = GenerateTicket(requireContext(), lang).create(
                    entityEo,
                    URL_LOGO + "co_black_logo.png",//R.drawable.ic_logo_black,
                    "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION",
                    null,
                    null
                )

                TicketPrinting(requireContext(), tickets).run()
            }catch (e: Exception){
                onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        }else {
            //val lang = Locale.getDefault().toString().toLowerCase()
            val config = requireActivity().resources.configuration
            val isRTL = config.layoutDirection != View.LAYOUT_DIRECTION_LTR
            var bmp: Bitmap? =
                null // BitmapFactory.decodeResource(ctx!!.resources, R.drawable.ic_logo_black)

            val mngr: AssetManager = requireContext().assets
            var `is`: InputStream? = null
            try {
                //`is` = mngr.open("images/co_logo.bmp")
                //bmp = BitmapFactory.decodeStream(`is`)
                //URL_LOGO + "co_black_logo.png"
                //`is` = mngr.open("images/co_logo.bmp")
                //bmp = BitmapFactory.decodeStream(`is`)
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
                val phones = if (entityEo.sl_org_phone != null) entityEo.sl_org_phone!!.replace(
                    "|",
                    "\n\r"
                ) else ""

                header.add(
                    HeaderFooterRow(
                        0,
                        null,
                        App.prefs.saveUser!!.client_name,
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        1,
                        null,
                        "${entityEo.sl_org_name}",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        2,
                        null,
                        phones,
                        11F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                //header.add(HeaderFooterRow(3, null, "Asia: 0770-6502228", 20F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
                header.add(
                    HeaderFooterRow(
                        3,
                        null,
                        "",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameAr
                    )
                )
                header.add(
                    HeaderFooterRow(
                        4,
                        null,
                        "",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        5,
                        null,
                        "",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )

                tbl.put(0, TCell("", 9F, false, 2f, "", Element.ALIGN_CENTER, 0))
                tbl.put(
                    1,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_list_name),
                        9f,
                        false,
                        15F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    2,
                    TCell(
                        entityEo.sl_vo_name!!,
                        9F,
                        false,
                        30F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(
                    3,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_invoice_no),
                        9F,
                        false,
                        15F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    4,
                    TCell(
                        entityEo.sl_refNo!!,
                        9F,
                        false,
                        30F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(
                    5,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_invoice_date),
                        9F,
                        false,
                        10F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    6,
                    TCell(
                        viewModel.returnDateString(entityEo.sl_doc_date!!),
                        9F,
                        false,
                        25F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )

                tbl.put(7, TCell("", 9F, false, 15F, "", Element.ALIGN_CENTER, 0))
                tbl.put(8, TCell("", 9F, false, 10F, "", Element.ALIGN_CENTER, 0))
                tbl.put(9, TCell("", 9F, false, 2F, "", Element.ALIGN_CENTER, 0))
                rws.add(CTable(tbl))
                tbl = hashMapOf()

                tbl.put(0, TCell("", 9F, false, 12F, "", Element.ALIGN_CENTER, 0))
                tbl.put(
                    1,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_customer),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    2,
                    TCell(
                        entityEo.sl_customer_name!!,
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )

                tbl.put(
                    3,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_contact_name),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontNameAr
                    )
                )
                tbl.put(
                    4,
                    TCell(
                        "${entityEo.sl_contact_name}",
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontNameAr
                    )
                )

                tbl.put(
                    5,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_phone),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    6,
                    TCell(
                        if (entityEo.sl_customer_phone == null) "" else entityEo.sl_customer_phone!!,
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(
                    7,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_cr_name),
                        9F,
                        false,
                        18F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    8,
                    TCell(
                        if (entityEo.sl_cr_name == null) "" else entityEo.sl_cr_name!!,
                        9F,
                        false,
                        18F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(9, TCell("", 9F, false, 12F, "", Element.ALIGN_CENTER, 0))
                rws.add(CTable(tbl))

                val cw: ArrayList<Int> = arrayListOf(5, 15, 25, 10, 30, 25, 15, 15, 10, 5)
                header.add(HeaderFooterRow(8, rws, null, cellsWidth = cw))

                val footer: ArrayList<HeaderFooterRow> = arrayListOf()
                footer.add(
                    HeaderFooterRow(
                        0,
                        null,
                        "موارد",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                footer.add(
                    HeaderFooterRow(
                        1,
                        null,
                        "الشركة الحديثة للبرامجيات الاتمتة المحدودة",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                footer.add(
                    HeaderFooterRow(
                        2,
                        null,
                        requireActivity().resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                val rowHeader: HashMap<Int, RowHeader> = hashMapOf()
                rowHeader.put(0, RowHeader("#", 9.0F, false, 4, "", 0, 0F))
                rowHeader.put(
                    1,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_barcode),
                        9.0F,
                        false,
                        15,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    2,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_prod_name),
                        9.0F,
                        false,
                        30,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    3,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_qty),
                        9.0F,
                        false,
                        5,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    4,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.unit_name),
                        9.0F,
                        false,
                        5,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    5,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_gift),
                        9.0F,
                        false,
                        5,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    6,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_unit_price),
                        9.0F,
                        false,
                        11,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    7,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_dis_value),
                        9.0F,
                        false,
                        7,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    8,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_net_total),
                        9.0F,
                        false,
                        11,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    9,
                    RowHeader(
                        requireActivity().resources!!.getString(R.string.rpt_notes),
                        9.0F,
                        false,
                        13,
                        "Total",
                        0,
                        0F
                    )
                )

                // Summary part
                val df1 = DecimalFormat("#,###")
                val df2 = DecimalFormat("#,###,###.#")
                val summary: ArrayList<HeaderFooterRow> = arrayListOf()
                tbl = hashMapOf()
                var srows: ArrayList<CTable> = arrayListOf()
                val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
                tbl.put(
                    0,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_total_qty),
                        9F,
                        false,
                        25F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(1, TCell("${df1.format(tQty)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
                srows.add(CTable(tbl))

                tbl = hashMapOf()
                val tweight =
                    entityEo.items.sumByDouble { if (it.sld_total_weight == null) 0.00 else it.sld_total_weight!! }
                tbl.put(
                    0,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_total_weight),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell("${df2.format(tweight)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)
                )
                srows.add(CTable(tbl))
                // row 2
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_total_amount),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        "${df2.format(entityEo.sl_total_amount)}",
                        9F,
                        false,
                        80F,
                        "",
                        Element.ALIGN_RIGHT,
                        1
                    )
                )
                srows.add(CTable(tbl))
                // row 3
                val tDiscount =
                    if (entityEo.sl_total_discount == null) 0.00 else entityEo.sl_total_discount
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_total_discount),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell("${df2.format(tDiscount)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)
                )
                srows.add(CTable(tbl))
                // row 4
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_net_amount),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        "${df2.format(entityEo.sl_net_amount)}",
                        9F,
                        false,
                        80F,
                        "",
                        Element.ALIGN_RIGHT,
                        1
                    )
                )
                srows.add(CTable(tbl))

                //sl_customer_balance
                var balance: Double = 0.00
                if (entityEo.sl_customer_balance != null) balance = entityEo.sl_customer_balance!!
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        requireActivity().resources!!.getString(R.string.rpt_cu_balance),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        "${df2.format(balance)}  ${entityEo.sl_cr_name}",
                        9F,
                        false,
                        80F,
                        "",
                        Element.ALIGN_RIGHT,
                        1
                    )
                )
                srows.add(CTable(tbl))

                val scw: java.util.ArrayList<Int> = arrayListOf(80, 20)
                summary.add(HeaderFooterRow(0, srows, null, cellsWidth = scw))

                summary.add(
                    HeaderFooterRow(
                        1,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                summary.add(
                    HeaderFooterRow(
                        2,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                summary.add(
                    HeaderFooterRow(
                        3,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                summary.add(
                    HeaderFooterRow(
                        4,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                srows = arrayListOf()
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        requireActivity().resources.getString(R.string.rpt_person_reciever_sig),
                        10F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_CENTER,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        requireActivity().resources.getString(R.string.rpt_storekeeper_sig),
                        10F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_CENTER,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    2,
                    TCell(
                        requireActivity().resources.getString(R.string.rpt_sales_manager_sig),
                        10F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_CENTER,
                        0,
                        fontName = fontNameAr
                    )
                )
                srows.add(CTable(tbl))

                summary.add(HeaderFooterRow(5, srows, null, cellsWidth = arrayListOf(35, 35, 34)))
                val act = requireActivity()
                GeneratePdf().createPdf(
                    act,
                    imgLogo,
                    entityEo.items,
                    rowHeader,
                    header,
                    footer,
                    null,
                    summary,
                    isRTL
                ) { _, path ->
                    onSuccess("Pdf Created Successfully")
                    GeneratePdf().printPDF(act, path)
                }
            } catch (e: Exception) {
                onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
