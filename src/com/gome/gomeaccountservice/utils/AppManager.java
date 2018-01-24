package com.gome.gomeaccountservice.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import com.gome.gomeaccountservice.Constants;

import java.util.Stack;

/**
 * @author
 * @version 1.0
 * @date 2017/9/14
 */

public class AppManager {
    String TAG = Constants.TAG_PRE+"AppManager";
    private static Stack<Activity> activityStack;
    private static Stack<Activity> tempActivityStack;
    private static AppManager instance;
    private PendingIntent restartIntent;

    private AppManager() {
    }

    /**
     * 单一实例   
     */
    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈   
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        if (tempActivityStack == null) {
            tempActivityStack = new Stack<Activity>();
        }
        Log.i(TAG,"addActivity() 000 activityStack.size():"+activityStack.size()+" tempActivityStack:"+tempActivityStack);
        if(!activityStack.contains(activity)) {
            Log.i(TAG,"addActivity() 111 not contains add activity:"+activity);
            activityStack.add(activity);
        }else{
            Log.i(TAG,"addActivity() 222 not add, already contains activity:"+activity);
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）   
     */
    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）   
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity   
     */
    public void finishActivity(Activity activity) {
        Log.i(TAG,"finishActivity() activity:"+activity+" activityStack:"+activityStack);
        if(null != activityStack) {
            Log.i(TAG, "finishActivity()  activityStack.size():" + activityStack.size());
            Log.i(TAG, "finishActivity()  activityStack.contains(activity):" + (null == activity ? "null" : activityStack.contains(activity)));
        }
        if (null != activityStack && activityStack.contains(activity)) {
            activityStack.remove(activity);
            activity.finish();
        }
    }

    /**
     * 结束指定类名的Activity   
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }
    /**
     * 结束所有属于同一个taskid 的Activity   
     */
    public void finishAllActivityByTaskId(int taskid) {
        Log.i(TAG,"finishAllActivityByTaskId() 111 activityStack.size()："+(null == activityStack?"null":activityStack.size())
            +" taskid:"+taskid);
        tempActivityStack.clear();
        if(null != activityStack) {
            for (int i = 0, size = activityStack.size(); i < size; i++) {
                Activity activity = activityStack.get(i);
                Log.i(TAG,"finishAllActivityByTaskId() 222 activityStack.get("+i+"):"+activity+" getTaskId:"
                        +(null != activity ? activity.getTaskId():"null"));
                if (null != activity && taskid == activity.getTaskId()) {
                    Log.i(TAG,"finishAllActivityByTaskId() 333 finish "+activity);
                    tempActivityStack.add(activity);
                    activity.finish();
                }
            }
            for (int i = 0, size = tempActivityStack.size(); i < size; i++) {
                Activity activity = tempActivityStack.get(i);
                Log.i(TAG,"finishAllActivityByTaskId() 444 activity:"+activity+" activityStack.contains(activity):"+activityStack.contains(activity));
                if(activityStack.contains(activity)){
                    activityStack.remove(activity);
                }
            }
            tempActivityStack.clear();
            //activityStack.clear();
        }
    }
    /**
     * 结束所有Activity   
     */
    public void finishAllActivity() {
        Log.i(TAG,"finishAllActivity() activityStack.size()："+(null == activityStack?"null":activityStack.size()));
        if(null != activityStack) {
            for (int i = 0, size = activityStack.size(); i < size; i++) {
                Log.i(TAG,"activityStack.get("+i+"):"+activityStack.get(i));
                if (null != activityStack.get(i)) {
                    activityStack.get(i).finish();
                }
            }
            activityStack.clear();
        }
    }

    /**
     * 退出应用程序   
     */
    public void exitApp(Context context) {
        try {
            finishAllActivity();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
        }
    }
}
