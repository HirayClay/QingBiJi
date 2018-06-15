package com.thinkernote.ThinkerNote.http;

/**
 * URL 统一管理类
 */

public class URLUtils {
    //URL根目录
//    public final static String API_BASE_URL = "http://new.qingbiji.cn:8088/";//内网测试
//    public final static String API_BASE_URL = "https://s.qingbiji.cn/";//外网测试
    public final static String API_BASE_URL = "http://new.qingbiji.cn/";//正式

    public final static String WEICHAT_BASE_URL = "https://api.weixin.qq.com";//微信登陆BaseURL

    /**
     * =============================================================================================
     * ============================================通用=================================================
     * =============================================================================================
     *
     */

    /**
     * 微信 获取accessToken
     */
    public static final String ACCESS_TOKEN = "/sns/oauth2/access_token";

    /**
     * 微信 获取个人信息
     */
    public static final String WEICHAT_MESSAGE = "/sns/userinfo";

    /**
     * =============================================================================================
     * ============================================首页相关=================================================
     * =============================================================================================
     *
     */

    /**
     * 登录相关(登录 注册 找回密码)
     */
    public static class Log {
        /**
         *
         */
        public static final String LOGINNORMAL = "api/login";

        /**
         * qq登录
         */
        public static final String LOGINQQ = "api/login/third";

        /**
         * 手机验证码接口
         */
        public static final String PHONE_QUERFY_CODE = "api/verifycode";
        /**
         * 邮箱验证码接口
         */
        public static final String EMAIL_QUERFY_CODE = "api/verifycode/email";

        /**
         * 绑定手机
         */
        public static final String LOGIN_BIND = "api/login/bind";

        /**
         * 图片验证
         */
        public static final String VERIFY_PIC = "api/captcha";

        /**
         * 找回密码 提交
         */
        public static final String SUBMIT_FINDPS = "api/user/password/reset";

        /**
         * 注册 提交
         */
        public static final String SUBMIT_RESGIST = "api/register";

        /**
         * 修改手机号 提交
         */
        public static final String CHANGE_PHONE = "api/user/profile";

        /**
         * 获取用户信息
         */
        public static final String USER_INFO = "api/user/profile";

        /**
         * logout
         */
        public static final String LOGOUT = "api/logout";

    }

    public static class Home {
        /**
         * 检查更新
         */
        public static final String UPGRADE = "api/app/upgrade";

        /**
         * 同步数据
         */
        public static final String SYNCHRONIZE = "api/app/upgrade";

        /**
         * feedBack
         */
        public static final String FEEDBACK = "api/feedback";

        /**
         * 支付
         */
        public static final String PAY_TIP = "api/margin/deposit";

        /**
         * pic 上传 （feedback，等）
         */
        public static final String UPLOAD_PIC = "api/attachment";


    }

    public static class Note {
        /**
         * 文件
         */
        public static final String FOLDER = "api/folders";

        /**
         * 标签
         */
        public static final String TAG = "api/tags";
        /**
         * 标签
         */
        public static final String TAGLIST = "api/tags";
        /**
         * getNote
         */
        public static final String NOTE = "api/note";

        /**
         * 设置默认路径
         */
        public static final String DEFAULT_FOLDER = "api/folders/default";

        /**
         * 设置默认路径
         */
        public static final String VERIFY_EMAIL = "api/user/verifyemail";


    }


}