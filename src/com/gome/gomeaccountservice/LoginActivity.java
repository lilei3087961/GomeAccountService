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

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.IrisManager;
import android.app.facecode.FacecodeManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.gomeaccountservice.controls.GomeSmsCodeEditText;
import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.AppManager;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;
import com.gome.gomeaccountservice.utils.HttpCallBackListener;
import com.gome.gomeaccountservice.utils.HttpUtil;
import com.gome.gomeaccountservice.utils.PrefParams;
import com.gome.gomeaccountservice.utils.ServerModel;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.SystemProperties;

import gome.app.GomeAlertDialog;
import gome.app.GomeProgressDialog;
//import com.gome.gomeaccountservice.R;
/**
 * Activity which displays login screen to the user.
 */
public class LoginActivity extends AccountAuthenticatorActivity implements TextChangeCallback{

    private static final String TAG = Constants.TAG_PRE+"LoginActivity";
    public static final String KEY_LOGIN_TASKID ="keyLoginTaskId";
    private AccountManager mAccountManager;
	Dialog mAlertDialog;
    private String mPassword;
    private static final int PROGRESS_DIALOG_TIME = 1000;
    private static final int PROGRESS_WECHAT_TIME = 100;
    private EditText editPassword;
    GomeSmsCodeEditText mGomeSmsCodeEditText;
    private EditText editSmsCodeForLoginMain;  //登录主界面 多次输入错误密码的短信验证码输入框
    private EditText editPhoneForSetPwd;  //第三方登录设置密码输入的手机号
    private EditText editSmsCodeForSetPwd;
    private EditText editPwdForSetPwd;
    private EditText editConfirmPwdForSetPwd;
    
    private ImageView imageDeleteAccountName;
    private ImageView imageDeletePwd;
    private ImageView imageDeletePhoneForSetPwd;
    private ImageView imageDeleteSmsCodeForSetPwd;
    private ImageView imageDeletePwdForSetPwd;
    private ImageView imageDeleteConfirmPwdForSetPwd;
	private ImageView imageBtnHidePassword;
	private ImageView imageBtnShowPassword;
	private GomeProgressDialog progressDialog = null;
	
    String mAccountStartMode = null;  //账号启动模式
    String mStartModeLoginFeedback = null;  //登录跳转页面
    
    private IWXAPI api;
    private Button btnLogin;
    private Button btnSetPwdStep1;
    private Button btnSetPwdStep2;
    LinearLayout layoutMoreLoginMode;
    RelativeLayout layoutBootWizardBottom;
    GomeAlertDialog mMoreLoginModeDialog;
//    AlertDialog mUserAgreementDialog;
//    AlertDialog mPrivacyPolicyDialog;
    private String mUsername;
	final String KEY_LAYOUT_LOGIN_MAIN = "loginMain";
	final String KEY_LAYOUT_SET_PWD_STEP1 = "setPwdStep1";
	final String KEY_LAYOUT_SET_PWD_STEP2 = "setPwdStep2";
	String mCurrnetStep = KEY_LAYOUT_LOGIN_MAIN;
	
	HashMap<String, LinearLayout> mLayouts;
	LinearLayout layoutLoginMain;
	LinearLayout layoutSetPwdStep1;
	LinearLayout layoutSetPwdStep2;

    LinearLayout layoutBottomTextNormal;  
    LinearLayout layoutBottomTextBootWizard;
	private boolean isUseRecentAccount = false; //是否使用最近登陆过的账号
	private String mRecentAccount;  //最近登陆过的账号

	TextView textMoreLoginMode;
    private EditText editUsername;
	String mNickName = null;
	String mEmailAddress = null;
	String mPhoneNumber = null;
	String mGomeId = null;
	String mRegisterType = null;
	String mLoginPwd = null;
	String mSex = null;
	String mUserLevel = null;
	String mServerAvatarPath = null;
	
	final int TYPE_EDIT_ACCOUNT_NAME = 1;
	final int TYPE_EDIT_PASSWORD = 2;
	final int TYPE_EDIT_SMS_CODE_FOR_LOGIN_MAIN = 3;
	final int TYPE_EDIT_PHONE_FOR_SET_PWD = 4;
	final int TYPE_EDIT_SMS_CODE_FOR_SET_PWD = 5;
	final int TYPE_EDIT_PWD_FOR_SET_PWD = 6;
	final int TYPE_EDIT_CONFIRM_PWD_FOR_SET_PWD = 7;
    //server 返回状态变量
	String mServerResCode = null;
	String mServerResMsg = null;

    private WechatReceiveBroadCast wechatReceiveBroadCast;
    private String wechatHeadUrl;
    private String wechatNickName;
	private static final HandlerThread sWorkerThread = new HandlerThread(
			"LoginActivity-loader");
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
	private Handler mHandler =new Handler();
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "onActivityResult() mDialog:"+mMoreLoginModeDialog);
		//hideMoreLoginModeDialog();
	}
	/**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {

        Log.i(TAG, "onCreate(" + icicle + ")");
        super.onCreate(icicle);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && this.isInMultiWindowMode()){
            Log.i(TAG, "onCreate() isInMultiWindowMode finish()");
            ActivityUtils.alert(this,getResources().getString(R.string.alert_gomeaccount_no_support_multi_window_mode));
            finish();
        }
        setContentView(R.layout.login_activity);
        wechatReceiveBroadCast = new WechatReceiveBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("wechatauthlogin");
        registerReceiver(wechatReceiveBroadCast, filter);
        initView();
        showStep(KEY_LAYOUT_LOGIN_MAIN);
        parseAction();
    }
	/**
     * 初始化不同的模式如：开机向导过来的请求等
     */
    void parseAction(){
        AccountUtils.clearLocalAccountStartMode(this); // 清除启动模式相关数据
    	String action = getIntent().getAction();
        String loginFeedback = getIntent().getStringExtra(Constants.KEY_START_MODE_LOGIN_FEEDBACK);
    	Log.i(TAG, "parseAction() action:"+action+" loginFeedback:"+loginFeedback);
    	if(Constants.BOOT_WIZARD_MODE.equals(action)){
    		Log.i(TAG, "parseAction() 111");
    		AccountUtils.persistLocalAccountStartModeToShare(LoginActivity.this, Constants.BOOT_WIZARD_MODE);
    		layoutBootWizardBottom.setVisibility(View.VISIBLE);
            layoutBottomTextNormal.setVisibility(View.GONE);
            layoutBottomTextBootWizard.setVisibility(View.VISIBLE);
    		textMoreLoginMode.setVisibility(View.GONE);
            ActivityUtils.sendHideNavigationBarBrocast(LoginActivity.this);
            getWindow().getDecorView().showNavigationBarDivider(false);
    	}else{
    		Log.i(TAG, "parseAction() 222");
            if(Constants.USER_CENTER_MODE.equals(action)){ //用户中心模式
                AccountUtils.persistLocalAccountStartModeAndFeedbackToShare(this,action,loginFeedback);
            }else {
                AccountUtils.persistLocalAccountStartModeToShare(LoginActivity.this, Constants.NORMAL_MODE);
            }
    		layoutBootWizardBottom.setVisibility(View.GONE);
            layoutBottomTextNormal.setVisibility(View.VISIBLE);
            layoutBottomTextBootWizard.setVisibility(View.GONE);
    		textMoreLoginMode.setVisibility(View.VISIBLE);
    	}
    }
    /**
     * 获取最近登陆过的账号,并显示到用户名输入框
     */
	private void getRecentAccountAndShow(){
	    mRecentAccount = AccountUtils.getRecentAccountFromShare(LoginActivity.this);
	    if(null != mRecentAccount){
			if(ActivityUtils.isMobileNO(mRecentAccount)){
				editUsername.setText(ActivityUtils.convertPhoneNumWithStar(mRecentAccount));
			}else if(ActivityUtils.isEmail(mRecentAccount)){
				editUsername.setText(ActivityUtils.convertEmailWithStar(mRecentAccount));
			}
			editUsername.setSelection(editUsername.getText().length());
			imageDeleteAccountName.setVisibility(View.VISIBLE);  //默认让删除按钮显示
	        isUseRecentAccount = true;
	    }
	    Log.i(TAG, "getRecentAccountAndShow() mRecentAccount:"+mRecentAccount+" isUseRecentAccount:"+isUseRecentAccount);
	 }

	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed() mCurrnetStep:"+mCurrnetStep);
		if(!showPreStep(mCurrnetStep)){
			Log.i(TAG, "onBackPressed() finish()");
			finish();
		}
	}
    //测试处理已经登录过的情形
    void doAlreadyLogin(){
        Log.i(TAG,"doAlreadyLogin() 00");
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                if(AccountUtils.getLoginState(LoginActivity.this)){ //本地登录状态
                    Log.i(TAG,"doAlreadyLogin() 11");
                    if(ActivityUtils.checkNetworkSimple(LoginActivity.this)) { //网络可用
                        Account gomeAccount = AccountUtils.getLatestGomeAccount(LoginActivity.this);
                        HashMap<String,String> tableAccountInfo =  new HashMap<String, String>();
                        AccountManager am = AccountManager.get(LoginActivity.this);  //AccountManager用来判断已经登录过的场景
                        String password = am.getPassword(gomeAccount);
                        tableAccountInfo.put(Constants.KEY_SERVER_TOKEN,password);
                        if(!AccountUtils.verifyTokenFromServer(LoginActivity.this,tableAccountInfo,null,true)){ //token 失效,退出登录
                            Log.i(TAG,"doAlreadyLogin() 222 token not valid ***");
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //getRecentAccountAndShow();//重新显示最近登录账号(刚加载调用和这个调用可能回调冲突暂时不需要)
                                }
                            });
                        }else{ //本地和服务端都是登录状态，关闭
                            Log.i(TAG,"doAlreadyLogin() 333 ");
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }
                    }else {  //无网络网络本地登录，
                        Log.i(TAG,"doAlreadyLogin() 444 ");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                }
            }
        });

    }
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "onResume()");
        doAlreadyLogin();
        mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(LoginActivity.this);
        Log.i(TAG, "onResume() 22 mAccountStartMode:"+mAccountStartMode);
        if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)) { //开机向导模式跳转开机向导界面
            ActivityUtils.sendHideNavigationBarBrocast(LoginActivity.this);
            getWindow().getDecorView().showNavigationBarDivider(false);
        }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        Log.i(TAG,"onDestroy() this.getTaskId():"+this.getTaskId());
		if(null != wechatReceiveBroadCast){
			unregisterReceiver(wechatReceiveBroadCast);
            wechatReceiveBroadCast = null;
		}
		AppManager appManager = AppManager.getAppManager();
        appManager.finishActivity(this);  //界面销毁清除缓存的当前activity对象,防止内存泄漏
	}
	void initView(){
        mAccountManager = AccountManager.get(this);
        Log.i(TAG, "initView()");
        Account account = AccountUtils.getLatestGomeAccount(LoginActivity.this);
        if(null != account){  //存在account记录,正常情况下只有未登录才会进入登录界面
            Log.i(TAG,"initView() error exist account.name:"+account.name);
        }
        layoutBottomTextNormal = (LinearLayout)findViewById(R.id.layout_bottom_text_normal);
        layoutBottomTextBootWizard = (LinearLayout)findViewById(R.id.layout_bottom_text_boot_wizard);
        layoutLoginMain = (LinearLayout)findViewById(R.id.layout_login_main);
        layoutSetPwdStep1 = (LinearLayout)findViewById(R.id.layout_set_pwd_step1);
        layoutSetPwdStep2 = (LinearLayout)findViewById(R.id.layout_set_pwd_step2);
        mLayouts = new HashMap<String, LinearLayout>();
        mLayouts.put(KEY_LAYOUT_LOGIN_MAIN, layoutLoginMain);
        mLayouts.put(KEY_LAYOUT_SET_PWD_STEP1, layoutSetPwdStep1);
        mLayouts.put(KEY_LAYOUT_SET_PWD_STEP2, layoutSetPwdStep2);
        
        mGomeSmsCodeEditText = (GomeSmsCodeEditText)findViewById(R.id.layout_gome_sms_code);
        editSmsCodeForLoginMain = mGomeSmsCodeEditText.getEditText(); //
        editSmsCodeForLoginMain.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE_FOR_LOGIN_MAIN,this));
        imageDeleteAccountName = (ImageView) findViewById(R.id.image_delete_account_name);
        editUsername = (EditText) findViewById(R.id.username_edit);
        Log.i(TAG, "initView() R.id.username_edit:"+R.id.username_edit+" mGomeSmsCodeEditText:"+mGomeSmsCodeEditText);
        getRecentAccountAndShow();//更新最近登录账号到用户名输入框
        editUsername.addTextChangedListener(new EditChangedListener(TYPE_EDIT_ACCOUNT_NAME,this));
        editPassword = (EditText) findViewById(R.id.password_edit);
        editPassword.addTextChangedListener(new EditChangedListener(TYPE_EDIT_PASSWORD,this));
        editPassword.setTypeface(Typeface.DEFAULT);
        
        editPhoneForSetPwd = (EditText) findViewById(R.id.edit_phone);//
        editPhoneForSetPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_PHONE_FOR_SET_PWD,this));
        editSmsCodeForSetPwd = (EditText) findViewById(R.id.edit_sms_verify_code_for_set_pwd);
        editSmsCodeForSetPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE_FOR_SET_PWD,this));
        editPwdForSetPwd = (EditText) findViewById(R.id.edit_input_password_for_phone);
        editPwdForSetPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_PWD_FOR_SET_PWD,this));
        editConfirmPwdForSetPwd = (EditText) findViewById(R.id.edit_input_password_again_for_phone);
        editConfirmPwdForSetPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_CONFIRM_PWD_FOR_SET_PWD,this));
        
        imageDeletePwd = (ImageView) findViewById(R.id.image_delete_password);
        imageDeletePhoneForSetPwd = (ImageView) findViewById(R.id.image_delete_phone);
        imageDeleteSmsCodeForSetPwd = (ImageView) findViewById(R.id.image_delete_sms_verify_code_for_set_pwd);
        imageDeletePwdForSetPwd = (ImageView) findViewById(R.id.image_delete_input_password_for_phone);
        imageDeleteConfirmPwdForSetPwd = (ImageView) findViewById(R.id.image_delete_input_password_again_for_phone);

		imageBtnHidePassword = (ImageView)findViewById(R.id.image_btn_hide_password);
		imageBtnShowPassword = (ImageView)findViewById(R.id.image_btn_show_password);
		textMoreLoginMode = (TextView)findViewById(R.id.text_more_login_mode);
        
        btnLogin = (Button)findViewById(R.id.btn_login);
        btnSetPwdStep1 = (Button)findViewById(R.id.btn_set_pwd_step1);
        btnSetPwdStep2 = (Button)findViewById(R.id.btn_set_pwd_step2);
        layoutMoreLoginMode = (LinearLayout)findViewById(R.id.layoutMoreLoginMode);
        layoutBootWizardBottom = (RelativeLayout)findViewById(R.id.layout_boot_wizard_boottom);

		imageBtnHidePassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			  editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editPassword.setSelection(editPassword.getText().length());
				imageBtnHidePassword.setVisibility(View.GONE);
				imageBtnShowPassword.setVisibility(View.VISIBLE);
			}
		});

		imageBtnShowPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                editPassword.setSelection(editPassword.getText().length());
				imageBtnShowPassword.setVisibility(View.GONE);
				imageBtnHidePassword.setVisibility(View.VISIBLE);
			}
		});
	}
	/**
	 * 
	 * @param keyLayout
	 * @return 是否有上一步界面，若没有则返回false
	 */
	boolean showPreStep(String keyLayout){
		if(KEY_LAYOUT_SET_PWD_STEP1.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_LOGIN_MAIN;
		}else if(KEY_LAYOUT_SET_PWD_STEP2.equals(keyLayout)){
			mCurrnetStep =KEY_LAYOUT_SET_PWD_STEP1;
		}else{
			return false;
		}
		showStep(mCurrnetStep);
		return true;
	}
	void showStep(String keyLayout){
		mCurrnetStep = keyLayout;
		Log.i(TAG, "showStep keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size()));
        ActivityUtils.closeKeyboard(this); //切换界面关闭软键盘
		if(null != mLayouts){
			for (Map.Entry<String, LinearLayout> entry : mLayouts.entrySet()) {  
				   String key = entry.getKey();
				   LinearLayout linearLayout = entry.getValue();
				   if(null == linearLayout){
					   Log.i(TAG, "showStep key:"+key+" null == linearLayout");
					   break;
				   }
				   if(keyLayout.equals(key)){
					   linearLayout.setVisibility(View.VISIBLE);
				   }else{
					   linearLayout.setVisibility(View.GONE);
				   }
			}
		}
	}
	
//////////点击事件处理begin
	public void doOnclickBootWizardLeftButton(View view){
		finish();
	}
	public void doOnclickBootWizardRightButton(View view){
        mAlertDialog = new GomeAlertDialog.Builder(this)
        .setTitle(getResources().getString(R.string.boot_wizard_login_skip_title))
        .setMessage(getResources().getString(R.string.boot_wizard_login_skip_content))
        .setPositiveButton(getResources().getString(R.string.text_skip), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            Log.i(TAG, "doOnclickBootWizardRightButton.setPositiveButton.onClick dissmiss");
            doLoginSkip();
            }
        }).
        setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub 
            Log.i(TAG, "doOnclickBootWizardRightButton.setNegativeButton.onClick  dissmiss");
            mAlertDialog.dismiss();
            }
        }).
        create();
        mAlertDialog.show();

	}
	//处理开机向导登录界面跳过
	void doLoginSkip(){
        //开机向导跳过弹出的界面
        boolean isSupportIris = SystemProperties.get("ro.gome_iris_support").equals("0")?false:true;
        FingerprintManager fpm = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
        int mFingerprintSize = fpm.getEnrolledFingerprints().size();
        FacecodeManager mFacecodemanager = (FacecodeManager) getSystemService(Context.FACECODE_SERVICE);
        boolean hasFaces = mFacecodemanager.hasFaces();
        Log.i(TAG, "doOnclickBootWizardRightButton() 000 mFingerprintSize："+mFingerprintSize+" hasFaces:"+hasFaces+" isSupportIris:"+isSupportIris);
        if(isSupportIris){
            IrisManager mIrisManager = (IrisManager) getSystemService(Context.IRIS_SERVICE);
            final boolean hasFeature = mIrisManager.isHasFeature(); //S7去掉虹膜功能
            if (mFingerprintSize>0&&hasFeature&&hasFaces){
                Log.i(TAG, "doOnclickBootWizardRightButton() 111 hasFeature:"+hasFeature);
                ComponentName cn = new ComponentName(ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_WELCOME_ACTIVITY);
                Intent  intent = new Intent();
                intent.putExtra("lastPage", true);
                intent.setComponent(cn);
                this.startActivity(intent);
            }else if (mFingerprintSize==0&&hasFeature&&hasFaces){
                Log.i(TAG, "doOnclickBootWizardRightButton() 222");
                ActivityUtils.startActivityByName(LoginActivity.this, ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_FINGER_PRINT_ACTIVITY);
            }else if (!hasFeature&&hasFaces){
                Log.i(TAG, "doOnclickBootWizardRightButton() 333");
                ActivityUtils.startActivityByName(LoginActivity.this, ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_IRIS_ACTIVITY);
            }else if (!hasFaces){
                Log.i(TAG, "doOnclickBootWizardRightButton() 444");
                ActivityUtils.startActivityByName(LoginActivity.this, ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_FACE_CODE_ACTIVITY);
            }
        }else {
            if (mFingerprintSize > 0 && hasFaces) {
                Log.i(TAG, "doOnclickBootWizardRightButton() 555");
                ComponentName cn = new ComponentName(ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_WELCOME_ACTIVITY);
                Intent intent = new Intent();
                intent.putExtra("lastPage", true);
                intent.setComponent(cn);
                this.startActivity(intent);
            } else if (mFingerprintSize == 0 && hasFaces) {
                Log.i(TAG, "doOnclickBootWizardRightButton() 666");
                ActivityUtils.startActivityByName(LoginActivity.this, ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_FINGER_PRINT_ACTIVITY);
            } else if (!hasFaces) {
                Log.i(TAG, "doOnclickBootWizardRightButton() 777");
                ActivityUtils.startActivityByName(LoginActivity.this, ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_FACE_CODE_ACTIVITY);
            }
        }
        finish();
    }
	public void doOnclickDeleteAccountNameButton(View view) {
		editUsername.setText("");
	}
	public void doOnclickDeletePwdButton(View view) {
		editPassword.setText("");
	}
    /**
     * 点击登录按钮
     * @param view The Submit button for which this method is invoked
     */
    public void doOnclickLogin(View view) {
    	if(!checkInput() || !ActivityUtils.checkNetwork(LoginActivity.this)){
    		return;
    	}
    	doLoginInTherad();

    }
    /**
     * 点击找回密码按钮
     * @param view
     */
    public void doOnclickRetrievePwd(View view) {
    	Intent i = new Intent(this, RetrievePwdActivity.class);
    	startActivity(i);
    	//finish();
    }
    /**
     * 点击退出按钮
     * @param view
     */
    public void doExit(View view){
    	finish();
    }
    /**
     * 点击更多登录方式按钮
     * @param view
     */
    public void doOnclickMoreLoginMode(View view){
    	showMoreLoginModeDialog();

    }
    /**
     * 点击注册国美账号按钮
     * @param view
     */
    public void doOnclickRegisterGomeAccount(View view){
        Log.i(TAG,"doOnclickRegisterGomeAccount()");
    	startRegisterActivity();
    }
    void startRegisterActivity(){
        Log.i(TAG,"startRegisterActivity() this.getTaskId():"+this.getTaskId());
    	Intent i = new Intent(this,RegisterActivity.class);
    	i.setAction(RegisterActivity.ACTION_REGIST_USE_CURRENT_PHONE);
        i.putExtra(KEY_LOGIN_TASKID,this.getTaskId());
    	startActivity(i);
        
        AppManager appManager = AppManager.getAppManager();
        appManager.addActivity(this);
    }
    /**
     * 点击微信登录按钮
     * @param view
     */
    public void doOnclickWeixinLogin(View view){
        if(isWechatInstalled(this)) {
            if(null == progressDialog){
                progressDialog = new GomeProgressDialog.Builder(this)
                .setProgressStyle(GomeProgressDialog.STYLE_SPINNER)
                .setCancelable(false).setMessage(getResources()
                .getString(R.string.alert_login_dialog))
                .create();
            }
            progressDialog.show();
            weChatAuth();
        } else {
            hideMoreLoginModeDialog();
            ActivityUtils.alert(LoginActivity.this,getResources().getString(R.string.alert_wechat_not_installed_dialog));
        }
    }

    public static boolean isWechatInstalled(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    //prepare to login wechat
    private void weChatAuth() {
        Log.e(TAG, "weChatAuth");
        hideMoreLoginModeDialog();
        new Handler().postDelayed(new Runnable(){  
            public void run() { 
                if (api == null) {
                    api = WXAPIFactory.createWXAPI(LoginActivity.this, App.WX_APPID, true);
                }
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "wx_login_duzun";
                api.sendReq(req);
                progressDialog.dismiss();
            } 
        }, PROGRESS_DIALOG_TIME);
    }
    /**
     * 点击微博登录按钮
     * @param view
     */
    public void doOnclickWeiboLogin(View view){
		Log.i(TAG, "doOnclickWeiboLogin() ");
		hideMoreLoginModeDialog();
    }
    /**
     * 点击用户协议按钮
     * @param view
     */
    public void doOnclickUserAgreement(View view){   	
    	ComponentName cn = new ComponentName(ActivityUtils.USER_CENTER_PACAAGE_NAME, ActivityUtils.USER_CENTER_USER_AGREE_AND_PRIVACY_POLICY_NAME);
		Intent intent = new Intent();
		intent.setAction("User_Agreement");
		intent.putExtra("title", getResources().getString(R.string.txt_login_bottom_user_agreement));
        mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(LoginActivity.this);
        Log.i(TAG, "doOnclickUserAgreement() 22 mAccountStartMode:"+mAccountStartMode);
        if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
            intent.putExtra(Constants.KEY_LOCAL_ACCOUNT_START_MODE,Constants.BOOT_WIZARD_MODE);
        }
		intent.setComponent(cn);
		startActivity(intent);
    }
    /**
     * 点击隐私政策按钮
     * @param view
     */
    public void doOnclickPrivacyPolicy(View view){
    	ComponentName cn = new ComponentName(ActivityUtils.USER_CENTER_PACAAGE_NAME, ActivityUtils.USER_CENTER_USER_AGREE_AND_PRIVACY_POLICY_NAME);
		Intent intent = new Intent();
		intent.setAction("Gome_Privacy_Policy");
		intent.putExtra("title", getResources().getString(R.string.txt_login_bottom_privacy_policy_title));
        mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(LoginActivity.this);
        Log.i(TAG, "doOnclickPrivacyPolicy() 22 mAccountStartMode:"+mAccountStartMode);
        if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
            intent.putExtra(Constants.KEY_LOCAL_ACCOUNT_START_MODE,Constants.BOOT_WIZARD_MODE);
        }
		intent.setComponent(cn);
		startActivity(intent);
    }
    
//////////点击事件处理end 
    /**
     * 显示更多登录方式弹出框
     */
    void showMoreLoginModeDialog(){
    	LayoutInflater inflater = getLayoutInflater();
    	//modify by yimin.zhu for bugid GMOS-3957 for remove blog function on 2017.8.31
    	//View layout = inflater.inflate(R.layout.alert_login_more_login_mode_layout,(ViewGroup) findViewById(R.id.layoutMoreLoginMode));
    	View layout = inflater.inflate(R.layout.alert_login_wechat_layout,(ViewGroup) findViewById(R.id.layoutMoreLoginMode));
    	mMoreLoginModeDialog = new GomeAlertDialog.Builder(this).setView(layout).create();
    	Window window = mMoreLoginModeDialog.getWindow();
    	mMoreLoginModeDialog.show();
    }
    /**
     * 关闭更多方式弹出框
     */
    void hideMoreLoginModeDialog(){
    	if(null != mMoreLoginModeDialog){
    		mMoreLoginModeDialog.dismiss();
    	}
    }
    boolean checkInput(){
		mUsername = isUseRecentAccount? mRecentAccount:editUsername.getText().toString();
        mPassword = editPassword.getText().toString();
		Log.i(TAG, "checkInput() mUsername:"+mUsername+" isUseRecentAccount:"+isUseRecentAccount
			+" mRecentAccount:"+mRecentAccount+" mPassword:"+mPassword+" mGomeSmsCodeEditText.getVisibility():"+mGomeSmsCodeEditText.getVisibility());
        if ((TextUtils.isEmpty(mUsername) && !isUseRecentAccount) || TextUtils.isEmpty(mPassword)) {
        	Log.e(TAG, "checkInput() 1111");
        	ActivityUtils.alert(LoginActivity.this, getMessage().toString());
        	return false;
        }
        if(!ActivityUtils.isEmail(mUsername) && !ActivityUtils.isMobileNO(mUsername)){
        	ActivityUtils.alert(LoginActivity.this, getResources().getString(R.string.alert_input_valid_phone_or_email));
        	return false;
        }
        if(View.VISIBLE == mGomeSmsCodeEditText.getVisibility()){
			EditText editSmsCode = mGomeSmsCodeEditText.getEditText();
			if(editSmsCode.getText().toString().trim().isEmpty()){
				return false;
			}
        }
    	return true;
    }

    void doLoginInTherad(){
		Log.i(TAG, "doLoginInTherad() isUseRecentAccount:"+isUseRecentAccount+" mRecentAccount:"+mRecentAccount+" mUsername:"+mUsername);
    	btnLogin.setEnabled(false);
        if(null == progressDialog){
            progressDialog = new GomeProgressDialog.Builder(this)
            .setProgressStyle(GomeProgressDialog.STYLE_SPINNER)
            .setCancelable(false).setMessage(getResources()
            .getString(R.string.alert_login_dialog))
            .create();
        }
        progressDialog.show(); 
    	sWorker.post(new Runnable() {
			@Override
			public void run() {
				HashMap<String,String> mTableAccountInfo = new HashMap<String,String>();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_USER_NAME, isUseRecentAccount?mRecentAccount:mUsername);
            	mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, mPassword);
				if(View.VISIBLE == mGomeSmsCodeEditText.getVisibility()){
					String smsCode = editSmsCodeForLoginMain.getText().toString().trim();
					Log.i(TAG, "doLoginInTherad() smsCode:"+smsCode);
					mTableAccountInfo.put(Constants.KEY_SMS_CODE, smsCode);
				}
                final ServerModel serverModel = new ServerModel();
                if(AccountUtils.login(LoginActivity.this, mTableAccountInfo,serverModel)){
	                mHandler.post(new Runnable() {
						@Override
						public void run() {
							doLogined();
                            progressDialog.dismiss();
							btnLogin.setEnabled(true);
						}
					});
            	
                }else{
                    mHandler.post(new Runnable() {
						@Override
						public void run() {
                            progressDialog.dismiss();
							String serverResCode = serverModel.getServerResCode();
							String emailActiveStatus = serverModel.getEmailActiveStatus();
							Log.i(TAG, "doLoginInTherad() serverResCode:"+serverResCode+" emailActiveStatus:"+emailActiveStatus);
							//处理多次输入错误密码的情形
							if(Constants.SERVER_MULTIPLE_PWD_ERROR.equals(serverResCode)|| Constants.SERVER_VERIFY_CODE_ERROR.equals(serverResCode)){
								String accountName = isUseRecentAccount?mRecentAccount:mUsername;
								mGomeSmsCodeEditText.setVisibility(View.VISIBLE);
								mGomeSmsCodeEditText.setSendVerifyCodeInfo(accountName,Constants.OPERATE_TYPE_LOGIN);
								if(Constants.SERVER_MULTIPLE_PWD_ERROR.equals(serverResCode)){
									editSmsCodeForLoginMain.setText("");
									btnLogin.setEnabled(false);
								}else{
									btnLogin.setEnabled(true);
								}

								if(ActivityUtils.isMobileNO(accountName)){
									editSmsCodeForLoginMain.setHint(getResources().getString(R.string.edit_hint_sms_verify_code));
								}else if(ActivityUtils.isEmail(accountName)){
									editSmsCodeForLoginMain.setHint(getResources().getString(R.string.edit_hint_email_verify_code));
								}
							}else{
								mGomeSmsCodeEditText.setVisibility(View.GONE);
								btnLogin.setEnabled(true);
							}
							//处理邮箱注册未激活的情况
							if(Constants.EMAIL_STATUS_REGISTER_NOT_ACTIVED.equals(emailActiveStatus)){
								doLoginEmailNotActived();
							}
						}
					});

                }
			}
		});
    }
    /**
     * 点击登录除了邮箱注册未激活的情形
     */
    void doLoginEmailNotActived(){
		final String accountName = isUseRecentAccount?mRecentAccount:mUsername;
		Log.i(TAG, "doLoginEmailNotActived() accountName:"+accountName);
        mAlertDialog = new GomeAlertDialog.Builder(this)
		.setTitle(getResources().getString(R.string.txt_login_verify_later_alert_title))
        .setMessage(getResources().getString(R.string.txt_login_verify_later_alert_txt))
		.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
				Log.i(TAG, "doVerifyLater.setPositiveButton.onClick dissmiss");
				Intent intent = new Intent(LoginActivity.this,CheckEmailActiveActivity.class);
				intent.putExtra(Constants.KEY_ACCOUNT_EMAIL, accountName);
				startActivity(intent);
				mAlertDialog.dismiss();
				finish();
            }
        }).
        setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub 
				Log.i(TAG, "doVerifyLater.setNegativeButton.onClick  dissmiss");
				mAlertDialog.dismiss();
            }
        }).
        create();
		mAlertDialog.show();
    }
    /**
     * 处理登录后相关界面显示
     */
    void doLogined(){
    	mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(LoginActivity.this);
        mStartModeLoginFeedback = AccountUtils.getLocalAccountStartModeFeedbackFromShare(LoginActivity.this);
    	Log.i(TAG, "doLogined() mAccountStartMode:"+mAccountStartMode+" mStartModeLoginFeedback:"+mStartModeLoginFeedback
                +" currentThread:"+Thread.currentThread());
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){ //开机向导模式跳转开机向导界面
			ActivityUtils.startActivityByName(LoginActivity.this, ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_LOGIN_SUCCESS_ACTIVITY);
		}else if(Constants.USER_CENTER_MODE.equals(mAccountStartMode)){
			if(null != mStartModeLoginFeedback){
				ActivityUtils.startActivityByName(LoginActivity.this, ActivityUtils.USER_CENTER_PACAAGE_NAME,mStartModeLoginFeedback);
			}
        }else{
			Intent i =new Intent(LoginActivity.this,AccountInfoActivity.class);
			startActivity(i);
		}
		finish();
    }

    //初始化全局变量
    void initVarible(){
    	mServerResCode = null;
    	mServerResMsg = null;
    }
//抽出到公共类    
    class EditChangedListener implements TextWatcher{
		private int mEditType = -1;
		private TextChangeCallback mTextChangeCallback;
		String newStr;
		public EditChangedListener(int editType,TextChangeCallback textChangeCallback){
			mEditType = editType;
			mTextChangeCallback = textChangeCallback;
		}
		@Override
		public void afterTextChanged(Editable s) {
			if(s.toString().length()>0){
				mTextChangeCallback.showAfterTextNotEmpty(mEditType);
			}else{
				mTextChangeCallback.showAfterTextEmpty(mEditType);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			//Log.i(TAG, "beforeTextChanged() mEditType:"+mEditType+" s:"+s);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if(TYPE_EDIT_ACCOUNT_NAME == mEditType){//账号输入框改变
				Log.i(TAG, "onTextChanged() 111 TYPE_EDIT_ACCOUNT_NAME s:"+s.toString()+" start:"+start+" before:"+before+" count:"+count
						+" isUseRecentAccount:"+isUseRecentAccount);
				if(isUseRecentAccount){
					isUseRecentAccount = false;
					newStr = s.toString().substring(start,start+count);
					Log.i(TAG, "onTextChanged 222 TYPE_EDIT_ACCOUNT_NAME  newStr:"+newStr);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Log.i(TAG, "onTextChanged 333 TYPE_EDIT_ACCOUNT_NAME  newStr:"+newStr);
							editUsername.setText(newStr);
							editUsername.setSelection(newStr.length());
						}
					});
				}
			}
		}
    }
  //输入框内容非空
  	public void showAfterTextNotEmpty(int editType){
        Log.i(TAG, "showAfterTextNotEmpty editType:"+editType);
		switch (editType) {
			case TYPE_EDIT_ACCOUNT_NAME:
				isUseRecentAccount = false;
				imageDeleteAccountName.setVisibility(View.VISIBLE);
				mGomeSmsCodeEditText.setVisibility(View.GONE);  //用户输入变化短信验证码界面隐藏
				if(checkLoginInputFull()){
					btnLogin.setEnabled(true);
				}
				break;
			case TYPE_EDIT_PASSWORD:
				imageDeletePwd.setVisibility(View.VISIBLE);
				Log.e(TAG, "afterTextChanged() 111");
				if(checkLoginInputFull()){
					Log.e(TAG, "afterTextChanged() 222");
					btnLogin.setEnabled(true);
				}
				break;
			case TYPE_EDIT_SMS_CODE_FOR_LOGIN_MAIN:
				if(checkLoginInputFull()){
					btnLogin.setEnabled(true);
				}
				break;
			case TYPE_EDIT_PHONE_FOR_SET_PWD:
				imageDeletePhoneForSetPwd.setVisibility(View.VISIBLE);
				if(checkSetPwdStep1InputFull()){
					btnSetPwdStep1.setEnabled(true);
				}
				break;
			case TYPE_EDIT_SMS_CODE_FOR_SET_PWD:
				imageDeleteSmsCodeForSetPwd.setVisibility(View.VISIBLE);
				if(checkSetPwdStep2InputFull()){
					btnSetPwdStep2.setEnabled(true);
				}
				break;
			case TYPE_EDIT_PWD_FOR_SET_PWD:
				imageDeletePwdForSetPwd.setVisibility(View.VISIBLE);
				if(checkSetPwdStep2InputFull()){
					btnSetPwdStep2.setEnabled(true);
				}
				break;
			case TYPE_EDIT_CONFIRM_PWD_FOR_SET_PWD:
				imageDeleteConfirmPwdForSetPwd.setVisibility(View.VISIBLE);
				if(checkSetPwdStep2InputFull()){
					btnSetPwdStep2.setEnabled(true);
				}
				break;
			default:
				break;
		}
  	}
	//输入框内容为空
	public void showAfterTextEmpty(int editType){
		Log.i(TAG, "showAfterTextEmpty editType:"+editType);
		switch (editType) {
			case TYPE_EDIT_ACCOUNT_NAME:
				imageDeleteAccountName.setVisibility(View.GONE);
				mGomeSmsCodeEditText.setVisibility(View.GONE);  //用户输入变化短信验证码界面隐藏
				btnLogin.setEnabled(false);
				break;
			case TYPE_EDIT_PASSWORD:
				imageDeletePwd.setVisibility(View.GONE);
				btnLogin.setEnabled(false);
				break;
			case TYPE_EDIT_SMS_CODE_FOR_LOGIN_MAIN:
				btnLogin.setEnabled(false);
				break;
			case TYPE_EDIT_PHONE_FOR_SET_PWD:
				imageDeletePhoneForSetPwd.setVisibility(View.GONE);
				btnSetPwdStep1.setEnabled(false);
				break;
			case TYPE_EDIT_SMS_CODE_FOR_SET_PWD:
				imageDeleteSmsCodeForSetPwd.setVisibility(View.VISIBLE);
				btnSetPwdStep2.setEnabled(false);
				break;
			case TYPE_EDIT_PWD_FOR_SET_PWD:
				imageDeletePwdForSetPwd.setVisibility(View.VISIBLE);
				btnSetPwdStep2.setEnabled(false);
				break;
			case TYPE_EDIT_CONFIRM_PWD_FOR_SET_PWD:
				imageDeleteConfirmPwdForSetPwd.setVisibility(View.VISIBLE);
				btnSetPwdStep2.setEnabled(false);
				break;
			default:
				break;
		}
	}
    /***
     * 判断邮箱步骤3是否都填写了
     * @return
     */
    boolean checkLoginInputFull(){
        Log.i(TAG, "checkLoginInputFull() mGomeSmsCodeEditText.getVisibility():"+mGomeSmsCodeEditText.getVisibility());
		if(editUsername.getText().toString().isEmpty()){//只有在未显示最近账号且用户名为空才认为输入为空
			return false;
		}else if(editPassword.getText().toString().isEmpty()){
			return false;
		}
        if(View.VISIBLE == mGomeSmsCodeEditText.getVisibility()){
			EditText editSmsCode = mGomeSmsCodeEditText.getEditText();
			if(editSmsCode.getText().toString().trim().isEmpty()){
				return false;
			}
        }
    	return true;
    }
    boolean checkSetPwdStep1InputFull(){
    	if(editPhoneForSetPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}
    	return true;
    }
    boolean checkSetPwdStep2InputFull(){
    	if(editSmsCodeForSetPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}else if(editPwdForSetPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}else if(editConfirmPwdForSetPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}
    	return true;
    }
    /**
     * Returns the message to be displayed at the top of the login dialog box.
     */
    private CharSequence getMessage() {
        if (TextUtils.isEmpty(mUsername)) {
            final CharSequence msg = getText(R.string.login_activity_loginfail_text_usernae_mmissing);
            return msg;
        }
        if (TextUtils.isEmpty(mPassword)) {
            return getText(R.string.login_activity_loginfail_text_pwmissing);
        }
        return null;
    }

    class WechatReceiveBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "wechat onReceive");
            new Handler().postDelayed(new Runnable(){  
                public void run() { 
                    getAccessToken();
                } 
            }, PROGRESS_WECHAT_TIME);
            /*Intent intent1 =new Intent(LoginActivity.this,AccountInfoActivity.class);
            startActivity(intent1);*/
        }
    }

    public void getAccessToken() {
        Log.e(TAG, "-----getAccessToken----");
        SharedPreferences WxSp = this.getApplicationContext()
                .getSharedPreferences(PrefParams.spName, Context.MODE_PRIVATE);
        String code = WxSp.getString(PrefParams.CODE, "");
        final SharedPreferences.Editor WxSpEditor = WxSp.edit();
        Log.e(TAG, "-----获取到的code----" + code);
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                + App.WX_APPID
                + "&secret="
                + App.WX_APPSecret
                + "&code="
                + code
                + "&grant_type=authorization_code";
        Log.e(TAG, "--------即将获取到的access_token的地址--------");
        HttpUtil.sendHttpRequest(url, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {

                //解析以及存储获取到的信息
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e(TAG, "-----获取到的json数据1-----" + jsonObject.toString());
                    String access_token = jsonObject.getString("access_token");
                    Log.e(TAG, "--------获取到的access_token的地址--------" + access_token);
                    String openid = jsonObject.getString("openid");
                    String refresh_token = jsonObject.getString("refresh_token");
                    Log.e(TAG, "-----access_token-----" + access_token);
                    Log.e(TAG, "-----refresh_token-----" + refresh_token);
                    Log.e(TAG, "-----openid-----" + openid);
                    if (!access_token.equals("")) {
                        WxSpEditor.putString(PrefParams.ACCESS_TOKEN, access_token);
                        WxSpEditor.apply();
                    }
                    if (!refresh_token.equals("")) {
                        WxSpEditor.putString(PrefParams.REFRESH_TOKEN, refresh_token);
                        WxSpEditor.apply();
                    }
                    if (!openid.equals("")) {
                        Log.e(TAG, "-----start getPersonMessage-----");
                        WxSpEditor.putString(PrefParams.WXOPENID, openid);
                        WxSpEditor.apply();
                        getPersonMessage(access_token, openid);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                //Toast.makeText(this, "通过code获取数据没有成功", Toast.LENGTH_SHORT).show();
                Log.i("yimin","log error");
            }
        });
    }

    private void getPersonMessage(String access_token, String openid) {
        Log.e(TAG, "------getPersonMessage------");
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token="
                + access_token
                + "&openid="
                + openid;
        HttpUtil.sendHttpRequest(url, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    wechatHeadUrl = jsonObject.getString("headimgurl");
                    wechatNickName = jsonObject.getString("nickname");
                    Log.i(TAG,"headimageurl:" + wechatHeadUrl + "wechat name:" + wechatNickName);
                    Log.e(TAG, "------onFinish------" + jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "------onError------");
            }
        });
        doWechatLoginInTherad(openid);
    }

    void doWechatLoginInTherad(String wechatId){
        Log.i(TAG, "doWechatLoginInTherad:" + wechatId);
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String,String> mTableAccountInfo = new HashMap<String,String>();
                //mTableAccountInfo.put(Constants.KEY_ACCOUNT_NAME, mUsername);
//              if(ActivityUtils.isMobileNO(mUsername)){
//                  mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mUsername);
//              }else if(ActivityUtils.isEmail(mUsername)){
//                  mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mUsername);
//              }else{
//                  Log.i(TAG, "doLoginInTherad() mUsername:"+mUsername+" error!! not mUsername not phone number or email!!");
//                  return;
//              }
                mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_ID, wechatId);
                mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_TYPE, "2");
                
                if(AccountUtils.checkWechatAccountBinded(LoginActivity.this, mTableAccountInfo)){
                    Log.i(TAG,"wechat has bindded phone");
                    mTableAccountInfo.clear();
                    mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_TYPE,"2");
                    mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_ID,wechatId);
                    if(AccountUtils.login(LoginActivity.this,mTableAccountInfo)){ 
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //ccountUtils.persistRecentAccountToShare(LoginActivity.this,mUsername);
    //                          editUsername.setText("");//登录或重新初始化输入的信息
    //                          editPassword.setText("");
                                Intent i =new Intent(LoginActivity.this,AccountInfoActivity.class);  
                                startActivity(i);
                                finish();
                            }
                        });
                    }
                } else {
                    Log.i(TAG,"wechat not bindded phone");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent1 =new Intent(LoginActivity.this,BindPhoneActivity.class);
                            intent1.putExtra("wechatId", wechatId);
                            intent1.putExtra("headimgurl", wechatHeadUrl);
                            intent1.putExtra("nickname", wechatNickName);
                            startActivity(intent1);
                        }
                    });
                }
            }
        });
    }
}
