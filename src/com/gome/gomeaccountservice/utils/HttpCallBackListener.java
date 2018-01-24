package com.gome.gomeaccountservice.utils;

/**
 * Created by jiang.zhang on 2017/7/24.
 */

public interface HttpCallBackListener {

    void onFinish(String response);

    void onError(Exception e);
}
