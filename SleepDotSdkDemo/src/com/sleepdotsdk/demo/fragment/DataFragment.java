package com.sleepdotsdk.demo.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.DeviceType;
import com.sleepace.sdk.sleepdot.constants.SleepConfig;
import com.sleepace.sdk.sleepdot.domain.Analysis;
import com.sleepace.sdk.sleepdot.domain.Detail;
import com.sleepace.sdk.sleepdot.domain.HistoryData;
import com.sleepace.sdk.sleepdot.domain.Summary;
import com.sleepace.sdk.sleepdot.util.AnalysisUtil;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.util.TimeUtil;
import com.sleepdotsdk.demo.R;
import com.sleepdotsdk.demo.bean.CvPoint;
import com.sleepdotsdk.demo.util.DensityUtil;
import com.sleepdotsdk.demo.util.HistoryDataComparator;
import com.sleepdotsdk.demo.view.graphview.GraphView;
import com.sleepdotsdk.demo.view.graphview.GraphViewSeries;
import com.sleepdotsdk.demo.view.graphview.GraphViewStyle;
import com.sleepdotsdk.demo.view.graphview.LineGraphView;
import com.sleepdotsdk.demo.view.graphview.LineGraphView.BedBean;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataFragment extends BaseFragment {

	private LayoutInflater inflater;
	private LinearLayout reportLayout;
	private Button btnAnalysis, btnImitateData;
	private HistoryData imitateData;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.fragment_data, null);
		SdkLog.log(TAG + " onCreateView-----------");
		findView(view);
		initListener();
		initUI();
		return view;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		btnAnalysis = (Button) root.findViewById(R.id.btn_sleep_analysis);
		btnImitateData = (Button) root.findViewById(R.id.btn_imitate_data);
		reportLayout = (LinearLayout) root.findViewById(R.id.layout_chart);
	}

	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		btnAnalysis.setOnClickListener(this);
		btnImitateData.setOnClickListener(this);
	}

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.report);
		btnAnalysis.setEnabled(getSleepDotHelper().isConnected());
		printLog(null);
		initDemoData();

		progressDialog = new ProgressDialog(mActivity);
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.setMessage(getString(R.string.data_analyzed));
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if (v == btnAnalysis) {
			progressDialog.show();
			printLog(R.string.data_analyzed);
			Calendar cal = Calendar.getInstance();
			int endTime = (int) (cal.getTimeInMillis() / 1000);
			cal.add(Calendar.DATE, -30);
			cal.set(Calendar.HOUR_OF_DAY, 20);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			int startTime = (int) (cal.getTimeInMillis() / 1000);//1505754919
			getSleepDotHelper().historyDownload(0, endTime, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							progressDialog.dismiss();
							if (checkStatus(cd)) {
								List<HistoryData> list = (List<HistoryData>) cd.getResult();
//								SdkLog.log(TAG+" download data before sort:" + list);
								if (list != null && list.size() > 0) {
									Collections.sort(list, new HistoryDataComparator());
//									SdkLog.log(TAG+" download data after sort:" + list);
									for(HistoryData tmp : list) {
										SdkLog.log(TAG+" download summary:" + tmp.getSummary());
										SdkLog.log(TAG+" download detail:" + tmp.getDetail());
										SdkLog.log(TAG+" download analysis:" + tmp.getAnaly());
										if(tmp.getAnaly() != null) {
											SdkLog.log(TAG+" download analysis SleepCurveArray:" + Arrays.toString(tmp.getAnaly().getSleepCurveArray()));
										}
									}
									
									HistoryData historyData = list.get(0);
									initReportView(historyData);
								}else{
									printLog(R.string.no_data);
								}
							}
						}
					});
				}
			});
		} else if (v == btnImitateData) {
			printLog(R.string.simulation_report);
			initReportView(imitateData);
		}
	}

	private void initDemoData() {
		// TODO Auto-generated method stub
		//imitateData = createLongReportData(1511538540, 550);
		imitateData = createLongReportData(1593433800, 629);
	}

	private void initReportView(HistoryData historyData) {
		reportLayout.removeAllViews();
		if(historyData.getDataStatus() == -1){
			printLog(R.string.data_exception);
			return;
		}
		
		String ver = null;
		Analysis analysis = historyData.getAnaly();
		if(analysis != null){
			ver = analysis.getAlgorithmVer();
		}
		if(ver == null) ver = "";
		printLog(getString(R.string.generate_sleep_report, ver));
		
		View view = inflater.inflate(R.layout.layout_report, null);
		LinearLayout mainGraph = (LinearLayout) view.findViewById(R.id.layout_chart);
		GraphView.GraphViewData[] mainData = getSleepGraphData(historyData.getAnaly(), 60, DeviceType.DEVICE_TYPE_SLEEPDOT);

		int think = (int) (DensityUtil.dip2px(mActivity, 1) * 0.8);
		final LineGraphView main_graph = new LineGraphView(mActivity, "");
		if (mainData == null) {
			mainData = new GraphView.GraphViewData[0];
		}

		GraphViewSeries series = new GraphViewSeries("", new GraphViewSeries.GraphViewSeriesStyle(getResources().getColor(R.color.COLOR_2), think), mainData);
		main_graph.addSeries(series);
		main_graph.isMySelft = true;
		if (mainData.length > 0) {
			main_graph.setViewPort(mainData[0].getX(), mainData[mainData.length - 1].getX());
		} else {
			main_graph.setViewPort(0, 10);
		}

		main_graph.setMinMaxY(-3, 2);
		main_graph.setVerticalLabels(
				new String[] { "", getString(R.string.wake_), getString(R.string.light_), getString(R.string.mid_), getString(R.string.deep_), "" });

		main_graph.setBeginAndOffset(historyData.getSummary().getStartTime(), TimeUtil.getTimeZoneHour(), 0);
		main_graph.setScalable(false);
		main_graph.setScrollable(false);
		main_graph.setShowLegend(false);
		main_graph.setMainPoint(points);
		main_graph.setDrawBackground(true);
		main_graph.testVLabel = "wake";
		main_graph.setPauseData(apneaPauseList, heartPauseList);

		// 说明没有 数据
		if (mainData.length == 0) {
			main_graph.setHorizontalLabels(new String[] { "1", "2", "3", "4", "5", "6", "7" });
		}

		GraphViewStyle gvs = main_graph.getGraphViewStyle();
		gvs.setVerticalLabelsAlign(Paint.Align.CENTER);
		gvs.setTextSize(DensityUtil.sp2px(mActivity, 12));
		gvs.setGridColor(Color.parseColor("#668492a6"));
		gvs.setHorizontalLabelsColor(getResources().getColor(R.color.COLOR_3));
		gvs.setVerticalLabelsColor(getResources().getColor(R.color.COLOR_3));
		gvs.setLegendBorder(DensityUtil.dip2px(mActivity, 12));
		gvs.setNumVerticalLabels(4);
		gvs.setVerticalLabelsWidth(DensityUtil.dip2px(mActivity, 40));
		gvs.setNumHorizontalLabels(7);
		gvs.setLegendWidth(DensityUtil.dip2px(mActivity, 30));
		main_graph.setBedBeans(bedBeans);
		main_graph.setSleepUpIn(SleepInUP);
		mainGraph.removeAllViews();
		mainGraph.addView(main_graph);
		// main_graph.setOnHeartClickListener(heartClick);
//		main_graph.setOnGraphViewScrollListener(new GraphView.OnGraphViewScrollListener() {
//			@Override
//			public void onTouchEvent(MotionEvent event, GraphView v) {
//				main_graph.onMyTouchEvent(event);
//			}
//		});
		// main_graph.setTouchDisallowByParent(true);

		TextView tvCollectDate = (TextView) view.findViewById(R.id.tv_collect_date);
		TextView tvSleepScore = (TextView) view.findViewById(R.id.tv_sleep_score);
		LinearLayout layoutDeductionPoints = (LinearLayout) view.findViewById(R.id.layout_deduction_points);
		TextView tvSleepTime = (TextView) view.findViewById(R.id.tv_sleep_time);
		TextView tvSleepDuration = (TextView) view.findViewById(R.id.tv_sleep_duration);
		TextView tvAsleepDuration = (TextView) view.findViewById(R.id.tv_fall_asleep_duration);
		TextView tvDeepSleepPer = (TextView) view.findViewById(R.id.tv_deep_sleep_proportion);
		TextView tvMidSleepPer = (TextView) view.findViewById(R.id.tv_medium_sleep_proportion);
		TextView tvLightSleepPer = (TextView) view.findViewById(R.id.tv_light_sleep_proportion);
		TextView tvWakeSleepPer = (TextView) view.findViewById(R.id.tv_Sober_proportion);
		TextView tvWakeTimes = (TextView) view.findViewById(R.id.tv_wake_times);

		if (analysis != null) {
			tvCollectDate.setText(dateFormat.format(new Date(historyData.getSummary().getStartTime() * 1000l)));
			tvSleepScore.setText(String.valueOf(analysis.getSleepScore()));

			int fallSleep = analysis.getFallsleepTimeStamp();
			int wakeUp = analysis.getWakeupTimeStamp();
			tvSleepTime.setText(timeFormat.format(new Date(fallSleep * 1000l)) + "(" + getString(R.string.asleep_point) + ")-"
					+ timeFormat.format(new Date(wakeUp * 1000l)) + "(" + getString(R.string.awake_point) + ")");

			int duration = analysis.getDuration();
			int hour = duration / 60;
			int minute = duration % 60;
			tvSleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));

			List<DeductItems> list = new ArrayList<DataFragment.DeductItems>();

			if (analysis.getMd_body_move_decrease_scale() != 0) {// 躁动不安
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.restless);
				item.score = analysis.getMd_body_move_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_perc_effective_sleep_decrease_scale() > 0) {// 良性睡眠扣分
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.benign_sleep);
				item.score = analysis.getMd_perc_effective_sleep_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_sleep_time_increase_scale() > 0) {// 睡眠时间过长
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.actual_sleep_long);
				item.score = analysis.getMd_sleep_time_increase_scale();
				list.add(item);
			}

			if (analysis.getMd_sleep_time_decrease_scale() > 0) {// 睡眠时长过短
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.actual_sleep_short);
				item.score = analysis.getMd_sleep_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_sleep_efficiency_decrease_scale() > 0) {// 睡眠效率
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.sleepace_efficient_low);
				item.score = analysis.getMd_sleep_efficiency_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_fall_asleep_time_decrease_scale() > 0) {// 入睡时间长
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.fall_asleep_hard);
				item.score = analysis.getMd_fall_asleep_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_start_time_decrease_scale() > 0) {// 上床时间较晚
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.start_sleep_time_too_latter);
				item.score = analysis.getMd_start_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_wake_cnt_decrease_scale() > 0) {// 清醒次数较多
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.wake_times_too_much);
				item.score = analysis.getMd_wake_cnt_decrease_scale();
				list.add(item);
			}

			int size = list.size();
			if(size > 0){
				for(int i=0;i<size;i++){
					View pointView = inflater.inflate(R.layout.layout_deduction_points, null);
					TextView tvDesc = (TextView) pointView.findViewById(R.id.tv_deduction_desc);
					TextView tvScore = (TextView) pointView.findViewById(R.id.tv_deduction_score);
					tvDesc.setText((i+1)+"."+list.get(i).desc);
					tvScore.setText("-" + Math.abs(list.get(i).score));
					layoutDeductionPoints.addView(pointView);
				}
			}
			
			hour = analysis.getFallAlseepAllTime() / 60;
			minute = analysis.getFallAlseepAllTime() % 60;
			tvAsleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));

			tvDeepSleepPer.setText(analysis.getDeepSleepPerc() + "%");
			tvMidSleepPer.setText(analysis.getInSleepPerc() + "%");
			tvLightSleepPer.setText(analysis.getLightSleepPerc() + "%");
			tvWakeSleepPer.setText(analysis.getWakeSleepPerc() + "%");
			tvWakeTimes.setText(analysis.getWakeTimes() + getString(R.string.unit_times));
		}

		reportLayout.addView(view);

	}
	
	
	class DeductItems {
		String desc;
		int score;
	}
	

	private HistoryData createLongReportData(int starttime, int count) {
		HistoryData historyData = new HistoryData();
		Summary summ = new Summary();
		summ.setStartTime(starttime);
		summ.setRecordCount(count);
		historyData.setSummary(summ);

		Detail detail = new Detail();
		int[] feature1 = new int[] { 65446, 3, 5, 4, 4, 4, 3, 4, 3, 4, 4, 4, 5, 4, 3, 4, 4, 5, 4, 4, 4, 4, 6, 5, 5, 2071, 4, 6, 6, 4, 6, 4, 5, 5, 4, 4, 6, 5, 4244, 4, 4, 3, 3, 4, 4, 8, 5, 4, 5, 11, 4,
				4, 5, 1036, 3, 3091, 5, 5, 7670, 3, 4, 3, 4, 4, 5, 5, 3, 4, 2062, 4, 5, 4, 5, 4, 4, 4, 3, 5, 3, 4, 3, 4, 4, 5, 3088, 3, 4160, 4, 5, 9, 7, 15359, 4, 3, 5, 3, 4, 4, 4, 4, 4, 4, 5, 4, 4,
				4, 3, 4, 3, 4, 3, 3, 5, 3, 4, 4, 5, 4, 4, 4, 3, 4, 4, 3, 4, 3, 4, 4, 4, 3, 4, 4, 3, 4, 3, 4, 4, 4, 5, 4, 4, 4, 3, 3, 4, 3, 3, 4, 4, 3, 3, 4, 3, 5, 5, 3, 4, 4, 3, 4, 3, 4, 3, 4, 4, 4,
				4, 4, 4, 4, 4, 3, 4, 4, 3, 3, 4, 3, 3, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 5, 3, 3, 4, 4, 3, 5, 3, 4, 3, 3, 4, 3, 4, 4, 4, 4, 3, 3, 3, 3, 4, 3, 4, 3, 4, 4, 4, 3, 5, 5, 4, 4, 3, 3, 4, 4, 5,
				4, 4, 3, 3, 3, 3, 3, 4, 3, 4, 4, 4, 5, 5, 4, 3, 3, 5, 3, 4, 4, 4, 3, 4, 4, 3, 3, 4, 3, 5, 4, 3, 4, 4, 4, 3, 3, 3, 3, 4, 4, 4, 4, 5, 4, 4, 4, 4, 3, 3, 3, 4, 3, 4, 3, 4, 4, 4, 4, 4, 4,
				3, 3, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 5, 4, 5, 3, 4, 3, 5, 4, 3, 3, 4, 4, 5, 3, 4, 3, 4, 4, 3, 3, 3, 4, 4, 4, 4, 3, 4, 4, 3, 4, 4, 3, 5, 4, 3, 4, 4, 3, 4, 3, 3, 3, 4,
				3, 3, 3, 4, 3, 5, 4, 4, 3, 4, 3, 3, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 5, 3, 4, 4, 4, 4, 3, 4, 4, 5, 4, 3, 3, 3, 3, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 3, 3, 4, 3, 3, 3, 4, 4, 4, 4, 3, 4, 4, 4,
				4, 4, 3, 3, 5, 4, 3, 3, 4, 4, 4, 4, 4, 4, 3, 4, 4, 3, 5, 3, 3, 4, 3, 3, 3, 4, 4, 3, 4, 4, 4, 4, 3, 3, 4, 4, 3, 3, 4, 3, 3, 4, 4, 3, 3, 4, 3, 3, 3, 4, 3, 3, 4, 3, 3, 4, 4, 3, 4, 3, 3,
				4, 3, 4, 3, 3, 4, 4, 3, 4, 3, 3, 4, 5, 5, 3, 4, 3, 5, 4, 3, 3, 3, 4, 4, 4, 3, 4, 4, 4, 5, 3, 4, 3, 3, 4, 4, 3, 3, 3, 3, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 3, 3, 4, 3, 4, 3, 4, 3, 3, 4, 4,
				5, 4, 4, 4, 3, 3, 4, 4, 4, 3, 4, 4, 3, 3, 4, 3, 4, 4, 4, 3, 4, 4, 4, 4, 4, 3, 4, 3, 4, 4, 4, 4, 4, 4, 3, 3, 4, 3, 3, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 4, 3, 4, 3, 3, 4, 3, 3, 3, 4, 3, 3,
				4, 4, 4, 4, 3, 3, 5, 4, 4, 4, 3, 3, 3, 4, 4, 4, 4, 4, 4, 3, 4, 3, 3, 3, 3, 3, 4, 3, 4, 4, 4, 3, 4, 3, 4, 3, 3};

		int[] feature2 = new int[] { 62404, 4, 3, 3, 4, 3, 4, 1031, 4, 3, 4, 3, 4, 3, 3, 5, 6, 4, 4, 6, 4, 5, 1034, 1031, 5, 3102, 4, 1032, 6, 6, 6, 5, 6, 4, 4, 4, 6, 5, 2161, 5, 3, 3, 3, 4, 6, 3082,
				4, 4, 1031, 2063, 5, 4, 4, 2068, 1051, 1031, 4, 4, 6307, 3, 3, 3, 1032, 3, 3, 5, 4, 5, 1040, 4, 4, 4, 3, 3, 6, 4, 4, 4, 3, 5, 4, 3, 3, 4, 2075, 4, 4179, 3, 4, 1034, 4105, 14915, 3, 3,
				3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4,
				3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 4, 4, 3, 3, 3, 4,
				4, 3, 3, 4, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 4, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 4, 4, 3,
				3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3,
				3, 4, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3,
				3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 4, 3, 3, 4, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 4, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3,
				3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 4, 3, 4, 3, 3, 4, 4, 4, 3, 3, 4, 3, 3, 3, 4, 3, 4, 3, 4, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};

		int[] statusFlag = new int[] { 0, 32, 32, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, -96, 32, 32, 32, 32, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33,
				33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33};

		detail.setFeature1(feature1);
		detail.setFeature2(feature2);
		detail.setStatusFlag(statusFlag);
		historyData.setDetail(detail);

		Analysis analysis = AnalysisUtil.analysData(historyData);
		SdkLog.log(TAG+" analysis:" + analysis);
		 
//		analysis = new Analysis();
//		analysis.setMd_body_move_decrease_scale((short) 5);
//		analysis.setMd_sleep_time_decrease_scale((short) 1);
//		analysis.setMd_fall_asleep_time_decrease_scale((short) 5);
//		analysis.setMd_wake_cnt_decrease_scale((short) 4);
//		analysis.setMd_sleep_efficiency_decrease_scale((short) 7);
//		analysis.setMd_perc_effective_sleep_decrease_scale((short) 14);
//
//		analysis.setDuration(413);
//		analysis.setSleepScore(64);
//		analysis.setLightSleepAllTime(263);
//		analysis.setInSleepAllTime(72);
//		analysis.setDeepSleepAllTime(28);
//		analysis.setDeepSleepPerc(6);
//		analysis.setInSleepPerc(15);
//		analysis.setLightSleepPerc(56);
//		analysis.setWakeSleepPerc(23);
//		analysis.setWakeTimes(2);
//		analysis.setWakeAndLeaveBedBeforeAllTime(6);
//		analysis.setFallAlseepAllTime(50);
//		
//		int fallsleepTimeStamp = summ.getStartTime() + analysis.getFallAlseepAllTime() * 60;
//        int wakeupTimeStamp = summ.getStartTime()  + (summ.getRecordCount() - analysis.getWakeAndLeaveBedBeforeAllTime()) * 60;
//        analysis.setFallsleepTimeStamp(fallsleepTimeStamp);      
//        analysis.setWakeupTimeStamp(wakeupTimeStamp);
//        
//		analysis.setSleepCurveArray(new float[] { 0.062541f, 0.058725f, 0.065542f, 0.069989f, 0.065416f, 0.068526f, 0.064941f, 0.060794f, 0.061167f, 0.058397f,
//				0.063292f, 0.065438f, 0.079831f, 0.079625f, 0.074011f, 0.062523f, 0.064089f, 0.059355f, 0.054355f, 0.072816f, 0.063374f, 0.057164f, 0.063779f,
//				0.050025f, 0.053493f, 0.071147f, 0.083149f, 0.069755f, 0.060164f, 0.054402f, 0.057915f, 0.072591f, 0.068803f, 0.065573f, 0.053346f, 0.043401f,
//				0.062326f, 0.067713f, 0.059349f, 0.05872f, 0.066232f, 0.064379f, 0.050265f, 0.045262f, 0.062923f, 0.074916f, 0.068195f, 0.053895f, 0.054209f,
//				0.056993f, 0.0625f, 0.25484f, 0.441328f, 0.621965f, 0.799565f, 0.971314f, 1.140027f, 1.302887f, 1.480727f, 1.646402f, 1.797098f, 1.932816f,
//				2.053554f, 2.159314f, 2.254555f, 2.339279f, 2.409024f, 2.474205f, 2.534821f, 2.590875f, 2.642365f, 2.68929f, 2.731651f, 2.76945f, 2.802684f,
//				2.831354f, 2.855461f, 2.860024f, 2.850133f, 2.822317f, 2.78461f, 2.743186f, 2.698045f, 2.649185f, 2.596609f, 2.540316f, 2.480306f, 2.416578f,
//				2.349133f, 2.277971f, 2.196918f, 2.114136f, 2.029627f, 1.94339f, 1.855426f, 1.765733f, 1.674313f, 1.581165f, 1.506072f, 1.449034f, 1.406334f,
//				1.381689f, 1.36275f, 1.349519f, 1.341994f, 1.340175f, 1.344064f, 1.353658f, 1.36896f, 1.389968f, 1.416683f, 1.453289f, 1.488677f, 1.522848f,
//				1.5558f, 1.587534f, 1.61805f, 1.647348f, 1.67021f, 1.683175f, 1.686242f, 1.68127f, 1.666401f, 1.647808f, 1.625493f, 1.599454f, 1.569691f,
//				1.536206f, 1.498997f, 1.451635f, 1.397837f, 1.337603f, 1.281086f, 1.222298f, 1.163096f, 1.101622f, 1.039735f, 0.975576f, 0.914363f, 0.866531f,
//				0.819221f, 0.772433f, 0.726167f, 0.680423f, 0.629983f, 0.583583f, 0.541224f, 0.502905f, 0.468627f, 0.438389f, 0.425051f, 0.421178f, 0.426771f,
//				0.433666f, 0.454515f, 0.485601f, 0.530641f, 0.585917f, 0.655148f, 0.727897f, 0.798947f, 0.874728f, 0.955239f, 1.04048f, 1.130452f, 1.23559f,
//				1.338422f, 1.438946f, 1.537165f, 1.633077f, 1.717945f, 1.785342f, 1.838983f, 1.880727f, 1.908716f, 1.92295f, 1.925286f, 1.913868f, 1.886835f,
//				1.848759f, 1.804859f, 1.755133f, 1.699582f, 1.638206f, 1.571005f, 1.497979f, 1.413911f, 1.327535f, 1.238853f, 1.141435f, 1.037139f, 0.941579f,
//				0.860332f, 0.793396f, 0.737055f, 0.695027f, 0.659876f, 0.631603f, 0.610208f, 0.597549f, 0.588662f, 0.583547f, 0.582205f, 0.584634f, 0.590835f,
//				0.600808f, 0.614554f, 0.632072f, 0.653361f, 0.678423f, 0.720116f, 0.774724f, 0.837226f, 0.896473f, 0.952463f, 1.007055f, 1.058391f, 1.110187f,
//				1.154549f, 1.191477f, 1.234835f, 1.275261f, 1.312756f, 1.347321f, 1.378954f, 1.407656f, 1.433428f, 1.456268f, 1.476177f, 1.493155f, 1.500772f,
//				1.498491f, 1.488169f, 1.467949f, 1.443406f, 1.414539f, 1.381349f, 1.343837f, 1.302001f, 1.265742f, 1.238775f, 1.202666f, 1.16531f, 1.12299f,
//				1.080923f, 1.039111f, 0.997551f, 0.956245f, 0.915193f, 0.874394f, 0.828631f, 0.795568f, 0.763421f, 0.73219f, 0.701876f, 0.672478f, 0.639192f,
//				0.607236f, 0.576609f, 0.547313f, 0.523234f, 0.496938f, 0.474855f, 0.451768f, 0.436969f, 0.418165f, 0.395354f, 0.372255f, 0.348867f, 0.325191f,
//				0.297508f, 0.281832f, 0.262122f, 0.244271f, 0.228279f, 0.214145f, 0.20187f, 0.201062f, 0.19571f, 0.191032f, 0.187028f, 0.177807f, 0.167085f,
//				0.154862f, 0.151574f, 0.144569f, 0.145728f, 0.15505f, 0.162155f, 0.17262f, 0.186444f, 0.211061f, 0.230103f, 0.248373f, 0.265872f, 0.2826f,
//				0.29298f, 0.297013f, 0.297156f, 0.309778f, 0.324444f, 0.341153f, 0.359906f, 0.380702f, 0.403542f, 0.423208f, 0.451994f, 0.482149f, 0.513674f,
//				0.556178f, 0.598509f, 0.640667f, 0.671673f, 0.705735f, 0.742851f, 0.783024f, 0.826251f, 0.883685f, 0.955326f, 1.026652f, 1.092086f, 1.156845f,
//				1.220931f, 1.284344f, 1.347082f, 1.409146f, 1.470537f, 1.526036f, 1.578589f, 1.628196f, 1.671912f, 1.715312f, 1.758395f, 1.815687f, 1.869922f,
//				1.921102f, 1.969227f, 2.005666f, 2.029158f, 2.039703f, 2.044563f, 2.043737f, 2.032911f, 2.01766f, 1.997984f, 1.973882f, 1.945356f, 1.912405f,
//				1.875028f, 1.833227f, 1.781424f, 1.72148f, 1.651534f, 1.571588f, 1.478096f, 1.379691f, 1.276371f, 1.164421f, 1.065906f, 0.972196f, 0.88329f,
//				0.799189f, 0.719891f, 0.649225f, 0.579756f, 0.511484f, 0.444408f, 0.374811f, 0.30827f, 0.244784f, 0.184354f, 0.13813f, 0.102396f, 0.08087f,
//				0.07355f, 0.067426f, 0.0625f, 0.063575f, 0.078085f, 0.087791f, 0.097007f, 0.105735f, 0.10917f, 0.111627f, 0.1184f, 0.127629f, 0.139315f,
//				0.149143f, 0.169351f, 0.188788f, 0.207453f, 0.225347f, 0.236894f, 0.239148f, 0.236913f, 0.230189f, 0.233846f, 0.247883f, 0.262691f, 0.274553f,
//				0.288275f, 0.303854f, 0.321293f, 0.350199f, 0.381941f, 0.405052f, 0.428054f, 0.450946f, 0.482359f, 0.508969f, 0.53635f, 0.559697f, 0.585442f,
//				0.613583f, 0.65373f, 0.692556f, 0.730061f, 0.760671f, 0.784383f, 0.806004f, 0.825533f, 0.84297f, 0.851886f, 0.85228f, 0.846424f, 0.838634f,
//				0.842235f, 0.84762f, 0.854788f, 0.859424f, 0.866331f, 0.875511f, 0.896571f, 0.916653f, 0.935756f, 0.949075f, 0.963275f, 0.972328f, 0.970549f,
//				0.978542f, 0.966318f, 0.955206f, 0.950364f, 0.957428f, 0.969594f, 0.976271f, 0.984625f, 0.971958f, 0.94767f, 0.916958f, 0.883195f, 0.854212f,
//				0.807003f, 0.746162f, 0.696997f, 0.639002f, 0.58395f, 0.531137f, 0.471337f, 0.417069f, 0.360536f, 0.319116f, 0.278365f, 0.223837f, 0.188758f,
//				0.158266f, 0.105252f, 0.06952f, 0.074118f });
//		
//		analysis.setSleepCurveStatusArray(new short[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0,
//				0 });
		
		historyData.setAnaly(analysis);
		return historyData;
	}

	private List<CvPoint> points = new ArrayList<CvPoint>();
	private List<LineGraphView.BedBean> bedBeans = new ArrayList<LineGraphView.BedBean>();
	private List<LineGraphView.BedBean> SleepInUP = new ArrayList<LineGraphView.BedBean>();
	/**
	 * 描述：呼吸暂停的集合
	 */
	private List<GraphView.GraphViewData> apneaPauseList = new ArrayList<GraphView.GraphViewData>();
	/**
	 * 描述：心跳暂停的集合
	 */
	private List<GraphView.GraphViewData> heartPauseList = new ArrayList<GraphView.GraphViewData>();

	/**
	 * <h3>新版 算出 睡眠周期图的数据结构</h3>
	 * 
	 * @param analysis
	 * @param timeStep
	 * @return
	 */
	private GraphView.GraphViewData[] getNewSleepGraphData(Analysis analysis, int timeStep, DeviceType deviceType) {
		GraphView.GraphViewData[] mainData = new GraphView.GraphViewData[analysis.getSleepCurveArray().length + 1];
		// 是手机监测的新版
		for (int i = 0; i < analysis.getSleepCurveArray().length; i++) {
			// 清醒，潜睡，中睡，深睡 手机给的是 0,1,2,3； ron画图的列表是: 1,0,-1,-2
			mainData[i] = new GraphView.GraphViewData(i * timeStep, 1 - analysis.getSleepCurveArray()[i]);
		}

		mainData[analysis.getSleepCurveArray().length] = new GraphView.GraphViewData(analysis.getSleepCurveArray().length * timeStep, 1);
		SleepInUP.clear();
		heartPauseList.clear();
		apneaPauseList.clear();
		bedBeans.clear();
		if (analysis.getSleepCurveStatusArray() != null && analysis.getSleepCurveStatusArray().length > 0) {
			for (int i = 0; i < analysis.getSleepCurveStatusArray().length/* mainData.length -1 */; i++) {

				if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewSleepInPoint) == SleepConfig.NewSleepInPoint) { // 入睡点
					LineGraphView.BedBean sleepIn = new LineGraphView.BedBean();
					sleepIn.setData(new GraphView.GraphViewData(i * timeStep, 0));
					sleepIn.setX(i * timeStep);
					sleepIn.setStatus(BedBean.SLEEPIN);
					sleepIn.setY(0);
					SleepInUP.add(sleepIn);
				}

				if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewWakeUpPoint) == SleepConfig.NewWakeUpPoint) { // 清醒点
					LineGraphView.BedBean waleUp = new LineGraphView.BedBean();
					waleUp.setData(new GraphView.GraphViewData(i * timeStep, 0));
					waleUp.setX(i * timeStep);
					waleUp.setStatus(BedBean.SLEEPUP);
					waleUp.setY(0);
					SleepInUP.add(waleUp);
				}
			}
		}
		return mainData;
	}

	/**
	 * <p>
	 * 分析detail
	 * </p>
	 * 
	 * @param analysis
	 * @param timeStep
	 */
	public GraphView.GraphViewData[] getSleepGraphData(Analysis analysis, int timeStep, DeviceType deviceType) {
		if (analysis == null || analysis.getSleepCurveArray() == null || analysis.getSleepCurveArray().length == 0){
			return null;
		}
		return getNewSleepGraphData(analysis, timeStep, deviceType);
	}

	/**
	 * <p>
	 * 由于datas是按照x轴为时间轴的， 保证第一个数 是小于 提供值x的值，就是最近的值
	 * </p>
	 */
	public static GraphView.GraphViewData findNear(GraphView.GraphViewData[] datas, int x) {
		if (datas == null) {
			return null;
		}
		if (datas.length == 0)
			return null;

		if (datas[0].getX() > x)
			return null;

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].getX() >= x)
				return datas[i];
		}
		return null;
	}

	/**
	 * <p>
	 * 由于datas是按照x轴为时间轴的， 保证第一个数 是小于 提供值x的值，就是最近的值
	 * </p>
	 */
	public static GraphView.GraphViewData findNear(GraphView.GraphViewData[] datas, int x, List<GraphView.GraphViewData> dt) {
		if (datas == null) {
			return null;
		}
		if (datas.length == 0)
			return null;

		if (datas[0].getX() > x)
			return null;

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].getX() >= x) {
				if (dt != null)
					for (GraphView.GraphViewData gv : dt) {
						if (gv.getX() == datas[i].getX()) {
							if (i + 1 < datas.length) {
								return datas[i + 1];
							}
						}
					}
				return datas[i];
			}
		}
		return null;
	}

}
