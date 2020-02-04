package com.mawared.mawaredvansale.controller.common

import android.content.Context
import android.text.TextUtils
import android.util.Log

import print.Print
import print.PublicFunction
import java.util.HashMap

class PublicAction {
    private var context: Context? = null
    private val CODEPAGE_KHEMR = "Khemr"

    constructor() {

    }

    constructor(con: Context) {
        context = con
    }

    fun BeforePrintAction() {
        try {
            Print.LanguageEncode = "iso8859-8"
            Print.setCodePage(0)
        } catch (e: Exception) {
            Log.e(
                "Print",
                StringBuilder("PublicAction --> BeforePrintAction ").append(e.message).toString()
            )
        }

    }

    fun BeforePrintActionText() {
        try {
            val PFun = PublicFunction(context)
            if (!TextUtils.isEmpty(PFun.ReadSharedPreferencesData("Codepage"))) {
                val codepage =
                    PFun.ReadSharedPreferencesData("Codepage").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                val sLEncode = PFun.getLanguageEncode(codepage)
                //设置Codepage
                Print.LanguageEncode = sLEncode
                //				if (CODEPAGE_KHEMR.equals(codepage)){
                //					//开启高棉语
                //					Print.setKhmerSwitch(true);
                //					Print.setKhemrOrder();
                //				}
            }
        } catch (e: Exception) {
            Log.e(
                "Print",
                StringBuilder("PublicAction --> BeforePrintAction ").append(e.message).toString()
            )
        }

    }

    fun AfterPrintActionText() {
        try {
            //			PublicFunction PFun=new PublicFunction(context);
            //			if (!TextUtils.isEmpty(PFun.ReadSharedPreferencesData("Codepage"))){
            //				String codepage = PFun.ReadSharedPreferencesData("Codepage").split(",")[1];
            //				if (CODEPAGE_KHEMR.equals(codepage)){
            //					//关闭高棉语
            //					Print.setKhemrEnd();
            //				}
            //			}
        } catch (e: Exception) {
            Log.e(
                "Print",
                StringBuilder("PublicAction --> AfterPrintAction ").append(e.message).toString()
            )
        }

    }

    fun AfterPrintAction() {

    }

    fun LanguageEncode(): String {
        try {
            val PFun = PublicFunction(context)
            val sLanguage =
                PFun.ReadSharedPreferencesData("Codepage").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            var sLEncode = "gb2312"
            var intLanguageNum = 0

            sLEncode = PFun.getLanguageEncode(sLanguage) as String
            intLanguageNum = PFun.getCodePageIndex(sLanguage)

            Print.LanguageEncode = sLEncode
            //			Print.SetCharacterSet((byte)intLanguageNum);

            return sLEncode
        } catch (e: Exception) {
            Log.e(
                "HPRTSDKSample",
                StringBuilder("PublicAction --> AfterPrintAction ").append(e.message).toString()
            )
            return ""
        }

    }
}

fun PublicFunction.getLanguageEncode(sCodepage: String): String? {
        val var10000 = HashMap<String,String>()
        var10000.put("Default", "gb2312")
        var10000.put("Chinese Simplified", "gb2312")
        var10000.put("Chinese Traditional", "big5")
        var10000.put("PC437(USA:Standard Europe)", "iso8859-1")
        var10000.put("KataKana", "Shift_JIS")
        var10000.put("PC850(Multilingual)", "iso8859-3")
        var10000.put("PC860(Portuguese)", "iso8859-6")
        var10000.put("PC863(Canadian-French)", "iso8859-1")
        var10000.put("PC865(Nordic)", "iso8859-1")
        var10000.put("PC857(Turkish)", "IBM857")
        var10000.put("PC737(Greek)", "iso8859-7")
        var10000.put("ISO8859-7(Greek)", "iso8859-7")
        var10000.put("WCP1252", "iso8859-1")
        var10000.put("PC866(Cyrillic #2)", "iso8859-5")
        var10000.put("PC852(Latin 2)", "iso8859-2")
        var10000.put("PC858(Euro)", "iso8859-15")
        var10000.put("KU42", "ISO8859-11")
        var10000.put("TIS11(Thai)", "ISO8859-11")
        var10000.put("TIS18(Thai)", "windows-874")
        var10000.put("PC720", "iso8859-6")
        var10000.put("WPC775", "iso8859-1")
        var10000.put("PC855(Cyrillic)", "iso8859-5")
        var10000.put("PC862(Hebrew)", "iso8859-8")
        var10000.put("PC864(Arabic)", "iso8859-6")
        var10000.put("ISO8859-2(Latin2)", "iso8859-2")
        var10000.put("ISO8859-15(Latin9)", "iso8859-15")
        var10000.put("WPC1250", "iso8859-2")
        var10000.put("WPC1251(Cyrillic)", "iso8859-5")
        var10000.put("WPC1253", "iso8859-7")
        var10000.put("WPC1254", "iso8859-3")
        var10000.put("WPC1255", "iso8859-8")
        var10000.put("WPC1256", "Windows-1256")
        var10000.put("WPC1257", "iso8859-1")
        var10000.put("WPC1258", "bg2312")
        var10000.put("MIK(Cyrillic/Bulgarian)", "iso8859-15")
        var10000.put("CP755(East Europe,Latvian 2)", "iso8859-5")
        var10000.put("Iran", "iso8859-6")
        var10000.put("Iran II", "iso8859-6")
        var10000.put("Latvian", "iso8859-4")
        var10000.put("ISO-8859-1(West Europe)", "iso8859-1")
        var10000.put("ISO-8859-3(Latin 3)", "iso8859-3")
        var10000.put("ISO-8859-4(Baltic)", "iso8859-4")
        var10000.put("ISO-8859-5(Cyrillic)", "iso8859-5")
        var10000.put("ISO-8859-6(Arabic)", "iso8859-6")
        var10000.put("ISO-8859-8(Hebrew)", "iso8859-8")
        var10000.put("ISO-8859-9(Turkish)", "iso8859-9")
        var10000.put("PC856", "iso8859-8")
        var10000.put("ABICOIM", "iso8859-15")
        var10000.put("Khemr", "UnicodeBigUnmarked")
        return var10000.get(sCodepage)
}

