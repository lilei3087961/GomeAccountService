package com.gome.gomeaccountservice.utils;

import com.gome.gomeaccountservice.Constants;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

public class EditChangedListener implements TextWatcher{
	static final String TAG = Constants.TAG_PRE+"EditChangedListener";
	private int mEditType = -1;
	private TextChangeCallback mTextChangeCallback;
	public EditChangedListener(int editType,TextChangeCallback textChangeCallback){
		mEditType = editType;
		mTextChangeCallback = textChangeCallback;
	}
	@Override
	public void afterTextChanged(Editable arg0) {
		Log.e(TAG, "afterTextChanged() 00 mEditType:"+mEditType+" arg0.toString():"+arg0.toString());
		if(arg0.toString().length()>0){
			mTextChangeCallback.showAfterTextNotEmpty(mEditType);
		}else{
			mTextChangeCallback.showAfterTextEmpty(mEditType);
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
	/**
	 * 输入文本变化监听回调接口
	 * @author lilei
	 *
	 */
	public  interface TextChangeCallback{
		/**
		 * afterTextChanged输入框内容非空回调
		 * @param editType  输入框识别类型
		 */
		void showAfterTextNotEmpty(int editType);
		/**
		 * afterTextChanged 输入框内容非空回调
		 * @param editType  输入框识别类型
		 */
		void showAfterTextEmpty(int editType);
	}
}
