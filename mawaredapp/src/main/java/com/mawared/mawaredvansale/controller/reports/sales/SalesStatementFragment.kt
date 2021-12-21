package com.mawared.mawaredvansale.controller.reports.sales

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.reports.SalesStatementPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.reports.fms.ReportRowHeader
import com.mawared.mawaredvansale.databinding.SalesStatementFragmentBinding
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import com.mawared.mawaredvansale.services.repositories.NetworkState
import kotlinx.android.synthetic.main.sales_statement_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*

class SalesStatementFragment : ScopedFragment(), KodeinAware, IDateRangePicker {

    override val kodein by kodein()

    private val factory: SalesStatementViewModelFactory by instance()

    private lateinit var binding: SalesStatementFragmentBinding

    private lateinit var navController: NavController

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(SalesStatementViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.sales_statement_fragment, container, false)

        viewModel.dateNavigator = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.report_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_print -> {

            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindUI()= GlobalScope.launch(Main) {

        try {

            val pageAdapter = SalesStatementPagedListAdapter(viewModel, requireActivity())
            val gridLayoutManager = GridLayoutManager(requireActivity(), 1)
            rcv_sales.apply {
                layoutManager = gridLayoutManager
                setHasFixedSize(true)
                adapter = pageAdapter
            }

            val header = ReportRowHeader(
                "#",
                "${resources.getString(R.string.rpt_customer)}",
                "${resources.getString(R.string.rpt_Ref_No)}",
                "${resources.getString(R.string.rpt_Ref_Date)}",
                "${resources.getString(R.string.rpt_pr_image)}",
                "${resources.getString(R.string.rpt_prod_name)}",
                "${resources.getString(R.string.rpt_qty)}",
                "${resources.getString(R.string.rpt_net_amount1)} ${App.prefs.saveUser?.cr_code}"
            )
            pageAdapter.setHeader(header)
            viewModel.sales.observe(viewLifecycleOwner, Observer {
                if(it != null){
                    pageAdapter.submitList(it)
                }
            })

            viewModel.doSearch()

            viewModel.rcv_networkState.observe(viewLifecycleOwner, Observer {
                progress_bar_sales.visibility = if(viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
                if (viewModel.listIsEmpty() && (it == NetworkState.ERROR || it == NetworkState.NODATA)) {
                    val pack = requireContext().packageName
                    val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
                    viewModel.errorMessage.value = resources.getString(id)
                    txt_error_sales.visibility = View.VISIBLE
                } else {
                    txt_error_sales.visibility = View.GONE
                }

                if(!viewModel.listIsEmpty()){
                    pageAdapter.setNetworkState(it)
                }

            })
        }catch (e: Exception){
            Log.e("Exc", "Error is ${e.message}")
        }
    }

    override fun fromDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->

            viewModel.dtFrom.value = "${yr}-${monthOfYear + 1}-${dayOfMonth}"
            viewModel.doSearch()
        }, year, month, day)
        dpd.show()
    }

    override fun toDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->

            viewModel.dtTo.value = "${yr}-${monthOfYear + 1}-${dayOfMonth}"
            viewModel.doSearch()

        }, year, month, day)
        dpd.show()
    }


}
