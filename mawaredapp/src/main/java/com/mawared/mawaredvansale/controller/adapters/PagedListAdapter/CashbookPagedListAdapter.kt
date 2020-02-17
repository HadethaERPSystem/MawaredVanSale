package com.mawared.mawaredvansale.controller.adapters.PagedListAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.reports.fms.CashbookStatementViewModel
import com.mawared.mawaredvansale.data.db.entities.reports.fms.ReportRowHeader
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.databinding.CashbookItemBinding
import com.mawared.mawaredvansale.databinding.CashbookItemFooterBinding
import com.mawared.mawaredvansale.databinding.CashbookItemHeaderBinding


import com.mawared.mawaredvansale.services.repositories.NetworkState
import kotlinx.android.synthetic.main.network_state_item.view.*

class CashbookPagedListAdapter(private val viewModel: CashbookStatementViewModel, val context: Context):
    PagedListAdapter<CashbookStatement, RecyclerView.ViewHolder>(DiffCallBack()) {

    val MAIN_VIEW_TYPE = 1
    val NETWORK_VIEW_TYPE = 2
    val HEADER_VIEW_TYPE = 3
    val FOOTER_VIEW_TYPE = 4
    private var header: ReportRowHeader? = null

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //val view: View
        val inflater = LayoutInflater.from(parent.context)
        when(viewType){
            MAIN_VIEW_TYPE ->{
                val bind : CashbookItemBinding = DataBindingUtil.inflate(inflater, R.layout.cashbook_item, parent, false)
                return ItemViewHolder(bind.root, bind )
            }
            HEADER_VIEW_TYPE ->{
                val bind: CashbookItemHeaderBinding = DataBindingUtil.inflate(inflater, R.layout.cashbook_item_header, parent, false)
                return HeaderViewHolder(bind.root, bind)
            }
            FOOTER_VIEW_TYPE ->{
                val bind : CashbookItemFooterBinding = DataBindingUtil.inflate(inflater, R.layout.cashbook_item_footer, parent, false)
                return FooterViewHolder(bind.root, bind )
            }
            NETWORK_VIEW_TYPE ->{
                val view = inflater.inflate(R.layout.network_state_item, parent, false)
                return NetworkStateItemViewHolder(view)
            }
        }
        val bind : CashbookItemBinding = DataBindingUtil.inflate(inflater, R.layout.cashbook_item, parent, false)
        return ItemViewHolder(bind.root, bind )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == MAIN_VIEW_TYPE) {
            (holder as ItemViewHolder).bind(getItem(position), viewModel)
        } else if (getItemViewType(position) == HEADER_VIEW_TYPE) {
            (holder as HeaderViewHolder).bind(header, viewModel)
        //} else if (getItemViewType(position) == FOOTER_VIEW_TYPE) {

        } else {
            (holder as NetworkStateItemViewHolder).bind(networkState, context)
        }
    }

    private fun hasExtraRow(): Boolean{
        return networkState != null && networkState != NetworkState.LOADED
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if(hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        //if(isHeader(position)) return HEADER_VIEW_TYPE
        //if(isFooter(position)) return FOOTER_VIEW_TYPE
        return if(position == 0){
            HEADER_VIEW_TYPE
        }else if(hasExtraRow() && position == itemCount -1){
            FOOTER_VIEW_TYPE //NETWORK_VIEW_TYPE
        }else{
            MAIN_VIEW_TYPE
        }
    }

    fun setHeader(_header: ReportRowHeader){
        this.header = _header
    }

//    private fun isHeader(position: Int): Boolean{ return  position == 0 }
//    private fun isFooter(position: Int): Boolean{ return position == itemCount - 1 && !hasExtraRow()}

    class DiffCallBack: DiffUtil.ItemCallback<CashbookStatement>(){
        override fun areItemsTheSame(oldItem: CashbookStatement, newItem: CashbookStatement): Boolean {
            return oldItem.cu_name == newItem.cu_name
        }

        override fun areContentsTheSame(oldItem: CashbookStatement, newItem: CashbookStatement): Boolean {
            return oldItem.equals(newItem)
        }
    }

    class ItemViewHolder(view: View, private val viewBinding: CashbookItemBinding): RecyclerView.ViewHolder(view){
        fun bind(baseEo: CashbookStatement?, viewModel: CashbookStatementViewModel){
            viewBinding.entityEo = baseEo
            viewBinding.viewmodel = viewModel
        }
    }

    class HeaderViewHolder(view: View, private val viewBinding: CashbookItemHeaderBinding): RecyclerView.ViewHolder(view){
        fun bind(baseEo: ReportRowHeader?, viewModel: CashbookStatementViewModel){
            viewBinding.entityEo = baseEo
            viewBinding.viewmodel = viewModel
        }
    }

    class FooterViewHolder(view: View, private val viewBinding: CashbookItemFooterBinding): RecyclerView.ViewHolder(view){
        fun bind(baseEo: CashbookStatement?, viewModel: CashbookStatementViewModel){
            viewBinding.entityEo = baseEo
            viewBinding.viewmodel = viewModel
        }
    }

    class NetworkStateItemViewHolder(view: View): RecyclerView.ViewHolder(view){
        fun bind(networkState: NetworkState?, context: Context){
            if(networkState != null && networkState == NetworkState.LOADING){
                itemView.progress_bar_item.visibility = View.VISIBLE
            }else{
                itemView.progress_bar_item?.visibility = View.GONE
            }

            if(networkState != null && networkState == NetworkState.ERROR){
                val pack = context.packageName
                val id = context.resources.getIdentifier(networkState.msg,"string", pack)
                itemView.error_msg_item.visibility = View.VISIBLE
                itemView.error_msg_item.text = context.resources.getString(id)
            }else if(networkState != null && networkState == NetworkState.ENDOFLIST){
                val pack = context.packageName
                val id = context.resources.getIdentifier(networkState.msg,"string", pack)
                itemView.error_msg_item.visibility = View.VISIBLE
                itemView.error_msg_item.text = context.resources.getString(id)
            }else{
                itemView.error_msg_item.visibility = View.GONE
            }
        }
    }

    fun setNetworkState(networkState: NetworkState){
        val previousState: NetworkState? = this.networkState
        val hadExtraRow: Boolean = hasExtraRow()
        this.networkState = networkState
        val hasExtraRow = hasExtraRow()

        if(hadExtraRow != hasExtraRow){
            if(hadExtraRow){                                //hadExtraRow is true and hasExtraRow false
                notifyItemRemoved(super.getItemCount())     //remove the progressbar at the end
            }else{                                          //hasExtraRow is true and hadExtraRow false
                notifyItemInserted(super.getItemCount())    //add the progressbar at the end
            }
        }else if(hasExtraRow && previousState != networkState){ // hasExtraRow is true and hadExtraRow true
            notifyItemChanged(itemCount-1)
        }
    }
}