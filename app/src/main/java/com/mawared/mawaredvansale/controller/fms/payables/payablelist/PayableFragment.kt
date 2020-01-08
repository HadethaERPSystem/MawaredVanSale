package com.mawared.mawaredvansale.controller.fms.payables.payablelist

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
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.databinding.PayableFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.payable_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PayableFragment : ScopedFragment(), KodeinAware, IMainNavigator<Payable>, IMessageListener {

    override val kodein by kodein()
    val factory: PayableViewModelFactory by instance()
    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(PayableViewModel::class.java)
    }
    private lateinit var binding: PayableFragmentBinding

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.payable_fragment, container, false)

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
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_payable_list_title)
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
                navController.navigate(R.id.action_payableFragment_to_payableEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI() {
        viewModel.baseEoList.observe(viewLifecycleOwner, Observer {
            group_loading?.hide()
            if(it == null) return@Observer
            initRecyclerView(it.sortedByDescending { it.py_doc_date }.toPayableRow())
        })

        viewModel.deleteRecord.observe(viewLifecycleOwner, Observer {
            group_loading.hide()
            if(it == "Successful"){
                onSuccess(getString(R.string.msg_success_delete))
                viewModel.setCustomer(null)
            }
            else{
                onFailure(getString(R.string.msg_failure_delete))
            }
        })
        viewModel.setCustomer(null)
    }

    private fun initRecyclerView(items: List<PayableRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(items)
        }

        rcv_payable.apply {
            layoutManager = LinearLayoutManager(this@PayableFragment.context!!)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Payable>.toPayableRow(): List<PayableRow>{
        return this.map {
            PayableRow(it, viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    override fun onItemDeleteClick(baseEo: Payable) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            onStarted()
            viewModel.confirmDelete(it)
        }
    }

    override fun onItemEditClick(baseEo: Payable) {
        val action = PayableFragmentDirections.actionPayableFragmentToPayableEntryFragment()
        action.pyId = baseEo.py_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Payable) {
        val action = PayableFragmentDirections.actionPayableFragmentToPayableEntryFragment()
        action.pyId = baseEo.py_Id
        action.mode ="View"
        navController.navigate(action)
    }

    override fun onStarted() {
        group_loading?.show()
    }

    override fun onSuccess(message: String) {
        group_loading?.hide()
        payable_list_cl?.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading?.hide()
        payable_list_cl?.snackbar(message)
    }
}
