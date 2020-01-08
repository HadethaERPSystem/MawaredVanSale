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
import com.mawared.mawaredvansale.data.db.entities.md.Salesman
import com.mawared.mawaredvansale.databinding.AutocompleteSmRowBinding
import java.util.*

class atc_sm_Adapter(context: Context, @LayoutRes private val layoutResource: Int, private val salesmanList: List<Salesman>):
    ArrayAdapter<Salesman>(context, layoutResource, salesmanList), Filterable {

    var mSalesmanList: List<Salesman>

     init {

             mSalesmanList = salesmanList.map {
                 return@map it
             }

     }

    override fun getCount(): Int {
        return mSalesmanList.count()
    }

    override fun getItem(position: Int): Salesman? {
        return mSalesmanList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: AutocompleteSmRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.autocomplete_sm_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }
    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                if(filterResults.values != null){
                    addAll(filterResults.values as List<Salesman>)
                    notifyDataSetChanged()
                }
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mSalesmanList
                else
                    salesmanList.filter {
                        it.sm_name_ar!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Salesman).sm_name_ar as CharSequence
            }
        }
    }
}