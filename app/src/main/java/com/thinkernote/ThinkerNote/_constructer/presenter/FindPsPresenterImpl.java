package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.FindPsModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IFindPsModule;
import com.thinkernote.ThinkerNote._interface.p.IFindPsPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnFindPsListener;

/**
 * 登录 p层 具体实现
 */
public class FindPsPresenterImpl implements IFindPsPresenter, OnFindPsListener {
    private Context context;
    private OnFindPsListener onView;
    //p层调用M层方法
    private IFindPsModule module;

    public FindPsPresenterImpl(Context context, OnFindPsListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new FindPsModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================

    //图片验证
    @Override
    public void getVerifyPic() {
        module.getVerifyPic(this);
    }

    //短信验证码
    @Override
    public void phoneVerifyCode(String mPhone, String name, String mAnswer, String mNonce, String mHashKey) {
        module.phoneVerifyCode(this, mPhone, name, mAnswer, mNonce, mHashKey);
    }

    //邮箱验证
    @Override
    public void mailVerifyCode(String mEmail, String name) {
        module.mailVerifyCode(this, mEmail, name);
    }

    //修改密码提交
    @Override
    public void submit(String phone, String ps, String vcode) {
        module.submit(this, phone, ps, vcode);
    }

    //登录
    @Override
    public void autoLogin(String phoneOrEmail, String ps) {
        module.autoLogin(this, phoneOrEmail, ps);
    }

    //更新
    @Override
    public void pProfile() {
        module.mProfile(this);
    }

    //==========================结果回调==============================
    //
    @Override
    public void onPicSuccess(Object obj) {
        onView.onPicSuccess(obj);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        onView.onPicFailed(msg, e);
    }

    //
    @Override
    public void onPhoneVCodeSuccess(Object obj) {
        onView.onPhoneVCodeSuccess(obj);
    }

    @Override
    public void onPhoneVCodeFailed(String msg, Exception e) {
        onView.onPhoneVCodeFailed(msg, e);
    }

    //
    @Override
    public void onMailVCodeSuccess(Object obj) {
        onView.onMailVCodeSuccess(obj);
    }

    @Override
    public void onMailVCodetFailed(String msg, Exception e) {
        onView.onMailVCodetFailed(msg, e);
    }

    //
    @Override
    public void onSubmitSuccess(Object obj) {
        onView.onSubmitSuccess(obj);
    }

    @Override
    public void onSubmitFailed(String msg, Exception e) {
        onView.onSubmitFailed(msg, e);
    }

    //
    @Override
    public void onAutoLoginSuccess(Object obj) {
        onView.onAutoLoginSuccess(obj);
    }

    @Override
    public void onAutoLoginFailed(String msg, Exception e) {
        onView.onAutoLoginFailed(msg, e);
    }

    //
    @Override
    public void onProfileSuccess(Object obj) {
        onView.onProfileSuccess(obj);
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        onView.onProfileFailed(msg, e);
    }


    //========================================================
}
