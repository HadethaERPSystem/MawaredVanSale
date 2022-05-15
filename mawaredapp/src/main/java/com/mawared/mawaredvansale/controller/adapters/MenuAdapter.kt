package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import kotlinx.android.synthetic.main.item_menu.view.*


class MenuAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Menu) -> Unit) : BaseAdapter<Menu>(null, layoutResource)  {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            menuName.text = (item.menu_name ?: "")

            if(!item.icon.isNullOrEmpty()){
                val iconId = menuAvatar.context.resources.getIdentifier(item.icon, "drawable", menuAvatar.context.packageName)
                if(iconId != 0){
                    menuAvatar.setImageDrawable(ContextCompat.getDrawable(menuAvatar.context, iconId))
                }
            }
            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Menu
                clickFunc1(item)
            }
        }
    }
}