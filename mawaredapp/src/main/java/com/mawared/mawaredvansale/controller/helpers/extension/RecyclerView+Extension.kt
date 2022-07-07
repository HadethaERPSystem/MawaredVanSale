package com.mawared.mawaredvansale.controller.helpers.extension

import android.content.Context
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mawared.mawaredvansale.controller.base.BaseAdapter

fun RecyclerView.setupGrid(context: Context?, adapter: BaseAdapter<Any>, spanCount: Int = 2) {
    setHasFixedSize(false)

    val count = if (adapter.itemCount == 1) 1 else spanCount
    layoutManager = GridLayoutManager(context, count)
    this.adapter = adapter
}

fun RecyclerView.setup(context: Context?, adapter: BaseAdapter<Any>, isInvert: Boolean = false) {
    this.adapter = adapter
    setHasFixedSize(false)

    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, isInvert)
    val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
    addItemDecoration(decoration)
}

fun RecyclerView.setupHorz(context: Context?, adapter: BaseAdapter<Any>, isInvert: Boolean = false){
    this.adapter = adapter
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    val decoration = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
    addItemDecoration(decoration)
}

fun RecyclerView.setLoadMoreFunction(reverse: Boolean = false, loadMore: () -> Unit) {
    addOnScrollListener(object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val pastVisiblesItems = lm.findFirstVisibleItemPosition()
            if ((dy > 0 && !reverse)) {
                val visibleItemCount = lm.childCount
                val totalItemCount = lm.itemCount
                if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                    loadMore()
                }
            }
            if ((dy < 0 && reverse)) {
                val visibleItemCount = lm.childCount
                val totalItemCount = lm.itemCount
                if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                    loadMore()
                }
            }
        }
    })
}