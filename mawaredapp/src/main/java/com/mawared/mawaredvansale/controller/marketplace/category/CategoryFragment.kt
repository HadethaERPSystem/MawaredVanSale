package com.mawared.mawaredvansale.controller.marketplace.category

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.CategoryAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.md.Product_Brand
import com.mawared.mawaredvansale.data.db.entities.md.Product_Category
import com.mawared.mawaredvansale.databinding.CategoryFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.category_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CategoryFragment : ScopedFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory : CategoryViewModelFactory by instance()
    private lateinit var binding : CategoryFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(CategoryViewModel::class.java)
    }

    private val layoutId = R.layout.category_fragment

    private var adapter = CategoryAdapter(R.layout.item_rv_main_category) { c ->
        val action = CategoryFragmentDirections.actionCategoryFragmentToItemsFragment()
        action.brandId = 0
        action.brandName = ""
        action.categoryId = c.pg_Id
        action.categoryName = c.pg_description_ar ?: ""
        navController.navigate(action)
    }

    private lateinit var navController: NavController

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate( inflater, layoutId, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this


        //bindUI()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                viewModel.term = p0
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "")
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.term = p0
                adapter.setList(null, 0)
                loadList(viewModel.term ?: "")
                return false
            }
        })

        (requireActivity() as AppCompatActivity).supportActionBar!!.subtitle = getString(R.string.layout_categories_title)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var cols = 2
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 3
        }
        @Suppress("UNCHECKED_CAST")
        rv_category.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rv_category.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")
        navController = Navigation.findNavController(view)
    }

//    private fun bindUI()= GlobalScope.launch(Dispatchers.Main){
//        try {
//
//            viewModel.categoryList.observe(viewLifecycleOwner, Observer {
//                if(it != null)
//                    adapter.setList(it)
//            })
//            viewModel._term.value = null
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//    }

    private fun loadList(term : String){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            viewModel.loadData(list, term,adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Product_Category>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
    }
}