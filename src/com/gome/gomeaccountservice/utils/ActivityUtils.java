package com.gome.gomeaccountservice.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.gomeaccountservice.Constants;
import com.gome.gomeaccountservice.R;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gome.app.GomeAlertDialog;

public class ActivityUtils {
	private static final String TAG = Constants.TAG_PRE+"ActivityUtils";
	static GomeAlertDialog mSetAvatarChooserDialog;
	static Dialog mWifiAlertDialog = null;
	public static final String BOOT_WIZARD_PACAAGE_NAME = "com.gome.bootwizard";  //开向导包名称
	public static final String BOOT_WIZARD_LOGIN_SUCCESS_ACTIVITY = "com.gome.bootwizard.activity.SuccessfulSettingActivity";//开机向导登录activity
	public static final String BOOT_WIZARD_WELCOME_ACTIVITY = "com.gome.bootwizard.activity.WelcomeActivity"; //开机向导跳过登录activity
	public static final String BOOT_WIZARD_FINGER_PRINT_ACTIVITY = "com.gome.bootwizard.activity.FingerPrintActivity"; //开机向导跳过登录activity
	public static final String BOOT_WIZARD_IRIS_ACTIVITY = "com.gome.bootwizard.activity.IrisActivity"; //开机向导跳过登录activity
	public static final String BOOT_WIZARD_FACE_CODE_ACTIVITY = "com.gome.bootwizard.activity.FaceCodeActivity"; //开机向导跳过登录activity
    
	public static final String USER_CENTER_PACAAGE_NAME = "com.gome.usercenter";  //用户中心包名称
	public static final String USER_CENTER_USER_AGREE_AND_PRIVACY_POLICY_NAME = "com.gome.usercenter.activity.ShowPolicyActivity";  //用户协议和隐藏政策类
    
	public static final String CLOUD_SERVICE_PACAAGE_NAME = "com.mediatek.backuprestore";  //云服务包名称
	public static final String CLOUD_SERVICE_MAIN_ACTIVITY = "com.gome.cloudservices.ui.CloudServiceMainActivity";  //云服务主界面名称
	
	public static final String ACTION_SMS_RECEIVER = "android.provider.Telephony.SMS_RECEIVED";  
	public static void alert(Context context,String str){
		try {
		    Log.d(TAG, "alert() LENGTH_SHORT str:"+str);
		    Toast toast = Toast.makeText(context,str,Toast.LENGTH_SHORT);
		    LinearLayout linearLayout = (LinearLayout)toast.getView();
		    TextView tv = (TextView)linearLayout.findViewById(com.android.internal.R.id.message); 
		    tv.setSingleLine(false);
		    toast.show();
            Log.i(TAG, "alert() linearLayout:"+linearLayout+" tv:"+tv);
		} catch (Exception ex){ 
            Log.i(TAG, "showDialog() ex:"+ex.toString());
        }
	}
	//for netwok check begin
	private static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();  
			}
		}
		return false;  
	}
	public static boolean checkNetwork(Context context){
		if(AccountUtils.NO_SERVER_TEST){
			return true;
		}
		if(!isNetworkConnected(context)){
			Log.d(TAG, "checkNetwork 111 network unavaiable!!");
			//ActivityUtils.alert(context,context.getResources().getString(R.string.alert_network_unavaiable));
			showSetWifiDialog(context);
			return false;
		}else{
			Log.d(TAG, "checkNetwork 222 network ok!!");
		}
		return true;
	}
	//只检查网络是否可用，不会弹出设置网络弹出框
    public static boolean checkNetworkSimple(Context context){
        if(AccountUtils.NO_SERVER_TEST){
            return true;
        }
        if(!isNetworkConnected(context)){
            Log.d(TAG, "checkNetworkSimple 111 network unavaiable!!");
            return false;
        }else{
            Log.d(TAG, "checkNetworkSimple 222 network ok!!");
        }
        return true;
    }
	static void showSetWifiDialog(final Context context){
		Log.i(TAG, "showSetWifiDialog() 11 context:"+context);
		if(null != mWifiAlertDialog && !context.equals(mWifiAlertDialog.getContext())){
			Log.i(TAG, "showSetWifiDialog() 22 new Context: context:"+context+" mWifiAlertDialog.getContext():"+mWifiAlertDialog.getContext());
            try {
                mWifiAlertDialog.cancel();
            }catch (Exception ex){
                Log.i(TAG, "showSetWifiDialog() mWifiAlertDialog.cancel() ex:"+ex.toString());
            }
            mWifiAlertDialog = null;
		}
		if(null == mWifiAlertDialog){
            String accountStartMode = AccountUtils.getLocalAccountStartModeFromShare(context);
            Log.i(TAG, "showSetWifiDialog()  accountStartMode:"+accountStartMode);
            if(Constants.BOOT_WIZARD_MODE.equals(accountStartMode)){
                mWifiAlertDialog = new GomeAlertDialog.Builder(context).
                        setTitle(context.getResources().getString(R.string.dialog_title_no_network)).
                        setPositiveButton(context.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub  
                                Log.i(TAG, "setPositiveButton.onClick setwifi dissmiss");
                                mWifiAlertDialog.dismiss();
                            }
                        }).
                        create();
            }else {
                mWifiAlertDialog = new GomeAlertDialog.Builder(context).
                setTitle(context.getResources().getString(R.string.dialog_title_no_network)).
                setPositiveButton(context.getResources().getString(R.string.txt_set_network), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub  
                        Log.i(TAG, "setPositiveButton.onClick setwifi dissmiss");
                        //context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        context.startActivity(intent);
                        mWifiAlertDialog.dismiss();
                    }
                }).
                setNegativeButton(context.getResources().getString(R.string.txt_set_network_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub    
                        Log.i(TAG, "setNeutralButton.onClick setwifi dissmiss");
                        mWifiAlertDialog.dismiss();
                    }
                }).
                create();
            }
		}
		Log.i(TAG, " all show mWifiAlertDialog.isShowing():"+mWifiAlertDialog.isShowing());
		mWifiAlertDialog.show();
	}
    
    //其他通用方法
    /**
     * 判断字符串是否邮箱
     * @param email
     * @return
     */
    public static boolean isEmail(String email){  
        if (null==email || "".equals(email)) return false;	
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配  
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配  
        Matcher m = p.matcher(email);  
        return m.matches();  
    }
    /**
     * 判断字符串是否手机号, 先只判断位数为11位
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles){
    	boolean match = false;
    	if(11 == mobiles.length()){
    		match = true;
    	}
//    	Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");  
//		Matcher m = p.matcher(mobiles);  
//		boolean match = m.matches(); 
//		Log.i(TAG, "isMobileNO() match:"+match);
	    return match;
	}
    /**
     * 登陆邮箱 邮件验证/激活
     * @param context
     */
    public static void verifyMail(Context context){
    	Intent intent = new Intent();        
        intent.setAction("android.intent.action.VIEW");    
        Uri content_url = Uri.parse("https://www.baidu.com");   
        intent.setData(content_url);  
        context.startActivity(intent);
    }

    //add by yimin.zhu for bugid GMOS-739 on 2017.8.31
    /**
     * 登陆邮箱 邮件验证/激活/根据邮箱地址跳转不同uri
     * @param context
     */
    public static void verifyMail(Context context,String emailAddress){
        //截取@之后的字符串
        String emailEnd = emailAddress.substring(emailAddress.lastIndexOf("@") + 1);
        Log.i(TAG,"email end with:" + emailEnd);
        String uri = null;
        if("qq.com".equals(emailEnd)) {
            uri = "http://mail.qq.com";
        } else if("126.com".equals(emailEnd)) {
            uri = "http://mail.126.com";
        } else if("163.com".equals(emailEnd)) {
            uri = "http://mail.163.com";
        } else if("sina.com".equals(emailEnd)) {
            uri = "http://mail.sina.com";
        } else if("hotmail.com".equals(emailEnd)) {
            uri = "http://mail.live.com";
        } else if("139.com".equals(emailEnd)) {
            uri = "http://mail.139.com";
        } else if("gmail.com".equals(emailEnd)) {
            uri = "https://mail.google.com";
        } else if("sohu.com".equals(emailEnd)) {
            uri = "http://mail.sohu.com";
        } else if("vip.qq.com".equals(emailEnd)) {
            uri = "http://mail.vip.qq.com";
        } else if("vip.163.com".equals(emailEnd)) {
            uri = "http://mail.163.com";
        } else if("wo.com".equals(emailEnd)) {
            uri = "http://mail.wo.cn";
        } else if("sina.cn".equals(emailEnd)) {
            uri = "http://mail.sina.cn";
        } else if("aliyun.com".equals(emailEnd)) {
            uri = "http://mail.aliyun.com";
        } else if("foxmail.com".equals(emailEnd)) {
            uri = "http://mail.foxmail.com";
        } else if("189.cn".equals(emailEnd)) {
            uri = "http://mail.189.cn";
        } else if("outlook.com".equals(emailEnd)) {
            uri = "https://outlook.live.com";
        } else {
            uri = "http://mail." + emailEnd;
        }
        Intent intent = new Intent();        
        intent.setAction("android.intent.action.VIEW");    
        Uri content_url = Uri.parse(uri);   
        intent.setData(content_url);  
        context.startActivity(intent);
    }
    /**
     * 将12345678901 改成  123 4567 8901 显示
     * @param phoneNum
     * @return
     */
    public static String convertPhoneNumShow(String phoneNum){
    	String str = phoneNum;
    	if(phoneNum.length() == 11){
    		str = phoneNum.substring(0, 3)+" "+phoneNum.substring(3, 7)+" "+phoneNum.substring(7, 11);
    	}
    	return str;
    }
    /**
     * 将12345678901 改成  123****8901 显示
     * @param phoneNum
     * @return
     */
    public static String convertPhoneNumWithStar(String phoneNum){
    	String str = phoneNum;
    	if(phoneNum.length() == 11){
            str = phoneNum.substring(0, 3)+"****"+phoneNum.substring(7, 11);
    	}
    	return str;
    }
    /**
     * 邮箱隐藏中间1-3位
     * 如： 12@qq.com => 1*@qq.com  123@qq.com => 1*3@qq.com  	1234@qq.com => 1**4@qq.com
     *  12345@qq.com => 1***5@qq.com    123456@qq.com => 12**56@qq.com
     * @param phoneNum
     * @return
     */
    public static String convertEmailWithStar(String email){
		Log.i(TAG, "convertEmailWithStar() email:"+email);
		String strPrefix = email.substring(0,email.indexOf("@"));
		String strPrefixHead;
		String strPrefixTail;
		String strTail = email.substring(email.indexOf("@"));//@以后的字符串包括@
		String strConvert = null;
		switch (strPrefix.length()) {
			case 1:
				strConvert = email;
				break;
			case 2:
				strPrefixHead = strPrefix.substring(0, 1);
				strConvert = strPrefixHead+"*"+strTail;
				break;
			case 3:
				strPrefixHead = strPrefix.substring(0, 1);
				strPrefixTail = strPrefix.substring(2, 3);
				strConvert = strPrefixHead+"*"+strPrefixTail+strTail;
				break;
			default:
				if(strPrefix.length()%2 ==0){ //偶数
					strConvert = strPrefix.substring(0, strPrefix.length()/2-1)+"**"+strPrefix.substring(strPrefix.length()/2+1)+strTail;
				}else{ //奇数
					strConvert = strPrefix.substring(0, strPrefix.length()/2-1)+"***"+strPrefix.substring(strPrefix.length()/2+2)+strTail;
				}
				break;
		}
		Log.i(TAG, "convertEmailWithStar() prefix:"+strPrefix+" strTail:"+strTail+" strConvert:"+strConvert);
		return strConvert;
    }
    /**
     *  获取当前手机号
     * @param context
     * @return
     */
    public static String getCurrentPhoneNumber(Context context){
    	TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    	String tel = tm.getLine1Number();
    	Log.i(TAG, "getCurrentPhoneNumber() tel:"+tel);
    	if(null != tel){
    		if(tel.length() == 11){
    			return tel;
    		}else if(tel.length() == 14){
    			return tel.substring(3);
    		}
    	}
    	return null;
    }
    /**
     * 打开AlertDialog软键盘
     * @param activity
     */
    public static void showAlertDialogKeyboard(GomeAlertDialog alertDialog) {
        Log.i(TAG,"showAlertDialogKeyboard()");
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
    /**
     * 关键软键盘
     * @param activity
     */
    public static void closeKeyboard(Activity activity) {
        Log.i(TAG,"closeKeyboard()");
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    //////////////////////dialog显示和隐藏
    //显示设置头像选择弹出框
    public static void showSetAvatarChooserDialog(Context context){
    	Log.i(TAG, "showSetAvatarChooserDialog() ");
    	LayoutInflater inflater =  LayoutInflater.from(context);
    	View layout = inflater.inflate(R.layout.alert_set_avatar_chooser_layout,null);
    	mSetAvatarChooserDialog = new GomeAlertDialog.Builder(context).setView(layout).create();
    	Window window = mSetAvatarChooserDialog.getWindow();
    	mSetAvatarChooserDialog.show();
    }
    //隐藏设置头像选择弹出框
    public static void hideSetAvatarChooserDialog(){
    	Log.i(TAG, "hideSetAvatarChooserDialog() ");
        try {
            if (null != mSetAvatarChooserDialog) {
                mSetAvatarChooserDialog.dismiss();
            }
        }catch (Exception ex){ //解决分屏模式关闭dialog后再次调用的异常
            Log.i(TAG, "hideSetAvatarChooserDialog() ex:"+ex.toString());
        }
    }
    /**
     * 检查activity是否存在
     * @param context
     * @param packageName  包名称
     * @param activityName 类名称(全路径包名+类名)
     * @return 
     */
    public static boolean isActivityExists(Context context,String packageName,String activityName){
		Log.i(TAG, "isActivityExists() packageName:"+packageName+" activityName:"+activityName);
		Intent intent = new Intent();
		intent.setClassName(packageName, activityName);
		if (context.getPackageManager().resolveActivity(intent, 0) == null) {  
	       return false;
		}
		return true;
	}
    /**
     * 通过包名称和类名启动activity
     * @param context
     * @param packageName  包名称
     * @param activityName 类名称(全路径包名+类名)
     * @return
     */
    public static void startActivityByName(Context context,String packageName,String activityName){
    	Log.i(TAG, "startActivityByName() FLAG_ACTIVITY_CLEAR_TOP currentThread:"+Thread.currentThread()+" packageName:"+packageName+" activityName:"+activityName);
		ComponentName cn = new ComponentName(packageName, activityName);
		Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//启动界面，若存在则清除该界面顶部界面
		intent.setComponent(cn);
		context.startActivity(intent);
    }

    /**
     * 获取当前语言的字符串 cn简体/tw繁体/en英文
     * @param context
     */
    public static String getCurrentLanguageStr(Context context){
        String languageStr = "cn"; //默认简体中文
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if("zh".equals(language) && "TW".equals(country)){
            languageStr = "tw";
        }else if("en".equals(language)){
            languageStr = "en";
        }
        Log.i(TAG,"getCurrentLanguageStr() languageStr:"+languageStr+" language:"+language+" country:"+country);
        return languageStr;
    }

    /**
     * 发送隐藏 虚拟按键栏广播，一般开机向导模式调用
     * @param context
     * @return
     */
    public static void sendHideNavigationBarBrocast(Context context){
        Log.i(TAG,"sendHideNavigationBarBrocast() ");
        Intent intentAction = new Intent();
        intentAction.setPackage("com.android.systemui");
        intentAction.setAction("hide_navigationbar");
        context.sendBroadcast(intentAction);
    }
}
