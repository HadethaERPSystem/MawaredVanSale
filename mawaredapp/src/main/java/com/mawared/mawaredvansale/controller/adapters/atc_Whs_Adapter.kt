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
import com.mawared.mawaredvansale.data.db.entities.md.Warehouse
import com.mawared.mawaredvansale.databinding.AutocompleteWarehouseRowBinding
import java.util.*
import kotlin.collections.ArrayList

class atc_Whs_Adapter(context: Context, @LayoutRes private val layoutResource: Int, private val baseEoList: List<Warehouse>):
    ArrayAdapter<Warehouse>(context, layoutResource, baseEoList), Filterable {

    var warehouses: ArrayList<Warehouse>
    var tempWarehouse: ArrayList<Warehouse>
    var suggestions: ArrayList<Warehouse>

    init {
        warehouses = ArrayList<Warehouse>(baseEoList)
        tempWarehouse = ArrayList<Warehouse>(baseEoList)
        suggestions = ArrayList<Warehouse>(baseEoList)
    }

    override fun getCount(): Int {
        return warehouses.count()
    }

    override fun getItem(position: Int): Warehouse? {
        return warehouses.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: AutocompleteWarehouseRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.autocomplete_warehouse_row, parent, false)
        binding.entityEo = getItem(position)
        return binding.root
    }

    override fun getFilter(): Filter {
        return myFilter
    }

    val myFilter = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if(constraint != null){
                suggestions.clear()
                for (w in tempWarehouse){
                    if(w.wr_description!!.toLowerCase(Locale.ENGLISH).contains(constraint.toString().toLowerCase(Locale.ENGLISH))){
                        suggestions.add(w)
                    }
                }
                val filterResults : FilterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                return filterResults
            }else{
                return FilterResults()
            }
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            val wr = resultValue as Warehouse
            return wr.wr_description.toString()
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val whs : ArrayList<Warehouse> = results?.values as ArrayList<Warehouse>
            if(results.count > 0){
                clear()
                for (w in whs){
                    add(w)
                    notifyDataSetChanged()
                }
            }else{
                clear()
                notifyDataSetChanged()
            }
        }
    }
}