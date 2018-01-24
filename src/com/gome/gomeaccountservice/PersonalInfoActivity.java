package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.BitmapUtils;
import com.gome.gomeaccountservice.utils.EditChangedListener;
import com.gome.gomeaccountservice.utils.EditChangedListener.TextChangeCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import citypicker.example.com.citypicker.widget.CityPicker;
import gome.app.GomeAlertDialog;
import gome.app.GomeProgressDialog;

//import com.example.citypicker.widget.CityPicker;

public class PersonalInfoActivity extends Activity implements TextChangeCallback{
	String TAG = Constants.TAG_PRE+"PersonalInfoActivity";
    private static final String PHOTO_DATE_FORMAT = "'IMG'_yyyyMMdd_HHmmss";
    private Uri mTempPhotoUri;
    Animation mAnimationOpenEnter;
    Animation mAnimationOpenExit;
    Animation mAnimationCloseEnter;
    Animation mAnimationCloseExit;

	private Bitmap mBitmap = null;  //头像的bitmap
	
    private String mToken = null;
    private String mEmailActiveStatus = null;
    private String mNickName = null;
    private String mEmailAddress = null;
    private String mPhoneNumber = null;
    private String mServerAvatarPath = null;
    private String mLocalAvatarPath = null;
    private String mLocalAvatarUri = null;
    private String mGomeId = null;
    private String mRegisterType = null;
    private String mSex = null;
    private String mBirthday = null;
    private String mArea = null;    //地区索引
    private String mAreaStr = null; //地区字符串
    private final String AREA_SPLITE = "-";
    
    GomeAlertDialog mDialog;
    CityPicker mCityPicker; //地区选择器
    TimePicker mTimePicker;  //时间选择器
    DatePicker mDatePicker;
//    AlertDialog mModifyNickNameDialog;
//    AlertDialog mModifySexDialog;
    int mYear, mMonth, mDay;
    
    HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
    
	static final int COMPRESS_THREADHOD = 1024*1024;
	final String DATE_SPLIT = "-"; //日期分隔符
	private String mImageType = "png"; //默认png
	
	HashMap<String, LinearLayout> mLayouts;
	LinearLayout layoutPersonalInfoMain;
	LinearLayout layoutCheckPwd;
	final String KEY_LAYOUT_PERSONAL_INFO_MAIN = "layoutPersonalInfoMain";
	final String KEY_LAYOUT_CHECK_PWD = "layoutCheckPwd";
	String mCurrnetStep = KEY_LAYOUT_PERSONAL_INFO_MAIN;
	
	final int REQUEST_CODE_PICK_IMAGE = 1;
	final int REQUEST_CODE_CAPTURE_CAMEIA  = 2;
	
	
	final int TYPE_EDIT_NICK_NAME = 1;
	final int TYPE_EDIT_SEX = 2;
	final int TYPE_EDIT_INPUT_PWD = 3;
	
	ImageView imageAvatar;   //显示头像界面图片
	ImageView imageDeleteNickName; //image_delete_nick_name
	ImageView imageDeleteInputPwd;
	ImageView imageHidePasswordForPassword;
	ImageView imageShowPasswordForPassword;
	Button btnCheckPwdNext;
	Button btnLoginOff;

	TextView textNickName;   //显示昵称
	TextView textAccount;   //显示账号
	TextView textSex;   	//显示性别
	TextView textBirthday;  //显示生日
	TextView textArea;  	//显示地区
	
	Button btnModifyNickName;  //
	Button btnModifySex;  //
	
	EditText editNickName; //编辑昵称
	EditText editInputPwd; //注销账号输入密码
	RadioButton radioMan;
	RadioButton radioWoman;
//	private WheelView mViewProvince;  //地区选择省份View
//	private WheelView mViewCity;      //地区选择城市View
//	private WheelView mViewDistrict;  //地区选择区View
//	AreaSelector mAreaSelector;   //地区选择器对象
	
	final int DIALOG_NICK_NAME =1;
	final int DIALOG_SEX =2;
	final int DIALOG_BIRTHDAY =3;
	final int DIALOG_LOGIN_OFF =4;
//	final int DIALOG_AREA =4;

    GomeProgressDialog mProgressDialog = null;
	private static final HandlerThread sWorkerThread = new HandlerThread("PersonalInfoActivity-loader");
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
		setContentView(R.layout.personal_info_activity);
		getAccountInfo();
		initView();
		showStep(KEY_LAYOUT_PERSONAL_INFO_MAIN);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onBackPressed() mCurrnetStep:"+mCurrnetStep+" mCityPicker:"+mCityPicker);
        if(null != mCityPicker){
            mCityPicker.hide();
        }

        if(!showPreStep(mCurrnetStep)){
			Log.i(TAG, "onBackPressed() finish()");
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy()");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			ActivityUtils.hideSetAvatarChooserDialog();
			/* modified-begin by zhiqiang.dong@gometech.com PRODUCTION-9982 */
			// first we will figure out uri
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
					//ActivityUtils.alert(this, "onActivityResult error："+e.toString());
					e.printStackTrace();
				}
				Log.i(TAG, "onActivityResult() 000 mimeType:"+mimeType);
			}
			Log.e(TAG, "onActivityResult  111  uri:" + uri+" mLocalAvatarUri:"+mLocalAvatarUri+" mImageType:"+mImageType
					+" mBitmap.getByteCount():"+mBitmap.getByteCount());
			if(null != mLocalAvatarUri && null != uri && mLocalAvatarUri.equals(uri.toString())){  //若设置的是同一图片则返回，防止多次设置相同的图片
				Log.i(TAG, "warnning !!! set the same image with uri:"+uri);
				return;
			}
			if(mBitmap.getByteCount() > COMPRESS_THREADHOD)  //若获取的bitmap大小超过阀值则压缩为原来的100分之一
				mBitmap = BitmapUtils.compressBitmap(mBitmap);//图像缩放宽高设置为原来的10分之一
			Log.e(TAG, "onActivityResult  222 mBitmap.getByteCount():"+mBitmap.getByteCount());
			mBitmap = BitmapUtils.imageCropSquare(mBitmap, true);
			Log.e(TAG, "onActivityResult  333 mBitmap.getByteCount():"+mBitmap.getByteCount());
//			imageAvatar.setImageBitmap(mBitmap);
//			imageAvatar.setImageBitmap(mBitmap);
			uploadLocalImageToServerInThread(mBitmap,null == uri? null:uri.toString());
		/* modified-begin by zhiqiang.dong@gometech.com PRODUCTION-4889 */
		// whaterver resultCode is ok or not ok, we should reset mTempPhotoUri
		} else {
			mTempPhotoUri = null;
		}
		/* modified-end */
	}
	public void getAccountInfo() {
    	Account gomeAccount = AccountUtils.getLatestGomeAccount(this); //获取本地账号
    	if(null != gomeAccount){
	    	HashMap<String,String> mTableAccountInfo = AccountUtils.getAccountDetailFromLocal(this,gomeAccount.name); 
	    	mToken = mTableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
	    	mEmailActiveStatus = mTableAccountInfo.get(Constants.KEY_EMAIL_ACTIVE_STATUS);
	    	mGomeId = mTableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
		    mNickName = mTableAccountInfo.get(Constants.KEY_ACCOUNT_NICK_NAME);
		    mEmailAddress = mTableAccountInfo.get(Constants.KEY_ACCOUNT_EMAIL);
		    mPhoneNumber = mTableAccountInfo.get(Constants.KEY_ACCOUNT_PHONE_NUMBER);
		    mServerAvatarPath = mTableAccountInfo.get(Constants.KEY_ACCOUNT_SERVER_AVATAR_PATH);
		    mLocalAvatarPath = mTableAccountInfo.get(Constants.KEY_LOCAL_AVATAR_PATH);
		    mLocalAvatarUri = mTableAccountInfo.get(Constants.KEY_LOCAL_AVATAR_URI);
		    mRegisterType = mTableAccountInfo.get(Constants.KEY_ACCOUNT_REGISTER_TYPE);
		    mSex = mTableAccountInfo.get(Constants.KEY_ACCOUNT_SEX);
		    mBirthday = mTableAccountInfo.get(Constants.KEY_ACCOUNT_BIRTHDAY);
		    mArea = mTableAccountInfo.get(Constants.KEY_ACCOUNT_AREA);
            mAreaStr = getAreaStr(mArea);
		    Log.i(TAG, "getInfo() 11 mNickName:"+mNickName+" mEmailAddress:"+mEmailAddress+" mPhoneNumber:"+mPhoneNumber
		    		+" mLocalAvatarPath:"+mLocalAvatarPath+" mLocalAvatarUri:"+mLocalAvatarUri+" mArea:"+mArea+" mAreaStr:"+mAreaStr);
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
					Log.i(TAG, "getInfo() error:"+e.toString());
					e.printStackTrace();
				}
		    }else{
		    	Log.i(TAG, "getInfo() cannot find image path："+mLocalAvatarPath);
		    	return;
		    }
		    Log.i(TAG, "getInfo() 222 fis:"+fis);
		    mBitmap  = BitmapFactory.decodeStream(fis);
	        Log.i(TAG, "getInfo() 333 mHeadBg:"+mBitmap);
    	}
    }
	void initView(){
		Log.i(TAG, "initView() radioMan:"+radioMan+" radioWoman:"+radioWoman+" mAreaStr:"+mAreaStr);
        mAnimationOpenEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_enter_layout);
        mAnimationOpenExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_open_exit_layout);
        mAnimationCloseEnter = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_enter_layout);
        mAnimationCloseExit = AnimationUtils.loadAnimation(this, R.anim.gome_activity_close_exit_layout);

		layoutPersonalInfoMain = (LinearLayout)findViewById(R.id.layout_personal_info_main);
		layoutCheckPwd = (LinearLayout)findViewById(R.id.layout_public_chekc_pwd);
		mLayouts = new HashMap<String, LinearLayout>();
		mLayouts.put(KEY_LAYOUT_PERSONAL_INFO_MAIN, layoutPersonalInfoMain);
		mLayouts.put(KEY_LAYOUT_CHECK_PWD, layoutCheckPwd);
		
		imageAvatar = (ImageView)findViewById(R.id.image_avatar);
		textNickName = (TextView)findViewById(R.id.text_nick_name);
		textAccount = (TextView)findViewById(R.id.text_account);
		textSex = (TextView)findViewById(R.id.text_sex);
		textBirthday = (TextView)findViewById(R.id.text_birthday);
		textArea = (TextView)findViewById(R.id.text_area);
		editInputPwd = (EditText)findViewById(R.id.edit_input_password);
		editInputPwd.addTextChangedListener(new EditChangedListener(TYPE_EDIT_INPUT_PWD,this));
        editInputPwd.setTypeface(Typeface.DEFAULT);
		imageDeleteInputPwd = (ImageView)findViewById(R.id.image_delete_input_password);
		imageHidePasswordForPassword = (ImageView)findViewById(R.id.image_hide_password_for_password);
		imageShowPasswordForPassword = (ImageView)findViewById(R.id.image_show_password_for_password);
		btnCheckPwdNext = (Button)findViewById(R.id.btn_public_check_pwd_next);
        btnLoginOff = (Button)findViewById(R.id.btn_login_off);

		if(null != mBitmap){
			imageAvatar.setImageBitmap(mBitmap);
		}
		if(null != mNickName && !mNickName.isEmpty()) { //非空才设置显示昵称
			textNickName.setText(mNickName);
		}else{
			String strNoNickName = getResources().getString(R.string.txt_not_set_nickname);
			textNickName.setText(strNoNickName);
		}
		if(null != mGomeId){//账号暂时先显示gomeid，需和ui确认
			textAccount.setText(mGomeId);
		}
		if(null != mSex){
			textSex.setText(mSex);
			if(Constants.SEX_WOMAN.equals(mSex)){
				textSex.setText(getResources().getString(R.string.txt_sex_woman));
			}else{
				textSex.setText(getResources().getString(R.string.txt_sex_man));
			}
		}else{
			textSex.setText(getResources().getString(R.string.txt_not_set));
		}
		if(null != mBirthday){
			textBirthday.setText(mBirthday);
		}else{
            textBirthday.setText(getResources().getString(R.string.txt_not_set));
        }
		if(null != mAreaStr && !mAreaStr.isEmpty()){
			textArea.setText(mAreaStr);
		}else{
            textArea.setText(getResources().getString(R.string.txt_area_not_set));
        }

		
	}
	/**
	 * 
	 * @param keyLayout
	 * @return 是否有上一步界面，若没有则返回false
	 */
	boolean showPreStep(String keyLayout){
		Log.i(TAG, "showPreStep() keyLayout:"+keyLayout);
		if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)){
			mCurrnetStep = KEY_LAYOUT_PERSONAL_INFO_MAIN;
		}else{
			return false;
		}
		showStep(mCurrnetStep,true);
		return true;
	}
	/**
	 * 显示的布局
	 * @param keyLayout
	 */
	void showStep(String keyLayout,boolean...backMode){
		mCurrnetStep = keyLayout;
		ActivityUtils.closeKeyboard(this);
        boolean isBackMode = false;
        boolean isUseAnimation = true;
        if(null != backMode &&  0 != backMode.length){
            isBackMode = true;
        }
        if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)){
            btnLoginOff.setEnabled(false);
        }
        if(KEY_LAYOUT_PERSONAL_INFO_MAIN.equals(keyLayout) && !isBackMode){  //从activity加载的第一个界面无需再加动画
            isUseAnimation = false;
        }
        Log.i(TAG, "showStep ** keyLayout:"+keyLayout+" mLayouts.size():"+ (null == mLayouts ?"null":mLayouts.size())
                +" isBackMode:"+isBackMode+" isUseAnimation:"+isUseAnimation);
		if(null != mLayouts){
			if(KEY_LAYOUT_PERSONAL_INFO_MAIN.equals(keyLayout)){//主界面不显示软键盘
				//ActivityUtils.closeKeyboard(PersonalInfoActivity.this);
			}
            if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)){ //进入输入密码界面先清除密码数据
                editInputPwd.setText("");
            }
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
                                       if(KEY_LAYOUT_CHECK_PWD.equals(keyLayout)){
                                           btnLoginOff.setEnabled(true);
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
	//显示性别radio,默认是男，
	void setSexRadio(String sex){
		if(Constants.SEX_WOMAN.equals(sex)){
			radioWoman.setChecked(true);
			radioMan.setChecked(false);
		}else{
			radioWoman.setChecked(false);
			radioMan.setChecked(true);
		}
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
				if(AccountUtils.verifyPwdOrSmsCodeFromServer(PersonalInfoActivity.this, mTableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							showDialog(PersonalInfoActivity.this, DIALOG_LOGIN_OFF);
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
	//注销账号
	void loginOffInThread(){
		Log.i(TAG, "loginOffInThread()");
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				loginOff();
			}
		});
	}
	void loginOff(){
		String pwd = editInputPwd.getText().toString().trim();
		Log.i(TAG, "loginOff() mToken:"+mToken+" pwd:"+pwd);
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, pwd);
		if(AccountUtils.loginOff(PersonalInfoActivity.this,mTableAccountInfo)){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(PersonalInfoActivity.this,LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PersonalInfoActivity.this.startActivity(i);
                    finish();
                }
            });

		}else{
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showStep(KEY_LAYOUT_PERSONAL_INFO_MAIN);
                }
            });
		}
	}
	///////////////////点击事件处理begin
	//设置密码界面点击返回
	public void doOnclickPublicCheckPwdButton(View view){
		showStep(KEY_LAYOUT_PERSONAL_INFO_MAIN);
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
	public void doOnclickLoginOffButton(View view){
		Log.i(TAG, "doOnclickLoginOffButton() ");
		showStep(KEY_LAYOUT_CHECK_PWD);
	}
	//
	public void doOnlcikPublicCheckPwdNextButton(View view){
		verifyPwdFromServer();
	}
	
	public void doOnclickPersonalInfoBackButton(View view){
		Log.i(TAG, "doOnclickPersonalInfoBackButton() ");
		onBackPressed();
	}
	//点击头像条目
	public void doOnclickAvatarItem(View view){
        hideAllDialog();
		if(ActivityUtils.checkNetwork(this)){
			ActivityUtils.showSetAvatarChooserDialog(PersonalInfoActivity.this);
		}
	}
	//点击昵称条目
	public void doOnclickNickNameItem(View view){
		//showModifyNickNameDialog(PersonalInfoActivity.this);
		showDialog(PersonalInfoActivity.this, DIALOG_NICK_NAME);
	}
	//点击账号条目
	public void doOnclickGomeIdItem(View view){
		//
	}
	//点击性别条目
	public void doOnclickSexItem(View view){
		//showModifySexDialog(PersonalInfoActivity.this);
		showDialog(PersonalInfoActivity.this, DIALOG_SEX);
	}
	//点击生日条目
	public void doOnclickBirthdayItem(View view){
		showDialog(PersonalInfoActivity.this, DIALOG_BIRTHDAY);
	}
	
	//点击地区条目
	public void doOnclickAreaItem(View view){
//		showDialog(PersonalInfoActivity.this,DIALOG_AREA);
		String province = "";
		String city = "";
		String district = "";
        Log.i(TAG,"doOnclickAreaItem() mArea:"+mArea+" mAreaStr:"+mAreaStr+" mDialog:"+mDialog+" mCityPicker:"+mCityPicker);
        hideAllDialog();//规避同时点击多个条目的问题

		if(null != mAreaStr && mAreaStr.split(AREA_SPLITE).length == 3){
			province = mAreaStr.split(AREA_SPLITE)[0];
			city = mAreaStr.split(AREA_SPLITE)[1];
			district  = mAreaStr.split(AREA_SPLITE)[2];
		}
        if(null != mAreaStr && mAreaStr.split(AREA_SPLITE).length == 2){
            province = mAreaStr.split(AREA_SPLITE)[0];
            city = mAreaStr.split(AREA_SPLITE)[1];
        }

        mCityPicker = new CityPicker.Builder(this)
				.textSize(15)
				.province(province)
				.city(city)
				.district(district)
				.build();
        mCityPicker.show();
        mCityPicker.setOnCityItemClickListener(new CityPicker.OnCityItemClickListener() {
			@Override
			public void onSelected(String... strings) {
                try {
                    Log.i(TAG,"onSelected() strings[]");
                    for(int i=0;i<strings.length;i++){
                        Log.i(TAG,"onSelected() strings["+i+"]:"+strings[i]);
                    }
                    String[] indexes = strings[3].split(",");
                    mArea = indexes[0] + AREA_SPLITE + indexes[1] + AREA_SPLITE + indexes[2];//存储index 中英文都存储三个
                    if ("en".equals(ActivityUtils.getCurrentLanguageStr(PersonalInfoActivity.this))) {
                        mAreaStr = strings[0] + AREA_SPLITE + strings[1]; //英文显示只显示两位，省市
                    } else {
                        mAreaStr = strings[0] + AREA_SPLITE + strings[1] + AREA_SPLITE + strings[2];
                    }
                }catch (Exception e){
                    Log.i(TAG,"onSelected() e:"+e.toString());
                    e.printStackTrace();
                }
                modifyArea();
                mCityPicker.hide();
			}

			@Override
			public void onCancel() {
			}
		});
	}
	//点击从图片获取照片信息
	public void doOnclickGalleryButton(View view){
		Log.i(TAG, "doOnclickGalleryButton() ");
		Intent intent = new Intent(Intent.ACTION_PICK);  
        intent.setType("image/*");//相片类型  
        Bundle b = new Bundle();
        b.putString("crop", "crop");
        intent.putExtras(b);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        ActivityUtils.hideSetAvatarChooserDialog();
	}
	/* modified-begin by zhiqiang.dong@gometech.com PRODUCTION-9982 */
	//点击从相机获取照片信息
	public void doOnclickCameraButton(View view){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		mTempPhotoUri = generateTempImageUri(this);
        Log.d(TAG, "doOnclickCameraButton() mTempPhotoUri:"+mTempPhotoUri.toString());
		addPhotoPickerExtras(intent, mTempPhotoUri);
		intent.putExtra("head_photo_from_camera","yes");
		startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
        ActivityUtils.hideSetAvatarChooserDialog();
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

	/* modified-end */
	//弹框 点击删除 修改昵称
	public void doOnclickClearEditNickNameButton(View view){
		editNickName.setText("");
	}
	//修改昵称点击确定按钮
	public void doOnclickModifyNickNameOkButton(View view){
		mNickName = editNickName.getText().toString().trim();

		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
		mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_NICK_NAME, mNickName);
		btnModifyNickName.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "doOnclickModifyNickNameOkButton() 111 mNickName:"+mNickName+" mToken:"+mToken);
				if(AccountUtils.modifyAccountInfoFromServer(PersonalInfoActivity.this,mTableAccountInfo)){
					mHandler.post(new Runnable() { //ui线程
						@Override
						public void run() {
                            if(null != mNickName && !mNickName.isEmpty()) { //非空才设置显示昵称
                                textNickName.setText(mNickName);
                            }else{
                                String strNoNickName = getResources().getString(R.string.txt_not_set_nickname);
                                textNickName.setText(strNoNickName);
                            }
							dismissDialog();
						}
					});

				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						btnModifyNickName.setEnabled(true);
					}
				});
			}
		});

	}
	//修改昵称点击取消按钮
	public void doOnclickModifyNickNameCancelButton(View view){
		dismissDialog();
	}
	
	public void doOnclickSexManItem(View view){
		Log.i(TAG, "doOnclickSexManItem() 111");
		setSexRadio(Constants.SEX_MAN);
		Log.i(TAG, "doOnclickSexManItem() mSex:"+mSex+" radioWoman.isChecked():"+radioWoman.isChecked()+" radioMan.isChecked():"+radioMan.isChecked());
		modifySex();
		dismissDialog();
	}
	
	public void doOnclickSexWomanItem(View view){
		Log.i(TAG, "doOnclickSexWomanItem() 111");
		setSexRadio(Constants.SEX_WOMAN);
		Log.i(TAG, "doOnclickSexWomanItem() mSex:"+mSex+" radioWoman.isChecked():"+radioWoman.isChecked()+" radioMan.isChecked():"+radioMan.isChecked());
		modifySex();
		dismissDialog();
	}
	//修改性别 点击确定按钮
	/*public void doOnclickModifySexOkButton(View view){
		modifySex();
	}
	
	//修改性别点击取消按钮
	public void doOnclickModifySexCancelButton(View view){
		Log.i(TAG, "doOnclickModifySexCancelButton()");
		dismissDialog();
	}*/
	public void doOnclickModifyAreaOkButton(View view){
		modifyArea();
	}
	public void doOnclickModifyAreaCancelButton(View view){
		Log.i(TAG, "doOnclickModifyAreaCancelButton()");
		dismissDialog();
	}
	///////////////////点击事件处理end
	//修改性别
	void modifySex(){
		final String sex;
		if(radioWoman.isChecked()){
			sex = Constants.SEX_WOMAN;
		}else{
			sex = Constants.SEX_MAN;
		}
		//btnModifySex.setEnabled(false);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_SEX, sex);
				Log.i(TAG, "modifySex() 111 sex:"+sex+" mToken:"+mToken);
				if(AccountUtils.modifyAccountInfoFromServer(PersonalInfoActivity.this,mTableAccountInfo)){//只有服务端设置成功才可以更新全局变量
					mHandler.post(new Runnable() {//ui线程
						@Override
						public void run() {
							if(radioWoman.isChecked()){
								mSex = Constants.SEX_WOMAN;
								textSex.setText(getResources().getString(R.string.txt_sex_woman));
							}else{
								mSex = Constants.SEX_MAN;
								textSex.setText(getResources().getString(R.string.txt_sex_man));
							}
							Log.i(TAG, "modifySex() 222 mSex:"+mSex+" radioWoman.isChecked():"+radioWoman.isChecked()+" radioMan.isChecked():"+radioMan.isChecked());
							dismissDialog();
						}
					});
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						//btnModifySex.setEnabled(true);
					}
				});
			}
		});
	}
	//修改地区
	void modifyArea(){
		Log.i(TAG, "modifyArea() mArea:"+mArea);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				mTableAccountInfo.clear();
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
				mTableAccountInfo.put(Constants.KEY_ACCOUNT_AREA, mArea);
				Log.e(TAG, "modifyArea() 111** mArea:"+mArea+" mAreaStr:"+mAreaStr+" mToken:"+mToken);
				if(AccountUtils.modifyAccountInfoFromServer(PersonalInfoActivity.this,mTableAccountInfo)){//只有服务端设置成功才可以更新全局变量
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							textArea.setText(mAreaStr);
							dismissDialog();
						}
					});
				}
			}
		});
	}

    /**
     * 根据索引获取地区字符串
     * @param areaIndexStr
     * @return
     */
	String getAreaStr(String areaIndexStr){
        String areaStr = "";
        CityPicker cityPicker = new CityPicker(PersonalInfoActivity.this);
        Map<String, String> map;
        Log.i(TAG,"getAreaStr() areaIndexStr:"+areaIndexStr);
        if(null == areaIndexStr || areaIndexStr.isEmpty()){
            return "";
        }
        String[] indexes = areaIndexStr.split(AREA_SPLITE);
        try {
            map = cityPicker.getProvincialInfo(Integer.parseInt(indexes[0]), Integer.parseInt(indexes[1]), Integer.parseInt(indexes[2]));//中文显示三行
            if ("en".equals(ActivityUtils.getCurrentLanguageStr(PersonalInfoActivity.this))) {
                Log.i(TAG,"getAreaStr() map.get(\"province\"):"+map.get("province")+" map.get(\"city\"):"+map.get("city"));
                areaStr = map.get("province") + AREA_SPLITE + map.get("city");
            } else {
                Log.i(TAG,"getAreaStr() map.get(\"province\"):"+map.get("province")+" map.get(\"city\"):"+map.get("city")
                +"  map.get(\"district\"):"+ map.get("district"));
                areaStr = map.get("province") + AREA_SPLITE + map.get("city") + AREA_SPLITE + map.get("district");
            }
        }catch (Exception e){
            Log.i(TAG,"getAreaStr() e:"+e.toString());
            e.printStackTrace();
        }
        Log.i(TAG,"getAreaStr() areaIndexStr:"+areaIndexStr+" areaStr:"+areaStr);
        return areaStr;
    }
	/////////////显示弹出框 begin//////////
    /**
     *     隐藏所有打开的dialog
     */
	void hideAllDialog(){
        Log.i(TAG,"hideAllDialog() mDialog:"+mDialog+" mCityPicker:"+mCityPicker);
        if(null != mDialog){
            mDialog.cancel();
        }
        if(null != mCityPicker){
            mCityPicker.hide();
        }
        ActivityUtils.hideSetAvatarChooserDialog();
    }
	//弹出框
	void showDialog(Context context,int dialogId){
        hideAllDialog();
    	LayoutInflater inflater =  LayoutInflater.from(context);
    	View layout = null;
		switch (dialogId) {
			case DIALOG_NICK_NAME:
		    	layout = inflater.inflate(R.layout.alert_modify_nickname_layout,null);
				editNickName = (EditText)layout.findViewById(R.id.edit_nick_name);
				imageDeleteNickName = (ImageView)layout.findViewById(R.id.image_delete_nick_name);
				btnModifyNickName = (Button)layout.findViewById(R.id.btn_modify_nick_name);
				editNickName.addTextChangedListener(new EditChangedListener(TYPE_EDIT_NICK_NAME,this));
				editNickName.setText(mNickName);
                editNickName.setSelection(editNickName.getText().length());
				Log.i(TAG, "showDialg() DIALOG_NICK_NAME  editNickName:"+editNickName);
				break;
			case DIALOG_SEX:
		    	layout = inflater.inflate(R.layout.alert_modify_sex_layout,null);
				radioMan = (RadioButton)layout.findViewById(R.id.radio_man);
				radioWoman = (RadioButton)layout.findViewById(R.id.radio_woman);
				//btnModifySex = (Button)layout.findViewById(R.id.btn_modify_sex);
				setSexRadio(mSex);
		    	Log.i(TAG, "showDialg() DIALOG_SEX  radioWoman:"+radioWoman);
				break;
			case DIALOG_BIRTHDAY:  //日期使用系统dialog
				
				Log.i(TAG, "showDialg() DIALOG_BIRTHDAY  mBirthday:"+mBirthday);
				if(null != mBirthday){
					String str [] = mBirthday.split(DATE_SPLIT);
					if(str.length == 3){
						mYear = Integer.parseInt(str[0]);
						mMonth = Integer.parseInt(str[1])-1;//系统日历的月份的值从0开始需将月的值减1
						mDay = Integer.parseInt(str[2]);
					}
				}else{//设置默认显示的年月日
                    Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);
                }
				View mView = LayoutInflater.from(PersonalInfoActivity.this).inflate(R.layout.alertdialog_content, null);
                
                GomeAlertDialog.Builder gomeAlertBuilder = new GomeAlertDialog.Builder(PersonalInfoActivity.this);
                gomeAlertBuilder.setTitle(R.string.date_selector);
                gomeAlertBuilder.setView(mView);
                gomeAlertBuilder.setPositiveButton(R.string.confirm,null);
                gomeAlertBuilder.setNegativeButton(R.string.cancel, new NegativeButtonOnclickListener());
                mDialog = gomeAlertBuilder.create();
                mDialog.show();
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //如果想关闭dialog直接加上下面这句代码就行
                        Calendar calendarNow = Calendar.getInstance();
                        Calendar calendarDatePicker = Calendar.getInstance();
                        calendarDatePicker.set(mDatePicker.getYear(),mDatePicker.getMonth(),mDatePicker.getDayOfMonth());
                        Log.i(TAG,"onClick() calendarDatePicker getYear:"+mDatePicker.getYear()+" getMonth:"+mDatePicker.getMonth()
                            +" getDayOfMonth:"+mDatePicker.getDayOfMonth()+" calendarNow getYear:"+calendarNow.get(Calendar.YEAR)
                                +" getMonth:"+calendarNow.get(Calendar.MONTH)   +" getDayOfMonth:"+calendarNow.get(Calendar.DAY_OF_MONTH)
                        +" calendarNow.getTimeInMillis():"+calendarNow.getTimeInMillis()+" calendarDatePicker.getTimeInMillis():"+calendarDatePicker.getTimeInMillis());
                        if(calendarNow.getTimeInMillis() >= calendarDatePicker.getTimeInMillis()) {
                            updateDateShow(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
                            mDialog.cancel();
                        }else{
                            ActivityUtils.alert(PersonalInfoActivity.this,getResources().getString(R.string.alert_birthday_early_than_now));
                        }
                    }
                });
				mDatePicker = (DatePicker)mView.findViewById(R.id.datePicker);
				mDatePicker.init(mYear, mMonth, mDay, null);
//				new DatePickerDialog(this, mdateListener, mYear, mMonth, mDay).show();
				return;  //国美dialog直接返回
			case DIALOG_LOGIN_OFF:
				Log.i(TAG, "showDialg() DIALOG_LOGIN_OFF");
                mDialog = new GomeAlertDialog.Builder(PersonalInfoActivity.this)
                    .setTitle(getResources().getString(R.string.txt_login_off_account))
					.setMessage(getResources().getString(R.string.txt_login_off_account_prompt))
	                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							loginOffInThread();//执行注销
						}
					})
	                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showStep(KEY_LAYOUT_PERSONAL_INFO_MAIN);
						}
					})
	                .show();
				return;//国美dialog直接返回
/*			case DIALOG_AREA:
		    	layout = inflater.inflate(R.layout.alert_modify_area_layout,null);
		    	mViewProvince = (WheelView)layout.findViewById(R.id.id_province);
		    	mViewCity = (WheelView)layout.findViewById(R.id.id_city);
		    	mViewDistrict = (WheelView)layout.findViewById(R.id.id_district);
		    	Log.e(TAG,"showDialog() DIALOG_AREA mViewProvince:"+mViewProvince);
		    	//for test begin  使用资源
		    	//mAreaSelector = new AreaSelector(PersonalInfoActivity.this, mViewProvince, mViewCity, mViewDistrict);
		    	mAreaSelector = new AreaSelector(PersonalInfoActivity.this, mViewProvince, mViewCity, mViewDistrict,R.layout.area_item_layout,R.id.area_item_textview);
		    	Log.i(TAG, "showDialg() DIALOG_AREA  set title color");
//		    	TextView tv = (TextView)layout.findViewById(R.id.text_modify_area_title);
//		    	tv.setTextColor(getResources().getColor(R.color.color_button_link));
		    	//for test end
		    	if(null != mArea && mArea.split(AREA_SPLITE).length == 3){
		    		mAreaSelector.initView(mArea.split(AREA_SPLITE)[0],mArea.split(AREA_SPLITE)[1],mArea.split(AREA_SPLITE)[2]);
		    	}else{
		    		mAreaSelector.initView("","","");  //先调用初始化数据接口，完成地区数据填充。
		    	}  
				break; */
			default:
				break;
		}
		mDialog = new GomeAlertDialog.Builder(context).setView(layout).create();
        Window window = mDialog.getWindow();
    	mDialog.show();
        if(DIALOG_NICK_NAME == dialogId){
            ActivityUtils.showAlertDialogKeyboard(mDialog);
        }
	}
	void dismissDialog(){
		Log.i(TAG, "dismissDialog()");
    	if(null != mDialog){
    		mDialog.dismiss();
    	}
	}
    class  PositiveButtonOnclickListener implements DialogInterface.OnClickListener{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			Log.i(TAG, "PositiveButtonOnclickListener.onClick()");
			updateDateShow(mDatePicker.getYear(),mDatePicker.getMonth(),mDatePicker.getDayOfMonth());
			
		}
    	
    }
    class  NegativeButtonOnclickListener implements DialogInterface.OnClickListener{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// do nothing
			
		}
    	
    }
    void updateDateShow(int year,int monthOfYear,int dayOfMonth){
    	mYear = year;
        mMonth = monthOfYear+1;//从0开始需+1
        mDay = dayOfMonth;
        Log.i(TAG, "updateDateShow mYear:"+mYear+" mMonth:"+mMonth+" mDay:"+mDay);
        mBirthday = mYear+DATE_SPLIT+mMonth+DATE_SPLIT+mDay;
        
		mTableAccountInfo.clear();
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
		mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
		mTableAccountInfo.put(Constants.KEY_ACCOUNT_BIRTHDAY, mBirthday);
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "updateDateShow() 111 mBirthday:"+mBirthday+" mToken:"+mToken);
	            if(AccountUtils.modifyAccountInfoFromServer(PersonalInfoActivity.this, mTableAccountInfo)){
	            	mHandler.post(new Runnable() {
						@Override
						public void run() {
			            	textBirthday.setText(mBirthday);
						}
					});
	            }
			}
		});
    }
	private DatePicker.OnDateChangedListener mdateListener2 = new DatePicker.OnDateChangedListener(){
		@Override
		public void onDateChanged(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
            mMonth = monthOfYear+1;//从0开始需+1
            mDay = dayOfMonth;
            Log.i(TAG, "onDateChanged mYear:"+mYear+" mMonth:"+mMonth+" mDay:"+mDay);
            mBirthday = mYear+DATE_SPLIT+mMonth+DATE_SPLIT+mDay;
            
    		mTableAccountInfo.clear();
    		mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
			mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
    		mTableAccountInfo.put(Constants.KEY_ACCOUNT_BIRTHDAY, mBirthday);
    		sWorker.post(new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "OnDateChangedListener() 111 mBirthday:"+mBirthday+" mToken:"+mToken);
		            if(AccountUtils.modifyAccountInfoFromServer(PersonalInfoActivity.this, mTableAccountInfo)){
		            	mHandler.post(new Runnable() {
							@Override
							public void run() {
				            	textBirthday.setText(mBirthday);
							}
						});
		            }
				}
			});
			
		}
		
	};
	private DatePickerDialog.OnDateSetListener mdateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear+1;//从0开始需+1
            mDay = dayOfMonth;
            Log.i(TAG, "onDateSet mYear:"+mYear+" mMonth:"+mMonth+" mDay:"+mDay);
            mBirthday = mYear+DATE_SPLIT+mMonth+DATE_SPLIT+mDay;
            
    		mTableAccountInfo.clear();
    		mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
			mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
    		mTableAccountInfo.put(Constants.KEY_ACCOUNT_BIRTHDAY, mBirthday);
    		sWorker.post(new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "doOnclickModifyNickNameOkButton() 111 mBirthday:"+mBirthday+" mToken:"+mToken);
		            if(AccountUtils.modifyAccountInfoFromServer(PersonalInfoActivity.this, mTableAccountInfo)){
		            	mHandler.post(new Runnable() {
							@Override
							public void run() {
				            	textBirthday.setText(mBirthday);
							}
						});
		            }
				}
			});
            //display();
        }

    };
	/////////////显示弹出框 end//////////
    
	//抽出到公共类     设置输入框输入监听
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
    
    boolean checkPwdInputFull(){
    	if(editInputPwd.getText().toString().trim().isEmpty()){
    		return false;
    	}
    	return true;
    }
	//输入框内容非空
	public void showAfterTextNotEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_NICK_NAME:
				imageDeleteNickName.setVisibility(View.VISIBLE);
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
	//输入框内容为空
	public void showAfterTextEmpty(int editType){
		switch (editType) {
			case TYPE_EDIT_NICK_NAME:
				imageDeleteNickName.setVisibility(View.GONE);
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
     * 上传本地头像到服务器,使用新的线程
     * @param bitMap
     */
    void uploadLocalImageToServerInThread(final Bitmap bitMap,final String strUri){
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
				tableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
				tableAccountInfo.put(Constants.KEY_AVATAR_TYPE, mImageType);//需要根据实际图片设置 by lilei
				tableAccountInfo.put(Constants.KEY_ACCOUNT_SERVER_AVATAR, BitmapUtils.bitmap2StrByBase64(bitMap));
				tableAccountInfo.put(Constants.KEY_LOCAL_AVATAR_URI,strUri);
				if(AccountUtils.uploadLocalImageToServer(PersonalInfoActivity.this, bitMap, tableAccountInfo)){
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Log.i(TAG, "uploadLocalImageToServerInThread() finish update avatar ui!! mBitmap:"+mBitmap+" bitMap:"+bitMap);
							imageAvatar.setImageBitmap(bitMap);
							getAccountInfo();//上传完成头像后更新本地参数
						}
					});
				}
				mHandler.post(new Runnable() {  //上传完成关闭等待框
					@Override
					public void run() {
                        try {
                            mProgressDialog.dismiss();
                        }catch (Exception ex){
                            Log.i(TAG,"mProgressDialog.dismiss error:"+ex.toString());
                        }
					}
				});
			}
		});
    }
}
