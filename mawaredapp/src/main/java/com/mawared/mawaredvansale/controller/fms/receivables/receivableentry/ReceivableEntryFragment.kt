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
import android.widget.Toast
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
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.databinding.ReceivableEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.URL_LOGO
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
        viewModel.ctx = requireActivity()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments != null){
            val args = ReceivableEntryFragmentArgs.fromBundle(requireArguments())
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

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = getString(R.string.layout_receivable_entry_title)
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
                   // UIUtil.hideKeyboard(activity!!)
                    hideKeyboard()
                    showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null,{
                        onStarted()
                        viewModel.location = getLocationData()
                        viewModel.onSave()
                    },{
                        viewModel.isRunning = false
                    })
                }
            }
            R.id.close_btn -> {
                //UIUtil.hideKeyboard(activity!!)
                hideKeyboard()
                requireActivity().onBackPressed()
                setHasOptionsMenu(false)
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
        hideKeyboard()
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
                requireActivity().onBackPressed()
                setHasOptionsMenu(false)
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }
        })

        viewModel.entityEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.doc_no.value = it.rcv_doc_no?.toString()
                viewModel.doc_date.value = viewModel.returnDateString(it.rcv_doc_date!!)
                viewModel.bc_amount.value = it.rcv_amount.toString()
                viewModel.lc_amount.value = it.rcv_lc_amount.toString()
                viewModel.bc_change.value = it.rcv_change.toString()
                viewModel.lc_change.value = it.rcv_lc_change.toString()
                viewModel.pbBalance.value = it.rcv_cu_balance.toString()
                viewModel.comment.value = it.rcv_comment
                binding.atcCustomer.setText("${it.rcv_cu_name}", true)

            }
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
            viewModel.selectedCustomer = adapter.getItem(position)
        }

        binding.atcCustomer.addTextChangedListener(object:TextWatcher {

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
                if(viewModel.mode != "Add" && cu.size > 0 && viewModel._entityEo != null){
                    viewModel.selectedCustomer = cu.find { it.cu_ref_Id == viewModel._entityEo?.rcv_cu_Id}
                }else{
                    binding.atcCustomer.showDropDown()
                }
            }

        })

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(viewLifecycleOwner, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.0
        })

        viewModel.setVoucherCode("Recievable")
        viewModel.setCurrencyId(App.prefs.saveUser!!.sf_cr_Id!!)
        if(viewModel.mode == "Add") viewModel.term.value = ""
        llProgressBar?.visibility = View.GONE
    }

    override fun clear(code: String) {
        if(code == "cu"){
            binding.atcCustomer.setText("", true)
        }
    }


    override fun onDelete(baseEo: Receivable) {
         //To change body of created functions use File | Settings | File Templates.
    }

    override fun onShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireActivity(), DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->

            viewModel.doc_date.value = "${yr}-${monthOfYear + 1}-${dayOfMonth}"

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

    private fun doPrint(_baseEo: Receivable) {
        if (App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                val tickets = GenerateTicket(requireActivity(), lang).create(
                    _baseEo,
                    URL_LOGO + "co_black_logo.png",
                    "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION",
                    null,
                    null
                )

                TicketPrinting(requireActivity(), tickets).run()
            } catch (e: Exception) {
                onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        } else {
            val config = requireActivity().resources.configuration
            val isRTL = config.layoutDirection != View.LAYOUT_DIRECTION_LTR
            var bmp: Bitmap? = null

            val mngr: AssetManager = requireActivity().assets
            var `is`: InputStream? = null
            try {
                //`is` = mngr.open("images/co_logo.bmp")
                //bmp = BitmapFactory.decodeStream(`is`)
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
                val phones = if (_baseEo.rcv_org_phone != null) _baseEo.rcv_org_phone!! else ""

                header.add(
                    HeaderFooterRow(
                        0,
                        null,
                        App.prefs.saveUser!!.client_name,
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameAr1
                    )
                )
                header.add(
                    HeaderFooterRow(
                        1,
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
                        2,
                        null,
                        "${_baseEo.rcv_org_name}",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        3,
                        null,
                        phones,
                        12F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )

                val footer: ArrayList<HeaderFooterRow> = arrayListOf()
                footer.add(
                    HeaderFooterRow(
                        0,
                        null,
                        "موارد" + " - " + "الشركة الحديثة للبرامجيات الاتمتة المحدودة",
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
                _baseEo.created_by =
                    requireActivity().resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}"

                val act = requireActivity()
                GeneratePdf().createPdf(
                    requireActivity(),
                    imgLogo,
                    _baseEo,
                    header,
                    footer,
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
