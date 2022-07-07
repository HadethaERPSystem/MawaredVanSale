package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.StockoutTypeAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import kotlinx.android.synthetic.main.select_stock_out_type_fragment.*

class SelectStockOutTypeFragment : Fragment() {

   var menu: ArrayList<Menu> = arrayListOf()

    lateinit var navController: NavController

    private val adapter = StockoutTypeAdapter(R.layout.stockouttype_item){
        val action = SelectStockOutTypeFragmentDirections.actionSelectStockOutTypeFragmentToSelectInvoiceFragment()
        action.sotType = it.menu_code
        navController.navigate(action)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.select_stock_out_type_fragment, container, false)
        menu = arrayListOf()
        menu.add(Menu( 1, "اخراج بيع", "Sale", ""))
        menu.add(Menu( 2, "اخراج مناقلة", "Transfer", ""))
        menu.add(Menu( 3, "اخراج مردود شراء", "PurchaseReturn", ""))
        menu.add(Menu( 4, "اخراج طلب مواد", "StoreRequisition", ""))

        return  view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        rcv_stockoutType.setupGrid(requireContext(), adapter as BaseAdapter<Any>, 2)
        adapter.setList(menu)
        navController = Navigation.findNavController(view)

    }

}