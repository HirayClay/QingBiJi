package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.CatInfoModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.LogModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ICatInfoModule;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.p.ICatInfoPresener;
import com.thinkernote.ThinkerNote._interface.p.ILogPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;

/**
 * 登录 p层 具体实现
 */
public class CatInfoPresenterImpl implements ICatInfoPresener, OnCommonListener {
    private Context context;
    private OnCommonListener onView;
    //p层调用M层方法
    private ICatInfoModule module;

    public CatInfoPresenterImpl(Context context, OnCommonListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new CatInfoModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================
    @Override
    public void pSetDefaultFolder(long catId) {
        module.mSetDefaultFolder(this, catId);
    }

    //==========================结果回调==============================
    @Override
    public void onSuccess(Object obj) {
        onView.onSuccess(obj);
    }

    @Override
    public void onFailed(String msg, Exception e) {
        onView.onFailed(msg, e);
    }
}