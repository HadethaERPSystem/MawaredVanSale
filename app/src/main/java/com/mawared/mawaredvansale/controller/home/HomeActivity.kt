package com.mawared.mawaredvansale.controller.home

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.auth.AuthViewModel
import com.mawared.mawaredvansale.controller.auth.AuthViewModelFactory
import com.mawared.mawaredvansale.controller.auth.LoginActivity
import com.mawared.mawaredvansale.databinding.NavHeaderBinding
import kotlinx.android.synthetic.main.activity_home.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.lang.Exception
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class HomeActivity : AppCompatActivity(), KodeinAware, NavigationView.OnNavigationItemSelectedListener{

    override val kodein by kodein()

    private val factory: HomeViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
    }

    lateinit var binding: NavHeaderBinding

    var navigationView: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale()// call loadLocale

        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)

        navigationView = findViewById(R.id.nav_view)
        navigationView!!.setNavigationItemSelectedListener (this)

        val navController = Navigation.findNavController(this, R.id.fragment)
        NavigationUI.setupWithNavController(nav_view, navController)
        NavigationUI.setupActionBarWithNavController(this, navController, drawer_layout)

        val viewHeader = nav_view.getHeaderView(0)
        binding = NavHeaderBinding.bind(viewHeader) // DataBindingUtil.setContentView(this, R.layout.nav_header)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

//        xmlParses()
//        val pullParserFactory: XmlPullParserFactory
//        try {
//            pullParserFactory = XmlPullParserFactory.newInstance()
//            val parser = pullParserFactory.newPullParser()
//            val inputStream = applicationContext.assets.open("templates/TransferTemplate.xml")
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
//            parser.setInput(inputStream, null)
//            val m = parseXml(parser)
//
//
//        }catch (e: XmlPullParserException){
//            e.printStackTrace()
//        }catch (e: IOException){
//            e.printStackTrace()
//        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseXml(parser: XmlPullParser){
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT){
            val name: String
            when(eventType){
                XmlPullParser.START_DOCUMENT -> {}
                XmlPullParser.START_TAG -> {
                    name = parser.name
                    when(name){
                        "PageSetting" ->{}
                        "Paper" -> {val paper = parser.nextText()}
                        "Width" -> {val width = parser.nextText() }
                        "Height" -> {val height = parser.nextText()}
                        "PageHeader" -> {}
                        "Title" -> {}
                        "Table" -> {}
                        "thead" -> {}
                        "tr" -> {}
                        "td" -> {}
                        "Separator" -> {}
                        "Body" -> {}
                        "GrandTotal" -> {}
                        "PageFooter" -> {}
                        "PageNumber" -> {}

                    }
                }
                XmlPullParser.END_TAG -> {
                    name = parser.name
                    when(name){
                        "PageSetting" -> {}
                        "Paper" -> {}
                        "Width" -> {}
                        "Height" -> {}
                        "PageHeader" -> {}
                        "Title" -> {}
                        "Table" -> {}
                        "thead" -> {}
                        "tr" -> {}
                        "td" -> {}
                        "Separator" -> {}
                        "Body" -> {}
                        "GrandTotal" -> {}
                        "PageFooter" -> {}
                        "PageNumber" -> {}
                    }
                }
            }
           eventType =  parser.next()
        }
    }

    fun xmlParses(){
        try {
            val inputStream = assets.open("templates/TransferTemplate.xml")
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(inputStream)
            val element = doc.documentElement
            element.normalize()

            val nList = doc.getElementsByTagName("PageHeader")
            for (i in 0 until nList.length){
                val node = nList.item(i)
                if(node.nodeType === Node.ELEMENT_NODE){
                    when(node.nodeName){

                    }
                    val element2 = node as Element
                    val m = "Paper :" + getValue("Paper", element2)
                    val w = "Width :" + getValue("Width", element2)
                    val h = "Height :" + getValue("Height", element2)

                }
            }
            val header = doc.getElementsByTagName("PageHeader")

            val footer = doc.getElementsByTagName("PageFooter")
            val body = doc.getElementsByTagName("Body")
            val grandTotal = doc.getElementsByTagName("GrandTotal")

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun getValue(tag: String, element: Element): String{
        val nodeList = element.getElementsByTagName(tag).item(0).childNodes
        val node = nodeList.item(0)
        return node.nodeValue
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            Navigation.findNavController(this, R.id.fragment),
            drawer_layout
        )
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id){
            R.id.menu_settings -> {

            }
            R.id.menu_changeLanguage -> {
                showChangeLang()
            }
            R.id.menu_logout -> {
                logout()
            }
        }
        return false
    }

    fun logOutBtn(v: View) {
        logout()
    }
    private fun logout(){
        if(App.prefs.saveUser != null){
            viewModel.deleteUser(App.prefs.saveUser!!)
        }
        App.prefs.isLoggedIn = false
        App.prefs.saveUser = null
        App.prefs.authToken = null

        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun showChangeLang(){
        val listItems = arrayOf("عربي", "English")
        val mBulider = AlertDialog.Builder(this)
        mBulider.setTitle(getString(R.string.change_language))
        mBulider.setSingleChoiceItems(listItems, -1){ dialog, which ->
            when(which){
                0 -> {
                    setLocale("ar-IQ")
                    recreate()
                }
                1 -> {
                    setLocale("en-US")
                    recreate()
                }
            }
            dialog.dismiss()
        }
        val mDialog = mBulider.create()
        mDialog.show()
    }

    private fun setLocale(lang: String){
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        App.prefs.systemLanguage = lang

    }

    private fun loadLocale(){
        val lang = Locale.getDefault().toString()
        setLocale(lang)
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}
