package com.mawared.mawaredvansale.controller.adapters

import android.app.Activity
import android.content.Context
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

class CustomerAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val allCustomers: List<Customer>):
    ArrayAdapter<Customer>(context, layoutResource, allCustomers), Filterable {

    private var mCustomers: List<Customer> = allCustomers

    override fun getCount(): Int {
        return mCustomers.size
    }

    override fun getItem(position: Int): Customer? {
        return mCustomers.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mCustomers.get(position).cu_Id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = (context as Activity).layoutInflater
        val binding: AutocompleteCustomerRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.autocomplete_customer_row, parent, false)
        binding.customer = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                mCustomers = filterResults?.values as List<Customer>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()

                val filterResult = Filter.FilterResults()
                filterResult.values = if(queryString.isNullOrEmpty())
                    allCustomers
                else
                    allCustomers.filter {
                        it.cu_name_ar!!.contains(queryString)
                    }
                return filterResult
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Customer).cu_name_ar as CharSequence
            }
        }
    }
}
