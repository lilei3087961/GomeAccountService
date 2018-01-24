package com.gome.gomeaccountservice;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gome.gomeaccountservice.SmsReceiver.SmsCallback;
import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.AppManager;
import com.gome.gomeaccountservice.utils.BitmapUtils;
import com.gome.gomeaccountservice.utils.EditChangedListener;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import gome.app.GomeAlertDialog;

public class RegisterActivity extends Activity implements TextChangeCallback,SmsCallback{
	static final String TAG = Constants.TAG_PRE+"RegisterActivity";

    Animation mAnimationOpenEnter;
    Animation mAnimationOpenExit;
    Animation mAnimationCloseEnter;
    Animation mAnimationCloseExit;
	private TimeCountForPhoneRegisterSmsCode timeSmsCodeForPhoneRegister = null;
	private TimeCountForMailRegisterSendEmail timeSendEmailForMailRegister = null;
	private Bitmap bitmap = null;
	private String username = null;
	private String phoneNum = null;
	private String emailAddress = null;
	private String checkNum = null;
	private String secreteNum = null;
	Dialog mAlertDialog;
	private byte buff[] = new byte[125 * 250];
	public static final int PREPARE_REGISTER = 520;
	//public static final int SAVE_REGISTER_INFO = 521;
	public static final int REGISTER_SUCCESS = 522;
	public static final int UPLOAD_IMAGE = 523;
	public static final String REGISTER_USERNAME = "nickName";
	public static final String REGISTER_PASSWORD = "loginPwd";
	public static final String REGISTER_PHONENUM = "phoneNo";
	public static final String REGISTER_EMAIL = "mallAddress";
	public static final String REGISTER_REGISTERTYPE = "registerType";
	public static final String REGISTER_AUTHCODE = "authCode";
	
	SmsReceiver mSmsReceiver = null;
	//开机向导底部布局，用来控件对应模式显示隐藏
	LinearLayout layoutStep1PublicBootWizardBottom;//手机注册第一步，开机向导底部布局
	View layoutStep2PhoneBootWizardBottom;//手机注册第二步，开机向导底部布局
	LinearLayout layoutSetp2PhonePartSms; //手机号注册设置密码界面的短信验证码布局
	//开机向导底部按钮，用来控制按钮 enable和disbale
	TextView textStep1PublicBootWizardNextButton;//text_step1_public_boot_wizard_next_button
	TextView textBootWizardVeirfySmsCodeNextButton;//text_boot_wizard_verify_sms_code_next_button
	TextView textStep2PhoneBootWizardNextButton;//text_step2_phone_boot_wizard_next_button
	ImageView imageStep1PublicBootWizardRightArrow;//image_step1_public_boot_wizard_right_arrow
	ImageView imageBootWizardVerifySmsCodeRightArrow;//image_boot_wizard_verify_sms_code_right_arrow
	ImageView imageStep2PhoneBootWizardRightArrow;//image_step2_phone_boot_wizard_right_arrow
	
	CheckBox checkAutoVerifySmsCode;//check_auto_verify_sms_code
	
	HashMap<String, LinearLayout> mLayouts;
	
	LinearLayout layoutBootWizardVerifySmsCode;  //开机向导手机手机注册第二步，验证手机验证码
	LinearLayout layoutSetp1Phone;  //默认是手机注册界面
	LinearLayout layoutSetp1Mail;
	LinearLayout layoutSetp2Mail;
	LinearLayout layoutSetp2Phone;

	
	LinearLayout layoutSetp3Public;
	final String KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE = "bootWizardVerifySmsCode";   //开机向导验证手机验证码界面
	final String KEY_LAYOUT_STEP1_PHONE = "step1Phone";   //输入手机号界面
	final String KEY_LAYOUT_STEP1_MAIL = "step1Mail";     //输入邮箱界面
	final String KEY_LAYOUT_STEP2_MAIL = "step2Mail";     //邮箱激活界面
	final String KEY_LAYOUT_STEP2_PHONE = "step2Phone";   //手机注册设置密码界面
	final String KEY_LAYOUT_STEP3_PUBLIC = "step3Public"; //邮箱注册设置密码界面
	
	String mCurrnetStep = KEY_LAYOUT_STEP1_PHONE;
	public static final String  ACTION_REGIST_USE_CURRENT_PHONE = "actionRegistUseCurrentPhone";
	//
	ImageView imageSetAvatar;
	//删除图标
	ImageView imageDeleteAccountPhone;//
	ImageView imageDeleteAccountMail;//
	ImageView imageDeleteInputPwd;
	ImageView imageDeleteInputPwdAgain;
	ImageView imageDeleteInputPwdPhone;//image_delete_input_password_for_phone
	ImageView imageDeleteInputPwdAgainPhone;
	ImageView imageDeleteSmsVerifyCode;//image_delete_sms_verify_code_for_step2
	ImageView imageDeleteSmsVerifyCodeForBootWizard;//image_delete_sms_verify_code_for_boot_wizard
	//密码可见隐藏图标
	ImageView imageHidePasswordForPhone;//image_hide_password_for_password_phone
	ImageView imageShowPasswordForPhone;
	ImageView imageHidePasswordForAgaginPhone;
	ImageView imageShowPasswordForAgaginPhone;
	ImageView imageHidePasswordForPassword;//image_hide_password_for_password
	ImageView imageShowPasswordForPassword;
	ImageView imageHidePasswordForPasswordAgain;
	ImageView imageShowPasswordForPasswordAgain;
	
	TextView textSendSmsCodeForStep2Phone;//手机注册发送验证码按钮
	TextView textSendSmsCodeForBootWizard;//text_send_sms_verify_code_for_boot_wizard
	TextView textSendEmailForStep2Mail; //邮箱注册发送邮件text_step2_mail_resend_mail
	TextView textStep2SendEmailReminder; //text_setp2_send_email_remiander
	

	TextView textPhoneNumber;  //设置密码界面显示手机号
	TextView textPhoneNumberForSmsVerify;//开机向导第二步显示手机号
	TextView txtEmailRegister;  //邮箱注册按钮
	FrameLayout laoutStep1PublicNextButton;
	FrameLayout laoutStep2PhoneCompleteButton;//laout_step2_phone_complete_button
	//TextView textPhoneBootWizardStep1;  //自动填充手机号
	EditText editPhone;
	EditText editEmail;
	EditText editSmsCodeForStep2Phone; //
	EditText editSmsCodeForBootWizard;//edit_sms_verify_code_for_boot_wizard
	EditText editInputPwd;
	EditText editInputPwdAgain;
	EditText editInputPwdPhone;
	EditText editInputPwdAgainPhone;
	Button btnStep1PhoneNext;
	Button btnStep1EmailNext;
	Button btnStep2PhoneComplete; //btn_step2_phone_complete
	Button btnStep3PublicNext; //btn_step3_public_next

	
	final int TYPE_EDIT_PHONE = 1;
	final int TYPE_EDIT_MAIL = 2;
	final int TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE = 3;
	final int TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD = 4;
	final int TYPE_EDIT_INPUT_PWD = 5;
	final int TYPE_EDIT_INPUT_PWD_AGAIN = 6;
	final int TYPE_EDIT_INPUT_PWD_PHONE = 7;
	final int TYPE_EDIT_INPUT_PWD_AGAIN_PHONE = 8;
	
	HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	
	private String gomeId = null;
	String mNickName = null;
	String mEmailAddress = null;
	String mPhoneNumber = null;
	String mGomeId = null;
	String mRegisterType = null;
	String mLoginPwd = null;
	String mSex = null;
	String mUserLevel = null;
	String mServerAvatarPath = null;
    String mToken = null;
    String mAccountStartMode = null;  //账号启动模式
    private String mVerifyEmailAddress = null; //需激活的邮箱地址
    //server 返回状态变量
	String mServerResCode = null;
	String mServerResMsg = null;
    int mLoginTaskId;
	private static final HandlerThread sWorkerThread = new HandlerThread(
			"RegisterActivity-loader");
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(
			sWorkerThread.getLooper());
	private static final Handler mHandler = new Handler();
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {

		return null;
	}
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {

	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        mLoginTaskId = this.getIntent().getIntExtra(LoginActivity.KEY_LOGIN_TASKID,0);
		Log.i(TAG, "onCreate() mLoginTaskId:"+mLoginTaskId);
		super.onCreate(savedInstanceState);
		// 注册接收短信广播receiver    
		mSmsReceiver = new SmsReceiver(this);    
        IntentFilter receiverFilter = new IntentFilter(ActivityUtils.ACTION_SMS_RECEIVER);    
	    registerReceiver(mSmsReceiver, receiverFilter);  
		
		setContentView(R.layout.register_activity);
		initView();
		verifyAccountStartMode();
	}
	@Override
	public void onGetSmsCode(String smsCode) {
		//获取到短信验证码，更新短信验证码
		Log.i(TAG, "onGetSmsCode smsCode:"+smsCode+" mCurrnetStep:"+mCurrnetStep+" checkAutoVerifySmsCode.isChecked():"+checkAutoVerifySmsCode.isChecked());
		if(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE.equals(mCurrnetStep) && checkAutoVerifySmsCode.isChecked()){ //当前在手机验证码自动验证界面
			editSmsCodeForBootWizard.setText(smsCode);
		}
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy()");
		if(null != mSmsReceiver){
			unregisterReceiver(mSmsReceiver);
		}
	}
	/**
	 * 检查当前的模式
	 */
	void verifyAccountStartMode(){
		mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(RegisterActivity.this);
		Log.i(TAG, "verifyAccountStartMode() mAccountStartMode:"+mAccountStartMode);
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
			//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			txtEmailRegister.setVisibility(View.INVISIBLE);//开机向导模式不显示邮箱注册链接
			//手机注册第一步
			laoutStep1PublicNextButton.setVisibility(View.INVISIBLE);
			layoutStep1PublicBootWizardBottom.setVisibility(View.VISIBLE); 
			//手机注册第二步
			layoutSetp2PhonePartSms.setVisibility(View.GONE);
			laoutStep2PhoneCompleteButton.setVisibility(View.GONE);
			layoutStep2PhoneBootWizardBottom.setVisibility(View.VISIBLE); 
			
			final String curPhone = ActivityUtils.getCurrentPhoneNumber(RegisterActivity.this);
			Log.i(TAG, "verifyAccountStartMode() 222 mAccountStartMode:"+mAccountStartMode+" curPhone:"+curPhone);
			if(null != curPhone){ //开机向导模式且可获取当前手机号，则显示自动填充手机号的注册流程
				editPhone.setText(curPhone);
			}
            ActivityUtils.sendHideNavigationBarBrocast(RegisterActivity.this);
            getWindow().getDecorView().showNavigationBarDivider(false);
			
		}else{
			//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			txtEmailRegister.setVisibility(View.VISIBLE);
			//
			//手机注册第一步
			laoutStep1PublicNextButton.setVisibility(View.VISIBLE);
			layoutStep1PublicBootWizardBottom.setVisibility(View.INVISIBLE); //手机注册第一步底部
			//手机注册第二步
			layoutSetp2PhonePartSms.setVisibility(View.VISIBLE);
			laoutStep2PhoneCompleteButton.setVisibility(View.VISIBLE);
			layoutStep2PhoneBootWizardBottom.setVisibility(View.GONE);
		}
		showStep(KEY_LAYOUT_STEP1_PHONE,false); //默认显示普通手机注册流程

	}
//	void verifyAccountStartModeInThread(){
//		mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(RegisterActivity.this);
//		Log.i(TAG, "verifyAccountStartMode() 111 mAccountStartMode:"+mAccountStartMode);
//		//step1处理邮箱链接是否显示
//		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
//			txtEmailRegister.setVisibility(View.INVISIBLE);//开机向导模式不显示邮箱注册链接
//		}else{
//			txtEmailRegister.setVisibility(View.VISIBLE);
//		}
//		//step2处理短信验证码布局是否显示
//		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
//			final String curPhone = ActivityUtils.getCurrentPhoneNumber(RegisterActivity.this);
//			Log.i(TAG, "verifyAccountStartMode() 222 mAccountStartMode:"+mAccountStartMode+" curPhone:"+curPhone);
//			if(null != curPhone){ //开机向导模式且可获取当前手机号，则显示自动填充手机号的注册流程
//				mTableAccountInfo.clear();
//	        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, curPhone);
//	        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);//注册验证手机号
//	        	sWorker.post(new Runnable() { 
//	    			@Override
//	    			public void run() {
//	    	        	if(AccountUtils.verifyPhoneOrEmailValidFromServer(RegisterActivity.this, mTableAccountInfo)){
//	    	        		mHandler.post(new Runnable() {
//								@Override
//								public void run() {
//			    	        		Log.i(TAG, "verifyAccountStartMode() 333 ");
//			    					textPhoneBootWizardStep1.setText(ActivityUtils.convertPhoneNumShow(curPhone));
//			    					layoutSetp2PhonePartSms.setVisibility(View.GONE); //开机向导模式且能获取当前手机号,且手机号未注册，才无需验证手机验证码
//			    					showStep(KEY_LAYOUT_STEP1_BOOT_WIZARD_PHONE);
//								}
//							});
//	    	        	}else{ //
//	    	        		mHandler.post(new Runnable() {
//								@Override
//								public void run() {
//			    	        		Log.i(TAG, "verifyAccountStartMode() 444 ");
//			    	        		layoutSetp2PhonePartSms.setVisibility(View.VISIBLE);
//			    	        		showStep(KEY_LAYOUT_STEP1_PHONE);
//								}
//							});
//	    	        	}
//	    			}
//	    		});
//	        	return;  //若当前手机号存在 返回
//			}
//		}
//		Log.i(TAG, "verifyAccountStartMode() 555 ");
//		layoutSetp2PhonePartSms.setVisibility(View.VISIBLE);
//		showStep(KEY_LAYOUT_STEP1_PHONE); //默认显示普通手机注册流程
//
//	}
	public void initView() {
		Log.i(TAG, "init view");
        mAnimationOpenEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_enter_layout);
        mAnimationOpenExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_exit_layout);
        mAnimationCloseEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_enter_layout);
        mAnimationCloseExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_exit_layout);
        
		layoutSetp1Phone = (LinearLayout)findViewById(R.id.layout_register_step1_public); //输入手机号界面
		layoutSetp1Mail = (LinearLayout)findViewById(R.id.layout_register_step1_mail);    //输入邮箱界面
		layoutSetp2Mail = (LinearLayout)findViewById(R.id.layout_register_step2_mail);	  //邮箱激活界面
		layoutSetp2Phone = (LinearLayout)findViewById(R.id.layout_register_step2_phone);  //手机注册设置密码界面
		layoutSetp3Public = (LinearLayout)findViewById(R.id.layout_register_step3_public);//邮箱注册设置密码界面
		layoutBootWizardVerifySmsCode = (LinearLayout)findViewById(R.id.layout_boot_wizard_verify_sms_code);//开机向导注册短信验证码验证界面
				
		mLayouts = new HashMap<String, LinearLayout>();
		mLayouts.put(KEY_LAYOUT_STEP1_PHONE, layoutSetp1Phone);
		mLayouts.put(KEY_LAYOUT_STEP1_MAIL, layoutSetp1Mail);
		mLayouts.put(KEY_LAYOUT_STEP2_MAIL, layoutSetp2Mail);
		mLayouts.put(KEY_LAYOUT_STEP2_PHONE, layoutSetp2Phone);
		mLayouts.put(KEY_LAYOUT_STEP3_PUBLIC, layoutSetp3Public);
		mLayouts.put(KEY_LAYOUT_STEP3_PUBLIC, layoutSetp3Public);
		mLayouts.put(KEY_LAYOUT_STEP3_PUBLIC, layoutSetp3Public);
		mLayouts.put(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE, layoutBootWizardVerifySmsCode);
		
		layoutSetp2PhonePartSms = (LinearLayout)findViewById(R.id.layout_sms_verify_code);//邮箱注册设置密码界面
		layoutStep1PublicBootWizardBottom = (LinearLayout)findViewById(R.id.layout_step1_public_boot_wizard_boottom);
		layoutStep2PhoneBootWizardBottom = findViewById(R.id.layout_step2_phone_boot_wizard_boottom);
		
		imageSetAvatar = (ImageView)findViewById(R.id.image_set_avatar);
		
		textSendSmsCodeForStep2Phone = (TextView)findViewById(R.id.text_send_sms_verify_code_for_step2_phone);
		textSendSmsCodeForBootWizard = (TextView)findViewById(R.id.text_send_sms_verify_code_for_boot_wizard);
		textSendEmailForStep2Mail = (TextView)findViewById(R.id.text_step2_mail_resend_mail);
		textStep2SendEmailReminder = (TextView)findViewById(R.id.text_setp2_send_email_remiander);
		
		textPhoneNumber = (TextView)findViewById(R.id.text_phone_number);
		textPhoneNumberForSmsVerify = (TextView)findViewById(R.id.text_phone_number_for_verify_sms_code);
		txtEmailRegister = (TextView)findViewById(R.id.txt_email_register);
		laoutStep1PublicNextButton = (FrameLayout)findViewById(R.id.laout_step1_public_next_button);
		laoutStep2PhoneCompleteButton = (FrameLayout)findViewById(R.id.laout_step2_phone_complete_button);
		editPhone = (EditText)findViewById(R.id.edit_phone);
		editPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_PHONE,this));
		editEmail = (EditText)findViewById(R.id.edit_mail);
		editEmail.addTextChangedListener(new EditChangedListener(TYPE_EDIT_MAIL,this));
		imageDeleteAccountPhone = (ImageView)findViewById(R.id.image_delete_account_phone);
		imageDeleteAccountMail = (ImageView)findViewById(R.id.image_delete_account_mail);
		
		editSmsCodeForStep2Phone = (EditText)findViewById(R.id.edit_sms_verify_code_for_setp2_phone);
		editSmsCodeForStep2Phone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE,this));
		editSmsCodeForBootWizard = (EditText)findViewById(R.id.edit_sms_verify_code_for_boot_wizard);//
		editSmsCodeForBootWizard.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD,this));
		
		editInputPwd = (EditText)findViewById(R.id.edit_input_password);
		editInputPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD,this));
        editInputPwd.setTypeface(Typeface.DEFAULT);
		editInputPwdAgain = (EditText)findViewById(R.id.edit_input_password_again);
		editInputPwdAgain.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD_AGAIN,this));
        editInputPwdAgain.setTypeface(Typeface.DEFAULT);
		imageDeleteSmsVerifyCode = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_step2);
		imageDeleteSmsVerifyCodeForBootWizard = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_boot_wizard);
		imageDeleteInputPwd = (ImageView)findViewById(R.id.image_delete_input_password);
		imageDeleteInputPwdAgain = (ImageView)findViewById(R.id.image_delete_input_password_again);
		imageHidePasswordForPassword = (ImageView)findViewById(R.id.image_hide_password_for_password);
		imageShowPasswordForPassword = (ImageView)findViewById(R.id.image_show_password_for_password);
		imageHidePasswordForPasswordAgain = (ImageView)findViewById(R.id.image_hide_password_for_password_again);
		imageShowPasswordForPasswordAgain = (ImageView)findViewById(R.id.image_show_password_for_password_again);
		
		btnStep3PublicNext = (Button)findViewById(R.id.btn_step3_public_next);
		
		editInputPwdPhone = (EditText)findViewById(R.id.edit_input_password_for_phone);
		editInputPwdPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD_PHONE,this));
        editInputPwdPhone.setTypeface(Typeface.DEFAULT);
		editInputPwdAgainPhone = (EditText)findViewById(R.id.edit_input_password_again_for_phone);
		editInputPwdAgainPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD_AGAIN_PHONE,this));
        editInputPwdAgainPhone.setTypeface(Typeface.DEFAULT);
		imageDeleteInputPwdPhone = (ImageView)findViewById(R.id.image_delete_input_password_for_phone);
		imageDeleteInputPwdAgainPhone = (ImageView)findViewById(R.id.image_delete_input_password_again_for_phone);
		imageHidePasswordForPhone = (ImageView)findViewById(R.id.image_hide_password_for_password_phone);
		imageShowPasswordForPhone = (ImageView)findViewById(R.id.image_show_password_for_password_phone);
		imageHidePasswordForAgaginPhone = (ImageView)findViewById(R.id.image_hide_password_for_password_again_phone);
		imageShowPasswordForAgaginPhone = (ImageView)findViewById(R.id.image_show_password_for_password_again_phone);
		
		btnStep2PhoneComplete = (Button)findViewById(R.id.btn_step2_phone_complete);
		
		
		btnStep1PhoneNext = (Button)findViewById(R.id.btn_step1_phone_next);
		btnStep1EmailNext = (Button)findViewById(R.id.btn_step1_mail_next);
		timeSmsCodeForPhoneRegister = new TimeCountForPhoneRegisterSmsCode(60000, 1000);
		timeSendEmailForMailRegister = new TimeCountForMailRegisterSendEmail(60000, 1000);
		
		//开机向导底部布局
		textStep1PublicBootWizardNextButton = (TextView)findViewById(R.id.text_step1_public_boot_wizard_next_button);
		textBootWizardVeirfySmsCodeNextButton = (TextView)findViewById(R.id.text_boot_wizard_verify_sms_code_next_button);
		textStep2PhoneBootWizardNextButton = (TextView)findViewById(R.id.text_step2_phone_boot_wizard_next_button);
		imageStep1PublicBootWizardRightArrow = (ImageView)findViewById(R.id.image_step1_public_boot_wizard_right_arrow);
		imageBootWizardVerifySmsCodeRightArrow = (ImageView)findViewById(R.id.image_boot_wizard_verify_sms_code_right_arrow);
		imageStep2PhoneBootWizardRightArrow = (ImageView)findViewById(R.id.image_step2_phone_boot_wizard_right_arrow);
		
		checkAutoVerifySmsCode = (CheckBox)findViewById(R.id.check_auto_verify_sms_code);
		checkAutoVerifySmsCode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.i(TAG, "onCheckedChanged() isChecked:"+isChecked);
				doOnCheckedChanged(buttonView,isChecked);
			}
		});
	}
	void doOnCheckedChanged(CompoundButton buttonView, boolean isChecked){
		Log.i(TAG, "doOnCheckedChanged() isChecked:"+isChecked);
		if(!isChecked){
			ActivityUtils.alert(this, getResources().getString(R.string.alert_auto_verify_sms_code_not_checked));
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "onResume() mVerifyEmailAddress:"+mVerifyEmailAddress);
        verifyEmailActived(false);
        mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(RegisterActivity.this);
        Log.i(TAG, "onResume() 22 mAccountStartMode:"+mAccountStartMode);
        if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
            ActivityUtils.sendHideNavigationBarBrocast(RegisterActivity.this);
            getWindow().getDecorView().showNavigationBarDivider(false);
        }
	}

    /**
     * 验证邮箱是否激活
     * @param isFromClick  是否手动点击按钮
     */
	void verifyEmailActived(final boolean isFromClick){
        Log.i(TAG, "verifyEmailActived() mVerifyEmailAddress:"+mVerifyEmailAddress+" isFromClick:"+isFromClick);
        if(null != mVerifyEmailAddress){//邮箱未激活
            sWorker.post(new Runnable() {
                @Override
                public void run() {
                    mTableAccountInfo.clear();
                    mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
                    mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mVerifyEmailAddress);
                    mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);
                    if(AccountUtils.verifyEmailStatusFromServer(RegisterActivity.this, mTableAccountInfo,true)){  //从服务器判断邮箱是否激活
                        mLoginPwd = editInputPwd.getText().toString().trim();
                        mTableAccountInfo.clear();
                        mTableAccountInfo.put(Constants.KEY_ACCOUNT_USER_NAME, mVerifyEmailAddress);//服务端接口修改
                        mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mVerifyEmailAddress);//for 无server test
                        mTableAccountInfo.put(Constants.KEY_ACCOUNT_REGISTER_TYPE, Constants.REGISTER_TYPE_EMAIL);//for 无server test
                        mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, mLoginPwd);
                        Log.i(TAG, "verifyEmailActived()  mVerifyEmailAddress:"+mVerifyEmailAddress+" mLoginPwd:"+mLoginPwd);
                        if(AccountUtils.login(RegisterActivity.this, mTableAccountInfo)){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {  //若判断邮箱激活，则立即登录
                                    doRegisterLogined();
                                }
                            });
                        }
                    }else{
                        if(isFromClick){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ActivityUtils.alert(RegisterActivity.this,getResources().getString(R.string.txt_gome_account_not_verified));
                                }
                            });
                        }
                    }
                }
            });

        }
    }
	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed() mCurrnetStep:"+mCurrnetStep);
		if(!showPreStep(mCurrnetStep)){
            ActivityUtils.closeKeyboard(this); //切换界面关闭软键盘
			finish();
		}
		if(KEY_LAYOUT_STEP1_PHONE != mCurrnetStep || KEY_LAYOUT_STEP1_MAIL != mCurrnetStep){  //若当前界面不是setp1则不执行系统返回键处理
			showPreStep(mCurrnetStep);
			return;
		}
		//super.onBackPressed();
	}
	boolean showPreStep(String keyLayout){//mRegisterType
		if(KEY_LAYOUT_STEP3_PUBLIC == keyLayout){  //当前邮箱注册设置密码界面
			mCurrnetStep = KEY_LAYOUT_STEP1_MAIL;
		}else if(KEY_LAYOUT_STEP2_PHONE == keyLayout){// || KEY_LAYOUT_STEP2_MAIL == keyLayout
			//String curPhone = ActivityUtils.getCurrentPhoneNumber(RegisterActivity.this);
	    	boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
			Log.i(TAG, "showPreStep keyLayout:"+keyLayout+" mAccountStartMode:"+mAccountStartMode+" notShowSmsCodeLayout:"+notShowSmsCodeLayout);
			if(notShowSmsCodeLayout){
				mCurrnetStep = KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE;
			}else{
				mCurrnetStep = KEY_LAYOUT_STEP1_PHONE;
			}
		}else if(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE == keyLayout){
			mCurrnetStep = KEY_LAYOUT_STEP1_PHONE;
		}else{
			return false;
		}
		showStep(mCurrnetStep,true);
		return true;
	}

    /**
     * 
     * @param keyLayout  需要显示的布局
     * @param backMode   是否返回模式
     * @param useAnimation 是否使用动画 默认使用动画
     */
	void showStep(String keyLayout,boolean backMode,boolean...useAnimation){
		mCurrnetStep = keyLayout;
        boolean isBackMode = false;
        boolean isUseAnimation = true;
        if(backMode){
            isBackMode = true;
        }
        if(KEY_LAYOUT_STEP1_PHONE.equals(keyLayout) && !isBackMode){  //从activity加载的第一个界面无需再加动画
            isUseAnimation = false;
        }
        if(null != useAnimation && 1 == useAnimation.length && !useAnimation[0]){ //传递false的情况
            isUseAnimation = false;
        }
		Log.i(TAG, "showStep keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size())
                +" isBackMode:"+isBackMode+" isUseAnimation:"+isUseAnimation+
                " useAnimation:"+useAnimation+" useAnimation.length:"+(null != useAnimation ? useAnimation.length:"null"));
		if(KEY_LAYOUT_STEP1_MAIL.equals(mCurrnetStep)) {
			Log.i("yimin","register email");
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		} else {
			Log.i("yimin","register others");
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
        ActivityUtils.closeKeyboard(this); //切换界面关闭软键盘
		if(KEY_LAYOUT_STEP2_PHONE.equals(mCurrnetStep)){ //处理返回到上一个输入手机界面，下一个界面发送短信验证码未更新的情况
			initTextSendSmsCodeForStep2Phone();
		}else if(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE.equals(mCurrnetStep)){
            initTextSendSmsCodeForBootWizard();
        }
		if(null != mLayouts){
			for (Map.Entry<String, LinearLayout> entry : mLayouts.entrySet()) {
				   String key = entry.getKey();
				   LinearLayout linearLayout = entry.getValue();
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
	//////////////////////////点击事件处理begin
	/**
	 * 点击以后验证
	 * @param view
	 */
	public void doVerifyLater(View view){
		mAlertDialog = new GomeAlertDialog.Builder(this)
		.setTitle(getResources().getString(R.string.txt_verify_later_alert_txt))
		.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override   
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub  
				Log.i(TAG, "doVerifyLater.setPositiveButton.onClick dissmiss");
				Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);  
				startActivity(intent);
				mAlertDialog.dismiss();
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
     * 点击已验证按钮
     * @param view
     */
    public void doAlreadyVerified(View view){
        Log.i(TAG,"doAlreadyVerified() ");
        verifyEmailActived(true);
    }
	public void doOnclickHidePasswordForPhone(View view){
		editInputPwdPhone.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editInputPwdPhone.setSelection(editInputPwdPhone.getText().length());
		imageHidePasswordForPhone.setVisibility(View.GONE);
		imageShowPasswordForPhone.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForPhone(View view){
		editInputPwdPhone.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editInputPwdPhone.setSelection(editInputPwdPhone.getText().length());
		imageHidePasswordForPhone.setVisibility(View.VISIBLE);
		imageShowPasswordForPhone.setVisibility(View.GONE);
	}
	public void doOnclickHidePasswordForAgainPhone(View view){
		editInputPwdAgainPhone.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editInputPwdAgainPhone.setSelection(editInputPwdAgainPhone.getText().length());
		imageHidePasswordForAgaginPhone.setVisibility(View.GONE);
		imageShowPasswordForAgaginPhone.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForAgainPhone(View view){
		editInputPwdAgainPhone.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editInputPwdAgainPhone.setSelection(editInputPwdAgainPhone.getText().length());
		imageHidePasswordForAgaginPhone.setVisibility(View.VISIBLE);
		imageShowPasswordForAgaginPhone.setVisibility(View.GONE);
	}
	
	public void doOnclickHidePasswordForPassword(View view){
		editInputPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editInputPwd.setSelection(editInputPwd.getText().length());
		imageHidePasswordForPassword.setVisibility(View.GONE);
		imageShowPasswordForPassword.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForPassword(View view){
		editInputPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editInputPwd.setSelection(editInputPwd.getText().length());
		imageHidePasswordForPassword.setVisibility(View.VISIBLE);
		imageShowPasswordForPassword.setVisibility(View.GONE);
	}
	public void doOnclickHidePasswordForPasswordAgain(View view){
		editInputPwdAgain.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editInputPwdAgain.setSelection(editInputPwdAgain.getText().length());
		imageHidePasswordForPasswordAgain.setVisibility(View.GONE);
		imageShowPasswordForPasswordAgain.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForPasswordAgain(View view){
		editInputPwdAgain.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editInputPwdAgain.setSelection(editInputPwdAgain.getText().length());
		imageHidePasswordForPasswordAgain.setVisibility(View.VISIBLE);
		imageShowPasswordForPasswordAgain.setVisibility(View.GONE);
	}
	/*
	 * 手机注册第一步 点击邮箱注册按钮
	 */
	public void doOnclickEmailRegister(View view){
		showStep(KEY_LAYOUT_STEP1_MAIL,false,false);
	}
	/*
	 * 邮箱注册第一步 点击手机注册按钮
	 */
	public void doOnclickPhoneRegister(View view){
		showStep(KEY_LAYOUT_STEP1_PHONE,true,false);
	}
	
	/*
	 * 第1步 点击返回按钮
	 */
	public void doOnclickStep1PublicBackButton(View view){
        ActivityUtils.closeKeyboard(this); //切换界面关闭软键盘
		finish();
	}
	//手机注册第1步，点击删除输入的手机账号按钮
	public void doOnclickDeleteAccountPhoneButton(View view) {
		editPhone.setText("");
	}
	//邮箱注册第1步，点击删除输入的邮箱账号按钮
	public void doOnclickDeleteAccountMailButton(View view) {
		editEmail.setText("");
	}
	public void doOnclickBootWizardVerifySmsCodeBackButton(View view) {
        Log.i(TAG,"doOnclickBootWizardVerifySmsCodeBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	View.OnClickListener doOnclickStep1PublicNextOnClickListener =  new View.OnClickListener(){
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
            Log.i(TAG,"doOnclickStep1PublicNextOnClickListener.onClick()");
            doOnclickStep1PublicNextButton(arg0);
	}
    };
	/**
	 * 第1步 输入手机号点击下一步按钮，默认是手机号注册
	 * @param view
	 */
	public void doOnclickStep1PublicNextButton(View view){
		final String strInput = editPhone.getText().toString().trim();
		Log.i(TAG, "doOnclickStep1PublicNextButton() mAccountStartMode:"+mAccountStartMode+" strInput:"+strInput);
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
			textStep1PublicBootWizardNextButton.setEnabled(false);
			imageStep1PublicBootWizardRightArrow.setEnabled(false);
            textStep1PublicBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
            textStep1PublicBootWizardNextButton.setOnClickListener(null);
            imageStep1PublicBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
            imageStep1PublicBootWizardRightArrow.setOnClickListener(null);
		}else{
			btnStep1PhoneNext.setEnabled(false);
		}
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(chekcStep1PhoneInput(strInput)){  //输入的手机号
					mVerifyEmailAddress = null;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mRegisterType = Constants.REGISTER_TYPE_PHONE;
							if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
								showStep(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE,false);
								textPhoneNumberForSmsVerify.setText(ActivityUtils.convertPhoneNumShow(strInput));
							}else{
								showStep(KEY_LAYOUT_STEP2_PHONE,false);
								textPhoneNumber.setText(ActivityUtils.convertPhoneNumShow(strInput));
							}

						}
					});
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
							textStep1PublicBootWizardNextButton.setEnabled(true);
							imageStep1PublicBootWizardRightArrow.setEnabled(true);
                            textStep1PublicBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                            textStep1PublicBootWizardNextButton.setOnClickListener(doOnclickStep1PublicNextOnClickListener);
                            imageStep1PublicBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                            imageStep1PublicBootWizardRightArrow.setOnClickListener(doOnclickStep1PublicNextOnClickListener);
						}else{
							btnStep1PhoneNext.setEnabled(true);
						}
					}
				});

			}
		});

	}
	/**
	 * 开机向导手机自动填充注册第1步  点击下一步按钮
	 * @param view
	 */
	public void doOnclickStep1BootWizardPhoneNextButton(View view){
		mRegisterType = Constants.REGISTER_TYPE_PHONE;
		String curPhone = ActivityUtils.getCurrentPhoneNumber(RegisterActivity.this);
		Log.i(TAG, "doOnclickStep1BootWizardPhoneNextButton()  mAccountStartMode:"+mAccountStartMode+" curPhone:"+curPhone);
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode) && null != curPhone){
			textPhoneNumber.setText(ActivityUtils.convertPhoneNumShow(curPhone));
		}
		showStep(KEY_LAYOUT_STEP2_PHONE,false);
	}
	/*
	 * 第1步 输入邮箱点击下一步按钮
	 */
	public void doOnclickStep1MailNextButton(View view){
		final String strInput = editEmail.getText().toString().trim();
		btnStep1EmailNext.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(chekcStep1EmailInput(strInput)){  //输入的邮箱
					mVerifyEmailAddress = null;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if(ActivityUtils.isEmail(strInput)){ //输入邮箱
								mRegisterType = Constants.REGISTER_TYPE_EMAIL;
								showStep(KEY_LAYOUT_STEP3_PUBLIC,false);  //输入邮箱先输入密码，
							}
						}
					});
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						btnStep1EmailNext.setEnabled(true);
					}
				});

			}
		});
	}
	/**
	 * 邮箱注册最后一步,显示邮箱界面处理
	 */
	void showEmailVerify(){
		mRegisterType = Constants.REGISTER_TYPE_EMAIL;
		mVerifyEmailAddress = editEmail.getText().toString().trim();
		ActivityUtils.closeKeyboard(RegisterActivity.this);
		//更新新界面提示
		String strFormat = getResources().getString(R.string.txt_send_email_reminder_part1);
		String str = String.format(strFormat, mVerifyEmailAddress);
		textStep2SendEmailReminder.setText(str);
		showStep(KEY_LAYOUT_STEP2_MAIL,false);
		//后面执行发送邮箱动作
	}
	/**
	 * 邮箱注册第2步 点击返回按钮，原来的邮箱验证变成邮箱注册的最后一步。
	 * @param view
	 */
	public void doOnclickStep2MailBackButton(View view){
		finish();
	}
	/**
	 * 邮箱注册第2步 点击查收邮件
	 * @param view
	 */
	public void doOnclickCheckEmail(View view){
		//ActivityUtils.verifyMail(this);
		ActivityUtils.verifyMail(this,editEmail.getText().toString().trim());
	}
	/**
	 * 邮箱注册第2步 点击重新发送邮件，****等服务器接口，已经ui是否更新界面
	 * @param view
	 */
	public void doOnclickResendEmail(View view){
		Log.i(TAG, "doOnclickResendEmail() ");
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mVerifyEmailAddress);
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);
				
				if(AccountUtils.sendEmailFromServer(RegisterActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							timeSendEmailForMailRegister.start();
						}
					});
				}
			}
		});
		//是否需要更新60秒倒计时看ui
	}
	/**
	 * 手机注册第2步 点击返回按钮 
	 * @param view
	 */
	public void doOnclickStep2PhoneBackButton(View view){
		//String curPhone = ActivityUtils.getCurrentPhoneNumber(RegisterActivity.this);
    	boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
		Log.i(TAG,"doOnclickStep2PhoneBackButton() mAccountStartMode:"+mAccountStartMode+" notShowSmsCodeLayout:"+notShowSmsCodeLayout
                +"mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//手机注册step2，开机向导校验验证码 ，点击删除输入验证码按钮
	public void doOnclickDeleteBootWizardSmsVerifyCodeButton(View view){
		Log.i(TAG, "doOnclickDeleteBootWizardSmsVerifyCodeButton() ");
		editSmsCodeForBootWizard.setText("");
	}
	//手机注册第2步，点击删除短信验证码按钮
	public void doOnclickDeleteSmsVerifyCodeButton(View view){
		editSmsCodeForStep2Phone.setText("");
	}
	//手机注册第2步，点击删除 输入密码按钮
	public void doOnclickDeleteInputPwdForPhoneButton(View view){
		editInputPwdPhone.setText("");
	}
	//手机注册第2步，点击删除 再次输入密码按钮
	public void doOnclickDeleteInputPwdAgainForPhoneButton(View view){
		editInputPwdAgainPhone.setText("");
	}
	/**
	 * 手机注册第2步点击发送短信验证码按钮
	 * @param view
	 */
	public void doOnclikStep2PhoneSendSmsVerifyCode(View view){
		doSendSmsVerifyCode();
	}
	/**
	 * step2  开机向导模式，发送短信验证码按钮
	 * @param view
	 */
	public void doOnclikSendSmsVerifyCode(View view){
		doSendSmsVerifyCode();
	}
	/**
	 * 手机注册第2步和开机向导单独短信验证码发送短信 调用发送短信
	 */
	void doSendSmsVerifyCode(){
		final String phoneNumber = editPhone.getText().toString().trim();
		Log.i(TAG, "doOnclikStep2PhoneSendSmsVerifyCode() mAccountStartMode:"+mAccountStartMode);
		if(ActivityUtils.isMobileNO(editPhone.getText().toString())){
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					HashMap<String,String> tableAccountInfo =new HashMap<String, String>();
					tableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, phoneNumber);//KEY_SERVER_REQUEST_SMS_MSG_TYPE
					tableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);
					tableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
					if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
						textSendSmsCodeForBootWizard.setClickable(false);
					}else{
						textSendSmsCodeForStep2Phone.setClickable(false);//防止多次点击发送验证码
					}
					if(AccountUtils.sendSmsVerifyCodeFromServer(RegisterActivity.this,tableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								timeSmsCodeForPhoneRegister.start();
							}
						});

					}else{
						Log.i(TAG, "doOnclikStep2PhoneSendSmsVerifyCode() send sms fail ,reset clickable");
						if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
							textSendSmsCodeForBootWizard.setClickable(true);
						}else{
							textSendSmsCodeForStep2Phone.setClickable(true); //防止多次点击发送验证码
						}
					}
				}
			});
		}
	}
    View.OnClickListener doOnlcikStep2PhoneCompleteOnClickListener =  new View.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG,"doOnlcikStep2PhoneCompleteOnClickListener.onClick()");
            doOnlcikStep2PhoneCompleteButton(arg0);
        }
    };
	/**
	 * 手机注册第2步 点击完成按钮
	 * @param view
	 */
	public void doOnlcikStep2PhoneCompleteButton(View view){
		
		if(checkStep2PhoneInput()){
			doStep3Register();
		}
	}
	/**
	 * 邮箱注册第2步 点击返回键
	 * @param view
	 */
	public void doOnclickStep3PublicBackButton(View view){
        Log.i(TAG,"doOnclickStep3PublicBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	/**
	 * 手机/邮箱注册第3步 点击下一步 色设置密码,****若是邮箱注册 原来的的注册完成调整到 激活邮箱界面
	 * @param view
	 */
	public void doOnlcikStep3PublicNextButton(View view){
		//doStep3Register();
		if(!chekcStep3PublicInput()){
			return;
		}
		String strInput = editEmail.getText().toString().trim();
		if(ActivityUtils.isEmail(strInput)){ //输入邮箱
			Log.i(TAG, "doOnlcikStep3PublicNextButton() ");
			doStep3Register();
		}
	}
	//邮箱注册，，点击删除输入密码 按钮
	public void doOnclickDeleteInputPwdButton(View view){
		editInputPwd.setText("");
	}
	//邮箱注册，点击删除再次输入密码 按钮
	public void doOnclickDeleteInputPwdAgainButton(View view){
		editInputPwdAgain.setText("");
	}

    View.OnClickListener doOnclickBootWizardVerifySmsCodeNextOnClickListener =  new View.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG,"doOnclickBootWizardVerifySmsCodeNextOnClickListener.onClick()");
            doOnclickBootWizardVerifySmsCodeNextButton(arg0);
        }
    };
	//开机向导验证短信验证码点击下一步
	public void doOnclickBootWizardVerifySmsCodeNextButton(View view){
		final String phoneNumber = editPhone.getText().toString().trim();
		String smsCode = editSmsCodeForBootWizard.getText().toString().trim();
		Log.i(TAG, "doOnclickBootWizardVerifySmsCodeNextButton() phoneNumber:"+phoneNumber+" smsCode:"+smsCode);
		if(editSmsCodeForBootWizard.getText().toString().isEmpty()){
			Log.i(TAG, "doOnclickBootWizardVerifySmsCodeNextButton() error smsCode not input");
			return;
		}
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, phoneNumber);
		mTableAccountInfo.put(Constants.KEY_SMS_CODE, smsCode);
		mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_REGISTER);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(AccountUtils.verifySmsCodeValidFromServer(RegisterActivity.this,mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							textPhoneNumber.setText(ActivityUtils.convertPhoneNumShow(phoneNumber));
							showStep(KEY_LAYOUT_STEP2_PHONE,false);
						}
					});
				}
				
			}
		});
	}
	//////////////////////////点击事件处理end
	//////////////////////////检查输入信息是否合法begin
	/*
	 * 检查第1步输入的手机号或邮箱是否合法，需要在线程里面调用
	 */
	private boolean chekcStep1PublicInput(String str){
		Log.i(TAG, "chekcStep1PublicInput()");
		//String str = editPhoneOrMail.getText().toString().trim();
        if(!ActivityUtils.isEmail(str) && !ActivityUtils.isMobileNO(str)){
        	ActivityUtils.alert(RegisterActivity.this, getResources().getString(R.string.alert_input_valid_phone_or_email));
        	return false;
        }
        if(ActivityUtils.isMobileNO(str)){   //输入手机号在服务器是否可用
        	mTableAccountInfo.clear();
        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, str);
        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);//注册验证手机号
        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(RegisterActivity.this, mTableAccountInfo)){
        		Log.e(TAG, "chekcStep1PublicInput() error str:"+str+" not valid from server!");
        		return false;
        	}
        }else if(ActivityUtils.isEmail(str)){  //输入邮箱在服务器是否可用
        	mTableAccountInfo.clear();
        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, str);
        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);//注册验证手机号
        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(RegisterActivity.this, mTableAccountInfo)){
        		Log.e(TAG, "chekcStep1PublicInput() error str:"+str+" not valid from server!");
        		return false;
        	}
        }
		return true;
	}
	/**
	 * 检查手机注册第一步输入的手机号是否合法
	 * @param str
	 * @return
	 */
	private boolean chekcStep1PhoneInput(String str){
		Log.i(TAG, "chekcStep1PhoneInput()");
		//String str = editPhoneOrMail.getText().toString().trim();
        if(!ActivityUtils.isMobileNO(str)){
        	ActivityUtils.alert(RegisterActivity.this, getResources().getString(R.string.alert_input_valid_phone));
        	return false;
        }
        if(ActivityUtils.isMobileNO(str)){   //输入手机号在服务器是否可用
        	mTableAccountInfo.clear();
        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, str);
        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);//注册验证手机号
        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(RegisterActivity.this, mTableAccountInfo)){
        		Log.e(TAG, "chekcStep1PhoneInput() error str:"+str+" not valid from server!");
        		return false;
        	}
        }
		return true;
	}
	/**
	 * 检查邮箱注册第一步输入的邮箱是否合法
	 * @param str
	 * @return
	 */
	private boolean chekcStep1EmailInput(String str){
		Log.i(TAG, "chekcStep1EmailInput()");
        if(!ActivityUtils.isEmail(str)){
        	ActivityUtils.alert(RegisterActivity.this, getResources().getString(R.string.alert_input_valid_email));
        	return false;
        }
        if(ActivityUtils.isEmail(str)){  //输入邮箱在服务器是否可用
        	mTableAccountInfo.clear();
        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, str);
        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);//注册验证邮箱
        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(RegisterActivity.this, mTableAccountInfo)){
        		Log.e(TAG, "chekcStep1EmailInput() error str:"+str+" not valid from server!");
        		return false;
        	}
        }
		return true;
	}
	/**
	 * 从服务器判断手机注册第2步输入的短信验证码是否正确
	 * @return
	 */
	private boolean checkStep2PhoneInput(){
		boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
		Log.i(TAG, "checkStep2PhoneInput() notShowSmsCodeLayout:"+notShowSmsCodeLayout);
		if(!notShowSmsCodeLayout && editSmsCodeForStep2Phone.getText().toString().isEmpty()){
			ActivityUtils.alert(RegisterActivity.this, getResources().getString(R.string.alert_text_sms_verify_code_null));
		}else if(editInputPwdPhone.getText().toString().isEmpty()){
			ActivityUtils.alert(RegisterActivity.this, getResources().getString(R.string.alert_new_pwd_null));
		}else if(editInputPwdAgainPhone.getText().toString().isEmpty()){
			ActivityUtils.alert(RegisterActivity.this, getResources().getString(R.string.alert_confirm_new_pwd_null));
		}else if(!editInputPwdPhone.getText().toString().equals(editInputPwdAgainPhone.getText().toString())){
			ActivityUtils.alert(RegisterActivity.this, getResources().getString(R.string.alert_two_new_pwd_not_equal));
		}else{
			return true;
		}
		return false;
	}
	/**
	 * 是否不显示 短信验证码布局,即开机向导模式,只有开机向导模式才不显示短信验证码布局
	 * @return
	 */
	boolean isNotShowSmsCodeLayout(){
		//String curPhone = ActivityUtils.getCurrentPhoneNumber(RegisterActivity.this);
		Log.i(TAG, "isNotShowSmsCodeLayout() mAccountStartMode:"+mAccountStartMode);
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
			return true;
		}
		return false;
	}
	/**
	 * 检查第3步 输入密码是否合法
	 * @return
	 */
	private boolean chekcStep3PublicInput(){
		String pwd = editInputPwd.getText().toString().trim();
		String confirmPwd = editInputPwdAgain.getText().toString().trim();
		if(null == pwd || pwd.isEmpty()){
			ActivityUtils.alert(this, getResources().getString(R.string.alert_new_pwd_null));
			return false;
		}else if(null == confirmPwd || confirmPwd.isEmpty()){
			ActivityUtils.alert(this, getResources().getString(R.string.alert_confirm_new_pwd_null));
			return false;
		}else if(!confirmPwd.equals(pwd)){
			ActivityUtils.alert(this, getResources().getString(R.string.alert_two_new_pwd_not_equal));
			return false;
		}
		
		return true;
	}
	//////////////////////////检查输入信息是否合法end
	void doStep3Register(){
		final String smsCode = editSmsCodeForStep2Phone.getText().toString().trim();
		final boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
		Log.i(TAG, "doStep3Register() 111 mRegisterType:"+mRegisterType+" mAccountStartMode:"+mAccountStartMode+" notShowSmsCodeLayout:"+notShowSmsCodeLayout);
		if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){ //点击按钮后设置不可点
			if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
				textStep2PhoneBootWizardNextButton.setEnabled(false);
				imageStep2PhoneBootWizardRightArrow.setEnabled(false);
                textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
                textStep2PhoneBootWizardNextButton.setOnClickListener(null);
                imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
                imageStep2PhoneBootWizardRightArrow.setOnClickListener(null);
            }else{
				btnStep2PhoneComplete.setEnabled(false);//
			}
		}else if(Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)){
			btnStep3PublicNext.setEnabled(false);
		}
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				String userName = null;
				mTableAccountInfo.clear();
				if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){   //输入手机号
					userName = editPhone.getText().toString().trim();
					
					mLoginPwd = editInputPwdPhone.getText().toString().trim();
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_REGISTER_TYPE, mRegisterType); //本地使用
					
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, userName);
					mTableAccountInfo.put(Constants.KEY_SMS_CODE, smsCode);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);//验证码需要操作类型

					Log.i(TAG, "doStep3Register() 222 mAccountStartMode:"+mAccountStartMode+" notShowSmsCodeLayout:"+notShowSmsCodeLayout
						+"mRegisterType:"+mRegisterType+" userName:"+userName+" mAccountStartMode:"+mAccountStartMode);
					if(!notShowSmsCodeLayout && !AccountUtils.verifySmsCodeValidFromServer(RegisterActivity.this, mTableAccountInfo)){
						//需先验证码通过才能执行，下一步，不正确返回
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								btnStep2PhoneComplete.setEnabled(true);
							}
						});
						Log.e(TAG, "doStep3Register() 221 error smsCode from server");
						return;
					}
					
				}else if(Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)){ //输入邮箱
					userName = editEmail.getText().toString().trim();
					Log.i(TAG, "doStep3Register() 333 mRegisterType:"+mRegisterType+" userName:"+userName);
					mLoginPwd = editInputPwd.getText().toString().trim();
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_REGISTER_TYPE, mRegisterType); //本地使用
					
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, userName);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);
				}
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_USER_NAME, userName);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, mLoginPwd);
				if(!notShowSmsCodeLayout){  //普通模式,开机向导模式显示短信验证码
					if(AccountUtils.register(RegisterActivity.this,mTableAccountInfo)){ 
						if(ActivityUtils.isEmail(userName)){  //邮箱注册不立即登录，显示邮箱激活界面
							Log.i(TAG, "doStep3Register() for mail step1");
							if(AccountUtils.sendEmailFromServer(RegisterActivity.this, mTableAccountInfo)){
								
							}
							Log.i(TAG, "doStep3Register() for mail step2");
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									showEmailVerify();
								}
							});
	
	
						}else if(ActivityUtils.isMobileNO(userName)){   //手机号注册完立即登录
							if(mTableAccountInfo.containsKey(Constants.KEY_SMS_CODE)){
								Log.i(TAG, "doStep3Register() before login() remove KEY_SMS_CODE");
								mTableAccountInfo.remove(Constants.KEY_SMS_CODE);
							}
							if(AccountUtils.login(RegisterActivity.this,mTableAccountInfo)){ //注册完成立即登陆
								mHandler.post(new Runnable() { //ui 操作放在主线程里面
									@Override
									public void run() {
										//showStep(KEY_LAYOUT_STEP4_PUBLIC);//进入注册完成提示界面
										Log.i(TAG, "doStep3Register() logined mAccountStartMode:"+mAccountStartMode);
										doRegisterLogined();
									}
								});
								
							}
						}
					}
				}else{ //开机向导模式不显示短信验证码
					if(AccountUtils.registerTrust(RegisterActivity.this,mTableAccountInfo)){  //无短信验证码模式注册
                        if(mTableAccountInfo.containsKey(Constants.KEY_SMS_CODE)){
                            Log.i(TAG, "doStep3Register() bootWizardMode before login() remove KEY_SMS_CODE");
                            mTableAccountInfo.remove(Constants.KEY_SMS_CODE);
                        }
						if(AccountUtils.login(RegisterActivity.this,mTableAccountInfo)){ //注册完成立即登陆
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									Log.i(TAG, "doStep3Register() logined mAccountStartMode:"+mAccountStartMode);
									doRegisterLogined();
									finish();
								}
							});
						}
					}
				}
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){ //执行完之后设置按钮可点
							if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
								textStep2PhoneBootWizardNextButton.setEnabled(true);
								imageStep2PhoneBootWizardRightArrow.setEnabled(true);
                                textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                                textStep2PhoneBootWizardNextButton.setOnClickListener(doOnlcikStep2PhoneCompleteOnClickListener);
                                imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                                imageStep2PhoneBootWizardRightArrow.setOnClickListener(doOnlcikStep2PhoneCompleteOnClickListener);
							}else{
								btnStep2PhoneComplete.setEnabled(true);//
							}
						}else if(Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)){
							btnStep3PublicNext.setEnabled(true);
						}
					}
				});

			}
		});

	}
	/**
	 * 处理注册并登录后 相关界面显示
	 */
	void doRegisterLogined(){
    	mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(RegisterActivity.this);
    	Log.i(TAG,"doRegisterLogined() mAccountStartMode:"+mAccountStartMode+" mLoginTaskId:"+mLoginTaskId);
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){ //开机向导模式登陆后，跳转开机向导界面
			ActivityUtils.startActivityByName(RegisterActivity.this, ActivityUtils.BOOT_WIZARD_PACAAGE_NAME, ActivityUtils.BOOT_WIZARD_LOGIN_SUCCESS_ACTIVITY);
		}else{
			String userName = null;
			ActivityUtils.closeKeyboard(RegisterActivity.this);
			Log.i(TAG, "doRegisterLogined() mRegisterType:"+mRegisterType);
			Intent i = new Intent(RegisterActivity.this,AccountInfoActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setAction(AccountInfoActivity.ACTION_REGISTER_SUCCESS);
			if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){   //输入手机号
				userName = editPhone.getText().toString().trim();
				i.putExtra(Constants.KEY_ACCOUNT_PHONE_NUMBER, userName);
			}else if(Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)){ //输入邮箱
				userName = editEmail.getText().toString().trim();
				i.putExtra(Constants.KEY_ACCOUNT_EMAIL, userName);
			}else{
				ActivityUtils.alert(RegisterActivity.this, " doRegisterLogined() error!! userName:"+userName);
				return;
			}
			i.putExtra(Constants.KEY_ACCOUNT_REGISTER_TYPE, mRegisterType);
			Log.i(TAG, "doRegisterLogined() before start AccountInfoActivity userName:"+userName+" mRegisterType:"+mRegisterType);
			RegisterActivity.this.startActivity(i);
		}
		if(mLoginTaskId > 0) {
            AppManager.getAppManager().finishAllActivityByTaskId(mLoginTaskId);//登录完成需要关闭登录界面，规避从登陆后的界面点击返回，会回到登录界面的现象
        }
		finish();
	}
	/**
	 * 将bitmap转换成base64字符串
	 * @param bit
	 * @return
	 */
	public String Bitmap2StrByBase64(Bitmap bit) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bit.compress(CompressFormat.PNG, 10, bos);// 参数100表示不压缩
		byte[] bytes = bos.toByteArray();
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			Log.e(TAG, "onActivityResult uri:" + uri.toString());
			ContentResolver cr = this.getContentResolver();
			try {
				bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
				 bitmap = BitmapUtils.compressBitmap(bitmap);
				// saveBitmap(bitmap);
				bitmap = BitmapUtils.imageCropSquare(bitmap, true);
				imageSetAvatar.setImageBitmap(bitmap);  //设置返回的头像信息
			} catch (FileNotFoundException e) {
				Log.e("Exception", e.getMessage(), e);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	class TimeCountForPhoneRegisterSmsCode extends CountDownTimer {

		public TimeCountForPhoneRegisterSmsCode(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			String strFormat = getResources().getString(R.string.register_code_send_again);
			String str = String.format(strFormat, millisUntilFinished/1000);

			if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
				textSendSmsCodeForBootWizard.setText(str);
				textSendSmsCodeForBootWizard.setEnabled(false);
				textSendSmsCodeForBootWizard.setClickable(false);
			}else{
				textSendSmsCodeForStep2Phone.setText(str);
				textSendSmsCodeForStep2Phone.setEnabled(false);
				textSendSmsCodeForStep2Phone.setClickable(false);
			}
		}
		@Override
		public void onFinish() {
			if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
				textSendSmsCodeForBootWizard.setText(getResources().getString(R.string.register_send_again));
				textSendSmsCodeForBootWizard.setEnabled(true);
				textSendSmsCodeForBootWizard.setClickable(true);
			}else{
				textSendSmsCodeForStep2Phone.setEnabled(true);
				textSendSmsCodeForStep2Phone.setClickable(true);
				textSendSmsCodeForStep2Phone.setText(getResources().getString(R.string.register_send_again));
			}
		}
	}
	class TimeCountForMailRegisterSendEmail extends CountDownTimer {

		public TimeCountForMailRegisterSendEmail(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {

			textSendEmailForStep2Mail.setEnabled(false);
			textSendEmailForStep2Mail.setClickable(false);
			String strFormat = getResources().getString(R.string.register_code_send_again);
			String str = String.format(strFormat, millisUntilFinished/1000);
			textSendEmailForStep2Mail.setText(str);
		}
		@Override
		public void onFinish() {
			textSendEmailForStep2Mail.setEnabled(true);
			textSendEmailForStep2Mail.setClickable(true);
			textSendEmailForStep2Mail.setText(getResources().getString(R.string.register_send_again));
		}
	}
	/**
	 * 初始化发送验证码
	 */
	void initTextSendSmsCodeForStep2Phone(){
		Log.i(TAG, "initTextSendSmsCodeForStep2Phone()");
		timeSmsCodeForPhoneRegister.cancel();
		textSendSmsCodeForStep2Phone.setEnabled(true);
		textSendSmsCodeForStep2Phone.setClickable(true);
		textSendSmsCodeForStep2Phone.setText(getResources().getString(R.string.btn_text_send_sms_verify_code));
        editSmsCodeForStep2Phone.setText("");
        editInputPwdPhone.setText("");
        editInputPwdAgainPhone.setText("");
	}
    /**
     * 开机向导初始化发送验证码
     */
    void initTextSendSmsCodeForBootWizard(){
        Log.i(TAG, "initTextSendSmsCodeForBootWizard()");
        timeSmsCodeForPhoneRegister.cancel();
        textSendSmsCodeForBootWizard.setEnabled(true);
        textSendSmsCodeForBootWizard.setClickable(true);
        textSendSmsCodeForBootWizard.setText(getResources().getString(R.string.btn_text_send_sms_verify_code));
        editSmsCodeForBootWizard.setText("");
        checkAutoVerifySmsCode.setChecked(true);
    }
	//抽出到公共类    设置输入框输入监听
//	class EditChangedListener implements TextWatcher{
//		private int mEditType = -1;
//		public EditChangedListener(int editType){
//			mEditType = editType;
//		}
//		@Override
//		public void afterTextChanged(Editable arg0) {
//			Log.e(TAG, "afterTextChanged() 00 mEditType:"+mEditType+" arg0.toString():"+arg0.toString());
//			if(arg0.toString().length()>0){
//				showAfterTextNotEmpty(mEditType);
//			}else{
//				showAfterTextEmpty(mEditType);
//			}
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after) {
//			Log.i(TAG, "beforeTextChanged() mEditType:"+mEditType+" s:"+s);
//		}
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count) {
//			Log.i(TAG, "onTextChanged() mEditType:"+mEditType+" s:"+s);
//		}
//		
//	}
	//输入框内容非空
	public void showAfterTextNotEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_PHONE:
				btnStep1PhoneNext.setEnabled(true);
				imageDeleteAccountPhone.setVisibility(View.VISIBLE);
                textStep1PublicBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                textStep1PublicBootWizardNextButton.setOnClickListener(doOnclickStep1PublicNextOnClickListener);
                imageStep1PublicBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                imageStep1PublicBootWizardRightArrow.setOnClickListener(doOnclickStep1PublicNextOnClickListener);
				break;
			case TYPE_EDIT_MAIL:
				btnStep1EmailNext.setEnabled(true);
				imageDeleteAccountMail.setVisibility(View.VISIBLE);
				break;
			case TYPE_EDIT_INPUT_PWD:
				imageDeleteInputPwd.setVisibility(View.VISIBLE);
				if(checkStep3PublicInputFull()){
					btnStep3PublicNext.setEnabled(true);
				}
				break;
			case TYPE_EDIT_INPUT_PWD_AGAIN:
				imageDeleteInputPwdAgain.setVisibility(View.VISIBLE);
				if(checkStep3PublicInputFull()){
					btnStep3PublicNext.setEnabled(true);
				}
				break;
			case TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE: //找回密码第二步，监听输入验证码，非开机向导模式才显示
				imageDeleteSmsVerifyCode.setVisibility(View.VISIBLE);
				if(checkStep2PhoneInputFull()){
					btnStep2PhoneComplete.setEnabled(true);
				}
				break;
			case TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD:
				imageDeleteSmsVerifyCodeForBootWizard.setVisibility(View.VISIBLE);
				if(checkBootWizardVerifySmsCodeInputFull()){
                    textBootWizardVeirfySmsCodeNextButton.setEnabled(true);
                    imageBootWizardVerifySmsCodeRightArrow.setEnabled(true);
                    textBootWizardVeirfySmsCodeNextButton.setTextColor(getResources().getColor((R.color.color_button_link)));
                    textBootWizardVeirfySmsCodeNextButton.setOnClickListener(doOnclickBootWizardVerifySmsCodeNextOnClickListener);
                    imageBootWizardVerifySmsCodeRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                    imageBootWizardVerifySmsCodeRightArrow.setOnClickListener(doOnclickBootWizardVerifySmsCodeNextOnClickListener);
				}
				break;
			case TYPE_EDIT_INPUT_PWD_PHONE:   	 //找回密码第二步，监听输入密码，
				imageDeleteInputPwdPhone.setVisibility(View.VISIBLE);
				if(checkStep2PhoneInputFull()){
					if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
						textStep2PhoneBootWizardNextButton.setEnabled(true);
						imageStep2PhoneBootWizardRightArrow.setEnabled(true);
                        textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                        textStep2PhoneBootWizardNextButton.setOnClickListener(doOnlcikStep2PhoneCompleteOnClickListener);
                        imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                        imageStep2PhoneBootWizardRightArrow.setOnClickListener(doOnlcikStep2PhoneCompleteOnClickListener);
					}else{
						btnStep2PhoneComplete.setEnabled(true);
					}
				}
				break;
			case TYPE_EDIT_INPUT_PWD_AGAIN_PHONE: //找回密码第二步，监听再次输入密码，
				imageDeleteInputPwdAgainPhone.setVisibility(View.VISIBLE);
				if(checkStep2PhoneInputFull()){
					if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
						textStep2PhoneBootWizardNextButton.setEnabled(true);
						imageStep2PhoneBootWizardRightArrow.setEnabled(true);
                        textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                        textStep2PhoneBootWizardNextButton.setOnClickListener(doOnlcikStep2PhoneCompleteOnClickListener);
                        imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                        imageStep2PhoneBootWizardRightArrow.setOnClickListener(doOnlcikStep2PhoneCompleteOnClickListener);
					}else{
						btnStep2PhoneComplete.setEnabled(true);
					}
				}
				break;
			default:
				break;
		}
	}
	//输入框内容为空
	public void showAfterTextEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_PHONE:
				btnStep1PhoneNext.setEnabled(false);
				imageDeleteAccountPhone.setVisibility(View.GONE);
                textStep1PublicBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
                textStep1PublicBootWizardNextButton.setOnClickListener(null);
                imageStep1PublicBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
                imageStep1PublicBootWizardRightArrow.setOnClickListener(null);
				break;
			case TYPE_EDIT_MAIL:
				btnStep1EmailNext.setEnabled(false);
				imageDeleteAccountMail.setVisibility(View.GONE);
				break;
			case TYPE_EDIT_INPUT_PWD:
				btnStep3PublicNext.setEnabled(false);
				imageDeleteInputPwd.setVisibility(View.GONE);
				break;
			case TYPE_EDIT_INPUT_PWD_AGAIN:
				btnStep3PublicNext.setEnabled(false);
				imageDeleteInputPwdAgain.setVisibility(View.GONE);
				break;
			case TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE:  //找回密码第二步，监听输入验证码，非开机向导模式才显示
				btnStep2PhoneComplete.setEnabled(false);
				imageDeleteSmsVerifyCode.setVisibility(View.GONE);
				break;
			case TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD:
				imageDeleteSmsVerifyCodeForBootWizard.setVisibility(View.GONE);
                textBootWizardVeirfySmsCodeNextButton.setEnabled(false);
                imageBootWizardVerifySmsCodeRightArrow.setEnabled(false);
                textBootWizardVeirfySmsCodeNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
                textBootWizardVeirfySmsCodeNextButton.setOnClickListener(null);
                imageBootWizardVerifySmsCodeRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
                imageBootWizardVerifySmsCodeRightArrow.setOnClickListener(null);
				break;
			case TYPE_EDIT_INPUT_PWD_PHONE:           //找回密码第二步，监听输入密码，
				if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
					textStep2PhoneBootWizardNextButton.setEnabled(false);
					imageStep2PhoneBootWizardRightArrow.setEnabled(false);
                    textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
                    textStep2PhoneBootWizardNextButton.setOnClickListener(null);
                    imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
                    imageStep2PhoneBootWizardRightArrow.setOnClickListener(null);
				}else{
					btnStep2PhoneComplete.setEnabled(false);
				}
				imageDeleteInputPwdPhone.setVisibility(View.GONE);
				break;
			case TYPE_EDIT_INPUT_PWD_AGAIN_PHONE:     //找回密码第二步，监听再次输入密码，
				if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
					textStep2PhoneBootWizardNextButton.setEnabled(false);
					imageStep2PhoneBootWizardRightArrow.setEnabled(false);
                    textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
                    textStep2PhoneBootWizardNextButton.setOnClickListener(null);
                    imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
                    imageStep2PhoneBootWizardRightArrow.setOnClickListener(null);
				}else{
					btnStep2PhoneComplete.setEnabled(false);
				}
				imageDeleteInputPwdAgainPhone.setVisibility(View.GONE);
				break;
			default:
				break;
		}
	}
	/**
	 * 判断开机向导验证短信验证码输入是否都填写了
	 * @return
	 */
	boolean checkBootWizardVerifySmsCodeInputFull(){
		if(editSmsCodeForBootWizard.getText().toString().trim().isEmpty()){
			return false;
		}
		return true;
	}
	/**
	 * 判断手机注册步骤2是否都填写了
	 * @return
	 */
    boolean checkStep2PhoneInputFull(){
		//boolean isNormalMode = !(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode));
    	boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
		Log.i(TAG, "checkStep2PhoneInputFull() mAccountStartMode:"+mAccountStartMode+" notShowSmsCodeLayout:"+notShowSmsCodeLayout);
    	if(!notShowSmsCodeLayout && editSmsCodeForStep2Phone.getText().toString().isEmpty()){//正常模式才需要验证短信验证码
			return false;
		}else if(editInputPwdPhone.getText().toString().isEmpty()){
			return false;
		}else if(editInputPwdAgainPhone.getText().toString().isEmpty()){
			return false;
		}
    	return true;
    }
    /***
     * 判断邮箱步骤3是否都填写了
     * @return
     */
    boolean checkStep3PublicInputFull(){
		
    	if(editInputPwd.getText().toString().isEmpty()){
			return false;
		}else if(editInputPwdAgain.getText().toString().isEmpty()){
			return false;
		}
    	return true;
    }

}
