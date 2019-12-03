package com.mawared.mawaredvansale.controller.home.dashboard

import android.content.res.Resources
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.databinding.ItemMenuBinding
import com.xwray.groupie.databinding.BindableItem

class MenuItem(private val menu: Menu, private val viewModel: DashboardViewModel) : BindableItem<ItemMenuBinding>() {

    override fun getLayout() = R.layout.item_menu

    override fun bind(viewBinding: ItemMenuBinding , position: Int) {
         viewBinding.menu = menu
        viewBinding.viewModel = viewModel
    }

    fun getMenu() : Menu{
        return  menu
    }
}