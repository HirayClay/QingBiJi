package com.thinkernote.ThinkerNote.wxapi;

import java.net.URLEncoder;

import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.Utils.MLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 微信登录 设置
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final int BACK_RESULT_CODE = 11;
	private IWXAPI api;
    private String WX_APP_ID = "wx2c2721939e9d54e3";
    private String WX_APP_SECRET = "51c4ca7f07d5e761f82028e49c05936a";
    // 获取第一步的code后，请求以下链接获取access_token
    private String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    // 获取用户个人信息
    private String GetUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        String result = "";
        switch (resp.errCode) {
        case BaseResp.ErrCode.ERR_OK:
            if (resp.transaction != null) {
                finish();
                Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show();
            } else {
                String code = ((SendAuth.Resp) resp).code;
            /*
             * 将你前面得到的AppID、AppSecret、code，拼接成URL 获取access_token等等的信息(微信)
             */
                String get_access_token = getCodeRequest(code);
                //异步
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(get_access_token, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {
                        super.onSuccess(statusCode, response);
                        try {
                            if (!response.equals("")) {
                                String access_token = response
                                        .getString("access_token");
                                String openid = response.getString("openid");
                                String refresh_token = response.getString("refresh_token");
                                String get_user_info_url = getUserInfo(access_token, openid);
                                //
                                getUserInfo(get_user_info_url, access_token, refresh_token);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            break;
        case BaseResp.ErrCode.ERR_USER_CANCEL:
            result = "发送取消";
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            finish();
            break;
        case BaseResp.ErrCode.ERR_AUTH_DENIED:
            result = "发送被拒绝";
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            finish();
            break;
        default:
            result = "发送返回";
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            finish();
            break;
        }
    }

    /**
     * 通过拼接的用户信息url获取用户信息
     * 
     * @param user_info_url
     */
    private void getUserInfo(String user_info_url, final String access_token, final String refresh_token) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(user_info_url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                super.onSuccess(statusCode, response);
                try {
                    if (!response.equals("")) {
                        String unionid = response.getString("unionid");
                        String nickName = response.getString("nickname");
                        //
                        MLog.d("触发微信登录界面");
                        //
//                        TNAction.runActionAsync(TNActionType.LoginThird, 9, unionid, System.currentTimeMillis(), access_token, refresh_token, nickName);

                        //数据返回,登录界面处理
                        Intent intent = new Intent();
                        intent.putExtra("unionid", unionid);
                        intent.putExtra("access_token", access_token);
                        intent.putExtra("refresh_token", refresh_token);
                        intent.putExtra("nickName", nickName);
                        setResult(BACK_RESULT_CODE,intent);

//                        WXEntryActivity.this.finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    MLog.e("bug--"+e.toString());
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    /**
     * 获取access_token的URL（微信）
     * 
     * @param code
     *            授权时，微信回调给的
     * @return URL
     */
    private String getCodeRequest(String code) {
        String result = null;
        GetCodeRequest = GetCodeRequest.replace("APPID",
                urlEnodeUTF8(WX_APP_ID));
        GetCodeRequest = GetCodeRequest.replace("SECRET",
                urlEnodeUTF8(WX_APP_SECRET));
        GetCodeRequest = GetCodeRequest.replace("CODE", urlEnodeUTF8(code));
        result = GetCodeRequest;
        return result;
    }

    /**
     * 获取用户个人信息的URL（微信）
     * 
     * @param access_token
     *            获取access_token时给的
     * @param openid
     *            获取access_token时给的
     * @return URL
     */
    private String getUserInfo(String access_token, String openid) {
        String result = null;
        GetUserInfo = GetUserInfo.replace("ACCESS_TOKEN",
                urlEnodeUTF8(access_token));
        GetUserInfo = GetUserInfo.replace("OPENID", urlEnodeUTF8(openid));
        result = GetUserInfo;
        return result;
    }

    private String urlEnodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	TNAction.unregister(this);
    }
    
}