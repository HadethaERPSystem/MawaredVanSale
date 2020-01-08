package com.mawared.mawaredvansale.controller.sales.delivery.deliverylist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist.InvoicesFragmentDirections
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.databinding.DeliveryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.delivery_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class DeliveryFragment : ScopedFragment(), KodeinAware, IMessageListener, IMainNavigator<Delivery> {

    override val kodein by kodein()

    private val factory: DeliveryViewModelFactory by instance()

    private lateinit var binding: DeliveryFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(DeliveryViewModel::class.java)
    }

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.delivery_fragment, container, false)

        viewModel.navigator = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItem = menu.findItem(R.id.addBtn)
        menuItem.isVisible = false
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_delivery_list_title)
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
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI()= GlobalScope.launch(Dispatchers.Main) {
        viewModel.entityEoList.observe(viewLifecycleOwner, Observer { dl ->
            gp_loading_dl.hide()
            if(dl == null) return@Observer
            initRecyclerView(dl.toRow())
        })

        viewModel.baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null){
                mPrint(it)
            }
        })
        viewModel.setCustomer(null)
    }

    private fun initRecyclerView(baseEo: List<DeliveryRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(baseEo)
        }

        rcv_delivery.apply {
            layoutManager = LinearLayoutManager(this@DeliveryFragment.context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Delivery>.toRow(): List<DeliveryRow>{
        return this.map {
            DeliveryRow( it, viewModel )
        }
    }

    override fun onStarted() {
        gp_loading_dl.show()
    }

    override fun onSuccess(message: String) {
        gp_loading_dl.hide()
        delivery_list_lc.snackbar(message)
    }

    override fun onFailure(message: String) {
        gp_loading_dl.hide()
        delivery_list_lc.snackbar(message)
    }

    override fun onItemDeleteClick(baseEo: Delivery) {
    }

    override fun onItemEditClick(baseEo: Delivery) {
        val action = DeliveryFragmentDirections.actionDeliveryFragmentToDeliveryEntryFragment()
        action.deliveryId = baseEo.dl_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Delivery) {
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToAddInvoiceFragment()
        action.saleId = baseEo.dl_Id
        action.mode = "View"
        navController.navigate(action)
    }

    fun mPrint(baseEo: Delivery){

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

}
