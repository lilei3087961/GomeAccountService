package com.gome.gomeaccountservice.controls;

import java.util.HashMap;

import com.gome.gomeaccountservice.Constants;
import com.gome.gomeaccountservice.RetrievePwdActivity;
import com.gome.gomeaccountservice.controls.GomeCommonEditText.EditChangedListener;
import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;

import android.accounts.Account;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gome.gomeaccountservice.R;

public class GomeSmsCodeEditText extends LinearLayout {
	static final String TAG = Constants.TAG_PRE+"GomeSmsCodeEditText";
	EditText mEditText;
	
	ImageView mImageDeleteEdit;
	TextView mTextSendSmsCode;
	String mUserName; 				//手机号或邮箱
	String mSendSmsCodeOperateType; //发送短信验证码类型
    String mToken = null;
    private SmsCodeTimeCount mSmsCodeTimeCount = null;
    private static final HandlerThread sWorkerThread = new HandlerThread("GomeSmsCodeEditText-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    Handler mHandler =new Handler();
    
	public GomeSmsCodeEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAccountInfo();
		LayoutInflater.from(context).inflate(R.layout.gome_sms_code_edittext_layout, this, true);
		Log.i(TAG, "GomeSmsCodeEditText()  00 this:"+this+"this.getParent():"+this.getParent());
		mEditText = (EditText)findViewById(R.id.edit);
		mEditText.addTextChangedListener(new EditChangedListener());
		mImageDeleteEdit = (ImageView)findViewById(R.id.image_delete_edit);
		mImageDeleteEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.setText("");
			}
		});
		mTextSendSmsCode = (TextView)findViewById(R.id.text_send_sms_verify_code);
		mTextSendSmsCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doOnclikSendSmsVerifyCode(v);
			}
		});
		mSmsCodeTimeCount = new SmsCodeTimeCount(60000, 1000);
	}
	public void getAccountInfo() {
		Account gomeAccount = AccountUtils.getLatestGomeAccount(getContext()); //获取本地账号
		if(null != gomeAccount){
			HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(getContext(),gomeAccount.name); 
			mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
			Log.i(TAG, "getAccountInfo() mToken:"+mToken);
		}
	}
	/**
	 * 点击发送短信验证码按钮
	 * @param view
	 */
	public void doOnclikSendSmsVerifyCode(View view){
		
		Log.i(TAG, "doOnclikSendSmsVerifyCode() mUserName:"+mUserName+" mSendSmsCodeOperateType:"+mSendSmsCodeOperateType+" mToken:"+mToken);
		if(ActivityUtils.isMobileNO(mUserName) || ActivityUtils.isEmail(mUserName)){
			final HashMap<String,String> tableAccountInfo =new HashMap<String, String>();

			sWorker.post(new Runnable() {
				@Override
				public void run() {
					mTextSendSmsCode.setClickable(false);//防止多次点击发送验证码
					if(ActivityUtils.isMobileNO(mUserName)){ //发送短信验证码
						tableAccountInfo.clear();
						tableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, mUserName);//KEY_SERVER_REQUEST_SMS_MSG_TYPE
						tableAccountInfo.put(Constants.KEY_OPERATE_TYPE, mSendSmsCodeOperateType);
						tableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
						if(AccountUtils.sendSmsVerifyCodeFromServer(getContext(),tableAccountInfo)){
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									mSmsCodeTimeCount.start();
								}
							});
						}else{
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									mTextSendSmsCode.setClickable(true);//防止多次点击发送验证码
								}
							});
						}
					}else{ //发送邮箱验证码
						tableAccountInfo.clear();
						tableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mUserName);//KEY_SERVER_REQUEST_SMS_MSG_TYPE
						tableAccountInfo.put(Constants.KEY_OPERATE_TYPE, mSendSmsCodeOperateType);
						if(AccountUtils.sendEmailFromServer(getContext(),tableAccountInfo)){
							mSmsCodeTimeCount.start();
						}else{
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									mTextSendSmsCode.setClickable(true);//防止多次点击发送验证码
								}
							});
						}
					}

				}
			});
		}
	}
	/**
	 * 设置用户名和发送验证码类型参数到本地
	 * @param userName
	 * @param operateType
	 */
	public void setSendVerifyCodeInfo(String userName,String operateType){
		 Log.i(TAG, "setSendVerifyCodeInfo() userName:"+userName+" operateType:"+operateType);
		 mUserName = userName;
		 mSendSmsCodeOperateType = operateType;
	}
	/**
	 * 获取当前自定义控件的 输入框
	 * @return
	 */
	public EditText getEditText(){
		return mEditText;
	}
	/**
	 * 点击删除输入的问题，自定义控件无法调用到
	 * @param view
	 */
	@Deprecated
	public void doOnclickDeleteEdit(View view){
		Log.i(TAG, "doOnclickDeleteEdit() ");
		mEditText.setText("");
	}
	class EditChangedListener implements TextWatcher{
		@Override
		public void afterTextChanged(Editable arg0) {
			Log.e(TAG, "afterTextChanged() 00  arg0.toString():"+arg0.toString());
			if(arg0.toString().length()>0){
				mImageDeleteEdit.setVisibility(View.VISIBLE);
			}else{
				mImageDeleteEdit.setVisibility(View.GONE);
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
			//Log.i(TAG, "onTextChanged() mEditType:"+mEditType+" s:"+s);
		}
	}
	class SmsCodeTimeCount extends CountDownTimer {
		public SmsCodeTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			String msgFormat = getResources().getString(R.string.btn_text_sending_sms_verify_code);
    		final String msg =  String.format(msgFormat, millisUntilFinished / 1000);
    		mTextSendSmsCode.setClickable(false);
    		mTextSendSmsCode.setText(msg);
		}
		@Override
		public void onFinish() {
			mTextSendSmsCode.setText(getResources().getString(R.string.register_send_again));
			mTextSendSmsCode.setClickable(true);
		}
	}
}
