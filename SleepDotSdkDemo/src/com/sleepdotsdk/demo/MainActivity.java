package com.sleepdotsdk.demo;

import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.sleepdot.SleepDotHelper;
import com.sleepdotsdk.demo.fragment.ControlFragment;
import com.sleepdotsdk.demo.fragment.DataFragment;
import com.sleepdotsdk.demo.fragment.DeviceFragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends BaseActivity{
	
	private RadioGroup rgTab;
	private RadioButton rbDevice;
	private FragmentManager fragmentMgr;
	private Fragment deviceFragment, controlFragment, dataFragment;
	private BleDevice device;
	private SleepDotHelper sleepDotHelper;
	private ProgressDialog upgradeDialog;
	//缓存数据
	public static String deviceName, deviceId, power, version;
	public static byte collectStatus = -100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sleepDotHelper = SleepDotHelper.getInstance(this);
		findView();
		initListener();
		initUI();
	}
	
	
	
	@Override
	protected void findView() {
		// TODO Auto-generated method stub
		super.findView();
		rgTab = (RadioGroup) findViewById(R.id.rg_tab);
		rbDevice = (RadioButton) findViewById(R.id.rb_device);
	}


	@Override
	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		rgTab.setOnCheckedChangeListener(checkedChangeListener);
	}


	@Override
	protected void initUI() {
		// TODO Auto-generated method stub
		super.initUI();
		device = (BleDevice) getIntent().getSerializableExtra("device");
		fragmentMgr = getFragmentManager();
		deviceFragment = new DeviceFragment();
		controlFragment = new ControlFragment();
		dataFragment = new DataFragment();
		rbDevice.setChecked(true);
		ivBack.setImageResource(R.drawable.tab_btn_scenes_home);
		
		upgradeDialog = new ProgressDialog(this);
		upgradeDialog.setMessage(getString(R.string.fireware_updateing, getString(R.string.device_name)));
		upgradeDialog.setCancelable(false);
		upgradeDialog.setCanceledOnTouchOutside(false);
	}
	
	
	public void setTitle(int res){
		tvTitle.setText(res);
	}
	
	public BleDevice getDevice() {
		return device;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == ivBack){
			exit();
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	public void exit(){
		sleepDotHelper.disconnect();
		clearCache();
		Intent intent = new Intent(this, SplashActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	
	
	private void clearCache(){
		collectStatus = -100;
		deviceName = null;
		deviceId = null;
		power = null;
		version = null;
	}
	
	
	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			FragmentTransaction trans = fragmentMgr.beginTransaction();
			if(checkedId == R.id.rb_device){
				trans.replace(R.id.content, deviceFragment);
			}else if(checkedId == R.id.rb_control){
				trans.replace(R.id.content, controlFragment);
			}else if(checkedId == R.id.rb_data){
				trans.replace(R.id.content, dataFragment);
			}
			trans.commit();
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			
		}
	}
	
	
	
	public void showUpgradeDialog(){
		upgradeDialog.show();
	}
	
	public void hideUpgradeDialog(){
		upgradeDialog.dismiss();
	}
	
}








































