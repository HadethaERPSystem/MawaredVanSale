package com.mawared.mawaredvansale.controller.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.md.PriceCategory
import com.mawared.mawaredvansale.databinding.PriceCategoryRowBinding
import java.util.*

class PriceCategoryAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val allPricesCategory: List<PriceCategory>):
    ArrayAdapter<PriceCategory>(context, layoutResource, allPricesCategory) {
    var mPricesCategory: List<PriceCategory> = allPricesCategory
    var lang: String = Locale.getDefault().toString().toLowerCase()

    override fun getCount(): Int {
        return mPricesCategory.size
    }

    override fun getItem(position: Int): PriceCategory? {
        return mPricesCategory.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mPricesCategory.get(position).prc_Id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = (context as Activity).layoutInflater
        val binding: PriceCategoryRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.price_category_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                mPricesCategory = filterResults?.values as List<PriceCategory>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()

                val filterResult = Filter.FilterResults()
                filterResult.values = if(queryString == null || queryString.isEmpty())
                    allPricesCategory
                else
                    allPricesCategory.filter {
                        it.prc_name!!.contains(queryString)
                    }
                return filterResult
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                val item = resultValue as PriceCategory
                val name = item.prc_name
                return name as CharSequence
            }
        }
    }
}