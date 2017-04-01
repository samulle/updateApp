package com.samulle.plugin.update.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;
import android.util.Log;


import com.samulle.plugin.update.util.SPUtils;

import java.io.File;

/**
 * 运用DownloadManager实现下载 并在通知栏会显示
 */

public class DownloadService extends Service {

    private static final String DOWNLOAD_FOLDER_NAME = "APP";
    private static final String DOWNLOAD_FILE_NAME = "newApp.apk";

    private DownloadManager downloadManager;

    private String downloadUrl;

    private long downloadId;
    private DownloadFinish downloadFinish;

    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("TAG", "onCreate() 启动服务");
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //下载之前先移除上一个 不然会导致 多次下载不成功问题
        long id = (Long) SPUtils.get(DownloadService.this, SPUtils.KEY, (long) 0);
        if (id != 0) {
            downloadManager.remove(id);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        downloadUrl = intent.getStringExtra("downloadUrl");
        initData();
        downloadFinish = new DownloadFinish();
        //动态注册广播接收器
        registerReceiver(downloadFinish, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return super.onStartCommand(intent, flags, startId);
    }


    private void initData() {
        Log.e("TAG", "initData() 执行了~");
        //判断文件目录是否存在，不存在就创建
        File folder = new File(DOWNLOAD_FOLDER_NAME);
        if(!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        startDownload();
    }
    /**
     * 内部类
     * 下载完成的广播
     */
    class DownloadFinish extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TAG", "DownloadFinish 广播接受完毕");

            //下载完成的id
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
            //如果完成id与下载的id一致表示下载完成
            if(downloadId == completeDownloadId) {
                Log.e("TAG", "DownloadFinish downloadId == completeDownloadId");
                //安装apk
                String apkFilePath = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath())
                                        .append(File.separator)
                                        .append(DOWNLOAD_FOLDER_NAME)
                                        .append(File.separator)
                                        .append(DOWNLOAD_FILE_NAME)
                                        .toString();
                Log.e("apkFilePath",apkFilePath);
                install(context, apkFilePath);
            }
        }
    }

    /**
     * 执行下载
     */
    private void startDownload() {
        //设置现在的url
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME,DOWNLOAD_FILE_NAME);
        //设置样式 貌似必须用 getString() 如果不用 下载完毕后会显示 下载的路径
        //request.setTitle(getString(R.string.download_notification_title));
        request.setTitle("程序更行");
        //描述
        request.setDescription("");

        Log.e("TAG", "正在下载时显示");
        /**
         * 在下载过程中通知栏会一直显示该下载的Notification，在下载完成后该Notification会继续显示，
         * 直到用户点击该Notification或者消除该Notification。
         */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //是否显示下载用户接口
        request.setVisibleInDownloadsUi(false);
        //设置此Type不然点击通知栏无法安装
        request.setMimeType("application/vnd.android.package-archive");

        downloadId = downloadManager.enqueue(request);
        SPUtils.put(this, SPUtils.KEY, downloadId);
    }

    /**
     * 安装apk
     * @param context
     * @param filePath
     */
    private void install(Context context, String filePath) {
        Log.e("TAG", "install() 安装");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Log.e("===========filePath",filePath);
        File file = new File(filePath);
        if(file != null && file.length()>0 && file.exists() && file.isFile()) {

            //判断是否是AndroidN以及更高的版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName()+".provider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.parse("file://" + filePath),"application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        }

        //停止服务
        stopSelf();
    }

    /**
     * 获取apk信息
     * @param context
     * @param path
     * @return
     */
    private static PackageInfo getApkInfo(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if(null != info) {
            return info;
        }
        return null;
    }

    /**
     * 比较两个apk的版本信息
     * @param apkInfo
     * @param context
     * @return
     */
    private static boolean compare(PackageInfo apkInfo, Context context) {
        if(null == apkInfo) {
            return false;
        }
        String localPackgeName = context.getPackageName();
        if(localPackgeName.equals(apkInfo.packageName)) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(localPackgeName,0);
                //比较当前apk和下载的apk版本号
                if(apkInfo.versionCode > packageInfo.versionCode) {
                    //如果下载的APK版本号大于当前安装的APK版本号，返回true
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * 是否存在sd卡
     * @return
     */
    private static boolean hasSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public void onDestroy() {
        Log.e("TAG", "onDestroy()");
        super.onDestroy();
        if(downloadFinish != null){
            unregisterReceiver(downloadFinish);
        }
    }
}
