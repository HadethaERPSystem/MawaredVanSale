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
import com.mawared.mawaredvansale.data.db.entities.md.Customer_Payment_Type
import com.mawared.mawaredvansale.databinding.AutocompleteCptRowBinding
import java.util.*

class AutoCompleteCustomerTypeAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val customerTypeList: List<Customer_Payment_Type>):
    ArrayAdapter<Customer_Payment_Type>(context, layoutResource, customerTypeList), Filterable {

    var mCustomerTypeList: List<Customer_Payment_Type>

     init {

             mCustomerTypeList = customerTypeList.map {
                 return@map it
             }

     }

    override fun getCount(): Int {
        return mCustomerTypeList.count()
    }

    override fun getItem(position: Int): Customer_Payment_Type? {
        return mCustomerTypeList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: AutocompleteCptRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.autocomplete_cpt_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                addAll(filterResults.values as List<Customer_Payment_Type>)
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mCustomerTypeList
                else
                    customerTypeList.filter {
                        it.cpt_name_ar.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Customer_Payment_Type).cpt_name_ar
            }
        }
    }
}