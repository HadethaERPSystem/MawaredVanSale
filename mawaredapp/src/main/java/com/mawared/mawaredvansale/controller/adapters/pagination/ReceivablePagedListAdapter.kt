package com.mawared.mawaredvansale.controller.adapters.pagination

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.fms.receivables.receivablelist.ReceivableViewModel
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.databinding.ReceivableRowBinding
import com.mawared.mawaredvansale.services.repositories.NetworkState
import kotlinx.android.synthetic.main.network_state_item.view.*

class ReceivablePagedListAdapter(private val viewModel: ReceivableViewModel, val context: Context):
    PagedListAdapter<Receivable, RecyclerView.ViewHolder>(ReceivableDiffCallback()) {

    val MAIN_VIEW_TYPE = 1
    val NETWORK_VIEW_TYPE = 2

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //val view: View
        val inflater = LayoutInflater.from(parent.context)

        if(viewType == MAIN_VIEW_TYPE){
            val bind : ReceivableRowBinding = DataBindingUtil.inflate(inflater, R.layout.receivable_row, parent, false)
            return ItemViewHolder(bind.root, bind)
        }else {
            val view = inflater.inflate(R.layout.network_state_item, parent, false)
            return NetworkStateItemViewHolder(
                view
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position) == MAIN_VIEW_TYPE){
            (holder as ItemViewHolder).bind(getItem(position), viewModel)
        }else{
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
        return if(hasExtraRow() && position == itemCount -1){
            NETWORK_VIEW_TYPE
        }else{
            MAIN_VIEW_TYPE
        }
    }

    class ReceivableDiffCallback: DiffUtil.ItemCallback<Receivable>(){
        override fun areItemsTheSame(oldItem: Receivable, newItem: Receivable): Boolean {
            return oldItem.rcv_Id == newItem.rcv_Id
        }

        override fun areContentsTheSame(oldItem: Receivable, newItem: Receivable): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(view: View, private val viewBinding: ReceivableRowBinding): RecyclerView.ViewHolder(view){
        fun bind(baseEo: Receivable?, viewModel: ReceivableViewModel){
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

            if(networkState != null){// && networkState == NetworkState.ERROR){
                val pack = context.packageName
                val id = context.resources.getIdentifier(networkState.msg,"string", pack)
                itemView.error_msg_item.visibility = View.VISIBLE
                itemView.error_msg_item.text = context.resources.getString(id)

//            }else if(networkState != null && networkState == NetworkState.ENDOFLIST){
//                val pack = context.packageName
//                val id = context.resources.getIdentifier(networkState.msg,"string", pack)
//                itemView.error_msg_item.visibility = View.VISIBLE
//                itemView.error_msg_item.text = context.resources.getString(id)
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