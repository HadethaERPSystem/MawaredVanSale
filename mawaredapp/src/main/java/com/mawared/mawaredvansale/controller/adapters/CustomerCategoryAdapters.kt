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
import com.mawared.mawaredvansale.data.db.entities.md.Customer_Category
import com.mawared.mawaredvansale.databinding.CustomerCatRowBinding

class CustomerCategoryAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val allCategories: List<Customer_Category>):
    ArrayAdapter<Customer_Category>(context, layoutResource, allCategories), Filterable {

    private var mCategories: List<Customer_Category> = allCategories

    override fun getCount(): Int {
        return mCategories.size
    }

    override fun getItem(position: Int): Customer_Category? {
        return mCategories.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mCategories.get(position).cat_Id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = (context as Activity).layoutInflater
        val binding: CustomerCatRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.customer_cat_row, parent, false)
        binding.baseEo = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                mCategories = filterResults?.values as List<Customer_Category>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()

                val filterResult = Filter.FilterResults()
                filterResult.values = if(queryString == null || queryString.isEmpty())
                    allCategories
                else
                    allCategories.filter {
                        it.cat_description_ar!!.contains(queryString)
                    }
                return filterResult
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Customer_Category).cat_description_ar as CharSequence
            }
        }
    }
}
