package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.BitmapUtils;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import gome.app.GomeAlertDialog;
import gome.app.GomeProgressDialog;

public class AccountInfoActivity extends Activity implements TextChangeCallback {
	static final String TAG = Constants.TAG_PRE+"AccountInfoActivity";
    private static final String PHOTO_DATE_FORMAT = "'IMG'_yyyyMMdd_HHmmss";
    private Uri mTempPhotoUri;
    Animation mAnimationOpenEnter;
    Animation mAnimationOpenExit;
    Animation mAnimationCloseEnter;
    Animation mAnimationCloseExit;

	public static String ACTION_REGISTER_SUCCESS = "action_register_success";
	public static String ACTION_BIND_EMAIL_SUCCESS = "action_bind_email_success";
	public static String ACTION_BIND_PHONE_SUCCESS = "action_bind_phone_success";
	public static String ACTION_UNBIND_EMAIL_SUCCESS = "action_unbind_email_success";
	public static String ACTION_UNBIND_PHONE_SUCCESS = "action_unbind_phone_success";
	public static String ACTION_BIND_SECURITY_PHONE_SUCCESS = "action_bind_security_phone_success";
	public static String ACTION_UNBIND_SECURITY_PHONE_SUCCESS = "action_unbind_security_phone_success";
	
	GomeAlertDialog mDialog;
//	AlertDialog mRegisterSuccess1Dialog;
//	AlertDialog mRegisterSuccess2Dialog;
//	AlertDialog mSetAvatarChooserDialog;
//	AlertDialog mLoginOutDialog;
	final int TYPE_EDIT_NICK_NAME = 1;
	final int TYPE_EDIT_BIND_PHONE_NUMBER = 2;
	final int TYPE_EDIT_SMS_CODE = 3;
	final int TYPE_EDIT_BIND_EMAIL = 4;
    
	EditText editNickName;
	EditText editBindEmail;
	EditText editBindPhone;
	EditText editBindPhoneSmsCode;
	ImageView imageSetAvatar;  //设置头像界面图片
	ImageView imageAvatar;   //显示头像界面图片
	ImageView imageDeleteNickName; //图片 删除输入昵称
	ImageView imageDeletePhoneNumber;
	ImageView imageDeleteSmsVerifyCode;
	ImageView imageDeleteEmail;
	
    Button btnLoginOut;
	Button btnStep5Next;
    Button btnStep6Next;
	Button btnStep7Complete;
	Button btnStep7Email;
	Button btnCheckPwdNext;

	TextView textSendSmsCodeForStep7Mail; //邮箱注册发送验证码按钮
	TextView textStep8SendEmailReminder;
	TextView textRegisterSuccess1Prompt; //
	TextView textNickName;  //账号主界面显示昵称
	TextView textGomeId;  //账号主界面显示账号
	TextView textBindPhone;  //账号主界面显示绑定手机号
	TextView textBindEmail;  //账号主界面显示绑定邮箱
	TextView textResendEmail;  //邮箱验证界面点击重新发送邮件
	EditText editInputPwd; //退出账号输入密码
	ImageView imageDeleteInputPwd;
	ImageView imageHidePasswordForPassword;
	ImageView imageShowPasswordForPassword;
	private TimeCountForMailRegisterSmsCode timeSmsCodeForMailRegister = null;
	private TimeCountForPhoneRegisterSendEmail timeSmsCodeForPhoneRegister = null;
	HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
	
    private String mNickName = null;
    private String mEmailAddress = null;
    private String mPhoneNumber = null;
    private String mServerAvatarPath = null;
    private String mLocalAvatarPath = null;
    private String mGomeId = null;
    private String mToken = null;
    private String mEmailActiveStatus = null;
    private String mRegBindEmailAddress = null; //需绑定的邮箱地址，只处理注册时候绑定的邮箱
//	private String mUnbindToActiveEmail = null; //解绑待激活的邮箱地址
    
	private Bitmap mBitmap = null;  //头像的bitmap
	static final int COMPRESS_THREADHOD = 1024*1024;
	String mRegisterType = null;
	private String mImageType = "png"; //默认png
	final int REQUEST_CODE_PICK_IMAGE = 1;
	final int REQUEST_CODE_CAPTURE_CAMEIA  = 2;
	final int REQUEST_CODE_PERSONAL_INFO  = 3;
	final int REQUEST_CODE_BIND_OR_UNBIND_EMAIL  = 4;
	final int REQUEST_CODE_BIND_OR_UNBIND_PHONE  = 5;
	final int REQUEST_CODE_PASSWORD_SECURITY  = 6;
	
	HashMap<String, LinearLayout> mLayouts;
	LinearLayout layoutActivityInfoMain;
	LinearLayout registerLayoutSetp5Public;
	LinearLayout registerLayoutSetp6Public;
	LinearLayout registerLayoutSetp7Mail;
	LinearLayout registerLayoutSetp7Phone;
	LinearLayout registerLayoutSetp8Phone;
	LinearLayout layoutCheckPwd;

	final String KEY_LAYOUT_ACTIVITY_INFO_MAIN = "activity_info_main";
	final String KEY_LAYOUT_STEP5_PUBLIC = "step5Public";
	final String KEY_LAYOUT_STEP6_PUBLIC = "step6Public";
	final String KEY_LAYOUT_STEP7_MAIL = "step7Mail";
	final String KEY_LAYOUT_STEP7_PHONE = "step7Phone";
	final String KEY_LAYOUT_STEP8_PHONE = "step8Phone";
	final String KEY_LAYOUT_CHECK_PWD = "layoutCheckPwd";

	String mCurrnetStep = KEY_LAYOUT_ACTIVITY_INFO_MAIN;

	final int DIALOG_REGISTER_SUCCESS1 =1;
	//final int DIALOG_REGISTER_SUCCESS2 =2;
	final int DIALOG_SET_AVATAR =3;
	final int DIALOG_LOGIN_OUT =4;
	final int DIALOG_BIND_EMAIL_SUCCESS =5;
	final int DIALOG_BIND_PHONE_SUCCESS =6;
	final int DIALOG_UNBIND_EMAIL_SUCCESS =7;
	final int DIALOG_UNBIND_PHONE_SUCCESS =8;
	final int DIALOG_UNBIND_SECURITY_PHONE_SUCCESS =9;
	final int TYPE_EDIT_INPUT_PWD = 10;
	
	GomeProgressDialog mProgressDialog = null;

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
		//setTheme(com.gome.R.style.Theme_GOME_Light);
		setContentView(R.layout.account_info_activity);
		getAccountInfo();
		initView();
		showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN);
		parseAction();

	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDestroy()");
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume() 111");
		// TODO Auto-generated method stub
		//getAccountInfo();//获取本地账号信息，解决点击返回状态未更新的问题
		refreshView();//更新本地数据，刷新当前界面
		String action = getIntent().getAction();
		Log.i(TAG, "onResume() action:"+action+" mEmailAddress:"+mEmailAddress+" mEmailActiveStatus:"+mEmailActiveStatus
				+" mRegBindEmailAddress:"+mRegBindEmailAddress);
		super.onResume();
		if(!AccountUtils.getLoginState(AccountInfoActivity.this)){
			Log.i(TAG, "onResume() not login finish()");
			finish();
			return;
		}
		verifyEmailBindOrUnbind(false);
		
	}
	/**
	 * 检查邮箱绑定解绑状态
	 */
	void verifyEmailBindOrUnbind(boolean isFromClick){
		if(null != mRegBindEmailAddress){//注册时判断绑定的邮箱是否激活，若激活则显示主界面(此时已经登录过了)
			verifyBindEmail(mRegBindEmailAddress,true,isFromClick);

		}else{ //非注册模式
			if(Constants.EMAIL_STATUS_TO_BIND.equals(mEmailActiveStatus) && null != mEmailAddress){  //存在待解绑邮箱
				verifyBindEmail(mEmailAddress,false,isFromClick);
	    	}
		}
		if(Constants.EMAIL_STATUS_TO_UNBIND.equals(mEmailActiveStatus) && null != mEmailAddress){ //若解绑待激活邮箱存在其本地是待激活状态，则判断是否激活
			verifyUnbindEmail(isFromClick);
		}
	}
    /**
     * 点击已验证按钮
     * @param view
     */
    public void doAlreadyVerified(View view) {
        Log.i(TAG, "doAlreadyVerified() ");
        verifyEmailBindOrUnbind(true);
    }
	void verifyBindEmail(final String email,final boolean isRegiter,final boolean isFromClick){
		Log.i(TAG, "verifyBindEmail() email:"+email+" isRegiter:"+isRegiter+" isFromClick:"+isFromClick);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, email);
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_BIND);
				if(AccountUtils.verifyEmailStatusFromServer(AccountInfoActivity.this, mTableAccountInfo,true)){  //从服务器判断邮箱是否激活
					mHandler.post(new Runnable() { //ui线程
						@Override
						public void run() {//注册绑定提示
							if(isRegiter){ //注册模式弹出提示
								showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN,true);
								//showDialog(AccountInfoActivity.this,DIALOG_REGISTER_SUCCESS2);
								showRegisterSuccess2Toast();
								mRegBindEmailAddress = null; //重置
							}else{      //普通绑定成功提示
								ActivityUtils.alert(AccountInfoActivity.this, getResources().getString(R.string.txt_bind_mail_success));
							}
							refreshView();
						}
					});
				}else{
                    if(isFromClick){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ActivityUtils.alert(AccountInfoActivity.this,getResources().getString(R.string.txt_gome_account_not_verified));
                            }
                        });
                    }
                }
				
			}
		});
	}
	/**
	 * 刷新账户界面显示
	 */
	void refreshView(){
		Log.i(TAG, "refreshView()");
		getAccountInfo(); //更新数据
		updateView();
	}
	void verifyUnbindEmail(final boolean isFromClick){
		Log.i(TAG, "verifyUnbindEmail() isFromClick:"+isFromClick);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN,mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mEmailAddress);  //邮箱传递
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_UNBIND); //操作类型，解绑
				mTableAccountInfo.put(Constants.KEY_EMAIL_ACTIVE_STATUS, mEmailActiveStatus);//当前本地邮箱激活状态
				if(AccountUtils.verifyEmailStatusFromServer(AccountInfoActivity.this, mTableAccountInfo,true)){  //向服务器请求判断解绑邮箱是否验证成功
					mHandler.post(new Runnable() { //ui 线程处理 ,重新加载账号界面,界面可能会闪烁
						@Override
						public void run() {
							mEmailAddress = null;
							Intent data = new Intent();
							data.setAction(AccountInfoActivity.ACTION_UNBIND_EMAIL_SUCCESS);
							onActivityResult(REQUEST_CODE_BIND_OR_UNBIND_EMAIL,RESULT_OK,data);
//							setResult(RESULT_OK,data);
//							finish();
							
						}
					});

				}else{
                    if(isFromClick){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ActivityUtils.alert(AccountInfoActivity.this,getResources().getString(R.string.txt_gome_account_not_verified));
                            }
                        });
                    }
                }
			}
		});
	}
	void showRegisterSuccess2Toast(){
		String str = getResources().getString(R.string.txt_register_success_info_part4)+getResources().getString(R.string.txt_register_success_info_part5);
		ActivityUtils.alert(AccountInfoActivity.this,str);
	}
	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed() mCurrnetStep:"+mCurrnetStep);
		if(!showPreStep(mCurrnetStep)){
			Log.i(TAG, "onBackPressed() finish()");
			finish();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult() resultCode:"+resultCode+" requestCode:"+requestCode);
		if (resultCode != RESULT_OK) {
			return;
		}
		getAccountInfo();//重新获取账号详细信息更新界面
		updateView(); //调用界面更新
		switch (requestCode) {
			case REQUEST_CODE_CAPTURE_CAMEIA:
			case REQUEST_CODE_PICK_IMAGE:
				doActivityResultImageRequest(requestCode,data);
				break;
			case REQUEST_CODE_BIND_OR_UNBIND_EMAIL:
			case REQUEST_CODE_BIND_OR_UNBIND_PHONE:
				doActivityResultBindOrUnbindRequest(data);
				break;
			case REQUEST_CODE_PASSWORD_SECURITY:
				doActivityResultPasswordSecurityRequest(data);
				break;
			default:
				break;
		}

		
	}
	//activity返回，处理图片请求
	void doActivityResultImageRequest(int requestCode,Intent data){
		dismissDialog();
        Uri uri;
        if (data != null && data.getData() != null) {
            uri = data.getData();
        } else {
            uri = mTempPhotoUri;
        }
        mTempPhotoUri = null;
        // second we figure out if there is a thumbnail.
        // if yes, then use the thumbnail. This may help save memory. if no, we have to decode origin jpeg image as bitmap.
        // In most cases, camera will return data as null, so we can think that "data==null" means there is no thumbnail.
        // **But app camera360 will return some interesting information in Extras, so we must judge mBitmap is null or not**
        if (data != null && data.getData() == null && data.getExtras() != null && data.getExtras().get("data") != null) {
            // Only when there is a thumbnail in the data and uri in data is null
            Bundle bundle = data.getExtras();
            mBitmap = (Bitmap) bundle.get("data"); //get bitmap
			/* modified-end */
        }else{
			if (uri == null) {
				Log.i(TAG, "data: [" + data + "], there is no thumbnail in data and the uri is " + uri);
				return;
			}
			ContentResolver cr = this.getContentResolver();
			String mimeType = cr.getType(uri);
			if(null != mimeType && !mimeType.isEmpty() && mimeType.indexOf("/")>0)
				mImageType = mimeType.substring(mimeType.lastIndexOf("/")+1);
			try {
				mBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
				ActivityUtils.alert(this, "onActivityResult error："+e.toString());
				e.printStackTrace();
			}
			Log.i(TAG, "doActivityResultImageRequest() 000 mimeType:"+mimeType);
		}
		Log.e(TAG, "doActivityResultImageRequest  111  uri:" + uri+" mImageType:"+mImageType+" mBitmap.getByteCount():"+mBitmap.getByteCount());
		if(mBitmap.getByteCount() > COMPRESS_THREADHOD)  //若获取的bitmap大小超过阀值则压缩为原来的16分之一
			mBitmap = BitmapUtils.compressBitmap(mBitmap);//图像缩放宽高设置为原来的4分之一
		Log.e(TAG, "doActivityResultImageRequest  222 mBitmap.getByteCount():"+mBitmap.getByteCount());
		mBitmap = BitmapUtils.imageCropSquare(mBitmap, true);
		Log.e(TAG, "doActivityResultImageRequest  333 mBitmap.getByteCount():"+mBitmap.getByteCount());
//		imageSetAvatar.setImageBitmap(mBitmap);
//		imageAvatar.setImageBitmap(mBitmap);
		uploadLocalImageToSeverInThread(mBitmap,null == uri? null:uri.toString());
	}
	//activity返回，处理绑定、解绑邮箱或手机号请求
	void doActivityResultBindOrUnbindRequest(Intent data){
		String action = data.getAction();
		Log.e(TAG, "doActivityResultBindOrUnbindRequest() action:"+action);
		if(ACTION_BIND_EMAIL_SUCCESS.equals(action)){  		  //绑定邮箱成功 改成toast弹出消息
			//showDialog(this, DIALOG_BIND_EMAIL_SUCCESS);  
			ActivityUtils.alert(AccountInfoActivity.this, getResources().getString(R.string.txt_bind_mail_success));
		}else if(ACTION_UNBIND_EMAIL_SUCCESS.equals(action)){ //解绑邮箱成功
			showDialog(this, DIALOG_UNBIND_EMAIL_SUCCESS);
		}else if(ACTION_BIND_PHONE_SUCCESS.equals(action)){   //绑定手机成功 改成toast弹出消息
			//showDialog(this, DIALOG_BIND_PHONE_SUCCESS);   
			ActivityUtils.alert(AccountInfoActivity.this, getResources().getString(R.string.txt_bind_phone_success));
		}else if(ACTION_UNBIND_PHONE_SUCCESS.equals(action)){ //解绑手机成功
			showDialog(this, DIALOG_UNBIND_PHONE_SUCCESS);
		}else{
			Log.e(TAG, "doActivityResultBindOrUnbindRequest() error!! no action:"+action);
		}
	}
	//acvity返回，处理密码安全返回
	void doActivityResultPasswordSecurityRequest(Intent data){
		String action = data.getAction();
		Log.e(TAG, "doActivityResultPasswordSecurityRequest() action:"+action);
		if(ACTION_BIND_SECURITY_PHONE_SUCCESS.equals(action)){
			ActivityUtils.alert(AccountInfoActivity.this, getResources().getString(R.string.txt_set_security_phone_number_success));
		}else if(ACTION_UNBIND_SECURITY_PHONE_SUCCESS.equals(action)){ //解绑安全号码不是弹出消息
			showDialog(AccountInfoActivity.this,DIALOG_UNBIND_SECURITY_PHONE_SUCCESS);
		}
	}

	public void getAccountInfo() {
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(this); //获取本地账号
    	if(null != gomeAccount){
			HashMap<String,String> tableAccountInfo = AccountUtils.getAccountDetailFromLocal(this,gomeAccount.name); 
			mToken = tableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
			mEmailAddress = tableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
			mEmailActiveStatus = tableAccountInfo.get(Constants.KEY_EMAIL_ACTIVE_STATUS);
			mGomeId = tableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
			mNickName = tableAccountInfo.get(Constants.KEY_ACCOUNT_NICK_NAME);
			mPhoneNumber = tableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
			mServerAvatarPath = tableAccountInfo.get(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH);
			mLocalAvatarPath = tableAccountInfo.get(Constants.KEY_LOCAL_AVATAR_PATH);
			mRegisterType = tableAccountInfo.get(Constants.KEY_ACCOUNT_REGISTER_TYPE);
		    
		    Log.i(TAG, "getAccountInfo() 11 mNickName:"+mNickName+" mEmailAddress:"+mEmailAddress+" mEmailActiveStatus:"+mEmailActiveStatus
		    		+" mPhoneNumber:"+mPhoneNumber+" mLocalAvatarPath:"+mLocalAvatarPath+" mServerAvatarPath:"+mServerAvatarPath
		    		+" mRegisterType:"+mRegisterType);
		    if(null == mLocalAvatarPath){
		    	return;
		    }
		    File imageFile = new File(mLocalAvatarPath);
		    
		    FileInputStream fis = null;
		    if(imageFile.exists()){
				try {
					fis = new FileInputStream(mLocalAvatarPath);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "getAccountInfo() error:"+e.toString());
					e.printStackTrace();
				}
		    }else{
		    	Log.i(TAG, "getAccountInfo() cannot find image path："+mLocalAvatarPath);
		    	return;
		    }
		    Log.i(TAG, "getAccountInfo() 222 fis:"+fis);
		    mBitmap  = BitmapFactory.decodeStream(fis);
	        Log.i(TAG, "getAccountInfo() 333 mHeadBg:"+mBitmap);
    	}
    }
	void initView(){
		Log.i(TAG, "initView()");
        mAnimationOpenEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_enter_layout);
        mAnimationOpenExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_exit_layout);
        mAnimationCloseEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_enter_layout);
        mAnimationCloseExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_exit_layout);

		layoutActivityInfoMain = (LinearLayout)findViewById(R.id.layout_activity_info_main);
		registerLayoutSetp5Public = (LinearLayout)findViewById(R.id.layout_register_step5_public);
		registerLayoutSetp6Public = (LinearLayout)findViewById(R.id.layout_register_step6_public);
		registerLayoutSetp7Mail = (LinearLayout)findViewById(R.id.layout_register_step7_mail);
		registerLayoutSetp7Phone = (LinearLayout)findViewById(R.id.layout_register_step7_phone);
		registerLayoutSetp8Phone = (LinearLayout)findViewById(R.id.layout_register_step8_phone);
		layoutCheckPwd = (LinearLayout)findViewById(R.id.layout_public_chekc_pwd);

		mLayouts = new HashMap<String, LinearLayout>();
		mLayouts.put(KEY_LAYOUT_ACTIVITY_INFO_MAIN, layoutActivityInfoMain);
		mLayouts.put(KEY_LAYOUT_STEP5_PUBLIC, registerLayoutSetp5Public);
		mLayouts.put(KEY_LAYOUT_STEP6_PUBLIC, registerLayoutSetp6Public);
		mLayouts.put(KEY_LAYOUT_STEP7_MAIL, registerLayoutSetp7Mail);
		mLayouts.put(KEY_LAYOUT_STEP7_PHONE, registerLayoutSetp7Phone);
		mLayouts.put(KEY_LAYOUT_STEP8_PHONE, registerLayoutSetp8Phone);
		mLayouts.put(KEY_LAYOUT_CHECK_PWD, layoutCheckPwd);

		editNickName = (EditText)findViewById(R.id.edit_nick_name);
		editBindEmail = (EditText)findViewById(R.id.edit_bind_mailbox_for_step7_phone);
		editBindPhone = (EditText)findViewById(R.id.edit_phone_number_for_step7_mail);
		editBindPhoneSmsCode = (EditText)findViewById(R.id.edit_sms_verify_code_for_step7_mail);
		imageSetAvatar = (ImageView)findViewById(R.id.image_set_avatar);
		imageAvatar = (ImageView)findViewById(R.id.image_avatar);
		imageDeleteNickName = (ImageView)findViewById(R.id.image_delete_nick_name);
		imageDeletePhoneNumber = (ImageView)findViewById(R.id.image_delete_phone_number);
		imageDeleteSmsVerifyCode = (ImageView)findViewById(R.id.image_delete_sms_verify_code_for_step7_mail);
		imageDeleteEmail = (ImageView)findViewById(R.id.image_delete_email);
		editInputPwd = (EditText)findViewById(R.id.edit_input_password);
		editInputPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD));
        editInputPwd.setTypeface(Typeface.DEFAULT);
		imageDeleteInputPwd = (ImageView)findViewById(R.id.image_delete_input_password);
		btnCheckPwdNext = (Button)findViewById(R.id.btn_public_check_pwd_next);

		editNickName.addTextChangedListener(new EditChangedListener(TYPE_EDIT_NICK_NAME));
		editBindPhone.addTextChangedListener(new EditChangedListener(TYPE_EDIT_BIND_PHONE_NUMBER));
		editBindPhoneSmsCode.addTextChangedListener(new EditChangedListener(TYPE_EDIT_SMS_CODE));
		editBindEmail.addTextChangedListener(new EditChangedListener(TYPE_EDIT_BIND_EMAIL));
		imageHidePasswordForPassword = (ImageView)findViewById(R.id.image_hide_password_for_password);
		imageShowPasswordForPassword = (ImageView)findViewById(R.id.image_show_password_for_password);

        btnLoginOut = (Button) findViewById(R.id.btn_login_out);
		btnStep5Next = (Button) findViewById(R.id.btn_step5_public_next);
        btnStep6Next = (Button) findViewById(R.id.btn_step6_public_next);
		btnStep7Complete = (Button) findViewById(R.id.btn_step7_mail_complete);
		btnStep7Email = (Button) findViewById(R.id.btn_step7_phone_next);
		
		textSendSmsCodeForStep7Mail = (TextView)findViewById(R.id.text_send_sms_verify_code_for_step7_mail);
		textNickName = (TextView)findViewById(R.id.text_nick_name);
		textBindPhone = (TextView)findViewById(R.id.text_bind_phone);
		textBindEmail = (TextView)findViewById(R.id.text_bind_email);
		textStep8SendEmailReminder = (TextView)findViewById(R.id.text_setp8_send_email_remiander);
		textGomeId = (TextView)findViewById(R.id.text_gome_id);
		textResendEmail = (TextView)findViewById(R.id.text_step8_resend_mail);
		
		timeSmsCodeForMailRegister = new TimeCountForMailRegisterSmsCode(60000, 1000);
		timeSmsCodeForPhoneRegister = new TimeCountForPhoneRegisterSendEmail(60000, 1000);
		updateView();


	}
	//更新界面数据
	void updateView(){
		Log.i(TAG, "updateView()");
		String strNotBind = getResources().getString(R.string.txt_not_bind);
		if(null != mBitmap)
			imageAvatar.setImageBitmap(mBitmap);
		else
			Log.i(TAG, "updateView() mBitmap is null");
		if(null != textNickName){
			String strNoNickName = getResources().getString(R.string.txt_not_set_nickname);
			textNickName.setText((null != mNickName && !mNickName.isEmpty())?mNickName:strNoNickName);
		}

		if(null != textGomeId){
			textGomeId.setText(mGomeId == null?"":mGomeId);
		}
		if(null != textBindPhone){
			textBindPhone.setText(mPhoneNumber == null?strNotBind:mPhoneNumber);
		}
		if(null != textBindEmail){
			Log.i(TAG, "updateView() mEmailAddress:"+mEmailAddress+" mEmailActiveStatus:"+mEmailActiveStatus); //邮箱状态正常，或者待解绑都显示
			textBindEmail.setText((mEmailAddress == null || Constants.EMAIL_STATUS_TO_BIND.equals(mEmailActiveStatus))?strNotBind:mEmailAddress);
		}
	}
	//注册处理注册过来的请求
	void parseAction(){
        AccountUtils.clearLocalAccountStartMode(this); // 清除启动模式相关数据
		String action = getIntent().getAction();
		mRegisterType = getIntent().getStringExtra(Constants.KEY_ACCOUNT_REGISTER_TYPE);
		Log.i(TAG, "parseAction() 111 action:"+action+" mRegisterType:"+mRegisterType);
		if(ACTION_REGISTER_SUCCESS.equals(action)){ //注册成功显示
			//showRegisterSuccess1Dialog();//先弹出框，然后在更新弹出框里面的信息
			if(null == mRegisterType){  //若intent传递过来的注册类型为null，则从本地获取注册类型
				getAccountInfo();
				Log.i(TAG, "parseAction() 222  mRegisterType:"+mRegisterType);
			}
			
			//showDialog(this,DIALOG_REGISTER_SUCCESS1);
            showSimpleGomeAlertDialog(this,DIALOG_REGISTER_SUCCESS1);
			String strFormat = getResources().getString(R.string.txt_register_success_info1_all);
			String str = null;
			if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){
				str = getResources().getString(R.string.txt_email);
				str = String.format(strFormat, str);	
			}else if(Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)){
				str = getResources().getString(R.string.txt_phone);//txt_phone
				str = String.format(strFormat, str);
			}
			Log.i(TAG, "parseAction() 333 str:"+str+" textRegisterSuccess1Prompt:"+textRegisterSuccess1Prompt);
			if(null !=textRegisterSuccess1Prompt && null != str)
				textRegisterSuccess1Prompt.setText(str);

		}
	}
	///////////////////////////点击事件处理begin
	public void doOnclickAccountInfoMainBackButton(View view){
		Log.i(TAG, "doOnclickAccountInfoMainBackButton()");
		onBackPressed();
	}
	//点击云服务条目
	public void doOnclickCloudServiceItem(View view){
		Log.i(TAG, "doOnclickCloudServiceItem() ");
		if(ActivityUtils.isActivityExists(AccountInfoActivity.this, ActivityUtils.CLOUD_SERVICE_PACAAGE_NAME, ActivityUtils.CLOUD_SERVICE_MAIN_ACTIVITY)){
			ActivityUtils.startActivityByName(AccountInfoActivity.this, ActivityUtils.CLOUD_SERVICE_PACAAGE_NAME, ActivityUtils.CLOUD_SERVICE_MAIN_ACTIVITY);
            //overridePendingTransition(R.anim.gome_activity_open_enter, R.anim.gome_activity_open_exit);
		}else{
			ActivityUtils.alert(AccountInfoActivity.this, getResources().getString(R.string.txt_cloud_servie_not_exists));
		}
	}
	public void doOnlcikStep4PublicTalkLaterButton(View view){
		Log.i(TAG, "doOnlcikStep4PublicTalkLaterButton()");
		dismissDialog();
	}
	//点击去设置
	public void doOnlcikStep4PublicGoSetButton(View view){
		Log.i(TAG, "doOnlcikStep4PublicGoSetButton()");
		dismissDialog();
		showStep(KEY_LAYOUT_STEP5_PUBLIC);
	}
	//设置昵称，点击返回 
	public void doOnclickStep5PublicBackButton(View view){
        Log.i(TAG,"doOnclickStep5PublicBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//设置昵称，点击跳过
	public void doOnclickStep5PublicSkipButton(View view){
		Log.i(TAG, "doOnclickStep5PublicSkipButton()");
		dismissDialog();
		showStep(KEY_LAYOUT_STEP6_PUBLIC);
	}
	//设置昵称，点击删除输入昵称
	public void doOnclickDeleteNickNameButton(View view){
		editNickName.setText("");
	}
	//设置昵称界面点击下一步
	public void doOnclickStep5PublicNextButton(View view){
		Log.i(TAG, "doOnclickStep5PublicNextButton()");
		if(editNickName.getText().toString().isEmpty()){
			String str = getResources().getString(R.string.alert_nick_name_null);
			ActivityUtils.alert(AccountInfoActivity.this, str);
			return;
		}
		if(null != mGomeId){ //设置昵称到服务器
			btnStep5Next.setEnabled(false);
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					mTableAccountInfo.clear();
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId); //用来本地存储
					mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_NICK_NAME,editNickName.getText().toString());
					if(AccountUtils.modifyAccountInfoFromServer(AccountInfoActivity.this,mTableAccountInfo)){
						mHandler.post(new Runnable() {  //ui线程，只有设置成功才执行下一步
							@Override
							public void run() {
								ActivityUtils.closeKeyboard(AccountInfoActivity.this);
								showStep(KEY_LAYOUT_STEP6_PUBLIC);
							}
						});
					}
					
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							btnStep5Next.setEnabled(true);
						}
					});
				}
			});
		}

	}
	//设置头像,点击返回按钮
	public void doOnclickStep6PublicBackButton(View view){
        Log.i(TAG,"doOnclickStep6PublicBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//设置头像,点击跳过按钮
	public void doOnclickStep6PublicSkipButton(View view){
		Log.i(TAG, "doOnclickStep6PublicSkipButton() ");
		step6PublicDoNext();
	}
	
	public void doOnclickRegisterStep6SetAvatar(View view){
		Log.i(TAG, "doOnclickRegisterStep6SetAvatar()");
		if(ActivityUtils.checkNetwork(this)){
			showDialog(this,DIALOG_SET_AVATAR);
		}
	}
	public void doOnclickStep6PublicNextButton(View view){
		Log.i(TAG, "doOnclickStep6PublicNextButton() ");
		step6PublicDoNext();
	}
	void step6PublicDoNext(){
		Log.i(TAG, "step6PublicDoNext() mRegisterType:"+mRegisterType);
		if(Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)){
			showStep(KEY_LAYOUT_STEP7_PHONE);
		}else if(Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)){
			showStep(KEY_LAYOUT_STEP7_MAIL);
		}
	}
	//点击从图片获取照片信息
	public void doOnclickGalleryButton(View view){
		Log.i(TAG, "doOnclickGalleryButton()");
		Intent intent = new Intent(Intent.ACTION_PICK);  
        intent.setType("image/*");//相片类型  
        Bundle b = new Bundle();
        b.putString("crop", "crop");
        intent.putExtras(b);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);  
	}
	//点击从相机获取照片信息
	public void doOnclickCameraButton(View view){
        Log.i(TAG, "doOnclickCameraButton()");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTempPhotoUri = generateTempImageUri(this);
        Log.d(TAG, "doOnclickCameraButton() mTempPhotoUri:"+mTempPhotoUri.toString());
        addPhotoPickerExtras(intent, mTempPhotoUri);
        intent.putExtra("head_photo_from_camera","yes");
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);  
	}
    /**
     * @param intent The intent to add extras to.
     * @param photoUri The uri of the file to save the image to.
     */
    public void addPhotoPickerExtras(Intent intent, Uri photoUri) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri(MediaStore.EXTRA_OUTPUT, photoUri));
    }

    public static Uri generateTempImageUri(Context context) {
        final String fileProviderAuthority = context.getResources().getString(
                R.string.photo_file_provider_authority);
        return FileProvider.getUriForFile(context, fileProviderAuthority,
                new File(pathForTempPhoto(context, generateTempPhotoFileName())));
    }

    private static String generateTempPhotoFileName() {
        final Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(PHOTO_DATE_FORMAT, Locale.US);
        return "AccountPhoto-" + dateFormat.format(date) + ".jpg";
    }

    private static String pathForTempPhoto(Context context, String fileName) {
        final File dir = context.getCacheDir();
        dir.mkdirs();
        final File f = new File(dir, fileName);
        return f.getAbsolutePath();
    }
	//手机注册 点击绑定邮箱下一步按钮
	public void doOnclickStep7PhoneBackButton(View view){
        Log.i(TAG,"doOnclickStep7PhoneBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//手机注册 绑定邮箱 ,点击跳过按钮
	public void doOnclickStep7PhoneSkipButton(View view){
		Log.i(TAG, "doOnclickStep7PhoneSkipButton()");
		showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN,true);
	}
	//手机注册 绑定邮箱,点击删除输入的邮箱号
	public void doOnclickDeleteEmailButton(View view){
		editBindEmail.setText("");
	}
	//手机注册 绑定邮箱点击下一步
	public void doOnlcikStep7PhoneNextButton(View view){
		final String email = editBindEmail.getText().toString().trim();
		if(ActivityUtils.isEmail(email)){
			ActivityUtils.closeKeyboard(AccountInfoActivity.this);
			btnStep7Email.setEnabled(false);
			sWorker.post(new Runnable() {
				@Override
				public void run() {
					mTableAccountInfo.clear();
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
					mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, email);
					mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
					mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND);
		        	if(!AccountUtils.verifyPhoneOrEmailValidFromServer(AccountInfoActivity.this, mTableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
				        		btnStep7Email.setEnabled(true);
							}
						});

		        		Log.e(TAG, "doOnlcikStep7PhoneNextButton() error email:"+email+" not valid!!");
		        		return;
		        	}
					if(AccountUtils.bindFromServer(AccountInfoActivity.this, mTableAccountInfo)){
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								mRegBindEmailAddress = editBindEmail.getText().toString().trim();
								String strFormat = getResources().getString(R.string.txt_send_email_reminder_part1);
								String str = String.format(strFormat, mRegBindEmailAddress);
								textStep8SendEmailReminder.setText(str);
								showStep(KEY_LAYOUT_STEP8_PHONE);
							}
						});
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
			        		btnStep7Email.setEnabled(true);
						}
					});
				}
			});
		}else{
			ActivityUtils.alert(AccountInfoActivity.this, getResources().getString(R.string.alert_input_valid_email));
		}
	}
	
	//邮箱验证界面点击返回按钮
	public void doOnclickStep8PhoneBackButton(View view){
        Log.i(TAG,"doOnclickStep8PhoneBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//邮箱验证界面点击跳过按钮
	public void doOnclickStep8PhoneSkipButton(View view){
		Log.i(TAG, "doOnclickStep8PhoneSkipButton()");
		showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN,true);
		//showDialog(AccountInfoActivity.this,DIALOG_REGISTER_SUCCESS2);
		showRegisterSuccess2Toast();
		mRegBindEmailAddress = null; //跳过也显示注册完成
	}
	public void doOnclickCheckEmail(View view){
			Log.i(TAG,"doOnclickCheckEmail() ");
			//ActivityUtils.verifyMail(this);
			ActivityUtils.verifyMail(this,editBindEmail.getText().toString().trim());
	}
	public void doOnclickResendEmail(View view){
		Log.i(TAG,"doOnclickResendEmail() ");
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_EMAIL, mRegBindEmailAddress);
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND);
				
				Log.i(TAG,"doOnclickResendEmail() mBindEmailAddress:"+mRegBindEmailAddress);
				if(AccountUtils.sendEmailFromServer(AccountInfoActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							timeSmsCodeForPhoneRegister.start();
						}
					});
				}
			}
		});

		//是否需要更新60秒倒计时看ui
	}
	//邮箱注册  绑定手机界面,点击返回
	public void doOnclickStep7MailBackButton(View view){
        Log.i(TAG,"doOnclickStep7MailBackButton() mCurrnetStep:"+mCurrnetStep);
        showPreStep(mCurrnetStep);
	}
	//邮箱注册  绑定手机界面,点击返回
	public void doOnclickStep7MailSkipButton(View view){
		Log.i(TAG, "doOnclickStep7MailSkipButton()");
		showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN,true);
		showRegisterSuccess2Toast();
	}
	//邮箱注册  绑定手机界面,点击删除短信验证码
	public void doOnclickDeleteSmsVerifyCodeForStep7MailButton(View view){
		editBindPhoneSmsCode.setText("");
	}
	//邮箱注册  绑定手机界面,点击删除输入的手机号
	public void doOnclickDeletePhoneNumberButton(View view){
		editBindPhone.setText("");
	}
	//邮箱注册  绑定手机界面点击点击发送短信验证码
	public void doOnclikSendSmsVerifyCode(View view){
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editBindPhone.getText().toString());
		mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE, Constants.OPERATE_TYPE_BIND);
		mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(AccountUtils.sendSmsVerifyCodeFromServer(AccountInfoActivity.this,mTableAccountInfo)){
					mHandler.post(new Runnable() { //ui线程
						@Override
						public void run() {
							timeSmsCodeForMailRegister.start();
						}
					});
				}
			}
		});

	}
	//邮箱注册 绑定手机号点击完成按钮
	public void doOnlcikStep7MailCompleteButton(View view){
		Log.i(TAG, "doOnlcikStep7MailCompleteButton() editBindPhone"+editBindPhone.getText().toString());
		if(!ActivityUtils.isMobileNO(editBindPhone.getText().toString())){
			ActivityUtils.alert(AccountInfoActivity.this, getResources().getString(R.string.alert_input_valid_phone));
			return;
		}
		btnStep7Complete.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SMS_CODE,editBindPhoneSmsCode.getText().toString().trim());
				
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER, editBindPhone.getText().toString().trim());
				mTableAccountInfo.put(Constants.KEY_OPERATE_TYPE,Constants.OPERATE_TYPE_BIND);
				if(!AccountUtils.verifySmsCodeValidFromServer(AccountInfoActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							btnStep7Complete.setEnabled(true);
						}
					});

					Log.e(TAG, "doOnlcikStep7MailCompleteButton() verify sms code error!!!");
					return;
				}
				
				if(AccountUtils.bindFromServer(AccountInfoActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {//ui线程
						@Override
						public void run() {
							//showDialog(AccountInfoActivity.this,DIALOG_REGISTER_SUCCESS2);
							refreshView();
							showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN,true);
							showRegisterSuccess2Toast();
						}
					});
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						btnStep7Complete.setEnabled(true);
					}
				});
			}
		});

	}
	public void doOnlcikRegisterSuccess2ExperienceNowButton(View view){
		//hideRegisterSuccess2Dialog();
		dismissDialog();
	}
	//点击账号主界面个人信息item
	public void doOnclickPersonalInfoButton(View view){
		Log.i(TAG, "doOnclickPersonalInfoButton() ###");
		Intent i = new Intent(AccountInfoActivity.this,PersonalInfoActivity.class);
		//startActivity(i);
		startActivityForResult(i,REQUEST_CODE_PERSONAL_INFO);
	}
	//点击账号主界面绑定手机item
	public void doOnclickBindPhoneButton(View view){
		Log.i(TAG, "doOnclickBindPhoneButton() ###");
		//add by yimin.zhu for bugid 1271
		//bindPhone();
		//有安全码的时候进行安全码验证
		if(AccountUtils.isSaftyNumberExist(this,mGomeId)) {
			doCheckSecurityPhoneForBindPhone();
		} else {
			//没有安全的时候进行密码验证
			doCheckPasswordForBindPhone();
		}
	}

	public void doCheckSecurityPhoneForBindPhone() {
		Log.i(TAG, "doCheckSecurityPhoneForBindPhone");
		Intent i = new Intent(AccountInfoActivity.this,CheckPhoneActivity.class);
		i.putExtra("activityName", "AccountInfoActivity-BindPhone");
		startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_PHONE);
	}

	public void doCheckPasswordForBindPhone() {
		Log.i(TAG, "doCheckPasswordForBindPhone");
		Intent i = new Intent(AccountInfoActivity.this,CheckPasswordActivity.class);
		i.putExtra("activityName", "AccountInfoActivity-BindPhone");
		startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_PHONE);
	}

	public void doCheckSecurityPhoneForBindEmail() {
		Log.i(TAG, "doCheckSecurityPhoneForBindEmail");
		Intent i = new Intent(AccountInfoActivity.this,CheckPhoneActivity.class);
		i.putExtra("activityName", "AccountInfoActivity-BindEmail");
		startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_EMAIL);
	}

	public void doCheckPasswordForBindEmail() {
		Log.i(TAG, "doCheckPasswordForBindEmail");
		Intent i = new Intent(AccountInfoActivity.this,CheckPasswordActivity.class);
		i.putExtra("activityName", "AccountInfoActivity-BindEmail");
		startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_EMAIL);
	}

	//点击账号主界面绑定邮箱item
	public void doOnclickBindMailButton(View view){
		Log.i(TAG, "doOnclickBindMailButton() ###");
		//add by yimin.zhu for bugid 1271
		//bindEmail();
		//有安全码的时候进行安全码验证
		if(AccountUtils.isSaftyNumberExist(this,mGomeId)) {
			doCheckSecurityPhoneForBindEmail();
		} else {
			//没有安全的时候进行密码验证
			doCheckPasswordForBindEmail();
		}
	}
	//点击账号主界面 密码安全item
	public void doOnclickPasswordSecurityButton(View view){
		Log.i(TAG, "doOnclickPasswordSecurityButton() ###");
		Intent i = new Intent(AccountInfoActivity.this,PasswordSecurityActivity.class);
		startActivityForResult(i, REQUEST_CODE_PASSWORD_SECURITY);  //
	}
	//点击应用退出按钮
	public void doOnclickLoginOutButton(View view){
		Log.i(TAG, "doOnclickLoginOutButton() ");
		showStep(KEY_LAYOUT_CHECK_PWD);
	}
	//退出弹出框点击确定按钮
	public void doOnclickLoginOutOkButton(View view){
		final String pwd = editInputPwd.getText().toString().trim();
		Log.i(TAG, "doOnclickLoginOutOkButton() FLAG_ACTIVITY_CLEAR_TOP pwd:"+pwd);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(AccountUtils.loginOut(AccountInfoActivity.this,mGomeId,pwd)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Intent i = new Intent(AccountInfoActivity.this,LoginActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							AccountInfoActivity.this.startActivity(i);
							finish();
						}
					});
				}
			}
		});
		dismissDialog();
	}

	public void doOnlcikPublicCheckPwdNextButton(View view){
		verifyPwdFromServer();
	}

	/**
	 * 验证密码是否正确，若正确弹出注销弹出框
	 */
	void verifyPwdFromServer(){
		String pwd = editInputPwd.getText().toString().trim();
		Log.i(TAG, "verifyPwdFromServer() mToken"+mToken+" pwd:"+pwd);
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, pwd);
        btnCheckPwdNext.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				if(AccountUtils.verifyPwdOrSmsCodeFromServer(AccountInfoActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							//showDialog(AccountInfoActivity.this,DIALOG_LOGIN_OUT);
                            showSimpleGomeAlertDialog(AccountInfoActivity.this,DIALOG_LOGIN_OUT);
						}
					});
				}
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        btnCheckPwdNext.setEnabled(true);
                    }
                });
			}
		});

	}
	//退出弹出框点击取消按钮
	public void doOnclickLoginOutCancelButton(View view){
		Log.i(TAG, "doOnclickLoginOutCancelButton() ");
		dismissDialog();
	}
	//关闭弹出框 绑定手机或邮箱成功界面点击确定执行
	public void doOnclickCloseDialog(View view){
		Log.i(TAG, "doOnclickCloseDialog() ");
		dismissDialog();
	}
	//解绑邮箱成功，点击确定按钮，更换邮箱绑定
	public void doOnclickUnbindEmailSuccessOkButton(View view){
		Log.i(TAG, "doOnclickUnbindEmailSuccessOkButton() ");
		dismissDialog();
		bindEmail();
	}
	//解绑邮箱成功，点击取消按钮，
	public void doOnclickUnbindEmailSuccessCancelButton(View view){
		Log.i(TAG, "doOnclickUnbindEmailSuccessCancelButton() ");
		dismissDialog();
	}
	//解绑手机号成功，点击确定按钮，
	public void doOnclickUnbindPhoneSuccessOkButton(View view){
		Log.i(TAG, "doOnclickUnbindPhoneSuccessOkButton() ");
		dismissDialog();
		bindPhone();
	}
	//解绑手机号成功，点击取消按钮，
	public void doOnclickUnbindPhoneSuccessCancelButton(View view){
		Log.i(TAG, "doOnclickUnbindPhoneSuccessCancelButton() ");
		dismissDialog();
	}
	//解绑安全手机号成功，点击确定按钮，
	public void doOnclickUnbindSecurityPhoneSuccessOkButton(View view){
		Log.i(TAG, "doOnclickUnbindSecurityPhoneSuccessOkButton() ");
		dismissDialog();
		bindSecurityPhone();
	}
	//解绑安全手机号成功，点击取消按钮，
	public void doOnclickUnbindSecurityPhoneSuccessCancelButton(View view){
		Log.i(TAG, "doOnclickUnbindSecurityPhoneSuccessCancelButton() ");
		dismissDialog();
	}
	///////////////////点击事件处理begin
	//设置密码界面点击返回
	public void doOnclickPublicCheckPwdButton(View view){
		showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN,true);
	}
	public void doOnclickDeleteInputPwdButton(View view){
		editInputPwd.setText("");
	}
	//////////////////////////点击事件处理end
	/**
	 * 注册后已经进入账号主界面的点击绑定邮箱操作，非刚注册后设置绑定邮箱操作
	 */
	void bindEmail(){
		Log.i(TAG, "bindEmail() use FLAG_ACTIVITY_NEW_TASK");
		Intent i = new Intent(AccountInfoActivity.this,BindOrUnbindActivity.class);
		i.setAction(BindOrUnbindActivity.ACTION_BIND_EMAIL);
		startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_EMAIL);  //
	}
	/**
	 * 注册后已经进入账号主界面的点击绑定手机操作，非刚注册后设置绑定手机操作
	 */
	void bindPhone(){
		Intent i = new Intent(AccountInfoActivity.this,BindOrUnbindActivity.class);
		i.setAction(BindOrUnbindActivity.ACTION_BIND_PHONE);
		startActivityForResult(i, REQUEST_CODE_BIND_OR_UNBIND_PHONE); 
	}
	/**
	 * 在密码安全中解绑安全号码成功后，跳转账号界面点击重新更换绑定时调用
	 */
	void bindSecurityPhone(){
		Intent i = new Intent(AccountInfoActivity.this,PasswordSecurityActivity.class);
		i.setAction(PasswordSecurityActivity.ACTION_BIND_SECURITY_PHONE);
		startActivityForResult(i, REQUEST_CODE_PASSWORD_SECURITY); 
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
	/**
	 * 
	 * @param keyLayout
	 * @return 是否有上一步界面，若没有则返回false
	 */
	boolean showPreStep(String keyLayout){
		Log.i(TAG, "showPreStep() keyLayout:"+keyLayout);
		if(KEY_LAYOUT_STEP8_PHONE.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_STEP7_PHONE;
		}else if(KEY_LAYOUT_STEP7_PHONE.equals(keyLayout) || KEY_LAYOUT_STEP7_MAIL.equals(keyLayout)){
			mCurrnetStep =KEY_LAYOUT_STEP6_PUBLIC;
		}else if(KEY_LAYOUT_STEP6_PUBLIC.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_STEP5_PUBLIC;
		}else if(KEY_LAYOUT_STEP5_PUBLIC.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_ACTIVITY_INFO_MAIN;
		}else if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)) {
            mCurrnetStep = KEY_LAYOUT_ACTIVITY_INFO_MAIN;
        }else{
			return false;
		}
		showStep(mCurrnetStep,true);
		return true;
	}
	void showStep(final String keyLayout,boolean...backMode){
		mCurrnetStep = keyLayout;
        boolean isBackMode = false;
        boolean isUseAnimation = true;
        if(null != backMode &&  0 != backMode.length){
            isBackMode = true;
        }
        if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)){ //add for bug PRODUCTION-12309
            btnLoginOut.setEnabled(false);
        }
        if(KEY_LAYOUT_ACTIVITY_INFO_MAIN.equals(keyLayout) && !isBackMode){  //从activity加载的第一个界面无需再加动画
            isUseAnimation = false;
        }
        Log.i(TAG, "showStep** keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size())
                +" isBackMode:"+isBackMode+" isUseAnimation:"+isUseAnimation);

		ActivityUtils.closeKeyboard(this);
        //设置状态栏背景颜色
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(KEY_LAYOUT_ACTIVITY_INFO_MAIN.equals(keyLayout)) { //主界面
            window.setStatusBarColor(getResources().getColor(R.color.color_title_bg_account_info_main));
        }else{
            window.setStatusBarColor(getResources().getColor(R.color.color_title_bg));
        }
        if(KEY_LAYOUT_STEP6_PUBLIC.equals(keyLayout)){  //设置头像界面只有设置好头像才让下一步可点击
            getAccountInfo();
            if(null != mLocalAvatarPath){
                btnStep6Next.setEnabled(true);
            }else{
                btnStep6Next.setEnabled(false);
            }
        }
        if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)){ //进入输入密码界面先清除密码数据
            editInputPwd.setText("");
        }
		if(null != mLayouts){
			//切换界面不显示软键盘
            ActivityUtils.closeKeyboard(AccountInfoActivity.this);
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
                                       if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)){ //add for bug PRODUCTION-12309
                                           btnLoginOut.setEnabled(true);
                                       }
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
	//只隐藏指定步骤的显示
	void hideStep(String keyLayout){
		Log.i(TAG, "hideStep keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size()));
		if(null != mLayouts){
			for (Map.Entry<String, LinearLayout> entry : mLayouts.entrySet()) {  
				   String key = entry.getKey();
				   LinearLayout linearLayout = entry.getValue();
				   if(keyLayout.equals(key)){
					   linearLayout.setVisibility(View.GONE);
				   }
			}
		}
	}
	/////////////////////////////////////////////dialog 相关begin
    void showDialog(Context context,int dialogId){
    	LayoutInflater inflater =  LayoutInflater.from(context);
    	View layout = null;
    	switch (dialogId) {
			case DIALOG_REGISTER_SUCCESS1:
				layout = inflater.inflate(R.layout.register_success1_layout,(ViewGroup)findViewById(R.id.root_view));
		    	textRegisterSuccess1Prompt = (TextView)layout.findViewById(R.id.text_register_success1_prompt);
		    	Log.i(TAG, "showDialg() DIALOG_REGISTER_SUCCESS1 textRegisterSuccess1Prompt:"+textRegisterSuccess1Prompt);
				break;
			case DIALOG_SET_AVATAR:
				layout = inflater.inflate(R.layout.alert_set_avatar_chooser_layout,null);
				break;
			case DIALOG_LOGIN_OUT:
				layout = inflater.inflate(R.layout.alert_login_out_layout,null);
				break;
			case DIALOG_BIND_EMAIL_SUCCESS:
				layout = inflater.inflate(R.layout.alert_bind_email_success_layout,null);
				break;
			case DIALOG_BIND_PHONE_SUCCESS:
				layout = inflater.inflate(R.layout.alert_bind_phone_success_layout,null);
				break;
			case DIALOG_UNBIND_EMAIL_SUCCESS:
				layout = inflater.inflate(R.layout.alert_unbind_email_success_layout,null);
				break;
			case DIALOG_UNBIND_PHONE_SUCCESS:
				layout = inflater.inflate(R.layout.alert_unbind_phone_success_layout,null);
				break;
			case DIALOG_UNBIND_SECURITY_PHONE_SUCCESS:
				layout = inflater.inflate(R.layout.alert_unbind_security_phone_success_layout,null);
				break;
			default:
				break;
		}
    	if(null != mDialog){  //若dialog非空强制取消，放置解绑邮箱成功弹出两次提示框的情况
			Log.i(TAG, "showDialog() mDialog != null cancel it!!");
			mDialog.cancel();
			mDialog = null;
    	}
    	mDialog = new GomeAlertDialog.Builder(this).setView(layout).create();
    	Window window = mDialog.getWindow();
    	WindowManager.LayoutParams lp = window.getAttributes();
    	lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
    	mDialog.show();
    }
    //显示非自定义布局的国美dialog
    void showSimpleGomeAlertDialog(Context context,int dialogId){
        Log.i(TAG,"showSimpleGomeAlertDialog() ** dialogId:"+dialogId);
        try {
            switch (dialogId) {
                case DIALOG_REGISTER_SUCCESS1:
                    String strFormat = getResources().getString(R.string.txt_register_success_info1_all);
                    String str = null;
                    if (Constants.REGISTER_TYPE_PHONE.equals(mRegisterType)) {
                        str = getResources().getString(R.string.txt_email);
                        str = String.format(strFormat, str);
                    } else if (Constants.REGISTER_TYPE_EMAIL.equals(mRegisterType)) {
                        str = getResources().getString(R.string.txt_phone);//txt_phone
                        str = String.format(strFormat, str);
                    }
                    Log.i(TAG, "showDialg() DIALOG_REGISTER_SUCCESS1 str:" + str);
                    new GomeAlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.txt_register_success))
                            .setMessage(str)
                            .setPositiveButton(R.string.btn_go_setup, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doOnlcikStep4PublicGoSetButton(null);
                                }
                            })
                            .setNegativeButton(R.string.btn_talk_later, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doOnlcikStep4PublicTalkLaterButton(null);
                                }
                            })
                            .show();
                    break;
                case DIALOG_LOGIN_OUT:
                    new GomeAlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.txt_login_out_alert_title))
                            .setMessage(getResources().getString(R.string.txt_login_out_prompt))
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doOnclickLoginOutOkButton(null);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showStep(KEY_LAYOUT_ACTIVITY_INFO_MAIN, true);
                                }
                            })
                            .show();
                    break;
                default:
                    break;
            }
        }catch (Exception ex){
            Log.i(TAG, "showSimpleGomeAlertDialog() error ex:" + ex.toString());
        }
    }
	void dismissDialog(){
		Log.i(TAG, "dismissDialog()");
    	if(null != mDialog){
    		mDialog.dismiss();
    	}
	}
/////////////////////////////////////////////dialog 相关end
    /////////////////////调用服务器请求相关操作begin///////
    /**
     * 上传本地头像到服务器,使用新的线程
     * @param bitMap
     */
    void uploadLocalImageToSeverInThread(final Bitmap bitMap,final String strUri){
    	if(null == mProgressDialog){
            mProgressDialog = new GomeProgressDialog.Builder(this)
                    .setProgressStyle(GomeProgressDialog.STYLE_SPINNER)
                    .setCancelable(false).setMessage(getResources()
                            .getString(R.string.txt_avatar_uploading))
                    .create();
    	}
    	mProgressDialog.show();
    	sWorker.post(new Runnable() {
			@Override
			public void run() {
				HashMap<String,String> tableAccountInfo = new HashMap<String, String>();
				tableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				tableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId); //本地保存需要用到
				tableAccountInfo.put(Constants.KEY_AVATAR_TYPE, mImageType);//需要根据实际图片设置 by lilei
				tableAccountInfo.put(Constants.KEY_ACCOUNT_SERVER_AVATAR, BitmapUtils.bitmap2StrByBase64(bitMap));
				tableAccountInfo.put(Constants.KEY_LOCAL_AVATAR_URI,strUri);
				if(AccountUtils.uploadLocalImageToServer(AccountInfoActivity.this, bitMap, tableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Log.i(TAG, "uploadLocalImageToSeverInThread() finish update avatar ui!! mBitmap:"+mBitmap+" bitMap:"+bitMap);
							imageSetAvatar.setImageBitmap(bitMap);
							imageAvatar.setImageBitmap(bitMap);
                            btnStep6Next.setEnabled(true);
						}
					});
				}
				mHandler.post(new Runnable() {  //上传完成关闭等待框
					@Override
					public void run() {
						mProgressDialog.dismiss();
					}
				});
			}
		});
    }
/////////////////////调用服务器请求相关操作end///////
	class TimeCountForMailRegisterSmsCode extends CountDownTimer {

		public TimeCountForMailRegisterSmsCode(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {

			textSendSmsCodeForStep7Mail.setEnabled(false);
			textSendSmsCodeForStep7Mail.setClickable(false);
			String strFormat = getResources().getString(R.string.register_code_send_again);
			String str = String.format(strFormat, millisUntilFinished/1000);
			textSendSmsCodeForStep7Mail.setText(str);
		}
		@Override
		public void onFinish() {
			textSendSmsCodeForStep7Mail.setEnabled(true);
			textSendSmsCodeForStep7Mail.setClickable(true);
			textSendSmsCodeForStep7Mail.setText(getResources().getString(R.string.register_send_again));
		}
	}
	class TimeCountForPhoneRegisterSendEmail extends CountDownTimer {
		public TimeCountForPhoneRegisterSendEmail(long millisInFuture,
				long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onTick(long millisUntilFinished) {
			textResendEmail.setEnabled(false);
			textResendEmail.setClickable(false);
			String strFormat = getResources().getString(R.string.register_code_send_again);
			String str = String.format(strFormat, millisUntilFinished/1000);
			textResendEmail.setText(str);
			
		}
		@Override
		public void onFinish() {
			textResendEmail.setEnabled(true);
			textResendEmail.setClickable(true);
			textResendEmail.setText(getResources().getString(R.string.register_send_again));
			
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
			case TYPE_EDIT_NICK_NAME:
				imageDeleteNickName.setVisibility(View.VISIBLE);
				btnStep5Next.setEnabled(true);
				break;
			case TYPE_EDIT_BIND_PHONE_NUMBER:
				imageDeletePhoneNumber.setVisibility(View.VISIBLE);
				if(checkStep7MailInputFull()){
					btnStep7Complete.setEnabled(true);
				}
				break;
			case TYPE_EDIT_SMS_CODE:
				imageDeleteSmsVerifyCode.setVisibility(View.VISIBLE);
				if(checkStep7MailInputFull()){
					btnStep7Complete.setEnabled(true);
				}
				break;
			case TYPE_EDIT_BIND_EMAIL://手机注册，绑定邮箱
				imageDeleteEmail.setVisibility(View.VISIBLE);
				if(checkStep7PhoneInputFull()){
					btnStep7Email.setEnabled(true);
				}
				break;
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

	public boolean checkPwdInputFull(){
    	if(editInputPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}
    	return true;
    }

	//输入框内容为空
	public void showAfterTextEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_NICK_NAME:
				imageDeleteNickName.setVisibility(View.GONE);
				btnStep5Next.setEnabled(false);
				break;
			case TYPE_EDIT_BIND_PHONE_NUMBER:
				imageDeletePhoneNumber.setVisibility(View.GONE);
				btnStep7Complete.setEnabled(false);
				break;
			case TYPE_EDIT_SMS_CODE:
				imageDeleteSmsVerifyCode.setVisibility(View.GONE);
				btnStep7Complete.setEnabled(false);
				break;
			case TYPE_EDIT_BIND_EMAIL:
				imageDeleteEmail.setVisibility(View.GONE);
				btnStep7Email.setEnabled(false);
				break;
			case TYPE_EDIT_INPUT_PWD:
				imageDeleteInputPwd.setVisibility(View.GONE);
				btnCheckPwdNext.setEnabled(false);
				break;
			default:
				break;
		}
	}
	/**
	 * 判断邮箱注册步骤7是否都填写了
	 * @return
	 */
    boolean checkStep7MailInputFull(){
    	if(editBindPhone.getText().toString().isEmpty()){
			return false;
		}else if(editBindPhoneSmsCode.getText().toString().isEmpty()){
			return false;
		}
    	return true;
    }
	/**
	 * 判断手机注册步骤7是否都填写了
	 * @return
	 */
    boolean checkStep7PhoneInputFull(){
    	if(editBindEmail.getText().toString().isEmpty()){
			return false;
		}
    	return true;
    }
}
