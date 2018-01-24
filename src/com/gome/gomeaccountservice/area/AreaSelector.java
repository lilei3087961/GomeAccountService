package com.gome.gomeaccountservice.area;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.gome.gomeaccountservice.R;



import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

//加载自定义控件
public class AreaSelector implements OnWheelChangedListener{
	
	final String TAG = "lilei_AreaSelector";
	Context mContext;
	private WheelView mViewProvince;
	private WheelView mViewCity;
	private WheelView mViewDistrict;

	final int INVALID_ID = -1;
	int mItemResource = INVALID_ID;//textview布局id
	int mItemTextResource = INVALID_ID; //textview id
	/**
	 * 所有省
	 */
	protected String[] mProvinceDatas;
	/**
	 * key - 省 value - 市
	 */
	protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
	/**
	 * key - 市 values - 区
	 */
	protected Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();
	
	/**
	 * key - 区 values - 邮编
	 */
	protected Map<String, String> mZipcodeDatasMap = new HashMap<String, String>(); 

	/**
	 * 当前省的名称
	 */
	protected String mCurrentProviceName;
	/**
	 * 当前市的名称
	 */
	protected String mCurrentCityName;
	/**
	 * 当前区的名称
	 */
	protected String mCurrentDistrictName ="";
	
	/**
	 * 当前区的邮政编码
	 */
	protected String mCurrentZipCode ="";

	public AreaSelector(Context context,WheelView viewProvince,WheelView viewCity,WheelView viewDistrict){
		mContext = context;
		mViewProvince = viewProvince;
		mViewCity = viewCity;
		mViewDistrict = viewDistrict;
	}
	public AreaSelector(Context context,WheelView viewProvince,WheelView viewCity,WheelView viewDistrict
			,int itemResource, int itemTextResource){
		mContext = context;
		mViewProvince = viewProvince;
		mViewCity = viewCity;
		mViewDistrict = viewDistrict;
		mItemResource = itemResource;
		mItemTextResource = itemTextResource;
		Log.e(TAG,"AreaSelector(5) mItemResource:"+mItemResource+" mItemTextResource:"+mItemTextResource);
	}
	/**
	 * 设置数据到地区view，初始化监听事件
	 */
	public void initView(String provice,String city,String district){
    	// 添加change事件
    	mViewProvince.addChangingListener(this);
		Log.e(TAG, "initView() provice:"+provice+" city:"+city+" district:"+district);
		//mViewProvince.addChangingListener(mOnWheelChangedListener);
    	// 添加change事件
    	mViewCity.addChangingListener(this);
		//mViewCity.addChangingListener(mOnWheelChangedListener);
    	// 添加change事件
    	mViewDistrict.addChangingListener(this);
		//mViewDistrict.addChangingListener(mOnWheelChangedListener);
    	initProvinceDatas();
    	if(INVALID_ID == mItemResource){
    		mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mProvinceDatas));
    	}else{
    		mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mProvinceDatas,mItemResource,mItemTextResource));
    	}
		//设置已经选中的省
		if(null != provice && !provice.isEmpty()){
			for(int i= 0;i<mProvinceDatas.length;i++){
				if(mProvinceDatas[i].equals(provice)){
					Log.e(TAG,"initView() 1111  i:"+i);
					mViewProvince.setCurrentItem(i);
				}
			}
		}
		
		// 设置可见条目数量
		mViewProvince.setVisibleItems(3);
		mViewCity.setVisibleItems(3);
		mViewDistrict.setVisibleItems(3);
		updateCities();
		//设置已经选中的市
		String[] cities = mCitisDatasMap.get(mCurrentProviceName);
		if (cities == null) {
			cities = new String[] { "" };
		}
		if(null != city && !city.isEmpty()){
			for(int i= 0;i<cities.length;i++){
				Log.e(TAG,"initView()  222 cities["+i+"]:"+cities[i]);
				if(cities[i].equals(city)){
					Log.e(TAG,"initView()  222 i:"+i);
					mViewCity.setCurrentItem(i);
				}
			}
		}

		updateAreas();
		//设置已经选中的区
		String[] areas = mDistrictDatasMap.get(mCurrentCityName);
		if (areas == null) {
			areas = new String[] { "" };
		}
		if(null != district && !district.isEmpty()){
			for(int i= 0;i<areas.length;i++){
				Log.e(TAG,"initView()  333 areas["+i+"]:"+areas[i]);
				if(areas[i].equals(district)){
					Log.e(TAG,"initView()  3333 i:"+i);
					mViewDistrict.setCurrentItem(i);
					//mCurrentDistrictName = district;
				}
			}
		}
	}
	/***
	 * 获取选中的地区
	 * @return
	 */
	public String getSelectAreaData(){
		return  "当前选中:"+mCurrentProviceName+","+mCurrentCityName+","
				+mCurrentDistrictName+","+mCurrentZipCode;
	}
	public String getSelectProvice(){
		return mCurrentProviceName;
	}
	public String getSelectCity(){
		return mCurrentCityName;
	}
	public String getSelectDistrict(){
		return mCurrentDistrictName;
	}
	public String getSelectZipCode(){
		return mCurrentDistrictName;
	}
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onChanged wheel:"+wheel+" oldValue:"+oldValue+" newValue:"+newValue);
		if (wheel == mViewProvince) {
			updateCities();
		} else if (wheel == mViewCity) {
			updateAreas();
		} else if (wheel == mViewDistrict) {
			mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[newValue];
			mCurrentZipCode = mZipcodeDatasMap.get(mCurrentDistrictName);
			//updateSelectTextColor(mCurrentDistrictName,mViewDistrict.getItemsLayout());
		}
	}
	//设置选中文本的颜色
	void updateSelectTextColor(String selectText,LinearLayout itemsLayout){
		//LinearLayout itemsLayout = mViewDistrict.getItemsLayout();
		Log.e(TAG,"updateSelectTextColor() 111 mViewDistrict:"+mViewDistrict+" itemsLayout.getChildCount():"+(itemsLayout == null?"null":itemsLayout.getChildCount()));
		if(null != itemsLayout && itemsLayout.getChildCount()>0){
			for(int i=0;i<itemsLayout.getChildCount();i++){
				if(itemsLayout.getChildAt(i) instanceof LinearLayout){
					LinearLayout textLayout = (LinearLayout)itemsLayout.getChildAt(i);
					TextView tv =  (TextView)textLayout.getChildAt(0);
					String tvText = tv.getText().toString();
					if(selectText.equals(tvText)){
						tv.setTextColor(mContext.getResources().getColor(R.color.color_button_link));
					}else{
						tv.setTextColor(mContext.getResources().getColor(R.color.color_text_line));
					}
					//tv.setText("lilei"+text1);
					Log.e(TAG, "updateSelectTextColor() 222 ** TextView("+i+"):"+tvText+" selectText:"+selectText);
				}
			}
		}
	}
	
	/**
	 * 根据当前的市，更新区WheelView的信息
	 */
	private void updateAreas() {
		int pCurrent = mViewCity.getCurrentItem();
		mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
		String[] areas = mDistrictDatasMap.get(mCurrentCityName);

		if (areas == null) {
			areas = new String[] { "" };
		}
		if(INVALID_ID == mItemResource){
			mViewDistrict.setViewAdapter(new ArrayWheelAdapter<String>(mContext, areas));
		}else{
			mViewDistrict.setViewAdapter(new ArrayWheelAdapter<String>(mContext, areas,mItemResource,mItemTextResource));
		}
		LinearLayout itemsLayout = mViewDistrict.getItemsLayout();
		Log.e(TAG,"updateAreas() mViewDistrict:"+mViewDistrict+" getItemsLayout():"+(itemsLayout == null?"null":itemsLayout.getChildCount()));
		mViewDistrict.setCurrentItem(0);
		mCurrentDistrictName = ((ArrayWheelAdapter)mViewDistrict.getViewAdapter()).getItemText(0).toString();
	}

	/**
	 * 根据当前的省，更新市WheelView的信息
	 */
	private void updateCities() {
		int pCurrent = mViewProvince.getCurrentItem();
		mCurrentProviceName = mProvinceDatas[pCurrent];
		String[] cities = mCitisDatasMap.get(mCurrentProviceName);
		if (cities == null) {
			cities = new String[] { "" };
		}
		if(INVALID_ID == mItemResource){
			mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(mContext, cities));
		}else{
			mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(mContext, cities,mItemResource,mItemTextResource));
		}
		mViewCity.setCurrentItem(0);
		updateAreas();
	}
	/**
	 * 解析省市区的XML数据
	 */
    protected void initProvinceDatas()
	{
		List<ProvinceModel> provinceList = null;
    	AssetManager asset = mContext.getAssets();
        try {
            InputStream input = asset.open("province_data.xml");
            // 创建一个解析xml的工厂对象
			SAXParserFactory spf = SAXParserFactory.newInstance();
			// 解析xml
			SAXParser parser = spf.newSAXParser();
			XmlParserHandler handler = new XmlParserHandler();
			parser.parse(input, handler);
			input.close();
			// 获取解析出来的数据
			provinceList = handler.getDataList();
			//*/ 初始化默认选中的省、市、区
			if (provinceList!= null && !provinceList.isEmpty()) {
				mCurrentProviceName = provinceList.get(0).getName();
				List<CityModel> cityList = provinceList.get(0).getCityList();
				if (cityList!= null && !cityList.isEmpty()) {
					mCurrentCityName = cityList.get(0).getName();
					List<DistrictModel> districtList = cityList.get(0).getDistrictList();
					mCurrentDistrictName = districtList.get(0).getName();
					mCurrentZipCode = districtList.get(0).getZipcode();
				}
			}
			//*/
			mProvinceDatas = new String[provinceList.size()];
        	for (int i=0; i< provinceList.size(); i++) {
        		// 遍历所有省的数据
        		mProvinceDatas[i] = provinceList.get(i).getName();
        		List<CityModel> cityList = provinceList.get(i).getCityList();
        		String[] cityNames = new String[cityList.size()];
        		for (int j=0; j< cityList.size(); j++) {
        			// 遍历省下面的所有市的数据
        			cityNames[j] = cityList.get(j).getName();
        			List<DistrictModel> districtList = cityList.get(j).getDistrictList();
        			String[] distrinctNameArray = new String[districtList.size()];
        			DistrictModel[] distrinctArray = new DistrictModel[districtList.size()];
        			for (int k=0; k<districtList.size(); k++) {
        				// 遍历市下面所有区/县的数据
        				DistrictModel districtModel = new DistrictModel(districtList.get(k).getName(), districtList.get(k).getZipcode());
        				// 区/县对于的邮编，保存到mZipcodeDatasMap
        				mZipcodeDatasMap.put(districtList.get(k).getName(), districtList.get(k).getZipcode());
        				distrinctArray[k] = districtModel;
        				distrinctNameArray[k] = districtModel.getName();
        			}
        			// 市-区/县的数据，保存到mDistrictDatasMap
        			mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
        		}
        		// 省-市的数据，保存到mCitisDatasMap
        		mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
        	}
        } catch (Throwable e) {  
            e.printStackTrace();  
        } finally {
        	
        } 
	}
	


}
