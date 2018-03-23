package com.sleepdotsdk.demo;

import java.util.Calendar;

import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.sleepdot.SleepDotHelper;
import com.sleepace.sdk.util.LogUtil;
import com.sleepace.sdk.util.StringUtil;
import com.sleepdotsdk.demo.view.wheelview.NumericWheelAdapter;
import com.sleepdotsdk.demo.view.wheelview.OnItemSelectedListener;
import com.sleepdotsdk.demo.view.wheelview.WheelAdapter;
import com.sleepdotsdk.demo.view.wheelview.WheelView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class SleepTimeActivity extends BaseActivity {
	private View startTimeView, endTimeView;
    private TextView tvStartTime, tvEndTime;
    private WheelView wvHour, wvMinute;
    private Button btnSave;
    private WheelAdapter hourAdapter, minuteAdapter;

    private String[] hourItems = new String[24];
    private String[] minuteItems = new String[60];
    private SleepDotHelper sleepDotHelper;
    private int sHour = 22, eHour = 7, sMinute = 0, eMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeptime);
        sleepDotHelper = SleepDotHelper.getInstance(this);
        findView();
        initListener();
        initUI();
    }


    public void findView() {
    	super.findView();
        startTimeView = findViewById(R.id.layout_start_time);
        endTimeView = findViewById(R.id.layout_end_time);
        tvStartTime = (TextView) findViewById(R.id.tv_start_time);
        tvEndTime = (TextView) findViewById(R.id.tv_end_time);
        wvHour = (WheelView) findViewById(R.id.hour);
        wvMinute = (WheelView) findViewById(R.id.minute);
        btnSave = (Button) findViewById(R.id.btn_save);
    }

    public void initListener() {
    	super.initListener();
        startTimeView.setOnClickListener(this);
        endTimeView.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    public void initUI() {
    	super.initUI();
        tvTitle.setText(R.string.set_auto_monitor);
        for (int i = 0; i < minuteItems.length; i++) {
        	if (i < hourItems.length) {
        		hourItems[i] = StringUtil.DF_2.format(i);
        	}
            minuteItems[i] = StringUtil.DF_2.format(i);
        }

        initWheelView();
        initView(sHour, sMinute, eHour, eMinute);
    }


    private void initWheelView() {

    	startTimeView.setBackgroundColor(getResources().getColor(R.color.COLOR_8));
    	
    	wvHour.setAdapter(new NumericWheelAdapter(0, 23));
        wvHour.setTextSize(20);
        wvHour.setCyclic(true);
        wvHour.setOnItemSelectedListener(onHourItemSelectedListener);

        wvMinute.setAdapter(new NumericWheelAdapter(0, 59));
        wvMinute.setTextSize(20);
        wvMinute.setCyclic(true);
        wvMinute.setOnItemSelectedListener(onMiniteItemSelectedListener);

        wvHour.setRate(5 / 4.0f);
        wvMinute.setRate(1 / 2.0f);
        
    }


    /**
     * 设置小时时间
     *
     * @param type
     */
    private void setHourTime(int type, int index) {
        if (type == START_TIME) {
        	sHour = index;
            setStartTimeText();
        } else if (type == END_TIME) {
        	eHour = index;
            setEndTimeText();
        }
    }

    /**
     * 设置分钟时间
     *
     * @param type
     */
    private void setMinuteTime(int type, int index) {
        if (type == START_TIME) {
            sMinute = index;
            setStartTimeText();
        } else if (type == END_TIME) {
            eMinute = index;
            setEndTimeText();
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    private static final int START_TIME = 0;
    private static final int END_TIME = 1;
    private int CURRENT_TIME = START_TIME;

    @Override
    public void onClick(View v) {
    	super.onClick(v);
        if (v == startTimeView) {
            CURRENT_TIME = START_TIME;
            if (v.getTag() == null) {
                v.setTag("checked");
                endTimeView.setTag(null);
                v.setBackgroundResource(R.drawable.clock_sleep_background);
                endTimeView.setBackgroundResource(R.drawable.transparent);
                setWheelViewText(sHour, sMinute);
                startTimeView.setBackgroundColor(getResources().getColor(R.color.COLOR_8));
            }
        } else if (v == endTimeView) {
            CURRENT_TIME = END_TIME;
            if (v.getTag() == null) {
                v.setTag("checked");
                startTimeView.setTag(null);
                v.setBackgroundResource(R.drawable.clock_sleep_background);
                startTimeView.setBackgroundResource(R.drawable.transparent);
                setWheelViewText(eHour, eMinute);
                endTimeView.setBackgroundColor(getResources().getColor(R.color.COLOR_8));
            }
        }else if(v == btnSave){
        	
        	Calendar cal = Calendar.getInstance();
        	cal.set(Calendar.HOUR_OF_DAY, sHour);
        	cal.set(Calendar.MINUTE, sMinute);
        	cal.set(Calendar.SECOND, 0);
        	
        	int stime = (int) (cal.getTimeInMillis() / 1000);
        	
        	cal.set(Calendar.HOUR_OF_DAY, eHour);
        	cal.set(Calendar.MINUTE, eMinute);
        	cal.set(Calendar.SECOND, 0);
        	
        	int etime = (int) (cal.getTimeInMillis() / 1000);
        	if(etime < stime){
        		etime += 24 * 60 * 60;
        	}
        	
        	printLog(getString(R.string.writing_monitoring_period__device, String.format("%02d:%02d", sHour, sMinute), String.format("%02d:%02d", eHour, eMinute)));
        	
        	final int timeLength = (etime - stime) / 60;
        	sleepDotHelper.setAutoCollection(sHour, sMinute, timeLength, 1000, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					LogUtil.log(TAG+" setAutoCollection hour:" + sHour+",minute:" + sMinute + ",duration:" + timeLength +"," + cd);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							String msg = null;
							if(cd.isSuccess()){
								msg = getString(R.string.write_success);
							}else{
								msg = getErrMsg(cd.getStatus());
							}
							printLog(msg);
							finish();
						}
					});
				}
			});
        }
    }


    private void setTimeText() {
        setStartTimeText();
        setEndTimeText();
    }

    private void setStartTimeText() {
    	tvStartTime.setText(StringUtil.DF_2.format(sHour) + ":" + StringUtil.DF_2.format(sMinute));
    }

    private void setEndTimeText() {
    	tvEndTime.setText(StringUtil.DF_2.format(eHour) + ":" + StringUtil.DF_2.format(eMinute));
    }

    private void setWheelViewText(int hour, int minute) {
    	wvHour.setCurrentItem(hour);
        wvMinute.setCurrentItem(minute);
    }

    private void initView(int sH, int sM, int eH, int eM) {
        setTimeText();
        setWheelViewText(sH, sM);
    }


    //更新控件快速滑动
    private OnItemSelectedListener onHourItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(int index) {
            setHourTime(CURRENT_TIME, index);
        }
    };

    private OnItemSelectedListener onMiniteItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(int index) {
            setMinuteTime(CURRENT_TIME, index);
        }
    };




    
}












