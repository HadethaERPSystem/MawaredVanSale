package com.mawared.mawaredvansale.controller.mnt.entry

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.*
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.sales.delivery.deliveryentry.DeliveryEntryFragment
import com.mawared.mawaredvansale.controller.sales.invoices.addinvoice.AddInvoiceFragmentArgs
import com.mawared.mawaredvansale.data.db.entities.dms.Document
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.mnt.MntServ
import com.mawared.mawaredvansale.data.db.entities.mnt.MntSpareParts
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.databinding.MntEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.MediaHelper
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.mnt_entry_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate

class MntEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Mnts>, IMessageListener {

    companion object{
        var MY_CAMERA_REQUEST_CODE = 7171
    }

    override val kodein by kodein()
    private val factory: MntEntryViewModelFactory by instance()
    private var SCREEN: Int = 0

//    var fileName = ""
//    var fileUri = Uri.parse("")
    var imageUri : Uri? = null
    lateinit var mediaHelper: MediaHelper

    val viewModel by lazy {
      ViewModelProviders.of(this, factory).get(MntEntryViewModel::class.java)
    }

    lateinit var binding: MntEntryFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.mnt_entry_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.doc_date.value = "${LocalDate.now()}"
        viewModel.ctx = requireActivity()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        mediaHelper = MediaHelper()

        bindUI()

        //goNext()
        binding.btnNext.setOnClickListener {
            goNext()
        }

        binding.btnBack.setOnClickListener {
            goBack()
        }

        binding.btnSave.setOnClickListener {
            save()
        }
        binding.btnTakePhoto.setOnClickListener {
            openCamera()
        }
        return binding.root
    }

    private fun goNext(){
        hideKeyboard()
        when(SCREEN){
            0->{
                SCREEN++
                binding.mcv1.visibility = View.GONE
                binding.mcv2.visibility = View.VISIBLE
                binding.btnBack.visibility = View.VISIBLE
            }
            1->{
                SCREEN++
                binding.mcv2.visibility = View.GONE
                binding.mcv3.visibility = View.VISIBLE
                binding.rcvInvoiceItems.visibility = View.VISIBLE
            }
            2->{
                SCREEN++
                binding.mcv3.visibility = View.GONE
                binding.rcvInvoiceItems.visibility = View.GONE
                binding.mcv4.visibility = View.VISIBLE
                binding.rcvServiceItems.visibility = View.VISIBLE
            }
            3->{
                SCREEN++
                binding.mcv4.visibility = View.GONE
                binding.rcvServiceItems.visibility = View.GONE
                binding.mcv5.visibility = View.VISIBLE
                binding.rcvDocs.visibility = View.VISIBLE
                binding.btnSave.visibility = View.VISIBLE
                binding.btnNext.visibility = View.GONE
            }

        }
    }

    private fun goBack(){
        hideKeyboard()
        when(SCREEN){
            1->{
                SCREEN--
                binding.mcv1.visibility = View.VISIBLE
                binding.mcv2.visibility = View.GONE
                binding.btnBack.visibility = View.GONE
            }
            2->{
                SCREEN--
                binding.mcv2.visibility = View.VISIBLE
                binding.mcv3.visibility = View.GONE
                binding.rcvInvoiceItems.visibility = View.GONE
            }
            3->{
                SCREEN--
                binding.mcv3.visibility = View.VISIBLE
                binding.rcvInvoiceItems.visibility = View.VISIBLE
                binding.mcv4.visibility = View.GONE
                binding.rcvServiceItems.visibility = View.GONE
            }
            4->{
                SCREEN--
                binding.mcv4.visibility = View.VISIBLE
                binding.rcvServiceItems.visibility = View.VISIBLE
                binding.mcv5.visibility = View.GONE
                binding.rcvDocs.visibility = View.GONE
                binding.btnSave.visibility = View.GONE
                binding.btnNext.visibility = View.VISIBLE
            }
            5->{

            }

        }
    }

    private fun save(){
        if(!viewModel.isRunning){
            viewModel.isRunning = true
            // UIUtil.hideKeyboard(activity!!)
            hideKeyboard()
            showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null,{
                onStarted()
                viewModel.location = getLocationData()
                viewModel.onSave({// On Success
                    viewModel.isRunning = false
                },{// On Fail
                    viewModel.isRunning = false
                })
            },{
                viewModel.isRunning = false
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            val args = AddInvoiceFragmentArgs.fromBundle(requireArguments())
            viewModel.mode = args.mode
            if(viewModel.mode == "View") viewModel.visible = View.GONE else viewModel.visible = View.VISIBLE
            if(viewModel.mode != "Add"){
                //viewModel.setInvoiceId(args.saleId)
            }
        }
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

        try {
            val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
            m.invoke(null)
        }catch (e: Exception){
            e.printStackTrace()
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
        //viewModel.entityEo.removeObservers(this)
        //viewModel.invoiceItems.removeObservers(this)
        viewModel.productList.removeObservers(this)
    }

    // inflate the menu
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        if(viewModel.mode == "View"){
//            inflater.inflate(R.menu.view_menu, menu)
//        }else{
//            inflater.inflate(R.menu.add_menu, menu)
//        }
//
//        super.onCreateOptionsMenu(menu, inflater)
//    }

    private fun bindUI() = GlobalScope.launch(Dispatchers.Main) {
        viewModel._baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                //doPrint(it)
                requireActivity().onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }

        })

        viewModel.entityEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.doc_no.value = it.doc_no?.toString()
                viewModel.doc_date.value = viewModel.returnDateString(it.doc_date!!)
                viewModel.oCu_Id = it.cust_Id!!
                viewModel.near_zone.value = it.near_point
                viewModel.serialNo.value = it.sr_no
                viewModel.war_date.value = it.war_date


                viewModel.setSpPartLines (it.SpPartsLines)
                viewModel.setServLines(it.ServLines)
                viewModel.setTechLines(it.TechLines)

                binding.atcCustomer.setText("${it.cust_name}", true)
                binding.atcInvoices.setText("${it.refNo}", true)
                binding.atcDevice.setText("${it.prod_name}", true)
                binding.atcWarranty.setText("${it.war_no}", true)
                //viewModel.setItems(it.items)
            }
        })

        viewModel.mntTypeList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            typeAutocompleteInit(it)
        })

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        // Regular Maintenance
        initRegMnt()

        // Invoice Adapter and Autocomplete
        initInvoices()

        // Customer autocomplete settings
        initCustomers()

        // Devices
        initDevices()

        // Warranty Adapter And Autocomplete
        initWarranty()

        viewModel.mntStatusList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            statusAutocompleteInit(it)
        })

        viewModel.priceCateList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            priceCategory(it)
        })

        // bind products to autocomplete
        initProducts()

        viewModel.WhsList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            whsAutocompleteInti(it)
        })

        viewModel.locationList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            locAutocompleteInti(it)
        })

        // Services
        initServices()

        viewModel.spPartsLines.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initRecyclerView(it.toSparePartRow())
            viewModel.setTotals()
        })

        viewModel.servLines.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initRecyclerViewService(it.toServiceRow())
            viewModel.setTotals()
        })

        viewModel.docLines.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initDocument(it.toDocuemntRow())
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {

            llProgressBar.visibility =  if(it == NetworkState.LOADING) View.VISIBLE else View.GONE
        })

        viewModel.vo_code.value = "Maintenance"
    }

    private fun initRecyclerView(rows: List<MntSparePartRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }
        rcv_invoice_items.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<MntSpareParts>.toSparePartRow(): List<MntSparePartRow>{
        return this.map {
            MntSparePartRow(it, viewModel)
        }
    }

    private fun initRecyclerViewService(rows: List<MntServiceRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }
        rcv_service_items.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<MntServ>.toServiceRow(): List<MntServiceRow>{
        return this.map{
            MntServiceRow(it, viewModel)
        }
    }

    private fun initDocument(rows: List<DocumentRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }
        var cols = 2
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 3
        }
        binding.rcvDocs.apply {
            layoutManager = GridLayoutManager(requireContext(), cols)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Document>.toDocuemntRow() : List<DocumentRow>{
        return this.map {
            DocumentRow(it, viewModel)
        }
    }



    private fun initRegMnt(){
        val adapter = MntReg_Adapter(requireContext().applicationContext, viewModel,  R.layout.support_simple_spinner_dropdown_item )
        binding.atcRegMnt.threshold  = 0
        binding.atcRegMnt.setAdapter(adapter)
        binding.btnOpenRegMnt.setOnClickListener {
            binding.atcRegMnt.showDropDown()
        }
        binding.atcRegMnt.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedRegMnt = adapter.getItem(position)
            viewModel.regMntNo.value = viewModel.selectedRegMnt!!.regMntNo.toString()
            viewModel.contRefNo.value = viewModel.selectedRegMnt!!.contRefNo.toString()

            viewModel.selectedCustomer = Customer("", null, null, null, null, "", viewModel.selectedRegMnt?.cust_name, "",
                "", "", "", "", "", "", null, null, null, "", viewModel.selectedRegMnt?.cust_Id,
                null, null, null, "", null, null, null, null, null, null, null)
            binding.atcCustomer.setText(viewModel.selectedRegMnt?.cust_name, true)
            viewModel.devTerm.value = ""
            binding.atcRegMnt.dismissDropDown()
        }

        binding.atcRegMnt.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.selectedRegMnt == null) viewModel.regTerm.value = s.toString() else binding.atcRegMnt.dismissDropDown()
            }
        })

        viewModel.mntRegList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            adapter.setData(it)
        })
    }

    private fun initInvoices(){
        val InvoiceAdapter = Mnt_Inv_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item )
        binding.atcInvoices.threshold = 0
        binding.atcInvoices.setAdapter(InvoiceAdapter)
        binding.btnOpenInvoices.setOnClickListener {
            binding.atcInvoices.showDropDown()
        }

        binding.atcInvoices.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedInvoices = InvoiceAdapter.getItem(position)
            viewModel.selectedCustomer = Customer("", null, null, null, null, "", viewModel.selectedInvoices?.sl_customer_name, "",
                "", "", "", "", "", "", null, null, null, "", viewModel.selectedInvoices?.sl_customerId,
                null, null, null, "", null, null, null, null, null, null, null)
            binding.atcCustomer.setText(viewModel.selectedInvoices?.sl_customer_name, true)
            binding.atcInvoices.dismissDropDown()
        }

        binding.atcInvoices.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.selectedInvoices == null) viewModel.invTerm.value = s.toString() else binding.atcInvoices.dismissDropDown()
            }
        })

        viewModel.mntInvoicesList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            InvoiceAdapter.setInvoices(it)
        })
    }

    private fun initCustomers(){
        val adapter = CustomerAdapter1(requireContext(), R.layout.support_simple_spinner_dropdown_item )

        binding.atcCustomer.threshold = 0
        binding.atcCustomer.dropDownWidth = resources.displayMetrics.widthPixels - 10
        binding.atcCustomer.setAdapter(adapter)
        binding.btnOpenCustomer.setOnClickListener {
            binding.atcCustomer.showDropDown()
        }

        binding.atcCustomer.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomer = adapter.getItem(position)

            if(viewModel.oCu_Id != viewModel.selectedCustomer?.cu_ref_Id){
                //viewModel.clearItems()
            }
            viewModel.cu_Id = viewModel.selectedCustomer?.cu_ref_Id
            //viewModel.setPriceCategory()
            binding.atcCustomer.dismissDropDown()
            viewModel.term.value = ""
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
                    viewModel.selectedCustomer = cu.find { it.cu_ref_Id == viewModel._entityEo?.cust_Id}
                }else{
                    binding.atcCustomer.showDropDown()
                }
            }
        })
    }

    private fun initDevices(){

        val Adapter = Products_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item  )
        binding.atcDevice.threshold = 0
        binding.atcDevice.setAdapter(Adapter)
        binding.btnOpenDevice.setOnClickListener {
            binding.atcDevice.showDropDown()
            binding.atcDevice.dismissDropDown()
        }

        binding.atcDevice.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedDevice = Adapter.getItem(position)
        }

        binding.atcDevice.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.selectedDevice == null) viewModel.devTerm.value = s.toString() else binding.atcDevice.dismissDropDown()
            }
        })

        viewModel.deviceList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            Adapter.setData(it)
        })
    }

    private fun initWarranty(){
        val War_Adapter = Mnt_Warranty_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item  )
        binding.atcWarranty.threshold = 0
        binding.atcWarranty.dropDownWidth = resources.displayMetrics.widthPixels
        binding.atcWarranty.setAdapter(War_Adapter)
        binding.btnOpenWarranty.setOnClickListener {
            binding.atcWarranty.showDropDown()

        }

        binding.atcWarranty.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedWarranty = War_Adapter.getItem(position)
            viewModel.serialNo.value = viewModel.selectedWarranty!!.serial_no
            viewModel.selectedInvoices = Sale(null, null, null, viewModel.selectedWarranty!!.ref_No, null, null, null,null,
                null, null,null, null,null, null,0.0,null,null, null,null,
                null,null, null,0.0, 0.0,0.0,0.0, null,null, null,null,null)
            viewModel.selectedInvoices!!.sl_Id = viewModel.selectedWarranty!!.ref_Id!!
            binding.atcInvoices.setText(viewModel.selectedWarranty?.cus_name, true)
            viewModel.selectedCustomer = Customer("", null, null, null, null, "", viewModel.selectedWarranty?.cus_name, "",
                "", "", "", "", "", "", null, null, null, "", viewModel.selectedWarranty?.cus_Id,
                null, null, null, "", null, null, null, null, null, null, null)
            binding.atcCustomer.setText(viewModel.selectedInvoices?.sl_customer_name, true)
            viewModel.selectedDevice = Product(null, null, viewModel.selectedWarranty!!.prod_name,viewModel.selectedWarranty!!.prod_name, null, null, null,null,
                null, viewModel.selectedWarranty!!.uom_Id, null,null, null, null, null, null, null,null,null, null, null, null,null,
                null, null, null, null, null,null)
            viewModel.selectedDevice!!.pr_Id = viewModel.selectedWarranty!!.prod_Id!!
            binding.atcDevice.setText(viewModel.selectedDevice!!.pr_description)
            binding.atcWarranty.dismissDropDown()
        }

        binding.atcWarranty.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.selectedWarranty == null) viewModel.warTerm.value = s.toString() else binding.atcWarranty.dismissDropDown()
            }
        })
        viewModel.mntWarrantyList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            War_Adapter.setData(it)
        })
    }

    private fun initProducts(){
        val adapter = Products_expiry_Adapter(requireContext(),
            R.layout.support_simple_spinner_dropdown_item)
        binding.atcProduct.threshold = 0
        binding.atcProduct.dropDownWidth = resources.displayMetrics.widthPixels
        binding.atcProduct.setAdapter(adapter)
        binding.btnOpenItems.setOnClickListener {
            binding.atcProduct.showDropDown()
        }

        binding.atcProduct.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedProduct = adapter.getItem(position)
            viewModel.unitPrice = viewModel.selectedProduct!!.pr_unit_price ?: 0.0
            viewModel._whsId.value = viewModel.selectedProduct!!.pr_wr_Id
            viewModel.selectedWhs = Warehouse(viewModel.selectedProduct!!.pr_wr_name, viewModel.selectedProduct!!.pr_wr_name, "")
            viewModel.selectedWhs!!.wr_Id = viewModel.selectedProduct!!.pr_wr_Id!!
            binding.atcWhs.setText(viewModel.selectedProduct!!.pr_wr_name, true)
            viewModel.setProductId(viewModel.selectedProduct!!.pr_Id)
        }
        binding.atcProduct.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.selectedProduct == null) viewModel._term.value = s.toString() else binding.atcProduct.dismissDropDown()
            }
        })

        viewModel.productList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            adapter.setData(it)
        })
    }

    private fun initServices(){
        val Serv_Adapter = Services_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item  )
        binding.atcService.threshold = 0
        binding.atcService.setAdapter(Serv_Adapter)
        binding.btnOpenService.setOnClickListener {
            binding.atcService.showDropDown()
            binding.atcService.dismissDropDown()
        }

        binding.atcService.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedService = Serv_Adapter.getItem(position)
            viewModel.serv_unitPrice = viewModel.selectedService!!.unit_price ?: 0.0
        }

        binding.atcService.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                if(viewModel.selectedService == null) viewModel.srvTerm.value = s.toString() else binding.atcService.dismissDropDown()
            }
        })
        viewModel.servicesList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            Serv_Adapter.setData(it)
        })
    }

    private fun typeAutocompleteInit(data: List<MntType>){
        val adapter = MntType_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            data
        )
        binding.atcMnttype.threshold = 0
        binding.atcMnttype.setAdapter(adapter)
        binding.btnOpenType.setOnClickListener {
            binding.atcMnttype.showDropDown()
        }

        binding.atcMnttype.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedDocType = adapter.getItem(position)
            if(viewModel.selectedDocType!!.code == "Regular Maintenance"){
                viewModel.regTerm.value = ""
            }
            viewModel.mntTypeCode = viewModel.selectedDocType!!.code!!
            viewModel.term.value = ""
        }
    }

    private fun statusAutocompleteInit(data: List<MntStatus>){
        val adapter = MntStatus_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            data
        )
        binding.atcMntStatus.threshold = 0
        binding.atcMntStatus.setAdapter(adapter)
        binding.btnOpenMntStatus.setOnClickListener {
            binding.atcMntStatus.showDropDown()
        }

        binding.atcMntStatus.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedMntStatus = adapter.getItem(position)
        }
    }

    private fun priceCategory(data: List<PriceCategory>){
        val adapter = PriceCategoryAdapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            data
        )
        binding.atcPricelist.threshold = 0
        binding.atcPricelist.setAdapter(adapter)
        binding.btnOpenPricelist.setOnClickListener {
            binding.atcPricelist.showDropDown()
        }

        binding.atcPricelist.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedPriceList = adapter.getItem(position)
            viewModel.price_cat_code = viewModel.selectedPriceList!!.prc_code!!
        }
    }

    private fun whsAutocompleteInti(data : List<Warehouse>){
        val adapter = atc_Whs_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            data
        )
        binding.atcWhs.threshold = 0
        binding.atcWhs.setAdapter(adapter)
        binding.btnOpenWhs.setOnClickListener {
            binding.atcWhs.showDropDown()
        }

        binding.atcWhs.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedWhs = adapter.getItem(position)
            viewModel._whsId.value = viewModel.selectedWhs?.wr_Id
        }
    }

    private fun locAutocompleteInti(data : List<Loc>){
        val adapter = Loc_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            data
        )
        binding.atcLoc.threshold = 0
        binding.atcLoc.setAdapter(adapter)
        binding.btnOpenLoc.setOnClickListener {
            binding.atcLoc.showDropDown()
        }

        binding.atcLoc.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedLoc = adapter.getItem(position)
        }
    }

    override fun onDelete(baseEo: Mnts) {

    }

    override fun onShowDatePicker(v: View) {

    }

    override fun clear(code: String) {
        when(code) {
            "cu"-> {
                viewModel.oCu_Id = viewModel.selectedCustomer?.cu_Id
                viewModel.selectedCustomer = null
                binding.atcCustomer.setText("", true)
            }
            "ty"-> {
                binding.atcMnttype.setText("", true)
                viewModel.selectedDocType = null
            }
            "st" ->{
               binding.atcMntStatus.setText("", true)
               viewModel.selectedMntStatus = null
            }
            "inv" ->{
                binding.atcInvoices.setText("", true)
                viewModel.selectedInvoices = null
            }
            "war" -> {
                binding.atcWarranty.setText("", true)
                viewModel.selectedWarranty = null
            }
            "whs" ->{
                binding.atcWhs.setText("", true)
                viewModel.selectedWhs = null
            }
            "loc" ->{
                binding.atcLoc.setText("", true)
                viewModel.selectedLoc = null
            }
            "dev"->{
                binding.atcDevice.setText("", true)
                viewModel.selectedDevice = null
            }
            "prod"->{
                binding.atcProduct.setText("", true)
                viewModel.selectedProduct = null
            }
            "pl"->{
               binding.atcPricelist.setText("", true)
               viewModel.selectedPriceList = null
            }
            "srv"->{
                binding.atcService.setText("", true)
                viewModel.selectedService = null
            }
            "reg" ->{
                binding.atcRegMnt.setText("", true)
                viewModel.selectedRegMnt = null
            }
        }
    }

    override fun onStarted() {
        hideKeyboard()
        llProgressBar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        hideKeyboard()
        llProgressBar?.visibility = View.GONE
        add_layout.snackbar(message)
    }

    override fun onFailure(message: String) {
        hideKeyboard()
        llProgressBar?.visibility = View.GONE
        add_layout.snackbar(message)
    }


//    fun requestPermissions() = runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA){
//        try{
//            fileUri = mediaHelper.getOutputMediaFileUri()
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
//            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//            if(intent.resolveActivity(requireActivity().packageManager) != null) {
//                startActivityForResult(intent, mediaHelper.getRcCamera())
//            }
//        }catch (e: Exception){
//            Log.e("Permission", e.message.toString())
//        }
//
//    }

    private fun openCamera(){
        Dexter.withContext(requireContext())
            .withPermissions(listOf(
                Manifest.permission.CAMERA
            )
            ).withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if(p0!!.areAllPermissionsGranted()){
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, "New Picture")
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From Your Camera")
                        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(intent, DeliveryEntryFragment.MY_CAMERA_REQUEST_CODE)
                    }
                    else{
                        Toast.makeText(requireActivity(), "You must accept all permission", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    Log.d("PermissionRationale", "Rationale should be shown for permissions: $p0")
                    // Show a dialog explaining why the app needs these permissions
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("This app requires camera and storage permissions to capture and save photos.")
                        .setPositiveButton("Grant") { dialog, _ ->
                            dialog.dismiss()
                            p1?.continuePermissionRequest()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            p1?.cancelPermissionRequest()
                        }
                        .show()
                }

            }).check()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK)
            if(requestCode == DeliveryEntryFragment.MY_CAMERA_REQUEST_CODE){
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                    val fn = mediaHelper.getMyFileName("DLV")
                    val doc = Document(
                        fileName = fn,
                        masterType = "Delivery",
                        base64String = mediaHelper.bitmapToString(bitmap),
                        bmp = bitmap,
                        isNew = "Y"
                    )
                    viewModel.addDocument(doc)
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }
    }
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(resultCode == Activity.RESULT_OK)
//            if(requestCode == mediaHelper.getRcCamera()){
//                val takenImage = BitmapFactory.decodeFile(fileUri.path)
//                //var imstr = mediaHelper.getBitmapToString(imv, fileUri)
//                fileName = mediaHelper.getMyFileName()
//                val doc = Document(
//                    fileName = mediaHelper.getMyFileName(),
//                    masterType = "Maintenance",
//                    base64String = mediaHelper.bitmapToString(takenImage),
//                    bmp = takenImage,
//                    isNew = "Y"
//                )
//                viewModel.addDocument(doc)
//            }
//    }
}