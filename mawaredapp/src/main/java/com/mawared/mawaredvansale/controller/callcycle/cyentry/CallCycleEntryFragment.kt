package com.mawared.mawaredvansale.controller.callcycle.cyentry

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.Lookup_Adapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.data.db.entities.md.Lookups
import com.mawared.mawaredvansale.databinding.CallCycleEntryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.reObserve
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.call_cycle_entry_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CallCycleEntryFragment : ScopedFragmentLocation(), KodeinAware, IAddNavigator<Call_Cycle>, IMessageListener {

    override val kodein by kodein()
    private val factory: CallCycleEntryViewModelFactory by instance()
    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CallCycleEntryViewModel::class.java)
    }
    lateinit var binding: CallCycleEntryFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.call_cycle_entry_fragment, container, false)

        viewModel.addNavigator = this
        viewModel.msgListener = this
        viewModel.ctx = requireContext()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_call_cycle_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_entry_sub_title)


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            val args = CallCycleEntryFragmentArgs.fromBundle(requireArguments())

            if(args.baseBO != null){
                viewModel._cyBaseEo = args.baseBO
            }
        }
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_btn ->{
                if(!viewModel.isRunning) {
                    viewModel.isRunning = true
                    hideKeyboard()
                    showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm), null , {
                        onStarted()
                        viewModel.location = getLocationData()
                        viewModel.onSave()
                    },{
                        viewModel.isRunning = false
                    })
                }
            }
            R.id.close_btn -> {
                requireActivity().onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }

    override fun onDelete(baseEo: Call_Cycle) {

    }

    override fun onShowDatePicker(v: View) {

    }

    override fun clear(code: String) {

    }

    override fun onStarted() {
    }

    override fun onSuccess(message: String) {

    }

    override fun onFailure(message: String) {
        mcv_callcycle.snackbar(message)
    }


    private fun bindUI() = GlobalScope.launch(Dispatchers.Main){

        viewModel.savedEntity.observe(viewLifecycleOwner, Observer {

        })
        viewModel.callCycleStatus.observe(viewLifecycleOwner, Observer {
            if(it != null) statusAutocompleteInit(it)
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            progress_bar_entry_cy.visibility = if (it == NetworkState.WAITING) View.VISIBLE else View.GONE
            if(it == NetworkState.SUCCESS){
                mcv_callcycle.snackbar(requireActivity().resources.getString(R.string.saved_successfully))
                requireActivity().onBackPressed()
            }else if(it == NetworkState.ERROR){
                viewModel.isRunning = false
                val pack = requireActivity().packageName
                val id = requireActivity().resources.getIdentifier(it.msg,"string", pack)
                mcv_callcycle.snackbar(requireContext().resources.getString(id))
            }

        })
    }

    private fun statusAutocompleteInit(data: List<Lookups>){
        val adapter = Lookup_Adapter(requireContext().applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            data
        )
        binding.atcStatus.threshold = 0
        binding.atcStatus.setAdapter(adapter)
        binding.btnOpenStatus.setOnClickListener {
            binding.atcStatus.showDropDown()
        }

        binding.atcStatus.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedStatus = adapter.getItem(position)
        }
    }
}
