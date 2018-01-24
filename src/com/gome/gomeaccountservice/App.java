package com.gome.gomeaccountservice;

import android.app.Application;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


public class App extends Application {

    //public static final String WX_APPID = "wxc66e3d52db3f3d5a";
    //public static final String WX_APPSecret = "a5f2ac556bbf48c7cc319b8a6740b40e";
	public static final String WX_APPID = "wx42504fc98c544a07";
    public static final String WX_APPSecret = "cb09694b027b81f1c260ec400cb711cb";

    private IWXAPI api;

    @Override
    public void onCreate() {
        super.onCreate();
        api = WXAPIFactory.createWXAPI(this, WX_APPID, true);
        api.registerApp(WX_APPID);
    }

}
