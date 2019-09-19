/**
 * 
 */
package com.sas.core.util;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.Seconds;
import com.sas.core.meta.SasMenuActivityScore;
import com.sas.core.meta.SasMenuActivityScoreGroup;
import com.sas.core.meta.SasMenuActivityScoreSetting;
import com.sas.core.service.SasMenuActivityScoreService;

/**
 * 成绩保存函数
 * @author Administrator
 *
 */
public final class ActivityScoreSavePoolExecuter {

	private static final Logger logger = Logger.getLogger(ActivityScoreSavePoolExecuter.class);
	
	private final SasMenuActivityScoreService sasMenuActivityScoreService;	
	private final ThreadPoolTaskExecutor threadPoolTaskExecutor;	
	private final SasMenuActivityScoreSetting scoreSetting;
	private final Map<Long, SasMenuActivityScoreGroup> allGroupMap;
	public  final Statistic statistic = new Statistic();
	
	
	public ActivityScoreSavePoolExecuter(
			SasMenuActivityScoreService sasMenuActivityScoreService,
			ThreadPoolTaskExecutor threadPoolTaskExecutor,
			SasMenuActivityScoreSetting scoreSetting,
			List<SasMenuActivityScoreGroup> allGroups) {
		super();
		this.sasMenuActivityScoreService = sasMenuActivityScoreService;
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
		this.scoreSetting = scoreSetting;
		this.allGroupMap = CollectionUtils.extractConllectionToMap(allGroups, "id");
	}

	/************
	 * 执行成绩更新
	 * @param score
	 * @param needReRank
	 */
	public void execute(final SasMenuActivityScore newScore, final SasMenuActivityScore oldScore,  final boolean needReRank){
		statistic.incTotalCount();
		threadPoolTaskExecutor.execute(new Runnable(){
			public void run(){
				int maxTime = 3;
				do{
					try{
						if(sasMenuActivityScoreService.saveActivityScore(scoreSetting, newScore, oldScore, allGroupMap.get(newScore.getUserGroupId()), needReRank) != null){
							statistic.incSuccCount();
						}else{
							logger.error("Fail to import score: match-num=" + newScore.getUserMatchNumber() + ", group=" + newScore.getUserGroupName() 
								+ ", truename" + newScore.getUserTrueName());
							statistic.incFailCount();
						}
						//System.out.println(statistic.getTotalCount() + ":" + statistic.getSuccCount() + ":" + statistic.getFailCount());
						break;
					}catch(Throwable ex){
						logger.error("Fail to execute score save thread: "+ ex.getMessage(), ex);
						ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
					}
				}while((--maxTime) >= 0);				
				if(maxTime < 0){
					statistic.incFailCount();
				}
			}
		});
	}
		
	/*************
	 * 是否完成
	 * @return
	 */
	public void waitUntilFinishAndReScoreRank(final long acitivtyId, final boolean needUpdateAllScore, final boolean needRerank, final boolean needRerank2){
		int maxSleepTimes = Seconds.OneMinute.seconds;
		while(statistic.getTotalCount() > (statistic.getSuccCount()  + statistic.getFailCount() ) && maxSleepTimes >= 0){
			maxSleepTimes --;
			//System.out.println(statistic.getTotalCount() + ":" + statistic.getSuccCount() + ":" + statistic.getFailCount());
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
		}
		sasMenuActivityScoreService.generateAllScoreAndReranking(acitivtyId, needUpdateAllScore, needRerank, needRerank2);
	}
	
	public void waitUntilFinish(){
		int maxSleepTimes = Seconds.OneMinute.seconds;
		while(statistic.getTotalCount() > (statistic.getSuccCount()  + statistic.getFailCount() ) && maxSleepTimes >= 0){
			maxSleepTimes --;
			//System.out.println(statistic.getTotalCount() + ":" + statistic.getSuccCount() + ":" + statistic.getFailCount());
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
		}
	}


	
	public class Statistic{
		
		private volatile int totalCount = 0;
		private volatile int succCount = 0;
		private volatile int failCount = 0;
		
		public synchronized int incTotalCount() {
			this.totalCount ++;
			return totalCount;
		}

		public synchronized  int incSuccCount() {
			this.succCount ++;
			return succCount;
		}

		public synchronized  int incFailCount() {
			this.failCount ++;
			return failCount;
		}

		public int getTotalCount() {
			return totalCount;
		}

		public int getSuccCount() {
			return succCount;
		}

		public int getFailCount() {
			return failCount;
		}
	}
}
