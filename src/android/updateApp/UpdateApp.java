package com.samulle.plugin.update.updateApp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.samulle.plugin.update.service.DownloadService;
import com.samulle.plugin.update.util.HttpDownloadUtil;
import com.samulle.plugin.update.util.ResourceHelper;
import com.samulle.plugin.update.util.SPUtils;
import com.samulle.plugin.update.util.VersionUtils;
import com.samulle.plugin.update.version.Version;
import com.samulle.plugin.update.xml.VersionContentHandler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

/**
 * APP更新
 */

public class UpdateApp extends CordovaPlugin {
    private CallbackContext callbackContext;
    private Activity activity;
    private boolean isCheck = false;
    private String versionCode;
    private String versionMsg;
    private String downloadUrl;
    private Version version;
    private Context mContext;
    private ResourceHelper resourceHelper;

    protected final static String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public static final int SAVE_TO_ALBUM_SEC = 1;
    public static final int PERMISSION_DENIED_ERROR = 20;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        activity = cordova.getActivity();
        mContext = activity.getApplicationContext();
        resourceHelper = new ResourceHelper(mContext.getPackageName(), mContext.getResources());
        //解析xml
        String xmlUrl = args.getString(0);
        Log.e("xmlUrl",xmlUrl);
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            VersionContentHandler versionContentHandler = new VersionContentHandler();
            xmlReader.setContentHandler(versionContentHandler);
            xmlReader.parse(new InputSource(new StringReader(downloadXML(xmlUrl))));
            //获得版本信息对象
            version = versionContentHandler.getVersion();
            versionCode = version.getVersionCode();
            downloadUrl = version.getUrl();
            versionMsg = version.getMsg();

            Log.e("downloadUrl",downloadUrl);
            Log.e("versionCode",versionCode);

            //权限控制
            if(!PermissionHelper.hasPermission(this,permissions[1])){
                PermissionHelper.requestPermission(this,SAVE_TO_ALBUM_SEC,permissions[1]);
            }else{
                progressVersion();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.execute(action, args, callbackContext);
    }

    /**
     *  获得网络资源
     * @param urlStr
     * @return
     */
    private String downloadXML(String urlStr) {
        HttpDownloadUtil httpDownloadUtil = new HttpDownloadUtil();
        return httpDownloadUtil.getURLDate(urlStr);
    }

    /**
     * 说明:
     * 比较服务器版本与当前apk版本,如果低于服务器版本
     * 1.wifi下自动更新开关打开 启动服务,后台静默下载apk,下载完毕后自动弹出安装界面
     * 2.wifi下自动更新开关关闭 启动服务,下载apk,并用notification通知栏显示下载进度等,下载完毕后自动弹出安装界面
     */
    private void progressVersion() {
        //VersionUtils.getVersionCode(this)工具类里获取当前安装的apk版本号
        int version = VersionUtils.compareVersion(String.valueOf(VersionUtils.getVersionCode(activity)),
                versionCode);
        /**
         * 比较版本大小 version1为当前所安装的版本
         * version1 < version2  则  返回 -1
         * version1 > version2  则  返回 1
         * version1 == version2 则 返回  0
         */
        if(version == -1) {
            //判断 忽略的版本sp信息是否与当前版本相等 如果不相等 则显示更新的dialog
            String spVersion = (String) SPUtils.get(activity, SPUtils.APK_VERSION, "");
            if (!spVersion.equals(versionCode)) {
                //下面是自定义dialog
                View view = View.inflate(activity, resourceHelper.getLayout(resourceHelper.DOWNLOAD_LAYOUT), null);
                final Dialog dialog = new AlertDialog.Builder(activity).create();
                dialog.show();

                dialog.setContentView(view);
                TextView content = (TextView) view.findViewById(resourceHelper.getId(resourceHelper.TV_CONTENT));
                content.setText(versionMsg);
                //取消
                TextView cancel = (TextView) view.findViewById(resourceHelper.getId(resourceHelper.BTN_CANCEL));
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //当true时 保存版本信息
                        if (isCheck) {
                            SPUtils.put(activity, SPUtils.APK_VERSION, versionCode);
                        }

                        //Log.e("TAG","isCheck == " + isCheck);

                        dialog.dismiss();
                    }
                });

                //确定
                TextView Sure = (TextView) view.findViewById(resourceHelper.getId(resourceHelper.BTN_OK));
                Sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, DownloadService.class);
                        intent.putExtra("downloadUrl",downloadUrl);
                        activity.startService(intent);
                        //当true时 保存版本信息
                        if (isCheck) {
                            SPUtils.put(activity, SPUtils.APK_VERSION, versionCode);
                        }

                        dialog.dismiss();
                    }
                });


                //忽略该版本
                CheckBox checkBox = (CheckBox) view.findViewById(resourceHelper.getId(resourceHelper.CB_IGNORE));
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            isCheck = true;
                        } else {
                            isCheck = false;
                        }

                    }
                });
            }

        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch(requestCode)
        {
            case SAVE_TO_ALBUM_SEC:
                progressVersion();
                break;
        }
    }
}
