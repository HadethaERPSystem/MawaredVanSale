package com.mawared.mawaredvansale.controller.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.databinding.ActivityDownloadBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class DownloadActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()

    private val factory: DownloadViewModelFactory by instance()

    private lateinit var viewModel: DownloadViewModel

    private lateinit var binding: ActivityDownloadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        viewModel = ViewModelProviders.of(this, factory).get(DownloadViewModel::class.java)

        // initialize binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_download)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        var toolbar : Toolbar = findViewById(R.id.download_toolbar)

        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Download Service"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
