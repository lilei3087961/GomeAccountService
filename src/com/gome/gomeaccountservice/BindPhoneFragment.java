package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.BitmapUtils;
import com.gome.gomeaccountservice.utils.EditChangedListener;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;
import com.gome.gomeaccountservice.utils.NetworkUtilities;

public class BindPhoneFragment extends Fragment {
	static final String TAG = Constants.TAG_PRE + "BindPhoneFragment";
	private EditText editPhoneNumber;
	private EditText verifyCode;
	private Button btnStep2PhoneComplete;
	private boolean isPhoneNumberCorrect = false;
	private boolean isVerifyCodeCorrect = false;
	private ImageView imageDeleteSmsVerifyCode;
	private ImageView imageDeleteAccountName;
	private String finalPhoneNumber;
	private ImageView backIcon;
	TextView textSendSmsCodeForStep2Phone;//手机注册发送验证码按钮
    boolean isBindSmsCodeTimeCounting = false; //是否绑定验证码正在倒计时
	private TimeCountForPhoneRegisterSmsCode timeSmsCodeForPhoneRegister = null;
	private View view = null;
	private static final HandlerThread sWorkerThread = new HandlerThread("RegisterActivity-loader");
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
	private static final Handler mHandler = new Handler();

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.bind_phone_fragment, null);
        editPhoneNumber = (EditText) view.findViewById(R.id.edit_phone_number);
		verifyCode = (EditText) view.findViewById(R.id.edit_sms_verify_code_for_setp2_phone);
		btnStep2PhoneComplete = (Button) view.findViewById(R.id.btn_step2_phone_complete);
		imageDeleteAccountName = (ImageView)view.findViewById(R.id.image_delete_account_name);
		imageDeleteSmsVerifyCode = (ImageView)view.findViewById(R.id.image_delete_sms_verify_code_for_step2);
		textSendSmsCodeForStep2Phone = (TextView)view.findViewById(R.id.text_send_sms_verify_code_for_step2_phone);
		backIcon = (ImageView)view.findViewById(R.id.head_left_arrow);
		timeSmsCodeForPhoneRegister = new TimeCountForPhoneRegisterSmsCode(60000, 1000);
		backIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ActivityUtils.closeKeyboard(getActivity());
                getActivity().finish();
            }
        });
		imageDeleteAccountName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editPhoneNumber.setText("");
			}
		});
        //for bug GMOS2X1-3271 by lilei begin
//		textSendSmsCodeForStep2Phone.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				doOnclikStep2PhoneSendSmsVerifyCode();
//			}
//		});
        if(editPhoneNumber.getText().toString().isEmpty()){//手机号为空则发送验证码灰色显示
            setBindSmsVerifyCodeEnable(false);
        }else{//手机号不为空,则发送验证码高亮显示
            setBindSmsVerifyCodeEnable(true);
        }
        isBindSmsCodeTimeCounting = false;
        //for bug GMOS2X1-3271 by lilei end

		btnStep2PhoneComplete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doOnlcikStep2PhoneCompleteButton();
			}
		});
		imageDeleteSmsVerifyCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				verifyCode.setText("");
			}
		});
		editPhoneNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.i(TAG,"phonenum:" + s);
				// TODO Auto-generated method stub
				if (s.length() > 0) {
					isPhoneNumberCorrect = true;
					imageDeleteAccountName.setVisibility(View.VISIBLE);
                    //add for bug GMOS2X1-3271 by lilei
                    setBindSmsVerifyCodeEnable(true);
				} else {
					isPhoneNumberCorrect = false;
					imageDeleteAccountName.setVisibility(View.INVISIBLE);
                    //add for bug GMOS2X1-3271 by lilei
                    if(!isBindSmsCodeTimeCounting) { //非验证码倒计时状态,输入手机号为空,验证码置灰显示
                        setBindSmsVerifyCodeEnable(false);
                    }
				}
				if (isInputCorrect()) {
					btnStep2PhoneComplete.setEnabled(true);
				} else {
					btnStep2PhoneComplete.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		verifyCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.i(TAG,"verifynum:" + s);
				// TODO Auto-generated method stub
				if (s.length() > 0) {
					isVerifyCodeCorrect = true;
					imageDeleteSmsVerifyCode.setVisibility(View.VISIBLE);
				} else {
					isVerifyCodeCorrect = false;
					imageDeleteSmsVerifyCode.setVisibility(View.INVISIBLE);
				}
				if (isInputCorrect()) {
					btnStep2PhoneComplete.setEnabled(true);
				} else {
					btnStep2PhoneComplete.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
        return view;
    }
    void setBindSmsVerifyCodeEnable(boolean enable){
        Log.i(TAG,"setBindSmsVerifyCodeEnable enable:"+enable);
        if(enable){
            //验证码不置灰显示
            textSendSmsCodeForStep2Phone.setEnabled(true);
            textSendSmsCodeForStep2Phone.setTextColor(getResources().getColor(R.color.color_button_link));
            textSendSmsCodeForStep2Phone.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doOnclikStep2PhoneSendSmsVerifyCode();
                }
            });
        }else{
            textSendSmsCodeForStep2Phone.setEnabled(false);
            textSendSmsCodeForStep2Phone.setTextColor(getResources().getColor(R.color.color_text_disable));
            textSendSmsCodeForStep2Phone.setOnClickListener(null);
        }
    }
    //modify for bugid GMOS-4006 for force close by yimin.zhu on 2017.8.24
    @Override  
    public void onPause() {  
        super.onPause();
        Log.d(TAG, "BindPhoneFragment pause");  
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timeSmsCodeForPhoneRegister.cancel();
        Log.d(TAG, "BindPhoneFragment onDetach()");
    }

    public boolean isInputCorrect() {
		if (isPhoneNumberCorrect && isVerifyCodeCorrect) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 手机注册 点击发送短信验证码按钮
	 * @param view
	 */
	public void doOnclikStep2PhoneSendSmsVerifyCode() {
		final String phoneNumber = editPhoneNumber.getText().toString().trim();
		if(ActivityUtils.isMobileNO(editPhoneNumber.getText().toString())){
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					HashMap<String,String> tableAccountInfo =new HashMap<String, String>();
					tableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, phoneNumber);//KEY_SERVER_REQUEST_SMS_MSG_TYPE
					tableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_REGISTER);
					tableAccountInfo.put(Constants.KEY_SERVER_TOKEN,null);
					textSendSmsCodeForStep2Phone.setClickable(false);//防止多次点击发送验证码
					if(AccountUtils.sendSmsVerifyCodeFromServer(getActivity(),tableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								timeSmsCodeForPhoneRegister.start();
                                isBindSmsCodeTimeCounting = true;//for bug GMOS2X1-3271 by lilei
							}
						});

					}else{
						Log.i(TAG, "doOnclikStep2PhoneSendSmsVerifyCode() send sms fail ,reset clickable");
						textSendSmsCodeForStep2Phone.setClickable(true); //防止多次点击发送验证码
					}
				}
			});
		} else {
			//for bug GM13C-737 by yimin.zhu
            if(editPhoneNumber.getText().toString().trim().isEmpty()){
                ActivityUtils.alert(getActivity(), getResources().getString(R.string.alert_text_phone_num_null));
            }else {
                ActivityUtils.alert(getActivity(), getResources().getString(R.string.alert_input_valid_phone));
            }
        }

	}

	public String getPhoneNumber() {
		return finalPhoneNumber;
	}


	/**
	 * 验证验证码的正确性
	 * @param view
	 */
	public void doOnlcikStep2PhoneCompleteButton() {
		final String phoneNumber = editPhoneNumber.getText().toString().trim();
		finalPhoneNumber = phoneNumber;
		if(ActivityUtils.isMobileNO(editPhoneNumber.getText().toString())){
			btnStep2PhoneComplete.setEnabled(false); 
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					HashMap<String,String> mTableAccountInfo =new HashMap<String, String>();
					mTableAccountInfo.put(Constants.KEY_SMS_CODE,verifyCode.getText().toString().trim());	
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editPhoneNumber.getText().toString().trim());
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_REGISTER);
					if(AccountUtils.verifySmsCodeValidFromServer(getActivity(),mTableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								Log.i(TAG, "correct mms code");
								btnStep2PhoneComplete.setEnabled(true); 
								//ActivityUtils.alert(getActivity(), getResources().getString(R.string.btn_complete));
								timeSmsCodeForPhoneRegister.cancel();
								((BindPhoneActivity)getActivity()).goWechatSecretFragment();
							}
						});

					}else{
						Log.i(TAG, "wrong mms code");
						ActivityUtils.alert(getActivity(), getResources().getString(R.string.txt_set_network_cancel));
					}
				}
			});
		}
	}

	class TimeCountForPhoneRegisterSmsCode extends CountDownTimer {

		public TimeCountForPhoneRegisterSmsCode(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {

			textSendSmsCodeForStep2Phone.setEnabled(false);
			textSendSmsCodeForStep2Phone.setClickable(false);
			String strFormat = getResources().getString(R.string.register_code_send_again);
			String str = String.format(strFormat, millisUntilFinished/1000);
			textSendSmsCodeForStep2Phone.setText(str);
		}

		@Override
		public void onFinish() {
			textSendSmsCodeForStep2Phone.setEnabled(true);
			textSendSmsCodeForStep2Phone.setClickable(true);
			textSendSmsCodeForStep2Phone.setText(getResources().getString(R.string.register_send_again));
            //for bug GMOS2X1-3271 by lilei begin
            if(editPhoneNumber.getText().toString().isEmpty()){//手机号为空则发送验证码灰色显示
                setBindSmsVerifyCodeEnable(false);
            }else{//手机号不为空,则发送验证码高亮显示
                setBindSmsVerifyCodeEnable(true);
            }
            isBindSmsCodeTimeCounting = false;
            //for bug GMOS2X1-3271 by lilei end
		}
	}

}
