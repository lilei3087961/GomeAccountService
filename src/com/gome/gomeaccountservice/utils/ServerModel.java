package com.gome.gomeaccountservice.utils;

public class ServerModel {
	String mServerResCode = null;  //服务端返回的errorcode
	String mEmailActiveStatus = null;//邮箱状态
    String mVerifyType = null; //校验类型
	public ServerModel(){
		
	}
	/**
	 * 获取服务端结果code
	 * @return
	 */
	public String getServerResCode(){
		return mServerResCode;
	}
	/**
	 * 设置服务端结果code
	 * @param serverResCode
	 */
	public void setServerResCode(String serverResCode){
		mServerResCode = serverResCode;
	}
	/**
	 * 获取邮箱状态
	 * @return
	 */
	public String getEmailActiveStatus(){
		return mEmailActiveStatus;
	}
	/**
	 * 设置邮箱状态
	 * @param emailActiveStatus
	 */
	public void setEmailActiveStatus(String emailActiveStatus){
		mEmailActiveStatus = emailActiveStatus;
	}
    /**
     * 获取校验类型
     * @return
     */
    public String getVerifyType(){
        return mVerifyType;
    }

    /**
     * 设置校验类型
     * @param verifyType
     * @return
     */
    public void setVerifyType(String verifyType){
        mVerifyType = verifyType;
    }
	
}
