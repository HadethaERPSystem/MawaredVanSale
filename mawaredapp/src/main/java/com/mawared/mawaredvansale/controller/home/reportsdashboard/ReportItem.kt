package com.mawared.mawaredvansale.controller.home.reportsdashboard

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.databinding.ReportItemBinding
import com.xwray.groupie.databinding.BindableItem

class ReportItem(private val menu: Menu, private val viewModel: ReportsViewModel) : BindableItem<ReportItemBinding>() {

    override fun getLayout() = R.layout.report_item

    override fun bind(viewBinding: ReportItemBinding , position: Int) {
        viewBinding.menu = menu
        viewBinding.viewModel = viewModel
    }

    fun getMenu() : Menu{
        return  menu
    }
}