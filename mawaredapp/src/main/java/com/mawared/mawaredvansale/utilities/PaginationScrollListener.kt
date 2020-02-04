package com.mawared.mawaredvansale.utilities

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by delaroy on 12/5/17.
 */
abstract class PaginationScrollListener (layoutManager: LinearLayoutManager):RecyclerView.OnScrollListener() {
    var layoutManager:LinearLayoutManager

    init{
        this.layoutManager = layoutManager
    }

    override fun onScrolled(recyclerView:RecyclerView, dx:Int, dy:Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.getChildCount()
        val totalItemCount = layoutManager.getItemCount()
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading() && !isLastPage())
        {
            if (((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= getTotalPageCount()))
            {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
    abstract fun getTotalPageCount():Int
    abstract fun isLastPage():Boolean
    abstract fun isLoading():Boolean
}