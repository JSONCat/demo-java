/**
 * 
 */
package com.sas.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.SortOrder;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.TernaryEntry;
import com.sas.core.meta.SasGamePlantUserStatistic;
import com.sas.core.util.meta.UserUtil;

/**
 * 植树送好礼
 * @author Administrator
 *
 */
public class PlantGameUtil {


	/*************
	 * 隐藏敏感信息
	 * @param friends
	 * @return
	 */
	public static final List<SasGamePlantUserStatistic> hiddenNameAndPhones(final List<SasGamePlantUserStatistic> friends)
	{
		if(CollectionUtils.isNotEmpty(friends)){
			for(final SasGamePlantUserStatistic u : friends)
			{
				PlantGameUtil.hiddenNameAndPhones(u);
			}
		}
		return friends;
	}
	
	public static final SasGamePlantUserStatistic hiddenNameAndPhones(final SasGamePlantUserStatistic u)
	{
		if(u == null){
			return null;
		}
		u.setTrueName(UserUtil.hiddenUserName(u.getTrueName()));
		u.setPhone(UserUtil.hiddenMobile(u.getPhone()));
		return u;
	}
	
	/****************
	 * 将奖品信息的等级设置 转成字符串
	 * @param levelFroms
	 * @param levelTos
	 * @param awards
	 * @return
	 */
	public static final BinaryEntry<String, String> getErrorAndConvertAwards2String(final int[] levelFroms, final int[] levelTos, final String[] awards)
	{
		if(ArrayUtils.isEmpty(levelFroms) || ArrayUtils.isEmpty(levelTos) || ArrayUtils.isEmpty(awards))
		{
			return new BinaryEntry<String, String>("请输入奖品信息！", "");
		}		
		final List<BinaryEntry<Integer, Integer>> allLevelRanges = new LinkedList<BinaryEntry<Integer, Integer>>();
		final Map<Integer, String> awardDescMap = new HashMap<Integer, String>();
		for(int i=0; i<levelFroms.length&&i<levelTos.length&&i<awards.length; i++)
		{
			awards[i] = XSSUtil.filter(awards[i], true).replaceAll("("+DividerChar.ComplexCharWithSemicolon.chars 
					+ ")|" + HtmlUtil.WhiteSpaceReg, "");
			if(StringUtils.isBlank(awards[i])){
				return new BinaryEntry<String, String>("请输入第"+(i+1)+"个奖品的信息！", "");
			}
			final int min = MathUtil.max(1, MathUtil.minInt(levelFroms[i], levelTos[i]));
			final int max = MathUtil.max(1, levelFroms[i], levelTos[i]);
			//范围是否交叉
			for(final BinaryEntry<Integer, Integer> entry : allLevelRanges){
				if((entry.key <= min && entry.value >= min)||(entry.key <= max && entry.value >= max)){
					return new BinaryEntry<String, String>("第"+(i+1)+"个奖品的名次与其他奖品名次重叠！", "");
				}
			}
			allLevelRanges.add(new BinaryEntry<Integer, Integer>(min, max));
			awardDescMap.put(min, awards[i]);
		}
		//按照等级从小到大进行排列
		CollectionUtils.sortTheList(allLevelRanges, "key", SortOrder.ASC);
		final StringBuilder sb = new StringBuilder("");
		for(final BinaryEntry<Integer, Integer> entry : allLevelRanges){
			if(sb.length() > 0){
				sb.append(DividerChar.ComplexCharWithSemicolon.chars);
			}
			sb.append(entry.key + DividerChar.SingleWells.chars + entry.value
					+ DividerChar.SingleWells.chars + awardDescMap.get(entry.key));
		}
		return new BinaryEntry<String, String>(null, sb.toString());
	}
	
	public static final List<TernaryEntry<Integer, Integer, String>> parseAwardsFromString(final String awards)
	{
		if(StringUtils.isBlank(awards))
		{
			return new ArrayList<TernaryEntry<Integer, Integer, String>>(0);
		}
		final List<TernaryEntry<Integer, Integer, String>> list = new LinkedList<TernaryEntry<Integer, Integer, String>>();
		final String[] array = awards.split(DividerChar.ComplexCharWithSemicolon.chars);
		for(final String e : array)
		{
			final String[] subArray = e.split(DividerChar.SingleWells.chars);
			if(subArray.length < 3){
				continue;
			}else if(subArray.length == 3){
				list.add(new TernaryEntry<Integer, Integer, String>(IdUtil.convertToInteger(subArray[0], 1),
						IdUtil.convertToInteger(subArray[1], 1), subArray[2]));
			}else{//描述信息也含有#
				final StringBuilder sb = new StringBuilder(subArray[2]);
				for(int i=3; i<subArray.length; i++){
					sb.append(DividerChar.SingleWells.chars + subArray[i]);
				}
				list.add(new TernaryEntry<Integer, Integer, String>(IdUtil.convertToInteger(subArray[0], 1),
						IdUtil.convertToInteger(subArray[1], 1), sb.toString()));
			}
		}
		return list;
	}
}
