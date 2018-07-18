package com.thinkernote.ThinkerNote.base;

import android.app.Application;
import android.os.AsyncTask;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDb2;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Service.LocationService;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.http.HttpUtils;
import com.thinkernote.ThinkerNote.http.third.WeichatHttpUtils;

/**
 * sjy 0607
 */
public class TNApplication extends Application {
    private static final String TAG = "TNApplication";
    private static TNApplication application;

    public static TNApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        initialize();

        //TODO 修改
        //注册反射方法，TNDb.java使用
        TNAction.regRunner(TNActionType.DbReportError, this, "DbReportError");

        //新网络框架 log初始化
        MLog.init(true, "SJY");

        //新网络框架 初始化
        HttpUtils.getInstance().init(this, MLog.DEBUG);

        //微信初始化网络
        WeichatHttpUtils.getInstance().init(this, MLog.DEBUG);
    }

    // private methods
    //-------------------------------------------------------------------------------
    private void initialize() {
        TNSettings settings = TNSettings.getInstance();
        settings.appContext = this;
        settings.readPref();

        // db initialize
        TNDb.getInstance();
        TNDb2.getInstance();
//		//设置定制的默认皮肤， 暂指三星定制的奥运皮肤
//		TNUtilsSkin.setAssetsSkins(TNUtilsSkin.DEFAULT_SKIN);

        //地图定位
//        TNLBSService.getInstance();
        //地图定位 新版
        LocationService.getInstance();
        watchAppSwitch();

        // TODO 删 service initialize
//		TNSyncService.getInstance();
//		TNNoteLocalService.getInstance();
//		TNAttLocalService.getInstance();
//		TNCacheService.getInstance();
//		TNUserService.getInstance();
//		TNNoteService.getInstance();
//		TNAttService.getInstance();
//		TNTagService.getInstance();
//		TNCatService.getInstance();
//		TNOAuth2.getInstance();
//		TNAttDownloadService.getInstance();
        // 设置此接口后，音频文件和识别结果文件保存在/sdcard/msc/record/目录下
        //com.iflytek.resource.MscSetting.setLogSaved(true);

    }

    public void DbReportError(TNAction aAction) {
        MLog.i(TAG, "DbReportError s" + TNSettings.getInstance().topAct);
        //TNUtilsUi.showToast("DB ERROR!!");
        if (TNSettings.getInstance().topAct != null) {
            TNUtilsUi.showNotification(TNSettings.getInstance().topAct,
                    R.string.alert_DBError, true);
        }
        TNSettings.getInstance().hasDbError = true;
        TNSettings.getInstance().savePref(false);

        aAction.finished();
        MLog.i(TAG, "DbReportError e");
    }

    // 检测db错误 /TNDb.java使用
    public void DbReportError(String error) {
        MLog.i("DbReportError s", TNSettings.getInstance().topAct);
        //TNUtilsUi.showToast("DB ERROR!!");
        if (TNSettings.getInstance().topAct != null) {
            TNUtilsUi.showNotification(TNSettings.getInstance().topAct,
                    R.string.alert_DBError, true);
        }
        TNSettings.getInstance().hasDbError = true;
        TNSettings.getInstance().savePref(false);

        MLog.i("DbReportError e");
    }

    private void watchAppSwitch() {
        //一个线程，让我一直检测
        AsyncTask<Object, Object, Object> taskWatcher =
                new AsyncTask<Object, Object, Object>() {

                    @Override
                    protected Object doInBackground(Object... params) {

                        //把这个while当成看门狗吧。
                        while (true) {
                            TNUtilsUi.checkLockScreen(TNApplication.this);

                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                };

        taskWatcher.execute(null, null);
    }
}
