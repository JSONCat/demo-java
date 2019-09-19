package com.sas.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import com.sas.core.constant.FullTextSearchConstant.FullTextField;
import com.sas.core.constant.ShoeConstant;
import com.sas.core.constant.ShoeConstant.ShoeFunction;
import com.sas.core.constant.ThirdPartConstant.QiNiuImageCompress;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.Shoe;
import com.sas.core.meta.ShoeComment;

public class ShoeUtil {
	
	private static final Logger logger = Logger.getLogger(ShoeUtil.class);
	
	/********************
	 * 创建跑鞋详情页的链接
	 * @param id
	 * @return
	 */
	public static final String createShoeDetailPageURL(final long shoeId)
	{
		return "/shoes?id=" + shoeId;
	}
	
	/*****************
	 * 将一批shoe转成document列表
	 * @param list
	 * @return
	 */
	public static final List<Document> convert2Documents(final List<Shoe> list){
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<Document>(0);
		}
		final List<Document> result = new ArrayList<Document>(list.size());
		for(final Shoe shoe : list){
			final Document doc = convert2Document(shoe);
			if(doc != null){
				result.add(doc);
			}
		}		
		return result;
	}
	
	/*****************
	 * 将一个shoe转成一个doc	
	 * @param Video
	 * @return
	 */
	public static final Document convert2Document(final Shoe shoe){
		try{
			Document doc = new Document();
			doc.add(new StringField(FullTextField.Id.name, String.valueOf(shoe.getId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField(FullTextField.UserId.name, String.valueOf(shoe.getUserId()), Field.Store.YES));//不分词的索引	
			doc.add(new StringField("brandId", String.valueOf(shoe.getBrandId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField("brandName", shoe.getBrandName(), Field.Store.YES));//不分词的索引	
			doc.add(new StringField("brandTypeId", String.valueOf(shoe.getBrandTypeId()), Field.Store.YES));//不分词的索引'
			doc.add(new TextField("brandTypeName", LuceneAnalyzerUtil.splitContentsByWhiteSpace2JoinWhiteSpace(shoe.getBrandTypeName()), Field.Store.YES));//分词的索引
			doc.add(new StringField("sex", String.valueOf(shoe.getSex()), Field.Store.YES));//不分词的索引
			doc.add(new StringField("purpose", String.valueOf(shoe.getPurpose()), Field.Store.YES));//不分词的索引
			doc.add(new IntField("weight", shoe.getWeight(), Field.Store.YES));//不分词的索引
			doc.add(new IntField("height", shoe.getHeight().intValue(), Field.Store.YES));//不分词的索引
			doc.add(new IntField("stability", shoe.getStability().intValue(), Field.Store.YES));//不分词的索引
			doc.add(new TextField("price", LuceneAnalyzerUtil.instance.splitContents2JoinWhiteSpace(shoe.getPrice()), Field.Store.YES));//不分词的索引
			doc.add(new IntField("brandPrice", shoe.getBrandPrice(), Field.Store.YES));//不分词的索引
			doc.add(new IntField("footSoleThickness", shoe.getFootSoleThickness().intValue(), Field.Store.YES));//不分词的索引
			doc.add(new IntField("footHeelThickness", shoe.getFootHeelThickness().intValue(), Field.Store.YES));//不分词的索引
			doc.add(new IntField("footThicknessBalance", shoe.getFootThicknessBalance().intValue(), Field.Store.YES));//不分词的索引
			doc.add(new StringField("footArchSupport", String.valueOf(shoe.getFootArchSupport()), Field.Store.YES));//不分词的索引
			doc.add(new StringField("footAnkleSupport", String.valueOf(shoe.getFootAnkleSupport()), Field.Store.YES));	
			doc.add(new StringField("footArchType", String.valueOf(shoe.getFootArchType()), Field.Store.YES));	
			doc.add(new StringField("runPosture", String.valueOf(shoe.getRunPosture()), Field.Store.YES));	
			doc.add(new StringField("function", String.valueOf(shoe.getFunction()), Field.Store.YES));	
			doc.add(new IntField("shoeFaceReverse", shoe.getShoeFaceReverse().intValue(), Field.Store.YES));	
			doc.add(new IntField("shoeBottomReverse", shoe.getShoeBottomReverse().intValue(), Field.Store.YES));	
			doc.add(new StringField("kind", String.valueOf(shoe.getKind()), Field.Store.YES));//不分词的索引			
			doc.add(new IntField("showDurability", shoe.getShowDurability().intValue(), Field.Store.YES));	
			doc.add(new IntField("comfort", shoe.getComfort().intValue(), Field.Store.YES));	
			doc.add(new StringField("roadSuit", String.valueOf(shoe.getRoadSuit()), Field.Store.YES));	
			doc.add(new LongField("sort", shoe.getSort(), Field.Store.YES));//权重
			doc.add(new LongField(FullTextField.CreateTime.name, shoe.getCreateTime(), Field.Store.YES));
			doc.add(new TextField(FullTextField.SearchData.name,  
					shoe.getBrandName().trim()
					+ " "
					+ shoe.getBrandTypeName().trim()
					+ " " 
					+ LuceneAnalyzerUtil.instance.splitContents2JoinWhiteSpace(shoe.getBrandName() + " " + shoe.getBrandTypeName())
					+ " "
					+ LuceneAnalyzerUtil.splitContentsByWhiteSpace2JoinWhiteSpace(shoe.getKeywords()) 
					, Field.Store.NO));
			return doc;
		}catch(Exception ex){
			logger.error("Fail to createDocument, ex="+ex.getMessage() + ", shoe=" + ReflectionToStringBuilder.toString(shoe), ex);
			return null;
		}
	}
	
	public static void main(String[] args){
		System.out.println(LuceneAnalyzerUtil.splitContentsByWhiteSpace2JoinWhiteSpace("aad ss%e%fr4 s_lab"));
	}
	
	/****************
	 * 对评论添加压缩样式， 同时转化表情
	 * @param comments
	 * @param emotionDomain
	 * @param compreeStyle
	 * @return
	 */
	public static final List<ShoeComment> addEmotionAndImageCompressStyleList(final List<ShoeComment> comments,
			final String emotionDomain, final QiNiuImageCompress compressStyle)
	{
		if(CollectionUtils.isEmpty(comments)){
			return comments;
		}
		for(final ShoeComment comment : comments){
			ShoeUtil.addEmotionAndImageCompressStyle(comment, emotionDomain, compressStyle);
		}
		return comments;
	}
	
	public static final ShoeComment addEmotionAndImageCompressStyle(ShoeComment comment,
			final String emotionDomain, final QiNiuImageCompress compressStyle)
	{
		comment.setContent(EmotionUtil.emotion2Img(emotionDomain, comment.getContent()));
		comment.setContent(HtmlUtil.processContent4Mobile(comment.getContent(), compressStyle.style, false));
		return comment;
	}
	
	/***************
	 * 对用户输入的价格进行解析
	 * @param price
	 * @param dividerChar
	 * @return
	 */
	public static final BinaryEntry<Integer, Integer> parseShoeSearchPrice(final String price, final String dividerChar)
	{
		int minPrice = ShoeConstant.minShoePrice, maxPrice = ShoeConstant.maxShoePrice;
		if (StringUtils.isNotBlank(price)) {
			final String[] priceArray = price.split(dividerChar);
			if (priceArray.length >= 2) {
				minPrice = IdUtil.convertToInteger(priceArray[0], 0);
				if(minPrice < ShoeConstant.minShoePrice){
					minPrice = ShoeConstant.minShoePrice;
				}
				maxPrice = IdUtil.convertToInteger(priceArray[1], maxPrice);
				if (maxPrice <= minPrice || maxPrice > ShoeConstant.maxShoePrice) {
					maxPrice = ShoeConstant.maxShoePrice;
				}
			}
		}
		return new BinaryEntry<Integer, Integer>(minPrice, maxPrice);
	}
	
	/*************
	 * 计算百分比
	 * 
	 * @param rank
	 * @param totalCount
	 * @return
	 */
	public static int calculatePercentRatio(final int rank, final long totalCount) {
		if (rank < 1 || totalCount < 1) {
			return 0;
		}
		if (rank >= totalCount) {
			return 100;
		}
		final long result = 1L * rank * 1000 / totalCount;
		return (result % 10) >= 5 ? (int)(result/10)+1 :  (int)(result/10);
	}
	
	/**************
	 * 生成重新搜索的链接参数
	 * 
	 * @param url
	 * @param paramNames
	 * @param paramValues
	 * @return
	 */
	public static String createReSeachURL(final String url, final String[] paramNames, final String[] paramValues) {
		final StringBuilder sb = new StringBuilder(url);
		if (ArrayUtils.isNotEmpty(paramNames)) {
			sb.append("?");
			for (int i = 0; i < paramNames.length; i++) {
				if (i > 0) {
					sb.append("&");
				}
				sb.append(paramNames[i] + "=" + String.valueOf(paramValues[i]));
			}
		}
		return sb.toString();
	}
	
	/**********************
	 * 计算跑鞋重最热门的功能类型
	 * @param shoes
	 * @return
	 */
	public static final ShoeFunction calculateHotesFunctionsByShoes(final List<Shoe> shoes)
	{
		if(CollectionUtils.isEmpty(shoes)){
			return null;
		}
		final Map<ShoeFunction, Integer> map = new HashMap<ShoeFunction, Integer>();
		for(final Shoe shoe : shoes){
			for(final ShoeFunction f : ShoeFunction.values()){
				if(f.isMe(shoe.getFunction())){
					Integer total = map.get(f);
					if(total == null){
						map.put(f, 1);
					}else{
						map.put(f, total+1);
					}
				}
			}
		}
		if(map.size() < 1){
			return null;
		}
		ShoeFunction hotFunction = null;
		int hotestCount = 0;
		for(Entry<ShoeFunction, Integer> entry : map.entrySet()){
			if(hotFunction == null || hotestCount < entry.getValue()){
				hotFunction = entry.getKey();
				hotestCount = entry.getValue();
			}
		}
		return hotFunction;
	}
	
}
