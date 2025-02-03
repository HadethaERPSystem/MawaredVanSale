package com.mawared.mawaredvansale.controller.sales.delivery.deliveryentry

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
//import com.google.zxing.integration.android.IntentIntegrator
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.dms.Document
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items
import com.mawared.mawaredvansale.databinding.DeliveryEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.MediaHelper
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.delivery_entry_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.util.*

class DeliveryEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Delivery_Items>,
    IMessageListener {

        companion object{
            var MY_CAMERA_REQUEST_CODE = 7171
        }

    override val kodein by kodein()

    private val factory: DeliveryEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(DeliveryEntryViewModel::class.java)
    }

    lateinit var binding: DeliveryEntryFragmentBinding

    var imageUri : Uri? = null

    lateinit var mediaHelper: MediaHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.delivery_entry_fragment, container, false)
        mediaHelper = MediaHelper()
        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.resources = resources
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        binding.btnTakePhoto.setOnClickListener {
            openCamera()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments != null){
            val args = DeliveryEntryFragmentArgs.fromBundle(requireArguments())
            viewModel.mode = args.mode
            if(viewModel.mode != "Add"){
                viewModel.dl_id.value = args.deliveryId
            }

            if(viewModel.mode != "Edit"){
                binding?.btnDelivered?.visibility = View.GONE
                binding?.btnTakePhoto?.visibility = View.GONE
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
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = getString(R.string.layout_entry_sub_title)
    }

//    fun initBarcode() {
//        // this for activity
//        // val scanner = IntentIntegrator(activity)
//        //scanner.initiateScan()
//
//        // this for fragment
//        val scanner = IntentIntegrator.forSupportFragment(this)
//        scanner.setDesiredBarcodeFormats(IntentIntegrator.CODE_128)
//        scanner.setBeepEnabled(false)
//        scanner.initiateScan()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if(resultCode == Activity.RESULT_OK){
//            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//            if(result != null){
//                if(result.contents == null){
//                    lst_layout?.snackbar("")
//                }else{
//                    // get barcode
//                    val barcode = result.contents
//                }
//            }else{
//                super.onActivityResult(requestCode, resultCode, data)
//            }
//        }
//    }

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
                showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ,{
                    onStarted()
                    viewModel.location = getLocationData()
                    viewModel.onSave()
                })
            }
            R.id.close_btn -> {
                requireActivity().onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Main) {

        viewModel._baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                requireActivity().onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }

        })

        viewModel.entityEo.observe(viewLifecycleOwner, Observer {
            if(it != null && !viewModel.isDelivered.value!!){
                viewModel._entityEo = it
                viewModel._entityEo?.DocLines = arrayListOf()
                viewModel.dl_doc_date.value = viewModel.returnDateString(it.dl_doc_date!!)
                viewModel.dl_refNo.value = it.dl_refNo
                viewModel.dl_customer_name.value = it.dl_customer_name
                viewModel.dl_cu_phone.value = it.dl_cu_phone
                viewModel.dl_region.value = it.dl_region_name
                viewModel.dl_net_amount.value = it.dl_net_amount

                viewModel.setItems(it.items)
            }
        })

        viewModel.items.observe(viewLifecycleOwner, Observer {
            llProgressBar?.visibility = View.GONE
            if(it == null) return@Observer
            initRecyclerView(it.toRow())
        })
    }

    // init invoices items
    private fun initRecyclerView(rows: List<DeliveryItemRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }

        rcv_delivery_items.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    // convert invoice items to invoice items row
    private fun List<Delivery_Items>.toRow(): List<DeliveryItemRow>{
        return this.map {
            DeliveryItemRow(it, viewModel)
        }
    }

    // clear
    override fun clear(code: String) {
    }

    override fun onDelete(baseEo: Delivery_Items) {

    }

    override fun onShowDatePicker(v: View) {

    }

    override fun onStarted() {
        llProgressBar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        llProgressBar?.visibility = View.GONE
        lst_layout?.snackbar(message)
    }

    override fun onFailure(message: String) {
        llProgressBar?.visibility = View.GONE
        lst_layout?.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    private fun openCamera(){
        Dexter.withContext(requireContext())
            .withPermissions(listOf(
                Manifest.permission.CAMERA
            )
            ).withListener(object:MultiplePermissionsListener{
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if(p0!!.areAllPermissionsGranted()){
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, "New Picture")
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From Your Camera")
                        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(intent, MY_CAMERA_REQUEST_CODE)
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
            if(requestCode == MY_CAMERA_REQUEST_CODE){
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

}
