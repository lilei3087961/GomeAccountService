package com.gome.gomeaccountservice;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.content.Intent;

public class BindPhoneActivity extends Activity {
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private String mThirdId;
    private String headimgurl;
    private String nickname;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent(); 
        mThirdId = intent.getStringExtra("wechatId");
        headimgurl = intent.getStringExtra("headimgurl");
        nickname = intent.getStringExtra("nickname");
		setContentView(R.layout.activity_wechat);
		initFragment();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	public void initFragment() {
		fragmentManager = getFragmentManager();
		fragment = new BindPhoneFragment();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment,"bindPhoneFragment");
        transaction.commit();
	}
	
	public void goWechatSecretFragment() {
		fragmentManager = getFragmentManager();
		fragment = new WechatSecretFragment();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.content, fragment,"wechatSecretFragment");
        transaction.commit();
	}

	public String getWechatId() {
		return mThirdId;
	}

	public String getWechatHeadImageUrl() {
		return headimgurl;
	}

	public String getWechatNickName() {
		return nickname;
	}

}
