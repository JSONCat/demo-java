package com.sas.core.util;

import java.util.ArrayList;
import java.util.List;

/******************
 * 保存两个列表里面的比较结果，包含不同部分和相同部分
 * @author zhuliming
 *
 * @param <T>
 */
public class ListCompareResult<T> {

	public ListCompareResult(List<T> newList, List<T> commonList,
			List<T> oldRemainList) {
		this.newList = newList;
		this.commonList = commonList;
		this.oldRemainList = oldRemainList;
	}

	public ListCompareResult(){}
	
	private List<T> newList;
	
	private List<T> commonList;
	
	private List<T> oldRemainList;

	/****************
	 * 是否有集合1中不同于集合2的新元素
	 * @return
	 */
	public boolean hasNewList() {
		return newList != null && newList.size() > 0;
	}
	public List<T> getNewList() {
		return newList == null ? new ArrayList<T>(0) : newList;
	}
	public void addNew(T t) {
		if(newList == null){
			newList = new ArrayList<T>();
		}
		newList.add(t);
	}
	
	/*****************
	 * 是否有两个集合相同的部分
	 * @return
	 */
	public boolean hasCommonList() {
		return commonList != null && commonList.size() > 0;
	}	
	public List<T> getCommonList() {
		return commonList == null ? new ArrayList<T>(0) : commonList;
	}
	public void addCommon(T t) {
		if(commonList == null){
			commonList = new ArrayList<T>();
		}
		commonList.add(t);
	}
	
	/*****************
	 * 是否有集合2中存在， 但是集合1不存在的老元素
	 * @return
	 */
	public boolean hasOldRemainList() {
		return oldRemainList != null && oldRemainList.size() > 0;
	}
	public List<T> getOldRemainList() {
		return oldRemainList == null ? new ArrayList<T>(0) : oldRemainList;
	}
	public void addOldRemain(T t) {
		if(oldRemainList == null){
			oldRemainList = new ArrayList<T>();
		}
		oldRemainList.add(t);
	}	
}
