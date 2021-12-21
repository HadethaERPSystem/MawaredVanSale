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
import com.mawared.mawaredvansale.data.db.entities.md.Region
import com.mawared.mawaredvansale.databinding.PriceCategoryRowBinding
import com.mawared.mawaredvansale.databinding.RegionRowBinding
import java.util.*

class RegionAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val regions: List<Region>):
ArrayAdapter<Region>(context, layoutResource, regions){
    var mRegions : List<Region> = regions
    var lang: String = Locale.getDefault().toString().toLowerCase()

    override fun getCount(): Int {
        return mRegions.size
    }


    override fun getItem(position: Int): Region? {
        return mRegions.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mRegions.get(position).rg_id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = (context as Activity).layoutInflater
        val binding: RegionRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.region_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                mRegions = filterResults?.values as List<Region>
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()

                val filterResult = Filter.FilterResults()
                filterResult.values = if(queryString == null || queryString.isEmpty())
                    regions
                else
                    regions.filter {
                        it.rg_description_ar!!.contains(queryString)
                    }
                return filterResult
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                val item = resultValue as Region
                val name = item.rg_description_ar
                return name as CharSequence
            }
        }
    }
}