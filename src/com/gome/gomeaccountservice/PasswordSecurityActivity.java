package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.EditChangedListener;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;

import java.util.HashMap;
import java.util.Map;

import gome.app.GomeAlertDialog;

public class PasswordSecurityActivity extends Activity implements TextChangeCallback{
	static final String TAG = Constants.TAG_PRE+"PasswordSecurityActivity";
    Animation mAnimationOpenEnter;
    Animation mAnimationOpenExit;
    Animation mAnimationCloseEnter;
    Animation mAnimationCloseExit;

	public static String ACTION_BIND_SECURITY_PHONE = "action_bind_security_phone"; //外部请求调用
	GomeAlertDialog mDialog;
	final int DIALOG_UNBIND_SECURITY_PHONE =1;
	
	private EditText editOldPwd;
	private EditText editNewPwd;
	private EditText editNewPwdConfirm;
	private EditText editSecurityPhone;
	private EditText editBindSmsVerifyCode;
	private EditText editUnbindSmsVerifyCode;
	
	private TextView textTitleUnbindPhone;
	private TextView textBindSendSmsVerifyCode; 
	private TextView textUnbindSendSmsVerifyCode;
	private TextView textPhoneUnbindStep1;
	private TextView textPhoneUnbindStep2;
	
	private ImageView imageSecurityPhone; 
	private ImageView imageBindSmsVerifyCode; 
	private ImageView imageUnbindSmsVerifyCode; 
	private ImageView imageDeleteOldPwd;
	private ImageView imageDeleteNewPwd;
	private ImageView imageDeleteConfirmNewPwd;
	private ImageView imageHidePasswordForOld;//image_hide_password_for_old
	private ImageView imageShowPasswordForOld;
	private ImageView imageHidePasswordForNew;
	private ImageView imageShowPasswordForNew;
	private ImageView imageHidePasswordForNewConfirm;
	private ImageView imageShowPasswordForNewConfirm;
	
	private Button btnBindSecurityPhone;
	private Button btnUnbindSecurityPhone;
	private Button btnResetPwd;
	
	
	final int TYPE_EDIT_SECURITY_PHONE = 1;
	final int TYPE_EDIT_BIND_SECURITY_PHONE_SMS_CODE = 2;
	final int TYPE_EDIT_UNBIND_SECURITY_PHONE_SMS_CODE = 3;
	final int TYPE_EDIT_RESET_PWD_OLD = 4;
	final int TYPE_EDIT_RESET_PWD_NEW = 5;
	final int TYPE_EDIT_RESET_PWD_CONFIRM_NEW = 6;
	final int REQUEST_CODE_PASSWORD_SECURITY  = 6;
	
	private BindSmsCodeTimeCount mBindSmsCodeTimeCount = null;
	private UnbindSmsCodeTimeCount mUnbindSmsCodeTimeCount = null;
	
    HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	//String mPhoneNumber = null;
	String mSecurityPhoneNumber = null;
	String mGomeId = null;
	String mRegisterType = null;
	String mOldPassword = null;
	String mNewPassword = null;
    String mToken = null;
    String mEmailActiveStatus = null;
	View layoutSecurityPhoneItem; //layout_security_phone_item
    
	String mCurrnetStep = null;
	HashMap<String, LinearLayout> mLayouts;
	LinearLayout layoutPasswordSecurityMain;
	LinearLayout layoutResetPwd;
	LinearLayout layoutBindSecurityPhoneStep1;
	LinearLayout layoutBindSecurityPhoneStep2;
	LinearLayout layoutUnbindSecurityPhoneStep1;
	LinearLayout layoutUnbindSecurityPhoneStep2;
	
	final String KEY_LAYOUT_PASSWORD_SECURITY_MAIN = "passwordSecurityMain";
	final String KEY_LAYOUT_RESET_PWD = "resetPwd";
	final String KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1 = "bindSecurityPhoneStep1";
	final String KEY_LAYOUT_BIND_SECURITY_PHONE_STEP2 = "bindSecurityPhoneStep2";
	final String KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP1 = "unbindSecurityPhoneStep1";
	final String KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP2 = "unbindSecurityPhoneStep2";

	
    private static final HandlerThread sWorkerThread = new HandlerThread("PasswordSecurityActivity-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    Handler mHandler =new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && this.isInMultiWindowMode()){
            Log.i(TAG, "onCreate() isInMultiWindowMode finish()");
            ActivityUtils.alert(this,getResources().getString(R.string.alert_gomeaccount_no_support_multi_window_mode));
            finish();
        }
		setContentView(R.layout.password_security_activity);
		getAccountInfo();
		initView();
		parseAction();
	}
	
	@Override
	public void onBackPressed() {
		String action = getIntent().getAction();
		Log.i(TAG, "onBackPressed() mCurrnetStep:"+mCurrnetStep+" action:"+action);
		if(!showPreStep(mCurrnetStep)){
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "PasswordSecurityActivity onActivityResult() resultCode:"+resultCode+" requestCode:"+requestCode);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
			case REQUEST_CODE_PASSWORD_SECURITY:
				setResult(resultCode,data);
				finish();
				break;
			default:
				break;
		}

		
	}

	void initView(){
        mAnimationOpenEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_enter_layout);
        mAnimationOpenExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_exit_layout);
        mAnimationCloseEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_enter_layout);
        mAnimationCloseExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_exit_layout);

		layoutPasswordSecurityMain = (LinearLayout)findViewById(R.id.layout_password_security_main);
		layoutResetPwd = (LinearLayout)findViewById(R.id.layout_reset_pwd);
		layoutBindSecurityPhoneStep1 = (LinearLayout)findViewById(R.id.layout_bind_security_phone_step1);
		layoutBindSecurityPhoneStep2 = (LinearLayout)findViewById(R.id.layout_bind_security_phone_step2);
		layoutUnbindSecurityPhoneStep1 = (LinearLayout)findViewById(R.id.layout_unbind_phone_step1);  //解绑手机界面公共
		layoutUnbindSecurityPhoneStep2 = (LinearLayout)findViewById(R.id.layout_unbind_phone_step2);  //解绑手机界面公共
		
		mLayouts = new HashMap<String, LinearLayout>();
		mLayouts.put(KEY_LAYOUT_PASSWORD_SECURITY_MAIN, layoutPasswordSecurityMain);
		mLayouts.put(KEY_LAYOUT_RESET_PWD, layoutResetPwd);
		mLayouts.put(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1, layoutBindSecurityPhoneStep1);
		mLayouts.put(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP2, layoutBindSecurityPhoneStep2);
		mLayouts.put(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP1, layoutUnbindSecurityPhoneStep1);
		mLayouts.put(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP2, layoutUnbindSecurityPhoneStep2);
		
		editOldPwd = (EditText) findViewById(R.id.edit_old_pwd);
		editOldPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_RESET_PWD_OLD,this));
        editOldPwd.setTypeface(Typeface.DEFAULT);
		editNewPwd = (EditText) findViewById(R.id.edit_new_pwd);
		editNewPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_RESET_PWD_NEW,this));
        editNewPwd.setTypeface(Typeface.DEFAULT);
		editNewPwdConfirm = (EditText) findViewById(R.id.edit_new_pwd_confirm);
		editNewPwdConfirm.addTextChangedListener(new EditChangedListener(TYPE_EDIT_RESET_PWD_CONFIRM_NEW,this));
        editNewPwdConfirm.setTypeface(Typeface.DEFAULT);
		
		editSecurityPhone = (EditText) findViewById(R.id.edit_security_phone); //
		editSecurityPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SECURITY_PHONE,this));
		editBindSmsVerifyCode = (EditText) findViewById(R.id.edit_bind_security_phone_sms_verify_code);
		editBindSmsVerifyCode.addTextChangedListener(new EditChangedListener(TYPE_EDIT_BIND_SECURITY_PHONE_SMS_CODE,this));
		editUnbindSmsVerifyCode = (EditText) findViewById(R.id.edit_unbind_sms_verify_code); 		//解绑手机号是公共界面
		editUnbindSmsVerifyCode.addTextChangedListener(new EditChangedListener(TYPE_EDIT_UNBIND_SECURITY_PHONE_SMS_CODE,this));
		//初始化共用界面的标题
		textTitleUnbindPhone = (TextView) findViewById(R.id.title_unbind_phone);
		textTitleUnbindPhone.setText(getResources().getString(R.string.ui_activity_title_security_phone_number));
		
		textBindSendSmsVerifyCode = (TextView) findViewById(R.id.text_bind_security_phone_send_sms_verify_code);
		textUnbindSendSmsVerifyCode = (TextView) findViewById(R.id.text_unbind_send_sms_verify_code); //解绑手机号是公共界面,包括解绑安全手机号
		textPhoneUnbindStep1 = (TextView) findViewById(R.id.text_phone_unbind_step1);
		textPhoneUnbindStep2 = (TextView) findViewById(R.id.text_phone_unbind_step2);
		
		imageSecurityPhone = (ImageView)findViewById(R.id.image_delete_bind_security_phone);
		imageBindSmsVerifyCode = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_bind_security_phone);
		imageUnbindSmsVerifyCode = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_unbind_phone);
		imageDeleteOldPwd = (ImageView)findViewById(R.id.image_delete_input_old_password);
		imageDeleteNewPwd = (ImageView)findViewById(R.id.image_delete_input_new_password);
		imageDeleteConfirmNewPwd = (ImageView)findViewById(R.id.image_delete_input_confirm_new_password);
		
		imageHidePasswordForOld = (ImageView)findViewById(R.id.image_hide_password_for_old);
		imageShowPasswordForOld = (ImageView)findViewById(R.id.image_show_password_for_old);
		imageHidePasswordForNew = (ImageView)findViewById(R.id.image_hide_password_for_new);
		imageShowPasswordForNew = (ImageView)findViewById(R.id.image_show_password_for_new);
		imageHidePasswordForNewConfirm = (ImageView)findViewById(R.id.image_hide_password_for_new_confirm);
		imageShowPasswordForNewConfirm = (ImageView)findViewById(R.id.image_show_password_for_new_confirm);
		
		btnBindSecurityPhone = (Button)findViewById(R.id.btn_bind_security_phone);
		btnUnbindSecurityPhone = (Button)findViewById(R.id.btn_unbind_phone_complete);
		btnResetPwd = (Button)findViewById(R.id.btn_rest_pwd);
        
        layoutSecurityPhoneItem = (ViewGroup)findViewById(R.id.layout_security_phone_item);
        layoutSecurityPhoneItem.setOnClickListener(doOnclickSecurityPhoneItemOnClickListener);
        
		mBindSmsCodeTimeCount = new BindSmsCodeTimeCount(60000, 1000);
		mUnbindSmsCodeTimeCount = new UnbindSmsCodeTimeCount(60000, 1000);
	}
	public void getAccountInfo() {
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(this); //获取本地账号
    	if(null != gomeAccount){
	    	HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(this,gomeAccount.name); 
	    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
	    	mEmailActiveStatus = tableAccountInfo.get(Constants.KEY_EMAIL_ACTIVE_STATUS);
	    	mGomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
	    	mSecurityPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
    	}
	}
	void parseAction(){
		String action = getIntent().getAction();
		Log.i(TAG, "parseAction() 111 action:"+action+" mSecurityPhoneNumber:"+mSecurityPhoneNumber);
		if(ACTION_BIND_SECURITY_PHONE.equals(action)){
			showStep(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1);
		}else{ //若无action请求，显示默认主界面
			//modify by yimin.zhu
			//从其他页面跳转过来携带intent信息
			if("CheckPasswordActivity".equals(getIntent().getStringExtra("activityName"))) {
				Log.i("yimin","start by CheckPasswordActivity");
				if(null != mSecurityPhoneNumber){
					textPhoneUnbindStep1.setText(ActivityUtils.convertPhoneNumShow(mSecurityPhoneNumber));
					showStep(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP1);
				}else{
					showStep(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1);
				}
			} else {
				showStep(KEY_LAYOUT_PASSWORD_SECURITY_MAIN);
			}
		}
	}
	void showStep(String keyLayout,boolean...backMode){
		mCurrnetStep = keyLayout;
		ActivityUtils.closeKeyboard(this);
		if(KEY_LAYOUT_RESET_PWD.equals(keyLayout)) {
			editOldPwd.setText("");
			editNewPwd.setText("");
			editNewPwdConfirm.setText("");
		}
		//界面切换动画相关
        boolean isBackMode = false;
        boolean isUseAnimation = true;
        if(null != backMode &&  0 != backMode.length){
            isBackMode = true;
        }
        if((KEY_LAYOUT_PASSWORD_SECURITY_MAIN.equals(keyLayout)||KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1.equals(keyLayout)) 
                && !isBackMode){  //从activity加载的第一个界面无需再加动画
            isUseAnimation = false;
        }
        Log.i(TAG, "showStep keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size())
                +" isBackMode:"+isBackMode+" isUseAnimation:"+isUseAnimation);
		if(null != mLayouts){
			for (Map.Entry<String, LinearLayout> entry : mLayouts.entrySet()) {  
				   String key = entry.getKey();
				   LinearLayout linearLayout = entry.getValue();
				   if(null == linearLayout){
					   Log.i(TAG, "showStep key:"+key+" null == linearLayout");
					   break;
				   }
				   if(keyLayout.equals(key)){
                       if(isUseAnimation) {
                           if (isBackMode) {
                               linearLayout.startAnimation(mAnimationCloseEnter);
                           } else {
                               linearLayout.startAnimation(mAnimationOpenEnter);
                           }
                       }
                       linearLayout.setVisibility(View.VISIBLE);
				   }else{
                       if(isUseAnimation && View.VISIBLE == linearLayout.getVisibility()) {
                           final LinearLayout mLinearLayout = linearLayout;
                           Log.i(TAG,"showStep() *****11 to hide linearLayout:"+linearLayout+" mLinearLayout:"+mLinearLayout);
                           if (isBackMode) {
                               linearLayout.startAnimation(mAnimationCloseExit);
                               mAnimationCloseExit.setAnimationListener(new Animation.AnimationListener() {
                                   @Override
                                   public void onAnimationStart(Animation animation) {

                                   }
                                   @Override
                                   public void onAnimationEnd(Animation animation) {
                                       Log.i(TAG,"showStep() *****22 hide  mLinearLayout:"+mLinearLayout);
                                       mLinearLayout.setVisibility(View.GONE);
                                   }
                                   @Override
                                   public void onAnimationRepeat(Animation animation) {

                                   }
                               });
                           } else {
                               linearLayout.startAnimation(mAnimationOpenExit);
                               mAnimationOpenExit.setAnimationListener(new Animation.AnimationListener() {
                                   @Override
                                   public void onAnimationStart(Animation animation) {

                                   }
                                   @Override
                                   public void onAnimationEnd(Animation animation) {
                                       Log.i(TAG,"showStep() *****33 hide  mLinearLayout:"+mLinearLayout);
                                       mLinearLayout.setVisibility(View.GONE);
                                       doOnOpenAnimationEnd();
                                   }
                                   @Override
                                   public void onAnimationRepeat(Animation animation) {

                                   }
                               });
                           }
                       }else {
                           linearLayout.setVisibility(View.GONE);
                       }
				   }
			}
		}
	}
	/**
	 * 
	 * @param keyLayout
	 * @return 是否有上一步界面，若没有则返回false
	 */
	boolean showPreStep(String keyLayout){
		if(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1.equals(keyLayout)){
			String action = getIntent().getAction();
			Log.i(TAG, "showPreStep() KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1 action:"+action);
			if(ACTION_BIND_SECURITY_PHONE.equals(action)){
				Log.i(TAG, "showPreStep() KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1 111");
				return false;//若是从其他界面请求绑定安全手机号，点击back 关闭当前activity
			}else{
				Log.i(TAG, "showPreStep() KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1 222");
				mCurrnetStep = KEY_LAYOUT_PASSWORD_SECURITY_MAIN;
			}
		}else if(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP2.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1;
		}else if(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP1.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_PASSWORD_SECURITY_MAIN;

		}else if(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP2.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP1;
		}else if(KEY_LAYOUT_RESET_PWD.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_PASSWORD_SECURITY_MAIN;
		}else{
			return false;
		}
		showStep(mCurrnetStep,true);
		return true;
	}
	//////////////点击事件处理begin   //////////////////////////////
    /**
     * 点击找回密码按钮
     * @param view
     */
    public void doOnclickRetrievePwd(View view) {
		Intent i = new Intent(this, RetrievePwdActivity.class);
		startActivity(i);
		//finish();
    }
	public void doOnclickHidePasswordForOld(View view){
		editOldPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editOldPwd.setSelection(editOldPwd.getText().length());
		imageHidePasswordForOld.setVisibility(View.GONE);
		imageShowPasswordForOld.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForOld(View view){
		editOldPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editOldPwd.setSelection(editOldPwd.getText().length());
		imageHidePasswordForOld.setVisibility(View.VISIBLE);
		imageShowPasswordForOld.setVisibility(View.GONE);
	}
	public void doOnclickHidePasswordForNew(View view){
		editNewPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editNewPwd.setSelection(editNewPwd.getText().length());
		imageHidePasswordForNew.setVisibility(View.GONE);
		imageShowPasswordForNew.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForNew(View view){
		editNewPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editNewPwd.setSelection(editNewPwd.getText().length());
		imageHidePasswordForNew.setVisibility(View.VISIBLE);
		imageShowPasswordForNew.setVisibility(View.GONE);
	}
	public void doOnclickHidePasswordForNewConfirm(View view){
		editNewPwdConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editNewPwdConfirm.setSelection(editNewPwdConfirm.getText().length());
		imageHidePasswordForNewConfirm.setVisibility(View.GONE);
		imageShowPasswordForNewConfirm.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForNewConfirm(View view){
		editNewPwdConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editNewPwdConfirm.setSelection(editNewPwdConfirm.getText().length());
		imageHidePasswordForNewConfirm.setVisibility(View.VISIBLE);
		imageShowPasswordForNewConfirm.setVisibility(View.GONE);
	}
	//密码安全主界面,点击返回按钮
	public void doOnclickPwdSecurityBackButton(View view){
		finish();
	}
	//密码安全主界面,点击 重置密码条目
	public void doOnclickResetPwdItem(View view){
		Log.i(TAG, "doOnclickResetPwdItem() ");
		showStep(KEY_LAYOUT_RESET_PWD);
	}
    //打开动画结束恢复按钮点击事件
    void doOnOpenAnimationEnd(){
        Log.i(TAG,"doOnOpenAnimationEnd()");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"doOnOpenAnimationEnd() postDelayed");
                layoutSecurityPhoneItem.setOnClickListener(doOnclickSecurityPhoneItemOnClickListener);
            }
        },500);

    }
    View.OnClickListener doOnclickSecurityPhoneItemOnClickListener =  new View.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG,"doOnclickLoginOutOnClickListener.onClick()");
            doOnclickSecurityPhoneItem(arg0);
        }
    };
	//密码安全主界面,点击 安全号码条目
	public void doOnclickSecurityPhoneItem(View view){
		Log.i(TAG, "doOnclickSecurityPhoneItem() mSecurityPhoneNumber:"+mSecurityPhoneNumber);
		//modify by yimin.zhu for bugid GMOS-745
		Log.i("yimin", "doOnclickSecurityPhoneItem");
		if(null == mSecurityPhoneNumber){
			Intent i = new Intent(PasswordSecurityActivity.this,CheckPasswordActivity.class);
			i.putExtra("activityName", "PasswordSecurityActivity");
			//startActivity(i);
			startActivityForResult(i,REQUEST_CODE_PASSWORD_SECURITY);
		} else {
            layoutSecurityPhoneItem.setOnClickListener(null);
			textPhoneUnbindStep1.setText(ActivityUtils.convertPhoneNumShow(mSecurityPhoneNumber));
			showStep(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP1);
		}
		/*
		if(null != mSecurityPhoneNumber){
			textPhoneUnbindStep1.setText(ActivityUtils.convertPhoneNumShow(mSecurityPhoneNumber));
			showStep(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP1);
		}else{
			showStep(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP1);
		}*/
	}
	
	//重置密码界面，点击返回按钮
	public void doOnclickResetPwdBackButton(View view){
		onBackPressed();
	}
	//重置密码界面，点击删除输入原密码按钮
	public void doOnclickDeleteInputOldPwdButton(View view){
		editOldPwd.setText("");
	}
	//重置密码界面，点击删除输入新密码按钮
	public void doOnclickDeleteInputNewPwdButton(View view){
		editNewPwd.setText("");
	}
	//重置密码界面，点击删除输入确认新密码按钮
	public void doOnclickDeleteInputConfirmNewPwdButton(View view){
		editNewPwdConfirm.setText("");
	}
	
	//重置密码界面，点击完成按钮 重置密码
	public void doOnclickRestPwdOkButton(View view){
		if(checkResetPwdInput()){
			btnResetPwd.setEnabled(false);
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					mTableAccountInfo.clear();
					mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, mOldPassword);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_NEW_PWD, mNewPassword);
					if(AccountUtils.resetPwd(PasswordSecurityActivity.this, mTableAccountInfo)){
						AccountUtils.loginOut(PasswordSecurityActivity.this, mGomeId,mNewPassword); //退出登录在线程里面处理
						mHandler.post(new Runnable() { //ui线程
							@Override
							public void run() {
								//showStep(KEY_LAYOUT_PASSWORD_SECURITY_MAIN);
								//重置密码成功退出登录，并跳转登录界面
								Intent i = new Intent(PasswordSecurityActivity.this,LoginActivity.class);
								startActivity(i);
								finish();
							}
						});
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							btnResetPwd.setEnabled(true);
						}
					});

				}
			});
		}
	}
	//绑定安全号码第1步，点击返回按钮
	public void doOnclickSecurityPhoneStep1BackButton(View view){
		onBackPressed();
	}
	//绑定安全号码第1步，点击立即设置按钮
	public void doOnclickSecurityPhoneStep1SetNowButton(View view){
		showStep(KEY_LAYOUT_BIND_SECURITY_PHONE_STEP2);
	}
	//绑定安全号码第2步，点击返回按钮
	public void doOnclickSecurityPhoneStep2BackButton(View view){
		ActivityUtils.closeKeyboard(PasswordSecurityActivity.this);
		onBackPressed();
	}
	//绑定安全号码第2步，点击删除安全手机按钮
	public void doOnclickDeleteBindSecurityPhoneButton(View view){
		editSecurityPhone.setText("");
	}
	//绑定安全号码第2步，点击删除短信验证码按钮
	public void doOnclickDeleteSmsVerifyCodeForBindSecurityPhoneButton(View view){
		editBindSmsVerifyCode.setText("");
	}
	
	//绑定安全号码第2步，点击发送短信验证码按钮
	public void doOnclikSecurityPhoneSendSmsVerifyCode(View view){
		if(checkSecurityPhoneStep2InputPhone()){
			doSendSmsVerifyCode(true);
		}
	}
	//绑定安全号码第2步，点击完成按钮
	public void doOnclickBindSecurityPhoneCompleteButton(View view){
		if(checkSecurityPhoneInput()){
			doBindOrUnbindSecurityPhone(true);
		}
	}
	//解绑安全手机号第1步，点击返回按钮
	public void doOnclickUnbindPhoneStep1BackButton(View view){
		onBackPressed();
	}
	//解绑安全手机号第1步，点击解除绑定按钮,弹出提示框
	public void doOnlcikUnbindPhoneStep1Button(View view){
		showDialog(PasswordSecurityActivity.this,DIALOG_UNBIND_SECURITY_PHONE);
	}
	//解绑安全手机号第1步，提示框 点击确定按钮
	public void doOnclickUnbindPhoneOkButton(View view){
		textPhoneUnbindStep2.setText(ActivityUtils.convertPhoneNumShow(mSecurityPhoneNumber));
		dismissDialog();
		showStep(KEY_LAYOUT_UNBIND_SECURITY_PHONE_STEP2);
	}
	//解绑安全手机号第1步，提示框 点击取消按钮
	public void doOnclickDismissDialogButton(View view){
		dismissDialog();
	}
	//解绑安全手机号第2步，点击返回按钮
	public void doOnclickUnbindPhoneStep2BackButton(View view){
        Log.i(TAG,"doOnclickUnbindPhoneStep2BackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//解绑安全手机号第2步，点击删除验证码按钮按钮
	public void doOnclickDeleteSmsVerifyCodeButton(View view){
		editUnbindSmsVerifyCode.setText("");
	}
	//解绑安全手机号第2步，点击发送短信验证码按钮
	public void doOnclikUnbindSendSmsVerifyCode(View view){
		doSendSmsVerifyCode(false);
	}
	//解绑安全手机号第2步，点击确定按钮
	public void doOnlcikUnbindPhoneConpleteButton(View view){
		doBindOrUnbindSecurityPhone(false);
	}
	//////////////点击事件处理end  //////////////////////////////

	/**
	 * 发送短信验证码
	 * @param isBind  是否绑定模式
	 */
	void doSendSmsVerifyCode(final boolean isBind){
		Log.i(TAG, "doSendSmsVerifyCode() isBind:"+isBind+" editSecurityPhone:"+editSecurityPhone.getText().toString().trim());
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				if(isBind){ //绑定安全手机号
					textBindSendSmsVerifyCode.setClickable(false);//防止多次点击发送验证码
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND_SECURITY_PHONE);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editSecurityPhone.getText().toString().trim());
				}else{ 		//解绑安全手机号
					textUnbindSendSmsVerifyCode.setClickable(false);//防止多次点击发送验证码
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_UNBIND_SECURITY_PHONE);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mSecurityPhoneNumber);
				}
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				if(AccountUtils.sendSmsVerifyCodeFromServer(PasswordSecurityActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if(isBind){ //绑定
								mBindSmsCodeTimeCount.start();
							}else{     //解绑
								mUnbindSmsCodeTimeCount.start();
							}
						}
					});
				}else{
					Log.i(TAG, "doSendSmsVerifyCode() send sms fail ,reset clickable");
					if(isBind){ //绑定
						textBindSendSmsVerifyCode.setClickable(true);//防止多次点击发送验证码
					}else{     //解绑
						textUnbindSendSmsVerifyCode.setClickable(true);//防止多次点击发送验证码
					}
				}
			}
		});
	}
	/**
	 * 绑定/解绑安全手机号
	 * @param isBind 是否绑定
	 */
	void doBindOrUnbindSecurityPhone(final boolean isBind){
		if(isBind){
			btnBindSecurityPhone.setEnabled(false);
		}else{
			btnUnbindSecurityPhone.setEnabled(false);
		}
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken); 			//服务端使用
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);  //本地使用

				if(isBind){
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER, editSecurityPhone.getText().toString().trim());//输入的安全号
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND_SECURITY_PHONE);
					mTableAccountInfo.put(Constants.KEY_SMS_CODE,editBindSmsVerifyCode.getText().toString().trim()); //验证码接口使用
					
				}else{
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER, mSecurityPhoneNumber);//缓存手机号
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_UNBIND_SECURITY_PHONE);
					mTableAccountInfo.put(Constants.KEY_SMS_CODE,editUnbindSmsVerifyCode.getText().toString().trim());//验证码接口使用
				}

				//step1  先判断验证码是否正确	
				if(!AccountUtils.verifySmsCodeValidFromServer(PasswordSecurityActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if(isBind){
								btnBindSecurityPhone.setEnabled(true);
							}else{
								btnUnbindSecurityPhone.setEnabled(true);
							}
						}
					});

					Log.e(TAG, "doBindOrUnbindSecurityPhone() verify sms code error!!");
					return;
				}
				//step2 若绑定安全号失败返回，若解绑安全号失败则返回 
				if(isBind){
					if(!AccountUtils.bindFromServer(PasswordSecurityActivity.this, mTableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								btnBindSecurityPhone.setEnabled(true); 
							}
						});

						Log.e(TAG, "doBindOrUnbindSecurityPhone() bind security phone error!!");
						return;
					}
				}else{
					if(!AccountUtils.unbindFromServer(PasswordSecurityActivity.this, mTableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								btnUnbindSecurityPhone.setEnabled(true);
							}
						});

						Log.e(TAG, "doBindOrUnbindSecurityPhone() unbind security phone error!!");
						return;
					}
				}
				//step3  绑定/解绑安全号成功
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						ActivityUtils.closeKeyboard(PasswordSecurityActivity.this); //隐藏软键盘
						Intent data = new Intent();
						if(isBind){
							data.setAction(AccountInfoActivity.ACTION_BIND_SECURITY_PHONE_SUCCESS);
						}else{
							data.setAction(AccountInfoActivity.ACTION_UNBIND_SECURITY_PHONE_SUCCESS);
						}
						setResult(RESULT_OK,data);
						finish();
					}
				});
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if(isBind){
							btnBindSecurityPhone.setEnabled(true);
						}else{
							btnUnbindSecurityPhone.setEnabled(true);
						}
					}
				});
			}
		});
	}
	
	
	
	boolean checkResetPwdInput(){
		mOldPassword = editOldPwd.getText().toString().trim();
		mNewPassword = editNewPwd.getText().toString().trim();
		String confirmNewPwd = editNewPwdConfirm.getText().toString();
		if(mOldPassword.isEmpty()){
			editOldPwd.setFocusable(true);
			editOldPwd.requestFocus(); 
			ActivityUtils.alert(this,getResources().getString(R.string.alert_old_pwd_null));
		}else if(mNewPassword.isEmpty()){ //新密码为空
			editNewPwd.setFocusable(true);
			editNewPwd.requestFocus();
			ActivityUtils.alert(this,getResources().getString(R.string.alert_new_pwd_null));
		}else if(confirmNewPwd.isEmpty()){ //确认新密码为空
			editNewPwdConfirm.setFocusable(true);
			editNewPwdConfirm.requestFocus();
			ActivityUtils.alert(this,getResources().getString(R.string.alert_confirm_new_pwd_null));
		}else if(mNewPassword.equals(mOldPassword)){//新密码和旧密码相同
			ActivityUtils.alert(this,getResources().getString(R.string.alert_new_pwd_same_as_old));
		}else if(!mNewPassword.equals(confirmNewPwd)){
			ActivityUtils.alert(this,getResources().getString(R.string.alert_two_new_pwd_not_equal));
		}else{
			return true;
		}
		return false;
	}
	
	boolean checkSecurityPhoneInput(){
		if(editSecurityPhone.getText().toString().isEmpty()){
			ActivityUtils.alert(this,getResources().getString(R.string.alert_text_phone_num_null));
		}else if(editBindSmsVerifyCode.getText().toString().isEmpty()){
			ActivityUtils.alert(this,getResources().getString(R.string.alert_text_sms_verify_code_null));
		}else{
			return true;
		}
		
		return false;
	}
	//弹出框
	void showDialog(Context context,int dialogId){
    	LayoutInflater inflater =  LayoutInflater.from(context);
    	View layout = null;
		switch (dialogId) {
			case DIALOG_UNBIND_SECURITY_PHONE:
				layout = inflater.inflate(R.layout.alert_unbind_phone_layout,null);
				break;
			default:
				break;
		}
    	mDialog = new GomeAlertDialog.Builder(this).setView(layout).create();
    	Window window = mDialog.getWindow();
    	mDialog.show();
	}
	void dismissDialog(){
		Log.i(TAG, "dismissDialog()");
    	if(null != mDialog){
    		mDialog.dismiss();
    	}
	}
	class BindSmsCodeTimeCount extends CountDownTimer {
		public BindSmsCodeTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			textBindSendSmsVerifyCode.setClickable(false);
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		textBindSendSmsVerifyCode.setText(msg); 

		}
		@Override
		public void onFinish() {
			textBindSendSmsVerifyCode.setText(getResources().getString(R.string.register_send_again));
			textBindSendSmsVerifyCode.setClickable(true);  
		}
	}
	class UnbindSmsCodeTimeCount extends CountDownTimer {
		public UnbindSmsCodeTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			textUnbindSendSmsVerifyCode.setClickable(false);
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		textUnbindSendSmsVerifyCode.setText(msg); 

		}
		@Override
		public void onFinish() {
			textUnbindSendSmsVerifyCode.setText(getResources().getString(R.string.register_send_again));
			textUnbindSendSmsVerifyCode.setClickable(true);  
		}
	}
	@Override
	public void showAfterTextNotEmpty(int editType) {
		switch (editType) {
			case TYPE_EDIT_SECURITY_PHONE:
				imageSecurityPhone.setVisibility(View.VISIBLE);
				if(checkSecurityPhoneStep2InputAll()){
					btnBindSecurityPhone.setEnabled(true);
				}
				break;
			case TYPE_EDIT_BIND_SECURITY_PHONE_SMS_CODE:
				imageBindSmsVerifyCode.setVisibility(View.VISIBLE);
				if(checkSecurityPhoneStep2InputAll()){
					btnBindSecurityPhone.setEnabled(true);
				}
				break;
			case TYPE_EDIT_UNBIND_SECURITY_PHONE_SMS_CODE:
				imageUnbindSmsVerifyCode.setVisibility(View.VISIBLE);
				if(checkUnbindSecurityPhoneStep2InputAll()){
					btnUnbindSecurityPhone.setEnabled(true);
				}
				break;
			case TYPE_EDIT_RESET_PWD_OLD:
				imageDeleteOldPwd.setVisibility(View.VISIBLE);
				if(checkResetPwdInputAll()){
					btnResetPwd.setEnabled(true);
				}
				break;
			case TYPE_EDIT_RESET_PWD_NEW:
				imageDeleteNewPwd.setVisibility(View.VISIBLE);
				if(checkResetPwdInputAll()){
					btnResetPwd.setEnabled(true);
				}
				break;
			case TYPE_EDIT_RESET_PWD_CONFIRM_NEW:
				imageDeleteConfirmNewPwd.setVisibility(View.VISIBLE);
				if(checkResetPwdInputAll()){
					btnResetPwd.setEnabled(true);
				}
				break;
			default:
				break;
		}
		
	}

	@Override
	public void showAfterTextEmpty(int editType) {
		switch (editType) {
			case TYPE_EDIT_SECURITY_PHONE:
				imageSecurityPhone.setVisibility(View.GONE);
				btnBindSecurityPhone.setEnabled(false);
				break;
			case TYPE_EDIT_BIND_SECURITY_PHONE_SMS_CODE:
				imageBindSmsVerifyCode.setVisibility(View.GONE);
				btnBindSecurityPhone.setEnabled(false);
				break;
			case TYPE_EDIT_UNBIND_SECURITY_PHONE_SMS_CODE:
				imageUnbindSmsVerifyCode.setVisibility(View.GONE);
				btnUnbindSecurityPhone.setEnabled(false);
				break;
			case TYPE_EDIT_RESET_PWD_OLD:
				imageDeleteOldPwd.setVisibility(View.GONE);
				btnResetPwd.setEnabled(false);
				break;
			case TYPE_EDIT_RESET_PWD_NEW:
				imageDeleteNewPwd.setVisibility(View.GONE);
				btnResetPwd.setEnabled(false);
				break;
			case TYPE_EDIT_RESET_PWD_CONFIRM_NEW:
				imageDeleteConfirmNewPwd.setVisibility(View.GONE);
				btnResetPwd.setEnabled(false);
				break;
			default:
				break;
		}
		
	}
	boolean checkSecurityPhoneStep2InputPhone(){
		if(!ActivityUtils.isMobileNO(editSecurityPhone.getText().toString().trim())){
			ActivityUtils.alert(PasswordSecurityActivity.this, getResources().getString(R.string.alert_input_valid_phone));
			return false;
		}
		return true;
	}
	boolean checkSecurityPhoneStep2InputAll(){
		if(editSecurityPhone.getText().toString().trim().isEmpty()){
			return false;
		}else if(editBindSmsVerifyCode.getText().toString().trim().isEmpty()){
			return false;
		}
		return true;
	}
	boolean checkUnbindSecurityPhoneStep2InputAll(){
		if(editUnbindSmsVerifyCode.getText().toString().trim().isEmpty()){
			return false;
		}
		return true;
	}
	boolean checkResetPwdInputAll(){
		if(editOldPwd.getText().toString().trim().isEmpty()){
			return false;
		}else if(editNewPwd.getText().toString().trim().isEmpty()){
			return false;
		}else if(editNewPwdConfirm.getText().toString().trim().isEmpty()){
			return false;
		}
		return true;
	}
}
