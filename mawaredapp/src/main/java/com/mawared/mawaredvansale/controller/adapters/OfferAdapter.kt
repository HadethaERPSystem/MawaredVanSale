package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.strike
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.md.Product_Price_List
import com.mawared.mawaredvansale.data.db.entities.md.UnitConvertion
import com.mawared.mawaredvansale.utilities.URL_GET_IMAGE
import kotlinx.android.synthetic.main.item_rv_product.view.*
import java.io.FileNotFoundException
class OfferAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Product) -> Unit, private val clickFunc2: (Product) -> Unit, private val clickFunc3: (Product, Success: (UnitConvertion?, Product_Price_List?) -> Unit) -> Unit) : BaseAdapter<Product>(null, layoutResource) {

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]
        var isOnTextChanged = false
        with(holder.itemView) {
            try {
                productName.text = item.pr_description_ar

                val options: RequestOptions = RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.imagenotfound)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .dontTransform()

                Glide.with(this)
                    .load(URL_GET_IMAGE + "/Products/" + (item.pr_image_name ?: "noimage"))
                    .apply(options)
                    .into(productAvatar)

                old_price.visibility = View.GONE
                productDisc.visibility = View.GONE
                promValue.visibility = View.GONE
                productQty.text = "${item.pr_SUoMQty?.toFormatNumber()} ${item.pr_SalUnitMsr ?: ""}"
                if (item.pr_price_AfD != null && item.pr_price_AfD != 0.0) {
                    new_price.text =
                        "${item.pr_price_AfD.toString()}  ${App.prefs.saveUser!!.ss_cr_code!!}"
                    old_price.visibility = View.VISIBLE
                    old_price.strike = true
                    old_price.text =
                        "${item.pr_unit_price.toString()}  ${App.prefs.saveUser!!.ss_cr_code!!}"
                } else {
                    new_price.text =
                        "${item.pr_unit_price.toString()}  ${App.prefs.saveUser!!.ss_cr_code!!}"
                }

                if (!item.pr_dis_type.isNullOrEmpty()) {
                    var prefix = "%"
                    if (item.pr_dis_type == "F") prefix = App.prefs.saveUser!!.ss_cr_code!!

                    productDisc.text = "${item.pr_dis_value} ${prefix}"
                    productDisc.visibility = View.VISIBLE
                }

                if (item.prom_qty != null && item.prom_qty != 0.0) {
                    promValue.text = "${item.prom_qty} + ${item.prom_ex_qty}"
                    promValue.visibility = View.VISIBLE
                }

                if (extraParameter == "Y") {
                    addBtn.visibility = View.GONE
                    addQty.visibility = View.GONE
                    minusBtn.visibility = View.GONE
                    plusBtn.visibility = View.GONE
                } else {
                    addBtn.visibility = View.VISIBLE
                    addQty.visibility = View.VISIBLE
                    minusBtn.visibility = View.VISIBLE
                    plusBtn.visibility = View.VISIBLE
                }

                this.tag = item
                this.setOnClickListener {
                    val data = it.tag as Product
                    clickFunc1(data)
                }

                if (extraParameter == "N") {
                    productQty.tag = item
                    productQty.setOnClickListener { v ->
                        val data = v.tag as Product
                        clickFunc3(data) { u, price ->
                            if (u != null && data.pr_SUoMEntry != u.uom) {
                                val pr_SUoMQty = data.pr_qty!! / u.qty!!
                                productQty.setText("${pr_SUoMQty.toFormatNumber()} ${u.uom_code ?: ""}")

                                var unit_price: Double = 0.0
                                if (price != null) {
                                    unit_price = price.pl_unitPirce!!
                                } else {
                                    if (data.pr_NumInSale!! > u.qty2!!) {
                                        unit_price = data.pr_unit_price!! / data.pr_NumInSale!!
                                    } else {
                                        unit_price = data.pr_unit_price!! * u.qty2!!
                                    }
                                }
                                items[position].pr_SalUnitMsr = u.uom_code
                                items[position].pr_NumInSale = u.qty
                                items[position].pr_unit_price = unit_price
                                items[position].pr_SUoMEntry = u.uom

                                if (item.pr_dis_type != null) {
                                    var price_AfD: Double = 0.0
                                    var prefix = "%"
                                    if (item.pr_dis_type == "F") prefix =
                                        App.prefs.saveUser!!.ss_cr_code!!
                                    if (prefix == "%") {
                                        price_AfD =
                                            items[position].pr_unit_price!! * (1 - (items[position].pr_dis_value!! / 100))

                                    } else {
                                        price_AfD =
                                            items[position].pr_unit_price!! - items[position].pr_dis_value!!
                                    }
                                    items[position].pr_price_AfD = price_AfD

                                    new_price.setText("${price_AfD}  ${App.prefs.saveUser!!.ss_cr_code!!}")
                                    //old_price.visibility = View.VISIBLE
                                    //old_price.strike = true
                                    old_price.setText("${item.pr_unit_price.toString()}  ${App.prefs.saveUser!!.ss_cr_code!!}")
                                } else {
                                    new_price.setText("${item.pr_unit_price.toString()}  ${App.prefs.saveUser!!.ss_cr_code!!}")
                                }


                            }
                        }
                    }
                }


                item.addQty =
                    if (!addQty.text.isNullOrEmpty()) addQty.text!!.toString().toDouble() else 0.0
                addBtn.tag = item
                addBtn.setOnClickListener {
                    val data = it.tag as Product
                    clickFunc2(data)
                    addQty.setText("1")
                    items[position].addQty = 1.0
                }

                plusBtn.tag = item
                plusBtn.setOnClickListener {
                    val data = it.tag as Product
                    val qty = data.addQty!! + 1.0
                    items[position].addQty = qty
                    addQty.setText(qty.toString())
                }

                minusBtn.tag = item
                minusBtn.setOnClickListener {
                    val data = it.tag as Product
                    if (data.addQty!! > 1.0) {
                        val qty = data.addQty!! - 1.0
                        items[position].addQty = qty
                        addQty.setText(qty.toString())
                    }

                }

                // for add qty
                addQty.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        isOnTextChanged = true
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (isOnTextChanged) {
                            isOnTextChanged = false
                            try {
                                val qty: Double = s.toString().toDouble()
                                items[position].addQty = qty
                            } catch (e: NumberFormatException) {

                            }
                        }
                    }

                })


            } catch (e: FileNotFoundException) {
            } catch (e: Exception) {
            }
        } // End with

    }
}