package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
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
import com.mawared.mawaredvansale.databinding.AtcBatchsRowBinding
import java.util.*

class atc_batchs_Adapter(context: Context, @LayoutRes private val layoutResource: Int, private val allProducts: List<Product>):
   ArrayAdapter<Product>(context, layoutResource, allProducts) {
    var mProducts: List<Product> = allProducts
    var lang: String = Locale.getDefault().toString().toLowerCase()

    override fun getCount(): Int {
        return mProducts.size
    }

    override fun getItem(position: Int): Product? {
        return mProducts.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mProducts.get(position).pr_Id.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = (context as Activity).layoutInflater
        val binding: AtcBatchsRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.atc_batchs_row, parent, false)
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
                            it.pr_batch_no!!.contains(queryString)
                    }
                return filterResult
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                val item = resultValue as Product
                val name = item.pr_batch_no ?: "" // if(lang == "en_us" && item.pr_description != null) item.pr_description else item.pr_description_ar
                return name
            }
        }
    }
}