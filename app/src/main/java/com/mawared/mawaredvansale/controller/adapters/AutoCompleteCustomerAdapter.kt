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
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.AutocompleteCustomerRowBinding
import java.util.*

class AutoCompleteCustomerAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val customerList: List<Customer>):
    ArrayAdapter<Customer>(context, layoutResource, customerList), Filterable {

    var mCustomerList: List<Customer>

     init {

             mCustomerList = customerList.map {
                 return@map it
             }

     }

    override fun getCount(): Int {
        return mCustomerList.count()
    }

    override fun getItem(position: Int): Customer? {
        return mCustomerList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: AutocompleteCustomerRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.autocomplete_customer_row, parent, false)
        binding.customer = getItem(position)
        return binding.root
    }
    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                if(filterResults.values != null){
                    addAll(filterResults.values as List<Customer>)
                    notifyDataSetChanged()
                }
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mCustomerList
                else
                    customerList.filter {
                        it.cu_name!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Customer).cu_name_ar as CharSequence
            }
        }
    }
}