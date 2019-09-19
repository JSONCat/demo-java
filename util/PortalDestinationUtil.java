/**
 * 
 */
package com.sas.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sas.core.dto.PortalDestinationDetail;
import com.sas.core.meta.PortalDestinationStatistic;

/**
 * @author Administrator
 *
 */
public class PortalDestinationUtil {

	private static final Logger logger = Logger.getLogger(PortalDestinationUtil.class);

	/****************
	 * 添加虚假的计数
	 * @param statistic
	 */
	public static final void addVirtualStatisticCount(final PortalDestinationStatistic statistic)
	{
		final long id = statistic.getDestinationId();
		statistic.setFavorityCount(id % 11 + statistic.getFavorityCount());
	}
	
	/*******************
	 * 转换成map
	 * @param details
	 * @return
	 */
	public static final Map<Long, PortalDestinationDetail> conert2Map(final List<PortalDestinationDetail> details){
		final Map<Long, PortalDestinationDetail> result = new HashMap<Long, PortalDestinationDetail>();
		if(CollectionUtils.isNotEmpty(details)){
			for(final PortalDestinationDetail detail : details){
				result.put(detail.getPortalDestination().getId(), detail);
			}
		}
		return result;	
	}
}
