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
import com.mawared.mawaredvansale.data.db.entities.md.Lookups
import com.mawared.mawaredvansale.databinding.LookupRowBinding
import java.util.*

class Lookup_Adapter(context: Context, @LayoutRes private val layoutResource: Int, private val lookupsList: List<Lookups>):
    ArrayAdapter<Lookups>(context, layoutResource, lookupsList), Filterable {

    var mLookupList: List<Lookups>

     init {

             mLookupList = lookupsList.map {
                 return@map it
             }

     }

    override fun getCount(): Int {
        return mLookupList.count()
    }

    override fun getItem(position: Int): Lookups? {
        return mLookupList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: LookupRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.lookup_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }
    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                if(filterResults.values != null){
                    addAll(filterResults.values as List<Lookups>)
                    notifyDataSetChanged()
                }
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mLookupList
                else
                    lookupsList.filter {
                        it.lk_name!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Lookups).lk_name as CharSequence
            }
        }
    }
}