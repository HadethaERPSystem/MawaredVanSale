/*
 * Copyright 2018 Jeremy Patrick Pacabis
 * Copyright 2017-2018 Evren Coşkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mawared.tableview.adapter.recyclerview

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mawared.tableview.adapter.ITableAdapter
import com.mawared.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder
import com.mawared.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.mawared.tableview.feature.sort.ColumnSortHelper

/**
 * Created by jeremypacabis on February 23, 2018.
 * @author Jeremy Patrick Pacabis <jeremy@ingenuity.ph>
 * com.mawared.tableview.adapter.recyclerview <android-tableview-kotlin>
 */
class ColumnHeaderRecyclerViewAdapter(
        context: Context,
        items: List<Any>?,
        private val tableAdapter: ITableAdapter
) : AbstractRecyclerViewAdapter(context, items) {

    var columnSortHelper: ColumnSortHelper? = null
        get() {
            if (field == null) {
                field = ColumnSortHelper(tableAdapter.tableView!!.columnHeaderLayoutManager)
            }

            return field
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return tableAdapter.onCreateColumnHeaderViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as AbstractViewHolder
        val value = getItem(position)
        tableAdapter.onBindColumnHeaderViewHolder(viewHolder, value!!, position)
    }

    override fun getItemViewType(position: Int): Int {
        return tableAdapter.getColumnHeaderItemViewType(position)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val viewHolder = holder as AbstractViewHolder
        val selectionState = tableAdapter.tableView!!.selectionHandler
                .getColumnSelectionState(viewHolder.adapterPosition)

        // Update selection colors
        if (!tableAdapter.tableView!!.ignoreSelectionColors) {
            tableAdapter.tableView!!.selectionHandler
                    .changeColumnBackgroundColorBySelectionStatus(viewHolder, selectionState)
        }

        // Update selection status
        viewHolder.setSelected(selectionState)

        // Determine if TableView is sorted or not
        if (tableAdapter.tableView!!.isSorted) {
            if (viewHolder is AbstractSorterViewHolder) {
                val state = columnSortHelper!!.getSortingStatus(viewHolder.getAdapterPosition())
                viewHolder.onSortingStatusChanged(state)
            }
        }
    }
}
