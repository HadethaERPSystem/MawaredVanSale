package com.mawared.mawaredvansale.controller.md.customerlist

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mawared.mawaredvansale.App

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.CustomerFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.customer_fragment.*
import kotlinx.android.synthetic.main.payable_fragment.group_loading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CustomerFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Customer> {

    override val kodein by kodein()
    private val factory: CustomerViewModelFactory by instance()

    private lateinit var binding: CustomerFragmentBinding

    val viewModel by lazy {
      ViewModelProviders.of(this, factory).get(CustomerViewModel::class.java)
    }

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.customer_fragment, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_customer_list_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = ""
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_search -> {

            }
            R.id.addBtn -> {
                navController.navigate(R.id.action_customerFragment_to_customerEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI()= GlobalScope.launch(Dispatchers.Main) {
        viewModel.baseEoList.observe(this@CustomerFragment, Observer {
            group_loading.hide()
            if(it == null) return@Observer
            initRecyclerView(it.toRow())
        })

        viewModel.setSalesmanId(App.prefs.savedSalesman!!.sm_id)
    }

    private fun initRecyclerView(saleItem: List<CustomerRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(saleItem)
        }

        rcv_customers.apply {
            layoutManager = LinearLayoutManager(this@CustomerFragment.context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Customer>.toRow(): List<CustomerRow>{
        return this.map {
            CustomerRow( it, viewModel )
        }
    }

    override fun onStarted() {
        group_loading.show()
    }

    override fun onSuccess(message: String) {
        group_loading.hide()
        customer_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading.hide()
        customer_list_lc.snackbar(message)
    }

    override fun onItemDeleteClick(baseEo: Customer) {

    }

    override fun onItemEditClick(baseEo: Customer) {
        val action = CustomerFragmentDirections.actionCustomerFragmentToCustomerEntryFragment()
        action.customerId = baseEo.cu_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Customer) {
        val action = CustomerFragmentDirections.actionCustomerFragmentToCustomerEntryFragment()
        action.customerId = baseEo.cu_Id
        action.mode = "View"
        navController.navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
