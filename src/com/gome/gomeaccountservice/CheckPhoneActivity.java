package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;

import java.util.HashMap;

public class CheckPhoneActivity extends Activity implements TextChangeCallback {
	static final String TAG = Constants.TAG_PRE+"CheckPhoneActivity";
	private TextView textPhoneNumber;
	private ImageView imageDeleteInputPwd;
	private EditText editInputPwd;//短信验证码
	private Button btnCheckPwdNext;
	private TextView textUnbindSmsVerifyCode;
	private String securityPhoneNumber = null;
	private String mGomeId = null;
	private String mToken = null;
	private String activityFrom;
	private UnbindSmsCodeTimeCount mUnbindSmsCodeTimeCount = null;
	private final int TYPE_EDIT_SMS_CODE = 1;
	private HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	private static final String ACCOUNT_INFO_ACTIVITY_BIND_PHONE = "AccountInfoActivity-BindPhone";
	private static final String ACCOUNT_INFO_ACTIVITY_BIND_EMAIL = "AccountInfoActivity-BindEmail";
	private static final HandlerThread sWorkerThread = new HandlerThread("CheckPhoneActivity-loader");
	private final int REQUEST_CODE_BIND_OR_UNBIND_EMAIL  = 4;
	private final int REQUEST_CODE_BIND_OR_UNBIND_PHONE  = 5;
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
	private static final Handler mHandler =  new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && this.isInMultiWindowMode()){
            Log.i(TAG, "onCreate() isInMultiWindowMode finish()");
            ActivityUtils.alert(this,getResources().getString(R.string.alert_gomeaccount_no_support_multi_window_mode));
            finish();
        }
		setContentView(R.layout.unbind_phone_step2_layout);
		getAccountInfo();
		initView();
	}
	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy()");
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume() 111");
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "CheckPhoneActivity-onActivityResult() resultCode:"+resultCode+" requestCode:"+requestCode);
		switch (requestCode) {
			case REQUEST_CODE_BIND_OR_UNBIND_EMAIL:
			case REQUEST_CODE_BIND_OR_UNBIND_PHONE:
				setResult(resultCode,data);
				finish();
				break;
			default:
				break;
		}		
	}
	public void initView() {
		activityFrom = getIntent().getStringExtra("activityName");
		textPhoneNumber = (TextView)findViewById(R.id.text_phone_unbind_step2);
		imageDeleteInputPwd = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_unbind_phone);
		btnCheckPwdNext = (Button)findViewById(R.id.btn_unbind_phone_complete);
		editInputPwd = (EditText)findViewById(R.id.edit_unbind_sms_verify_code);
		mUnbindSmsCodeTimeCount = new UnbindSmsCodeTimeCount(60000, 1000);
		textUnbindSmsVerifyCode = (TextView)findViewById(R.id.text_unbind_send_sms_verify_code);
		editInputPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE));
		securityPhoneNumber = AccountUtils.getSaftyNumber(this,mGomeId);
		textPhoneNumber.setText(ActivityUtils.convertPhoneNumShow(securityPhoneNumber));
	}

	public void getAccountInfo() {
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(this); //获取本地账号
    	if(null != gomeAccount){
			HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(this,gomeAccount.name); 
			mGomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
			mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	}
    }

    	//设置输入框输入监听
	class EditChangedListener implements TextWatcher{
		private int mEditType = -1;
		public EditChangedListener(int editType){
			mEditType = editType;
		}
		@Override
		public void afterTextChanged(Editable s) {
			Log.e(TAG, "afterTextChanged() 00 mEditType:"+mEditType+" s.toString():"+s.toString());
			if(s.toString().length()>0){
				showAfterTextNotEmpty(mEditType);
			}else{
				showAfterTextEmpty(mEditType);
			}
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			Log.i(TAG, "beforeTextChanged() mEditType:"+mEditType+" s:"+s);
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			Log.i(TAG, "onTextChanged() mEditType:"+mEditType+" s:"+s);
			
		}
		
	}

	//输入框内容非空
	public void showAfterTextNotEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_SMS_CODE:
				if(checkPwdInputFull()){
					btnCheckPwdNext.setEnabled(true);
					imageDeleteInputPwd.setVisibility(View.VISIBLE);
				}
				break;
			default:
				break;
		}
	}

	//输入框内容为空
	public void showAfterTextEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_SMS_CODE:
				btnCheckPwdNext.setEnabled(false);
				imageDeleteInputPwd.setVisibility(View.GONE);
				break;
			default:
				break;
		}
	}

	public boolean checkPwdInputFull() {
    	if(editInputPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}
    	return true;
    }

    //解绑手机第2步，点击返回按钮
	public void doOnclickUnbindPhoneStep2BackButton(View view){
		ActivityUtils.closeKeyboard(this);
		finish();
	}

	//点击删除短信验证码按钮
	public void doOnclickDeleteSmsVerifyCodeButton(View view){
		editInputPwd.setText("");
	}

	//验证安全手机号码
	public void doOnlcikUnbindPhoneConpleteButton(View view){
		btnCheckPwdNext.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);//本地使用
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_SECURITY_PHONE_NUMBER,securityPhoneNumber);
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_BIND_PRE_VERIFY);
				mTableAccountInfo.put(Constants.KEY_SMS_CODE,editInputPwd.getText().toString().trim());//验证码接口使用
				//step1  先判断验证码是否正确	
				if(AccountUtils.verifySmsCodeValidFromServer(CheckPhoneActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							btnCheckPwdNext.setEnabled(true);
							if(ACCOUNT_INFO_ACTIVITY_BIND_PHONE.equals(activityFrom)) {
								//进入绑定手机页面
								Intent i = new Intent(CheckPhoneActivity.this,BindOrUnbindActivity.class);
								i.setAction(BindOrUnbindActivity.ACTION_BIND_PHONE);
								startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_PHONE);
							}
							if(ACCOUNT_INFO_ACTIVITY_BIND_EMAIL.equals(activityFrom)) {
								//进入绑定Email页面
								Intent i = new Intent(CheckPhoneActivity.this,BindOrUnbindActivity.class);
								i.setAction(BindOrUnbindActivity.ACTION_BIND_EMAIL);
								startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_EMAIL);
							}
						}
					});
				}									
			}
		});
	}

	//点击发送短信验证码按钮
	public void doOnclikUnbindSendSmsVerifyCode(View view){

		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, securityPhoneNumber);
				Log.i(TAG,"security number:" + securityPhoneNumber);
				//modify by yimin.zhu for bugid 4705 on 2017.8.28
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND_PRE_VERIFY);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				textUnbindSmsVerifyCode.setClickable(false);//防止多次点击发送验证码
				if(AccountUtils.sendSmsVerifyCodeFromServer(CheckPhoneActivity.this, mTableAccountInfo)){
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
}
