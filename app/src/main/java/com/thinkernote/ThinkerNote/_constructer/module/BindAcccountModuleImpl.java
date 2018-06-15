package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IBindAccountModule;
import com.thinkernote.ThinkerNote._interface.v.OnBindAccountListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * 绑定手机号
 */
public class BindAcccountModuleImpl implements IBindAccountModule{

    private  Context context;
    public BindAcccountModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mVerifyCode(final OnBindAccountListener listener, String phone, String t) {
        MyHttpService.NoCacheBuilder.getHttpServer()//固定样式，可自定义其他网络
                .postVerifyCode2(phone, t)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d( "验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e( "验证码--登录失败异常onError:" + e.toString());
                        listener.onVerifyCodeFailed("异常"  ,new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d( "验证码-成功");
                            listener.onVerifyCodeSuccess(bean);
                        } else{
                            listener.onVerifyCodeFailed(bean.getMessage(),null);
                        }
                    }

                });
    }

    @Override
    public void mLoginBind(final OnBindAccountListener listener, int btype, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode, String sign) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginBindPhone(btype, bid,name,accessToken,refreshToken,currentTime,phone,vcode,sign)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d( "绑定手机号--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e( "绑定手机号--登录失败异常onError:" + e.toString());
                        listener.onVerifyCodeFailed("异常"  ,new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("绑定手机号-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d( "绑定手机号-成功");
                            listener.onVerifyCodeSuccess(bean);
                        } else{
                            listener.onVerifyCodeFailed(bean.getMessage(),null);
                        }
                    }

                });
    }

    @Override
    public void autoLoginAferBind(final OnBindAccountListener listener, int btype, final String bid, final long stamp, String sign ) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginQQ(btype, bid,stamp,sign)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("绑定登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e( "绑定登录--登录失败异常onError:" + e.toString());
                        listener.onAutoLogFailed("异常"  ,new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "绑定登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "绑定登录-成功");
                            listener.onAutoLogSuccess(bean);
                        } else{
                            listener.onAutoLogFailed(bean.getMessage(),null);
                        }
                    }

                });
    }
}