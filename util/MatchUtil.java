package com.sas.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.UserConstant;
import com.sas.core.meta.PortalMatch;
import com.sas.core.meta.PortalMatchEntry;
import com.sas.core.meta.PortalMatchStatistic;
import com.sas.core.meta.User;

/*************
 * 运营活动的util
 * @author Administrator
 *
 */
public class MatchUtil {

	public static final String createMatchDetailURL(final long matchId, final boolean isWap){
		return isWap ? "/m/match?id=" + matchId : "/match?id=" + matchId; 
	}
	
	public static final String createMatchDetailURLByPath(final long matchId, final boolean isWap){
		return isWap ? "/m/match/" + matchId : "/match/" + matchId; 
	}
	
	public static final String createMatchEntryDetailURL(final long matchEntryId, final boolean isWap){
		return isWap ? "/m/match/entry?id=" + matchEntryId : "/match/entry?id=" + matchEntryId; 
	}
	
	public static final String createMatchEntryDetailURLByPath(final long matchEntryId, final boolean isWap){
		return isWap ? "/m/match/entry/" + matchEntryId : "/match/entry/" + matchEntryId; 
	}
	
	/*****************
	 * 插入最近提交作品的人
	 * @param statistic
	 * @param user
	 * @return
	 */
	public static final PortalMatchStatistic insertOneUserEntry(final PortalMatchStatistic statistic, final User user)
	{
		if(statistic == null || user == null){
			return statistic;
		}
		//处理参数
		final String userIdString = String.valueOf(user.getId());
		final String userAvator = (StringUtils.isBlank(user.getAvatarUrl())) ? UserConstant.UserDefaultAvatar : user.getAvatarUrl().trim();
		final String userNickName = (StringUtils.isBlank(user.getNickname())) ? "-": user.getNickname().trim().replaceAll(DividerChar.ComplexCharWithSemicolon.chars, "");
		
		//老用户数据
		final String[] visitorIdArray = statistic.getLastUserIds().split(DividerChar.ComplexCharWithSemicolon.chars);
		if(ArrayUtils.isEmpty(visitorIdArray) || visitorIdArray[0].length() < 1){
			statistic.setLastUserIds(userIdString);
			statistic.setLastUserAvatars(userAvator);
			statistic.setLastUserNicknames(userNickName);
		}else{
			final String[] visitorNickNamesArray = statistic.getLastUserNicknames().split(DividerChar.ComplexCharWithSemicolon.chars);
			final String[] visitorAvasArray = statistic.getLastUserAvatars().split(DividerChar.ComplexCharWithSemicolon.chars);
			final int maxLength = MathUtil.minInt(visitorIdArray.length, visitorNickNamesArray.length, visitorAvasArray.length, 20);
			//拼接信息
			final StringBuilder allLastUserIds = new StringBuilder(userIdString);
			final StringBuilder allUserAvators = new StringBuilder(userAvator);
			final StringBuilder allUserNickNames = new StringBuilder(userNickName);
			//去重
			final Set<String> uniqueUserIdSet = new HashSet<String>();
			uniqueUserIdSet.add(userIdString);
			for(int i=0; i<maxLength; i++)
			{
				if(uniqueUserIdSet.contains(visitorIdArray[i])){
					continue;
				}
				uniqueUserIdSet.add(visitorIdArray[i]);
				allLastUserIds.append(DividerChar.ComplexCharWithSemicolon.chars + visitorIdArray[i]);
				allUserAvators.append(DividerChar.ComplexCharWithSemicolon.chars + visitorAvasArray[i]);
				allUserNickNames.append(DividerChar.ComplexCharWithSemicolon.chars + visitorNickNamesArray[i]);
			}
			statistic.setLastUserIds(allLastUserIds.toString());
			statistic.setLastUserAvatars(allUserAvators.toString());
			statistic.setLastUserNicknames(allUserNickNames.toString());
		}

		return statistic;
	}
	
	/*****************
	 * 插入最近对作品投票的人
	 * @param statistic
	 * @param user
	 * @return
	 */
	public static final PortalMatchEntry insertOneUserEntryVote(final PortalMatchEntry entry, final User user)
	{
		if(entry == null || user == null){
			return entry;
		}
		//处理参数
		final String userIdString = String.valueOf(user.getId());
		final String userAvator = (StringUtils.isBlank(user.getAvatarUrl())) ? UserConstant.UserDefaultAvatar : user.getAvatarUrl().trim();
		final String userNickName = (StringUtils.isBlank(user.getNickname())) ? "-": user.getNickname().trim().replaceAll(DividerChar.ComplexCharWithSemicolon.chars, "");
		
		//老用户数据
		final String[] visitorIdArray = entry.getLastVoterIds().split(DividerChar.ComplexCharWithSemicolon.chars);
		if(ArrayUtils.isEmpty(visitorIdArray) || visitorIdArray[0].length() < 1){
			entry.setLastVoterIds(userIdString);
			entry.setLastVoterAvatars(userAvator);
			entry.setLastVoterNicknames(userNickName);
		}else{
			final String[] visitorNickNamesArray = entry.getLastVoterNicknames().split(DividerChar.ComplexCharWithSemicolon.chars);
			final String[] visitorAvasArray = entry.getLastVoterAvatars().split(DividerChar.ComplexCharWithSemicolon.chars);
			final int maxLength = MathUtil.minInt(visitorIdArray.length, visitorNickNamesArray.length, visitorAvasArray.length, 20);
			//拼接信息
			final StringBuilder allLastVoterIds = new StringBuilder(userIdString);
			final StringBuilder allUserAvators = new StringBuilder(userAvator);
			final StringBuilder allUserNickNames = new StringBuilder(userNickName);
			//去重
			final Set<String> uniqueUserIdSet = new HashSet<String>();
			uniqueUserIdSet.add(userIdString);
			for(int i=0; i<maxLength; i++)
			{
				if(uniqueUserIdSet.contains(visitorIdArray[i])){
					continue;
				}
				uniqueUserIdSet.add(visitorIdArray[i]);
				allLastVoterIds.append(DividerChar.ComplexCharWithSemicolon.chars + visitorIdArray[i]);
				allUserAvators.append(DividerChar.ComplexCharWithSemicolon.chars + visitorAvasArray[i]);
				allUserNickNames.append(DividerChar.ComplexCharWithSemicolon.chars + visitorNickNamesArray[i]);
			}
			entry.setLastVoterIds(allLastVoterIds.toString());
			entry.setLastVoterAvatars(allUserAvators.toString());
			entry.setLastVoterNicknames(allUserNickNames.toString());
		}

		return entry;
	}
	
	/*************
	 * 分割用户信息
	 * @param lastUserIds
	 * @param lastUserAvatars
	 * @param lastUserNicknames
	 * @return
	 */
	public static final List<User> splitUsers(final String lastUserIds, final String lastUserAvatars, final String lastUserNicknames, final int maxCount){
		if(StringUtils.isBlank(lastUserIds) || StringUtils.isBlank(lastUserAvatars) || StringUtils.isBlank(lastUserNicknames)){
			return new ArrayList<User>(0);
		}
		final String[] visitorIdArray = lastUserIds.split(DividerChar.ComplexCharWithSemicolon.chars);
		if(ArrayUtils.isEmpty(visitorIdArray) || visitorIdArray[0].length() < 1){
			return new ArrayList<User>(0);
		}
		final String[] visitorNickNamesArray = lastUserNicknames.split(DividerChar.ComplexCharWithSemicolon.chars);
		final String[] visitorAvasArray = lastUserAvatars.split(DividerChar.ComplexCharWithSemicolon.chars);
		final int maxLength = MathUtil.minInt(visitorIdArray.length, visitorNickNamesArray.length, visitorAvasArray.length, 20);
		final List<User> users = new LinkedList<User>();
		for(int i=0; i<maxLength; i++){
			final long userId = IdUtil.convertTolong(visitorIdArray[i], 0);
			if(userId < 1){
				continue;
			}
			final User u = new User();
			u.setId(userId);
			u.setAvatarUrl(visitorAvasArray[i]);
			u.setNickname(visitorNickNamesArray[i]);
			users.add(u);
			if(maxCount > 0 && users.size() >= maxCount){
				break;
			}
		}
		return users;
	}
	
	/***************
	 * 可以正常提交作品和进行投票
	 * @param match
	 * @return
	 */
	public static final boolean canJoinMatch(final PortalMatch match){
		final long now = System.currentTimeMillis();
		return (match != null && match.getStartTime() < now && match.getEndTime() > now);
	}
	
	public static final boolean canVoteMatch(final PortalMatch match){
		final long now = System.currentTimeMillis();
		return (match != null && match.getStartTime() < now && match.getEndVoteTime() > now);
	}
	
	/**************
	 * 生成作品排序值， 投票多的考前， 同样投票的， 则越老的越在前
	 * @param totalVoteCount
	 * @param entryId
	 * @return
	 */
	public static final long generateEntrySort(final long totalVoteCount, final long entryId){
		long IdBalance = 9999999999L - entryId; //保证老的在前面
		final String v = String.valueOf(totalVoteCount) + String.valueOf(IdBalance);
		return Long.parseLong(v);
	}
	
	public static void main(String[] args)
	{
		System.out.println(generateEntrySort(9000000, 15555555L));//922 3372 0368 5          477 5807
	}
}
