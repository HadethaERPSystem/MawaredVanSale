package com.mawared.mawaredvansale.controller.fms.receivables.receivablelist

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
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.databinding.ReceivableFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.receivable_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ReceivableFragment : ScopedFragment(), KodeinAware, IMainNavigator<Receivable>, IMessageListener {

    override val kodein by kodein()

    val factory: ReceivableViewModelFactory by instance()

    val viewModel by lazy {
      ViewModelProviders.of(this, factory).get(ReceivableViewModel::class.java)
    }

    private lateinit var binding: ReceivableFragmentBinding

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.receivable_fragment, container, false)

        viewModel.navigator = this
        viewModel.msgListener = this
        viewModel.activity = activity as AppCompatActivity
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_receivable_list_title)
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
                navController.navigate(R.id.action_receivableFragment_to_receivableEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // binding recycler view
    private fun bindUI() {
        viewModel.baseEoList.observe(viewLifecycleOwner, Observer {
            group_loading?.hide()
            if(it == null) return@Observer
            initRecyclerView(it.sortedByDescending { it.rcv_doc_date }.toReceivableRow())
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

        viewModel.baseEo.observe(viewLifecycleOwner, Observer {
            if(it != null && viewModel.isPrint) {
                viewModel.onPrintTicket(it)
            }

        })

        viewModel.setCustomer(null)
    }

    private fun initRecyclerView(items: List<ReceivableRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(items)
        }

        rcv_receivable.apply {
            layoutManager = LinearLayoutManager(this@ReceivableFragment.context!!)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Receivable>.toReceivableRow(): List<ReceivableRow>{
        return this.map {
            ReceivableRow(it, viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    override fun onItemDeleteClick(baseEo: Receivable) {
        showDialog(context!!, getString(R.string.delete_dialog_title), getString(R.string.msg_confirm_delete), baseEo){
            onStarted()
            viewModel.confirmDelete(it)
        }
    }

    override fun onItemEditClick(baseEo: Receivable) {
        val action = ReceivableFragmentDirections.actionReceivableFragmentToReceivableEntryFragment()
        action.rcvId = baseEo.rcv_Id
        action.mode ="Edit"
        navController.navigate(action)
    }

    override fun onItemViewClick(baseEo: Receivable) {
        val action = ReceivableFragmentDirections.actionReceivableFragmentToReceivableEntryFragment()
        action.rcvId = baseEo.rcv_Id
        action.mode ="View"
        navController.navigate(action)
    }

    override fun onStarted() {
        group_loading?.show()
    }

    override fun onSuccess(message: String) {
        group_loading?.hide()
        receivable_list_cl.snackbar(message)
    }

    override fun onFailure(message: String) {
        group_loading?.hide()
        receivable_list_cl?.snackbar(message)
    }
}
