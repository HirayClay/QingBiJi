package com.thinkernote.ThinkerNote.Activity;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.TNLinearLayout;
import com.thinkernote.ThinkerNote.Other.TNLinearLayout.TNLinearLayoutListener;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.p.ILogPresener;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote._constructer.presenter.LogPresenterImpl;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.Toast;

/**
 * sjy 0607
 */
public class TNLoginAct extends TNActBase implements OnClickListener, OnLogListener {

    // Class members
    //-------------------------------------------------------------------------------
    private String mUserName, mPassword;
    private Dialog mLoginingDialog = null;
    private AlphaAnimation mAlphaAnimation;
    private static IWXAPI WXapi;
    private Weibo mWeibo;
    private Boolean isClickQQ = false;

    /**
     * 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能
     */
    private Oauth2AccessToken mAccessToken;

    /**
     * 注意：SsoHandler 仅当sdk支持sso时有效
     */
    private SsoHandler mSsoHandler;

    private Tencent mTencent;//qq
    private String mLoginId;

    //p层相关
    ILogPresener logPresener;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        TNActivityManager.getInstance().finishOtherActivity(this);
        // TODO
        TNAction.regResponder(TNActionType.Login, this, "respondLogin");
        TNAction.regResponder(TNActionType.LoginThird, this, "respondLoginThird");

        initView();
        //初始化 p
        logPresener = new LogPresenterImpl(this, this);
    }

    private void initView() {
        // 根据屏幕大小改变布局
        ((TNLinearLayout) findViewById(R.id.login_layout)).setListener(new TNLinearLayoutListener() {
            @Override
            public void onTNLinearLayoutMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            }
        });

        // initialize event
        findViewById(R.id.login_trial_btn).setOnClickListener(this);
        findViewById(R.id.login_login).setOnClickListener(this);
        findViewById(R.id.login_forget_password).setOnClickListener(this);
        findViewById(R.id.login_weixin).setOnClickListener(this);
        findViewById(R.id.login_qq).setOnClickListener(this);
        findViewById(R.id.login_sina).setOnClickListener(this);

        //加载动画
        mLoginingDialog = TNUtilsUi.progressDialog(TNLoginAct.this, R.string.logging_in);
        mAlphaAnimation = new AlphaAnimation(0, 1);
        mAlphaAnimation.setDuration(1500);
    }

    protected void configView() {
        if (createStatus == 0) {
            TNSettings settings = TNSettings.getInstance();
            EditText name = (EditText) findViewById(R.id.login_username);
            if (settings.syncOldDb) {
                if (settings.loginname.length() > 0) {
                    name.setText(settings.loginname);
                    name.setSelection(settings.loginname.length());
                }
            } else {
                if (settings.username.length() > 0) {
                    name.setText(settings.username);
                    name.setSelection(settings.username.length());
                } else if (settings.userId != 0) {
                    String userName = UserDbHelper.getOldDbUserName(settings.userId);
                    name.setText(userName);
                    name.setSelection(userName.length());
                }
            }
        } else {
            if (!isClickQQ)
                mLoginingDialog.hide();//获取第三方信息后直接点返回
        }
    }

    @Override
    public void onDestroy() {
        mLoginingDialog.dismiss();
        super.onDestroy();
    }

    // implements OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login://正常登录
                login();
                break;
            case R.id.login_qq://qq登录
//			TNUtilsUi.alert(this, "即将上线，\n有问题请联系QQ客服:1657975956");
                mLoginingDialog.show();
                isClickQQ = true;
                loginQQ();
                break;
            case R.id.login_sina://新浪登录
                mLoginingDialog.show();
                loginSina();
                break;
            case R.id.login_weixin://微信登录
                mLoginingDialog.show();
                loginWechat();

                break;
            case R.id.login_forget_password://忘记密码
                startActivity(TNFindPasswordAct.class);
                break;

            case R.id.login_trial_btn: {//注册

                Bundle b = new Bundle();
                b.putString("type", "register");
                startActivity(TNRegistAct.class, b);
                break;
            }


        }
    }
    //-------------------------------------正常登录------------------------------------------

    private void login() {
        mUserName = ((EditText) findViewById(R.id.login_username))
                .getText().toString().trim().toLowerCase();
        if (mUserName.length() == 0) {
            TNUtilsUi.alert(this, R.string.alert_Login_UsernameBlank);
            ((EditText) findViewById(R.id.login_username)).requestFocus();
            return;
        }

        mPassword = ((EditText) findViewById(R.id.login_password))
                .getText().toString();
        if (mPassword.length() == 0) {
            TNUtilsUi.alert(this, R.string.alert_Login_PasswordBlank);
            ((EditText) findViewById(R.id.login_password)).requestFocus();
            return;
        }
        TNSettings settings = TNSettings.getInstance();
        settings.loginname = mUserName;
        settings.password = mPassword;
        settings.userType = 0;
        settings.savePref(false);

        TNUtilsUi.hideKeyboard(this, R.id.login_password);
        mLoginingDialog.show();

        //调用p层代码
        pLoginNormal(mUserName, mPassword);

    }
//-------------------------------------QQ登录------------------------------------------

    private void loginQQ() {
        mTencent = Tencent.createInstance(TNConst.QQ_APP_ID, this.getApplicationContext());
        IUiListener listener = new IUiListener() {
            @Override
            public void onError(UiError arg0) {
                mLoginingDialog.hide();
                isClickQQ = false;
                Toast.makeText(getApplicationContext(), "Auth error : " + arg0.errorDetail, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(JSONObject jobj) {
                // openid 用于唯一标识用户身份（每一个openid与QQ号码对应）。
                // access_token 用户进行应用邀请、分享、支付等基本业务请求的凭据。
                // expires_in access_token的有效时间，在有效期内可以发起业务请求，过期失效。
//				isClickQQ = false;
                mLoginId = jobj.optString("openid");
                String accessToken = jobj.optString("access_token");
                String refreshToken = jobj.optString("pay_token");
                getQQUnionId(accessToken, refreshToken);
            }

            @Override
            public void onCancel() {
                isClickQQ = false;
                mLoginingDialog.hide();
                Toast.makeText(getApplicationContext(), "Auth cancel",
                        Toast.LENGTH_LONG).show();
            }
        };
        mSsoHandler = null;
        mTencent.login(this, TNConst.QQ_SCOPE, listener);
    }

    private void getQQUnionId(final String accessToken, final String refreshToken) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://graph.qq.com/oauth2.0/me?access_token=" + accessToken + "&unionid=1";
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] data) {
                super.onSuccess(statusCode, headers, data);
                String s = new String(data);
                String[] split = s.split(":");
                s = split[split.length - 1];
                split = s.split("\"");
                s = split[1];
                String unionId = s;
                //
                pLoginQQ(3, unionId, System.currentTimeMillis(), accessToken, refreshToken, "QQ" + System.currentTimeMillis());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] data, Throwable e) {
                super.onFailure(statusCode, headers, data, e);
                Toast.makeText(getApplicationContext(), "Auth Fail", Toast.LENGTH_LONG).show();
            }
        });
    }


    //-------------------------------------wechat登录------------------------------------------
    private void loginWechat(){
        WXapi = WXAPIFactory.createWXAPI(this, TNConst.WX_APP_ID, true);
        WXapi.registerApp(TNConst.WX_APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo";
        WXapi.sendReq(req);
    }

    //-------------------------------------sina登录------------------------------------------

    private void loginSina() {
        mWeibo = Weibo.getInstance(TNConst.SINA_APP_KEY, TNConst.SINA_REDIRECT_URL,
                TNConst.SINA_SCOPE);

        mSsoHandler = new SsoHandler(TNLoginAct.this, mWeibo);
        mSsoHandler.authorize(new AuthDialogListener(), null);
    }

    /**
     * 微博认证授权回调类。 1. SSO登陆时，需要在{@link #onActivityResult}
     * 中调用mSsoHandler.authorizeCallBack后， 该回调才会被执行。 2. 非SSO登陆时，当授权后，就会被执行。
     * 当授权成功后，请保存该access_token、expires_in等信息到SharedPreferences中。
     */
    class AuthDialogListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            String access_token = values.getString("access_token");
            String refresh_token = values.getString("refresh_token");
            String expires_in = values.getString("expires_in");
            String uid = values.getString("uid");
            mAccessToken = new Oauth2AccessToken(access_token, expires_in);
            if (mAccessToken.isSessionValid()) {
                pLoginSina(2, uid, System.currentTimeMillis(), access_token, refresh_token, "sina" + System.currentTimeMillis());
            } else {
                mLoginingDialog.hide();
                Toast.makeText(getApplicationContext(), "Auth Fail", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onError(WeiboDialogError e) {
            mLoginingDialog.hide();
            Toast.makeText(getApplicationContext(),
                    "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
            mLoginingDialog.hide();
            Toast.makeText(getApplicationContext(), "Auth cancel",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            mLoginingDialog.hide();
            Toast.makeText(getApplicationContext(),
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    //--------------------------------------其他重写-----------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MLog.d("登录返回结果处理"+requestCode+"--"+resultCode);
        //wechat
        if (resultCode == 11) {//微信登录 成功返回
            MLog.d("登录返回结果处理");
            String access_token = data.getStringExtra("access_token");
            String refresh_token = data.getStringExtra("refresh_token");
            String uid = data.getStringExtra("unionid");
            String nickName = data.getStringExtra("nickName");
            MLog.d("登录返回结果处理"+nickName);
            //微信登录
            pLoginWechat(9, uid, System.currentTimeMillis(), access_token, refresh_token, nickName);

        } else {
            //qq
            if (mTencent != null) {
                mTencent.onActivityResult(requestCode, resultCode, data);
            }

            // sina
            // SSO 授权回调 重要：发起 SSO 登陆的Activity必须重写onActivityResult
            if (mSsoHandler != null) {
                mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    //-------------------------------------------------------------------------------

    public void respondLoginThird(TNAction aAction) {
        if (aAction.inputs.size() != 6)
            return;
        mLoginingDialog.hide();
        MLog.d("第三方登录返回==" + aAction.result + "--" + TNActionResult.Finished);
        if (aAction.result == TNActionResult.Finished) {
            TNSettings settings = TNSettings.getInstance();
            settings.isLogout = false;
            settings.firstLaunch = false;
            settings.savePref(false);
            startActivity(TNMainAct.class);
            finish();
        } else {
            int bType = (Integer) aAction.inputs.get(0);
            String bid = (String) aAction.inputs.get(1);
            long stamp = (Long) aAction.inputs.get(2);
            String outputs = (String) aAction.outputs.get(0);
            if ("用户未绑定轻笔记".equals(outputs)) {
                String accessToken = (String) aAction.inputs.get(3);
                String refreshToken = (String) aAction.inputs.get(4);
                String name = (String) aAction.inputs.get(5);
                Bundle b = new Bundle();
                b.putString("type", "third");
                b.putInt("bType", bType);
                b.putString("bid", bid);
                b.putLong("stamp", stamp);
                b.putString("accessToken", accessToken);
                b.putString("refreshToken", refreshToken);
                b.putString("name", name);
//				runActivity("TNRegistAct", b);
                startActivity(TNBindAccountAct.class, b);
            } else {
                TNHandleError.handleResult(TNLoginAct.this, aAction);
            }
        }
    }

    //-----------------------------------p层调用接口--------------------------------------------

    //正常登录
    private void pLoginNormal(String name, String ps) {
        logPresener.loginNormal(name, ps);
    }

    //qq登录
    private void pLoginQQ(int aArray, String unionId, long currentTime, String accessToken, String refreshToken, String name) {
        logPresener.loginQQ(aArray, unionId, currentTime, accessToken, refreshToken, name);
    }

    //sina 登录
    private void pLoginSina(int aArray, String unionId, long currentTime, String accessToken, String refreshToken, String name) {
        logPresener.loginSina(aArray, unionId, currentTime, accessToken, refreshToken, name);
    }

    //wechat 登录
    private void pLoginWechat(int aArray, String unionId, long currentTime, String accessToken, String refreshToken, String name) {
        logPresener.loginWechat(aArray, unionId, currentTime, accessToken, refreshToken, name);
    }

    //-----------------------------------接口返回的回调--------------------------------------------

    //正常登录
    @Override
    public void onLoginNormalSuccess(Object obj) {
        mLoginingDialog.hide();
        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;
        settings.savePref(false);
        startActivity(TNMainAct.class);
        finish();
    }

    @Override
    public void onLoginNormalFailed(String msg, Exception e) {
        mLoginingDialog.hide();
        if (e == null) {
            TNUtilsUi.alert(this, msg);
        } else {
            TNUtilsUi.alert(this, "error!");
        }
    }

    //qq登录
    @Override
    public void onLoginQQSuccess(Object obj) {
        mLoginingDialog.hide();
        //
        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;
        settings.firstLaunch = false;
        settings.savePref(false);
        startActivity(TNMainAct.class);
        finish();
    }

    @Override
    public void onLoginQQFailed(String msg, Exception e, String bid, long currentTime, String accessToken, String refreshToken, String name) {
        mLoginingDialog.hide();
        if (msg.equals("用户未绑定轻笔记")) {//登录的参数还需要使用，所以在回调中再返回
            Bundle b = new Bundle();
            b.putString("type", "third");
            b.putInt("bType", 3);
            b.putString("bid", bid);
            b.putLong("stamp", currentTime);
            b.putString("accessToken", accessToken);
            b.putString("refreshToken", refreshToken);
            b.putString("name", name);
            startActivity(TNBindAccountAct.class, b);//绑定手机号
        } else {
            TNUtilsUi.alert(this, msg);
        }
    }

    @Override
    public void onLoginWechatSuccess(Object obj) {
        mLoginingDialog.hide();
        //
        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;
        settings.firstLaunch = false;
        settings.savePref(false);
        startActivity(TNMainAct.class);
        finish();
    }

    @Override
    public void onLoginWechatFailed(String msg, Exception e, String bid, long currentTime, String accessToken, String refreshToken, String name) {
        mLoginingDialog.hide();
        if (msg.equals("用户未绑定轻笔记")) {//登录的参数还需要使用，所以在回调中再返回
            Bundle b = new Bundle();
            b.putString("type", "third");
            b.putInt("bType", 9);
            b.putString("bid", bid);
            b.putLong("stamp", currentTime);
            b.putString("accessToken", accessToken);
            b.putString("refreshToken", refreshToken);
            b.putString("name", name);
            startActivity(TNBindAccountAct.class, b);//绑定手机号
        } else {
            TNUtilsUi.alert(this, msg);
        }
    }

    @Override
    public void onLoginSinaSuccess(Object obj) {
        mLoginingDialog.hide();
        //
        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;
        settings.firstLaunch = false;
        settings.savePref(false);
        startActivity(TNMainAct.class);
        finish();
    }

    @Override
    public void onLoginSinaFailed(String msg, Exception e, String bid, long currentTime, String accessToken, String refreshToken, String name) {
        mLoginingDialog.hide();
        if (msg.equals("用户未绑定轻笔记")) {//登录的参数还需要使用，所以在回调中再返回
            Bundle b = new Bundle();
            b.putString("type", "third");
            b.putInt("bType", 2);
            b.putString("bid", bid);
            b.putLong("stamp", currentTime);
            b.putString("accessToken", accessToken);
            b.putString("refreshToken", refreshToken);
            b.putString("name", name);
            startActivity(TNBindAccountAct.class, b);//绑定手机号
        } else {
            TNUtilsUi.alert(this, msg);
        }
    }
    //-------------------------------------------------------------------------------

}