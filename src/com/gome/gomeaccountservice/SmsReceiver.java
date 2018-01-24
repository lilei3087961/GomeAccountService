package com.gome.gomeaccountservice;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
	static final String TAG = Constants.TAG_PRE+"SmsReceiver";
	SmsCallback mSmsCallback =  null;
	public SmsReceiver(SmsCallback smsCallback){
		mSmsCallback = smsCallback;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive() 111 intent.getAction():"+intent.getAction()+" mSmsCallback:"+mSmsCallback);
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){  
            Object[] pdus=(Object[])intent.getExtras().get("pdus");  
            SmsMessage[] message=new SmsMessage[pdus.length];  
            StringBuilder sb=new StringBuilder();  
            Log.i(TAG, "onReceive() 222 pdus.length:"+pdus.length);
            for(int i=0;i<pdus.length;i++){  
                message[i]=SmsMessage.createFromPdu((byte[])pdus[i]);  
                sb.append("内容:"+message[i].getDisplayMessageBody());  
                String smsCode = getSmsCodeFromSmsMessage(message[i].getDisplayMessageBody());
                if(null != mSmsCallback && null != smsCode){
                    Log.i(TAG, "onReceive() 223 call mSmsCallback.onGetSmsCode smsCode:"+smsCode);
                	mSmsCallback.onGetSmsCode(smsCode);
                }
            }
            Log.i(TAG, "onReceive()333 sb.toString():"+sb.toString());


        }

	}
	/**
	 * 短信监听接口
	 * @author lilei
	 *
	 */
	public interface SmsCallback{
		void onGetSmsCode(String smsCode);
	}
	/**
	 * 从短消息中获取短信验证码
	 * @param msg
	 * @return 短信验证码
	 */
	public static String getSmsCodeFromSmsMessage(String msg){
		//String msg = "【国美手机】验证码718735，10分钟内有效。如非本人操作，请忽略本消息。";
		String smsCode = null;
		int index = -1;
		if(msg.contains("验证码")){
			index = msg.indexOf("验证码")+3;
			smsCode = msg.substring(index, index+6);
		}
		Log.i(TAG, "getSmsCodeFromSmsMessage() index:"+index+" smsCode:"+smsCode);
		return smsCode;
	}
}
