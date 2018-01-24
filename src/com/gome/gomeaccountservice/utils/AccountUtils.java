package com.gome.gomeaccountservice.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.gome.gomeaccountservice.AccountInfoActivity;
import com.gome.gomeaccountservice.Constants;
import com.gome.gomeaccountservice.LoginActivity;
import com.gome.gomeaccountservice.R;
import com.gome.gomeaccountservice.RegisterActivity;
import com.gome.gomeaccountservice.RetrievePwdActivity;

//账号相关操作处理
public class AccountUtils {
	static final String TAG = Constants.TAG_PRE+"AccountUtils";
	
	public static String ACTION_LOGIN_IN = "com.gome.gomeaccountservice.ACTION_GOME_ACCOUNT_LOGIN";   //发送登录 成功广播action
	public static String ACTION_UPDATE_INFO = "com.gome.gomeaccountservice.ACTION_GOME_ACCOUNT_UPDATE_INFO";   //发送账号数据更新广播
	public static String ACTION_LOGIN_OUT = "com.gome.gomeaccountservice.ACTION_GOME_ACCOUNT_LOGOUT"; //发送退出登录广播action
	public static final boolean NO_SERVER_TEST = false;
	public static final String  NO_SERVER_TEST_GOMEID = "100001";
	//public static final String DELETE_LOCAL_EMAIL_STRING = "delete";
	//public static final String DELETE_LOCAL_PHONE_STRING = "delete";
	//public static final String DELETE_LOCAL_SECURITY_PHONE_STRING = "delete";
	static HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	
	static String mToken = null;
	static String mEmailActiveStatus = null;  //服务端同步的 邮箱状态
	static String mEmailValidFlag = null;  //服务端同步的 找回密码邮箱激活状态
	static String mNickName = null;
	static String mEmailAddress = null;
	static String mPhoneNumber = null;
	static String mSecurityPhoneNumber = null;
	static String mGomeId = null;
	static String mRegisterType = null;
	static String mLoginPwd = null;
	static String mSex = null;
	static String mBirthday = null;
	static String mArea = null;
	static String mUserLevel = null;
	static String mLocalAvatarPath = null;
	static String mLocalAvatarUri = null; //本地设置头像的uri
	static String mServerAvatarPath = null;
	static String mOperateType = null;  //操作类型 如注册，绑定手机，解绑手机等
//	static String mUnbindToActiveEmail = null;     //解绑待激活邮箱，本地存储
	static String mUnbindEmailActiveState = null;  //解绑邮箱激活状态，本地不存储
	
	static String mImageType = Constants.HEAD_PORTRAIT_SUFFIX; //默认jpg
    //server 返回状态变量
	static String mServerResCode = null;
	static String mServerResMsg = null;
	/**
	 * 账号注册
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean register(Context context,HashMap<String,String> tableAccountInfo){
		Log.i(TAG, "register 111 NO_SERVER_TEST:"+NO_SERVER_TEST);
		if(NO_SERVER_TEST){  //仅本地测试用
			mGomeId = NO_SERVER_TEST_GOMEID;
			mToken = NO_SERVER_TEST_GOMEID;
			tableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
			persistAccountDetailToLocal(context, tableAccountInfo);
			return true;
		}
		
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String jsonRegisterResult = NetworkUtilities.authenticate(context, NetworkUtilities.AUTHENTICATE_TYPE_REGISTER,tableAccountInfo);
		if(!parseJsonResult(context, jsonRegisterResult)){ //服务器返回失败退出
			Log.i(TAG, "register 222 fail !!!!");
			return false;
		}
		return true;
	}
	/**
	 * 无验证码账号注册
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean registerTrust(Context context,HashMap<String,String> tableAccountInfo){
		Log.i(TAG, "registerTrust() 111 NO_SERVER_TEST:"+NO_SERVER_TEST);
		if(NO_SERVER_TEST){  //仅本地测试用
			mGomeId = NO_SERVER_TEST_GOMEID;
			mToken = NO_SERVER_TEST_GOMEID;
			tableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
			persistAccountDetailToLocal(context, tableAccountInfo);
			return true;
		}
		
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String jsonResult = NetworkUtilities.authenticate(context, NetworkUtilities.AUTHENTICATE_TYPE_REGISTER_TRUST,tableAccountInfo);
		if(!parseJsonResult(context, jsonResult)){ //服务器返回失败退出
			Log.i(TAG, "registerTrust() 222 fail !!!!");
			return false;
		}
		return true;
	}
	/**
	 * 账号登录
	 * @param context
	 * @param tableLoginAccountInfo
	 * @param serverModel   返回服务端错误代码
	 * @return
	 */
	public static boolean login(Context context,HashMap<String,String> tableLoginAccountInfo,ServerModel...serverModel){
		if(AccountUtils.NO_SERVER_TEST){  //仅本地测试用
			//mNickName = "test_li";
			HashMap<String,String> tableAccountInfo  = getAccountDetailFromLocal(context,mGomeId);
			String localPhone = "18217387473";//tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
			String localEmail = "3@qq.com";//tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
			String localPwd = "1";//tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
			String userName = "";
			
			mGomeId = NO_SERVER_TEST_GOMEID;
			mToken = NO_SERVER_TEST_GOMEID;
			mRegisterType = tableLoginAccountInfo.get(Constants.KEY_ACCOUNT_REGISTER_TYPE);
			mLoginPwd = tableLoginAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
			userName = tableLoginAccountInfo.get(Constants.KEY_ACCOUNT_USER_NAME);
			
			Log.i(TAG, "login() 111 localPhone:"+localPhone+" localEmail:"+localEmail+" localPwd:"+localPwd
					+" mRegisterType:"+mRegisterType+" userName:"+userName);
			if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){
				Log.i(TAG, "login() 222 localPhone:"+localPhone+" userName:"+userName+" localPwd:"+localPwd);
				mPhoneNumber = userName;
				if(!(userName.equals(localPhone) && mLoginPwd.equals(localPwd))){
					Log.i(TAG, "login() 222 login fail");
					ActivityUtils.alert(context, context.getResources().getString(R.string.alert_account_or_pwd_error));
					return false;
				}
			}else if(Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)){
				Log.i(TAG, "login() 222 localEmail:"+localEmail+" userName:"+userName+" localPwd:"+localPwd);
				mEmailAddress = userName;
				if(!(userName.equals(localEmail) && mLoginPwd.equals(localPwd))){
					Log.i(TAG, "login() 333 login fail");
					ActivityUtils.alert(context, context.getResources().getString(R.string.alert_account_or_pwd_error));
					return false;
				}
			}
			//loginOut(context, mGomeId);
			Account latestGomeAccount = AccountUtils.getLatestGomeAccount(context);
			Log.i(TAG, "login() NO_SERVER_TEST after call loginOut() latestGomeAccount:"+latestGomeAccount);
		}else{
			if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
				return false;
			}
			
			String jsonLoginResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_LOGIN, tableLoginAccountInfo);
			if(null == jsonLoginResult){
				ActivityUtils.alert(context, context.getResources().getString(R.string.alert_server_return_null_error));
				return false;
			}
			parseJson(jsonLoginResult);
			Log.i(TAG, "login() 111 mServerResCode："+mServerResCode+" mServerResMsg:"+mServerResMsg+" serverModel:"+serverModel
					+" mEmailActiveStatus:"+mEmailActiveStatus);
			if(!Constants.SERVER_SUCCESS_RESULT_CODE.equals(mServerResCode)){  //服务器返回失败退出
				if(null != serverModel && serverModel.length == 1){
					Log.i(TAG, "login()  22 setServerResCode");
					(serverModel[0]).setServerResCode(mServerResCode);
				}
				ActivityUtils.alert(context, mServerResMsg);
				return false;
			}
			//处理邮箱注册未激活的登录,正常调整激活界面
			if(Constants.SERVER_SUCCESS_RESULT_CODE.equals(mServerResCode) && Constants.EMAIL_STATUS_REGISTER_NOT_ACTIVED.equals(mEmailActiveStatus)){
				if(null != serverModel && serverModel.length == 1){
					Log.i(TAG, "login()  33 setEmailActiveStatus");
					(serverModel[0]).setEmailActiveStatus(mEmailActiveStatus);
				}
				return false;
			}
		}
        
		mRegisterType = tableLoginAccountInfo.get(Constants.KEY_ACCOUNT_REGISTER_TYPE);
        //保存账号信息到本地
        HashMap<String,String> tableAccountInfo = new HashMap<String, String>();
		tableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
		tableAccountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, mEmailActiveStatus);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_NICK_NAME, mNickName);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH, mServerAvatarPath); //若未设置头像信息，则mServerAvatarPath为null
		tableAccountInfo.put(Constants.KEY_ACCOUNT_SEX, mSex);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_BIRTHDAY, mBirthday);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_AREA, mArea);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER, mSecurityPhoneNumber);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
		if(null != mRegisterType){
			tableAccountInfo.put(Constants.KEY_ACCOUNT_REGISTER_TYPE, mRegisterType);
		}

		AccountUtils.persistAccountDetailToLocal(context,tableAccountInfo);
        if(null == mGomeId){
            Log.i(TAG, "login() step2 null == mGomeId return false");
            return false;
        }
        //登录成功，保存最近登录账号信息到本地
        String userName = tableLoginAccountInfo.get(Constants.KEY_ACCOUNT_USER_NAME);
        AccountUtils.persistRecentAccountToShare(context,userName);

        //登录完成添加Account记录,account name为mGomeId
        //mLoginPwd = tableLoginAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
        AccountManager accountManager= AccountManager.get(context);
        final Account account = new Account(mGomeId, Constants.ACCOUNT_TYPE);
        boolean result = accountManager.addAccountExplicitly(account,mToken,null); //本地account密码存储服务端token

        String pwd = accountManager.getPassword(account);
        Log.i(TAG, "login() addAccount result:"+result+" mToken:"+mToken+" pwd:"+pwd+" mRegisterType:"+mRegisterType
                +" mGomeId:"+mGomeId);
        
        Log.i(TAG,"login() before sendBroadcast mLocalAvatarPath:"+mLocalAvatarPath+" mEmailAddress:"+mEmailAddress
            +" mPhoneNumber:"+mPhoneNumber+" mNickName:"+mNickName);
        
		Intent intent = new Intent(ACTION_LOGIN_IN);  //发送登录成功广播
        intent.putExtra(Constants.KEY_SERVER_TOKEN,mToken);
        intent.putExtra(Constants.KEY_ACCOUNT_GOME_ID,mGomeId);
        intent.putExtra(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
        intent.putExtra(Constants.KEY_ACCOUNT_EMAIL,mEmailAddress);
        intent.putExtra(Constants.KEY_ACCOUNT_NICK_NAME,mNickName);//
        intent.putExtra(Constants.KEY_LOCAL_AVATAR_PATH,mLocalAvatarPath);
		context.sendBroadcast(intent);
		return true;
	}
	/**
	 * 获取登录状态
	 * @param context
	 * @return 若已登录状态 return true
	 */
	public static boolean getLoginState(Context context){
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(context); //测试
    	if(null != gomeAccount){
            final AccountManager am = AccountManager.get(context);  //AccountManager用来判断已经登录过的场景
            final String password = am.getPassword(gomeAccount);
            Log.i(TAG, "getLoginState() gomeAccount.name:"+gomeAccount.name+" password:"+password);
            if (password != null) {
            	return true;
            }
    	}
		return false;
	}
	/**
	 * 重置密码
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean resetPwd(Context context,HashMap<String,String> tableAccountInfo){
    	if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
    	
		String jsonResetPwdResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_RESET_PWD, tableAccountInfo);
		Log.i(TAG, "resetPwd() 111 jsonResetPwdResult："+jsonResetPwdResult);
		if(!AccountUtils.parseJsonResult(context, jsonResetPwdResult)){ //服务器返回失败退出
			Log.i(TAG, "resetPwd() 222 fail !!!!");
			return false;
		}

		return true;
	}

	/**
	 * 微信登录
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean checkWechatAccountBinded(Context context,HashMap<String,String> tableAccountInfo){
		String jsonWechatResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_LOGIN, tableAccountInfo);
		Log.i(TAG, "login wechat："+jsonWechatResult);
		if(null == jsonWechatResult){
            ActivityUtils.alert(context, context.getResources().getString(R.string.alert_server_return_null_error));
    		return false;
    	}
    	JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonWechatResult);
			//modify for bugid GMOS-4242 by yimin.zhu on 2017.8.28
	    	//mPhoneNumber = jsonObject.getString(Constants.KEY_ACCOUNT_PHONE_NUMBER);
	    	//通过token值判断微信是否具有绑定信息
	    	mPhoneNumber = jsonObject.getString(Constants.KEY_SERVER_TOKEN);
	    	Log.i(TAG,"mPhoneNumber:" + mPhoneNumber);
	    	if(mPhoneNumber!=null) {
	    		return true;
	    	} else {
	    		return false;
	    	}
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "parseJsonResult error!!");
			//ActivityUtils.alert(context, "parseJsonResult error:"+e.toString());
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 找回密码
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean retrievePwd(Context context,HashMap<String,String> tableAccountInfo){
    	if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
    	
		String jsonRetrievePwdResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_RETRIEVE_PWD, tableAccountInfo);
		Log.i(TAG, "retrievePwd() 111 jsonRetrievePwdResult："+jsonRetrievePwdResult);
		if(!AccountUtils.parseJsonResult(context, jsonRetrievePwdResult)){ //服务器返回失败退出
			Log.i(TAG, "retrievePwd 222 fail !!!!");
			return false;
		}
		//添加找回密码成功退出账号功能
		Account gomeAccount = AccountUtils.getLatestGomeAccount(context); //测试
		if(null != gomeAccount){
			String pwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_NEW_PWD);
			Log.i(TAG, "retrievePwd() 333 gomeAccount.name:"+gomeAccount.name+" pwd:"+pwd);
			loginOut(context, gomeAccount.name,pwd);
		}
		return true;
	}
	/**
	 * 验证密码或验证码
	 * @param context
	 * @param tableAccountInfo
	 * @param simpleMode 若为true则只是验证，不做提前的处理
	 * @return
	 */
	public static boolean verifyPwdOrSmsCodeFromServer(Context context,HashMap<String,String> tableAccountInfo,boolean...simpleMode){
		Log.i(TAG, "verifyPwdOrSmsCodeFromServer() ");
		if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
        if(null != simpleMode &&  0 != simpleMode.length){
            if (!ActivityUtils.checkNetworkSimple(context)) { //后台检查网络是否可用,无需弹出WiFi是否可用弹出框
                return false;
            }
        }else {
            if (!ActivityUtils.checkNetwork(context)) { //检查网络是否可用
                return false;
            }
        }
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_VERIFY_PWD_OR_SMS_CODE, tableAccountInfo);
		Log.i(TAG, "verifyPwdOrSmsCodeFromServer() 111 jsonResult："+jsonResult+" simpleMode:"+simpleMode);
		if(null != simpleMode &&  0 != simpleMode.length){
			if(!AccountUtils.parseJsonResultSimple(context, jsonResult)){ //服务器返回失败退出
				Log.i(TAG, "verifyPwdOrSmsCodeFromServer 222 fail !!!!");
				return false;
			}
		}else{
			if(!AccountUtils.parseJsonResult(context, jsonResult)){ //服务器返回失败退出
				Log.i(TAG, "verifyPwdOrSmsCodeFromServer 333 fail !!!!");
				return false;
			}
		}
		return true;
	}
	/**
	 * 获取邮箱状态,保存邮箱状态到本地
	 * @param context
	 * @param tableAccountInfo
	 * @param isSaveLocal   是否本地存储数据
	 * @return 正常激活状态返回true
	 */
	public static boolean verifyEmailStatusFromServer(Context context,HashMap<String,String> tableAccountInfo,boolean isSaveLocal){
		Log.i(TAG, "verifyEmailStatusFromServer() ");
		if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_GET_EMAIL_STATUS, tableAccountInfo);
		Log.i(TAG, "verifyEmailStatusFromServer() 111 jsonResult："+jsonResult);
		if(!AccountUtils.parseJsonResult(context, jsonResult)){ //服务器返回失败退出
			Log.i(TAG, "getEmailStatus 222 fail !!!!");
			return false;
		}
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonResult);
			if(jsonObject.has(Constants.KEY_EMAIL_ACTIVE_STATUS)){
				mEmailActiveStatus = jsonObject.getString(Constants.KEY_EMAIL_ACTIVE_STATUS);
			}else{
				mEmailActiveStatus = null;
			}
			mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE);
	    	mGomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
	    	mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
	    	String localEmailActiveStatus = tableAccountInfo.get(Constants.KEY_EMAIL_ACTIVE_STATUS);//本地邮箱激活状态
	    	//若解绑邮箱激活成功，即：邮箱状态从 待解绑状态=》null状态，则需要删除本地邮箱信息，和待解绑邮箱信息 
	    	if(Constants.EMAIL_STATUS_TO_UNBIND.equals(localEmailActiveStatus) && null == mEmailActiveStatus){
	    		mUnbindEmailActiveState = Constants.UNBIND_EMAIL_ACTIVE_TRUE;
	    	}else{
	    		mUnbindEmailActiveState = null;
	    	}
	    	Log.i(TAG, "verifyEmailStatusFromServer() mOperateType:"+mOperateType+" mEmailAddress:"+mEmailAddress+" mEmailActiveStatus:"+mEmailActiveStatus
	    			+" localEmailActiveStatus:"+localEmailActiveStatus+" mUnbindEmailActived:"+mUnbindEmailActiveState+" mGomeId:"+mGomeId
	    			+" isSaveLocal:"+isSaveLocal);
	    	if(null != mGomeId){ //若存在gomeid则保存状态到本地，只有mOperateType为绑定或解绑才会调用此处
		    	HashMap<String,String> accountInfo = new HashMap<String, String>();
		    	accountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
		    	accountInfo.put(Constants.KEY_OPERATE_TYPE, mOperateType);  //解绑返回状态成功,json结果返回的邮箱为null，则需要删除邮箱状态的节点
		    	accountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, mEmailActiveStatus); //
		    	
		    	if(null != mUnbindEmailActiveState){	//这两个变量主要处理解绑邮箱
			    	accountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);  
			    	accountInfo.put(Constants.KEY_UNBIND_EMAIL_ACTIVE_STATE, mUnbindEmailActiveState);
		    	}
		    	if(isSaveLocal)
		    		AccountUtils.persistAccountDetailToLocal(context,accountInfo);

		    	//若是绑定且状态为正常则返回成功
                if(Constants.OPERATE_TYPE_BIND.equals(mOperateType)&& Constants.EMAIL_STATUS_NORMAL.equals(mEmailActiveStatus)){
		    		return true;
		    	}
		    	//若是解绑，且邮箱状态为null，则返回成功
				if(Constants.OPERATE_TYPE_UNBIND.equals(mOperateType) && null == mEmailActiveStatus){
					return true;
				}
	    	}else{  //注册，或找回密码无gomeid，直接返回
	    		//邮箱注册未激活返回false
	    		if(Constants.EMAIL_STATUS_REGISTER_NOT_ACTIVED.equals(mEmailActiveStatus) || null == mEmailActiveStatus){
	    			return false;
	    		}
	    		return true;
	    	}
		} catch (JSONException e) {
			Log.i(TAG, "verifyEmailStatusFromServer() error!! e："+e.toString());
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 获取邮箱激活状态，保存邮箱激活状态到本地，目前只有找回密码邮箱激活状态
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean verifyEmailValidFlagFromServer(Context context,HashMap<String,String> tableAccountInfo){
		if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_GET_EMAIL_VALID_FLAG, tableAccountInfo);
		Log.i(TAG, "verifyEmailValidFlagFromServer() 111 jsonResult："+jsonResult);
		if(!AccountUtils.parseJsonResult(context, jsonResult)){ //服务器返回失败退出
			Log.i(TAG, "verifyEmailValidFlagFromServer 222 fail !!!!");
			return false;
		}
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonResult);
			Log.i(TAG, "verifyEmailValidFlagFromServer() 333");
			if(jsonObject.has(Constants.KEY_EMAIL_VALID_FLAG)){
				mEmailValidFlag = jsonObject.getString(Constants.KEY_EMAIL_VALID_FLAG);
				Log.i(TAG, "verifyEmailValidFlagFromServer() 444 mEmailValidFlag:"+mEmailValidFlag);
				if(Constants.EMAIL_VALID_FLAG_ACTIVE.equals(mEmailValidFlag)){ //若找回密码已经激活
					Log.i(TAG, "verifyEmailValidFlagFromServer() 555");
					return true;
				}
			}
		}catch (JSONException e) {
			Log.i(TAG, "verifyEmailValidFlagFromServer() error!! e："+e.toString());
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 退出登录,暂不处理服务端请求
	 * @param context
	 * @param gomeId  想要退出的账号id
     * @param pwd 若密码不为null则需要服务端退出,否则只是本地退出
	 * @return
	 */
	public static boolean loginOut(Context context,String gomeId,String pwd){
		Log.i(TAG, "loginOut() 11 ");
		AccountManager mAccountManager = AccountManager.get(context);
		final Context mContext = context;
		AccountManagerCallback<Boolean> callback = new AccountManagerCallback<Boolean>() {
			@Override
			public void run(AccountManagerFuture<Boolean> arg0) {
				// TODO Auto-generated method stub
				try {
					Log.v(TAG, "loginOut() callback.run() 111 arg0.isDone():"+arg0.isDone()+" arg0.getResult():"+arg0.getResult());
					Intent intent = new Intent(ACTION_LOGIN_OUT);  //发送退出登录广播
					mContext.sendBroadcast(intent);
				} catch (OperationCanceledException e) {
					// TODO Auto-generated catch block
					Log.v(TAG, "loginOut() callback.run() 222 error!!");
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					Log.v(TAG, "loginOut() callback.run() 333 error!!");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v(TAG, "loginOut() callback.run() 444 error!!");
					e.printStackTrace();
				}
			}
		};

		final AccountManager am = AccountManager.get(context); 
		Account [] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
		Log.e(TAG, "loginOut() 22  accounts.length:"+(accounts ==null ?"null":accounts.length)+" gomeId:"+gomeId+" pwd:"+pwd);
		if(0 == accounts.length){
			ActivityUtils.alert(context, "loginOut() error! no account gomeId:"+gomeId);
			return false;
		}
		for(int i=0;i<accounts.length;i++){
			Account account =  accounts[i];
			String jsonResult = null;
			if(null != pwd){//不是所有情况都需要服务端退出
				HashMap<String,String> tableAccountInfo = new HashMap<String, String>();
				tableAccountInfo.put(Constants.KEY_SERVER_TOKEN, am.getPassword(account)); //token存储在密码中
				tableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, pwd);
				jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_LOGIN_OUT, tableAccountInfo);
			}
			Log.e(TAG, "loginOut() 33 account.name:"+account.name+" gomeId:"+gomeId+" am.getPassword:"+am.getPassword(account)
					+" jsonResult:"+jsonResult);
			mAccountManager.removeAccount(account, callback, null);   //删除accountmanager中的账号
			deleteLocalAccountDetail(context,account.name);  //删除sharepreference中存储的账号详细信息
		}
		return true;

	}
	/**
	 * 注销账号
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean loginOff(Context context,HashMap<String,String> tableAccountInfo){
		Log.i(TAG, "loginOff()");
    	if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_LOGIN_OFF, tableAccountInfo);
		Log.i(TAG, "loginOff() 111 jsonResult："+jsonResult);
		if(!AccountUtils.parseJsonResult(context, jsonResult)){ //注销账号失败
			Log.i(TAG, "loginOff() 222 fail !!!!");
			return false;
		}else{  //注销账号成功退出登录
			String gomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
			Log.i(TAG, "loginOff() 333 gomeId:"+gomeId);
			loginOut(context,gomeId,null);//不需要服务端退出，只需本地退出
		}

		return true;
	}
	//上传图片到服务器
	public static boolean uploadLocalImageToServer(Context context,Bitmap bitMap,HashMap<String,String> tableAccountInfo){
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String gomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
		String imageType = tableAccountInfo.get(Constants.KEY_AVATAR_TYPE);
		String strUri = tableAccountInfo.get(Constants.KEY_LOCAL_AVATAR_URI);
		Log.i(TAG, "uploadLocalImageToSever() gomeId:"+gomeId+" imageType:"+imageType+" strUri:"+strUri);
		if(AccountUtils.NO_SERVER_TEST){  //本地测试用
			String fileName = gomeId+"."+imageType;
			BitmapUtils.bitmap2File(bitMap,fileName);
			if(null != gomeId){ //设置头像 路径到本地缓存
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, gomeId);
				mTableAccountInfo.put(Constants.KEY_LOCAL_AVATAR_PATH,Constants.HEAD_PORTRAIT_DIR_PATH+fileName);
				AccountUtils.persistAccountDetailToLocal(context,mTableAccountInfo);
			}
			return true;
		}
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_UPLOAD_AVATAR, tableAccountInfo);
		if(null != jsonResult){
			if(AccountUtils.parseJsonResult(context,jsonResult)){  //若上传头像成功
				if(null != gomeId){ //设置头像 路径到本地缓存

					AccountUtils.parseJson(jsonResult);  //解析完整json的数据，上传头像只返回了avatarPath
					String fileName = gomeId+"."+imageType;
					BitmapUtils.bitmap2File(bitMap,fileName); //保存头像信息

					mTableAccountInfo.clear();
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, gomeId);
					mTableAccountInfo.put(Constants.KEY_LOCAL_AVATAR_PATH,Constants.HEAD_PORTRAIT_DIR_PATH+fileName);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH, mServerAvatarPath);//保存上传后的图片地址
					mTableAccountInfo.put(Constants.KEY_LOCAL_AVATAR_URI,strUri);
					AccountUtils.persistAccountDetailToLocal(context,mTableAccountInfo);
				}
				//downloadServerImageToLocal(); //本地缓存不直接从服务器下载
				return true;
			}
		}else{
			Log.i(TAG, "uploadLocalImageToSever error!! jsonResult is null");
		}
		return false;
	}
	/**
	 * 绑定  手机号、邮箱、安全手机号 到服务器和本地
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean bindFromServer(Context context,HashMap<String,String> tableAccountInfo){
		Log.i(TAG, "bindFromServer() 00");
		if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_BIND, tableAccountInfo);
		Log.i(TAG, "bindFromServer() 111 jsonResult："+jsonResult);
		if(AccountUtils.parseJsonResult(context, jsonResult)){ //服务器返回成功
			persistAccountDetailToLocal(context, tableAccountInfo); //将绑定的账号信息存储到本地，即使邮箱未激活也保存邮箱信息
			return true;
		}else{
			Log.i(TAG, "bindFromServer 222 fail !!!!");
		}
		return false;
	}
	/**
	 * 解绑  手机号、邮箱、安全手机号 到服务器和本地
	 * @param context
	 * @param tableAccountInfo
	 * @return
	 */
	public static boolean unbindFromServer(Context context,HashMap<String,String> tableAccountInfo){
		if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_UNBIND, tableAccountInfo);
		Log.i(TAG, "unbindFromServer() 111 jsonResult："+jsonResult);
		if(AccountUtils.parseJsonResult(context, jsonResult)){ //服务器返回成功
			persistAccountDetailToLocal(context, tableAccountInfo); 
			return true;
		}else{
			Log.i(TAG, "unbindFromServer 222 fail !!!!");
		}
		return false;
	}

	
    /**
     * 解析json账号详细信息数据,  
     * 注意：调用此接口，全局变量会被重新初始化，然后从json中赋值
     * @param jsonString
     */
    static void parseJson(String jsonString){
    	try {
    		initVarible();
			JSONObject jsonObject = new JSONObject(jsonString);
			
			if(jsonObject.has(Constants.KEY_SERVER_TOKEN))
				mToken = jsonObject.getString(Constants.KEY_SERVER_TOKEN);
			if(jsonObject.has(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH))
				mServerAvatarPath = jsonObject.getString(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH);  //获取服务器头像路径key
			if(jsonObject.has(Constants.KEY_ACCOUNT_NICK_NAME))
				mNickName = jsonObject.getString(Constants.KEY_ACCOUNT_NICK_NAME);
			if(jsonObject.has(Constants.KEY_ACCOUNT_EMAIL))
				mEmailAddress = jsonObject.getString(Constants.KEY_ACCOUNT_EMAIL);  
			if(jsonObject.has(Constants.KEY_EMAIL_ACTIVE_STATUS))
				mEmailActiveStatus = jsonObject.getString(Constants.KEY_EMAIL_ACTIVE_STATUS);
			if(jsonObject.has(Constants.KEY_ACCOUNT_PHONE_NUMBER))
				mPhoneNumber = jsonObject.getString(Constants.KEY_ACCOUNT_PHONE_NUMBER);
			if(jsonObject.has(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER))
				mSecurityPhoneNumber = jsonObject.getString(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
			if(jsonObject.has(Constants.KEY_ACCOUNT_GOME_ID))
				mGomeId = jsonObject.getString(Constants.KEY_ACCOUNT_GOME_ID);
			if(jsonObject.has(Constants.KEY_ACCOUNT_SEX))
				mSex = jsonObject.getString(Constants.KEY_ACCOUNT_SEX);
			if(jsonObject.has(Constants.KEY_ACCOUNT_BIRTHDAY))
				mBirthday = jsonObject.getString(Constants.KEY_ACCOUNT_BIRTHDAY);
			if(jsonObject.has(Constants.KEY_ACCOUNT_AREA))
				mArea = jsonObject.getString(Constants.KEY_ACCOUNT_AREA);
			if(jsonObject.has(Constants.KEY_SERVER_RESULT_CODE))
				mServerResCode = jsonObject.getString(Constants.KEY_SERVER_RESULT_CODE);
			if(jsonObject.has(Constants.KEY_SERVER_RESULT_MSG))
				mServerResMsg = jsonObject.getString(Constants.KEY_SERVER_RESULT_MSG);
	    	
			//mRegisterType = jsonObject.getString(Constants.KEY_ACCOUNT_REGISTER_TYPE);
			//mLoginPwd = jsonObject.getString(Constants.KEY_ACCOUNT_PWD);
			//mUserLevel = jsonObject.getString(Constants.KEY_ACCOUNT_USER_LEVEL);
			
			Log.i(TAG, "~~parseJson() mToken:"+mToken+" mServerAvatarPath:"+mServerAvatarPath+" mNickName:"+mNickName
				+" mEmailAddress:"+mEmailAddress+" mEmailActiveStatus:"+mEmailActiveStatus
				+" mPhoneNumber:"+mPhoneNumber+" mSecurityPhoneNumber:"+mSecurityPhoneNumber+" mGomeId:"+mGomeId+" mSex:"
				+mSex+" mBirthday:"+mBirthday+" mArea:"+mArea+" mServerResCode:"+mServerResCode+" mServerResMsg:"+mServerResMsg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "parseJson() error!!");
			e.printStackTrace();
		}  
    }
	private static void initVarible(){
		Log.i(TAG, "initVarible()");
		mToken = null;
		mEmailActiveStatus = null;
		mNickName = null;
		mEmailAddress = null;
		mPhoneNumber = null;
		mSecurityPhoneNumber = null;
		mGomeId = null;
		mRegisterType = null;
		mLoginPwd = null;
		mSex = null;
		mBirthday = null;
		mArea = null;
		mUserLevel = null;
		mLocalAvatarPath = null;
		mLocalAvatarUri = null;
		mServerAvatarPath = null;
		mOperateType = null;  //操作类型 如注册，绑定手机，解绑手机等
		mUnbindEmailActiveState = null;
			
		mImageType = Constants.HEAD_PORTRAIT_SUFFIX; //默认jpg

		mServerResCode = null;
		mServerResMsg = null;
    }
	/**
     * 应用退出时候调用，删除本地缓存账号详细信息,清除 头像所在目录所有文件
     */
    public static void deleteLocalAccountDetail(Context context,String accountName){
    	String fileName = Constants.SHAREPREFEENCE_ACCOUNT_INFO_PREFIX+accountName+".xml";
    	Log.i(TAG, "deleteLocalAccountDetail() fileName:"+fileName);
    	String dirPath = "/data/data/"+context.getPackageName().toString()+"/shared_prefs/";
        File file= new File(dirPath,fileName);
        if(file.exists()){
            file.delete();
            Log.i(TAG, "deleteLocalAccountDetail delete file success ,file:"+dirPath+fileName);
            //alert(context, "退出成功！已经成功删除文件："+dirPath+fileName);
       }else{
    	   Log.i(TAG, "deleteLocalAccountDetail file not exists ,file:"+dirPath+fileName);
    	    //alert(context, "退出成功！不存在文件："+dirPath+fileName);
       }
       //deleteCachedImages();  //退出应用暂不删除缓存头像信息。
    }
    /**
     * 清除缓存头像目录
     */
    public static void deleteCachedImages(){
        File dir = new File(Constants.HEAD_PORTRAIT_DIR_PATH); 
        File[] files = dir.listFiles();
        Log.i(TAG, "deleteCachedImages() "+Constants.HEAD_PORTRAIT_DIR_PATH+" listFiles length is:"+files.length);
        for(int i=0;i<files.length;i++){
     	   File image = files[i];
     	   image.delete();
        }
    }
    /***
     * 根据mGomeId获取对应名称的SharedPreferences文件，获取账号信息
     * @param context
     * @param accountName
     * @return
     */
    public static HashMap<String,String> getAccountDetailFromLocal(Context context,String accountName){
    	initVarible();
    	Log.i(TAG, "getAccountDetailFromShare() accountName:"+accountName);
    	HashMap<String,String> tableAccountInfo = new HashMap<String, String>();
    	SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNT_INFO_PREFIX+accountName, Context.MODE_PRIVATE);
    	mToken = sharedPreferences.getString(Constants.KEY_SERVER_TOKEN, null);
    	mNickName = sharedPreferences.getString(Constants.KEY_ACCOUNT_NICK_NAME, null);
    	mEmailAddress = sharedPreferences.getString(Constants.KEY_ACCOUNT_EMAIL, null);
    	mEmailActiveStatus = sharedPreferences.getString(Constants.KEY_EMAIL_ACTIVE_STATUS, null);
    	
    	mPhoneNumber = sharedPreferences.getString(Constants.KEY_ACCOUNT_PHONE_NUMBER, null);
    	mSecurityPhoneNumber = sharedPreferences.getString(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER, null);
    	mGomeId = sharedPreferences.getString(Constants.KEY_ACCOUNT_GOME_ID, null);
    	mRegisterType = sharedPreferences.getString(Constants.KEY_ACCOUNT_REGISTER_TYPE, null);
    	mLoginPwd = sharedPreferences.getString(Constants.KEY_ACCOUNT_PWD, null);
    	mSex = sharedPreferences.getString(Constants.KEY_ACCOUNT_SEX, null);
    	mBirthday = sharedPreferences.getString(Constants.KEY_ACCOUNT_BIRTHDAY, null);
    	mArea = sharedPreferences.getString(Constants.KEY_ACCOUNT_AREA, null);
    	//mUserLevel = sharedPreferences.getString(Constants.KEY_ACCOUNT_USER_LEVEL, null);
    	mLocalAvatarPath = sharedPreferences.getString(Constants.KEY_LOCAL_AVATAR_PATH, null);
    	mLocalAvatarUri = sharedPreferences.getString(Constants.KEY_LOCAL_AVATAR_URI, null);
    	mServerAvatarPath = sharedPreferences.getString(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH, null);
		Log.i(TAG, "getAccountDetailFromShare() mToken:"+mToken+" mNickName:"+mNickName+" mEmailAddress:"+mEmailAddress+" mEmailActiveStatus:"
			+mEmailActiveStatus+" mPhoneNumber:"+mPhoneNumber+" mSecurityPhoneNumber:"+mSecurityPhoneNumber
			+" mGomeId:"+mGomeId+" mRegisterType:"+mRegisterType+" mLoginPwd:"+mLoginPwd+" mSex:"+mSex+" mUserLevel:"+mUserLevel+" mBirthday:"
			+mBirthday+" mArea:"+mArea+" mLocalAvatarPath:"+mLocalAvatarPath+" mLocalAvatarUri:"+mLocalAvatarUri
			+" mServerAvatarPath:"+mServerAvatarPath);
		
		tableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_NICK_NAME, mNickName);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
		tableAccountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, mEmailActiveStatus);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER, mSecurityPhoneNumber);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_REGISTER_TYPE, mRegisterType);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, mLoginPwd);  //正常为空
		tableAccountInfo.put(Constants.KEY_ACCOUNT_SEX, mSex);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_BIRTHDAY, mBirthday);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_AREA, mArea);
		tableAccountInfo.put(Constants.KEY_LOCAL_AVATAR_PATH, mLocalAvatarPath);
		tableAccountInfo.put(Constants.KEY_LOCAL_AVATAR_URI, mLocalAvatarUri);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH, mServerAvatarPath);
		//tableAccountInfo.put(Constants.KEY_ACCOUNT_USER_LEVEL, mUserLevel);

    	return tableAccountInfo;
    }
    /***
     * 根据mGomeId获取对应名称的SharedPreferences文件，保存账号信息,保存头像信息到本地
     * @param context
     * @param tableAccountInfo
     */
    public static void persistAccountDetailToLocal(Context context,HashMap<String,String> tableAccountInfo){
    	initVarible();
    	if(null != tableAccountInfo){
    		
			mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);  
			mNickName = tableAccountInfo.get(Constants.KEY_ACCOUNT_NICK_NAME);
			mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
			mEmailActiveStatus = tableAccountInfo.get(Constants.KEY_EMAIL_ACTIVE_STATUS);
			
			mUnbindEmailActiveState = tableAccountInfo.get(Constants.KEY_UNBIND_EMAIL_ACTIVE_STATE); //解绑邮件激活状态
			
			mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
			mSecurityPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
			mGomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
			mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
			mSex = tableAccountInfo.get(Constants.KEY_ACCOUNT_SEX);
			mBirthday = tableAccountInfo.get(Constants.KEY_ACCOUNT_BIRTHDAY);
			mArea = tableAccountInfo.get(Constants.KEY_ACCOUNT_AREA);
			mOperateType = tableAccountInfo.get(Constants.KEY_OPERATE_TYPE); //操作类型
			mRegisterType = tableAccountInfo.get(Constants.KEY_ACCOUNT_REGISTER_TYPE); //注册类型 本地使用
			
			mServerAvatarPath = tableAccountInfo.get(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH);
			Log.i(TAG,"persistAccountDetailToLocal 000 mLocalAvatarPath:"+mLocalAvatarPath);
			mLocalAvatarPath = tableAccountInfo.get(Constants.KEY_LOCAL_AVATAR_PATH);
			Log.i(TAG,"persistAccountDetailToLocal 111 mLocalAvatarPath:"+mLocalAvatarPath);
			mLocalAvatarUri = tableAccountInfo.get(Constants.KEY_LOCAL_AVATAR_URI);
			if(null == mLocalAvatarPath && null != mServerAvatarPath){ //若还未从服务器下载图片，则执行下载图片动作
				String serverImagePath = mServerAvatarPath;//服务器传递全路径
				String tempImageType = mServerAvatarPath.substring(mServerAvatarPath.lastIndexOf("."));
                if(tempImageType.indexOf("?")>0){ ///处理url包含这种格式结尾的后缀 123.png?8
                    mImageType = tempImageType.substring(0, tempImageType.indexOf("?"));
                }else{
                    mImageType = tempImageType;
                }
				mLocalAvatarPath = Constants.HEAD_PORTRAIT_DIR_PATH+mGomeId+mImageType;  //本地路径为/sdcard/gomeaccount/gomeid.jpg
				NetworkUtilities.downloadAvatarImage(serverImagePath,mLocalAvatarPath);
			}
			
			Log.i(TAG, "persistAccountDetailToShare() mToken:"+mToken+" mNickName:"+mNickName+" mEmailAddress:"+mEmailAddress+" mEmailActiveStatus:"+mEmailActiveStatus
			 +" mPhoneNumber:"+mPhoneNumber+" mSecurityPhoneNumber:"+mSecurityPhoneNumber+" mGomeId:"+mGomeId+" mSex:"+mSex
			 +" mBirthday:"+mBirthday+" mArea:"+mArea+" mOperateType:"+mOperateType+" mUnbindEmailActived:"+mUnbindEmailActiveState
			 +" mServerAvatarPath:"+mServerAvatarPath+" mLocalAvatarPath:"+mLocalAvatarPath+" mLocalAvatarUri:"+mLocalAvatarUri
			 +" mRegisterType:"+mRegisterType);
        	SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNT_INFO_PREFIX+mGomeId, Context.MODE_PRIVATE);
        	Editor editor = sharedPreferences.edit();
        	if(null != mToken){              //token
        		editor.putString(Constants.KEY_SERVER_TOKEN,mToken);
        	}
        	if(null != mEmailActiveStatus){  //邮箱状态
        		editor.putString(Constants.KEY_EMAIL_ACTIVE_STATUS,mEmailActiveStatus);
            }else if(null == mEmailActiveStatus && Constants.OPERATE_TYPE_UNBIND.equals(mOperateType)) {
                //若邮箱状态为空，且操作类型为解绑的删除邮箱状态节点,解绑手机的时候也会调用，不冲突
        		Log.i(TAG, "persistAccountDetailToLocal()  remove KEY_EMAIL_ACTIVE_STATUS");
        		editor.remove(Constants.KEY_EMAIL_ACTIVE_STATUS);
        	}
        	if(null != mNickName){
        		editor.putString(Constants.KEY_ACCOUNT_NICK_NAME,mNickName);
        	}
        	if(null != mEmailAddress){
        		//解绑邮箱激活成功,删除本地邮箱、删除本地解绑待激活邮箱
        		if(null != mUnbindEmailActiveState && Constants.UNBIND_EMAIL_ACTIVE_TRUE.equals(mUnbindEmailActiveState)){
        			Log.i(TAG, "persistAccountDetailToShare()  delete KEY_ACCOUNT_EMAIL");
        			editor.remove(Constants.KEY_ACCOUNT_EMAIL);
        		}else{
        			editor.putString(Constants.KEY_ACCOUNT_EMAIL,mEmailAddress);
        		}
        	}
        	if(null != mPhoneNumber){
        	    if(Constants.OPERATE_TYPE_UNBIND.equals(mOperateType)){ //若是解绑手机号
        			Log.i(TAG, "persistAccountDetailToShare() delete KEY_ACCOUNT_PHONE_NUMBER");
        			editor.remove(Constants.KEY_ACCOUNT_PHONE_NUMBER);
        		}else{
        			editor.putString(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
        		}
        	}
        	if(null != mSecurityPhoneNumber){
        		if(Constants.OPERATE_TYPE_UNBIND_SECURITY_PHONE.equals(mOperateType)){//若是解绑安全手机号
        			Log.i(TAG, "persistAccountDetailToShare() delete KEY_ACCOUNT_SECURITY_PHONE_NUMBER");
        			editor.remove(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
        		}else{
        			editor.putString(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER,mSecurityPhoneNumber);
        		}
        	}
        	if(null != mGomeId) //
        		editor.putString(Constants.KEY_ACCOUNT_GOME_ID,mGomeId);
        	if(null != mRegisterType) //注册类型，服务端不保存
        		editor.putString(Constants.KEY_ACCOUNT_REGISTER_TYPE,mRegisterType);
        	if(NO_SERVER_TEST){  //若是无服务器测试，则本地保存密码
	        	if(null != mLoginPwd)  //本地不保存密码
	        		editor.putString(Constants.KEY_ACCOUNT_PWD,mLoginPwd);
        	}
        	if(null != mSex)     	//性别
        		editor.putString(Constants.KEY_ACCOUNT_SEX,mSex);
        	if(null != mBirthday) 	//生日
        		editor.putString(Constants.KEY_ACCOUNT_BIRTHDAY,mBirthday);
        	if(null != mArea)    	//地区
        		editor.putString(Constants.KEY_ACCOUNT_AREA,mArea);
        	if(null != mUserLevel)  //用户等级
        		editor.putString(Constants.KEY_ACCOUNT_USER_LEVEL,mUserLevel);
        	if(null != mLocalAvatarPath)  //保存本地头像存储路径
        		editor.putString(Constants.KEY_LOCAL_AVATAR_PATH,mLocalAvatarPath);
        	if(null != mLocalAvatarUri)
        		editor.putString(Constants.KEY_LOCAL_AVATAR_URI,mLocalAvatarUri);
        	if(null != mServerAvatarPath)  //保存服务器头像存储目录路径
        		editor.putString(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH,mServerAvatarPath);
        	
        	editor.commit();
    	}else{
    		Log.i(TAG, "persistAccountDetailToShare error!!");
    	}

        //获取广播传递的邮箱参数
        if(Constants.OPERATE_TYPE_BIND.equals(mOperateType) && Constants.EMAIL_STATUS_NORMAL.equals(mEmailActiveStatus)){
            mEmailAddress = getEmailFromLocal(context,mGomeId);//绑定邮箱成功
        }else if(Constants.OPERATE_TYPE_UNBIND.equals(mOperateType) && null == mEmailActiveStatus && null == mPhoneNumber){
            mEmailAddress = "";   //解绑邮箱成功,操作类型是解绑且，邮箱激活状态为null，手机参数为null(排除手机解绑),广播传递空串
        }else{
            //mEmailAddress = null; //默认邮箱需要传递解决登录广播未传递邮箱的问题
        }

        //获取广播传递的手机参数
        if(Constants.OPERATE_TYPE_UNBIND.equals(mOperateType) && null != mPhoneNumber){
            mPhoneNumber = ""; //解绑手机号,广播传递空串
        }
        Log.i(TAG,"~~~persistAccountDetailToLocal() before sendBroadcast ## mLocalAvatarPath:"+mLocalAvatarPath
            +" mEmailAddress:"+mEmailAddress+" mPhoneNumber:"+mPhoneNumber);
		Intent intent = new Intent(ACTION_UPDATE_INFO);  //发送账号信息更新的广播
        intent.putExtra(Constants.KEY_SERVER_TOKEN,mToken);
        intent.putExtra(Constants.KEY_ACCOUNT_GOME_ID,mGomeId);
        intent.putExtra(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
        intent.putExtra(Constants.KEY_ACCOUNT_EMAIL,mEmailAddress);
        intent.putExtra(Constants.KEY_ACCOUNT_NICK_NAME,mNickName);//
        intent.putExtra(Constants.KEY_LOCAL_AVATAR_PATH,mLocalAvatarPath);
		context.sendBroadcast(intent);
    }
    
    public static void persistAccountPwdToShare(Context context,String gomeId,String pwd){
    	Log.i(TAG, "persistAccountPwdToShare gomeId:"+gomeId+" pwd:"+pwd);
    	SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNT_INFO_PREFIX+mGomeId, Context.MODE_PRIVATE);
    	Editor editor = sharedPreferences.edit();
    	editor.putString(Constants.KEY_ACCOUNT_PWD,pwd);
    	editor.commit();
    }
    //保存最新登录的用户名到本地
    public static void persistRecentAccountToShare(Context context,String accountName){
    	Log.i(TAG, "persistRecentAccountToShare()");
    	SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNTS, Context.MODE_PRIVATE);
    	Editor editor = sharedPreferences.edit();
    	editor.putString(Constants.KEY_LOCAL_RECENT_ACCOUNT_NAME,accountName);
    	editor.commit();
    }
    //从本地获取最新登录的用户名
    public static String getRecentAccountFromShare(Context context){
    	Log.i(TAG, "getRecentAccountFromShare()");
    	SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNTS, Context.MODE_PRIVATE);
    	return sharedPreferences.getString(Constants.KEY_LOCAL_RECENT_ACCOUNT_NAME,null); 
    }
    /**
     * 保存账号本地启动模式
     * @param context
     * @param accountName
     */
    public static void persistLocalAccountStartModeToShare(Context context,String startMode){
    	Log.i(TAG, "persistLocalAccountStartModeToShare()");
    	SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNTS, Context.MODE_PRIVATE);
    	Editor editor = sharedPreferences.edit();
    	editor.putString(Constants.KEY_LOCAL_ACCOUNT_START_MODE,startMode);
    	editor.commit();
    }

    /**
     * 保存账号本地启动模式,和登录跳转界面
     * @param context
     * @param startMode
     * @param loginFeedback
     */
    public static void persistLocalAccountStartModeAndFeedbackToShare(Context context,String startMode,String loginFeedback){
        Log.i(TAG, "persistLocalAccountStartModeAndFeedbackToShare()");
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNTS, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_LOCAL_ACCOUNT_START_MODE,startMode);
        editor.putString(Constants.KEY_START_MODE_LOGIN_FEEDBACK,loginFeedback);
        editor.commit();
    }
    /**
     * 获取账号本地启动模式
     * @param context
     * @param accountName
     */
    public static String getLocalAccountStartModeFromShare(Context context){
    	Log.i(TAG, "getLocalAccountStartModeFromShare()");
    	SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNTS, Context.MODE_PRIVATE);
    	return sharedPreferences.getString(Constants.KEY_LOCAL_ACCOUNT_START_MODE,null); 
    }

    /**
     * 清除本地缓存启动模式相关数据
     * @param context
     */
    public static void clearLocalAccountStartMode(Context context){
        Log.i(TAG, "clearLocalAccountStartMode()");
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNTS, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.remove(Constants.KEY_LOCAL_ACCOUNT_START_MODE);
        editor.remove(Constants.KEY_START_MODE_LOGIN_FEEDBACK);
        editor.commit();
    }
    /**
     * 获取当前启动模式登陆后需要跳转的界面
     * @param context
     * @return
     */
    public static String getLocalAccountStartModeFeedbackFromShare(Context context){
        Log.i(TAG, "getLocalAccountStartModeFeedbackFromShare()");
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNTS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.KEY_START_MODE_LOGIN_FEEDBACK,null);
    }
    /**
     * 获取本机国美账号
     * @param context
     * @return
     */
    public static Account getLatestGomeAccount(Context context){
    	Account latestAccount = null;
    	final AccountManager am = AccountManager.get(context); 
    	Account [] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
    	Log.e(TAG, "getLatestGomeAccount()  accounts.length:"+(accounts ==null ?"null":accounts.length));
    	for(int i=0;i<accounts.length;i++){
    		Account account =  accounts[i];
    		latestAccount = account;
    		Log.e(TAG, "getLatestGomeAccount()  i:"+i+" account.name:"+account.name+" account.type:"+account.type);
    	}
    	return latestAccount;
    }
    /////////////////////////////////本地缓存数据end
    ////////////////////////////////向服务器请求begin
    /**
     * 向服务器请求发送短信验证码
     * @param context
     * @param tableAccountInfo
     * @return
     */
    public static boolean sendSmsVerifyCodeFromServer(Context context,HashMap<String,String> tableAccountInfo){
    	if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_SEND_SMS_VERIFY_CODE, tableAccountInfo);
		Log.i(TAG, "sendSmsVerifyCodeFromServer() jsonResult："+jsonResult);

		if(parseJsonResult(context,jsonResult)){
			return true;
		}
		return false;
	}
    /**
     * 验证手机号或邮箱是否可用，合法并且未被使用
     * @param context
     * @param tableAccountInfo
     * @return
     */
    public static boolean verifyPhoneOrEmailValidFromServer(Context context,HashMap<String,String> tableAccountInfo){
    	if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
		String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_VERIFY_PHONE_OR_MAIL_VALID, tableAccountInfo);
		Log.i(TAG, "verifyPhoneOrEmailValidFromServer() jsonResult："+jsonResult+" context:"+context);
		if(parseJsonResult(context,jsonResult)){
			return true;
		}

    	return false;
    }
    /**
     * 验证短信验证码是否正确
     * @param context
     * @param tableAccountInfo
     * @return
     */
    public static boolean verifySmsCodeValidFromServer(Context context,HashMap<String,String> tableAccountInfo){
    	if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
		
    	String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_VERIFY_SMS_CODE_VALID, tableAccountInfo);
		Log.i(TAG, "verifySmsCodeValidFromServer() jsonResult："+jsonResult);
		if(parseJsonResult(context,jsonResult)){
			return true;
		}
    	return false;
    }

    /**
     * 判断token是否可用，用户是否登录状态,主要在外部接口调用界面或判断登录状态调用
     * @param context
     * @param tableAccountInfo
     * @param simpleMode 若为true则只是验证，不做提前的处理,比如token失效跳转等,但是需要做本地退出
     * @return
     */
    public static boolean verifyTokenFromServer(Context context,HashMap<String,String> tableAccountInfo,ServerModel serverModel,boolean...simpleMode){
        if(NO_SERVER_TEST){  //测试返回true
            return true;
        }
        //不判断网络状态，默认只有网络可用才可以调用
        String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_VERIFY_TOKEN, tableAccountInfo);
        Log.i(TAG, "verifyTokenFromServer() 111 jsonResult："+jsonResult+" simpleMode:"+simpleMode);
        if(null != simpleMode &&  0 != simpleMode.length){
            if(AccountUtils.parseJsonResultSimple(context, jsonResult)) { //服务器返回失败退出
                Log.i(TAG, "verifyTokenFromServer 222");
                return true;
            }
            if(null != serverModel){ //simpleMode 为true 若验证token失败返回resultcode
                Log.i(TAG, "verifyTokenFromServer()  22 mServerResCode:"+mServerResCode);
                serverModel.setServerResCode(mServerResCode);
            }
        }else {
            if (parseJsonResult(context, jsonResult)) {
                Log.i(TAG, "verifyTokenFromServer 333");
                return true;
            }
        }
        return false;
    }
    /**
     * 发送邮件
     * @param context
     * @param tableAccountInfo
     * @param serverModel
     * @return
     */
    public static boolean sendEmailFromServer(Context context,HashMap<String,String> tableAccountInfo,ServerModel...serverModel){
    	if(NO_SERVER_TEST){  //测试返回true
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
    	
    	String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_SEND_EMAIL, tableAccountInfo);
		Log.i(TAG, "sendEmailFromServer() jsonResult："+jsonResult);
        //解析验证类型
        String verifyType = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonResult);
            verifyType = jsonObject.getString(Constants.KEY_VERIFY_TYPE);
        } catch (JSONException e) {
            Log.i(TAG, "sendEmailFromServer error:"+e.toString());
            e.printStackTrace();
        }
        if(null != verifyType){
            if(null != serverModel && serverModel.length == 1){
                Log.i(TAG, "sendEmailFromServer()  setVerifyType:"+verifyType);
                (serverModel[0]).setVerifyType(verifyType);
            }
        }
		if(parseJsonResult(context,jsonResult)){
			return true;
		}
    	return false;
    }
    /**
     * 更新个人信息到 服务器和本地存储
     * @param context
     * @param tableAccountInfo
     * @return
     */
    public static boolean modifyAccountInfoFromServer(Context context,HashMap<String,String> tableAccountInfo){
    	if(NO_SERVER_TEST){  //测试返回true
    		persistAccountDetailToLocal(context, tableAccountInfo);
    		return true;
    	}
		if(!ActivityUtils.checkNetwork(context)){ //检查网络是否可用
			return false;
		}
    	
    	String jsonResult = NetworkUtilities.authenticate(context,NetworkUtilities.AUTHENTICATE_TYPE_MODIFY_ACCOUNT_INFO, tableAccountInfo);
		Log.i(TAG, "modifyAccountInfoFromServer() jsonResult："+jsonResult);
		if(parseJsonResult(context,jsonResult)){
			persistAccountDetailToLocal(context, tableAccountInfo);
			return true;
		}
    	return false;
    }
    //
    ////////////////////////////////向服务器请求end
    /**
     * 解析服务器返回的json结果，用来判定是否成功,若失败弹出失败消息
     * @param jsonResult
     * @return 是否请求成功
     */
    private static boolean parseJsonResult(Context context,String jsonResult){
    	if(null == jsonResult){
    		//ActivityUtils.alert(context, "server error!! server return null");//alert_server_return_null_error
    		ActivityUtils.alert(context, context.getResources().getString(R.string.alert_server_return_null_error));
    		return false;
    	}
    	JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonResult);
	    	mServerResCode = jsonObject.getString(Constants.KEY_SERVER_RESULT_CODE);
	    	mServerResMsg = jsonObject.getString(Constants.KEY_SERVER_RESULT_MSG);
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "parseJsonResult error!!");
			ActivityUtils.alert(context, "parseJsonResult error:"+e.toString());
			e.printStackTrace();
		}
		if(Constants.SERVER_TOKEN_ERROR_CODE.equals(mServerResCode)){//token失效退出并跳转登录界面
			Log.i(TAG, "parseJsonResult() SERVER_TOKEN_ERROR_CODE");
			doServerTokenError(context);
		}
		Log.i(TAG, "parseJsonResult() mServerResCode:"+mServerResCode+" mServerResMsg:"+mServerResMsg
				+" SERVER_TOKEN_ERROR_CODE?"+Constants.SERVER_TOKEN_ERROR_CODE.equals(mServerResCode));
		if(null != mServerResMsg && !mServerResMsg.isEmpty()){ //只要有message消息就弹出toast
			ActivityUtils.alert(context,mServerResMsg);
		}
		if(Constants.SERVER_SUCCESS_RESULT_CODE.equals(mServerResCode)){
			return true;
		}
		return false;
    }
    /**
     * 只解析json结果，不做额外处理
     * @param context
     * @param jsonResult
     * @return
     */
    private static boolean parseJsonResultSimple(Context context,String jsonResult){
    	Log.i(TAG, "parseJsonResultSimple()");
    	if(null == jsonResult){
    		return false;
    	}
    	JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonResult);
	    	mServerResCode = jsonObject.getString(Constants.KEY_SERVER_RESULT_CODE);
	    	mServerResMsg = jsonObject.getString(Constants.KEY_SERVER_RESULT_MSG);
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "parseJsonResultSimple error!!");
			e.printStackTrace();
		}
		if(Constants.SERVER_SUCCESS_RESULT_CODE.equals(mServerResCode)){
			return true;
		}
        if(Constants.SERVER_TOKEN_ERROR_CODE.equals(mServerResCode)){//token失效退出不跳转登录界面
            Log.i(TAG, "parseJsonResultSimple() SERVER_TOKEN_ERROR_CODE");
            doServerTokenErrorSimple(context);
        }
		return false;
    }
    /**
     * 处理token失效，账号退出并跳转登录界面
     * @param context
     */
    private static void doServerTokenError(Context context){
		Account gomeAccount = AccountUtils.getLatestGomeAccount(context);
		Log.i(TAG, "doServerTokenError() gomeAccount:"+gomeAccount);
		if(null != gomeAccount){
			loginOut(context, gomeAccount.name,null);
			Intent i = new Intent(context,LoginActivity.class);
			context.startActivity(i);
		}
    }
    /**
     * 处理token失效，账号退出不跳转登录界面
     * @param context
     */
    private static void doServerTokenErrorSimple(Context context){
        Account gomeAccount = AccountUtils.getLatestGomeAccount(context);
        Log.i(TAG, "doServerTokenErrorSimple() gomeAccount:"+gomeAccount);
        if(null != gomeAccount) {
            loginOut(context, gomeAccount.name, null);
        }
    }
    /////////////发送短信相关 end

    /**
     * 判断是否有安全码
     * @param context
     */
    public static boolean isSaftyNumberExist(Context context,String accountName){
		SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNT_INFO_PREFIX+accountName, Context.MODE_PRIVATE);
    	String saftyNumber = sharedPreferences.getString(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER, null);
    	if(saftyNumber == null) {
    		Log.i(TAG,"saftyNumber not existed");
    		return false;
    	} else {
    		Log.i(TAG,"saftyNumber existed ：" + saftyNumber);
    		return true;
    	}
    }

    /**
     * 获取安全码
     * @param context
     */
    public static String getSaftyNumber(Context context,String accountName){
		SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNT_INFO_PREFIX+accountName, Context.MODE_PRIVATE);
    	return sharedPreferences.getString(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER, null);
    }

    /**
     * 获取本地邮箱
     * @param context
     * @param accountName
     * @return
     */
    public static String getEmailFromLocal(Context context,String accountName){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHAREPREFEENCE_ACCOUNT_INFO_PREFIX+accountName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.KEY_ACCOUNT_EMAIL, null);
    }
}
