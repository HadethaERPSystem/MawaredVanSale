@file:Suppress("DEPRECATION")

package com.mawared.mawaredvansale.controller.reports.stock

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
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.pagination.reports.StockPagedListAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.reports.fms.ReportRowHeader
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.databinding.StockFragmentBinding
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.stock_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*

class StockFragment : ScopedFragment(), KodeinAware, IDateRangePicker {

    override val kodein by kodein()

    private val factory: StockViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(StockViewModel::class.java)
    }
    private lateinit var binding: StockFragmentBinding

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.stock_fragment, container, false)

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


    override fun fromDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, _, _, _ ->
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

    private fun bindUI()= GlobalScope.launch(Main){
        try {

            val pagerAdapter = StockPagedListAdapter(viewModel, requireActivity())
            val gridLayoutManager = GridLayoutManager(requireActivity(), 1)

            rcv_stock.apply {
                layoutManager = gridLayoutManager
                setHasFixedSize(true)
                adapter = pagerAdapter
            }

            val header =
                ReportRowHeader(
                    "${resources.getString(R.string.rpt_pr_image)}",
                    "${resources.getString(R.string.rpt_prod_name)}",
                    "${resources.getString(R.string.rpt_barcode)}",
                    "${resources.getString(R.string.rpt_qty)}"
                )
            pagerAdapter.setHeader(header)

            viewModel.stocks.observe(viewLifecycleOwner, Observer {
                pagerAdapter.submitList(it)
            })

            viewModel.printingStock.observe(viewLifecycleOwner, Observer {
                if(it != null){
                    printReport(it)
                }
            })
            viewModel.doSearch()

            viewModel.networkState.observe(viewLifecycleOwner, Observer {
                progress_bar_stock.visibility = if(viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
                if (viewModel.listIsEmpty() && (it == NetworkState.ERROR || it == NetworkState.NODATA)) {
                    val pack = requireContext().packageName
                    val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
                    viewModel.errorMessage.value = resources.getString(id)
                    txt_error_stock.visibility = View.VISIBLE
                } else {
                    txt_error_stock.visibility = View.GONE
                }

                if(!viewModel.listIsEmpty()){
                    pagerAdapter.setNetworkState(it)
                }
            })
        }catch (ex: Exception){
            Log.i("Exc", "Error is ${ex.message}")
        }
    }

    private fun printReport(dtx: List<StockStatement>){
        try {
            val lang = Locale.getDefault().toString().toLowerCase()
            val tickets = GenerateTicket(requireActivity(), lang).create(
                dtx,
                viewModel.returnDateString(viewModel.dtTo.value) ,
                viewModel._wr_Name ?: "",
                URL_LOGO + "co_black_logo.png",
                "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION",
                null,
                null
            )

            TicketPrinting(requireActivity(), tickets).run()
            onSuccess("Print Successfully")

        } catch (e: Exception) {
            onFailure("Error Exception ${e.message}")
            e.printStackTrace()
        }
    }

    fun onSuccess(message: String) {
        stock_lc.snackbar(message)
    }

    fun onFailure(message: String) {
        stock_lc.snackbar(message)
    }
}
