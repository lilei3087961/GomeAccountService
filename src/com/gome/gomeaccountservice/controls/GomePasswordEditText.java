package com.gome.gomeaccountservice.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gome.gomeaccountservice.Constants;
import com.gome.gomeaccountservice.R;

public class GomePasswordEditText extends LinearLayout {
	static final String TAG = Constants.TAG_PRE+"GomePasswordEditText";
	EditText mEditText;
	ImageView mImageDeleteEdit;
	ImageView mImageShowPwd;
	ImageView mImageHidePwd;
	public GomePasswordEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, "GomePasswordEditText()  00");
		LayoutInflater.from(context).inflate(R.layout.gome_password_edittext_layout, this, true);
		mEditText = (EditText)findViewById(R.id.edit);
		mEditText.addTextChangedListener(new EditChangedListener());
        mEditText.setTypeface(Typeface.DEFAULT);
		mImageDeleteEdit = (ImageView)findViewById(R.id.image_delete_edit);
		mImageShowPwd = (ImageView)findViewById(R.id.image_show_password);
		mImageHidePwd = (ImageView)findViewById(R.id.image_hide_password);
		
		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GomeEditText);
		if (attributes != null) {
			String editHint = attributes.getString(R.styleable.GomeEditText_edit_hint);
			Log.i(TAG, "GomePasswordEditText() 11 editHint:"+editHint);
			mEditText.setHint(editHint);
		}
	}
	//点击删除输入的密码
	public void doOnclickDeleteInputPwd(View view){
		mEditText.setText("");
	}
	//点击隐藏密码按钮
	public void doOnclickHidePassword(View view){
		mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
		mImageHidePwd.setVisibility(View.VISIBLE);
		mImageShowPwd.setVisibility(View.GONE);
	}
	//点击显示密码按钮
	public void doOnclickShowPassword(View view){
		mEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		mImageHidePwd.setVisibility(View.GONE);
		mImageShowPwd.setVisibility(View.VISIBLE);
	}
	/**
	 * 获取当前自定义控件的 输入框
	 * @return
	 */
	public EditText getEditText(){
		return mEditText;
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
}
