package com.gome.gomeaccountservice.controls;

import com.gome.gomeaccountservice.Constants;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.gome.gomeaccountservice.R;

public class GomeCommonEditText extends LinearLayout {
	static final String TAG = Constants.TAG_PRE+"GomeCommonEditText";
	EditText mEditText;
	ImageView mImageDeleteEdit;
	public GomeCommonEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, "GomeCommonEditText()  00");
		LayoutInflater.from(context).inflate(R.layout.gome_common_edittext_layout, this, true);
		mEditText = (EditText)findViewById(R.id.edit);
		mEditText.addTextChangedListener(new EditChangedListener());
		mImageDeleteEdit = (ImageView)findViewById(R.id.image_delete_edit);
		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GomeEditText);
		if (attributes != null) {
			String editHint = attributes.getString(R.styleable.GomeEditText_edit_hint);
			Log.i(TAG, "GomeCommonEditText() 11 editHint:"+editHint);
			mEditText.setHint(editHint);
		}
	}
	/**
	 * 获取当前自定义控件的 输入框
	 * @return
	 */
	public EditText getEditText(){
		return mEditText;
	}
	/**
	 * 点击删除输入的问题
	 * @param view
	 */
	public void doOnclickDeleteEdit(View view){
		mEditText.setText("");
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
