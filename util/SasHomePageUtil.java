/**
 * 
 */
package com.sas.core.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sas.core.constant.CommonConstant;
import com.sas.core.constant.MenuConstant.MenuType;
import com.sas.core.meta.SasHomePageModule;
import com.sas.core.meta.SasHomePageModulePicture;
import com.sas.core.util.meta.SasActivityUtil;
import com.sas.core.util.meta.SasPhotoUtil;

/**
 * @author zhuliming
 *
 */
public class SasHomePageUtil {

	/**************
	 * 设置图片的模块信息
	 * @param module
	 * @param pictures
	 */
	public static final void setModuleInformation(final SasHomePageModule module, final List<SasHomePageModulePicture> pictures)
	{
		//确认图片数据正确并保证顺序
		final long now = System.currentTimeMillis();
		int i = 0;
		for(final SasHomePageModulePicture p : pictures){
			if(StringUtils.isBlank(p.getPicUrl())){
				p.setPicUrl(CommonConstant.image404Url);
			}
			p.setSasId(module.getSasId());
			p.setSort(now + i);
			p.setCreateTime(now); 
		    p.setModuleId(module.getId());
		    i ++;
		}
	}
	
	/*******************
	 * 生成模块的默认sort值
	 * 设置sort值， 因为新增模块在下面，故sort处理方式和别人不同,因为需要放在最底部
	 * @return
	 */
	public static final long generateHomePageModuleDefaultSort(){
		long sort = TimeUtil.getMiliseconds(2060, 1, 1) - System.currentTimeMillis(); //因为需要放在最底部
		if(sort < 1){
			sort = TimeUtil.getMiliseconds(2150, 1, 1) - System.currentTimeMillis();
		}
		return sort;
	}
	
	/**
	 * 判断是否是首页模块的菜单类型
	 */
	public static boolean isHomePageModuleType(final String menuTypeCode) {
		final MenuType type = MenuType.parse(menuTypeCode);
		return (MenuType.MultiArticles.isMe(menuTypeCode)) 
				|| (SasActivityUtil.isActivityMenu(type))
				|| (SasPhotoUtil.isAlbumMenu(type))
				|| (ForumUtil.isBBSMenu(type))
				|| (MenuType.Person == type)
				|| (MenuType.Video == type)
				|| (MenuType.ScoreQueryMenu == type)
				|| (MenuType.MultiGoods == type)
				|| (MenuType.PictureModule == type)
				|| (MenuType.AdModule == type)
				|| (MenuType.ShoeRmd == type)
				|| (MenuType.ApplyQueryMenu == type)
				|| (MenuType.QuickEntryModule == type)
        || (MenuType.GoodsOrActivitySearchModule == type);
	}

	/*********************
	 * 是否拥有相同的模块配置
	 * @param allModules
	 * @param newModuleConfig
	 * @return
	 */
	public static final boolean hasSameMenuModuleConfig(final List<SasHomePageModule> allModules,
			final SasHomePageModule newModuleConfig)
	{
		if(CollectionUtils.isEmpty(allModules)){
			return false;
		}
		final long newModuleConfigMenuId = newModuleConfig.getMenuId();
		for(final SasHomePageModule m : allModules){
			if(newModuleConfigMenuId > 0){
				if(newModuleConfigMenuId == m.getMenuId()){
					return true;
				}
			}else{
				if(newModuleConfig.getMenuTypeCode().equals(m.getMenuTypeCode())){
					return true;
				}
			}
		}			
		return false;
	}
}
