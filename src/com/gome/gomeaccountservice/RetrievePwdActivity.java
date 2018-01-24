package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.gome.gomeaccountservice.utils.EditChangedListener;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;
import com.gome.gomeaccountservice.utils.ServerModel;

import java.util.HashMap;
import java.util.Map;

public class RetrievePwdActivity extends Activity implements TextChangeCallback,SmsCallback{

	static final String TAG = Constants.TAG_PRE+"RetrievePwdActivity";
    Animation mAnimationOpenEnter;
    Animation mAnimationOpenExit;
    Animation mAnimationCloseEnter;
    Animation mAnimationCloseExit;

	EditText editPhone; //手机号找回密码第一步编辑框
	ImageView imageDeletePhone;//
	EditText editPhoneOrMail;
	EditText editSmsVerifyCode;
	TextView textSendSmsVerifyCode;
	TextView textSendSmsCodeForBootWizard;//text_send_sms_verify_code_for_boot_wizard
	TextView textResendEmail;
	TextView textSendEmailReminder;
	TextView textPhone;
	
	EditText editNewPwd;
	EditText editConfirmNewPwd;
	EditText editNewPwdPhone;
	EditText editConfirmNewPwdPhone;
	
	Button btnRetrievePwd;  //step4 重置密码按钮
	Button btnStep1Next;
	Button btnStep2PhoneComplete;
	Button btnStep3Complete; //主要是邮件找回密码
	boolean isEmailWhithSecurityPhone = false; //邮箱有安全码找回密码
    String mAccountStartMode = null;  //账号启动模式
	//开向导相关布局
	View layoutStep2PhoneBootWizardBottom;//手机找回密码第二步，开机向导底部布局
	LinearLayout layoutSetp2PhonePartSms; //手机号找回密码界面的短信验证码布局
	FrameLayout laoutStep2PhoneCompleteButton;//手机找回密码完成按钮布局
	
	TextView textRetrievePwdStep1Message;//text_retrieve_pwd_step1_message
	TextView textStep1PhoneBootWizardNextButton;//text_step1_phone_boot_wizard_next_button
	ImageView imageStep1PhoneBootWizardRightArrow;//image_step1_phone_boot_wizard_right_arrow
	
	TextView textBootWizardVerifySmsCodeNextButton;//text_boot_wizard_verify_sms_code_next_button
	ImageView imageBootWizardVerifySmsCodeRightArrow;//image_boot_wizard_verify_sms_code_right_arrow
	
	TextView textPhoneVeirfySmsCode;//text_phone_number_for_verify_sms_code
	EditText editSmsCodeForBootWizard;//edit_sms_verify_code_for_boot_wizard
	ImageView imageDeleteSmsVerifyCodeForBootWizard;//image_delete_sms_verify_code_for_boot_wizard
	CheckBox checkAutoVerifySmsCode;
	TextView textStep2PhoneBootWizardNextButton; //text_step2_phone_boot_wizard_next_button
	ImageView imageStep2PhoneBootWizardRightArrow;//image_step2_phone_boot_wizard_right_arrow
	
	HashMap<String, LinearLayout> mLayouts;
	LinearLayout step1Layout; //stpe1输入手机号或邮件布局
	LinearLayout step1PhoneLayout; //stpe1输入手机号布局
	LinearLayout step2Layout; //step2输入手机验证码布局，包括邮箱找回包含安全码的情况
	LinearLayout step3Layout; //step3重置密码布局
	LinearLayout step4Layout; //step4发送邮箱验证码布局
	LinearLayout layoutBootWizardVerifySmsCode;//layout_boot_wizard_verify_sms_code
	
	final String KEY_LAYOUT_STEP1 = "step1";
	final String KEY_LAYOUT_STEP1_PHONE = "step1Phone";
	final String KEY_LAYOUT_STEP2 = "step2";  
	final String KEY_LAYOUT_STEP3 = "step3";
	final String KEY_LAYOUT_STEP4 = "step4";  
	final String KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE = "bootWizardVerifySmsCode";  
	
	SmsReceiver mSmsReceiver = null;
	//int mCurrnetStep = 0;//当前步骤
	String mCurrnetStep = KEY_LAYOUT_STEP1;//当前步骤
	
	final int TYPE_EDIT_PHONE = 1;
	final int TYPE_EDIT_PHONE_OR_MAIL = 2;
	final int TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE = 3;
	final int TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD = 4;
	final int TYPE_EDIT_INPUT_PWD = 5;
	final int TYPE_EDIT_INPUT_PWD_AGAIN = 6;
	final int TYPE_EDIT_INPUT_PWD_PHONE = 7;
	final int TYPE_EDIT_INPUT_PWD_AGAIN_PHONE = 8;
	
	ImageView imageDeleteAccountName;//
	ImageView imageDeleteSmsVerifyCode;
	ImageView imageDeleteInputPwdPhone;//image_delete_input_password_for_phone
	ImageView imageDeleteInputPwdAgainPhone;
	ImageView imageDeleteInputPwd;
	ImageView imageDeleteInputPwdAgain;
	ImageView imageHidePasswordForPhone;//image_hide_password_for_password_phone
	ImageView imageShowPasswordForPhone;
	ImageView imageHidePasswordForAgaginPhone;
	ImageView imageShowPasswordForAgaginPhone;
	ImageView imageHidePasswordForPassword;//image_hide_password_for_password
	ImageView imageShowPasswordForPassword;
	ImageView imageHidePasswordForPasswordAgain;
	ImageView imageShowPasswordForPasswordAgain;
	
	HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	
	String mNickName = null;
	String mEmailAddress = null;
	String mPhoneNumber = null;
    String mSecurityPhoneNumber = null;
	String mGomeId = null;
	String mRegisterType = null;
	String mLoginPwd = null;
	String mSex = null;
	String mUserLevel = null;
	String mServerAvatarPath = null;
    private String mToVerifyEmailAddress = null; //需验证的邮箱地址
    String mToken = null;
    
    //server 返回状态变量
	String mServerResCode = null;
	String mServerResMsg = null;
	
	private SmsCodeTimeCount mSmsCodeTimeCount = null;
	private SendEmailTimeCount mSendEmailTimeCount = null;
	
    private static final HandlerThread sWorkerThread = new HandlerThread("RetrievePwdActivity-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    Handler mHandler =new Handler();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate()");
		// 注册接收短信广播receiver    
		mSmsReceiver = new SmsReceiver(this);    
        IntentFilter receiverFilter = new IntentFilter(ActivityUtils.ACTION_SMS_RECEIVER);    
	    registerReceiver(mSmsReceiver, receiverFilter);

		setContentView(R.layout.retrieve_password_activity);
		initView();
		getAccountInfo();
		verifyAccountStartMode();
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
	 * 检查当前的模式,开机向导，普通模式已登录，未登录
	 */
	void verifyAccountStartMode(){
		mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(RetrievePwdActivity.this);
		Log.i(TAG, "verifyAccountStartMode() 111 mAccountStartMode:"+mAccountStartMode);
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
			//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			//找回密码第二步
			layoutStep2PhoneBootWizardBottom.setVisibility(View.VISIBLE);
			layoutSetp2PhonePartSms.setVisibility(View.GONE);
			laoutStep2PhoneCompleteButton.setVisibility(View.GONE);
			showStep(KEY_LAYOUT_STEP1_PHONE);
            ActivityUtils.sendHideNavigationBarBrocast(RetrievePwdActivity.this);
            getWindow().getDecorView().showNavigationBarDivider(false);
		}else{
			layoutStep2PhoneBootWizardBottom.setVisibility(View.GONE);
			layoutSetp2PhonePartSms.setVisibility(View.VISIBLE);
			laoutStep2PhoneCompleteButton.setVisibility(View.VISIBLE);
			showStep(KEY_LAYOUT_STEP1);//初始化应用显示第一步界面
			Log.i(TAG, "verifyAccountStartMode() 222 mPhoneNumber:"+mPhoneNumber+" mEmailAddress:"+mEmailAddress);
			if(null != mPhoneNumber || null != mEmailAddress){
				Log.i(TAG, "verifyAccountStartMode() 333");
				if(null != mPhoneNumber){
					editPhoneOrMail.setText(mPhoneNumber);
				}else if(null != mEmailAddress){
					editPhoneOrMail.setText(mEmailAddress);
				}
				editPhoneOrMail.setEnabled(true);
				editPhoneOrMail.setFocusable(true);
				editPhoneOrMail.setFocusableInTouchMode(true);
			}else{
				Log.i(TAG, "verifyAccountStartMode() 444");
				editPhoneOrMail.setEnabled(true);
				editPhoneOrMail.setFocusable(true);
				editPhoneOrMail.setFocusableInTouchMode(true);
			}
		}
	}
	public void getAccountInfo() {
		Account gomeAccount = AccountUtils.getLatestGomeAccount(this); //获取本地账号
		if(null != gomeAccount){
			HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(this,gomeAccount.name); 
			mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
			mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
            mSecurityPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER);
			mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
			Log.i(TAG, "getAccountInfo()  mToken:"+mToken+" mEmailAddress:"+mEmailAddress+" mPhoneNumber:"+mPhoneNumber+" mSecurityPhoneNumber:"+mSecurityPhoneNumber);
		}
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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "onResume()");
        verifyEmailActived(false);
        Log.i(TAG, "onResume() 22 mAccountStartMode:"+mAccountStartMode);
        if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
            ActivityUtils.sendHideNavigationBarBrocast(RetrievePwdActivity.this);
            getWindow().getDecorView().showNavigationBarDivider(false);
        }
	}
    /**
     * 验证邮箱是否激活
     * @param isFromClick  是否手动点击按钮
     */
    void verifyEmailActived(final boolean isFromClick){
        Log.i(TAG, "verifyEmailActived() mToVerifyEmailAddress:"+mToVerifyEmailAddress+" isFromClick:"+isFromClick);
        if(null != mToVerifyEmailAddress){
            sWorker.post(new Runnable() {
                @Override
                public void run() {
                    mTableAccountInfo.clear();
                    //mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
                    mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_RETRIEVE_PWD);
                    mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mToVerifyEmailAddress);
                    if(AccountUtils.verifyEmailValidFlagFromServer(RetrievePwdActivity.this, mTableAccountInfo)){  //从服务器判断邮箱是否激活
                        mHandler.post(new Runnable() { //ui线程
                            @Override
                            public void run() {
                                showStep(KEY_LAYOUT_STEP3);
                            }
                        });
                    }else{
                        if(isFromClick){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ActivityUtils.alert(RetrievePwdActivity.this,getResources().getString(R.string.txt_gome_account_not_verified));
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
		Log.i(TAG, "onBackPressed() 111 mCurrnetStep:"+mCurrnetStep);
		if(!showPreStep(mCurrnetStep)){
            Log.i(TAG, "onBackPressed() 222");
            ActivityUtils.closeKeyboard(this); //切换界面关闭软键盘
			finish();
		}
	}
	void initView(){
        mAnimationOpenEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_enter_layout);
        mAnimationOpenExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_exit_layout);
        mAnimationCloseEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_enter_layout);
        mAnimationCloseExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_exit_layout);

		mSmsCodeTimeCount = new SmsCodeTimeCount(60000, 1000);
		mSendEmailTimeCount = new SendEmailTimeCount(60000, 1000);
		editPhone = (EditText)findViewById(R.id.edit_phone);
		editPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_PHONE,this));//TYPE_EDIT_PHONE
		
		editPhoneOrMail = (EditText)findViewById(R.id.edit_phone_or_mail);
		editPhoneOrMail.addTextChangedListener(new EditChangedListener(TYPE_EDIT_PHONE_OR_MAIL,this));

		editNewPwd = (EditText)findViewById(R.id.edit_new_password);
		editNewPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD, this));
        editNewPwd.setTypeface(Typeface.DEFAULT);
		editConfirmNewPwd = (EditText)findViewById(R.id.edit_confirm_new_password);
		editConfirmNewPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD_AGAIN, this));
        editConfirmNewPwd.setTypeface(Typeface.DEFAULT);
		
		editNewPwdPhone = (EditText)findViewById(R.id.edit_new_password_for_phone);
		editNewPwdPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD_PHONE,this));
        editNewPwdPhone.setTypeface(Typeface.DEFAULT);
		editConfirmNewPwdPhone = (EditText)findViewById(R.id.edit_confirm_new_password_for_phone);
		editConfirmNewPwdPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD_AGAIN_PHONE,this));
        editConfirmNewPwdPhone.setTypeface(Typeface.DEFAULT);
		editSmsVerifyCode = (EditText)findViewById(R.id.edit_sms_verify_code);
		editSmsVerifyCode.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE,this));
		
		textSendSmsVerifyCode = (TextView)findViewById(R.id.text_send_sms_verify_code);//点击一次后会变灰色一分钟
		textSendSmsCodeForBootWizard = (TextView)findViewById(R.id.text_send_sms_verify_code_for_boot_wizard); 
		textResendEmail = (TextView)findViewById(R.id.step4_resend_mail);//点击一次后会变灰色一分钟
		textSendEmailReminder = (TextView)findViewById(R.id.step4_text_send_mail_reminder);
		textPhone = (TextView)findViewById(R.id.text_phone_number);
		
		imageDeleteAccountName = (ImageView)findViewById(R.id.image_delete_account_name);
		imageDeleteSmsVerifyCode = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_step2);
		imageDeleteInputPwdPhone = (ImageView)findViewById(R.id.image_delete_input_password_for_phone);
		imageDeleteInputPwdAgainPhone = (ImageView)findViewById(R.id.image_delete_input_password_again_for_phone);
		imageDeleteInputPwd = (ImageView)findViewById(R.id.image_delete_input_password);
		imageDeleteInputPwdAgain = (ImageView)findViewById(R.id.image_delete_input_password_again);
		
		imageHidePasswordForPassword = (ImageView)findViewById(R.id.image_hide_password_for_password);
		imageShowPasswordForPassword = (ImageView)findViewById(R.id.image_show_password_for_password);
		imageHidePasswordForPasswordAgain = (ImageView)findViewById(R.id.image_hide_password_for_password_again);
		imageShowPasswordForPasswordAgain = (ImageView)findViewById(R.id.image_show_password_for_password_again);
		imageHidePasswordForPhone = (ImageView)findViewById(R.id.image_hide_password_for_password_phone);
		imageShowPasswordForPhone = (ImageView)findViewById(R.id.image_show_password_for_password_phone);
		imageHidePasswordForAgaginPhone = (ImageView)findViewById(R.id.image_hide_password_for_password_again_phone);
		imageShowPasswordForAgaginPhone = (ImageView)findViewById(R.id.image_show_password_for_password_again_phone);
		
		btnStep1Next = (Button)findViewById(R.id.btn_step1_next);
		btnStep2PhoneComplete = (Button)findViewById(R.id.btn_step2_phone_complete);
		btnStep3Complete = (Button)findViewById(R.id.btn_step3_complete);
		
		step1Layout = (LinearLayout)findViewById(R.id.retrieve_pwd_step1_layout);
		step1PhoneLayout = (LinearLayout)findViewById(R.id.retrieve_pwd_step1_phone_layout);
		step2Layout = (LinearLayout)findViewById(R.id.retrieve_pwd_step2_layout);
		step3Layout = (LinearLayout)findViewById(R.id.retrieve_pwd_step3_layout);
		step4Layout = (LinearLayout)findViewById(R.id.retrieve_pwd_step4_layout);
		layoutBootWizardVerifySmsCode = (LinearLayout)findViewById(R.id.layout_boot_wizard_verify_sms_code);
		mLayouts = new HashMap<String, LinearLayout>();
		mLayouts.put(KEY_LAYOUT_STEP1, step1Layout);
		mLayouts.put(KEY_LAYOUT_STEP1_PHONE, step1PhoneLayout);
		mLayouts.put(KEY_LAYOUT_STEP2, step2Layout);
		mLayouts.put(KEY_LAYOUT_STEP3, step3Layout);
		mLayouts.put(KEY_LAYOUT_STEP4, step4Layout);
		mLayouts.put(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE, layoutBootWizardVerifySmsCode);
		
		//开机向导相关
		layoutStep2PhoneBootWizardBottom = findViewById(R.id.layout_step2_phone_boot_wizard_boottom);
		layoutSetp2PhonePartSms = (LinearLayout)findViewById(R.id.layout_sms_verify_code);
		laoutStep2PhoneCompleteButton = (FrameLayout)findViewById(R.id.laout_retriece_pwd_step2_phone_complete_button);
		
		textStep1PhoneBootWizardNextButton = (TextView)findViewById(R.id.text_step1_phone_boot_wizard_next_button);
		imageStep1PhoneBootWizardRightArrow = (ImageView)findViewById(R.id.image_step1_phone_boot_wizard_right_arrow);
		
		imageDeletePhone = (ImageView)findViewById(R.id.image_delete_phone);
		textPhoneVeirfySmsCode = (TextView)findViewById(R.id.text_phone_number_for_verify_sms_code);
		editSmsCodeForBootWizard = (EditText)findViewById(R.id.edit_sms_verify_code_for_boot_wizard);//
		editSmsCodeForBootWizard.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD,this));
		imageDeleteSmsVerifyCodeForBootWizard = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_boot_wizard);
		
		textStep2PhoneBootWizardNextButton = (TextView)findViewById(R.id.text_step2_phone_boot_wizard_next_button);
		imageStep2PhoneBootWizardRightArrow = (ImageView)findViewById(R.id.image_step2_phone_boot_wizard_right_arrow);
		
		textBootWizardVerifySmsCodeNextButton = (TextView)findViewById(R.id.text_boot_wizard_verify_sms_code_next_button);
		imageBootWizardVerifySmsCodeRightArrow = (ImageView)findViewById(R.id.image_boot_wizard_verify_sms_code_right_arrow);
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
//////////////////////点击事件处理begin
    /**
     * 点击已验证按钮
     * @param view
     */
    public void doAlreadyVerified(View view){
        Log.i(TAG,"doAlreadyVerified() ");
        verifyEmailActived(true);
    }
	public void doOnclickHidePasswordForPhone(View view){
		editNewPwdPhone.setTransformationMethod(PasswordTransformationMethod.getInstance());
		imageHidePasswordForPhone.setVisibility(View.GONE);
		imageShowPasswordForPhone.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForPhone(View view){
		editNewPwdPhone.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		imageHidePasswordForPhone.setVisibility(View.VISIBLE);
		imageShowPasswordForPhone.setVisibility(View.GONE);
	}
	public void doOnclickHidePasswordForAgainPhone(View view){
		editConfirmNewPwdPhone.setTransformationMethod(PasswordTransformationMethod.getInstance());
		imageHidePasswordForAgaginPhone.setVisibility(View.GONE);
		imageShowPasswordForAgaginPhone.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForAgainPhone(View view){
		editConfirmNewPwdPhone.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		imageHidePasswordForAgaginPhone.setVisibility(View.VISIBLE);
		imageShowPasswordForAgaginPhone.setVisibility(View.GONE);
	}
	
	public void doOnclickHidePasswordForPassword(View view){ //普通密码一般是邮箱设置使用
		editNewPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editNewPwd.setSelection(editNewPwd.getText().length());
		imageHidePasswordForPassword.setVisibility(View.GONE);
		imageShowPasswordForPassword.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForPassword(View view){
		editNewPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editNewPwd.setSelection(editNewPwd.getText().length());
		imageHidePasswordForPassword.setVisibility(View.VISIBLE);
		imageShowPasswordForPassword.setVisibility(View.GONE);
	}
	public void doOnclickHidePasswordForPasswordAgain(View view){
		editConfirmNewPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editConfirmNewPwd.setSelection(editConfirmNewPwd.getText().length());
		imageHidePasswordForPasswordAgain.setVisibility(View.GONE);
		imageShowPasswordForPasswordAgain.setVisibility(View.VISIBLE);
	}
	public void doOnclickShowPasswordForPasswordAgain(View view){
		editConfirmNewPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        editConfirmNewPwd.setSelection(editConfirmNewPwd.getText().length());
		imageHidePasswordForPasswordAgain.setVisibility(View.VISIBLE);
		imageShowPasswordForPasswordAgain.setVisibility(View.GONE);
	}	
	/**
	 * step1 点击back按钮
	 * @param view
	 */
	public void doOnclickStep1BackButton(View view){
        ActivityUtils.closeKeyboard(this); //切换界面关闭软键盘
		finish();
	}
	//step1  点击删除输入框账号按钮
	public void doOnclickDeleteAccountNameButton(View view){
		editPhoneOrMail.setText("");
	}
	//step1 手机号找回密码，删除输入的手机号
	public void doOnclickDeleteAccountPhoneButton(View view){
		editPhone.setText("");
	}
	/**
	 * step1 输入手机号或者邮箱后，点击下一步
	 * @param view
	 */
	public void doOnlcikStep1NextButton(View view){
        Log.i(TAG, "doOnlcikStep1NextButton() 11");
		btnStep1Next.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(chekcStep1Input()){  //输入的手机号或者邮箱合法

					final String strInput = editPhoneOrMail.getText().toString().trim();
                    Log.i(TAG, "doOnlcikStep1NextButton() mPhoneNumber:"+mPhoneNumber+" mEmailAddress:"
                            +mEmailAddress+" mSecurityPhoneNumber:"+mSecurityPhoneNumber);
                    if(null != mPhoneNumber || null != mEmailAddress || null != mSecurityPhoneNumber){
                        if(!strInput.equals(mPhoneNumber) && !strInput.equals(mEmailAddress) && !strInput.equals(mSecurityPhoneNumber)){
                            ActivityUtils.alert(RetrievePwdActivity.this,getResources().getString(R.string.alert_retrieve_pwd_not_bind_phone_or_email));
                            return;
                        }
                    }
					if(ActivityUtils.isMobileNO(strInput)){   //输入手机号
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								textPhone.setText(ActivityUtils.convertPhoneNumShow(strInput));
								showStep(KEY_LAYOUT_STEP2);
							}
						});
					}else if(ActivityUtils.isEmail(strInput)){ //输入邮箱
						mToVerifyEmailAddress = strInput;
						doStep1NextSendEmail(); //发送邮件
					}
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						btnStep1Next.setEnabled(true);
					}
				});

			}
		});

	}
	/**
	 * 开机向导短信验证码界面点击返回
	 * @param view
	 */
	public void doOnclickBootWizardVerifySmsCodeBackButton(View view) {
        Log.i(TAG,"doOnclickBootWizardVerifySmsCodeBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
    View.OnClickListener doOnclickStep1PhoneNextOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG, "doOnclickStep1PhoneNextOnClickListener.onClick()");
            doOnlcikStep1PhoneNextButton(arg0);
        }
    };
	/**
	 * step1 输入手机号 点击下一步
	 * @param view
	 */
	public void doOnlcikStep1PhoneNextButton (View view){
		final String phoneNumber = editPhone.getText().toString().trim();
		Log.i(TAG, "doOnlcikStep1PhoneNextButton() phoneNumber:"+phoneNumber);
        if(null == phoneNumber || !ActivityUtils.isMobileNO(phoneNumber)){
        	Log.i(TAG, "doOnlcikStep1PhoneNextButton() please input valid phone ,return!!");
        	ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_input_valid_phone));
        	return;
        }
		imageStep1PhoneBootWizardRightArrow.setEnabled(false);
		textStep1PhoneBootWizardNextButton.setEnabled(false);
        textStep1PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
        textStep1PhoneBootWizardNextButton.setOnClickListener(null);
        imageStep1PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
        imageStep1PhoneBootWizardRightArrow.setOnClickListener(null);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(chekcStep1PhoneInput()){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							textPhoneVeirfySmsCode.setText(ActivityUtils.convertPhoneNumShow(phoneNumber));
							showStep(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE);
						}
					});
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						imageStep1PhoneBootWizardRightArrow.setEnabled(true);
						textStep1PhoneBootWizardNextButton.setEnabled(true);
                        textStep1PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                        textStep1PhoneBootWizardNextButton.setOnClickListener(doOnclickStep1PhoneNextOnClickListener);
                        imageStep1PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                        imageStep1PhoneBootWizardRightArrow.setOnClickListener(doOnclickStep1PhoneNextOnClickListener);
					}
				});
			}
		});
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
			Log.i(TAG, "doOnclickBootWizardVerifySmsCodeNextButton()  smsCode not input,return!!");
			return;
		}
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, phoneNumber);
		mTableAccountInfo.put(Constants.KEY_SMS_CODE, smsCode);
		mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_RETRIEVE_PWD);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(AccountUtils.verifySmsCodeValidFromServer(RetrievePwdActivity.this,mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
                            textPhone.setText(ActivityUtils.convertPhoneNumShow(phoneNumber));
							showStep(KEY_LAYOUT_STEP2);
						}
					});
				}
				
			}
		});
	}
	//该方法需在线程里面执行
	void doStep1NextSendEmail(){
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mToVerifyEmailAddress);
		mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);
        final ServerModel serverModel = new ServerModel();
		if(AccountUtils.sendEmailFromServer(RetrievePwdActivity.this, mTableAccountInfo,serverModel)){
			mHandler.post(new Runnable() {
				@Override
				public void run() {
                    String verifyType = serverModel.getVerifyType();
                    String accountName = editPhoneOrMail.getText().toString().trim();
                    Log.i(TAG,"doStep1NextSendEmail() verifyType:"+verifyType+" accountName:"+accountName);
                    if(Constants.KEY_VERIFY_TYPE_CODE.equals(verifyType)){ //
                        isEmailWhithSecurityPhone = true;
                        textPhone.setText(accountName);
                        showStep(KEY_LAYOUT_STEP2);
                    }else {
                        isEmailWhithSecurityPhone = false;
                        showStep(KEY_LAYOUT_STEP4);
                    }
				}
			});
		}
	}
	/**
	 * step2  点击back按钮
	 * @param view
	 */
	public void doOnclickStep2BackButton(View view){
        Log.i(TAG, "doOnclickStep2BackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//手机找回密码step2，开机向导校验验证码 ，点击删除验证码按钮
	public void doOnclickDeleteBootWizardSmsVerifyCodeButton(View view){
		Log.i(TAG, "doOnclickDeleteBootWizardSmsVerifyCodeButton() ");
		editSmsCodeForBootWizard.setText("");
	}
	//手机重置密码step2 ，点击删除输入的密码按钮
	public void doOnclickDeleteSmsVerifyCodeButton(View view){
		Log.i(TAG, "doOnclickDeleteSmsVerifyCodeButton() ");
		editSmsVerifyCode.setText("");
	}
	//手机重置密码step2 ，点击删除输入密码按钮
	public void doOnclickDeleteInputPwdForPhoneButton(View view){
		editNewPwdPhone.setText("");
	}
	//手机重置密码step2 ，点击删除再次输入的密码按钮
	public void doOnclickDeleteInputPwdAgainForPhoneButton(View view){
		editConfirmNewPwdPhone.setText("");
	}
	/**
	 * step2  点击发送短信验证码按钮,发送验证码界面有可能是邮箱过来，发送安全码中
	 * @param view
	 */
	public void doOnclikSendSmsVerifyCode(View view){
		final String userName;
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
			userName = editPhone.getText().toString().trim();
		}else{
			userName = editPhoneOrMail.getText().toString().trim();
		}
		Log.i(TAG, "doOnclikSendSmsVerifyCode() mAccountStartMode:"+mAccountStartMode+" userName:"+userName);
		if(ActivityUtils.isMobileNO(userName) || ActivityUtils.isEmail(userName)){  //用户名有可能是邮箱
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					HashMap<String,String> tableAccountInfo =new HashMap<String, String>();
                    if(ActivityUtils.isEmail(userName)){  //账号是邮箱
                        tableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, userName);
                    }else {
                        tableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, userName);//KEY_SERVER_REQUEST_SMS_MSG_TYPE
                    }
					tableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);
					tableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
					if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
						textSendSmsCodeForBootWizard.setClickable(false);//
					}else{
						textSendSmsVerifyCode.setClickable(false);//防止多次点击发送验证码
					}
					if(AccountUtils.sendSmsVerifyCodeFromServer(RetrievePwdActivity.this,tableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								mSmsCodeTimeCount.start();
							}
						});
					}else{ //发送短信失败，立即设置发送短信按钮可点
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								Log.i(TAG, "doOnclikSendSmsVerifyCode() send sms fail ,reset clickable");
								if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
									textSendSmsCodeForBootWizard.setClickable(true);//
								}else{
									textSendSmsVerifyCode.setClickable(true);//防止多次点击发送验证码
								}
							}
						});

					}
				}
			});
		}
	}
    View.OnClickListener doOnlcikStep2CompleteOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG, "doOnlcikStep2CompleteOnClickListener.onClick()");
            doOnlcikStep2CompleteButton(arg0);
        }
    };
	/**
	 * step2  点击完成按钮，检测短信验证码，设置新密码
	 * @param view
	 */
	public void doOnlcikStep2CompleteButton(View view){
		if(checkStep2Input()){
			doRetrievePwd();
		}

	}
	/**
	 * step3  点击back按钮
	 * @param view
	 */
	public void doOnclickStep3BackButton(View view){
        Log.i(TAG, "doOnclickStep3BackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//step3 点击删除 输入的密码按钮
	public void doOnclickDeleteInputPwdButton(View view){
		editNewPwd.setText("");
	}
	//step3 点击删除 再次输入的密码按钮
	public void doOnclickDeleteInputPwdAgainButton(View view){
		editConfirmNewPwd.setText("");
	}
	/**
	 * step3 点击完成，通过手机号号或邮箱重置密码
	 * @param view
	 */
	public void doOnlcikStep3CompleteButton(View view){
		Log.i(TAG, "doOnlcikStep3CompleteButton()");
		if(checkStep3Input()){ //实际上现在只有邮箱找回密码才调用step3
			doRetrievePwd();
		}
	}
	/**
	 * step4 点击back按钮
	 * @param view
	 */
    public void doOnclickStep4BackButton(View view){
        Log.i(TAG, "doOnclickStep4BackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
    }
    /**
     * step4 点击查收邮件按钮
     * @param view
     */
    public void doOnclickCheckEmail(View view){
    	//ActivityUtils.verifyMail(this);
        ActivityUtils.verifyMail(this,editPhoneOrMail.getText().toString());
    }
    public void doOnclickResendEmail(View view){
    	Log.i(TAG, "doOnclickResendEmail() mToVerifyEmailAddress:"+mToVerifyEmailAddress);
    	sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mToVerifyEmailAddress);
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);
				
		    	if(AccountUtils.sendEmailFromServer(RetrievePwdActivity.this, mTableAccountInfo)){
		    		mHandler.post(new Runnable() {
						@Override
						public void run() {
				    		mSendEmailTimeCount.start();
						}
					});
		    	}
			}
		});

    }


//////////////////////点击事件处理end
	////////////
	private void doRetrievePwd(){
		final String userName;
		if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
			userName = editPhone.getText().toString().trim();
		}else{
			userName = editPhoneOrMail.getText().toString().trim();
		}
		final boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
		Log.i(TAG, "doRetrievePwd() 111 mAccountStartMode:"+mAccountStartMode+" userName:"+userName);
		if(ActivityUtils.isMobileNO(userName) || isEmailWhithSecurityPhone){ //手机号或者邮箱有安全码找回密码
			//imageStep2PhoneBootWizardRightArrow
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
		}else if(ActivityUtils.isEmail(userName)){
			btnStep3Complete.setEnabled(false);
		}
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				String newPwd;
				mTableAccountInfo.clear();
				if(ActivityUtils.isMobileNO(userName) || isEmailWhithSecurityPhone){ //手机号或者邮箱有安全码找回密码
					String smsCode = editSmsVerifyCode.getText().toString().trim();
					newPwd = editNewPwdPhone.getText().toString().trim();
					mTableAccountInfo.put(Constants.KEY_RETRIEVE_PWD_TYPE, Constants.RETRIEVE_PWD_TYPE_PHONE);
					if(ActivityUtils.isMobileNO(userName)) {
                        mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, userName);
                    }else{
                        mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, userName);
                    }
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_NEW_PWD, newPwd);
					mTableAccountInfo.put(Constants.KEY_SMS_CODE, smsCode);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);
					if(!notShowSmsCodeLayout && !AccountUtils.verifySmsCodeValidFromServer(RetrievePwdActivity.this, mTableAccountInfo)){
						//需先验证码通过才能执行，下一步，验证码不正确返回参数
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								btnStep2PhoneComplete.setEnabled(true);
							}
						});

						Log.e(TAG, "doRetrievePwd() smscode error from server!");
						return;
					}
				}else if(ActivityUtils.isEmail(userName)){
					newPwd = editNewPwd.getText().toString().trim();
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, userName);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_NEW_PWD, newPwd);
					mTableAccountInfo.put(Constants.KEY_RETRIEVE_PWD_TYPE, Constants.RETRIEVE_PWD_TYPE_MAIL);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);
				}else{
					Log.e(TAG, "doRetrievePwd()  error!!!!!!!!");
					return; 
				}
				
				Log.i(TAG, "doRetrievePwd()  222");
				if(AccountUtils.retrievePwd(RetrievePwdActivity.this,mTableAccountInfo)){//先找回密码
					mHandler.post(new Runnable() { //ui线程操作,找回密码后跳转登录界面
						@Override
						public void run() {
							ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.txt_modify_pwd_success));
							Intent i = new Intent(RetrievePwdActivity.this,LoginActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
								i.setAction(Constants.BOOT_WIZARD_MODE);
							}
							ActivityUtils.closeKeyboard(RetrievePwdActivity.this);//关闭软键盘
							RetrievePwdActivity.this.startActivity(i);
					        finish();
						}
					});

				}
				Log.i(TAG, "doRetrievePwd()  333");
				//设置按钮复位
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if(ActivityUtils.isMobileNO(userName)){
							if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
								textStep2PhoneBootWizardNextButton.setEnabled(true);
								imageStep2PhoneBootWizardRightArrow.setEnabled(true);
                                textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                                textStep2PhoneBootWizardNextButton.setOnClickListener(doOnlcikStep2CompleteOnClickListener);
                                imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                                imageStep2PhoneBootWizardRightArrow.setOnClickListener(doOnlcikStep2CompleteOnClickListener);
							}else{
								btnStep2PhoneComplete.setEnabled(true);
							}
						}else if(ActivityUtils.isEmail(userName)){
							btnStep3Complete.setEnabled(true);
						}
					}
				});
				

			}
		});
	}

	void showStep(String keyLayout,boolean...backMode){
		mCurrnetStep = keyLayout;
        boolean isBackMode = false;
        boolean isUseAnimation = true;
        if(null != backMode &&  0 != backMode.length){
            isBackMode = true;
        }
        if((KEY_LAYOUT_STEP1_PHONE.equals(keyLayout) || KEY_LAYOUT_STEP1.equals(keyLayout))&& !isBackMode){  //从activity加载的第一个界面无需再加动画
            isUseAnimation = false;
        }
        Log.i(TAG, "showStep keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size())
                +" isBackMode:"+isBackMode+" isUseAnimation:"+isUseAnimation);
		if(KEY_LAYOUT_STEP1.equals(mCurrnetStep)) {
			Log.i("yimin","modify email first step");
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		} else {
			Log.i("yimin","modify others");
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
        ActivityUtils.closeKeyboard(this); //切换界面关闭软键盘
		if(KEY_LAYOUT_STEP4.equals(mCurrnetStep)){ //处理返回到上一个输入手机界面，下一个界面发送短信验证码未更新的情况
			String strFormat = getResources().getString(R.string.txt_send_email_reminder_part1);
			String str = String.format(strFormat,editPhoneOrMail.getText().toString());
			textSendEmailReminder.setText(str);
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
	boolean showPreStep(String currentStep){
		if(KEY_LAYOUT_STEP2.equals(currentStep)){ //手机输入密码界面返回
            mAccountStartMode = AccountUtils.getLocalAccountStartModeFromShare(RetrievePwdActivity.this);
            Log.i(TAG, "showPreStep() KEY_LAYOUT_STEP2 mAccountStartMode:"+mAccountStartMode);
            if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
                mCurrnetStep = KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE;
            }else{
                mCurrnetStep = KEY_LAYOUT_STEP1;
            }
		}else if(KEY_LAYOUT_STEP3.equals(currentStep)){ //邮箱输入密码界面返回
			mCurrnetStep = KEY_LAYOUT_STEP1;
		}else if(KEY_LAYOUT_STEP4.equals(currentStep)){ //邮箱激活界面返回
			mCurrnetStep = KEY_LAYOUT_STEP1;
		}else if(KEY_LAYOUT_BOOT_WIZARD_VERIFY_SMS_CODE == currentStep){
			mCurrnetStep = KEY_LAYOUT_STEP1_PHONE;
		}else{
			return false;
		}
		showStep(mCurrnetStep,true);
		return true;
	}

	
	/**
	 * 检查手机号
	 * @param phoneNum
	 * @return
	 */
    @Deprecated
	private boolean checkPhoneNum(String phoneNumber) {
		if (phoneNumber.length() == 11) {
			return true;
		} else if (phoneNumber.length() == 0) {
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_text_phone_num_null));
		} else { 
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_text_phone_num_length_error));
		}
		return false;
	}
    //需要在线程里面调用
	private boolean chekcStep1Input(){
		String str = editPhoneOrMail.getText().toString().trim();
        if(!ActivityUtils.isEmail(str) && !ActivityUtils.isMobileNO(str)){
        	ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_input_valid_phone_or_email));
        	return false;
        }
        if(ActivityUtils.isMobileNO(str)){   //输入手机号在服务器是否可用
        	mTableAccountInfo.clear();
        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, str);
        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);//注册验证手机号
        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(getApplicationContext(), mTableAccountInfo)){
        		return false;
        	}
        }else if(ActivityUtils.isEmail(str)){  //输入邮箱在服务器是否可用
        	mTableAccountInfo.clear();
        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, str);
        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);//注册验证手机号
        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(getApplicationContext(), mTableAccountInfo)){
        		return false;
        	}
        }
		return true;
	}
	//需要在线程里面调用
	private boolean chekcStep1PhoneInput(){
		String str = editPhone.getText().toString().trim();
		if(ActivityUtils.isMobileNO(str)){   //输入手机号在服务器是否可用
        	mTableAccountInfo.clear();
        	mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, str);
        	mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_RETRIEVE_PWD);//注册验证手机号
        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(RetrievePwdActivity.this, mTableAccountInfo)){
        		return false;
        	}
	    }
		return true;
	}
	/**
	 * 
	 * @return
	 */
	private boolean checkStep2Input(){
		//String str = editSmsVerifyCode.getText().toString().trim();
    	boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
		Log.i(TAG, "checkStep2Input() mAccountStartMode:"+mAccountStartMode+" notShowSmsCodeLayout:"+notShowSmsCodeLayout);
		if(!notShowSmsCodeLayout && editSmsVerifyCode.getText().toString().isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_text_sms_verify_code_null));
		}else if(editNewPwdPhone.getText().toString().isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_new_pwd_null));
		}else if(editConfirmNewPwdPhone.getText().toString().isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_confirm_new_pwd_null));
		}else if(!editNewPwdPhone.getText().toString().equals(editConfirmNewPwdPhone.getText().toString())){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_two_new_pwd_not_equal));
		}else{
			return true;
		}
		return false;
//		mTableAccountInfo.clear();
//		mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
//		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editPhoneOrMail.getText().toString().trim());
//		mTableAccountInfo.put(Constants.KEY_SMS_CODE,editSmsVerifyCode.getText().toString().trim());
//		if(!AccountUtils.verifySmsCodeFromServer(RetrievePwdActivity.this, mTableAccountInfo)){
//			Log.i(TAG, "checkStep2Input error!! return");
//			return false;
//		}

	}
	private boolean checkStep3Input(){
		String newPwd = editNewPwd.getText().toString().trim();
		String confirmNewPwd = editConfirmNewPwd.getText().toString().trim();
		if(null == newPwd || newPwd.isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_new_pwd_null));
			return false;
		}else if(null == confirmNewPwd || confirmNewPwd.isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_confirm_new_pwd_null));
			return false;
		}else if(!confirmNewPwd.equals(newPwd)){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_two_new_pwd_not_equal));
			return false;
		}
		return true;
	}
	private boolean checkInput(){
		String phoneNumber = editPhoneOrMail.getText().toString().trim();
		String smsCode = editSmsVerifyCode.getText().toString().trim();
		String newPwd = editNewPwd.getText().toString().trim();
		String confirmNewPwd = editConfirmNewPwd.getText().toString().trim();
		if(!checkPhoneNum(phoneNumber)){
			return false;
		}else if(null == smsCode || smsCode.isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_text_sms_verify_code_null));
			return false;
		}else if(null == newPwd || newPwd.isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_new_pwd_null));
			return false;
		}else if(null == confirmNewPwd || confirmNewPwd.isEmpty()){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_confirm_new_pwd_null));
			return false;
		}else if(!confirmNewPwd.equals(newPwd)){
			ActivityUtils.alert(RetrievePwdActivity.this, getResources().getString(R.string.alert_two_new_pwd_not_equal));
			return false;
		}
		return true;
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
	class SmsCodeTimeCount extends CountDownTimer {
		public SmsCodeTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
			if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
				textSendSmsCodeForBootWizard.setClickable(false);
				textSendSmsCodeForBootWizard.setText(msg);
			}else{
				textSendSmsVerifyCode.setClickable(false);
				textSendSmsVerifyCode.setText(msg);
			}

		}
		@Override
		public void onFinish() {
			if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
				textSendSmsCodeForBootWizard.setClickable(true);
				textSendSmsCodeForBootWizard.setText(getResources().getString(R.string.register_send_again));
			}else{
				textSendSmsVerifyCode.setText(getResources().getString(R.string.register_send_again));
				textSendSmsVerifyCode.setClickable(true);
			}
		}
	}
	class SendEmailTimeCount extends CountDownTimer {
		public SendEmailTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			//textSendEmail.setBackgroundColor(getResources().getColor(R.color.color_button_diable_gray));
			textResendEmail.setClickable(false);
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		textResendEmail.setText(msg);
			//btnSendSmsVerifyCode.setText("(" + millisUntilFinished / 1000 + ")s后重新发送");
		}
		@Override
		public void onFinish() {
			textResendEmail.setText(getResources().getString(R.string.register_send_again));
			textResendEmail.setClickable(true);
			//textSendEmail.setBackgroundColor(getResources().getColor(R.color.gray_light));

		}
	}
	//设置输入框输入监听
//	class EditChangedListener implements TextWatcher{
//		private int mEditType = -1;
//		public EditChangedListener(int editType){
//			mEditType = editType;
//		}
//		@Override
//		public void afterTextChanged(Editable s) {
//			Log.e(TAG, "afterTextChanged() 00 mEditType:"+mEditType+" s.toString():"+s.toString());
//			if(s.toString().length()>0){
//				showAfterTextNotEmpty(mEditType);
//			}else{
//				showAfterTextEmpty(mEditType);
//			}
//			
//		}
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
//	}
	//输入框内容非空
	public void showAfterTextNotEmpty(int editType){
		Log.i(TAG, "showAfterTextNotEmpty() editType:"+editType);
		switch (editType) {
			case TYPE_EDIT_PHONE:
				imageDeletePhone.setVisibility(View.VISIBLE);
				textStep1PhoneBootWizardNextButton.setEnabled(true);
				imageStep1PhoneBootWizardRightArrow.setEnabled(true);
                textStep1PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                textStep1PhoneBootWizardNextButton.setOnClickListener(doOnclickStep1PhoneNextOnClickListener);
                imageStep1PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                imageStep1PhoneBootWizardRightArrow.setOnClickListener(doOnclickStep1PhoneNextOnClickListener);
				break;
			case TYPE_EDIT_PHONE_OR_MAIL:
				if(!AccountUtils.getLoginState(RetrievePwdActivity.this)) //登录状态自动填充文本框不可编辑，非登录状态才显示删除按钮
					imageDeleteAccountName.setVisibility(View.VISIBLE);
				btnStep1Next.setEnabled(true);
				break;
			case TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE://找回密码第二步，监听输入验证码，非开机向导模式才显示
				imageDeleteSmsVerifyCode.setVisibility(View.VISIBLE);
				if(checkStep2PhoneInputFull()){
					btnStep2PhoneComplete.setEnabled(true);
				}
				break;
			case TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD:
				imageDeleteSmsVerifyCodeForBootWizard.setVisibility(View.VISIBLE);
				if(checkBootWizardVerifySmsCodeInputFull()){
					textBootWizardVerifySmsCodeNextButton.setEnabled(true);
					imageBootWizardVerifySmsCodeRightArrow.setEnabled(true);
                    textBootWizardVerifySmsCodeNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                    textBootWizardVerifySmsCodeNextButton.setOnClickListener(doOnclickBootWizardVerifySmsCodeNextOnClickListener);
                    imageBootWizardVerifySmsCodeRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                    imageBootWizardVerifySmsCodeRightArrow.setOnClickListener(doOnclickBootWizardVerifySmsCodeNextOnClickListener);
				}
				break;
			case TYPE_EDIT_INPUT_PWD_PHONE: //找回密码第二步，监听输入密码，
				imageDeleteInputPwdPhone.setVisibility(View.VISIBLE);
				if(checkStep2PhoneInputFull()){
					if(Constants.BOOT_WIZARD_MODE.equals(mAccountStartMode)){
						textStep2PhoneBootWizardNextButton.setEnabled(true);
						imageStep2PhoneBootWizardRightArrow.setEnabled(true);
                        textStep2PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_button_link));
                        textStep2PhoneBootWizardNextButton.setOnClickListener(doOnlcikStep2CompleteOnClickListener);
                        imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                        imageStep2PhoneBootWizardRightArrow.setOnClickListener(doOnlcikStep2CompleteOnClickListener);
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
                        textStep2PhoneBootWizardNextButton.setOnClickListener(doOnlcikStep2CompleteOnClickListener);
                        imageStep2PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right);
                        imageStep2PhoneBootWizardRightArrow.setOnClickListener(doOnlcikStep2CompleteOnClickListener);
					}else{
						btnStep2PhoneComplete.setEnabled(true);
					}
				}
				break;
			case TYPE_EDIT_INPUT_PWD:
				imageDeleteInputPwd.setVisibility(View.VISIBLE);
				if(checkStep2EmailInputFull()){
					btnStep3Complete.setEnabled(true);
				}
				break;
			case TYPE_EDIT_INPUT_PWD_AGAIN:
				imageDeleteInputPwdAgain.setVisibility(View.VISIBLE);
				if(checkStep2EmailInputFull()){
					btnStep3Complete.setEnabled(true);
				}
				break;
			default:
				break;
		}
	}
	//输入框内容为空
	public void showAfterTextEmpty(int editType){
		Log.i(TAG, "showAfterTextEmpty() editType:"+editType);
		switch (editType) {
			case TYPE_EDIT_PHONE:
				imageDeletePhone.setVisibility(View.GONE);
				textStep1PhoneBootWizardNextButton.setEnabled(false);
				imageStep1PhoneBootWizardRightArrow.setEnabled(false);
                textStep1PhoneBootWizardNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
                textStep1PhoneBootWizardNextButton.setOnClickListener(null);
                imageStep1PhoneBootWizardRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
                imageStep1PhoneBootWizardRightArrow.setOnClickListener(null);
				break;
			case TYPE_EDIT_PHONE_OR_MAIL:
				imageDeleteAccountName.setVisibility(View.GONE);
				btnStep1Next.setEnabled(false);
				break;
			case TYPE_EDIT_SMS_CODE_FOR_STEP2_PHONE:   //找回密码第二步，监听输入验证码，非开机向导模式才显示
				imageDeleteSmsVerifyCode.setVisibility(View.GONE);
				btnStep2PhoneComplete.setEnabled(false);
				break;
			case TYPE_EDIT_SMS_CODE_FOR_BOOT_WIZARD:
				imageDeleteSmsVerifyCodeForBootWizard.setVisibility(View.GONE);
				textBootWizardVerifySmsCodeNextButton.setEnabled(false);
				imageBootWizardVerifySmsCodeRightArrow.setEnabled(false);
                textBootWizardVerifySmsCodeNextButton.setTextColor(getResources().getColor(R.color.color_text_disable));
                textBootWizardVerifySmsCodeNextButton.setOnClickListener(null);
                imageBootWizardVerifySmsCodeRightArrow.setImageResource(R.drawable.boot_wizard_arrow_right_disable);
                imageBootWizardVerifySmsCodeRightArrow.setOnClickListener(null);
				break;
			case TYPE_EDIT_INPUT_PWD_PHONE: //找回密码第二步，监听输入密码，
				imageDeleteInputPwdPhone.setVisibility(View.GONE);
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
				break;
			case TYPE_EDIT_INPUT_PWD_AGAIN_PHONE: //找回密码第二步，监听再次输入密码，
				imageDeleteInputPwdAgainPhone.setVisibility(View.GONE);
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
				break;
			case TYPE_EDIT_INPUT_PWD:
				imageDeleteInputPwd.setVisibility(View.GONE);
				btnStep3Complete.setEnabled(false);
				break;
			case TYPE_EDIT_INPUT_PWD_AGAIN:
				imageDeleteInputPwdAgain.setVisibility(View.GONE);
				btnStep3Complete.setEnabled(false);
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
	 * 判断手机找回密码步骤2是否都填写了
	 * @return
	 */
    boolean checkStep2PhoneInputFull(){
    	final boolean notShowSmsCodeLayout = isNotShowSmsCodeLayout();
    	Log.i(TAG, "checkStep2PhoneInputFull() notShowSmsCodeLayout:"+notShowSmsCodeLayout);
    	if(!notShowSmsCodeLayout && editSmsVerifyCode.getText().toString().trim().isEmpty()){
    		return false;
    	}else if(editNewPwdPhone.getText().toString().trim().isEmpty()){
    		return false;
    	}else if(editConfirmNewPwdPhone.getText().toString().trim().isEmpty()){
    		return false;
    	}
    	return true;
    }
    /**
     * 判断邮箱找回密码步骤2是否都填写了
     * @return
     */
    boolean checkStep2EmailInputFull(){
    	if(editNewPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}else if(editConfirmNewPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}
    	return true;
    }
}
