package com.mawared.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author feicien (ithcheng@gmail.com)
 * @since 2016-07-05 19:21
 */
class CheckUpdateTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog dialog;
    private final Context mContext;
    private final int mType;
    private final boolean mShowProgressDialog;
    private static final String url = Constants.UPDATE_URL;

    CheckUpdateTask(Context context, int type, boolean showProgressDialog) {

        this.mContext = context;
        this.mType = type;
        this.mShowProgressDialog = showProgressDialog;

    }


    protected void onPreExecute() {
        if (mShowProgressDialog) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage(mContext.getString(R.string.android_auto_update_dialog_checking));
            dialog.show();
        }
    }


    @Override
    protected void onPostExecute(String result) {

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        }
    }

    private void parseJson(String result) {
        try {
            String msg1 =  mContext.getString(R.string.android_msg_version);
            JSONObject obj = new JSONObject(result);
            //String updateMessage = obj.getString(Constants.APK_UPDATE_CONTENT);

            String apkUrl = obj.getString(Constants.APK_DOWNLOAD_URL);
            int apkCode = obj.getInt(Constants.APK_VERSION_CODE);

            int versionCode = AppUtils.getVersionCode(mContext);
            String upmsg = String.format(msg1, versionCode, apkCode);// String.format(msg1.toString(), versionCode) + "\n" + String.format(msg2.toString(), apkCode) + "\n" + msg3;

            if (apkCode > versionCode) {
                if (mType == Constants.TYPE_NOTIFICATION) {
                    new NotificationHelper(mContext).showNotification(upmsg, apkUrl);
                } else if (mType == Constants.TYPE_DIALOG) {
                    showDialog(mContext, upmsg, apkUrl);
                }
            } else if (mShowProgressDialog) {
                Toast.makeText(mContext, mContext.getString(R.string.android_auto_update_toast_no_new_update), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e(Constants.TAG, "parse json error");
        }
    }


    /**
     * Show dialog
     */
    private void showDialog(Context context, String content, String apkUrl) {
        UpdateDialog.show(context, content, apkUrl);
    }


    @Override
    protected String doInBackground(Void... args) {
        return HttpUtils.get(url);
    }
}
