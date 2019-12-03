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
import com.mawared.mawaredvansale.databinding.ProductAutocompleteRowBinding
import java.util.*

class AutoCompleteProductAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val productList: List<Product>):
   ArrayAdapter<Product>(context, layoutResource, productList) {
    var productListFull: List<Product>
    var lang: String = Locale.getDefault().toString().toLowerCase()
    init {
        productListFull = productList.map {
            return@map it
        }
    }

    override fun getCount(): Int {
        return productListFull.count()
    }

    override fun getItem(position: Int): Product? {
        return productListFull.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ProductAutocompleteRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.product_autocomplete_row, parent, false)
        binding.setProduct(getItem(position))
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                addAll(filterResults.values as List<Product>)
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    productListFull
                else
                    productList.filter {
                        if(lang == "en_us"){
                            it.pr_description!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                        }else{
                            it.pr_description_ar!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                        }

                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                val prod = if(lang == "en_us") (resultValue as Product).pr_description else (resultValue as Product).pr_description_ar
                return prod as CharSequence
            }
        }
    }
}