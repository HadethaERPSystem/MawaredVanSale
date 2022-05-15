package com.mawared.mawaredvansale.controller.marketplace.cart

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CartAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragmentLocation
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems
import com.mawared.mawaredvansale.databinding.CartFragmentBinding
import com.mawared.mawaredvansale.databinding.CartPaymentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.cart_fragment.*
import kotlinx.android.synthetic.main.invoice_payment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CartFragment : ScopedFragmentLocation(), KodeinAware, IMessageListener {
    override val kodein by kodein()
    private val factory: CartViewModelFactory by instance()
    private lateinit var binding: CartFragmentBinding
    lateinit var binding1: CartPaymentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CartViewModel::class.java)
    }
    private val layoutId = R.layout.cart_fragment
    private val rv_layoutId = R.layout.item_rv_cart

    private var adapter = CartAdapter(rv_layoutId){
        delete(it){
            val result = "result"
            // Use the Kotlin extension in the fragment-ktx artifact
            requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.ctx = requireContext()
        bindUI()

        binding.saveBtn.setOnClickListener {
            when(viewModel.voucher?.vo_code){
                "SaleInvoice"->{ saveInvoice()}
                "SaleOrder" ->{ saveSaleOrder()}
                "PSOrder" ->{ saveSaleOrder()}
            }
        }
        (requireActivity() as AppCompatActivity).supportActionBar!!.subtitle =  getString(R.string.layout_cart_title)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(arguments != null){
            val args = CartFragmentArgs.fromBundle(requireArguments())
            if(args.customer != null && !args.vocode.isNullOrEmpty()){
                viewModel.customer = args.customer
                viewModel.setVoucherCode(args.vocode!!)
                viewModel.customer_name.value = args.customer?.cu_name_ar
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        rv_cart.setupGrid(requireContext(), adapter as BaseAdapter<Any>, 1)
        loadData()
        if(arguments != null){
            val args = CartFragmentArgs.fromBundle(requireArguments())
            if(args.customer != null && !args.vocode.isNullOrEmpty()){
                viewModel.customer = args.customer
                viewModel.setVoucherCode(args.vocode!!)
                viewModel.customer_name.value = args.customer?.cu_name_ar
            }
        }
    }

    fun bindUI() = GlobalScope.launch(Dispatchers.Main){
        viewModel.mVoucher.observe(viewLifecycleOwner, Observer {
            viewModel.voucher = it
        })

        viewModel.currencyRate.observe(viewLifecycleOwner, Observer {
            viewModel.rate = if(it.cr_rate != null) it.cr_rate!! else 0.0
        })

        viewModel.setCurrencyId(App.prefs.saveUser!!.sf_cr_Id!!)
    }

    fun delete(item: OrderItems, Success: () -> Unit) {
        showDialog(
            requireContext(),
            getString(R.string.delete_dialog_title),
            getString(R.string.msg_confirm_delete),
            item,
            {
                viewModel.delete(it, {
                    mcv_invoices?.snackbar(getString(R.string.msg_success_delete))
                    loadData()
                    Success()
                }, {
                    mcv_invoices?.snackbar(getString(R.string.msg_failure_delete))
                })
            })
    }

    fun deleteAll(Success: () -> Unit){
        showDialog(
            requireContext(),
            getString(R.string.delete_dialog_title),
            getString(R.string.msg_confirm_deleteAll),
            null,
            {
                viewModel.deleteAll( {
                    mcv_invoices?.snackbar(getString(R.string.msg_success_delete))
                    loadData()
                    Success()
                }, {
                    mcv_invoices?.snackbar(getString(R.string.msg_failure_delete))
                })
            })
    }

    fun loadData(){
        viewModel.loadOrders(){
            viewModel.setTotals()
            adapter.setList(viewModel.orders)
        }
    }

    override fun onStarted() {
        progress_bar_cart?.visibility = View.VISIBLE
    }

    override fun onSuccess(message: String) {
        progress_bar_cart?.visibility = View.GONE
        mcv_invoices?.snackbar(message)
    }

    override fun onFailure(message: String) {
        progress_bar_cart?.visibility = View.GONE
        mcv_invoices?.snackbar(message)
    }

    // Save Invoice
    private fun saveInvoice(){
        viewModel.setTotals()
        viewModel.updateRemain()
        if(!viewModel.isRunning){
            viewModel.isRunning = true
            hideKeyboard()
            // create data binding
            val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
            binding1 = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.cart_payment, null, false)
            binding1.viewmodel = viewModel
            binding1.lifecycleOwner = this
            // AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(requireContext())
                .setView(binding1.root)
                .setCustomTitle(titleView)
                .setCancelable(false)

            // show dialog
            val mAlertDialog = mBuilder.show()
            mAlertDialog.saveButton.setOnClickListener {
                showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null, {
                    onStarted()
                    viewModel.location = getLocationData()
                    viewModel.onSaveInvoice ({// On Success
                        mAlertDialog.dismiss()
                        viewModel.isRunning = false
                        viewModel.deleteAll(
                            {
                                loadData()
                                val result = "result"
                                // Use the Kotlin extension in the fragment-ktx artifact
                                requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                            })
                        onSuccess(getString(R.string.saved_successfully))
                    },{
                        // On Fail
                        viewModel.isRunning = false
                        if(!it.isNullOrEmpty())
                            onFailure(it)
                    })
                },{// On Select cancel
                    viewModel.isRunning = false
                })
            }

            mAlertDialog.cancelButton.setOnClickListener {
                viewModel.isRunning = false
                mAlertDialog.dismiss()
            }

            //======== OnTextChanged
            //======== Paid USD
            mAlertDialog.edtxt_amount_sc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            //======== Change USD
            mAlertDialog.edtxt_change_sc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            //======== Paid IQD
            mAlertDialog.edtxt_amount_fc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            //======== Change USD
            mAlertDialog.edtxt_change_fc.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateRemain()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            //====================
        }
    }

    // Save Sale Order or Presale order
    private fun saveSaleOrder(){
        if(!viewModel.isRunning){
            viewModel.isRunning = true
            hideKeyboard()
            showDialog(requireContext(), getString(R.string.save_dialog_title), getString(R.string.msg_save_confirm),null ,{
                onStarted()
                viewModel.location = getLocationData()
                viewModel.onSaveOrder({// On Success
                    viewModel.isRunning = false
                    viewModel.deleteAll(
                        {
                            loadData()
                            val result = "result"
                            // Use the Kotlin extension in the fragment-ktx artifact
                            requireActivity().supportFragmentManager.setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                        })
                    onSuccess(getString(R.string.saved_successfully))
                },{// On Fail
                    viewModel.isRunning = false
                    onFailure(getString(R.string.msg_failure_saved))
                })
            },{
                viewModel.isRunning = false
            })
        }
    }
}
