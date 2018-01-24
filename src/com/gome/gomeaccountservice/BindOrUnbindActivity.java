package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

public class BindOrUnbindActivity extends Activity implements TextChangeCallback{

	static final String TAG = Constants.TAG_PRE+"BindOrUnbindActivity";
    Animation mAnimationOpenEnter;
    Animation mAnimationOpenExit;
    Animation mAnimationCloseEnter;
    Animation mAnimationCloseExit;

    HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	String mEmailAddress = null;
	String mPhoneNumber = null;
	String mGomeId = null;
	String mRegisterType = null;
    String mToken = null;
    String mEmailActiveStatus = null;
    
	public static String ACTION_BIND_EMAIL = "action_bind_email";
	public static String ACTION_BIND_PHONE = "action_bind_phone";
	
	private String mBindToActiveEmail = null; //需绑定的邮箱地址
	private String mUnbindToActiveEmail = null; //解绑待激活的邮箱地址
	
	GomeAlertDialog mDialog;
	
	TextView textBindResendEmail;
	TextView textUnbindResendEmail;
	TextView textEmail;
	TextView textBindEmailSendEmailReminder;
	TextView textUnbindEmailSendEmailReminder;
	TextView textBindSmsVerifyCode;
	TextView textUnbindSmsVerifyCode;
	TextView textBindPhone;
	TextView textPhoneUnbindStep1;
	TextView textPhoneUnbindStep2;
	
	TextView textUnbindPhoneHint;
	TextView textUnbindEmailHint;
	
	Button btnBindEmail;
	Button btnBindPhone;
	Button btnUnbindEmail;
	Button btnAlertUnbindEmail;
	Button btnUnbindPhone;
	Button btnUnbindPhoneComplete;
	
	ImageView imageDeleteBindEmail;
	ImageView imageDeleteBindPhone;
	ImageView imageDeleteSmsCodeForBindPhone;
	ImageView imageDeleteSmsCodeForUnbindPhone;
	
	EditText editBindEmail;
	EditText editBindPhone;
	EditText editBindSmsVerifyCode;
	EditText editUnbindSmsVerifyCode;
	
	final int TYPE_EDIT_BIND_MAIL = 1;
	final int TYPE_EDIT_BIND_PHONE = 2;
	final int TYPE_EDIT_BIND_SMS_CODE = 3;
	final int TYPE_EDIT_UNBIND_SMS_CODE = 4;
	
    boolean isBindSmsCodeTimeCounting = false; //是否绑定验证码正在倒计时
	private BindSmsCodeTimeCount mBindSmsCodeTimeCount = null;
	private BindSendEmailTimeCount mBindSendEmailTimeCount = null;
	private UnbindSmsCodeTimeCount mUnbindSmsCodeTimeCount = null;
	private UnbindSendEmailTimeCount mUnbindSendEmailTimeCount;
	String mCurrnetStep = null;
	HashMap<String, LinearLayout> mLayouts;
	LinearLayout layoutBindEmailStep1;
	LinearLayout layoutBindEmailStep2;
	LinearLayout layoutUnbindEmailStep1;
	LinearLayout layoutUnbindEmailStep2;
	LinearLayout layoutBindPhoneStep1;
	//LinearLayout layoutBindPhoneStep2;
	LinearLayout layoutUnbindPhoneStep1;
	LinearLayout layoutUnbindPhoneStep2;
	
	final String KEY_LAYOUT_BIND_EMAIL_STEP1 = "bindEmailStep1";
	final String KEY_LAYOUT_BIND_EMAIL_STEP2 = "bindEmailStep2";
	final String KEY_LAYOUT_UNBIND_EMAIL_STEP1 = "unbindEmailStep1";
	final String KEY_LAYOUT_UNBIND_EMAIL_STEP2 = "unbindEmailStep2";
	final String KEY_LAYOUT_BIND_PHONE_STEP1 = "bindPhoneStep1";
	//final String KEY_LAYOUT_BIND_PHONE_STEP2 = "bindPhoneStep2";
	final String KEY_LAYOUT_UNBIND_PHONE_STEP1 = "unbindPhoneStep1";
	final String KEY_LAYOUT_UNBIND_PHONE_STEP2 = "unbindPhoneStep2";
	  
	final int DIALOG_UNBIND_EMAIL =1;
	final int DIALOG_UNBIND_PHONE =2;
	private static final HandlerThread sWorkerThread = new HandlerThread("AccountInfoActivity-loader");
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
	private static final Handler mHandler =  new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && this.isInMultiWindowMode()){
            Log.i(TAG, "onCreate() isInMultiWindowMode finish()");
            ActivityUtils.alert(this,getResources().getString(R.string.alert_gomeaccount_no_support_multi_window_mode));
            finish();
        }
		setContentView(R.layout.bind_or_unbind_phone_mail_activity);
		getAccountInfo();
		initView();
		parseAction();
		

	}
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }
	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed() mCurrnetStep:"+mCurrnetStep);
		if(!showPreStep(mCurrnetStep)){
			finish();
		}
	}
	/**
	 * 
	 * @param keyLayout
	 * @return 是否有上一步界面，若没有则返回false
	 */
	boolean showPreStep(String keyLayout){
		if(KEY_LAYOUT_BIND_EMAIL_STEP2.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_BIND_EMAIL_STEP1;
		}else if(KEY_LAYOUT_UNBIND_EMAIL_STEP2.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_UNBIND_EMAIL_STEP1;
			unBindEmailShowPreStep();
			return true;
		}else if(KEY_LAYOUT_UNBIND_PHONE_STEP2.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_UNBIND_PHONE_STEP1;
		}else{
			return false;
		}
		showStep(mCurrnetStep,true);
		return true;
	}
	/**
	 * 解绑邮箱点击上一步，需先判断是否解绑成功，若解绑成功跳转主界面，若未解绑成功跳转原来的界面界面。
	 */
	void unBindEmailShowPreStep(){
		Log.i(TAG, "unBindEmailShowPreStep() 111");
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mUnbindToActiveEmail);  //邮箱传递删除标志
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_UNBIND); //解绑邮箱
				mTableAccountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, Constants.EMAIL_STATUS_TO_UNBIND);//解绑状态
				if(AccountUtils.verifyEmailStatusFromServer(BindOrUnbindActivity.this, mTableAccountInfo,false)){  //向服务器请求判断解绑邮箱是否验证成功
					mHandler.post(new Runnable() { //ui 线程处理
						@Override
						public void run() {
							Log.i(TAG, "unBindEmailShowPreStep() 222");
							mUnbindToActiveEmail = null;
							Intent data = new Intent();
							data.setAction(AccountInfoActivity.ACTION_UNBIND_EMAIL_SUCCESS);
							setResult(RESULT_OK,data);
							Log.i(TAG, "unBindEmailShowPreStep() 333 setResult ACTION_UNBIND_EMAIL_SUCCESS");
							finish();
						}
					});

				}else{  //未解绑成功
					Log.i(TAG, "unBindEmailShowPreStep() 333");
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mCurrnetStep = KEY_LAYOUT_UNBIND_EMAIL_STEP1;
							showStep(mCurrnetStep,true);
						}
					});
				}
			}
		});
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		Log.i(TAG, "onResume() mBindToActiveEmail:"+mBindToActiveEmail+" mUnbindToActiveEmail:"+mUnbindToActiveEmail+" mEmailActiveStatus:"+mEmailActiveStatus);
		if(null != mBindToActiveEmail && null != mUnbindToActiveEmail){
			Log.e(TAG, "onResume() error!!! mBindEmailAddress and mUnbindToActiveEmail not null");
		}
        verifyEmailActived(false);

	}
    /**
     * 验证邮箱是否激活
     * @param isFromClick  是否手动点击按钮
     */
    void verifyEmailActived(final boolean isFromClick){
        Log.i(TAG,"verifyEmailActived isFromClick:"+isFromClick+" mBindToActiveEmail:"+mBindToActiveEmail
                +" mUnbindToActiveEmail:"+mUnbindToActiveEmail);
        if(null != mBindToActiveEmail){   //需绑定的邮箱未激活
            verifyBindEmail(isFromClick);
        }else if(null != mUnbindToActiveEmail){
            verifyUnbindEmail(isFromClick);
        }
    }
    /**
     * 点击已验证按钮
     * @param view
     */
    public void doAlreadyVerified(View view) {
        Log.i(TAG, "doAlreadyVerified() ");
        verifyEmailActived(true);
    }
	//验证绑定邮箱是否激活
	void verifyBindEmail(final boolean isFromClick){
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "verifyBindEmail() mGomeId:"+mGomeId+" mBindToActiveEmail:"+mBindToActiveEmail+" mToken:"+mToken
                    +" isFromClick:"+isFromClick);
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mBindToActiveEmail);  //传递需要绑定的邮箱
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_BIND);

				if(AccountUtils.verifyEmailStatusFromServer(BindOrUnbindActivity.this, mTableAccountInfo,false)){  //从服务器判断邮箱是否激活
					mHandler.post(new Runnable() {//ui 线程处理
						@Override
						public void run() {
							Intent data = new Intent();
							data.setAction(AccountInfoActivity.ACTION_BIND_EMAIL_SUCCESS);
							setResult(RESULT_OK,data);
							finish();
							
						}
					});

				}else{
                    if(isFromClick){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ActivityUtils.alert(BindOrUnbindActivity.this,getResources().getString(R.string.txt_gome_account_not_verified));
                            }
                        });
                    }
                }
			}
		});
	}
	void verifyUnbindEmail(final boolean isFromClick){
		Log.i(TAG, "verifyUnbindEmail() isFromClick:"+isFromClick);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mUnbindToActiveEmail);  //邮箱传递删除标志
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_UNBIND); //解绑邮箱
				mTableAccountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, Constants.EMAIL_STATUS_TO_UNBIND);//解绑状态
				if(AccountUtils.verifyEmailStatusFromServer(BindOrUnbindActivity.this, mTableAccountInfo,false)){  //向服务器请求判断解绑邮箱是否验证成功
					mHandler.post(new Runnable() { //ui 线程处理
						@Override
						public void run() {
							mUnbindToActiveEmail = null;
							Intent data = new Intent();
							data.setAction(AccountInfoActivity.ACTION_UNBIND_EMAIL_SUCCESS);
							setResult(RESULT_OK,data);
							finish();
						}
					});

				}else{
                    if(isFromClick){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ActivityUtils.alert(BindOrUnbindActivity.this,getResources().getString(R.string.txt_gome_account_not_verified));
                            }
                        });
                    }
                }
			}
		});
	}
	public void getAccountInfo() {
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(this); //获取本地账号
    	if(null != gomeAccount){
	    	HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(this,gomeAccount.name); 
	    	mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
	    	mEmailActiveStatus = tableAccountInfo.get(Constants.KEY_EMAIL_ACTIVE_STATUS);
	    	mGomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
		    mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);  			//
		    mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
		    Log.i(TAG, "getAccountInfo() mToken:"+mToken+" mGomeId:"+mGomeId+" mEmailAddress:"+mEmailAddress+
		    		" mPhoneNumber:"+mPhoneNumber+" mEmailActiveStatus:"+mEmailActiveStatus);
    	}
	}
	void parseAction(){
		String action = getIntent().getAction();
		Log.i(TAG, "parseAction() 111 action:"+action+" mPhoneNumber:"+mPhoneNumber+" mEmailAddress:"+mEmailAddress+
				" mEmailActiveStatus:"+mEmailActiveStatus);
		if(ACTION_BIND_EMAIL.equals(action)){
			if(null != mEmailAddress && !Constants.EMAIL_STATUS_TO_BIND.equals(mEmailActiveStatus)){  //解绑邮箱 ，非待绑定状态都可以执行解绑
				showUnbindEmailLayout();
			}else{                      //绑定邮箱
				showStep(KEY_LAYOUT_BIND_EMAIL_STEP1);
			}
		}else if(ACTION_BIND_PHONE.equals(action)){
			if(null != mPhoneNumber){  //解绑手机号
				showUnbindPhoneLayout();
			}else{                      //绑定手机号
				showStep(KEY_LAYOUT_BIND_PHONE_STEP1);
			}
		}
	}
	//判断是否可以解绑，手机号不为空，且邮箱不为空非待绑定状态
	boolean canUnbind(){
		Log.i(TAG, "canUnbind() mEmailAddress:"+mEmailAddress+" mPhoneNumber:"+mPhoneNumber);
		if((null != mEmailAddress && !Constants.EMAIL_STATUS_TO_BIND.equals(mEmailActiveStatus)) && null != mPhoneNumber){ //只有绑定手机号和邮箱才可以解绑
			return true;
		}
		return false;
	}
	//显示解绑手机布局
	void showUnbindPhoneLayout(){
		Log.i(TAG, "showUnbindPhoneLayout() canUnbind:"+canUnbind());
		if(canUnbind()){
			btnUnbindPhone.setEnabled(true);
			textUnbindPhoneHint.setVisibility(View.GONE);
		}else{
			btnUnbindPhone.setEnabled(false);
			textUnbindPhoneHint.setVisibility(View.VISIBLE);
		}
		textPhoneUnbindStep1.setText(ActivityUtils.convertPhoneNumShow(mPhoneNumber));
		showStep(KEY_LAYOUT_UNBIND_PHONE_STEP1);

	}
	//显示解绑邮箱布局
	void showUnbindEmailLayout(){
		Log.i(TAG, "showUnbindEmailLayout() canUnbind:"+canUnbind());
		if(canUnbind()){
			btnUnbindEmail.setEnabled(true);
			textUnbindEmailHint.setVisibility(View.GONE);
		}else{
			btnUnbindEmail.setEnabled(false);
			textUnbindEmailHint.setVisibility(View.VISIBLE);
		}
		showStep(KEY_LAYOUT_UNBIND_EMAIL_STEP1);
		textEmail.setText(mEmailAddress);
	}
	void initView(){
        mAnimationOpenEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_enter_layout);
        mAnimationOpenExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_exit_layout);
        mAnimationCloseEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_enter_layout);
        mAnimationCloseExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_exit_layout);

		layoutBindEmailStep1 = (LinearLayout)findViewById(R.id.layout_bind_email_step1);
		layoutBindEmailStep2 = (LinearLayout)findViewById(R.id.layout_bind_email_step2);
		layoutUnbindEmailStep1 = (LinearLayout)findViewById(R.id.layout_unbind_email_step1);
		layoutUnbindEmailStep2 = (LinearLayout)findViewById(R.id.layout_unbind_email_step2);
		layoutBindPhoneStep1 = (LinearLayout)findViewById(R.id.layout_bind_phone_step1);
		layoutUnbindPhoneStep1 = (LinearLayout)findViewById(R.id.layout_unbind_phone_step1);
		layoutUnbindPhoneStep2 = (LinearLayout)findViewById(R.id.layout_unbind_phone_step2);
		
		mLayouts = new HashMap<String, LinearLayout>();
		mLayouts.put(KEY_LAYOUT_BIND_EMAIL_STEP1, layoutBindEmailStep1);
		mLayouts.put(KEY_LAYOUT_BIND_EMAIL_STEP2, layoutBindEmailStep2);
		mLayouts.put(KEY_LAYOUT_UNBIND_EMAIL_STEP1, layoutUnbindEmailStep1);
		mLayouts.put(KEY_LAYOUT_UNBIND_EMAIL_STEP2, layoutUnbindEmailStep2);
		mLayouts.put(KEY_LAYOUT_BIND_PHONE_STEP1, layoutBindPhoneStep1);
		mLayouts.put(KEY_LAYOUT_UNBIND_PHONE_STEP1, layoutUnbindPhoneStep1);
		mLayouts.put(KEY_LAYOUT_UNBIND_PHONE_STEP2, layoutUnbindPhoneStep2);
		
		mBindSmsCodeTimeCount = new BindSmsCodeTimeCount(60000, 1000);
		mBindSendEmailTimeCount = new BindSendEmailTimeCount(60000, 1000);
		mUnbindSmsCodeTimeCount = new UnbindSmsCodeTimeCount(60000, 1000);
		mUnbindSendEmailTimeCount = new UnbindSendEmailTimeCount(60000, 1000);
		
		editBindEmail = (EditText)findViewById(R.id.edit_bind_mail);
		editBindEmail.addTextChangedListener(new EditChangedListener(TYPE_EDIT_BIND_MAIL,this));
		editBindPhone = (EditText)findViewById(R.id.edit_bind_phone);//
		editBindPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_BIND_PHONE,this));
		editBindSmsVerifyCode = (EditText)findViewById(R.id.edit_bind_sms_verify_code);
		editBindSmsVerifyCode.addTextChangedListener(new EditChangedListener(TYPE_EDIT_BIND_SMS_CODE,this));
		editUnbindSmsVerifyCode = (EditText)findViewById(R.id.edit_unbind_sms_verify_code);//
		editUnbindSmsVerifyCode.addTextChangedListener(new EditChangedListener(TYPE_EDIT_UNBIND_SMS_CODE,this));
		
		
		btnBindEmail = (Button)findViewById(R.id.btn_bind_email);
		btnBindPhone = (Button)findViewById(R.id.btn_bind_phone);
		btnUnbindEmail = (Button)findViewById(R.id.btn_unbind_email);
		btnUnbindPhone = (Button)findViewById(R.id.btn_unbind_phone);
		btnUnbindPhoneComplete = (Button)findViewById(R.id.btn_unbind_phone_complete);
		
		imageDeleteBindEmail = (ImageView)findViewById(R.id.image_delete_bind_email);
		imageDeleteBindPhone = (ImageView)findViewById(R.id.image_delete_bind_phone);
		imageDeleteSmsCodeForBindPhone = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_bind_phone);
		imageDeleteSmsCodeForUnbindPhone = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_unbind_phone);
		
		textBindResendEmail = (TextView)findViewById(R.id.text_bind_resend_mail);//点击一次后会变灰色一分钟
		textUnbindResendEmail = (TextView)findViewById(R.id.text_unbind_resend_mail);//点击一次后会变灰色一分钟
		textEmail = (TextView)findViewById(R.id.text_email);
		textBindPhone = (TextView)findViewById(R.id.text_bind_phone);
		textPhoneUnbindStep1 = (TextView)findViewById(R.id.text_phone_unbind_step1);
		textPhoneUnbindStep2 = (TextView)findViewById(R.id.text_phone_unbind_step2);
		textBindSmsVerifyCode = (TextView)findViewById(R.id.text_bind_send_sms_verify_code);
		textUnbindSmsVerifyCode = (TextView)findViewById(R.id.text_unbind_send_sms_verify_code);
		
		textUnbindPhoneHint = (TextView)findViewById(R.id.txt_cannot_unbind_phone_hint);
		textUnbindEmailHint = (TextView)findViewById(R.id.txt_cannot_unbind_email_hint);
		
		textBindEmailSendEmailReminder = (TextView)findViewById(R.id.text_bind_email_send_email_remiander);
		textUnbindEmailSendEmailReminder = (TextView)findViewById(R.id.text_unbind_email_send_email_remiander);
        if(editBindPhone.getText().toString().isEmpty()){//手机号为空则发送验证码灰色显示
            setBindSmsVerifyCodeEnable(false);
        }else{//手机号不为空,则发送验证码高亮显示
            setBindSmsVerifyCodeEnable(true);
        }
	}
	void showStep(String keyLayout,boolean...backMode){
		mCurrnetStep = keyLayout;
        boolean isBackMode = false;
        boolean isUseAnimation = true;
        if(null != backMode &&  0 != backMode.length){
            isBackMode = true;
        }
        if((KEY_LAYOUT_BIND_EMAIL_STEP1.equals(keyLayout)||KEY_LAYOUT_UNBIND_EMAIL_STEP1.equals(keyLayout)
            ||KEY_LAYOUT_BIND_PHONE_STEP1.equals(keyLayout)||KEY_LAYOUT_UNBIND_PHONE_STEP1.equals(keyLayout))
                && !isBackMode){  //从activity加载的第一个界面无需再加动画
            isUseAnimation = false;
        }
        if(KEY_LAYOUT_BIND_PHONE_STEP1.equals(keyLayout)) {//绑定手机界面初始化是否正在倒计时状态
            isBindSmsCodeTimeCounting = false;
        }
        Log.i(TAG, "showStep keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size())
                +" isBackMode:"+isBackMode+" isUseAnimation:"+isUseAnimation);
        if(KEY_LAYOUT_BIND_EMAIL_STEP1.equals(mCurrnetStep)) {
            Log.i("yimin","register email");
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            Log.i("yimin","register others");
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
		ActivityUtils.closeKeyboard(this);
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
	///////////////////////////////////////////////点击事件处理begin
    //解绑手机输入验证码界面，点击x清除验证码
    public void doOnclickDeleteSmsVerifyCodeButton(View view){
        editUnbindSmsVerifyCode.setText("");
    }
    //绑定邮箱第1步，点击返回按钮  ////////////////////////////////////////////////////////////////////
	public void doOnclickBindEmailStep1BackButton(View view){
		Log.i(TAG, "doOnclickBindEmailStep1BackButton() before call finish()");
        ActivityUtils.closeKeyboard(BindOrUnbindActivity.this);
		finish();
	}
	//绑定邮箱第1步，点击删除绑定邮箱按钮
	public void doOnclickDeleteBindEmailButton(View view){
		editBindEmail.setText("");
	}
	//绑定邮箱第1步，点击绑定按钮
	public void doOnclickBindEmailButton(View view){
		Log.i(TAG, "doOnclickBindEmailButton()  ");
		if(ActivityUtils.isEmail(editBindEmail.getText().toString())){
			btnBindEmail.setEnabled(false); //线程执行前设置按钮disbale，线程执行完成设置可点
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					String email = editBindEmail.getText().toString().trim();
					mTableAccountInfo.clear();
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
					mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, email);
					mTableAccountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, Constants.EMAIL_STATUS_TO_BIND);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND);
					Log.i(TAG, "doOnclickBindEmailButton() email:"+email+" mGomeId:"+mGomeId);
		        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
				        		btnBindEmail.setEnabled(true); 
							}
						});

		        		Log.e(TAG, "doOnclickBindEmailButton() error mBindToActiveEmail:"+mBindToActiveEmail+" not valid from server!");
		        		return;
		        	}
					if(AccountUtils.bindFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){ //发送成功才执行下一步
						mHandler.post(new Runnable() { //ui线程处理
							@Override
							public void run() {
								mBindToActiveEmail = editBindEmail.getText().toString().trim();
								String strFormat = getResources().getString(R.string.txt_send_email_reminder_part1);
								String str = String.format(strFormat, mBindToActiveEmail);
								textBindEmailSendEmailReminder.setText(str);
								ActivityUtils.closeKeyboard(BindOrUnbindActivity.this);
								showStep(KEY_LAYOUT_BIND_EMAIL_STEP2);
							}
						});
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
			        		btnBindEmail.setEnabled(true); 
						}
					});
				}
			});

		}else{
			ActivityUtils.alert(BindOrUnbindActivity.this, getResources().getString(R.string.alert_input_valid_email));
		}
	}
	//绑定邮箱第2步，点击返回按钮
	public void doOnclickBindEmailStep2BackButton(View view){
        Log.i(TAG,"doOnclickBindEmailStep2BackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//绑定/解绑邮箱第2步，点击查收邮件按钮
    public void doOnclickCheckEmail(View view){
    	//ActivityUtils.verifyMail(this);
        String email = "";
        if(!editBindEmail.getText().toString().isEmpty()){
            email = editBindEmail.getText().toString().trim();
        }else{
            email = mEmailAddress;
        }
        Log.i(TAG,"doOnclickCheckEmail() email:"+email); 
        ActivityUtils.verifyMail(this,email);
    }
    //绑定 邮箱第2步，点击重新发送按钮
    public void doOnclickBindResendEmail(View view){
    	Log.e(TAG, "doOnclickBindResendEmail() ");
    	doResendEmail(true);
    }
    private void doResendEmail(final boolean isBind){
    	sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				if(isBind){
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mBindToActiveEmail);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND);
				}else{
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mUnbindToActiveEmail);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_UNBIND);
				}
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
		    	if(AccountUtils.sendEmailFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
		    		mHandler.post(new Runnable() {//ui线程处理
						@Override
						public void run() {
							if(isBind){
								mBindSendEmailTimeCount.start();
							}else{
								mUnbindSendEmailTimeCount.start();
							}
						}
					});
		    	}
			}
		});
    }
    //解绑邮箱第1步，点击返回按钮   ////////////////////////////////////////////////////////////////////
    public void doOnclickUnbindEmailStep1BackButton(View view){
    	finish();
    }
    //解绑邮箱第1步，点击解除绑定按钮
    public void doOnlcikUnbindEmailButton(View view){
    	showDialog(BindOrUnbindActivity.this,DIALOG_UNBIND_EMAIL);
    }
    //解绑邮箱第1步，弹窗点击确定按钮
    public void doOnclickUnbindEmailOkButton(View view){
    	Log.i(TAG,"doOnclickUnbindEmailOkButton() ");
    	btnAlertUnbindEmail.setEnabled(false);
    	sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);
				mTableAccountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, Constants.EMAIL_STATUS_TO_UNBIND);//本地使用
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_UNBIND);
				if(AccountUtils.unbindFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){  //调用解绑接口
					mHandler.post(new Runnable() {//ui线程处理
						@Override
						public void run() {
							mUnbindToActiveEmail = mEmailAddress;//设置待解绑待激活的邮箱
							
							String strFormat = getResources().getString(R.string.txt_send_email_reminder_part1);
							String str = String.format(strFormat, mEmailAddress);
							textUnbindEmailSendEmailReminder.setText(str);
							showStep(KEY_LAYOUT_UNBIND_EMAIL_STEP2);
							dismissDialog();
						}
					});

				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						btnAlertUnbindEmail.setEnabled(true);
					}
				});

			}
		});

    }
    //通用关闭弹出框按钮事件
    public void doOnclickDismissDialogButton(View view){
    	dismissDialog();
    }
    //解绑邮箱第2步，弹窗点击返回按钮
    public void doOnclickUnbindEmailStep2BackButton(View view){
        Log.i(TAG,"doOnclickUnbindEmailStep2BackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
    }
    //解绑邮箱第2步，点击重新发送邮件
    public void doOnclickUnbindResendEmail(View view){
    	Log.e(TAG, "doOnclickUnbindResendEmail() ");
    	doResendEmail(false);
    }
    
    
    //绑定手机第1步，点击返回按钮 ////////////////////////////////////////////////////////////////////
    public void doOnclickBindPhoneStep1BackButton(View view){
        ActivityUtils.closeKeyboard(BindOrUnbindActivity.this);
    	finish();
    }
    //绑定手机第1步，点击删除绑定手机号按钮 
    public void doOnclickDeleteBindPhoneButton(View view){
    	editBindPhone.setText("");
    }
    //绑定手机第1步，点击删除手机验证码按钮 
    public void doOnclickDeleteSmsVerifyCodeForBindPhoneButton(View view){
    	editBindSmsVerifyCode.setText("");
    }
	//绑定手机第1步，点击绑定按钮
	public void doOnclickBindPhoneButton(View view){
		Log.i(TAG, "doOnclickBindPhoneButton()");
		if(checkBindPhoneInput()){
			bindPhone();
		}
	}
	View.OnClickListener doOnclickBindSendSmsVerifyCodeOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Log.i(TAG,"doOnclikBindSendSmsVerifyCodeOnClickListener.onClick()");
            doOnclikBindSendSmsVerifyCode(arg0);
        }
    };
	//绑定手机第1步，点击发送短信验证码按钮
	public void doOnclikBindSendSmsVerifyCode(View view){
		if(!checkSendSmsVerifyCodeInput()){
			return;
		}
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editBindPhone.getText().toString());
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				textBindSmsVerifyCode.setClickable(false); //防止多次点击发送验证码
				if(AccountUtils.sendSmsVerifyCodeFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mBindSmsCodeTimeCount.start();
                            isBindSmsCodeTimeCounting = true;
						}
					});
				}else{
					Log.i(TAG, "doOnclikBindSendSmsVerifyCode() send sms fail ,reset clickable");
					textBindSmsVerifyCode.setClickable(true); //防止多次点击发送验证码
				}
			}
		});

	}
	void bindPhone(){
		btnBindPhone.setEnabled(false);
		sWorker.post(new Runnable() { //网络请求放在子线程处理
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SMS_CODE,editBindSmsVerifyCode.getText().toString().trim());
				
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editBindPhone.getText().toString().trim());
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_BIND);
				if(!AccountUtils.verifySmsCodeValidFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							btnBindPhone.setEnabled(true);
						}
					});

					Log.e(TAG, "bindPhone() verify smscode error!! ");
					return;
				}
				if(AccountUtils.bindFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {//ui线程处理
						@Override
						public void run() {
							ActivityUtils.closeKeyboard(BindOrUnbindActivity.this); //隐藏软键盘
							Intent data = new Intent();
							data.setAction(AccountInfoActivity.ACTION_BIND_PHONE_SUCCESS);
							setResult(RESULT_OK,data);
							finish();
							
						}
					});
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						btnBindPhone.setEnabled(true);
					}
				});  //线程执行完成设置成可点
			}
		});
	}
	//解绑手机第1步，点击返回按钮 ////////////////////////////////////////////////////////////////////
	public void doOnclickUnbindPhoneStep1BackButton(View view){
		finish();
	}
	//解绑手机第1步，点击解除绑定按钮
	public void doOnlcikUnbindPhoneStep1Button(View view){
		showDialog(this,DIALOG_UNBIND_PHONE);
	}
	//解绑手机第1步，弹出框点击确定
	public void doOnclickUnbindPhoneOkButton(View view){
		dismissDialog();
		textPhoneUnbindStep2.setText(ActivityUtils.convertPhoneNumShow(mPhoneNumber));
		showStep(KEY_LAYOUT_UNBIND_PHONE_STEP2);
	}
	//解绑手机第2步，点击返回按钮
	public void doOnclickUnbindPhoneStep2BackButton(View view){
        Log.i(TAG,"doOnclickUnbindPhoneStep2BackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//解绑手机第2步，点击发送短信验证码按钮
	public void doOnclikUnbindSendSmsVerifyCode(View view){

		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mPhoneNumber);
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_UNBIND);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				textUnbindSmsVerifyCode.setClickable(false);//防止多次点击发送验证码
				if(AccountUtils.sendSmsVerifyCodeFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mUnbindSmsCodeTimeCount.start();
						}
					});
				}else{
					Log.i(TAG, "doOnclikUnbindSendSmsVerifyCode() send sms fail ,reset clickable");
					textUnbindSmsVerifyCode.setClickable(true);//防止多次点击发送验证码
				}
			}
		});
	}
	//解绑手机第2步，点击确定按钮
	public void doOnlcikUnbindPhoneConpleteButton(View view){
//		mTableAccountInfo.clear();
//		mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
//		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editBindPhone.getText().toString().trim());
//		mTableAccountInfo.put(Constants.KEY_SMS_CODE,editBindSmsVerifyCode.getText().toString().trim());
		btnUnbindPhoneComplete.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);//本地使用
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER,mPhoneNumber);
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_UNBIND);
				mTableAccountInfo.put(Constants.KEY_SMS_CODE,editUnbindSmsVerifyCode.getText().toString().trim());//验证码接口使用
				//step1  先判断验证码是否正确	
				if(!AccountUtils.verifySmsCodeValidFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							btnUnbindPhoneComplete.setEnabled(true);
						}
					});

					Log.e(TAG, "doOnlcikUnbindPhoneConpleteButton() verify sms code error!!");
					return;
				}
				
				//step2 解绑失败返回，
				if(!AccountUtils.unbindFromServer(BindOrUnbindActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							btnUnbindPhoneComplete.setEnabled(true);
						}
					});
					Log.e(TAG, "doOnlcikUnbindPhoneConpleteButton() unbind security phone error!!");
					return;
				}
					
				//step3  解绑手机号成功
				mHandler.post(new Runnable() {						
					@Override
					public void run() {
						ActivityUtils.closeKeyboard(BindOrUnbindActivity.this); //隐藏软键盘
						Intent data = new Intent();
						data.setAction(AccountInfoActivity.ACTION_UNBIND_PHONE_SUCCESS);
						setResult(RESULT_OK,data);
						finish();
					}
				});
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						btnUnbindPhoneComplete.setEnabled(true);
					}
				});
			}
		});

	}
    ///////////////////////////////////////////////点击事件处理end
	boolean checkSendSmsVerifyCodeInput(){
		if(editBindPhone.getText().toString().isEmpty()){
			ActivityUtils.alert(BindOrUnbindActivity.this, getResources().getString(R.string.alert_text_phone_num_null));
		}else if(!ActivityUtils.isMobileNO(editBindPhone.getText().toString().trim())){
			ActivityUtils.alert(this, getResources().getString(R.string.alert_input_valid_phone));
		}else{
			return true;
		}
		return false;
	}
	boolean checkBindPhoneInput(){
		if(editBindPhone.getText().toString().isEmpty()){
			ActivityUtils.alert(BindOrUnbindActivity.this, getResources().getString(R.string.alert_text_phone_num_null));
		}else if(!ActivityUtils.isMobileNO(editBindPhone.getText().toString().trim())){
			ActivityUtils.alert(this, getResources().getString(R.string.alert_input_valid_phone));
		}else if(editBindSmsVerifyCode.getText().toString().isEmpty()){
			ActivityUtils.alert(BindOrUnbindActivity.this, getResources().getString(R.string.alert_text_sms_verify_code_null));
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
			case DIALOG_UNBIND_EMAIL:
				layout = inflater.inflate(R.layout.alert_unbind_email_layout,null);
				btnAlertUnbindEmail = (Button)layout.findViewById(R.id.btn_alert_unbind_email);
				break;
			case DIALOG_UNBIND_PHONE:
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
    void setBindSmsVerifyCodeEnable(boolean enable){
        Log.i(TAG,"setBindSmsVerifyCodeEnable enable:"+enable);
        if(enable){
            //验证码不置灰显示
            textBindSmsVerifyCode.setEnabled(true);
            textBindSmsVerifyCode.setTextColor(getResources().getColor(R.color.color_button_link));
            textBindSmsVerifyCode.setOnClickListener(doOnclickBindSendSmsVerifyCodeOnClickListener);
        }else{
            textBindSmsVerifyCode.setEnabled(false);
            textBindSmsVerifyCode.setTextColor(getResources().getColor(R.color.color_text_disable));
            textBindSmsVerifyCode.setOnClickListener(null);
        }
    }
    ////////////
	class BindSmsCodeTimeCount extends CountDownTimer {
		public BindSmsCodeTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			textBindSmsVerifyCode.setClickable(false);
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		textBindSmsVerifyCode.setText(msg); 

		}
		@Override
		public void onFinish() {
            Log.i(TAG,"BindSmsCodeTimeCount.onFinish()");
			textBindSmsVerifyCode.setText(getResources().getString(R.string.register_send_again));
			textBindSmsVerifyCode.setClickable(true);
            isBindSmsCodeTimeCounting = false;
            if(editBindPhone.getText().toString().isEmpty()){//手机号为空则发送验证码灰色显示
                setBindSmsVerifyCodeEnable(false);
            }else{//手机号不为空,则发送验证码高亮显示
                setBindSmsVerifyCodeEnable(true);
            }
		}
	}
	class UnbindSmsCodeTimeCount extends CountDownTimer {
		public UnbindSmsCodeTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			textUnbindSmsVerifyCode.setClickable(false);
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		textUnbindSmsVerifyCode.setText(msg); 

		}
		@Override
		public void onFinish() {
			textUnbindSmsVerifyCode.setText(getResources().getString(R.string.register_send_again));
			textUnbindSmsVerifyCode.setClickable(true);  
		}
	}
	class BindSendEmailTimeCount extends CountDownTimer {
		public BindSendEmailTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {

			textBindResendEmail.setClickable(false);
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		textBindResendEmail.setText(msg);
		}
		@Override
		public void onFinish() {
			textBindResendEmail.setText(getResources().getString(R.string.register_send_again));
			textBindResendEmail.setClickable(true);

		}
	}
	class UnbindSendEmailTimeCount extends CountDownTimer {
		public UnbindSendEmailTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {

			textUnbindResendEmail.setClickable(false);
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		textUnbindResendEmail.setText(msg);
		}
		@Override
		public void onFinish() {
			textUnbindResendEmail.setText(getResources().getString(R.string.register_send_again));
			textUnbindResendEmail.setClickable(true);

		}
	}
	
	boolean checkBindPhoneStep1InputAll(){
		if(editBindPhone.getText().toString().trim().isEmpty()){
			return false;
		}else if(editBindSmsVerifyCode.getText().toString().trim().isEmpty()){
			return false;
		}
		return true;
	}
	boolean checkUnbindPhoneStep2InputAll(){
		if(editUnbindSmsVerifyCode.getText().toString().trim().isEmpty()){
			return false;
		}
		return true;
	}
	@Override
	public void showAfterTextNotEmpty(int editType) {
		switch (editType) {
			case TYPE_EDIT_BIND_MAIL:
				imageDeleteBindEmail.setVisibility(View.VISIBLE);
				btnBindEmail.setEnabled(true);
				break;
			case TYPE_EDIT_BIND_PHONE:
				imageDeleteBindPhone.setVisibility(View.VISIBLE);
				if(checkBindPhoneStep1InputAll()){
					btnBindPhone.setEnabled(true);
				}
                //输入手机号不为空，验证码高亮显示
                setBindSmsVerifyCodeEnable(true);
				break;
			case TYPE_EDIT_BIND_SMS_CODE:
				imageDeleteSmsCodeForBindPhone.setVisibility(View.VISIBLE);
				if(checkBindPhoneStep1InputAll()){
					btnBindPhone.setEnabled(true);
				}
				break;
			case TYPE_EDIT_UNBIND_SMS_CODE:
				imageDeleteSmsCodeForUnbindPhone.setVisibility(View.VISIBLE);
				if(checkUnbindPhoneStep2InputAll()){
					btnUnbindPhoneComplete.setEnabled(true);
				}
				break;
			default:
				break;
		}
		
	}
	@Override
	public void showAfterTextEmpty(int editType) {
		switch (editType) {
			case TYPE_EDIT_BIND_MAIL:
				imageDeleteBindEmail.setVisibility(View.GONE);
				btnBindEmail.setEnabled(false);
				break;
			case TYPE_EDIT_BIND_PHONE:
				imageDeleteBindPhone.setVisibility(View.GONE);
				btnBindPhone.setEnabled(false);
                if(!isBindSmsCodeTimeCounting) { //非验证码倒计时状态,输入手机号为空,验证码置灰显示
                    setBindSmsVerifyCodeEnable(false);
                }
				break;
			case TYPE_EDIT_BIND_SMS_CODE:
				imageDeleteSmsCodeForBindPhone.setVisibility(View.GONE);
				btnBindPhone.setEnabled(false);
				break;
			case TYPE_EDIT_UNBIND_SMS_CODE:
				imageDeleteSmsCodeForUnbindPhone.setVisibility(View.GONE);
				btnUnbindPhoneComplete.setEnabled(false);
				break;
			default:
				break;
		}
		
	}
	
}
