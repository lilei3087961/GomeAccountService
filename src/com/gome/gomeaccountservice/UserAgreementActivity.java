package com.gome.gomeaccountservice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class UserAgreementActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_user_agreement_layout);
	}
	public void doOnclickExitButton(View view){
		finish();
	}

}
