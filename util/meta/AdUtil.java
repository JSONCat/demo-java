package com.sas.core.util.meta;

import java.util.ArrayList;
import java.util.List;

import com.sas.core.meta.SasAd;
import com.sas.core.meta.SystemAd;

/**
 * 
 * 广告工具类
 * 
 * @author qiubinying
 *
 */
public class AdUtil {

	/**
	 * 关闭赛事的系统广告匹配替换方法
	 * @param sasAdList
	 * @param systemAdList
	 * @return
	 */
//	public static final List<SasAd> matchSasAdWithSystemAd(final List<SasAd> sasAdList, final List<SystemAd> systemAdList)
//	{
//		final List<SasAd> resultAdList = new ArrayList<SasAd>(sasAdList.size());
//		for(final SasAd sasAd : sasAdList)
//		{
//			double gap = 10000;
//			final double scale = sasAd.getAdWidth()  * 1.0 / sasAd.getAdHeight();
//			SasAd resultSasAd = null;
//			for(final SystemAd systemAd : systemAdList){
//				final double systemAdGap = Math.abs(systemAd.getAdWidth() * 1.0 / systemAd.getAdHeight() - scale);
//				if(systemAdGap < gap){
//					resultSasAd = AdUtil.convertSystemAdToSasAd(sasAd, systemAd);
//					if(systemAdGap < 0.01){
//						break;
//					}else{
//						gap = systemAdGap;
//					}
//				}
//			}
//			if(resultSasAd != null){
//				resultAdList.add(resultSasAd);
//			}else{
//				resultAdList.add(sasAd);
//			}
//		}
//		return resultAdList;
//	}
	
	/**
	 * 合并处理转换后的SasAd
	 * @param sasAd
	 * @param systemAd
	 * @return
	 */
	public static final SasAd convertSystemAdToSasAd(SasAd sasAd, SystemAd systemAd){
		final SasAd resultSasAd = new SasAd();
		resultSasAd.setId(systemAd.getId());
		resultSasAd.setPositionName(sasAd.getPositionName());
		resultSasAd.setTitle(systemAd.getName());
		resultSasAd.setAdPicUrl(systemAd.getAdPicUrl());
		resultSasAd.setUrl(systemAd.getUrl());
		resultSasAd.setCode(sasAd.getCode());
		resultSasAd.setAdHeight(sasAd.getAdHeight());
		resultSasAd.setAdWidth(sasAd.getAdWidth());
		resultSasAd.setWindowOpenType(sasAd.getWindowOpenType());
		return resultSasAd;
	}
}