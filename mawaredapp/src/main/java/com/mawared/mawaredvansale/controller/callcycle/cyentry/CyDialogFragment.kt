package com.mawared.mawaredvansale.controller.callcycle.cyentry

import android.app.ActionBar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.Lookup_Adapter
import com.mawared.mawaredvansale.controller.common.dialog.GenericDialog
import com.mawared.mawaredvansale.controller.common.gpslocation.GpsCurrentLocation
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.data.db.entities.md.Lookups
import com.mawared.mawaredvansale.databinding.CallCycleDialogFragmentBinding
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.call_cycle_dialog_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CyDialogFragment(val lifecycleOwner: LifecycleOwner, val baseEo: Call_Cycle): DialogFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: CallCycleEntryViewModelFactory by instance()
    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CallCycleEntryViewModel::class.java)
    }


    lateinit var binding: CallCycleDialogFragmentBinding


    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //val view = inflater.inflate( R.layout.call_cycle_dialog_fragment, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.call_cycle_dialog_fragment, container, false)
        viewModel._cyBaseEo = baseEo
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        dialog?.setTitle("Call Cycle")
        val gpsLocation = GpsCurrentLocation(requireActivity(), this)

        val mSave = binding.root.findViewById<Button>(R.id.saveButton)
        mSave.setOnClickListener {
            GenericDialog.showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ){
                viewModel.location = gpsLocation.getLocation()
                viewModel.onSave()
            }
        }

        val mCancel =binding.root.findViewById<Button>(R.id.cancelButton)
        mCancel.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                dismiss()
            }
        })

        bindUI()

        return binding.root
    }

    private fun bindUI() = GlobalScope.launch(Main){

        viewModel.callCycleStatus.observe(lifecycleOwner, Observer {
            if(it != null) statusAutocompleteInit(it)
        })

        viewModel.networkState.observe(lifecycleOwner, Observer {
            progress_bar_dialog_cy.visibility = if (it == NetworkState.WAITING) View.VISIBLE else View.GONE
            if(it == NetworkState.SUCCESS){
                dismiss()
            }else if(it == NetworkState.ERROR){
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
        binding.atcStatus.setOnFocusChangeListener { _, b ->
            if(b) binding.atcStatus.showDropDown()
        }
        binding.atcStatus.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedStatus = adapter.getItem(position)
        }
    }
}