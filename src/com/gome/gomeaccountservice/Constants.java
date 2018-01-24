/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.gome.gomeaccountservice;

public class Constants {
	public static final String TAG_PRE = "gomeaccountservice_";
    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.gome.gomeaccountservice";
    /** The Intent extra to store username. */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.gome.gomeaccountservice";
    
    static final String KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE = "key_auth_token_type_get_login_state";
    static final String KEY_AUTH_TOKEN_TYPE_GET_LOCAL_LOGIN_STATE = "key_auth_token_type_get_local_login_state";
	static final String KEY_AUTH_TOKEN_TYPE_LOGIN = "key_auth_token_type_login";
	static final String KEY_AUTH_TOKEN_TYPE_LOGIN_INFO = "key_auth_token_type_login_info";
	static final String KEY_AUTH_TOKEN_TYPE_VERIFY_PWD = "key_auth_token_type_verify_pwd";//验证密码
	static final String KEY_VERIFY_PWD_RESULT = "key_verify_pwd_result";//验证密码结果key
	static final String KEY_AUTH_TOKEN_TYPE_RESET_PWD = "key_auth_token_type_reset_pwd";
	static final String KEY_AUTH_TOKEN_TYPE_LOGIN_OUT = "key_auth_token_type_login_out";
	static final String KEY_AUTH_TOKEN_TYPE_REGISTER = "key_auth_token_type_register";
	static final String KEY_AUTH_TOKEN_TYPE_WEIBO_LOGIN = "key_auth_token_type_weibo_login";

    
    public static final String KEY_SERVER_AUTHEN_SUCCESS = "success";
    public static final String KEY_NETWORK_UNCONNECT = "networkUnconnect";
    public static final String KEY_SERVER_AUTHEN_NAME_NOT_FOUND = "username not found";
    public static final String KEY_SERVER_AUTHEN_PASSWOR_ERROR = "password error";
    public static final String KEY_SERVER_AUTHEN_LOGIN_TIMEOUT = "login timout";
    
    //本地缓存信息key
    public static final String HEAD_PORTRAIT_DIR_PATH = "/sdcard/.gomeaccount/";
    public static final String HEAD_PORTRAIT_SUFFIX = "jpg";
    public static final String SHAREPREFEENCE_ACCOUNTS = "gome_accounts"; //临时保存账号信息，最近账号，启动模式等
    public static final String SHAREPREFEENCE_ACCOUNT_INFO_PREFIX = "account_";//账号详细信息
    public static final String KEY_UNBIND_EMAIL_ACTIVE_STATE = "keyUnbindEmailActiveState";
    public static final String UNBIND_EMAIL_ACTIVE_TRUE = "1";
    public static final String UNBIND_EMAIL_ACTIVE_FALSE = "0";
    //public static final String KEY_UNBIND_TO_ACTIVE_EMAIL = "unbindToActiveEmail"; //解绑待激活的邮箱
    //server 账号信息key
    public static final String KEY_ACCOUNT_USER_NAME = "userName";//登录使用的账号，包括手机号邮箱等
    public static final String KEY_ACCOUNT_NICK_NAME = "nickName";
    public static final String KEY_ACCOUNT_PWD = "loginPwd";
    public static final String KEY_ACCOUNT_NEW_PWD = "newLoginPwd"; //重置密码用
    public static final String KEY_ACCOUNT_EMAIL = "mallAddress";
    public static final String KEY_ACCOUNT_PHONE_NUMBER = "phoneNo";
    public static final String KEY_ACCOUNT_SECURITY_PHONE_NUMBER = "safePhoneNo";
    public static final String KEY_SMS_CODE = "authCode";  //短信验证码
    public static final String KEY_ACCOUNT_GOME_ID = "gomeId";

    public static final String KEY_ACCOUNT_CREATE_TIME = "createdTime";
    public static final String KEY_ACCOUNT_SEX = "sex";
    public static final String KEY_ACCOUNT_USER_LEVEL = "userLevel";
    public static final String KEY_ACCOUNT_BIRTHDAY = "birthday";
    public static final String KEY_ACCOUNT_AREA = "userArea";
    //手机相关信息
    public static final String KEY_ACCOUNT_MODEL = "model"; //手机型号
    public static final String KEY_ACCOUNT_IMEI = "imei"; //手机标识
    public static final String KEY_ACCOUNT_LANGUAGE = "language"; //key手机设置的语言
    //第三方登录
    public static final String KEY_ACCOUNT_THIRD_ID = "thirdId";
    public static final String KEY_ACCOUNT_THIRD_TYPE = "thirdType";
    //本地存储
    public static final String KEY_LOCAL_AVATAR_PATH = "localAvatarPath";  //本地头像路径key
    public static final String KEY_LOCAL_AVATAR_URI = "localAvatarUri";  //本地设置头像的uri key
    public static final String KEY_LOCAL_RECENT_ACCOUNT_NAME = "localRecentAccountName"; //最新登录的账号key
    public static final String KEY_LOCAL_ACCOUNT_START_MODE = "localAccountStartMode";  //账号启动模式
    public static final String KEY_START_MODE_LOGIN_FEEDBACK = "startModeLoginFeedback";  //账号启动模式登录跳转的界面
    public static final String NORMAL_MODE = "nomal";     //普通模式
    public static final String BOOT_WIZARD_MODE= "bootWizard"; //开机向导模式 
    public static final String USER_CENTER_MODE= "userCenter"; //用户中心模式 
    //邮箱绑定相关
    public static final String KEY_EMAIL_ACTIVE_STATUS = "activeStatus"; //key 绑定状态
    public static final String EMAIL_STATUS_NORMAL = "0";   //正常状态
    public static final String EMAIL_STATUS_REGISTER_NOT_ACTIVED = "1";   //邮箱注册未激活状态
    public static final String EMAIL_STATUS_TO_BIND = "3";   //待绑定
    public static final String EMAIL_STATUS_TO_UNBIND = "4"; //待解绑
    //找回密码邮箱激活相关
    public static final String KEY_EMAIL_VALID_FLAG = "validFlag"; //key 找回密码邮箱激活
    public static final String EMAIL_VALID_FLAG_NOT_ACTIVE = "0";  //找回密码邮箱未激活
    public static final String EMAIL_VALID_FLAG_ACTIVE = "1";      //找回密码邮箱已经激活
    //校验类型
    public static final String KEY_VERIFY_TYPE = "verifyType";  //校验类型
    public static final String KEY_VERIFY_TYPE_CODE = "0";  //校验类型验证码
    //性别相关
    public static final String SEX_MAN = "1";
    public static final String SEX_WOMAN = "0";
    //头像相关
    //public static final String ACCOUNT_SERVER_AVATAR_PREFIX = "http://192.168.1.129/";
    public static final String KEY_ACCOUNT_SERVER_AVATAR = "avatar";  //上传服务器头像数据key，
    public static final String KEY_ACCOUNT_SERVER_AVATAR_PATH = "avatarPath";  //服务器头像路径key，
    public static final String KEY_AVATAR_TYPE = "avatarType";
    //发送短信验证码类型
//    public static final String KEY_SEND_SMS_MSG_TYPE = "msgType";
//    public static final String SEND_SMS_MSG_TYPE_REGISTER = "1";
//    public static final String SEND_SMS_MSG_TYPE_RETRIEVE = "2";
//    public static final String SEND_SMS_MSG_TYPE_BIND = "3";
    
    //注册类型
    public static final String KEY_ACCOUNT_REGISTER_TYPE = "registerType";  //注册类型0手机1邮箱3其他 内部使用服务端无需
    public static final String REGISTER_TYPE_PHONE = "0";
    public static final String REGISTER_TYPE_EMAIL = "1";
    //找回密码类型 
    public static final String KEY_RETRIEVE_PWD_TYPE = "retrievePwdType";  //找回密码类型*****本地使用服务端不包括
    public static final String RETRIEVE_PWD_TYPE_PHONE = "0"; //找回密码类型*****需和服务器对应
    public static final String RETRIEVE_PWD_TYPE_MAIL = "1"; //找回密码类型*****需和服务器对应
    //绑定解绑,验证码，邮件 相关  需和服务器匹配
    public static final String KEY_OPERATE_TYPE = "operateType";  //请求类型
    public static final String OPERATE_TYPE_REGISTER = "1"; //注册
    public static final String OPERATE_TYPE_RETRIEVE_PWD = "2"; //找回密码
    public static final String OPERATE_TYPE_BIND = "3"; //绑定手机号或邮箱
    public static final String OPERATE_TYPE_UNBIND = "4"; //解绑手机号或邮箱
    public static final String OPERATE_TYPE_BIND_SECURITY_PHONE = "5"; //解绑
    public static final String OPERATE_TYPE_UNBIND_SECURITY_PHONE = "6"; //解绑安全码
    public static final String OPERATE_TYPE_BIND_PRE_VERIFY = "7"; //绑定-解绑前一步校验
    public static final String OPERATE_TYPE_LOGIN = "8"; //登录验证码校验
    
//    public static final String KEY_BIND_TYPE = "bindType";  //本地使用
//    public static final String BIND_TYPE_PHONE = "bindTypePhone";
//    public static final String BIND_TYPE_SECURITY_PHONE = "bindTypeSecurityPhone";  
//    public static final String BIND_TYPE_EMAIL = "bindTypeEmail";  
//    public static final String KEY_UNBIND_TYPE = "unbindType";  //本地使用
//    public static final String UNBIND_TYPE_PHONE = "unbindTypePhone";  
//    public static final String UNBIND_TYPE_SECURITY_PHONE = "unbindTypeSecurityPhone"; 
//    public static final String UNBIND_TYPE_EMAIL = "unbindTypeEmail";
    
    //server 返回状态key
    public static final String KEY_SERVER_TOKEN = "token";
    public static final String KEY_SERVER_RESULT_CODE = "resCode";
    public static final String KEY_SERVER_RESULT_MSG = "resMsg";
    public static final String SERVER_SUCCESS_RESULT_CODE = "2000"; //成功状态
    public static final String SERVER_TOKEN_ERROR_CODE = "4017";    //token失效状态
    public static final String SERVER_MULTIPLE_PWD_ERROR = "4031";  //多次输入密码错误
    public static final String SERVER_VERIFY_CODE_ERROR = "4009";  //验证码错误
    public static final String SERVER_TIMEOUT_RESULT_CODE = "-1"; //请求超时resultcode
    //图片配置
    public static final float IMAGE_SCALE = 0.25f; //设置图片宽高缩放的值
    public static final int IMAGE_COMPRESS_QUALITY = 100;//100表示不压缩
    //广播相关
    public static final String ACTION_GOME_ACCOUNT_LOGIN_OUT = "gome account login out";
    //微博相关
    /** 当前 DEMO 应用的 APP_KEY，第三方应用应该使用自己的 APP_KEY 替换该 APP_KEY */
    public static final String APP_KEY      = "1866397036";//2032678546
    /** 
     * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
     * 
     * <p>
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     * </p>
     */
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
}
