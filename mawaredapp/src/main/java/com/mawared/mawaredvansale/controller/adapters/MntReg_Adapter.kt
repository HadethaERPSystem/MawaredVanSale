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
import com.mawared.mawaredvansale.controller.mnt.entry.MntEntryViewModel
import com.mawared.mawaredvansale.data.db.entities.md.RegMnt
import com.mawared.mawaredvansale.databinding.MntregRowBinding
import java.util.*

class MntReg_Adapter(context: Context, private val viewModel: MntEntryViewModel, @LayoutRes private val layoutResource: Int):
    ArrayAdapter<RegMnt>(context, layoutResource), Filterable {

    private var mdataList: List<RegMnt> = arrayListOf()
    private var allList: List<RegMnt> = arrayListOf()

    fun setData(dataList: List<RegMnt>){
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

    override fun getItem(position: Int): RegMnt? {
        return mdataList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: MntregRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.mntreg_row, parent, false)
        binding.entityEo = getItem(position)
        binding.viewmodel = viewModel
        return binding.root
    }
    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                clear()
                if(filterResults.values != null){
                    addAll(filterResults.values as List<RegMnt>)
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
                        it.regMntNo!!.toString().toLowerCase(Locale.ENGLISH).contains(queryString)
                    }
                return filterResults
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as RegMnt).regMntNo!!.toString() as CharSequence
            }
        }
    }
}