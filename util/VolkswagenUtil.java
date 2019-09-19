/**
 * 
 */
package com.sas.core.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sas.core.dto.BinaryEntry;

/**
 * @author Administrator
 *
 */
public class VolkswagenUtil {

	/**********
	 * 途昂统计信息
	 * @author Administrator
	 */
	public static final class TuAngVotePriceData
	{
		public List<BinaryEntry<Integer, Integer>> min = null;
		public long totalMinAmount = 0L; 
		public long  totalMinVoterCount = 0L;
		public List<BinaryEntry<Integer, Integer>> max = null;
		public long totalMaxAmount = 0L;
		public long totalMaxVoterCount = 0L;

		public TuAngVotePriceData(List<BinaryEntry<Integer, Integer>> min,
				long totalMinAmount, long totalMinVoterCount,
				List<BinaryEntry<Integer, Integer>> max, long totalMaxAmount,
				long totalMaxVoterCount) {
			super();
			this.min = min;
			this.totalMinAmount = totalMinAmount;
			this.totalMinVoterCount = totalMinVoterCount;
			this.max = max;
			this.totalMaxAmount = totalMaxAmount;
			this.totalMaxVoterCount = totalMaxVoterCount;
		}

		public static final TuAngVotePriceData fromJson(final String json){
			if(StringUtils.isBlank(json)){
				return VolkswagenUtil.initTuAngVoteMap(); 
			}
			final Map<String, String> map = JsonUtil.getObject(json, Map.class);
			//低价
			final List<BinaryEntry<Integer, Integer>> min = TuAngVotePriceData.toList(map.get("min"));
			final long totalMinVoterCount= IdUtil.convertTolong(map.get("min_count"), 0);
			final long totalMinAmount= IdUtil.convertTolong(map.get("min_total"), 0);
			//高价
			final List<BinaryEntry<Integer, Integer>> max = TuAngVotePriceData.toList(map.get("max"));
			final long totalMaxVoterCount= IdUtil.convertTolong(map.get("max_count"), 0);
			final long totalMaxAmount= IdUtil.convertTolong(map.get("max_total"), 0);
			return new TuAngVotePriceData(min, totalMinAmount, totalMinVoterCount, max, totalMaxAmount, totalMaxVoterCount);
		}		
		public final String toJson(){
			final Map<String, String> map = new HashMap<String, String>();
			map.put("min_count", String.valueOf(totalMinVoterCount));
			map.put("min_total", String.valueOf(totalMinAmount));
			map.put("min", this.fromList(min));
			map.put("max_total", String.valueOf(totalMaxAmount));
			map.put("max_count", String.valueOf(totalMaxVoterCount));
			map.put("max", this.fromList(max));
			return JsonUtil.getJsonString(map);
		}
		private static List<BinaryEntry<Integer, Integer>> toList(final String str){
			final List<BinaryEntry<Integer, Integer>> list = new LinkedList<BinaryEntry<Integer, Integer>>();
			if(str == null || str.length() < 1){
				return list;
			}
			final String[] array = str.split("#");
			for(final String a : array){
				final String[] subArray = a.split("=");
				list.add(new BinaryEntry<Integer, Integer>(IdUtil.convertToInteger(subArray[0], 0),
						IdUtil.convertToInteger(subArray[1], 0)));
			}
			return list;
		}
		
		private static String fromList(final List<BinaryEntry<Integer, Integer>> list){
			final StringBuilder sb = new StringBuilder("");
			for(final BinaryEntry<Integer, Integer> entry : list){
				if(sb.length() > 0){
					sb.append("#");
				}
				sb.append(entry.key + "=" + entry.value);
			}
			return sb.toString();
		}
	}
	
	/**********
	 * 初始化途昂的投票Map
	 * @return
	 */
	public static final TuAngVotePriceData initTuAngVoteMap()
	{
		final List<BinaryEntry<Integer, Integer>> min = new LinkedList<BinaryEntry<Integer, Integer>>();
		final long totalMinAmount = 0L, totalMinVoterCount = 0L;
		min.add(new BinaryEntry<Integer, Integer>(290000, 0)); //29万以下
		min.add(new BinaryEntry<Integer, Integer>(350000, 0)); //30-35		
		min.add(new BinaryEntry<Integer, Integer>(400000, 0)); //36-40
		min.add(new BinaryEntry<Integer, Integer>(450000, 0)); //41-45
		min.add(new BinaryEntry<Integer, Integer>(500000, 0));//46-50
		min.add(new BinaryEntry<Integer, Integer>(550000, 0)); //51-55
		min.add(new BinaryEntry<Integer, Integer>(600000, 0)); //56-60
		min.add(new BinaryEntry<Integer, Integer>(650000, 0)); //61-65
		min.add(new BinaryEntry<Integer, Integer>(700000, 0)); //66-70
		min.add(new BinaryEntry<Integer, Integer>(750000, 0));  //71-75
		min.add(new BinaryEntry<Integer, Integer>(1000000, 0));  //76-1000万
		//System.out.println("min_count=" + totalMinVoterCount + ", min_total=" + totalMinAmount + ", min_average=" + (totalMinAmount/totalMinVoterCount));
		final List<BinaryEntry<Integer, Integer>> max = new LinkedList<BinaryEntry<Integer, Integer>>();
		final long totalMaxAmount = 0L, totalMaxVoterCount = 0L;
		max.add(new BinaryEntry<Integer, Integer>(290000, 0)); //29万以下
		max.add(new BinaryEntry<Integer, Integer>(350000, 0)); //30-35
		max.add(new BinaryEntry<Integer, Integer>(400000, 0)); //36-40
		max.add(new BinaryEntry<Integer, Integer>(450000, 0)); //41-45
		max.add(new BinaryEntry<Integer, Integer>(500000, 0));//46-50
		max.add(new BinaryEntry<Integer, Integer>(550000, 0)); //51-55
		max.add(new BinaryEntry<Integer, Integer>(600000, 0)); //56-60
		max.add(new BinaryEntry<Integer, Integer>(650000, 0)); //61-65
		max.add(new BinaryEntry<Integer, Integer>(700000, 0)); //66-70
		max.add(new BinaryEntry<Integer, Integer>(750000, 0));  //71-75
		max.add(new BinaryEntry<Integer, Integer>(1000000, 0));  //76-1000万
		//System.out.println("max_count=" + totalMaxVoterCount + ", max_total=" + totalMaxAmount + ", max_average=" + (totalMaxAmount/totalMaxVoterCount));
		return new TuAngVotePriceData(min, totalMinAmount, totalMinVoterCount, max, totalMaxAmount, totalMaxVoterCount);
	}

	/***********
	 * 根据范围进行过滤
	 * @param list
	 * @param min
	 * @param max
	 * @return
	 */
	public static final List<BinaryEntry<Integer, Integer>> filterTuAngVoteVotesByRange(final List<BinaryEntry<Integer, Integer>> list, final int min, final int max,
			final Map<Integer, Integer> virtalCountMap)
	{		
		final List<BinaryEntry<Integer, Integer>> result = new LinkedList<BinaryEntry<Integer, Integer>>();
		if(CollectionUtils.isEmpty(list)){
			return result;
		}
		for(BinaryEntry<Integer, Integer> b : list)
		{
			if(b.key >= min && b.key <= max){
				final Integer virtual = virtalCountMap.get(b.key);
				if(virtual != null){
					b.value += virtual;
				}
				result.add(b);
			}
		}
		return result;
	}
	
	public static final Map<Integer, Integer> getMinVirtualCountMap(){
		final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		m.put(350000, 20);
		m.put(400000, 88);
		m.put(450000, 32);
		return m;
	}
	public static final Map<Integer, Integer> getMaxVirtualCountMap(){
		final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		m.put(500000, 24);
		m.put(550000, 42);
		m.put(600000, 93);
		return m;
	}
	/*************
	 * 价格投票
	 * @param list
	 * @param price
	 * @return
	 */
	public static final List<BinaryEntry<Integer, Integer>> addTuAngVotePrice(final List<BinaryEntry<Integer, Integer>> list, final int price)
	{
		BinaryEntry<Integer, Integer> lastEntry = null;
		for(final BinaryEntry<Integer, Integer> entry : list)
		{
			if(price <= entry.key){
				entry.value = entry.value + 1;
				return list;
			}
			lastEntry = entry;
		}
		if(lastEntry != null){
			lastEntry.value = lastEntry.value + 1;
		}
		return list;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initTuAngVoteMap();
	}

}
