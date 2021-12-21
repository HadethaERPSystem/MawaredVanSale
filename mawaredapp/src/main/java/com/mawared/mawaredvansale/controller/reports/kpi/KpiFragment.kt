package com.mawared.mawaredvansale.controller.reports.kpi

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.Lookup_Adapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.data.db.entities.md.Lookups
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash1
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash2
import com.mawared.mawaredvansale.databinding.KpiFragmentBinding
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class KpiFragment : ScopedFragment(), KodeinAware, IDateRangePicker {

    override val kodein by kodein()

    private val factory: KpiViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(KpiViewModel::class.java)
    }

    private lateinit var binding: KpiFragmentBinding

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.kpi_fragment, container, false)

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

    override fun fromDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->
                val mnth = if(monthOfYear + 1 > 9) (monthOfYear + 1).toString() else "0" + (monthOfYear + 1).toString()
                val days = if(dayOfMonth > 9) dayOfMonth.toString() else "0" + dayOfMonth.toString()
                viewModel.dtFrom.value = "${yr}-${mnth}-${days}"
                viewModel.doApplyDateFilter()
            },
            year,
            month,
            day
        )
        dpd.show()
    }
    override fun toDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->
                val mnth = if(monthOfYear + 1 > 9) (monthOfYear + 1).toString() else "0" + (monthOfYear + 1).toString()
                val days = if(dayOfMonth > 9) dayOfMonth.toString() else "0" + dayOfMonth.toString()
                viewModel.dtTo.value = "${yr}-${mnth}-${days}"
                viewModel.doApplyDateFilter()

            },
            year,
            month,
            day
        )
        dpd.show()
    }

    private fun bindUI() = GlobalScope.launch(Dispatchers.Main){
        try {

            // bind customer to autocomplete
            viewModel.planList.observe(viewLifecycleOwner, Observer {
                if (it != null)
                    SalesPlanAtcInit(it)
            })
            viewModel.kpi_sm.observe(viewLifecycleOwner, Observer {
                setUpPieChartData(it)
            })
            viewModel.kpi_cus.observe(viewLifecycleOwner, Observer {
                setupBarChartData(it)
            })
            viewModel.dtFrom.value = viewModel.returnDateString(LocalDate.now().toString())
            viewModel.dtTo.value = viewModel.returnDateString(LocalDate.now().toString())
            viewModel.planId.value = 1
            viewModel.doApplyDateFilter()


//            viewModel.networkState.observe(viewLifecycleOwner, Observer {
//                progress_bar_cu_statement.visibility = if(viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
//                if (viewModel.listIsEmpty() && (it == NetworkState.ERROR || it == NetworkState.NODATA)) {
//                    val pack = requireContext().packageName
//                    val id = requireContext().resources.getIdentifier(it.msg,"string", pack)
//                    viewModel.errorMessage.value = resources.getString(id)
//                    txt_error_cu_statement.visibility = View.VISIBLE
//                } else {
//                    txt_error_cu_statement.visibility = View.GONE
//                }
//
//                if(!viewModel.listIsEmpty()){
//                    pageAdapter.setNetworkState(it)
//                }
//            })

           // viewModel.term.value = ""
        }catch (ex: Exception){
            Log.i("Exc", "Error is ${ex.message}")
        }

    }

    private fun SalesPlanAtcInit(data: List<Lookups>){
        val adapter = Lookup_Adapter(
            requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            data
        )
        binding.atcPlan.threshold = 0
        binding.atcPlan.setAdapter(adapter)
        binding.btnOpenPlan.setOnClickListener {
            binding.atcPlan.showDropDown()
        }

        binding.atcPlan.setOnItemClickListener { _, _, position, _ ->
            val salesPlan = adapter.getItem(position)
            if(salesPlan != null)
                viewModel.planId.value = salesPlan.lk_Id
        }
    }

    // Chart function
    private fun setUpPieChartData(smDash1: sm_dash1) {
        val yVals = ArrayList<PieEntry>()


        yVals.add(PieEntry(smDash1.tot_cust!!, "Total Customers"))
        yVals.add(PieEntry(smDash1.tot_visited!!, "Visited Customers"))
        yVals.add(PieEntry(smDash1.tot_unvisit!!, "Un-Visited Customers"))

        val dataSet = PieDataSet(yVals, "Customers")
        //dataSet.valueTextSize=0f

        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)
        binding.pieChart.data = data
        //binding.pieChart.centerTextRadiusPercent = 0f
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.legend.isEnabled = true
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = "Customers"
        binding.pieChart.animate()
        binding.pieChart.invalidate()
    }

    private fun setupBarChartData(smDash2: sm_dash2) {
        // create BarEntry for Bar Group

        val bargroup = ArrayList<BarEntry>()
        val Total1 = smDash2.pl_sales!! + smDash2.ex_sales!!
        val Total2 = smDash2.pl_collected!! + smDash2.ex_collected!!
        val Total3 = smDash2.pl_time!! + smDash2.ex_time!!
        val Total4 = smDash2.pl_visit!!+ smDash2.ex_visit!!

        val yVal1 = (smDash2.pl_sales!! / Total1)
        val yVal2 = (smDash2.ex_sales!! / Total1)
        val yVal3 = (smDash2.pl_collected!! / Total2)
        val yVal4 = (smDash2.ex_collected!! / Total2)
        val yVal5 = (smDash2.pl_time!! / Total3)
        val yVal6 = (smDash2.ex_time!! / Total3)
        val yVal7 = (smDash2.pl_visit!! / Total4)
        val yVal8 = (smDash2.ex_visit!! / Total4)
        bargroup.add(BarEntry(0f, floatArrayOf(yVal1, yVal2), "Sales"))
        bargroup.add(BarEntry(1f, floatArrayOf(yVal3, yVal4), "Collecting"))
        bargroup.add(BarEntry(2f, floatArrayOf(yVal5, yVal6), "Time"))
        bargroup.add(BarEntry(3f, floatArrayOf(yVal7, yVal8), "Visit"))

        val labels: ArrayList<String> = arrayListOf<String>("Planning", "Actual")//, "Collecting", "Time", "Visit")

        // creating dataset for Bar Group
        val barDataSet = BarDataSet(bargroup, "Sales Plan")
        //HorizontalBarChart()
        val colors = arrayListOf<Int>(
            ColorTemplate.MATERIAL_COLORS[0],
            ColorTemplate.MATERIAL_COLORS[0]
        )
        barDataSet.colors = getColors().toList()//  ColorTemplate.MATERIAL_COLORS.toList() // ContextCompat.getColor(requireContext(), R.color.amber)
        barDataSet.stackLabels = labels.toArray(arrayOfNulls<String>(0))

        val data = BarData(barDataSet)
        data.setDrawValues(true)
        binding.barChart.setData(data)
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.xAxis.isEnabled = true
        binding.barChart.axisLeft.isEnabled = false
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.xAxis.setDrawAxisLine(false)
        binding.barChart.xAxis.labelCount = 3
        binding.barChart.xAxis.setDrawLabels(true)
        binding.barChart.xAxis.valueFormatter = MyXAxisFormatter()
        binding.barChart.description.isEnabled = false
        binding.barChart.animateY(1000)
        binding.barChart.legend.isEnabled = true
        binding.barChart.setPinchZoom(true)
        binding.barChart.data.setDrawValues(false)
    }

    private fun getColors(): IntArray {

        // have as many colors as stack-values per entry
        val colors = IntArray(2)
        System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 2)
        return colors
    }
}
class MyXAxisFormatter : ValueFormatter() {
    private val days = arrayOf("Sales", "Collecting", "Time", "Visit")
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return days.getOrNull(value.toInt()) ?: value.toString()
    }
}