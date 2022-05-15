package com.mawared.mawaredvansale.controller.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

open class BaseAdapter<T>(private val clickFunc: ((Int) -> Unit)?, private val layoutId: Int):
    RecyclerView.Adapter<BaseAdapter.ViewHolder>() {

    class ViewHolder(var view: View): RecyclerView.ViewHolder(view)

    protected var items = listOf<T>()
    protected var parent: ViewGroup? = null
    protected var extraParameter: String? = null
    var pageCount = 0

    fun setList(list: List<T>?, count: Int) {
        setList(list)
        pageCount = count
    }

    open fun setList(list: List<T>?) {
        this.items = list ?: listOf()
        notifyDataSetChanged()
    }

    fun setExtra(extra: String){
        extraParameter = extra
    }

    fun getItemByPos(index: Int): T? {
        if ((index > items.size - 1) || (index < 0))
            return null
        return items[index]
    }

    fun getList() = items

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        this.parent = parent
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    protected open fun loadImage(url: String?, placeholder: Int, image: ImageView) {
        if (url == null) {
            if (placeholder == -1) image.setImageBitmap(null)
            else image.setImageResource(placeholder)
        } else {
            Glide.with(image.context)
                .asDrawable()
                .load(url)
                .into(image)
        }
    }

    protected fun setClickListener(view: View?, id: Int) {
        view?.tag = id
        view?.setOnClickListener { clickFunc?.invoke(it.tag as Int) }
    }

    companion object {
        const val pageSize = 25
    }

}

// Service Interface

interface ServicesInterface<T>{
    fun showProducts(entity: T)
}