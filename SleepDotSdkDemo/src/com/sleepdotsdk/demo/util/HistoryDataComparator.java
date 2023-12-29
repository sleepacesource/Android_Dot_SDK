package com.sleepdotsdk.demo.util;

import java.util.Comparator;

import com.sleepace.sdk.sleepdot.domain.HistoryData;

public class HistoryDataComparator implements Comparator<HistoryData> {

	@Override
	public int compare(HistoryData o1, HistoryData o2) {
		// TODO Auto-generated method stub
		/**
		 * 如果要按照升序排序， 则o1 小于o2，返回-1（负数），相等返回0，o1大于o2返回1（正数） 
		 */
		if(o1.getSummary().getStartTime() < o2.getSummary().getStartTime()){
			return 1;
		}else if(o1.getSummary().getStartTime() > o2.getSummary().getStartTime()){
			return -1;
		}
		return 0;
	}

}











