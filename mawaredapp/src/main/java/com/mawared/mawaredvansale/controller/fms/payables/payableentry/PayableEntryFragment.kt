package com.mawared.mawaredvansale.controller.fms.payables.payableentry

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter1
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.databinding.PayableEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.payable_entry_fragment.*
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
        viewModel.ctx = requireActivity()
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

        if(arguments != null){
            val args = PayableEntryFragmentArgs.fromBundle(requireArguments())
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

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = getString(R.string.layout_payable_entry_title)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        if(viewModel.mode == "View"){
            menu.findItem(R.id.save_btn).isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_btn ->{
                if(!viewModel.isRunning){
                    viewModel.isRunning = true
                    hideKeyboard()
                    showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null, {
                        onStarted()
                        viewModel.location = getLocationData()
                        viewModel.onSave()
                    },{
                        viewModel.isRunning = false
                    })
                }
            }
            R.id.close_btn -> {
                hideKeyboard()
                requireActivity().onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main) {

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
                viewModel.doc_no.value = it.py_doc_no?.toString()
                viewModel.doc_date.value = viewModel.returnDateString(it.py_doc_date!!)
                viewModel.selectedCustomer?.cu_ref_Id = it.py_cu_Id!!
                viewModel.selectedCustomer?.cu_name = it.py_cu_name
                viewModel.bc_amount.value = it.py_amount.toString()
                viewModel.lc_amount.value = it.py_lc_amount.toString()
                viewModel.bc_change.value = it.py_change.toString()
                viewModel.lc_change.value = it.py_lc_change.toString()

                viewModel.comment.value = it.py_comment
                binding.atcCustomer.setText("${it.py_cu_name}", true)

            }
        })

        // Customer autocomplete settings
        val adapter = CustomerAdapter1(requireContext(), R.layout.support_simple_spinner_dropdown_item )

        binding.atcCustomer.threshold = 0
        binding.atcCustomer.dropDownWidth = resources.displayMetrics.widthPixels - 50
        binding.atcCustomer.setAdapter(adapter)
        binding.btnOpenCustomer.setOnClickListener {
            binding.atcCustomer.showDropDown()
        }

        binding.atcCustomer.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomer = adapter.getItem(position)
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
            adapter.setCustomers(cu)
            if(viewModel.mode != "Add" && cu.size > 0 && viewModel._entityEo != null){
                viewModel.selectedCustomer = cu.find { it.cu_ref_Id == viewModel._entityEo?.py_cu_Id}
            }else{
                binding.atcCustomer.showDropDown()
            }
        })


        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(viewLifecycleOwner, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.0
        })

        viewModel.setVoucherCode("Payable")
        viewModel.setCurrencyId(App.prefs.saveUser!!.sf_cr_Id!!)
        llProgressBar?.visibility = View.GONE
    }

    override fun clear(code: String) {
        if(code == "cu"){
            binding.atcCustomer.setText("", true)
        }
    }

    override fun onDelete(baseEo: Payable) {
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
        addPayable_layout?.snackbar(message)
    }

    override fun onFailure(message: String) {
        llProgressBar?.visibility = View.GONE
        addPayable_layout?.snackbar(message)
    }
}
