package com.mawared.mawaredvansale.controller.Barcode

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.mawared.mawaredvansale.R
import kotlinx.android.synthetic.main.activity_continuous.*
import java.util.*

class ContinuousActivity : AppCompatActivity() {
    private lateinit var captureManager: CaptureManager
    private var torchState: Boolean = false

    private var scanContinuousState: Boolean = false
    private lateinit var scanContinuousBG: Drawable
    lateinit var beepManager: BeepManager
    private var lastScan = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_continuous)

        title = "Continuous Scan"

        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)

        beepManager = BeepManager(this)
        beepManager.isVibrateEnabled = true

        scanContinuousBG = btnScanContinuous.background

        var callback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    val current = Date()
                    val diff = current.time - lastScan.time
                    if(diff >= 1000){
                        txtResultContinuous.text = it.text
                        lastScan = current
                        beepManager.playBeepSoundAndVibrate()

                        animateBackground()

                        val returnIntent: Intent = Intent()
                        returnIntent.putExtra("return_barcode", it.text)

                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            }
        }

        btnScanContinuous.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, InlineScanActivity::class.java)
//            startActivity(intent)
            if(!scanContinuousState){
                scanContinuousState = !scanContinuousState
                btnScanContinuous.setBackgroundColor(ContextCompat.getColor(InlineScanActivity@this, R.color.colorPrimary))
                txtResultContinuous.text = "scanning..."
                barcodeView.decodeContinuous(callback)
            } else {
                scanContinuousState = !scanContinuousState
                btnScanContinuous.background = scanContinuousBG
                barcodeView.barcodeView.stopDecoding()
            }
        })

        btnTorch.setOnClickListener {
            if(torchState){
                torchState = false
                barcodeView.setTorchOff()
            } else {
                torchState = true
                barcodeView.setTorchOn()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    private fun animateBackground(){
        val colorFrom = resources.getColor(R.color.colorAccent)
        val colorTo = resources.getColor(R.color.colorPrimary)
        val colorAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 250 // milliseconds

        colorAnimation.addUpdateListener { animator -> txtResultContinuous.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
    }
}