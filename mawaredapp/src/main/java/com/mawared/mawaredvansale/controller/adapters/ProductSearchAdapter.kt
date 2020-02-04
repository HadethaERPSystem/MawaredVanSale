package com.mawared.mawaredvansale.controller.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.databinding.ProductSearchRowBinding
import java.util.*

class ProductSearchAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val allProducts: List<Product>):
   ArrayAdapter<Product>(context, layoutResource, allProducts) {
    var mProducts: List<Product> = allProducts
    var lang: String = Locale.getDefault().toString().toLowerCase()


    override fun getCount(): Int {
        return mProducts.count()
    }

    override fun getItem(position: Int): Product? {
        return mProducts.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mProducts.get(position).pr_Id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ProductSearchRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.product_search_row, parent, false)
        binding.product = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                mProducts = filterResults?.values as List<Product>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()

                val filterResult = Filter.FilterResults()
                filterResult.values = if(queryString == null || queryString.isEmpty())
                    allProducts
                else
                    allProducts.filter {
//                        if(lang == "en_us" && it.pr_description != null)
//                            it.pr_description!!.contains(queryString)
//                        else
                            it.pr_description_ar!!.contains(queryString)
                    }
                return filterResult
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                val item = resultValue as Product
                val name = item.pr_description_ar // if(lang == "en_us" && item.pr_description != null) item.pr_description else item.pr_description_ar
                return name as CharSequence
            }
        }
    }
}