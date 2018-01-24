package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;

import java.util.HashMap;

public class CheckPasswordActivity extends Activity implements TextChangeCallback {
	static final String TAG = Constants.TAG_PRE+"CheckPasswordActivity";

	private String activityFrom;

	private Button btnCheckPwdNext;
	private EditText editInputPwd; 
	private ImageView imageDeleteInputPwd;
	private ImageView imageHidePasswordForPassword;
	private ImageView imageShowPasswordForPassword;
    private String mToken = null;

    private final int REQUEST_CODE_BIND_OR_UNBIND_EMAIL  = 4;
	private final int REQUEST_CODE_BIND_OR_UNBIND_PHONE  = 5;
	private final int REQUEST_CODE_PASSWORD_SECURITY  = 6;
	private static final String ACCOUNT_INFO_ACTIVITY_BIND_PHONE = "AccountInfoActivity-BindPhone";
	private static final String ACCOUNT_INFO_ACTIVITY_BIND_EMAIL = "AccountInfoActivity-BindEmail";

	HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();

	private final int TYPE_EDIT_INPUT_PWD = 1;
	private static final HandlerThread sWorkerThread = new HandlerThread("CheckPasswordActivity-loader");
	private static final String PASSWORD_SECURUTY_ACTIVITY = "PasswordSecurityActivity";
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
		setContentView(R.layout.public_check_pwd);
		activityFrom = getIntent().getStringExtra("activityName");
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
		Log.e(TAG, "CheckPasswordActivity-onActivityResult() resultCode:"+resultCode+" requestCode:"+requestCode);
		switch (requestCode) {
			case REQUEST_CODE_BIND_OR_UNBIND_EMAIL:
			case REQUEST_CODE_BIND_OR_UNBIND_PHONE:
			case REQUEST_CODE_PASSWORD_SECURITY:
				setResult(resultCode,data);
				finish();
				break;
			default:
				break;
		}		
	}

	public void initView() {
		editInputPwd = (EditText)findViewById(R.id.edit_input_password);
		editInputPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD));
        editInputPwd.setTypeface(Typeface.DEFAULT);
		imageDeleteInputPwd = (ImageView)findViewById(R.id.image_delete_input_password);
		btnCheckPwdNext = (Button)findViewById(R.id.btn_public_check_pwd_next);
		imageHidePasswordForPassword = (ImageView)findViewById(R.id.image_hide_password_for_password);
		imageShowPasswordForPassword = (ImageView)findViewById(R.id.image_show_password_for_password);
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
			case TYPE_EDIT_INPUT_PWD:
				imageDeleteInputPwd.setVisibility(View.VISIBLE);
				if(checkPwdInputFull()){
					btnCheckPwdNext.setEnabled(true);
				}
				break;
			default:
				break;
		}
	}

	//输入框内容为空
	public void showAfterTextEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_INPUT_PWD:
				imageDeleteInputPwd.setVisibility(View.GONE);
				btnCheckPwdNext.setEnabled(false);
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
	
	public void doOnlcikPublicCheckPwdNextButton(View view) {
		verifyPwdFromServer();
	}

	/**
	 * 验证密码是否正确，若正确弹出注销弹出框
	 */
	void verifyPwdFromServer() {
		String pwd = editInputPwd.getText().toString().trim();
		Log.i(TAG, "verifyPwdFromServer() mToken"+mToken+" pwd:"+pwd);
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, pwd);
        btnCheckPwdNext.setEnabled(false); //add for bug GM12E-1728 by lilei
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(AccountUtils.verifyPwdOrSmsCodeFromServer(CheckPasswordActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							//校验密码成功，根据不同启动Activity跳转不同界面
							if(PASSWORD_SECURUTY_ACTIVITY.equals(activityFrom)) {
								Intent i = new Intent(CheckPasswordActivity.this,PasswordSecurityActivity.class);
								i.putExtra("activityName", "CheckPasswordActivity");
								i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								//startActivity(i);
								//finish();
								startActivityForResult(i,REQUEST_CODE_PASSWORD_SECURITY);
							}
							if(ACCOUNT_INFO_ACTIVITY_BIND_PHONE.equals(activityFrom)) {
								//进入绑定手机页面
								Intent i = new Intent(CheckPasswordActivity.this,BindOrUnbindActivity.class);
								i.setAction(BindOrUnbindActivity.ACTION_BIND_PHONE);
								startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_PHONE);
							}
							if(ACCOUNT_INFO_ACTIVITY_BIND_EMAIL.equals(activityFrom)) {
								//进入绑定Email页面
								Intent i = new Intent(CheckPasswordActivity.this,BindOrUnbindActivity.class);
								i.setAction(BindOrUnbindActivity.ACTION_BIND_EMAIL);
								startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_EMAIL);
							}
						}
					});
				}
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        btnCheckPwdNext.setEnabled(true); //add for bug GM12E-1728 by lilei
                    }
                });
			}
		});

	}
	
	///////////////////点击事件处理begin
	//设置密码界面点击返回
	public void doOnclickPublicCheckPwdButton(View view){
		ActivityUtils.closeKeyboard(this);
		finish();
	}
	
	public void doOnclickDeleteInputPwdButton(View view){
		editInputPwd.setText("");
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

	public void getAccountInfo() {
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(this); //获取本地账号
    	if(null != gomeAccount){
			HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(this,gomeAccount.name); 
			mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
    	}
    }
}
