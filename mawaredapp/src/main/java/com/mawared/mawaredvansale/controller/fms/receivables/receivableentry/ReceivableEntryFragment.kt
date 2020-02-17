package com.mawared.mawaredvansale.controller.fms.receivables.receivableentry

import android.app.DatePickerDialog
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter1
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.databinding.ReceivableEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.receivable_entry_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.io.IOException
import java.io.InputStream
import java.util.*


class ReceivableEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Receivable>,
    IMessageListener {

    override val kodein by kodein()

    private val factory: ReceivableEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(ReceivableEntryViewModel::class.java)
    }
    lateinit var binding: ReceivableEntryFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.receivable_entry_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.doc_date.value = "${LocalDate.now()}"
        viewModel.ctx = activity!!
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_receivable_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)
        if(arguments != null){
            val args = ReceivableEntryFragmentArgs.fromBundle(arguments!!)
            viewModel.mode = args.mode
            if(viewModel.mode != "Add"){
                viewModel.setReceivableId(args.rcvId)
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
                if(!viewModel.isRunning){
                    hideKeyboard()
                    showDialog(context!!, getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ){
                        onStarted()
                        viewModel.location = getLocationData()
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

    override fun onResume() {
        removeObservers()
        super.onResume()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun removeObservers() {
        viewModel._baseEo.removeObservers(this)
        viewModel.entityEo.removeObservers(this)

    }
    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main) {
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
                viewModel.doc_no.value = it.rcv_doc_no?.toString()
                viewModel.doc_date.value = viewModel.returnDateString(it.rcv_doc_date!!)
                viewModel.selectedCustomer?.cu_ref_Id = it.rcv_cu_Id!!
                viewModel.selectedCustomer?.cu_name = it.rcv_cu_name
                viewModel.bc_amount.value = it.rcv_amount.toString()
                viewModel.lc_amount.value = it.rcv_lc_amount.toString()
                viewModel.bc_change.value = it.rcv_change.toString()
                viewModel.lc_change.value = it.rcv_lc_change.toString()

                viewModel.comment.value = it.rcv_comment
                binding.atcCustomer.setText("${it.rcv_cu_name}", true)

            }
        })

        // Customer autocomplete settings
        val adapter = CustomerAdapter1(context!!, R.layout.support_simple_spinner_dropdown_item )

        binding.atcCustomer.threshold = 0
        binding.atcCustomer.dropDownWidth = resources.displayMetrics.widthPixels - 50
        binding.atcCustomer.setAdapter(adapter)
        binding.atcCustomer.setOnFocusChangeListener { _, b ->
            if(b) binding.atcCustomer.showDropDown()
        }

        binding.atcCustomer.setOnTouchListener(OnTouchListener { v, event ->
            binding.atcCustomer.showDropDown()
            false
        })
        binding.atcCustomer.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomer = adapter.getItem(position)
        }

        binding.atcCustomer.addTextChangedListener(object:TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                viewModel.term.value = s.toString()
            }
        })
        // bind customer to autocomplete
        viewModel.customerList.observe(viewLifecycleOwner, Observer { cu ->
            adapter.setCustomers(cu)
            binding.atcCustomer.showDropDown()
        })

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(viewLifecycleOwner, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.00
        })

        viewModel.setVoucherCode("Recievable")
        viewModel.setCurrencyId(App.prefs.saveUser!!.sl_cr_Id!!)
        viewModel.term.value = ""
        llProgressBar?.visibility = View.GONE
    }

    override fun clear(code: String) {
        if(code == "cu"){
            binding.atcCustomer.setText("", true)
        }
    }


    override fun onDelete(baseEo: Receivable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->

            viewModel.doc_date.value = "${dayOfMonth}-${monthOfYear + 1}-${yr}"

        }, year, month, day)
        dpd.show()
    }

    override fun onStarted() {
        llProgressBar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {

        llProgressBar?.visibility = View.GONE
        addReceivable_layout?.snackbar(message)
    }

    override fun onFailure(message: String) {
        llProgressBar?.visibility = View.GONE
        addReceivable_layout?.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    private fun doPrint(_baseEo: Receivable){
        val config = activity!!.resources.configuration
        val isRTL = if(config.layoutDirection == View.LAYOUT_DIRECTION_LTR) false else true
        var bmp: Bitmap? = null

        val mngr: AssetManager = activity!!.getAssets()
        var `is`: InputStream? = null
        try {
            `is` = mngr.open("images/co_logo.bmp")
            bmp = BitmapFactory.decodeStream(`is`)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        val fontNameEn = "assets/fonts/arial.ttf"
        val fontNameAr = "assets/fonts/arial.ttf"
        val fontNameAr1 = "assets/fonts/droid_kufi_regular.ttf"
        try {

            val imgLogo = RepLogo(bmp, 10F, 800F)
            val header: ArrayList<HeaderFooterRow> = arrayListOf()
            var tbl: HashMap<Int, TCell> = hashMapOf()
            var rws: ArrayList<CTable> = arrayListOf()
            val phones = if(_baseEo.rcv_org_phone != null) _baseEo.rcv_org_phone!! else ""

            header.add(HeaderFooterRow(0, null, "شركة النادر التجارية", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameAr1 ))
            header.add(HeaderFooterRow(1, null, "Al-Nadir Trading Company",  14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(2, null, "${_baseEo.rcv_org_name}", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(3, null, phones, 12F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))

            val footer: ArrayList<HeaderFooterRow> = arrayListOf()
            footer.add(HeaderFooterRow(0,null,"موارد" + " - " + "الشركة الحديثة للبرامجيات الاتمتة المحدودة",fontSize = 9F,align = Element.ALIGN_LEFT,fontName = fontNameAr))
            footer.add(HeaderFooterRow(2,null,activity!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
            _baseEo.created_by = activity!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}"

            val act = activity!!
            GeneratePdf().createPdf(activity!!,imgLogo, _baseEo, header, footer,isRTL) { _, path ->
                onSuccess("Pdf Created Successfully")
                GeneratePdf().printPDF(act, path)
            }
        } catch (e: Exception) {
            onFailure("Error Exception ${e.message}")
            e.printStackTrace()
        }
    }
}
