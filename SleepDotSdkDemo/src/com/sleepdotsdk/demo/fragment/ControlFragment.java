package com.sleepdotsdk.demo.fragment;

import com.sleepace.sdk.interfs.IDataCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.util.LogUtil;
import com.sleepace.sdk.util.StatusCode;
import com.sleepdotsdk.demo.MainActivity;
import com.sleepdotsdk.demo.R;
import com.sleepdotsdk.demo.SleepTimeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ControlFragment extends BaseFragment {
	
	private Button btnAutoStart, btnCollectStatus, /*btnSleepStatus,*/ btnStopCollect;
	private TextView tvCollectStatus/*, tvSleepStatus*/;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_control, null);
		LogUtil.log(TAG+" onCreateView-----------");
		findView(view);
		initListener();
		initUI();
		return view;
	}
	
	
	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		btnAutoStart = (Button) root.findViewById(R.id.btn_set_auto_collect);
		btnCollectStatus = (Button) root.findViewById(R.id.btn_collect_status);
//		btnSleepStatus = (Button) root.findViewById(R.id.btn_sleep_status);
		btnStopCollect = (Button) root.findViewById(R.id.btn_stop_collect);
		
		tvCollectStatus = (TextView) root.findViewById(R.id.tv_collect_status);
//		tvSleepStatus = (TextView) root.findViewById(R.id.tv_sleep_status);
	}


	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		btnAutoStart.setOnClickListener(this);
		btnCollectStatus.setOnClickListener(this);
//		btnSleepStatus.setOnClickListener(this);
		btnStopCollect.setOnClickListener(this);
	}


	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.control);
		btnAutoStart.setEnabled(getSleepDotHelper().isConnected());
		btnCollectStatus.setEnabled(getSleepDotHelper().isConnected());
//		btnSleepStatus.setEnabled(getSleepDotHelper().isConnected());
		btnStopCollect.setEnabled(false);
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		printLog(null);
		LogUtil.log(TAG+" onResume collectStatus:" + MainActivity.collectStatus);
		if(MainActivity.collectStatus == 1){
			tvCollectStatus.setText(R.string.working_state_ing);
			btnStopCollect.setEnabled(true);
		}else if(MainActivity.collectStatus == 0){
			tvCollectStatus.setText(R.string.working_state_not);
			
		}else if(MainActivity.collectStatus == -1){
			tvCollectStatus.setText(R.string.working_state_no);
		}
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if(v == btnAutoStart){
			
			Intent intent = new Intent(mActivity, SleepTimeActivity.class);
        	startActivity(intent);
			
		}else if(v == btnCollectStatus){
			printLog(R.string.getting_device_status);
			getSleepDotHelper().getCollectionStatus(1000, new IDataCallback<Byte>() {
				@Override
				public void onDataCallback(final CallbackData<Byte> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							
							if(checkStatus(cd)){
								MainActivity.collectStatus = cd.getResult();
								LogUtil.log(TAG+" getCollectionStatus collS:" + MainActivity.collectStatus);
								int textRes = MainActivity.collectStatus == 1 ? R.string.working_state_ing : R.string.working_state_not;
								tvCollectStatus.setText(textRes);
								String str = getString(R.string.get_working_status, getString(textRes));
								printLog(str);
								
								if(MainActivity.collectStatus == 1){//采集中
									btnStopCollect.setEnabled(true);
								}else{//非采集
									btnStopCollect.setEnabled(false);
								}
							}else{
								if(cd.getStatus() == StatusCode.STATUS_DISCONNECT){
									tvCollectStatus.setText(R.string.working_state_no);
								}
							}
						}
					});
				}
			});
		}/*else if(v == btnSleepStatus){
			printLog(R.string.getting_sleep_state);
			getSleepDotHelper().getSleepStatus(1000, new IDataCallback() {
				@Override
				public void onDataCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								SleepStatus status = (SleepStatus) cd.getResult();
								if(status.getSleepStatus() == 1){//入睡
									tvSleepStatus.setText(R.string.sleep_);
									printLog(getString(R.string.get_sleep, getString(R.string.sleep_)));
								}else if(status.getWakeupStatus() == 1){//清醒
									tvSleepStatus.setText(R.string.wake_);
									printLog(getString(R.string.get_sleep, getString(R.string.wake_)));
								}else{
									printLog(getString(R.string.get_sleep, getString(R.string.sleep_) +":" + status.getSleepStatus()+","+getString(R.string.wake_)+":"+status.getWakeupStatus()));
								}
							}
						}
					});
				}
			});
		}*/else if(v == btnStopCollect){
			printLog(R.string.notified_acquisition_off);
			getSleepDotHelper().stopCollection(1000, new IDataCallback() {
				@Override
				public void onDataCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(checkStatus(cd)){
								MainActivity.collectStatus = 0;
								printLog(R.string.close_acquisition_success);
//								tvCollectStatus.setText(R.string.working_state_not);
								btnStopCollect.setEnabled(false);
								//tvSleepStatus.setText(null);
							}
						}
					});
				}
			});
		}
	}

}

















