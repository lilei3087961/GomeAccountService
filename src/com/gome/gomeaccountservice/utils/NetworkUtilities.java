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

package com.gome.gomeaccountservice.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gome.gomeaccountservice.Constants;
import com.gome.gomeaccountservice.R;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import android.os.SystemProperties;
/**
 * Provides utility methods for communicating with the server.
 */
final public class NetworkUtilities {
    /** The tag used to log to adb console. */
    private static final String TAG = Constants.TAG_PRE+"NetworkUtilities";
    /**
     * 是否加密
     */
    private static final boolean IS_ENCRYPTION = true;  //是否加密
    private static final boolean IS_PASSWORD_MD5_ENCRYPT = false;  //密码是否md5单独加密加密
    /** POST parameter name for the user's account name */
    public static final String PARAM_USERNAME = "username";
    /** POST parameter name for the user's password */
    public static final String PARAM_PASSWORD = "password";
    /** POST parameter name for the user's authentication token */
    public static final String PARAM_AUTH_TOKEN = "authtoken";
    /** POST parameter name for the client's last-known sync state */
    public static final String PARAM_SYNC_STATE = "syncstate";
    /** POST parameter name for the sending client-edited contact info */
    public static final String PARAM_CONTACTS_DATA = "contacts";
    /** Timeout (in ms) we specify for each http request */
    public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
    /** Base URL for the v2 Sample Sync Service */
    public static final String BASE_URL = "https://samplesyncadapter2.appspot.com";
    /** URI for authentication service */
    public static final String AUTH_URI = BASE_URL + "/auth";
    /** URI for sync service */
    public static final String SYNC_CONTACTS_URI = BASE_URL + "/sync"; //authenticate
    private static final int DEFAULT_REQUEST_TIMEOUT = 20000; // 20 seconds
    private static final int SHORT_REQUEST_TIMEOUT = 10000; // 10 seconds,主要是获取登录状态超时时间
    public static final String ENCRYPTION_HTTPS_HOST = "https://www.gomefota.com/"; //url为默认生产环境
    public static final String PRE_TEST_HTTPS_HOST = "https://www.prep.gomefota.com/";//url为预发环境
    public static String HTTPS_HOST = ENCRYPTION_HTTPS_HOST;//默认使用生产环境url
    
    public static final int AUTHENTICATE_TYPE_REGISTER = 1;
    public static final int AUTHENTICATE_TYPE_REGISTER_TRUST = 2;  //无验证码注册
    public static final int AUTHENTICATE_TYPE_LOGIN = 3;
    public static final int AUTHENTICATE_TYPE_LOGIN_OUT = 4; //退出账号暂未使用
    public static final int AUTHENTICATE_TYPE_LOGIN_OFF = 5; //注销账号
    public static final int AUTHENTICATE_TYPE_RESET_PWD = 6; //登陆后重置密码
    public static final int AUTHENTICATE_TYPE_SEND_SMS_VERIFY_CODE = 7;
    public static final int AUTHENTICATE_TYPE_RETRIEVE_PWD = 8; //未登录通过短信验证码重置密码
    public static final int AUTHENTICATE_TYPE_UPLOAD_AVATAR = 9; //上传头像
    public static final int AUTHENTICATE_TYPE_UPLOAD_PRIKEY = 10;//上传钥匙串私钥***
    public static final int AUTHENTICATE_TYPE_VERIFY_PHONE_OR_MAIL_VALID = 11; //验证手机号或邮箱是否合法
    public static final int AUTHENTICATE_TYPE_VERIFY_SMS_CODE_VALID = 12; //验证短信验证码
    public static final int AUTHENTICATE_TYPE_SEND_EMAIL = 13; //发送邮件
    public static final int AUTHENTICATE_TYPE_MODIFY_ACCOUNT_INFO = 14; //更新个人信息
    public static final int AUTHENTICATE_TYPE_GET_EMAIL_STATUS = 15; //获取邮箱状态
    public static final int AUTHENTICATE_TYPE_GET_EMAIL_VALID_FLAG = 16; //获取邮箱激活状态
    public static final int AUTHENTICATE_TYPE_BIND = 17;    //绑定
    public static final int AUTHENTICATE_TYPE_UNBIND = 18;  //解绑
    public static final int AUTHENTICATE_TYPE_VERIFY_PWD_OR_SMS_CODE = 19;  //验证密码或者短信验证码
    public static final int AUTHENTICATE_TYPE_VERIFY_TOKEN = 20;  //验证token是否生效即是否登录状态
    
    //public static final int AUTHENTICATE_TYPE_SEND_EMAIL = 11;
    static String mAccountName = null;
    static String mNickName = null;
    static String mEmailAddress = null;
    static String mPhoneNumber = null;
    static String mSecurityPhoneNumber = null;
    static String mAvatar = null;
    static String mAvatarType = null;
    
    static String mAuthCode = null;
    static String mLoginPwd = null;
    static String mNewLoginPwd = null;
    static String mLoginPwdMD5Encrypt = null;
    static String mNewLoginPwdMD5Encrypt = null;
    static String mRegisterType = null;
    static String mThirdId = null;
    static String mThirdType = null;
    
	static String mGomeId = null;
	static String mGomeIdBase64Encrypt = null;
	static String mSmsCode = null;
	static String mSex = null;
	static String mBirthday = null;
	static String mArea = null;
	static String mOperateType = null;  //操作类型 如注册，绑定手机，解绑手机等
	static String mToken = null;        //登陆后返回的token
	//static String mBindType = null;        //绑定类型，本地使用
	static String mUnbindType = null;      //解绑类型，本地使用
    static String mAuthUrl = null;
    static String mRetrievePwdType = null; //找回密码类型
    
    static String mJsonRequest = null;
    static String mJsonRequestEncrypt = null;
    static String mJsonResult = null;
    static String mJsonResultEncrypt = null;
    
	private static final HandlerThread sWorkerThread = new HandlerThread("NetworkUtilities-loader");
	static {
	    sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
	Handler mHandler =new Handler();
	    
    private NetworkUtilities() {
    }

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static HttpClient getHttpClient() {
        HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        return httpClient;
    }

//add by lilei begin
    /***
     *  初始化static变量
     */
    static void initVariable(){
         if(SystemProperties.getBoolean("persist.sys.inPreTestNet", false)){
            HTTPS_HOST = PRE_TEST_HTTPS_HOST;
         }else{
            HTTPS_HOST = ENCRYPTION_HTTPS_HOST;
         }
         Log.i(TAG,"initVariable() HTTPS_HOST:"+HTTPS_HOST);
    	 mAccountName = null;
         mNickName = null;
         mEmailAddress = null;
         mPhoneNumber = null;
         mSecurityPhoneNumber = null;
         mAvatar = null;
         mAvatarType = null;
        
         mAuthCode = null;
         mLoginPwd = null;
         mNewLoginPwd = null;
         mLoginPwdMD5Encrypt = null;
         mNewLoginPwdMD5Encrypt = null;
         mRegisterType = null;
         mThirdId = null;
         mThirdType = null;
        
    	 mGomeId = null;
    	 mGomeIdBase64Encrypt = null;
    	 mSmsCode = null;
    	 mSex = null;
    	 mBirthday = null;
    	 mArea = null;
    	 mOperateType = null;  //操作类型 如注册，绑定手机，解绑手机等
    	 mToken = null;        //登陆后返回的token
    	 mUnbindType = null;      //解绑类型，本地使用
         mAuthUrl = null;
         mRetrievePwdType = null; //找回密码类型
        
         mJsonRequest = null;
         mJsonRequestEncrypt = null;
         mJsonResult = null;
         mJsonResultEncrypt = null;
    }
    /***
     * 向服务器请求
     * @param authenType  请求类型
     * @param tableAccountInfo  请求参数
     * @return  返回解码后的json字符串
     */
    public synchronized static String authenticate(Context context,int authenType,HashMap<String,String> tableAccountInfo){
    	Log.i(TAG, "authenticate() Enter authenType:"+authenType);
        initVariable();
        boolean useNewTimeout = false;
        //JSONObject jsonObject = new JSONObject();
        String strRequestJson = null;
        switch (authenType) {
			case AUTHENTICATE_TYPE_REGISTER:  //注册
				strRequestJson = getRegisterRequestJson(context,tableAccountInfo);
	            mAuthUrl = HTTPS_HOST+"account-rest/account/register";
				break;
			case AUTHENTICATE_TYPE_REGISTER_TRUST:
				strRequestJson = getRegisterTrustRequestJson(context,tableAccountInfo);
	            mAuthUrl = HTTPS_HOST+"account-rest/account/registerTrust";
				break;
			case AUTHENTICATE_TYPE_LOGIN: //登录账号密码验证   邮箱+密码或者手机+密码
				strRequestJson = getLoginRequestJson(context,tableAccountInfo);
	        	mAuthUrl = HTTPS_HOST+"account-rest/account/login";
				break;
			case AUTHENTICATE_TYPE_LOGIN_OUT: 			//退出账号
				strRequestJson = getLoginOutRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/logout";
				break;
			case AUTHENTICATE_TYPE_LOGIN_OFF: 			//注销账号
				strRequestJson = getLoginOffRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/logoff";
				break;
			case AUTHENTICATE_TYPE_RESET_PWD:     //重置密码
				strRequestJson = getResetPwdRequestJson(context,tableAccountInfo);
	    		mAuthUrl = HTTPS_HOST+"account-rest/account/modifyPwd";
				break;
			case AUTHENTICATE_TYPE_SEND_SMS_VERIFY_CODE:  //发送短信验证码
				strRequestJson = getSendSmsVerifyCodeRequestJson(context,tableAccountInfo);
	        	mAuthUrl = HTTPS_HOST+"account-rest/account/sendAuthSms";
				break;
			case AUTHENTICATE_TYPE_RETRIEVE_PWD:  //忘记密码
				strRequestJson = getRetrievePwdRequestJson(context,tableAccountInfo);
	    		mAuthUrl = HTTPS_HOST+"account-rest/account/findPwd";
				break;
			case AUTHENTICATE_TYPE_UPLOAD_AVATAR://向服务器上传头像请求
				strRequestJson = getUploadAvatarRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/modifyAvatar";
				break;
			case AUTHENTICATE_TYPE_UPLOAD_PRIKEY:  //向服务器上传钥匙串私钥
				
				break;
			case AUTHENTICATE_TYPE_VERIFY_PHONE_OR_MAIL_VALID:  //验证手机邮箱是否可用
				strRequestJson = getVerifyPhoneOrMailRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/verifyParam";
				break;
			case AUTHENTICATE_TYPE_VERIFY_SMS_CODE_VALID:   //验证短信验证码是否正确
				strRequestJson = getVerifySmsCodeRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/verifyAuthSms";
				break; 
			case AUTHENTICATE_TYPE_SEND_EMAIL:   		//发送邮件
				strRequestJson = getSendEmailRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/sendAuthEmail";
				break;
			case AUTHENTICATE_TYPE_MODIFY_ACCOUNT_INFO: //更新账号信息
				strRequestJson = getModifyAccountInfoRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/modifyAccountInfo";
				break;
			case AUTHENTICATE_TYPE_GET_EMAIL_STATUS:  //获取邮箱状态
				strRequestJson = getEmailStatusRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/getMailAddress";
				break;
			case AUTHENTICATE_TYPE_GET_EMAIL_VALID_FLAG:  //获取邮箱激活状态
				strRequestJson = getEmailValidFlagRequestJson(context,tableAccountInfo);
				mAuthUrl = HTTPS_HOST+"account-rest/account/getMailStatus";
				break;
			case AUTHENTICATE_TYPE_BIND:
				strRequestJson = getBindRequestJson(context,tableAccountInfo);  //绑定
				mAuthUrl = HTTPS_HOST+"account-rest/account/bind";
				break;
			case AUTHENTICATE_TYPE_UNBIND:
				strRequestJson = getUnbindRequestJson(context,tableAccountInfo);  //解绑
				mAuthUrl = HTTPS_HOST+"account-rest/account/unbind";
				break;
			case AUTHENTICATE_TYPE_VERIFY_PWD_OR_SMS_CODE:
				strRequestJson = getVerifyPwdOrSmsCodeRequestJson(context,tableAccountInfo);  //验证密码或者短信验证码
				mAuthUrl = HTTPS_HOST+"account-rest/account/safeVerify";
				break;
            case AUTHENTICATE_TYPE_VERIFY_TOKEN:
                strRequestJson = getVerifyTokenRequestJson(context,tableAccountInfo);  //验证token是否生效即是否登录状态
                mAuthUrl = HTTPS_HOST+"account-rest/account/verifyToken";
                useNewTimeout = true;
                break;
			default:
				break;
		}
        Log.i(TAG, "authenticate before authenticateFromServer useNewTimeout:"+useNewTimeout);
        if(useNewTimeout) { //使用非默认超时时间
            mJsonResultEncrypt = authenticateFromServer(context, mAuthUrl, strRequestJson,SHORT_REQUEST_TIMEOUT);
        }else {
            mJsonResultEncrypt = authenticateFromServer(context, mAuthUrl, strRequestJson);
        }
        mJsonResult = getDecryptString(mJsonResultEncrypt);
    	Log.i(TAG, "authenticate() End authenType:"+authenType+" mJsonResultEncrypt:"+mJsonResultEncrypt+" mJsonResult:"+mJsonResult);
        return mJsonResult;
    }
    /**
     * 获取解密后的字符串
     * @param jsonResult
     * @return
     */
    static String getDecryptString(String str){
    	if(!IS_ENCRYPTION){ //不加密
    		mJsonResult = str;
    		return mJsonResult;
    	}
    	try {
    		mJsonResult = DesUtil.decrypt3DES(str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "getDecryptString error:"+e.toString());
			e.printStackTrace();
		}
    	return mJsonResult;
    }
    /**
     * 获取加密后的字符串
     * @param jsonRequest
     * @return
     */
    public static String getEncryptString(String str){
    	mJsonRequestEncrypt = null;
    	if(!IS_ENCRYPTION){ //不加密
    		mJsonRequestEncrypt = str;
    		return mJsonRequestEncrypt;
    	}
    	try {
    		Log.i(TAG, "getEncryptString () 111 before encrypt str:"+str);
			mJsonRequestEncrypt = DesUtil.encrypt3DES(str);
			Log.i(TAG, "getEncryptString () 222 after encrypt mJsonRequestEncrypt:"+mJsonRequestEncrypt);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "getEncryptString(..)  error:"+e.toString());
			e.printStackTrace();
		}
    	return mJsonRequestEncrypt;
    }
    /**
     * 获取加密后的注册请求json字符串
     * @param tableAccountInfo
     * @return
     */
    static String getRegisterRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	mSmsCode = tableAccountInfo.get(Constants.KEY_SMS_CODE);
    	mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
        //add by yimin.zhu for wechat
        mThirdType = tableAccountInfo.get(Constants.KEY_ACCOUNT_THIRD_TYPE);
        mThirdId = tableAccountInfo.get(Constants.KEY_ACCOUNT_THIRD_ID);  //第三方登录待后面确认
    	if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
    		mLoginPwdMD5Encrypt = DesUtil.MD5(mLoginPwd);
    	}else{
    		mLoginPwdMD5Encrypt = mLoginPwd;
    	}
    	mRegisterType = tableAccountInfo.get(Constants.KEY_ACCOUNT_REGISTER_TYPE); //本地使用
    	
    	Log.i(TAG, "getRegisterRequestJson(..)  IS_ENCRYPTION:"+IS_ENCRYPTION+" IS_PASSWORD_MD5_ENCRYPT:"+IS_PASSWORD_MD5_ENCRYPT
    			+"phoneNo:"+mPhoneNumber+" mallAddress:"+mEmailAddress+" mSmsCode:"+mSmsCode
    			+" loginPwd:"+mLoginPwd+" mLoginPwdMD5Encrypt:"+mLoginPwdMD5Encrypt+" registerType:"+mRegisterType);
    	try {
            jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);//密码使用md5加密
            //weichat register needed info by yimin.zhu
            if("2".equals(mThirdType)) {
                jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
                jsonObject.put(Constants.KEY_ACCOUNT_THIRD_TYPE,mThirdType);
                jsonObject.put(Constants.KEY_ACCOUNT_THIRD_ID,mThirdId);
            } else {
                if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){
                    jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
                    jsonObject.put(Constants.KEY_SMS_CODE,mSmsCode);
                }else{
                    jsonObject.put(Constants.KEY_ACCOUNT_EMAIL,mEmailAddress);
                } 
            }
			jsonAppendPhoneInfo(context,jsonObject);
		} catch (JSONException e) {
			Log.i(TAG, "getRegisterRequestJson(..)  JSONException:"+e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取加密后的trust注册请求json字符串
     * @param tableAccountInfo
     * @return
     */
    static String getRegisterTrustRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
    	mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
    	if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
    		mLoginPwdMD5Encrypt = DesUtil.MD5(mLoginPwd);
    	}else{
    		mLoginPwdMD5Encrypt = mLoginPwd;
    	}
    	Log.i(TAG, "getRegisterTrustRequestJson(..)  IS_ENCRYPTION:"+IS_ENCRYPTION+" IS_PASSWORD_MD5_ENCRYPT:"+IS_PASSWORD_MD5_ENCRYPT
    			+"phoneNo:"+mPhoneNumber+" loginPwd:"+mLoginPwd+" mLoginPwdMD5Encrypt:"+mLoginPwdMD5Encrypt);
    	try {
			jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);//密码使用md5加密
    		jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
			jsonAppendPhoneInfo(context,jsonObject);
		} catch (JSONException e) {
			Log.i(TAG, "getRegisterTrustRequestJson(..)  JSONException:"+e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取登录请求json字符串，暂未处理第三方登录
     * @param tableAccountInfo
     * @return
     */
    static String getLoginRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mAccountName = tableAccountInfo.get(Constants.KEY_ACCOUNT_USER_NAME);
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
		mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
		mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
		mThirdId = tableAccountInfo.get(Constants.KEY_ACCOUNT_THIRD_ID);  //第三方登录待后面确认
		mThirdType = tableAccountInfo.get(Constants.KEY_ACCOUNT_THIRD_TYPE);
		mSmsCode = tableAccountInfo.get(Constants.KEY_SMS_CODE);
		if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
			mLoginPwdMD5Encrypt = DesUtil.MD5(mLoginPwd);
		}else{
			mLoginPwdMD5Encrypt = mLoginPwd;
		}
		Log.i(TAG, "getLoginRequestJson(..) mAccountName:" +mAccountName+" nickName:"+mNickName+" mSmsCode:"+mSmsCode
				+" loginPwd:"+mLoginPwd+" mLoginPwdMD5Encrypt:"+mLoginPwdMD5Encrypt+" mPhoneNumber:"+mPhoneNumber);
    	try {
//    		if(null != mEmailAddress && !mEmailAddress.isEmpty()){
//    			jsonObject.put(Constants.KEY_ACCOUNT_EMAIL,mEmailAddress);
//    		}else if(null != mPhoneNumber && !mPhoneNumber.isEmpty()){
//    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
//    		}
    		/*jsonObject.put(Constants.KEY_ACCOUNT_USER_NAME,mAccountName); //统一的账号可以是手机号或邮箱
			jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);
			if(null != mSmsCode){
				jsonObject.put(Constants.KEY_SMS_CODE,mSmsCode);
			}
			jsonAppendPhoneInfo(context,jsonObject);*/

            if("2".equals(mThirdType)) {
                jsonObject.put(Constants.KEY_ACCOUNT_THIRD_ID,mThirdId); //统一的账号可以是手机号或邮箱
                jsonObject.put(Constants.KEY_ACCOUNT_THIRD_TYPE,mThirdType);
                Log.i(TAG,"put wechat args");
            } else {
                /*jsonObject.put(Constants.KEY_ACCOUNT_USER_NAME,mAccountName); //统一的账号可以是手机号或邮箱
                jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);
                jsonAppendPhoneInfo(context,jsonObject);*/
                jsonObject.put(Constants.KEY_ACCOUNT_USER_NAME,mAccountName); //统一的账号可以是手机号或邮箱
                jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);
                if(null != mSmsCode){
                    jsonObject.put(Constants.KEY_SMS_CODE,mSmsCode);
                }
                jsonAppendPhoneInfo(context,jsonObject);
                Log.i(TAG,"put normal args");
            }

		} catch (JSONException e) {
			Log.i(TAG, "getLoginRequestJson(..)  JSONException:"+e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取重置密码请求json字符串
     * @param tableAccountInfo
     * @return
     */
    static String getResetPwdRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
		mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
		mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
		mNewLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_NEW_PWD);
		if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
			mLoginPwdMD5Encrypt = DesUtil.MD5(mLoginPwd);
			mNewLoginPwdMD5Encrypt = DesUtil.MD5(mNewLoginPwd);
		}else{
			mLoginPwdMD5Encrypt = mLoginPwd;
			mNewLoginPwdMD5Encrypt = mNewLoginPwd;
		}
		Log.i(TAG, "getResetPwdRequestJson(..)  mToken:"+mToken+" mLoginPwd:"+mLoginPwd+" mNewLoginPwd:"+mNewLoginPwd
				+"mLoginPwdMD5Encrypt:"+mLoginPwdMD5Encrypt+" mNewLoginPwdMD5Encrypt:"+mNewLoginPwdMD5Encrypt);
		try {
			jsonObject.put(Constants.KEY_SERVER_TOKEN,mToken);
			jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);
			jsonObject.put(Constants.KEY_ACCOUNT_NEW_PWD,mNewLoginPwdMD5Encrypt);
			jsonAppendPhoneInfo(context,jsonObject);
		}catch (JSONException e) {
			Log.i(TAG, "getResetPwdRequestJson(..)  JSONException:"+e.toString());
			e.printStackTrace();
		}
		return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取短信验证码请求json字符串
     * @param tableAccountInfo  暂时需要手机号和请求类型
     * @return
     */
    static String getSendSmsVerifyCodeRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
        mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
    	mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);//Constants.SMS_MSG_TYPE_RETRIEVE;

    	Log.i(TAG, "getSmsVerifyCodeRequestJson(..) mToken:"+mToken+" mPhoneNumber:"+mPhoneNumber+" mOperateType:"+mOperateType
            +" mEmailAddress:"+mEmailAddress);
		try {
			if(!Constants.OPERATE_TYPE_REGISTER.equals(mOperateType) && !Constants.OPERATE_TYPE_RETRIEVE_PWD.equals(mOperateType)){  //非注册传递token
				jsonObject.put(Constants.KEY_SERVER_TOKEN,mToken);
			}
			if(null != mPhoneNumber) {
                jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
            }else{
                jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
            }
			jsonObject.put(Constants.KEY_OPERATE_TYPE,mOperateType);
			jsonAppendPhoneInfo(context,jsonObject);
		}catch (JSONException e) {
			Log.i(TAG, "getSmsVerifyCodeRequestJson(..)  JSONException:"+e.toString());
			e.printStackTrace();
		}
		return getEncryptString(jsonObject.toString());
    }
    /**
     * 通过短信验证码重置密码请求json字符串
     * @param tableAccountInfo
     * @return 返回加密后的json请求，密码md5加密
     */
    static String getRetrievePwdRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mRetrievePwdType = tableAccountInfo.get(Constants.KEY_RETRIEVE_PWD_TYPE);
    	if(!Constants.RETRIEVE_PWD_TYPE_PHONE.equals(mRetrievePwdType) && !Constants.RETRIEVE_PWD_TYPE_MAIL.equals(mRetrievePwdType)){  //
    		Log.i(TAG, "getRetrievePwdRequestJson() mRetrievePwdType is not phone or email return null!!!!");
    		return null;
    	}
    	mSmsCode = tableAccountInfo.get(Constants.KEY_SMS_CODE);
		mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
		mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
		mNewLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_NEW_PWD);
		if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
			mNewLoginPwdMD5Encrypt = DesUtil.MD5(mNewLoginPwd);
		}else{
			mNewLoginPwdMD5Encrypt = mNewLoginPwd;
		}
		Log.i(TAG, "getRetrievePwdRequestJson(..)  mPhoneNumber:"+mPhoneNumber+" mNewLoginPwd:"+mNewLoginPwd
			+" mNewLoginPwdMD5Encrypt:"+mNewLoginPwdMD5Encrypt+" mRetrievePwdType:"+mRetrievePwdType+" mEmailAddress:"+mEmailAddress);
		try {
			if(null != mPhoneNumber){
				jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
			}else{
				jsonObject.put(Constants.KEY_ACCOUNT_EMAIL,mEmailAddress);
			}
			jsonObject.put(Constants.KEY_ACCOUNT_NEW_PWD,mNewLoginPwdMD5Encrypt);
			jsonAppendPhoneInfo(context,jsonObject);
		}catch (JSONException e) {
			Log.i(TAG, "getRetrievePwdRequestJson(..) JSONException:"+e.toString());
			e.printStackTrace();
		}
		return getEncryptString(jsonObject.toString());
    }
    /**
     * 上传头像到服务器请求json字符串
     * @param tableAccountInfo
     * @return 只是gomeid的值加密，头像默认加密字符串，其他不加密
     */
    static String getUploadAvatarRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mAvatarType = tableAccountInfo.get(Constants.KEY_AVATAR_TYPE);
		mAvatar = tableAccountInfo.get(Constants.KEY_ACCOUNT_SERVER_AVATAR);
		Log.i(TAG, "getUploadAvatarRequestJson(..) mToken:"+mToken+" mAvatarType:"+mAvatarType+" mAvatar.length():"+mAvatar.length());
		try {
			jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
			jsonObject.put(Constants.KEY_AVATAR_TYPE, mAvatarType);
			jsonObject.put(Constants.KEY_ACCOUNT_SERVER_AVATAR, mAvatar);
			jsonAppendPhoneInfo(context,jsonObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "getUploadAvatarRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return jsonObject.toString();
    }
    /**
     * 获取验证手机号或邮箱是否合法的json请求
     * @param tableAccountInfo
     * @return
     */
    static String getVerifyPhoneOrMailRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);
    	Log.i(TAG, "getVerifyPhoneOrMailRequestJson(..) mPhoneNumber:"+mPhoneNumber+" mEmailAddress:"
    			+mEmailAddress+" mToken:"+mToken+" mOperateType:"+mOperateType);
    	try {
    		if(null != mPhoneNumber){
    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
    		}else if(null != mEmailAddress){
    			jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
    		}else{
    			Log.e(TAG, "getVerifyPhoneOrMailRequestJson(..) error!! not phone or email");
    			return null;
    		}
    		if(!Constants.OPERATE_TYPE_REGISTER.equals(mOperateType) && !Constants.OPERATE_TYPE_RETRIEVE_PWD.equals(mOperateType))  //非注册才需传入token
    			jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
			jsonObject.put(Constants.KEY_OPERATE_TYPE, mOperateType);
			jsonAppendPhoneInfo(context,jsonObject);
		} catch (JSONException e) {
			Log.i(TAG, "getVerifyPhoneOrMailRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取 验证短信验证码是否合法的json请求,包括手机号和安全手机号
     * @param tableAccountInfo
     * @return
     */
    static String getVerifySmsCodeRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
        mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
    	mSecurityPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
    	mAuthCode = tableAccountInfo.get(Constants.KEY_SMS_CODE);
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);
    	Log.i(TAG, "getVerifySmsCodeRequestJson(..) mPhoneNumber:"+mPhoneNumber+" mAuthCode:"+mAuthCode
    			+" mEmailAddress:"+mEmailAddress+" mToken:"+mToken+" mOperateType:"+mOperateType);
    	try {
			jsonObject.put(Constants.KEY_SMS_CODE, mAuthCode);
			jsonObject.put(Constants.KEY_OPERATE_TYPE, mOperateType);
    		if(!Constants.OPERATE_TYPE_REGISTER.equals(mOperateType) && !Constants.OPERATE_TYPE_RETRIEVE_PWD.equals(mOperateType)){
    			jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
    		}
			jsonAppendPhoneInfo(context,jsonObject);
            if(null != mEmailAddress){
                jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
            }
    		if(null != mPhoneNumber){
    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
    		}else if(null != mSecurityPhoneNumber){
    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mSecurityPhoneNumber);//安全号码使用，手机号的key：phoneNo
    		}

		} catch (JSONException e) {
			Log.i(TAG, "getVerifySmsCodeRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取发送邮件json请求
     * @param tableAccountInfo
     * @return
     */
    static String getSendEmailRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);
    	Log.i(TAG, "getSendEmailRequestJson(..) mEmailAddress:"+mEmailAddress+" mToken:"+mToken+" mOperateType:"+mOperateType);
    	try {
			jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
			jsonObject.put(Constants.KEY_OPERATE_TYPE, mOperateType);
			if(!Constants.OPERATE_TYPE_REGISTER.equals(mOperateType) && !Constants.OPERATE_TYPE_RETRIEVE_PWD.equals(mOperateType)){
				jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
			}
			jsonAppendPhoneInfo(context,jsonObject);
		} catch (JSONException e) {
			Log.i(TAG, "getSendEmailRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	Log.i(TAG, "getSendEmailRequestJson(..) jsonObject.toString():"+jsonObject.toString());
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取 修改个人信息json请求
     * @param tableAccountInfo
     * @return
     */
    static String getModifyAccountInfoRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mNickName = tableAccountInfo.get(Constants.KEY_ACCOUNT_NICK_NAME);
    	mSex = tableAccountInfo.get(Constants.KEY_ACCOUNT_SEX);
    	mBirthday = tableAccountInfo.get(Constants.KEY_ACCOUNT_BIRTHDAY);
    	mArea = tableAccountInfo.get(Constants.KEY_ACCOUNT_AREA);
    	Log.i(TAG, "getModifyAccountInfoRequestJson(..) mToken:"+mToken+" mNickName:"+mNickName+" mSex:"+mSex
    			+" mBirthday:"+mBirthday+" mArea:"+mArea);
    	try {
			jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
			if(null != mNickName)
				jsonObject.put(Constants.KEY_ACCOUNT_NICK_NAME, mNickName);
			if(null != mSex)
				jsonObject.put(Constants.KEY_ACCOUNT_SEX, mSex);
			if(null != mBirthday)
				jsonObject.put(Constants.KEY_ACCOUNT_BIRTHDAY, mBirthday);
			if(null != mArea)
				jsonObject.put(Constants.KEY_ACCOUNT_AREA, mArea);
			jsonAppendPhoneInfo(context,jsonObject);
		} catch (JSONException e) {
			Log.i(TAG, "getModifyAccountInfoRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 退出账号
     * @param tableAccountInfo
     * @return
     */
    static String getLoginOutRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
    	if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
			mLoginPwdMD5Encrypt = DesUtil.MD5(mLoginPwd);
		}else{
			mLoginPwdMD5Encrypt = mLoginPwd;
		}
    	Log.i(TAG, "getLoginOutRequestJson(..) mToken:"+mToken+" loginPwd:"+mLoginPwd+" mLoginPwdMD5Encrypt:"+mLoginPwdMD5Encrypt);
    	try {
			jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
			jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);
			jsonAppendPhoneInfo(context,jsonObject);
    	}catch (JSONException e) {
			Log.i(TAG, "getLoginOutRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 注销账号
     * @param tableAccountInfo
     * @return
     */
    static String getLoginOffRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
    	if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
			mLoginPwdMD5Encrypt = DesUtil.MD5(mLoginPwd);
		}else{
			mLoginPwdMD5Encrypt = mLoginPwd;
		}
    	Log.i(TAG, "getLoginOffRequestJson(..) mToken:"+mToken+" loginPwd:"+mLoginPwd+" mLoginPwdMD5Encrypt:"+mLoginPwdMD5Encrypt);
    	try {
			jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
			jsonObject.put(Constants.KEY_ACCOUNT_PWD,mLoginPwdMD5Encrypt);
			jsonAppendPhoneInfo(context,jsonObject);
    	}catch (JSONException e) {
			Log.i(TAG, "getLoginOffRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取邮箱状态
     * @param tableAccountInfo
     * @return
     */
    static String getEmailStatusRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	Log.i(TAG, "getEmailStatusRequestJson(..) mToken:"+mToken+" mEmailAddress:"+mEmailAddress);
    	try {
    		if(null != mToken)
    			jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
			jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
			jsonAppendPhoneInfo(context,jsonObject);
    	}catch (JSONException e) {
			Log.i(TAG, "getEmailStatusRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取邮箱激活状态 主要是找回密码调用
     * @param tableAccountInfo
     * @return
     */
    static String getEmailValidFlagRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	Log.i(TAG, "getEmailValidFlagRequestJson(..) 11 mOperateType:"+mOperateType+" mEmailAddress:"+mEmailAddress);
    	try {
			jsonObject.put(Constants.KEY_OPERATE_TYPE, mOperateType);
			jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
			jsonAppendPhoneInfo(context,jsonObject);
			Log.i(TAG, "getEmailValidFlagRequestJson(..) 222 jsonObject.toString():"+jsonObject.toString());
    	}catch (JSONException e) {
			Log.i(TAG, "getEmailValidFlagRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * json拼接手机信息
     * @param context
     * @param jsonObject
     */
    static void jsonAppendPhoneInfo(Context context,JSONObject jsonObject){
    	String model = android.os.Build.MODEL;
    	TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE); 
    	String imei = TelephonyMgr.getDeviceId(); 
    	if(null == imei){
    		imei = "000000000000000";
    	}
        String language = ActivityUtils.getCurrentLanguageStr(context);
        Log.i(TAG, "jsonAppendPhoneInfo() model:"+model+" imei:"+imei+" language:"+language);
    	try {
	    	jsonObject.put(Constants.KEY_ACCOUNT_MODEL,model);
	    	jsonObject.put(Constants.KEY_ACCOUNT_IMEI,imei);
            jsonObject.put(Constants.KEY_ACCOUNT_LANGUAGE,language);
    	}catch (JSONException e) {
			Log.i(TAG, "jsonAppendPhoneInfo  JSONException:" + e.toString());
			e.printStackTrace();
		}
    }
    /**
     * 绑定 包括：手机号/邮箱 安全手机号
     * @param tableAccountInfo
     * @return
     */
    static String getBindRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
    	mSecurityPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);
    	
    	Log.i(TAG, "getBindRequestJson(..) mToken:"+mToken+" mEmailAddress:"+mEmailAddress+" mPhoneNumber:"+mPhoneNumber
    			+" mSecurityPhoneNumber:"+mSecurityPhoneNumber+" mOperateType:"+mOperateType);
    	try {

    		jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
    		jsonObject.put(Constants.KEY_OPERATE_TYPE, mOperateType);
			jsonAppendPhoneInfo(context,jsonObject);
    		if(null != mEmailAddress){
    			jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
    		}else if(null != mPhoneNumber){
    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
    		}else if(null != mSecurityPhoneNumber){
    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mSecurityPhoneNumber);  //服务端手机号和安全手机使用同一个key：phoneNo
    		}else{
    			Log.e(TAG, "getBindRequestJson() error!!!");
    			return null;
    		}
    		
    	}catch (JSONException e) {
			Log.i(TAG, "getBindRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 解绑 ，包括：解绑手机号/邮箱 安全手机号
     * @param tableAccountInfo
     * @return
     */
    static String getUnbindRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
    	mSecurityPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
    	mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);
    	
    	Log.i(TAG, "getUnbindRequestJson(..) mToken:"+mToken+" mEmailAddress:"+mEmailAddress+" mPhoneNumber:"+mPhoneNumber
    			+" mOperateType:"+mOperateType);
    	try {

    		jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
    		jsonObject.put(Constants.KEY_OPERATE_TYPE, mOperateType);
			jsonAppendPhoneInfo(context,jsonObject);
    		if(null != mEmailAddress){
    			jsonObject.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
    		}else if(null != mPhoneNumber){
    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
    		}else if(null != mSecurityPhoneNumber){
    			jsonObject.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mSecurityPhoneNumber);//服务端手机号和安全手机使用同一个key：phoneNo
    		}else{
    			Log.e(TAG, "getUnbindRequestJson() error!!!");
    			return null;
    		}
    		
    	}catch (JSONException e) {
			Log.i(TAG, "getUnbindRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }
    /**
     * 获取验证密码或者短信验证码 请求json
     * @param context
     * @param tableAccountInfo
     * @return
     */
    static String getVerifyPwdOrSmsCodeRequestJson(Context context,HashMap<String,String> tableAccountInfo){
    	JSONObject jsonObject = new JSONObject();
    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	mSmsCode = tableAccountInfo.get(Constants.KEY_SMS_CODE);
    	mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
		
		if(IS_ENCRYPTION && IS_PASSWORD_MD5_ENCRYPT){
			mLoginPwdMD5Encrypt = DesUtil.MD5(mLoginPwd);
		}else{
			mLoginPwdMD5Encrypt = mLoginPwd;
		}
    	
    	Log.i(TAG, "getVerifyPwdOrSmsCodeRequestJson(..) mToken:"+mToken+" mSmsCode:"+mSmsCode+" mLoginPwd:"+mLoginPwd
    			+" mLoginPwdMD5Encrypt:"+mLoginPwdMD5Encrypt);
    	try {
    		jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
    		jsonObject.put(Constants.KEY_SMS_CODE, mSmsCode);
    		jsonObject.put(Constants.KEY_ACCOUNT_PWD, mLoginPwdMD5Encrypt);
			jsonAppendPhoneInfo(context,jsonObject);
    		
    	}catch (JSONException e) {
			Log.i(TAG, "getUnbindRequestJson  JSONException:" + e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonObject.toString());
    }

    /**
     * 验证token是否生效
     * @param context
     * @param tableAccountInfo
     * @return
     */
    static String getVerifyTokenRequestJson(Context context,HashMap<String,String> tableAccountInfo){
        JSONObject jsonObject = new JSONObject();
        mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
        Log.i(TAG, "getVerifyTokenRequestJson(..) mToken:"+mToken);
        try {
            jsonObject.put(Constants.KEY_SERVER_TOKEN, mToken);
            jsonAppendPhoneInfo(context,jsonObject);

        }catch (JSONException e) {
            Log.i(TAG, "getUnbindRequestJson  JSONException:" + e.toString());
            e.printStackTrace();
        }
        return getEncryptString(jsonObject.toString());
    }
    /**
     * 生成json结果，主要处理异常的情况,本地使用
     * @param resultCode
     * @param resultMsg
     * @return
     */
    static String getResultMessageJson(String resultCode,String resultMsg){
    	String jsonResult = null;
    	JSONObject jsonObject;
    	try {
			jsonObject = new JSONObject();
	    	jsonObject.put(Constants.KEY_SERVER_RESULT_CODE,resultCode);
	    	jsonObject.put(Constants.KEY_SERVER_RESULT_MSG,resultMsg);
	    	jsonResult = jsonObject.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "getResultMessageJson() error:"+e.toString());
			e.printStackTrace();
		}
    	return getEncryptString(jsonResult);//同服务器返回的结果，若加密则需要加密
    }
    /***
     * 从服务器下载头像图片到本地
     * @param serverPath
     * @param localPath
     */
    public static void downloadAvatarImage(final String serverImagePath,final String localImagePath){
		File dir = new File(Constants.HEAD_PORTRAIT_DIR_PATH);
		if (!dir.exists()){
			Log.i(TAG, "downloadAvatarImage mkdir");
			dir.mkdirs();
		}
    	File file = new File(localImagePath);
    	Log.i(TAG, "downloadAvatarImage serverImagePath:"+serverImagePath+" localImagePath:"+localImagePath+" file.exists():"+file.exists());
    	if(file.exists()){
    		Log.i(TAG, "downloadAvatarImage() image file already exists,do not download avatar Imgage!");
    	}else{
            //modify by yimin.zhu for bugid GM-OS3020
			/*sWorker.post(new Runnable() {
				@Override
				public void run() {*/
					try {    
                        Log.i(TAG, "start download head img to local!");
				        URL url = new URL(serverImagePath);     
				         // 打开连接     
				         URLConnection con = url.openConnection();  
				         //获得文件的长度  
				         int contentLength = con.getContentLength();  
				         Log.i(TAG, "downloadAvatarImage() image contentLength:"+contentLength);  
				         // 输入流     
				         InputStream is = con.getInputStream();    
				         // 1K的数据缓冲     
				         byte[] bs = new byte[1024];     
				         // 读取到的数据长度     
				         int len;     
				         // 输出的文件流     
				         OutputStream os = new FileOutputStream(localImagePath);     
				         // 开始读取     
				         while ((len = is.read(bs)) != -1) {     
				             os.write(bs, 0, len);     
				         }    
				         // 完毕，关闭所有链接     
				         os.close();    
				         is.close();  
				         Log.i(TAG, "finish() download head img to local!");     
	    			} catch (Exception e) {
	    					Log.i(TAG, "downloadAvatarImage error:"+e.toString());
	    			        e.printStackTrace();  
	    			}  
				//}
			//});
		}
    }
    /***
     * 根据url 和 json参数，获取服务器验证信息。
     * @param authUrl
     * @param jsonString
     * @return 从服务器返回json字符串
     */
    private static String authenticateFromServer(Context context,String authUrl,String jsonString,int...timeOut){
    	String result = null;
    	try {
            Log.i(TAG, "authenticateFromServer 11 authUrl:" + authUrl + " timeOut:" + timeOut);
            HttpURLConnection urlConn = null;
            URL url = new URL(authUrl);
            if (url.getProtocol().toUpperCase().equals("HTTPS")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);//HttpIgnoreSSL.DO_NOT_VERIFY);  
                urlConn = https;
            } else {
                urlConn = (HttpURLConnection) url.openConnection();
            }
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type", "application/json");
            if (null != timeOut && timeOut.length == 1) {
                Log.i(TAG, "authenticateFromServer 112  timeOut:" + timeOut[0]);
                urlConn.setReadTimeout(timeOut[0]);
                urlConn.setConnectTimeout(timeOut[0]);
            } else{
                urlConn.setReadTimeout(DEFAULT_REQUEST_TIMEOUT);
                urlConn.setConnectTimeout(DEFAULT_REQUEST_TIMEOUT);
            }
            urlConn.connect();
            Log.i(TAG, "authenticateFromServer 222");
            PrintWriter printWriter = new PrintWriter(urlConn.getOutputStream());
            printWriter.write(jsonString);//post的参数 xx=xx&yy=yy
            printWriter.flush();
            Log.d(TAG, "authenticateFromServer(..)  urlConn.getResponseCode() :" + urlConn.getResponseCode()+" jsonString:"+jsonString);
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	InputStream inptStream = urlConn.getInputStream();
            	result = streamToString(inptStream);
                Log.e(TAG, "authenticateFromServer(..) Post Phone info success !! mResult:" + result);
                
            } else {
                Log.e(TAG, "authenticateFromServer(..) Post phone info fail !!");
                return null;
            }
            urlConn.disconnect();
    	}catch(SocketTimeoutException e){
    		result = getResultMessageJson(Constants.SERVER_TIMEOUT_RESULT_CODE,context.getResources().getString(R.string.alert_network_connect_timeout));
            //ActivityUtils.alert(context, context.getResources().getString(R.string.alert_network_connect_timeout));
    		Log.e(TAG, "authenticateFromServer error:"+e.toString()+" result:"+result);
    		e.printStackTrace();
    	}catch(UnknownHostException e) { //如假网情况下找不到服务器也会在超时之后反馈，同网络超时返回resultcode
            result = getResultMessageJson(Constants.SERVER_TIMEOUT_RESULT_CODE,context.getResources().getString(R.string.alert_network_connect_timeout));
            //ActivityUtils.alert(context, context.getResources().getString(R.string.alert_network_connect_timeout));
            Log.e(TAG, "authenticateFromServer error:"+e.toString()+" result:"+result);
            e.printStackTrace();
        }catch (Exception e) {
            Log.e(TAG, "authenticateFromServer error:"+e.toString());
            e.printStackTrace();
        }
    	return result;
    }
    public static void trustAllHosts() {  
        // Create a trust manager that does not validate certificate chains  
        // Android use X509 cert  
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {  
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
                return new java.security.cert.X509Certificate[] {};  
            }  
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// TODO Auto-generated method stub
				
			}  
        } };  
  
        // Install the all-trusting trust manager  
        try {  
            SSLContext sc = SSLContext.getInstance("TLS");  
            sc.init(null, trustAllCerts, new java.security.SecureRandom());  
            HttpsURLConnection  
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    public static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            Log.e(TAG, "streamToString: 00");
            while ((len = is.read(buffer)) != -1) {
            	Log.e(TAG, "streamToString: 111");
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
    ////add by lilei end

}
