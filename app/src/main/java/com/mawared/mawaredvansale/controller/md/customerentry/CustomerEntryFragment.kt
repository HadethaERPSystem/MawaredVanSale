package com.mawared.mawaredvansale.controller.md.customerentry

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.AutoCompleteCustomerGroupAdapter
import com.mawared.mawaredvansale.controller.adapters.AutoCompleteCustomerTypeAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.map.MapsActivity
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Customer_Group
import com.mawared.mawaredvansale.data.db.entities.md.Customer_Payment_Type
import com.mawared.mawaredvansale.databinding.CustomerEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.EXTRA_CURRENT_LOCATION
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.customer_entry_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CustomerEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Customer>,
    IMessageListener {

    override val kodein by kodein()

    private val factory: CustomerEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CustomerEntryViewModel::class.java)
    }

    lateinit var binding: CustomerEntryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.customer_entry_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_customer_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)

        bindUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            val args = CustomerEntryFragmentArgs.fromBundle(arguments!!)
            viewModel.mode = args.mode
            if(viewModel.mode != "Add"){
                viewModel.setCustomerId(args.customerId)
            }
        }

        currentlocation_btn.setOnClickListener {
            viewModel.location = getLocationData()
            viewModel.mcu_latitude.value = viewModel.location?.latitude.toString()
            viewModel.mcu_longitude.value = viewModel.location?.longitude.toString()
        }
        openMap_btn.setOnClickListener {
            val loc = getLocationData()
            val intent = Intent(activity, MapsActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_CURRENT_LOCATION, loc)
            intent.putExtras(bundle)

            startActivity(intent)
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
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main) {
        viewModel.savedEntity.observe(this@CustomerEntryFragment, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                viewModel.onNew()
                activity!!.onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }
        })

        viewModel.entityEo.observe(this@CustomerEntryFragment, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.mcu_code.value = it.cu_code
                viewModel.mcu_barcode.value = it.cu_barcode
                viewModel.mcu_name_ar.value = it.cu_name_ar
                viewModel.mcu_name.value = it.cu_name
                viewModel.mcu_trade_name.value = it.cu_trade_name
                viewModel.mcu_address_ar.value = it.cu_address_ar
                viewModel.mcu_address.value = it.cu_address
                viewModel.mcu_phone.value = it.cu_phone
                viewModel.mcu_mobile.value = it.cu_mobile
                viewModel.mcu_contact_name.value = it.cu_contact_name
                viewModel.mcu_notes.value = it.cu_notes
                viewModel.mcu_balance.value = it.cu_balance.toString()
                viewModel.mcu_credit_limit.value = it.cu_credit_limit.toString()
                viewModel.mcu_payment_terms.value = it.cu_payment_terms
                viewModel.mcu_longitude.value = "${it.cu_longitude}"
                viewModel.mcu_latitude.value = "${it.cu_latitude}"

                binding.atcCpt.setText("${it.cu_payment_Id}", true)
                binding.atcGroup.setText("${it.cu_cg_Id}", true)

            }
        })

        // bind customer type to autocomplete
        viewModel.cpt_List.await().observe(this@CustomerEntryFragment, Observer { cu ->
            if(cu == null) return@Observer
            initCptAutocomplete(cu)
        })
        // bind customer gropu to autocomplete
        viewModel.CG_List.await().observe(this@CustomerEntryFragment, Observer {
            if(it == null) return@Observer
            initAutocompleteCustomerGroup(it)
        })
    }

    // init customer autocomplete view
    private fun initCptAutocomplete(customers: List<Customer_Payment_Type>){
        val adapter = AutoCompleteCustomerTypeAdapter(context!!.applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            customers
        )
        binding.atcCpt.threshold = 0
        //binding.atcCpt.dropDownWidth = resources.displayMetrics.widthPixels - 20
        binding.atcCpt.setAdapter(adapter)
        binding.atcCpt.setOnFocusChangeListener { _, b ->
            if(b) binding.atcCpt.showDropDown()
        }
        binding.atcCpt.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCPT = adapter.getItem(position)
        }
    }

    private fun initAutocompleteCustomerGroup(groups: List<Customer_Group>){
        val adapter = AutoCompleteCustomerGroupAdapter(context!!.applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            groups
        )
        binding.atcGroup.threshold = 0
        //binding.atcGroup.dropDownWidth = resources.displayMetrics.widthPixels - 20
        binding.atcGroup.setAdapter(adapter)
        binding.atcGroup.setOnFocusChangeListener { _, b ->
            if(b) binding.atcGroup.showDropDown()
        }
        binding.atcGroup.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomerGroup = adapter.getItem(position)
        }
    }

    override fun clear(code: String) {
        when(code){
            "cpt" -> binding.atcCpt.setText("", true)
            "cg" -> binding.atcGroup.setText("", true)
        }
    }

    override fun onDelete(baseEo: Customer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onShowDatePicker(v: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStarted() {
        group_loading.show()
    }

    override fun onSuccess(message: String) {
        group_loading.hide()
        mcv_customer.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading.hide()
        mcv_customer.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
