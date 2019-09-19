package com.sas.core.util;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/*******************
 * 线程相关的util
 * @author zhuliming
 *
 */
public class ThreadUtil {

	private static final Logger logger = Logger.getLogger(ThreadUtil.class);	
	
	/**************
	 * 默认的线程池
	 */
	private static final ExecutorService threadPool = Executors.newCachedThreadPool(); 
	
	/***************
	 * 睡眠固定时间
	 * @param miliseconds
	 */
	public static final void sleepNoException(long miliseconds){
		try {
			Thread.sleep(miliseconds);
		} catch (InterruptedException e) {
			logger.error("InterruptedException, ex="+e.getMessage(), e);
		}
	}
	
	/****************
	 * 执行任务
	 * @param t
	 */
	public static final void execute(Runnable t)
	{
		if(t != null){
			threadPool.execute(t);
		}
	}
}
