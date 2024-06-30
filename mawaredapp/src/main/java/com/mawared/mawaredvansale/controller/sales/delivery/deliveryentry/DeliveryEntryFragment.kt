package com.mawared.mawaredvansale.controller.sales.delivery.deliveryentry

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.zxing.integration.android.IntentIntegrator
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items
import com.mawared.mawaredvansale.databinding.DeliveryEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
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

class DeliveryEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Delivery_Items>,
    IMessageListener {

    override val kodein by kodein()

    private val factory: DeliveryEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(DeliveryEntryViewModel::class.java)
    }

    lateinit var binding: DeliveryEntryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.delivery_entry_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.resources = resources
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

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
            if(it != null){
                viewModel._entityEo = it
                viewModel.dl_doc_date.value = viewModel.returnDateString(it.dl_doc_date!!)
                viewModel.dl_refNo.value = it.dl_doc_no.toString()
                viewModel.dl_customer_name.value = it.dl_customer_name
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

}
