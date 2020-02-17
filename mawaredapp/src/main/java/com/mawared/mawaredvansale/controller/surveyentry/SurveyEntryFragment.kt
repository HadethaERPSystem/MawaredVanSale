package com.mawared.mawaredvansale.controller.surveyentry

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CustomerAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.srv.Question
import com.mawared.mawaredvansale.data.db.entities.srv.Survey_Detail
import com.mawared.mawaredvansale.databinding.SurveyEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.survey_entry_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class SurveyEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Survey_Detail>,
    IMessageListener {

    override val kodein by kodein()

    private val factory: SurveyEntryViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(SurveyEntryViewModel::class.java)
    }

    lateinit var binding: SurveyEntryFragmentBinding

    // survey variables
    private var SERVER_TOKEN = "survey_question"

    private var SPLASH_DISPLAY_LENGTH = 3000

    private var TAG = SurveyEntryFragment::class.java.getSimpleName()

    private var allSurveyQuestions: List<Survey_Detail>? = null

    private var answerList: List<String>? = null

    private var dotLayout: LinearLayout? = null

    private var dots: ArrayList<ImageView> = arrayListOf()

    private var dotsCount = 10

    private var linearLayout: LinearLayout? = null

    private var nextQuestionBtn: Button? = null

    private var noSurveyText: TextView? = null

    private var progressBar: ProgressBar? = null

    private var questionNumber = 0

    private var radioGroup: RadioGroup? = null
    private var et_text_survey: EditText? = null
    private var et_area_survey: EditText? = null

    private var relativeLayout: RelativeLayout? = null

    private var surveyCategory: String? = null

    private var surveyQuestionView: TextView? = null

    private var surveyTotalCount = 0


    private var btn_next: Button? = null
    private var btn_prev: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.survey_entry_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.resources = this.resources

        viewModel.mSrv_vst_date.value = "${LocalDate.now()}"
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        dotLayout = binding.root.findViewById(R.id.dot_layout)
        radioGroup = binding.root.findViewById(R.id.survey_radio_group)
        et_text_survey = binding.root.findViewById(R.id.survey_tv)
        et_area_survey = binding.root.findViewById(R.id.survey_tva)
        btn_next = binding.root.findViewById(R.id.next_qn_btn)
        btn_prev = binding.root.findViewById(R.id.prev_qn_btn)

        btn_next!!.setOnClickListener {
            btn_prev!!.visibility = if(questionNumber == 0) View.GONE else View.VISIBLE
            if(questionNumber <= surveyTotalCount -1){
                when(viewModel.qnEoList[questionNumber-1].qn_input_type){
                    "text" -> {
                        if(viewModel.mAnswer_text.value.isNullOrEmpty()){
                            onFailure("You must select an answer")
                            return@setOnClickListener
                        }
                        viewModel.qnEoList[questionNumber-1].qn_answer = viewModel.mAnswer_text.value
                    }
                    "area" -> {
                        if(viewModel.mAnswer_text.value.isNullOrEmpty()){
                            onFailure("You must select an answer")
                            return@setOnClickListener
                        }
                        viewModel.qnEoList[questionNumber-1].qn_answer = viewModel.mAnswer_area.value
                    }
                    "radio" -> {
                        val i = radioGroup!!.checkedRadioButtonId
                        if(i == -1){
                            onFailure("You must select an answer")
                            return@setOnClickListener
                        }
                        val str = activity!!.findViewById<RadioButton>(i).text.toString()
                        viewModel.qnEoList[questionNumber-1].qn_answer = str
                    }
                }
                drawPageSelectionIndicators(questionNumber)
                displaySurveyQuestion(viewModel.qnEoList)
                if(questionNumber == surveyTotalCount -1){
                    btn_next!!.text = getString(R.string.finish_btn)
                }else{
                    btn_next!!.text = getString(R.string.next_btn)
                }
            }else{
                onSuccess("Saved successfully")
            }

        }
        btn_prev!!.setOnClickListener {
            questionNumber--
            drawPageSelectionIndicators(questionNumber)
            displaySurveyQuestion(viewModel.qnEoList)
        }

        bindUI()


        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_survey_entry_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)

        return binding.root
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        super.onCreate(savedInstanceState)

    }
    fun initBarcode() {
        // this for activity
        // val scanner = IntentIntegrator(activity)
        //scanner.initiateScan()

        // this for fragment
        val scanner = IntentIntegrator.forSupportFragment(this)
        scanner.setDesiredBarcodeFormats(IntentIntegrator.CODE_128)
        scanner.setBeepEnabled(false)
        scanner.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if(result != null){
                if(result.contents == null){
                    entry_layout.snackbar("")
                }else{
                    // get barcode
                    val barcode = result.contents
                }
            }else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

//    // inflate the menu
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.add_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    // handle item clicks of menu
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.save_btn ->{
//                showDialog(context!!, getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ){
//                    onStarted()
//                    viewModel.location = getLocationData()
//                    //viewModel.onSave()
//                }
//            }
//            R.id.close_btn -> {
//                activity!!.onBackPressed()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }


    // bind recycler view and autocomplete
    private fun bindUI() = GlobalScope.launch(Dispatchers.Main) {

        val items = viewModel.questions.await()
        items.observe(viewLifecycleOwner, Observer {
            //group_loading.hide()
            if(it == null) return@Observer
            viewModel.qnEoList = it.sortedBy {
                it.qn_Id
            }
            surveyTotalCount = it.size
            drawPageSelectionIndicators(questionNumber)
            displaySurveyQuestion(viewModel.qnEoList)
        })

        // bind customer to autocomplete
        viewModel.customerList.observe(viewLifecycleOwner, Observer { cu ->
            if(cu == null) return@Observer
            initCustomerAutocomplete(cu)

        })

        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

    }

    // init customer autocomplete view
    private fun initCustomerAutocomplete(customers: List<Customer>){
        val adapter = CustomerAdapter(context!!.applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            customers
        )
        binding.atcCustomer.threshold = 0
        binding.atcCustomer.dropDownWidth = resources.displayMetrics.widthPixels
        binding.atcCustomer.setAdapter(adapter)
        binding.atcCustomer.setOnFocusChangeListener { _, b ->
            if(b) binding.atcCustomer.showDropDown()
        }
        binding.atcCustomer.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCustomer = adapter.getItem(position)
        }

    }

    // clear
    override fun clear(code: String) {
        when(code) {
            "cu"-> {
                binding.atcCustomer.setText("", true)
            }
            "prod"-> {

            }
        }

    }

    override fun onDelete(baseEo: Survey_Detail) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            //viewModel.deleteItem(it)
        }
    }

    override fun onShowDatePicker(v: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->
            viewModel.mSrv_vst_date.value = "${dayOfMonth}-${monthOfYear + 1}-${yr}"

        }, year, month, day)
        dpd.show()
    }

    override fun onStarted() {
    }

    override fun onSuccess(message: String) {
        entry_layout.snackbar(message)
    }

    override fun onFailure(message: String) {
        entry_layout.snackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }


    private fun displaySurveyQuestion(paramList: List<Question>) {
        val data = paramList[this.questionNumber]
        viewModel.mQustion_Name.value = data.qn_name
        et_text_survey!!.visibility = View.GONE
        et_area_survey!!.visibility = View.GONE
        radioGroup!!.visibility = View.GONE
        when(data.qn_input_type){
            "text" -> {
                et_text_survey!!.visibility = View.VISIBLE

                viewModel.mAnswer_text.value = data.qn_answer ?: ""
            }
            "area" -> {
                et_area_survey!!.visibility = View.VISIBLE
                viewModel.mAnswer_text.value = data.qn_answer ?: ""
            }
            "radio" ->{
                radioGroup!!.removeAllViews()
                radioGroup!!.visibility = View.VISIBLE
                var j = 1
                for (i: String in data.qn_option!!){
                    val button: RadioButton = RadioButton(activity)
                    button.id = j
                    button.text = i
                    button.isChecked = false
                    if(data.qn_answer != null && data.qn_answer == i){
                        button.isChecked = true
                    }
                    radioGroup!!.addView(button)
                    j++
                }
            }
        }
        questionNumber++
    }

    private fun drawPageSelectionIndicators(paramInt: Int) {
        if (this.dotLayout != null)
            this.dotLayout?.removeAllViews()
        //this.dots = arrayOf(this.surveyTotalCount)
        for (i in 0 until this.surveyTotalCount) {
            this.dots.add(ImageView(activity!!))
            if (i == paramInt) {
                this.dots[i].setImageDrawable(resources.getDrawable(R.drawable.selecteddot))
            } else {
                this.dots[i].setImageDrawable(resources.getDrawable(R.drawable.unselecteddot))
            }
            val layoutParams = LinearLayout.LayoutParams(-2, -2)
            layoutParams.setMargins(4, 0, 4, 0)
            this.dotLayout!!.addView(this.dots[i] as View, layoutParams as ViewGroup.LayoutParams)
        }
    }

}
