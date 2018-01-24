package com.gome.gomeaccountservice;

import java.util.HashMap;

import com.gome.gomeaccountservice.RegisterActivity.TimeCountForMailRegisterSendEmail;
import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import gome.app.GomeAlertDialog;

public class CheckEmailActiveActivity extends Activity {
	static final String TAG = Constants.TAG_PRE+"CheckEmailActiveActivity";
	Dialog mAlertDialog;
	TextView textSendEmail; //邮箱注册发送邮件text_step2_mail_resend_mail
	TextView textSendEmailReminder; //text_setp2_send_email_remiander
	private TimeCountForMailRegisterSendEmail timeSendEmailForMailRegister = null;
    private String mVerifyEmailAddress = null; //需激活的邮箱地址
	HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	private static final HandlerThread sWorkerThread = new HandlerThread(
			"CheckEmailActiveActivity-loader");
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(
			sWorkerThread.getLooper());
	private static final Handler mHandler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_step2_mail_layout);
		initView();
		parseAction();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "onResume() mVerifyEmailAddress:"+mVerifyEmailAddress);
        verifyEmailActived(false);
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
                mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mVerifyEmailAddress);
                if(AccountUtils.verifyEmailStatusFromServer(CheckEmailActiveActivity.this, mTableAccountInfo,false)){  //从服务器判断邮箱是否激活
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(CheckEmailActiveActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
                }else{
                    if(isFromClick){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ActivityUtils.alert(CheckEmailActiveActivity.this,getResources().getString(R.string.txt_gome_account_not_verified));
                            }
                        });
                    }
                }
                }
            });

        }
    }
	void initView(){
		textSendEmail = (TextView)findViewById(R.id.text_step2_mail_resend_mail);
		timeSendEmailForMailRegister = new TimeCountForMailRegisterSendEmail(60000, 1000);
		textSendEmailReminder = (TextView)findViewById(R.id.text_setp2_send_email_remiander);
	}
	void parseAction(){
		String action = getIntent().getAction();
		mVerifyEmailAddress = getIntent().getStringExtra(Constants.KEY_ACCOUNT_EMAIL);
		Log.i(TAG,"parseAction() action:"+action+" mVerifyEmailAddress:"+mVerifyEmailAddress);
		//更新新界面提示
		String strFormat = getResources().getString(R.string.txt_send_email_reminder_part1);
		String str = String.format(strFormat, mVerifyEmailAddress);
		textSendEmailReminder.setText(str);
	}
	/**
	 * 点击返回按钮
	 * @param view
	 */
	public void doOnclickStep2MailBackButton(View view){
		finish();
	}
    /**
     * 点击查收邮件按钮
     * @param view
     */
    public void doOnclickCheckEmail(View view){
    	//ActivityUtils.verifyMail(this);
    	ActivityUtils.verifyMail(this,mVerifyEmailAddress);
    }
    /**
	 * 点击以后验证
	 * @param view
	 */
	public void doVerifyLater(View view){
		Log.i(TAG, "doVerifyLater() ");
		mAlertDialog = new GomeAlertDialog.Builder(this)
		.setTitle(getResources().getString(R.string.txt_verify_later_alert_txt))
		.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override   
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub  
            	Log.i(TAG, "doVerifyLater.setPositiveButton.onClick dissmiss");
            	Intent intent = new Intent(CheckEmailActiveActivity.this,LoginActivity.class);  
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
     * 点击已验证按钮
     * @param view
     */
    public void doAlreadyVerified(View view){
        Log.i(TAG,"doAlreadyVerified()  call onResume()");
        verifyEmailActived(true);
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
				
				if(AccountUtils.sendEmailFromServer(CheckEmailActiveActivity.this, mTableAccountInfo)){
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
    
	class TimeCountForMailRegisterSendEmail extends CountDownTimer {

		public TimeCountForMailRegisterSendEmail(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {

			textSendEmail.setEnabled(false);
			textSendEmail.setClickable(false);
			String strFormat = getResources().getString(R.string.register_code_send_again);
			String str = String.format(strFormat, millisUntilFinished/1000);
			textSendEmail.setText(str);
		}
		@Override
		public void onFinish() {
			textSendEmail.setEnabled(true);
			textSendEmail.setClickable(true);
			textSendEmail.setText(getResources().getString(R.string.register_send_again));
		}
	}
}
