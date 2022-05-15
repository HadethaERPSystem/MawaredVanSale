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
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.databinding.ProductExpiryAtcRowBinding
import java.util.*

class Products_expiry_Adapter(context: Context, @LayoutRes private val layoutResource: Int):
   ArrayAdapter<Product>(context, layoutResource) {
    private var mdataList: List<Product> = arrayListOf()
    private var allList: List<Product> = arrayListOf()

    fun setData(dataList: List<Product>){
        mdataList = dataList.map {
            return@map it
        }
        allList = dataList.map{
            return@map it
        }
        notifyDataSetChanged()
    }
    override fun getCount(): Int {
        return mdataList.count()
    }

    override fun getItem(position: Int): Product? {
        return mdataList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = (context as Activity).layoutInflater
        val binding: ProductExpiryAtcRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.product_expiry_atc_row, parent, false)
        binding.product = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                if(filterResults.values != null){
                    addAll(filterResults.values as List<Product>)
                    notifyDataSetChanged()
                }
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase(Locale.ENGLISH)
                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    mdataList
                else
                    allList.filter {
                        it.pr_description_ar!!.toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Product).pr_description_ar as CharSequence
            }
        }
    }
}