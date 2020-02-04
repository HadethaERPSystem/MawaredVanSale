package com.mawared.mawaredvansale.controller.inventory.stockin.addstockin

import android.app.DatePickerDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.kodein.di.generic.instance
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.AutoCompleteProductAdapter
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.databinding.AddStockInFragmentBinding
import com.mawared.mawaredvansale.interfaces.IDatePicker
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.interfaces.IResetNavigator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.add_stock_in_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.threeten.bp.LocalDate
import java.util.*

class AddStockInFragment : Fragment(), KodeinAware, IDatePicker, IResetNavigator, IMessageListener {

    override val kodein by kodein()

    val factory: AddStockInViewModelFactory by instance()

    lateinit var binding: AddStockInFragmentBinding

   val viewModel by lazy {
       ViewModelProviders.of(this, factory).get(AddStockInViewModel::class.java)
   }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.add_stock_in_fragment, container, false)

        viewModel.setDatePicker(this)
        viewModel.setNavigator(this)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.docDate.value = "${LocalDate.now()}"
        bindUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.setTitle("Add Invoice")
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
                //viewModel.saveInvoice()
            }
            R.id.close_btn -> {
                activity!!.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Date picker
    override fun ShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->

            //val myFormat = "dd/MM/yyyy" // mention the format you need
            //val sdf = SimpleDateFormat(myFormat, Locale.US)
            viewModel.docDate.value = "${dayOfMonth}-${monthOfYear + 1}-${yr}"

        }, year, month, day)
        dpd.show()
    }

    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main) {

        viewModel.items.observe(viewLifecycleOwner, Observer {
//            if(it == null) return@Observer
//            initRecyclerView(it.toInvoiceItemRow())

        })

        // bind customer to autocomplete
        val customerList = viewModel.customerList.await()
        customerList.observe(viewLifecycleOwner, Observer { cu ->
            if(cu == null) return@Observer
            initCustomerAutocomplete(cu)

        })

        // bind products to autocomplete
        viewModel.productList.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            initProductAutocomplete(it)
        })


        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

       // viewModel.setTerm("")
       // viewModel.setVoucherCode("SaleInvoice")
    }

    // init invoices items
    private fun initRecyclerView(rows: List<StockinItemRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(rows)
        }

        rcv_stockin_items.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    // convert invoice items to invoice items row
    private fun List<Stockin_Items>.toInvoiceItemRow(): List<StockinItemRow>{
        return this.map {
            StockinItemRow(it, viewModel)
        }
    }

    // init customer autocomplete view
    private fun initCustomerAutocomplete(customers: List<Customer>){
        val adapter = CustomerAdapter(context!!.applicationContext,
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
    // init product autocomplete view
    private fun initProductAutocomplete(products: List<Product>){
        val adapter = AutoCompleteProductAdapter(context!!.applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            products
        )
        binding.atcProduct.threshold = 0
        binding.atcProduct.setAdapter(adapter)

        binding.atcProduct.setOnFocusChangeListener { _, b ->
            if(b) binding.atcProduct.showDropDown()
        }
        binding.atcProduct.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedProduct = adapter.getItem(position)

        }
    }

    // clear selected customer
    override fun clear(code: String) {
        when(code){
            "cu" ->  binding.atcCustomer.setText("", true)
            "prod" -> binding.atcProduct.setText("", true)
        }
    }

    override fun onStarted() {
        //progressBar_loading.show()
    }

    override fun onSuccess(message: String) {
        //addInvoice_layout.snackbar(message)
        //progressBar_loading.hide()
    }

    override fun onFailure(message: String) {

        //progressBar_loading.hide()
        //addInvoice_layout.snackbar(message)
        //toast(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
