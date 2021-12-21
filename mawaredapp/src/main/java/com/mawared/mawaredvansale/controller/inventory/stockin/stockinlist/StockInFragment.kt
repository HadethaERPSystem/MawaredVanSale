package com.mawared.mawaredvansale.controller.inventory.stockin.stockinlist

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.databinding.StockInFragmentBinding
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.stock_in_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class StockInFragment : Fragment(), KodeinAware, IMainNavigator<Stockin> {

    override val kodein by kodein()

    private val factory: StockInViewModelFactory by instance()

    private lateinit var binding: StockInFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(StockInViewModel::class.java)
    }

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.stock_in_fragment, container, false)

        viewModel.setNavigator(this)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        bindUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (activity as AppCompatActivity).supportActionBar!!.title = "Stock in"
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_search -> {

            }
            R.id.addBtn -> {
                navController.navigate(R.id.action_invoicesFragment_to_addInvoiceFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemDeleteClick(baseEo: Stockin) {

    }

    override fun onItemEditClick(baseEo: Stockin) {

    }

    override fun onItemViewClick(baseEo: Stockin) {

    }

    // binding recycler view
    private fun bindUI() =  GlobalScope.launch(Dispatchers.Main) {
        val list =  viewModel.baseEoList.await()
            list.observe(viewLifecycleOwner, Observer { sl ->
            if(sl == null) return@Observer
            initRecyclerView(sl.toStockinRow())
        })
    }

    private fun initRecyclerView(items: List<StockinRow>){
        val groupAdapter = GroupAdapter<ViewHolder>().apply {
            addAll(items)
        }

        rcv_stockin.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = groupAdapter
        }
    }

    private fun List<Stockin>.toStockinRow(): List<StockinRow>{
        return this.map {
            StockinRow(it, viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}
