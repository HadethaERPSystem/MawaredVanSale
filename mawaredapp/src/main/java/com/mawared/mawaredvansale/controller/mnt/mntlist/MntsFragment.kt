package com.mawared.mawaredvansale.controller.mnt.mntlist

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.MntsAdapter
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.base.ScopedFragment
import com.mawared.mawaredvansale.controller.helpers.extension.setLoadMoreFunction
import com.mawared.mawaredvansale.controller.helpers.extension.setupGrid
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.databinding.MntsFragmentBinding
import com.mawared.mawaredvansale.utilities.MenuSysPrefs
import com.mawared.mawaredvansale.utilities.snackbar
import com.microsoft.appcenter.utils.HandlerUtils
import kotlinx.android.synthetic.main.mnts_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class MntsFragment : ScopedFragment(), KodeinAware, SearchView.OnQueryTextListener {

    override val kodein by kodein()
    private val factory: MntsVieModelFactory by instance()
    private val permission = MenuSysPrefs.getPermission("Mnt")
    private lateinit var binding: MntsFragmentBinding

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(MntsViewModel::class.java)
    }

    private lateinit var navController: NavController

    private var adapter = MntsAdapter(R.layout.mnts_row, permission){ e, t->
        when(t){
            "E" -> onItemEditClick(e)
            "V" -> onItemViewClick(e)
            "D" -> onItemDeleteClick(e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.mnts_fragment, container, false)

        //viewModel.msgListener = this
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        removeObservers()
        binding.pullToRefresh.setOnRefreshListener {
            loadList(viewModel.term ?: "")
            binding.pullToRefresh.isRefreshing = false
        }
        binding.btnReload.setOnClickListener { loadList(viewModel.term ?: "") }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        removeObservers()
        super.onViewCreated(view, savedInstanceState)

        var cols = 1
        val currentOrientation = resources.configuration.orientation
        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            cols = 2
        }
        @Suppress("UNCHECKED_CAST")
        rcv_mnts.setupGrid(requireContext(), adapter as BaseAdapter<Any>, cols)
        rcv_mnts.setLoadMoreFunction { loadList(viewModel.term ?: "") }
        loadList(viewModel.term ?: "")

        navController = Navigation.findNavController(view)

    }

    // enable options menu in this fragment
    override fun onResume() {
        removeObservers()
        super.onResume()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun removeObservers(){
        viewModel.baseEo.removeObservers(this)

    }

    override fun onDestroyView() {
        removeObservers()
        onDestroy()
        super.onDestroyView()
    }

    // enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    // inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val perm = permission.split("|")
        if(perm.count() > 0 && perm[0] == "1"){
            inflater.inflate(R.menu.list_menu, menu)
        }else{
            inflater.inflate(R.menu.search_menu, menu)
        }
        val search = menu?.findItem(R.id.app_bar_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_search -> {

            }
            R.id.addBtn -> {
                navController.navigate(R.id.action_mntsFragment_to_mntEntryFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.term = newText
        adapter.setList(null, 0)
        loadList(viewModel.term ?: "")
        return true
    }


    fun onItemDeleteClick(baseEo: Mnts) {
        TODO("Not yet implemented")
    }

    fun onItemEditClick(baseEo: Mnts) {
        val action = MntsFragmentDirections.actionMntsFragmentToMntEntryFragment()
        action.mntId = baseEo.mntId
        action.mode ="Edit"
        navController.navigate(action)
    }

    fun onItemViewClick(baseEo: Mnts) {
        val action = MntsFragmentDirections.actionMntsFragmentToMntEntryFragment()
        action.mntId = baseEo.mntId
        action.mode ="View"
        navController.navigate(action)
    }

    fun onStarted() {
        progress_bar.visibility = View.VISIBLE
    }

    fun onSuccess(message: String) {
        progress_bar.visibility = View.GONE
        mnts_list_cl.snackbar(message)
    }

    fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        mnts_list_cl.snackbar(message)
    }

    private fun loadList(term : String){
        val list = adapter.getList().toMutableList()
        if(adapter.pageCount <= list.size / BaseAdapter.pageSize){
            onStarted()
            viewModel.loadData(list, term,adapter.pageCount + 1){data, pageCount ->
                showResult(data!!, pageCount)
            }
        }
    }

    fun showResult(list: List<Mnts>, pageCount: Int) = HandlerUtils.runOnUiThread {
        adapter.setList(list, pageCount)
        progress_bar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJob()
    }
}