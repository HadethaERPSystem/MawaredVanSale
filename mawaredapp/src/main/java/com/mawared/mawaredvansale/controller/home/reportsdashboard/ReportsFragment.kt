package com.mawared.mawaredvansale.controller.home.reportsdashboard

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
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.databinding.ReportsFragmentBinding
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.update.AppUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.reports_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ReportsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()

    private val factory: ReportViewModelFactory by instance()

    val viewModel by lazy{
        ViewModelProviders.of(this, factory).get(ReportsViewModel::class.java)
    }

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle?): View? {
        val binding: ReportsFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.reports_fragment, container, false)

        viewModel.ctx = context
        viewModel.system_version = AppUtils.getVersionName(context) //AppUtils.getVersionCode(this)
        var s =  AppUtils.getVersionCode(context)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        bindUI()

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.reports_page_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = ""

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    private fun bindUI() = Coroutines.main {
        viewModel.menus.observe(viewLifecycleOwner, Observer {
            initRecyclerView(it.toMenuItem())
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            progress_bar.visibility =  if(viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
            txt_error_menu.visibility = if(viewModel.listIsEmpty() && it == NetworkState.ERROR) View.VISIBLE else View.GONE
        })
    }

    private fun initRecyclerView(menuItem: List<ReportItem>) {
        val mAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(menuItem)
        }

        rcv_reports.apply {
            layoutManager = GridLayoutManager(context, 2)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        mAdapter.setOnItemClickListener { item, _ ->
            (item as? ReportItem)?.let {
                showFragment(it.getMenu())
            }
        }
    }

    // extension method to List<Menu>
    private fun List<Menu>.toMenuItem() : List<ReportItem>{
        return this.map {ReportItem(it, viewModel)}
    }

    fun showFragment(menu: Menu){
        try {
            when (menu.menu_code){
                "CustomerStatement" ->{
                    navController.navigate(R.id.action_reportsFragment_to_customerStatementFragment)
                }
                "CashbookStatement"->{
                    navController.navigate(R.id.action_reportsFragment_to_cashbookStatementFragment)
                }
                "SalesStatement"->{
                    navController.navigate(R.id.action_reportsFragment_to_salesStatementFragment)
                }
                "StockStatement"->{
                    navController.navigate(R.id.action_reportsFragment_to_stockFragment)
                }
            }
        }catch (e: Exception){
            txt_error_menu.visibility = View.VISIBLE
        }

    }
}
