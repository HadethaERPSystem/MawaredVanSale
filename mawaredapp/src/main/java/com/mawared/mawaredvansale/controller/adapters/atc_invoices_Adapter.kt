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
import com.mawared.mawaredvansale.data.db.entities.md.DocRefDto
import com.mawared.mawaredvansale.databinding.AtcInvoicesRowBinding
import java.util.*

class atc_invoices_Adapter(context: Context, @LayoutRes private val layoutResource: Int, private val docs: List<DocRefDto>):
   ArrayAdapter<DocRefDto>(context, layoutResource, docs) {
    var mDocs: List<DocRefDto> = docs
    var lang: String = Locale.getDefault().toString().toLowerCase()

    override fun getCount(): Int {
        return mDocs.size
    }

    override fun getItem(position: Int): DocRefDto? {
        return mDocs.get(position)
    }

    override fun getItemId(position: Int): Long {
        return mDocs.get(position).ref_Id!!.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = (context as Activity).layoutInflater
        val binding: AtcInvoicesRowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.atc_invoices_row, parent, false)
        binding.docSale = getItem(position)
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                if(filterResults?.values != null) {
                    mDocs = filterResults?.values as List<DocRefDto>
                    notifyDataSetChanged()
                }
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint?.toString()

                val filterResult = Filter.FilterResults()
                filterResult.values = if(queryString == null || queryString.isEmpty())
                    docs
                else
                    docs.filter {
                            it.ref_no!!.contains(queryString)
                    }
                return filterResult
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                val item = resultValue as DocRefDto
                val name = item.ref_no ?: "" // if(lang == "en_us" && item.pr_description != null) item.pr_description else item.pr_description_ar
                return name
            }
        }
    }
}