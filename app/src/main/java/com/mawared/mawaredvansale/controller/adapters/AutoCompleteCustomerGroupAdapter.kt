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
import com.mawared.mawaredvansale.data.db.entities.md.Customer_Group
import com.mawared.mawaredvansale.databinding.AutocompleteCustomerGroupRowBinding
import java.util.*

class AutoCompleteCustomerGroupAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val groupList: List<Customer_Group>):
    ArrayAdapter<Customer_Group>(context, layoutResource, groupList), Filterable {

    var mGroupList: List<Customer_Group>

     init {

             mGroupList = groupList.map {
                 return@map it
             }

     }

    override fun getCount(): Int {
        return mGroupList.count()
    }

    override fun getItem(position: Int): Customer_Group? {
        return mGroupList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: AutocompleteCustomerGroupRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.autocomplete_customer_group_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                addAll(filterResults.values as List<Customer_Group>)
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mGroupList
                else
                    groupList.filter {
                        it.cg_description_ar!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Customer_Group).cg_description_ar as CharSequence
            }
        }
    }
}