package com.mawared.mawaredvansale.controller.fms.payables.payableentry

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.PayableEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.payable_entry_fragment.*
import kotlinx.android.synthetic.main.payable_fragment.group_loading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*

class PayableEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Payable>,  IMessageListener {

    override val kodein by kodein()

    private val factory: PayableEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(PayableEntryViewModel::class.java)
    }

    lateinit var binding: PayableEntryFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.payable_entry_fragment, container, false)

        //viewModel.showDatePicker = this
        viewModel.ctx = activity!!
        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.doc_date.value = "${LocalDate.now()}"

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_payable_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)
        if(arguments != null){
            val args = PayableEntryFragmentArgs.fromBundle(arguments!!)
            viewModel.mode = args.mode
            if(viewModel.mode != "Add"){
                viewModel.setPayableId(args.pyId)
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
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main) {

        viewModel._baseEo.observe(this@PayableEntryFragment, Observer {
            if(it != null){
                onSuccess(getString(R.string.msg_success_saved))
                activity!!.onBackPressed()
            }else{
                onFailure(getString(R.string.msg_failure_saved))
            }
        })

        viewModel.entityEo.observe(this@PayableEntryFragment, Observer {
            if(it != null){
                viewModel._entityEo = it
                viewModel.doc_no.value = it.py_doc_no?.toString()
                viewModel.doc_date.value = viewModel.returnDateString(it.py_doc_date!!)
                viewModel.selectedCustomer?.cu_Id = it.py_cu_Id!!
                viewModel.selectedCustomer?.cu_name = it.py_cu_name
                viewModel.bc_amount.value = it.py_amount.toString()
                viewModel.lc_amount.value = it.py_lc_amount.toString()
                viewModel.bc_change.value = it.py_change.toString()
                viewModel.lc_change.value = it.py_lc_change.toString()

                viewModel.comment.value = it.py_comment
                binding.atcCustomer.setText("${it.py_cu_name}", true)

            }
        })

        // bind customer to autocomplete
        val customerList = viewModel.customerList.await()
        customerList.observe(this@PayableEntryFragment, Observer { cu ->
            if(cu == null) return@Observer
            initCustomerAutocomplete(cu)

        })

        viewModel.mVoucher.observe(this@PayableEntryFragment, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(this@PayableEntryFragment, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.00
        })

        viewModel.saleCurrency.observe(this@PayableEntryFragment, Observer {
            viewModel.bcCurrency = it
        })

        viewModel.ndCurrency.observe(this@PayableEntryFragment, Observer {
            viewModel.lcCurrency = it
        })

        viewModel.setVoucherCode("Payable")
        viewModel.setCurrencyId(App.prefs.saveUser!!.sl_cr_Id!!)
        viewModel.setSaleCurrency("$")
        viewModel.setSecondCurrency("IQD")
        group_loading.hide()
    }

    // init customer autocomplete view
    private fun initCustomerAutocomplete(customers: List<Customer>){
        val adapter = CustomerAdapter(context!!,
            R.layout.support_simple_spinner_dropdown_item,
            customers
        )
        binding.atcCustomer.threshold = 0
        binding.atcCustomer.setAdapter(adapter)
        binding.atcCustomer.setOnFocusChangeListener { _, b ->
            if(b) binding.atcCustomer.showDropDown()
        }
        binding.atcCustomer.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomer = adapter.getItem(position)
        }

    }

    override fun clear(code: String) {
        if(code == "cu"){
            binding.atcCustomer.setText("", true)
        }
    }

    override fun onDelete(baseEo: Payable) {
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
        group_loading.show()
    }

    override fun onSuccess(message: String) {
        group_loading.hide()
        addPayable_layout.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading.hide()
        addPayable_layout.snackbar(message)
    }
}
