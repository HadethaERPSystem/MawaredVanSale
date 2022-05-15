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
import com.mawared.mawaredvansale.data.db.entities.md.MntType
import com.mawared.mawaredvansale.databinding.MnttypeRowBinding
import java.util.*

class MntType_Adapter(context: Context, @LayoutRes private val layoutResource: Int, private val dataList: List<MntType>):
    ArrayAdapter<MntType>(context, layoutResource, dataList), Filterable {

    var mdataList: List<MntType>

     init {

             mdataList = dataList.map {
                 return@map it
             }

     }

    override fun getCount(): Int {
        return mdataList.count()
    }

    override fun getItem(position: Int): MntType? {
        return mdataList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: MnttypeRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.mnttype_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }
    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                if(filterResults.values != null){
                    addAll(filterResults.values as List<MntType>)
                    notifyDataSetChanged()
                }
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mdataList
                else
                    dataList.filter {
                        it.name!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as MntType).name as CharSequence
            }
        }
    }
}