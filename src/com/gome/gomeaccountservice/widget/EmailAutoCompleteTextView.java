package com.gome.gomeaccountservice.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.gome.gomeaccountservice.Constants;
import com.gome.gomeaccountservice.R;
public class EmailAutoCompleteTextView extends AutoCompleteTextView implements TextWatcher {
	private static final String TAG = Constants.TAG_PRE+"EmailAutoCompleteTextView";
	public  boolean hasFoucs;
	private Drawable mClearDrawable;
	private String[] emailSufixs = new String[] { 
		"@qq.com",
		"@126.com",
		"@163.com",
		"@sina.com",
		"@hotmail.com",
		"@139.com",
		"@gmail.com",
		"@sohu.com",
		"@vip.qq.com",
		"@vip.163.com",
		"@wo.cn",
		"@sina.cn",
		"@aliyun.com",
		"@foxmail.com",
		"@189.cn",
		"@outlook.com"
	};

	public EmailAutoCompleteTextView(Context context) {
		super(context);
		init(context);
	}

	public EmailAutoCompleteTextView(Context context, AttributeSet attrs) {
		this(context, attrs,android.R.attr.editTextStyle);
	}

	public EmailAutoCompleteTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void setAdapterString(String[] es) {
		if(es != null && es.length > 0)
			this.emailSufixs = es;
	}
	
	private void init(final Context context) {
		this.setAdapter(new EmailAutoCompleteAdapter(context,
				R.layout.auto_complete_item, emailSufixs));
		this.setThreshold(1);
		this.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				EmailAutoCompleteTextView.this.hasFoucs = hasFocus;
				if (hasFocus) {
					setClearIcoVisible(getText().length() > 0);
					String text = EmailAutoCompleteTextView.this.getText()
							.toString();
					if (!"".equals(text))
						performFiltering(text, 0);
				} else {
					setClearIcoVisible(false);
					//modify by yimin.zhu for bugid GMOS-3701
					/*EmailAutoCompleteTextView ev = (EmailAutoCompleteTextView) v;
					String text = ev.getText().toString();
					if (text != null
							&& text.matches("^[a-zA-Z0-9_]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
						Toast to = new Toast(context);
						ImageView i = new ImageView(context);
						i.setBackgroundResource(R.drawable.ico_refund_success);
						to.setView(i);
						to.show();
					} else {
					}*/
				}
			}
		});
	}

	@Override
	protected void replaceText(CharSequence text) {
		Log.i(TAG + " replaceText", text.toString());
		String t = this.getText().toString();
		int index = t.indexOf("@");
		if(index != -1)
			t = t.substring(0, index);
		super.replaceText(t + text);
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		Log.i(TAG + " performFiltering", text.toString() + "   " + keyCode);
		String t = text.toString();
		int index = t.indexOf("@");
        int starIndex = t.indexOf("*");
        Log.i(TAG, " performFiltering() @ index:"+index+" starIndex:"+starIndex);
		if(index == -1) {
			if(t.matches("^[a-zA-Z0-9_]+$+@+$")) {
				super.performFiltering("@", keyCode);
			}
			else
				this.dismissDropDown();
		} else {
            if(starIndex == -1) {//包含@且账户不存在*号，则弹出提示框
                super.performFiltering(t.substring(index), keyCode);
            }else{
                this.dismissDropDown();
            }
		}
	}

	
	private class EmailAutoCompleteAdapter extends ArrayAdapter<String> {

		public EmailAutoCompleteAdapter(Context context, int textViewResourceId, String[] email_s) {
			super(context, textViewResourceId, email_s);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.i(TAG, "in GetView");
			View v = convertView;
			if (v == null)
				v = LayoutInflater.from(getContext()).inflate(
						R.layout.auto_complete_item, null);
			TextView tv = (TextView) v.findViewById(R.id.tv);
			
			String t = EmailAutoCompleteTextView.this.getText().toString();
			int index = t.indexOf("@");
			if(index != -1)
				t = t.substring(0, index);
			tv.setText(t + getItem(position));
			Log.i(TAG, tv.getText().toString());
			return v;
		}
	}

	private void setClearIcoVisible(boolean visible) {
		Drawable rightDrawable = visible ? mClearDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], rightDrawable, getCompoundDrawables()[3]);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (getCompoundDrawables()[2] != null) {
			boolean touchable = (event.getX() > (getWidth() - getTotalPaddingRight())) && (event.getX() < (getWidth() - getPaddingRight()));
			if (touchable) {
				this.setText("");
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

		if (hasFoucs) {
			setClearIcoVisible(length() > 0);
		}
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}

	public void setShakeAnimation() {

		this.startAnimation(shakeAnimation(5));
	}

	public static Animation shakeAnimation(int counts) {

		Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
		translateAnimation.setInterpolator(new CycleInterpolator(counts));
		translateAnimation.setDuration(1000);
		return translateAnimation;
	}
}
