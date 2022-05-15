package com.mawared.mawaredvansale.controller.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.md.Warranty
import com.mawared.mawaredvansale.databinding.MntWarrantyRowBinding
import java.util.*

class Mnt_Warranty_Adapter(context: Context, @LayoutRes private val layoutResource: Int):
    ArrayAdapter<Warranty>(context, layoutResource), Filterable {

    var mdataList: List<Warranty> = arrayListOf()
    var allList: List<Warranty> = arrayListOf()


    fun setData(dataList: List<Warranty>){
        mdataList = dataList.map {
            return@map it
        }
        allList = dataList.map {
            return@map it
        }
        notifyDataSetChanged()
    }
    override fun getCount(): Int {
        return mdataList.count()
    }

    override fun getItem(position: Int): Warranty? {
        return mdataList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: MntWarrantyRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.mnt_warranty_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }
    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                if(filterResults.values != null){
                    addAll(filterResults.values as List<Warranty>)
                    notifyDataSetChanged()
                }
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mdataList
                else
                    allList.filter {
                        it.war_No!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Warranty).war_No as CharSequence
            }
        }
    }
}