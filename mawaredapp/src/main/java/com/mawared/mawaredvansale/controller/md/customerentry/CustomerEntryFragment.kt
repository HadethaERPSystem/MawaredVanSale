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
import com.mawared.mawaredvansale.controller.adapters.CustomerCategoryAdapter
import com.mawared.mawaredvansale.controller.adapters.PriceCategoryAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.map.MapsActivity
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.databinding.CustomerEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.EXTRA_CURRENT_LOCATION
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
        viewModel.resources = resources
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
        viewModel._baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                activity!!.onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }
        })

        viewModel.entityEo.observe(viewLifecycleOwner, Observer {
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
                viewModel.mcu_longitude.value = if(it.cu_longitude != null) it.cu_longitude.toString() else ""
                viewModel.mcu_latitude.value = if(it.cu_latitude != null) it.cu_latitude.toString() else ""
                val payment_name = it.cu_payment_name ?: ""
                val group_name = it.cu_cg_name ?: ""
                val cat_name = it.cu_cat_name ?: ""
                val price_cat = it.cu_price_cat_name
                binding.atcCpt.setText(payment_name, true)
                binding.atcGroup.setText(group_name, true)
                binding.atcCategory.setText(cat_name, true)
                binding.atcPriceCategory.setText(price_cat, true)
            }
        })

        // bind customer type to autocomplete
        viewModel.cpt_List.await().observe(viewLifecycleOwner, Observer { cu ->
            if(cu == null) return@Observer
            initCptAutocomplete(cu)
        })
        // bind customer gropu to autocomplete
        viewModel.CG_List.await().observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initAutocompleteCustomerGroup(it)
        })

        viewModel.Category_List.await().observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initCategory(it)
        })

        viewModel.priceCatList.await().observe(viewLifecycleOwner, Observer {
            if(it != null)
                initAtcPriceCategory(it)
        })
    }

    // init customer autocomplete view
    private fun initCptAutocomplete(customers: List<Customer_Payment_Type>){
        val adapter = AutoCompleteCustomerTypeAdapter(context!!.applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            customers
        )
        binding.atcCpt.threshold = 0
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
        binding.atcGroup.setAdapter(adapter)
        binding.atcGroup.setOnFocusChangeListener { _, b ->
            if(b) binding.atcGroup.showDropDown()
        }
        binding.atcGroup.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomerGroup = adapter.getItem(position)
        }
    }

    private fun initCategory(list: List<Customer_Category>){
        val adapter = CustomerCategoryAdapter(activity!!,
            R.layout.support_simple_spinner_dropdown_item,
            list
        )
        binding.atcCategory.threshold = 0
        binding.atcCategory.setAdapter(adapter)
        binding.atcCategory.setOnFocusChangeListener { _, b ->
            if(b) binding.atcCategory.showDropDown()
        }
        binding.atcCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomerCat = adapter.getItem(position)
        }
    }

    private fun initAtcPriceCategory(prclist: List<PriceCategory>){
        val adapter = PriceCategoryAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, prclist)
        binding.atcPriceCategory.threshold = 0
        binding.atcPriceCategory.setAdapter(adapter)
        binding.atcPriceCategory.setOnFocusChangeListener { _, b ->
            if(b) binding.atcPriceCategory.showDropDown()
        }
        binding.atcPriceCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedPriceCategory = adapter.getItem(position)
        }
    }

    override fun clear(code: String) {
        when(code){
            "cpt" -> binding.atcCpt.setText("", true)
            "cg" -> binding.atcGroup.setText("", true)
            "cat" -> binding.atcCategory.setText("", true)
            "prcode" -> binding.atcPriceCategory.setText("", true)
        }
    }

    override fun onDelete(baseEo: Customer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onShowDatePicker(v: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStarted() {
        llProgressBar?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        llProgressBar?.visibility = View.GONE
        mcv_customer?.snackbar(message)
    }

    override fun onFailure(message: String) {
        llProgressBar?.visibility = View.GONE
        mcv_customer?.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
