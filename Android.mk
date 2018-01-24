LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
                     
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_STATIC_JAVA_LIBRARIES := org.apache.http.legacy \
	wechat
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
LOCAL_STATIC_JAVA_AAR_LIBRARIES:= \
	CityPicker
LOCAL_JAVA_LIBRARIES += telephony-common


LOCAL_PACKAGE_NAME := GomeAccountService

LOCAL_PRIVILEGED_MODULE := true


LOCAL_AAPT_FLAGS := \
--auto-add-overlay \
--extra-packages citypicker.example.com.citypicker

include $(BUILD_PACKAGE)

#编译引用jar包
include $(CLEAR_VARS)  
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := CityPicker:libs/CityPicker.aar
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := wechat:libs/libammsdk.jar
include $(BUILD_MULTI_PREBUILT)




