package com.mawared.mawaredvansale.controller.home.dashboard

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.databinding.DashboardFragmentBinding
import com.mawared.mawaredvansale.services.repositories.Status
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.snackbar
import com.mawared.update.AppUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class DashboardFragment : Fragment(), KodeinAware{//}, IMainNavigator {

    override val kodein by kodein()

    private val factory: DashboardViewModelFactory by instance()

    val viewModel by lazy{
        ViewModelProviders.of(this, factory).get(DashboardViewModel::class.java)
    }

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle?): View? {
        val binding: DashboardFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.dashboard_fragment, container, false)

        viewModel.res = resources
        viewModel.ctx = context
        viewModel.system_version = AppUtils.getVersionName(context) //AppUtils.getVersionCode(this)
        //var s =  AppUtils.getVersionCode(context)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        bindUI()

        binding.pullToRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { viewModel.refresh() }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setHasOptionsMenu(true)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    private fun bindUI() = Coroutines.main {
        //progress_bar.show()
        viewModel.menus.observe(viewLifecycleOwner, Observer {
            //progress_bar.hide()
            if(it != null) {
                viewModel.menusCount = it.count()
                initRecyclerView(it.toMenuItem())
            }else{
                viewModel.menusCount = 0
                menurecyclerview.removeAllViews()
            }
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            progress_bar.visibility =  if(viewModel.listIsEmpty() && it.status == Status.RUNNING) View.VISIBLE else View.GONE

            if (viewModel.listIsEmpty() && (it.status == Status.FAILED)) {
                val pack = requireContext().packageName
                val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
                viewModel.errorMessage.value = resources.getString(id)
                ll_error.visibility = View.VISIBLE
                menurecyclerview.visibility = View.GONE
            } else {
                menurecyclerview.visibility = View.VISIBLE
                ll_error.visibility = View.GONE
            }

        })

        //viewModel.loadMenus()
    }

    private fun initRecyclerView(menuItem: List<MenuItem>) {
        val mAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(menuItem)
        }
        var cols = 2
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 3
        }
        menurecyclerview.apply {
            layoutManager = GridLayoutManager(context, cols)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        mAdapter.setOnItemClickListener { item, _ ->
            (item as? MenuItem)?.let {
                showFragment(it.getMenu())
            }
        }
    }

    // extension method to List<Menu>
    private fun List<Menu>.toMenuItem() : List<MenuItem>{
        return this.map {
            MenuItem(it, viewModel)
        }
    }

    fun showFragment(menu: Menu){

        when (menu.menu_code){
            "Invoice" -> {
                if(App.prefs.savedSalesman != null){
                    if(App.prefs.savedSalesman!!.sm_warehouse_id != null){
                        navController.navigate(R.id.action_dashboardFragment_to_invoicesFragment)
                    }else{
                        root_layout?.snackbar(resources.getString(R.string.msg_user_not_hve_warehouse))
                    }
                }else {
                    root_layout?.snackbar(resources.getString(R.string.msg_user_not_authorize))
                }
            }
            "PSOrder"-> {
                if (App.prefs.savedSalesman != null) {
                    navController.navigate(R.id.action_dashboardFragment_to_PSOrdersFragment)
                } else {
                    root_layout?.snackbar(resources.getString(R.string.msg_user_not_authorize))
                }
            }
            "Order"-> {
                if (App.prefs.savedSalesman != null) {
                    navController.navigate(R.id.action_dashboardFragment_to_ordersFragment)
                } else {
                    root_layout?.snackbar(resources.getString(R.string.msg_user_not_authorize))
                }
            }
            "SaleReturn"->{
                if(App.prefs.savedSalesman != null){
                    if(App.prefs.savedSalesman!!.sm_warehouse_id != null){
                        navController.navigate(R.id.action_dashboardFragment_to_saleReturnFragment)
                    }else{
                        root_layout?.snackbar(resources.getString(R.string.msg_user_not_hve_warehouse))
                    }
                }else {
                    root_layout?.snackbar(resources.getString(R.string.msg_user_not_authorize))
                }
            }
            "Transfer" -> {
                if(App.prefs.savedSalesman != null){
                    if(App.prefs.savedSalesman!!.sm_warehouse_id != null){
                        navController.navigate(R.id.action_dashboardFragment_to_transferFragment)
                    }else{
                        root_layout?.snackbar(resources.getString(R.string.msg_user_not_hve_warehouse))
                    }
                }else {
                    root_layout?.snackbar(resources.getString(R.string.msg_user_not_authorize))
                }
            }
            "Receipt"->{
                navController.navigate(R.id.action_dashboardFragment_to_receivableFragment)
            }
            "Payment"->{
                navController.navigate(R.id.action_dashboardFragment_to_payableFragment)
            }
            "Delivery" -> {
                navController.navigate(R.id.action_dashboardFragment_to_deliveryFragment)
            }
            "Survey" -> {
                navController.navigate(R.id.action_dashboardFragment_to_surveyEntryFragment)
            }
            "StockIn"->{

            }
            "StockOut"->{

            }
            "Customer"->{
                navController.navigate(R.id.action_dashboardFragment_to_customerFragment)
            }
            "Reports"->{
                navController.navigate(R.id.action_dashboardFragment_to_reportsFragment)
            }
            "Settings" ->{
                navController.navigate(R.id.action_dashboardFragment_to_settingsFragment)
            }
            "CallCycle" ->{
                navController.navigate(R.id.action_dashboardFragment_to_callCycleFragment)
            }
            "Map" ->{
                navController.navigate(R.id.action_dashboardFragment_to_mapFragment)
            }
            "KPI" ->{
                navController.navigate(R.id.action_dashboardFragment_to_kpiFragment)
            }
        }
    }
}
