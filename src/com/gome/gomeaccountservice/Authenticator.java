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


import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.ServerModel;

import java.io.IOException;
import java.util.HashMap;

/**
 * This class is an implementation of AbstractAccountAuthenticator for
 * authenticating accounts in the com.example.android.samplesync domain. The
 * interesting thing that this class demonstrates is the use of authTokens as
 * part of the authentication process. In the account setup UI, the user enters
 * their username and password. But for our subsequent calls off to the service
 * for syncing, we want to use an authtoken instead - so we're not continually
 * sending the password over the wire. getAuthToken() will be called when
 * SyncAdapter calls AccountManager.blockingGetAuthToken(). When we get called,
 * we need to return the appropriate authToken for the specified account. If we
 * already have an authToken stored in the account, we return that authToken. If
 * we don't, but we do have a username and password, then we'll attempt to talk
 * to the sample service to fetch an authToken. If that fails (or we didn't have
 * a username/password), then we need to prompt the user - so we create an
 * AuthenticatorActivity intent and return that. That will display the dialog
 * that prompts the user for their login information.
 */
class Authenticator extends AbstractAccountAuthenticator {

    /** The tag used to log to adb console. **/
    private static final String TAG = Constants.TAG_PRE+"Authenticator";
    static String mServerToken = null;
	static String mNickName = null;
	static String mEmailAddress = null;
	static String mPhoneNumber = null;
	static String mGomeId = null;
	static String mRegisterType = null;
	static String mLoginPwd = null;
	static String mSex = null;
	static String mUserLevel = null;
    private String mLocalAvatarPath = null; //本地头像路径
	
    // Authentication Service context
    private final Context mContext;
	final Handler mHandler = new Handler(); 
	
    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
            String authTokenType, String[] requiredFeatures, Bundle options) {
        Log.v(TAG, "addAccount() authTokenType:"+authTokenType);
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse response, Account account, Bundle options) {
        Log.v(TAG, "confirmCredentials()");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.v(TAG, "editProperties()");
        throw new UnsupportedOperationException();
    }
//测试

  /***
   * 从本地缓存获取账号信息到全局变量
   * @param context  
   * @param accountName 账号名为 gomeid
   */
  void getAccountDetailToGlobalVarible(Context context,String accountName,String serverToken){  
		HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(context, accountName);
        if(null != serverToken){
            mServerToken = serverToken;
        }else {
            mServerToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
        }
		mNickName = tableAccountInfo.get(Constants.KEY_ACCOUNT_NICK_NAME);  
		mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);  
		mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
		mGomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
		mRegisterType = tableAccountInfo.get(Constants.KEY_ACCOUNT_REGISTER_TYPE);
		mLoginPwd = tableAccountInfo.get(Constants.KEY_ACCOUNT_PWD);
		mSex = tableAccountInfo.get(Constants.KEY_ACCOUNT_SEX);
		mUserLevel = tableAccountInfo.get(Constants.KEY_ACCOUNT_USER_LEVEL);
		mLocalAvatarPath = tableAccountInfo.get(Constants.KEY_LOCAL_AVATAR_PATH);
  }
  /***
   *  将全局变量信息存储到bundle
   * @param bundle
   */
  void setGlobalVaribleToBundle(Bundle result){
	  	result.putString(Constants.KEY_SERVER_TOKEN,mServerToken);
		result.putString(Constants.KEY_ACCOUNT_NICK_NAME,mNickName);
		result.putString(Constants.KEY_ACCOUNT_EMAIL,mEmailAddress);
		result.putString(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
		result.putString(Constants.KEY_ACCOUNT_GOME_ID,mGomeId);
		result.putString(Constants.KEY_ACCOUNT_REGISTER_TYPE,mRegisterType);
		//result.putString(Constants.KEY_ACCOUNT_PWD,mLoginPwd);
		result.putString(Constants.KEY_ACCOUNT_SEX,mSex);
		result.putString(Constants.KEY_ACCOUNT_USER_LEVEL,mUserLevel);
		result.putString(Constants.KEY_LOCAL_AVATAR_PATH,mLocalAvatarPath);
  }
//
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle loginOptions) throws NetworkErrorException {
        Log.v(TAG, "***** getAuthToken() 111 authTokenType:"+authTokenType);
        
        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
 
        
        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity panel.
        Bundle result = null;
        if(authTokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE)){ //获取登录状态
            result = getLoginState(mContext,authTokenType);
        }else if(authTokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_GET_LOCAL_LOGIN_STATE)) {//获取本地登录状态
            result = getLocalLoginState(mContext,authTokenType);
        }else if(authTokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_LOGIN)){	 //或登录界面intent
        	result = login(mContext,authTokenType,response);
        }else if(authTokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_LOGIN_INFO)){//获取账号界面intent
        	result = loginInfo(mContext,authTokenType,response);
        }else if(authTokenType.equals(Constants.KEY_AUTH_TOKEN_TYPE_VERIFY_PWD)){ //验证密码
			result = verifyPwd(mContext,authTokenType,loginOptions);//verifyPwd
        }
        return result;
    }
	/**
	 * 获取登录状态
	 * @param context
	 * @param authTokenType
	 * @return
	 */
    Bundle getLoginState(Context context,String authTokenType){
    	final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        result.putString(Constants.PARAM_AUTHTOKEN_TYPE, authTokenType);
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(context); //测试
    	if(null != gomeAccount){
            final AccountManager am = AccountManager.get(context);  //AccountManager用来判断已经登录过的场景
            final String password = am.getPassword(gomeAccount);  //此时的密码是登录时服务器返回的token
            result.putString(AccountManager.KEY_ACCOUNT_NAME, gomeAccount.name);
            Log.i(TAG, "getLoginState() 11 gomeAccount.name:"+gomeAccount.name+" password:"+password+" currentThread:"+ Thread.currentThread().getName());
            if (password != null) {
                if(ActivityUtils.checkNetworkSimple(context)) { //网络可用
                    HashMap<String,String> tableAccountInfo =  new HashMap<String, String>();
                    tableAccountInfo.put(Constants.KEY_SERVER_TOKEN,password);
                    final ServerModel serverModel = new ServerModel();
                    if(AccountUtils.verifyTokenFromServer(context,tableAccountInfo,serverModel,true)) { //先向服务端请求
                        Log.i(TAG,"getLoginState() 22");
                        result.putString(AccountManager.KEY_AUTHTOKEN, Constants.KEY_SERVER_AUTHEN_SUCCESS);//	
                        getAccountDetailToGlobalVarible(context, gomeAccount.name,password);//获取账号详细信息到变量
                        setGlobalVaribleToBundle(result);//将账号详细变量信息更新到bundle
                    }else {  //若token验证失败则返回resultcode
                        String serverResCode = serverModel.getServerResCode();
                        Log.i(TAG,"getLoginState() 223 serverResCode:"+serverResCode);
                        result.putString(Constants.KEY_SERVER_RESULT_CODE, serverResCode);
                    }
                }else{  //网络不可用
                    Log.i(TAG,"getLoginState() 333");
                    result.putString(AccountManager.KEY_AUTHTOKEN, Constants.KEY_SERVER_AUTHEN_SUCCESS);//网络未连接
                    getAccountDetailToGlobalVarible(context, gomeAccount.name,password);//获取账号详细信息到变量
                    setGlobalVaribleToBundle(result);//将账号详细变量信息更新到bundle
                }
            }
        }
    	return result;
    }
    /**
     * 获取本地登录状态
     * @param context
     * @param authTokenType
     * @return
     */
    Bundle getLocalLoginState(Context context,String authTokenType){
        Log.i(TAG, "getLocalLoginState() 00");
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        result.putString(Constants.PARAM_AUTHTOKEN_TYPE, authTokenType);
        Account gomeAccount = AccountUtils.getLatestGomeAccount(context); //测试
        if(null != gomeAccount){
            final AccountManager am = AccountManager.get(context);  //AccountManager用来判断已经登录过的场景
            final String password = am.getPassword(gomeAccount);  //此时的密码是登录时服务器返回的token
            result.putString(AccountManager.KEY_ACCOUNT_NAME, gomeAccount.name);
            Log.i(TAG, "getLocalLoginState() 11 gomeAccount.name:"+gomeAccount.name+" password:"+password+" currentThread:"+ Thread.currentThread().getName());
            if (password != null) {
                Log.i(TAG,"getLocalLoginState() 22");
                result.putString(AccountManager.KEY_AUTHTOKEN, Constants.KEY_SERVER_AUTHEN_SUCCESS);//	
                getAccountDetailToGlobalVarible(context, gomeAccount.name,password);//获取账号详细信息到变量
                setGlobalVaribleToBundle(result);//将账号详细变量信息更新到bundle
            }
        }
        return result;
    }
    /**
     * 登录
     * @param context
     * @param authTokenType
     * @param response
     * @return
     */
    Bundle login(Context context,String authTokenType,AccountAuthenticatorResponse response){
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putString(Constants.PARAM_AUTHTOKEN_TYPE, authTokenType);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }
    Bundle loginInfo(Context context,String authTokenType,AccountAuthenticatorResponse response){
        final Intent intent = new Intent(context, AccountInfoActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putString(Constants.PARAM_AUTHTOKEN_TYPE, authTokenType);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    Bundle verifyPwd(Context context,String authTokenType,Bundle bundle){
		mServerToken =  bundle.getString(Constants.KEY_SERVER_TOKEN);
		mLoginPwd =  bundle.getString(Constants.KEY_ACCOUNT_PWD);
		Log.i(TAG, "verifyPwd() mServerToken:"+mServerToken+" mLoginPwd:"+mLoginPwd);
		HashMap<String,String> tableAccountInfo =  new HashMap<String, String>();
		tableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mServerToken);
		tableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, mLoginPwd);
		boolean verifyResult = AccountUtils.verifyPwdOrSmsCodeFromServer(context, tableAccountInfo,true);
		final Bundle result = new Bundle();
		result.putString(Constants.PARAM_AUTHTOKEN_TYPE, authTokenType);
		result.putBoolean(Constants.KEY_VERIFY_PWD_RESULT, verifyResult);
		return result;
    }
    /**
     * 直接退出账号
     * @param context
     * @param authTokenType
     * @return
     */
    Bundle loginOut(Context context,String authTokenType){
    	AccountManager mAccountManager = AccountManager.get(context);
		AccountManagerCallback<Boolean> callback = new AccountManagerCallback<Boolean>() {
			@Override
			public void run(AccountManagerFuture<Boolean> arg0) {
				// TODO Auto-generated method stub
				try {
					Log.v(TAG, "loginOut() callback.run() 111 arg0.isDone():"+arg0.isDone()+" arg0.getResult():"+arg0.getResult());
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
		Account gomeAccount = AccountUtils.getLatestGomeAccount(context); //测试
   		//ActivityUtils.deleteLocalAccountDetail(context,gomeAccount.name);  退出不清除缓存信息
		mAccountManager.removeAccount(gomeAccount, callback, mHandler);

    	final Bundle bundle = new Bundle();
    	bundle.putString(Constants.PARAM_AUTHTOKEN_TYPE, authTokenType);
		Log.v(TAG, "loginOut() before return");
    	return bundle;
    }
    /**
     * 在新界面退出
     * @param context
     * @param authTokenType
     * @return 返回保护退出界面的intent
     */
    Bundle loginOutWithActivity(Context context,String authTokenType,AccountAuthenticatorResponse response){
    		return null;
    }
    Bundle register(Context context,String authTokenType,AccountAuthenticatorResponse response){
//    	Log.i("yimin","start register activity");
//    	final Intent intent = new Intent(mContext, RegisterActivity.class);
//    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    	mContext.startActivity(intent);
    	
        final Intent intent = new Intent(context, RegisterActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putString(Constants.PARAM_AUTHTOKEN_TYPE, authTokenType);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        
//        HashMap<String,String> mTableAccountInfo = new HashMap<String,String>();
//        mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, "18217387473");
//        mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, "346072730@qq.com");
//        mTableAccountInfo.put(Constants.KEY_ACCOUNT_NAME, "lilei");
//        mTableAccountInfo.put(Constants.KEY_ACCOUNT_SMS_CODE, "000000");
//        mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, "123");
//        mTableAccountInfo.put(Constants.KEY_ACCOUNT_REGISTER_TYPE, "0");
//        NetworkUtilities.authenticate(NetworkUtilities.AUTHENTICATE_TYPE_REGISTER,mTableAccountInfo);
        
        return bundle;
    }
    
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // null means we don't support multiple authToken types
        Log.v(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse response, Account account, String[] features) {
        // This call is used to query whether the Authenticator supports
        // specific features. We don't expect to get called, so we always
        // return false (no) for any queries.
        Log.v(TAG, "hasFeatures()");
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle loginOptions) {
        Log.v(TAG, "updateCredentials()");
        return null;
    }
}
