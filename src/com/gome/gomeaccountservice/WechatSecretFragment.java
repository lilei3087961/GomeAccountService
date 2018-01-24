package com.gome.gomeaccountservice;

import android.accounts.Account;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gome.gomeaccountservice.utils.AccountUtils;
import com.gome.gomeaccountservice.utils.ActivityUtils;
import com.gome.gomeaccountservice.utils.BitmapUtils;
import com.gome.gomeaccountservice.utils.NetworkUtilities;
import gome.app.GomeProgressDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class WechatSecretFragment extends Fragment {
    static final String TAG = Constants.TAG_PRE + "WechatSecretFragment";
    private View view = null;
    private ImageView backIcon;
    private TextView textPhoneNumber;
    private EditText secretOne;
    private EditText secretTwo;
    private ImageView deleteSecretOne;
    private ImageView deleteSecretTwo;
    private ImageView hideSecretOne;
    private ImageView hideSecretTwo;
    private ImageView showSecretOne;
    private ImageView showSecretTwo;
    private boolean isSecretOneEmpty = true;
    private boolean isSecretTwoEmpty = true;
    private Button finishBtn;
    private String phoneNumber = null; //phone numbrt
    private String mLoginPwd = null; //secret number
    private String mThirdType = "2";
    private String mThirdId = null;
    private String wechatHeadUrl;
    private String wechatNickName;
    private String mToken;
    private String mGomeId;
    private String mImageType = "jpg"; //默认jpg
    //private ProgressDialog mProgressDialog = null;
    private GomeProgressDialog mProgressDialog = null;
    private static final HandlerThread sWorkerThread = new HandlerThread("RegisterActivity-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private static final Handler mHandler = new Handler();
    HashMap<String,String> mTableAccountInfo = new HashMap<String, String>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.wechat_secret_fragment, null);
        phoneNumber = ((BindPhoneFragment) getActivity().getFragmentManager().findFragmentByTag("bindPhoneFragment")).getPhoneNumber();
        mThirdId = ((BindPhoneActivity)getActivity()).getWechatId();
        wechatHeadUrl = ((BindPhoneActivity)getActivity()).getWechatHeadImageUrl();
        wechatNickName = ((BindPhoneActivity)getActivity()).getWechatNickName();
        Log.i(TAG,"headimageurl:" + wechatHeadUrl + "wechat name:" + wechatNickName);
        Log.i(TAG,"phoneNumber:" + phoneNumber);
        backIcon = (ImageView)view.findViewById(R.id.head_left_arrow);
        textPhoneNumber = (TextView) view.findViewById(R.id.text_phone_number);
        secretOne = (EditText) view.findViewById(R.id.edit_input_password_for_phone);
        secretOne.setTypeface(Typeface.DEFAULT);
        secretTwo = (EditText) view.findViewById(R.id.edit_input_password_again_for_phone);
        secretTwo.setTypeface(Typeface.DEFAULT);
        deleteSecretOne = (ImageView) view.findViewById(R.id.image_delete_input_password_for_phone);
        deleteSecretTwo = (ImageView) view.findViewById(R.id.image_delete_input_password_again_for_phone);
        hideSecretOne = (ImageView) view.findViewById(R.id.image_hide_password_for_password_phone);
        hideSecretTwo = (ImageView) view.findViewById(R.id.image_hide_password_for_password_again_phone);
        showSecretOne = (ImageView) view.findViewById(R.id.image_show_password_for_password_phone);
        showSecretTwo = (ImageView) view.findViewById(R.id.image_show_password_for_password_again_phone);
        finishBtn = (Button) view.findViewById(R.id.btn_step2_phone_complete);
        textPhoneNumber.setText(ActivityUtils.convertPhoneNumShow(phoneNumber));
        backIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ActivityUtils.closeKeyboard(getActivity());
                getActivity().finish();
            }
        });

        finishBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(isTwoSecretSame()) {
                    mLoginPwd = secretOne.getText().toString();
                    doStep2Register();
                }
            }
        });

        hideSecretOne.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                secretOne.setTransformationMethod(PasswordTransformationMethod.getInstance());
                hideSecretOne.setVisibility(View.GONE);
                showSecretOne.setVisibility(View.VISIBLE);
            }
        });

        hideSecretTwo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                secretTwo.setTransformationMethod(PasswordTransformationMethod.getInstance());
                hideSecretTwo.setVisibility(View.GONE);
                showSecretTwo.setVisibility(View.VISIBLE);
            }
        });

        showSecretOne.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                secretOne.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showSecretOne.setVisibility(View.GONE);
                hideSecretOne.setVisibility(View.VISIBLE);
            }
        });

        showSecretTwo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                secretTwo.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showSecretTwo.setVisibility(View.GONE);
                hideSecretTwo.setVisibility(View.VISIBLE);
            }
        });

        deleteSecretOne.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                secretOne.setText("");
            }
        });

        deleteSecretTwo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                secretTwo.setText("");
            }
        });

        secretOne.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                Log.i(TAG,"phonenum:" + s);
                // TODO Auto-generated method stub
                if (s.length() > 0) {
                    isSecretOneEmpty = false;
                    deleteSecretOne.setVisibility(View.VISIBLE);
                } else {
                    isSecretOneEmpty = true;
                    deleteSecretOne.setVisibility(View.INVISIBLE);
                }
                if (isBothTwoSecretNotEmpty()) {
                    finishBtn.setEnabled(true);
                } else {
                    finishBtn.setEnabled(false);
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
        secretTwo.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                Log.i(TAG,"verifynum:" + s);
                // TODO Auto-generated method stub
                if (s.length() > 0) {
                    isSecretTwoEmpty = false;
                    deleteSecretTwo.setVisibility(View.VISIBLE);
                } else {
                    isSecretTwoEmpty = true;
                    deleteSecretTwo.setVisibility(View.INVISIBLE);
                }
                if (isBothTwoSecretNotEmpty()) {
                    finishBtn.setEnabled(true);
                } else {
                    finishBtn.setEnabled(false);
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

    public boolean isBothTwoSecretNotEmpty() {
        if(!isSecretOneEmpty && !isSecretTwoEmpty) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isTwoSecretSame() {
        Log.i(TAG,"one:" + secretOne.getText() + " two:" + secretTwo.getText());
        if(secretOne.getText().toString().equals(secretTwo.getText().toString())) {
            Log.i(TAG,"one equals two");
            //ActivityUtils.alert(getActivity(), getResources().getString(R.string.btn_complete));
            return true;
        } else {
            Log.i(TAG,"one not equals two");
            ActivityUtils.alert(getActivity(), getResources().getString(R.string.alert_two_new_pwd_not_equal));
            return false;
        }
    }

    void doStep2Register(){
        mLoginPwd = secretOne.getText().toString();
        Log.i(TAG, "secret input finish");
        finishBtn.setEnabled(false);
        if(null == mProgressDialog){
                    //mProgressDialog = new ProgressDialog(getActivity(),com.gome.R.style.Theme_GOME_Light_Dialog);
                    //mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog = new GomeProgressDialog.Builder(getActivity())
                    .setProgressStyle(GomeProgressDialog.STYLE_SPINNER)
                    .setCancelable(false).setMessage(getResources()
                    .getString(R.string.register_submitting))
                    .create();
                }
        mProgressDialog.show();
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                mTableAccountInfo.clear();
                mTableAccountInfo.put(Constants.KEY_ACCOUNT_PHONE_NUMBER,phoneNumber);
                mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_TYPE,mThirdType);
                mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_ID,mThirdId);
                mTableAccountInfo.put(Constants.KEY_ACCOUNT_PWD, mLoginPwd);
                Log.i(TAG,"phone:" + phoneNumber + "thirdType:" + mThirdType + "thirdId:" + mThirdId + "loginPwd:" + mLoginPwd);
                Log.i("yimin","mProgressDialog");
                if(AccountUtils.register(getActivity(),mTableAccountInfo)){
                    Log.i(TAG,"register success");
                    mTableAccountInfo.clear();
                    mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_TYPE,mThirdType);
                    mTableAccountInfo.put(Constants.KEY_ACCOUNT_THIRD_ID,mThirdId);
                    if(AccountUtils.login(getActivity(),mTableAccountInfo)){ //注册完成立即登陆
                        Log.i(TAG,"login success:start up info");
                        if(!"/0".equals(wechatHeadUrl)) {
                            Log.i(TAG,"has wechat head");
                            //modify by yimin.zhu for bugid PRODUCTION-8353
                            NetworkUtilities.downloadAvatarImage(wechatHeadUrl,Constants.HEAD_PORTRAIT_DIR_PATH + "wechatHeadUrl.jpg");
                            String mLocalAvatarPath = Constants.HEAD_PORTRAIT_DIR_PATH + "wechatHeadUrl.jpg";
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
                            Bitmap mBitmap  = BitmapFactory.decodeStream(fis);
                            Account gomeAccount = AccountUtils.getLatestGomeAccount(getActivity()); //获取本地账号
                            if(null != gomeAccount){
                                HashMap<String,String> mTableAccountInfo = AccountUtils.getAccountDetailFromLocal(getActivity(),gomeAccount.name); 
                                mToken = mTableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
                                mGomeId = mTableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
                            }
                            mTableAccountInfo.clear();
                            mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
                            mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
                            mTableAccountInfo.put(Constants.KEY_AVATAR_TYPE, mImageType);
                            mTableAccountInfo.put(Constants.KEY_ACCOUNT_SERVER_AVATAR, BitmapUtils.bitmap2StrByBase64(mBitmap));
                            Log.i(TAG,"token:" + mToken + "gomeId:" + mGomeId);
                            //压缩16倍后上传
                            if(AccountUtils.uploadLocalImageToServer(getActivity(), BitmapUtils.compressBitmap(mBitmap), mTableAccountInfo)){
                                Log.i(TAG,"upload image success!");
                                mTableAccountInfo.clear();
                                mTableAccountInfo.put(Constants.KEY_ACCOUNT_NICK_NAME, wechatNickName);
                                mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
                                mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
                                if(AccountUtils.modifyAccountInfoFromServer(getActivity(),mTableAccountInfo)) {
                                    Log.i(TAG,"update name success!");
                                    mHandler.post(new Runnable() { //ui 操作放在主线程里面
                                        @Override
                                        public void run() {
                                            //login succeed and modify wechat info to server
                                            finishBtn.setEnabled(true);
                                            mProgressDialog.dismiss();
                                            Intent i =new Intent(getActivity(),AccountInfoActivity.class);  
                                            startActivity(i);
                                            getActivity().finish();
                                        }
                                    });
                                } else {
                                    Log.i(TAG,"modify info fail!");
                                    mProgressDialog.dismiss();
                                    //modify by yimin.zhu for bugid 4774 on 2017.8.30
                                    mHandler.post(new Runnable() { //ui 操作放在主线程里面
                                        @Override
                                        public void run() {
                                            finishBtn.setEnabled(true);
                                        }
                                    });
                                }
                            } else {
                                Log.i(TAG,"upload fail!");
                                mProgressDialog.dismiss();
                                mHandler.post(new Runnable() { //ui 操作放在主线程里面
                                    @Override
                                    public void run() {
                                            finishBtn.setEnabled(true);
                                    }
                                });
                            }
                        } else {
                            Log.i(TAG,"no wechat head");
                            Account gomeAccount = AccountUtils.getLatestGomeAccount(getActivity());
                            if(null != gomeAccount){
                                HashMap<String,String> mTableAccountInfo = AccountUtils.getAccountDetailFromLocal(getActivity(),gomeAccount.name); 
                                mToken = mTableAccountInfo.get(Constants.KEY_SERVER_TOKEN);
                                mGomeId = mTableAccountInfo.get(Constants.KEY_ACCOUNT_GOME_ID);
                            }
                            mTableAccountInfo.clear();
                            mTableAccountInfo.put(Constants.KEY_ACCOUNT_NICK_NAME, wechatNickName);
                            mTableAccountInfo.put(Constants.KEY_SERVER_TOKEN, mToken);
                            mTableAccountInfo.put(Constants.KEY_ACCOUNT_GOME_ID, mGomeId);
                            if(AccountUtils.modifyAccountInfoFromServer(getActivity(),mTableAccountInfo)) {
                                Log.i(TAG,"update name success!");
                                mHandler.post(new Runnable() { //ui 操作放在主线程里面
                                    @Override
                                    public void run() {
                                        //login succeed and modify wechat info to server
                                        finishBtn.setEnabled(true);
                                        mProgressDialog.dismiss();
                                        Intent i =new Intent(getActivity(),AccountInfoActivity.class);  
                                        startActivity(i);
                                        getActivity().finish();
                                    }
                                });
                            } else {
                                Log.i(TAG,"modify info fail!");
                                mProgressDialog.dismiss();
                                //modify by yimin.zhu for bugid 4774 on 2017.8.30
                                mHandler.post(new Runnable() { //ui 操作放在主线程里面
                                    @Override
                                    public void run() {
                                        finishBtn.setEnabled(true);
                                    }
                                });
                            }
                        }
                    } else {
                        Log.i(TAG,"register fail!");
                        mProgressDialog.dismiss();
                        mHandler.post(new Runnable() { //ui 操作放在主线程里面
                            @Override
                            public void run() {
                                finishBtn.setEnabled(true);
                            }
                        });
                    }
                } else {
                    Log.i(TAG,"register fail!");
                    mProgressDialog.dismiss();
                    mHandler.post(new Runnable() { //ui 操作放在主线程里面
                        @Override
                        public void run() {
                            finishBtn.setEnabled(true);
                        }
                    });
                }
            }
        });

    }



}
