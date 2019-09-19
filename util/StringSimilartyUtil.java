package com.sas.core.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/*****************
 * 计算两个字符串的相似度
 * @author zhuliming
 *
 */
public class StringSimilartyUtil {
	
	/****************
	 * 计算相似度
	 * @param doc1
	 * @param doc2
	 * @return
	 */
	public static double calculateSimilarity(String doc1, String doc2) 
	{
		if(StringUtils.isBlank(doc1) || StringUtils.isBlank(doc2)){
			return 0;
		}
		doc1 = StringUtils.reverse(doc1);
		doc2 = StringUtils.reverse(doc2);
		Map<Integer, int[]> algorithmMap = new HashMap<Integer, int[]>();
		//将两个字符串中的中文字符以及出现的总数封装到，AlgorithmMap中
		for (int i = 0; i < doc1.length(); i++) {
			char d1 = doc1.charAt(i);
			int charIndex = (int)d1;
			if(charIndex != -1){
				int[] fq = algorithmMap.get(charIndex);
				if(fq != null && fq.length == 2){
					fq[0]++;
				}else {
					fq = new int[2];
					fq[0] = 1;
					fq[1] = 0;
					algorithmMap.put(charIndex, fq);
				}
			}			
		}
		//计算第二个里面存在的字符情况
		for (int i = 0; i < doc2.length(); i++) 
		{
			char d2 = doc2.charAt(i);
			int charIndex = (int)d2;
			if(charIndex != -1){
				int[] fq = algorithmMap.get(charIndex);
				if(fq != null && fq.length == 2){
					fq[1]++;
				}else {
					fq = new int[2];
					fq[0] = 0;
					fq[1] = 1;
					algorithmMap.put(charIndex, fq);
				}
			}			
		}
		//计算复杂度
		double sqdoc1 = 0;
		double sqdoc2 = 0;
		double denominator = 0;
		for(Map.Entry<Integer, int[]> entry: algorithmMap.entrySet()){
			int[] c = entry.getValue();
			denominator += c[0]*c[1];
			sqdoc1 += c[0]*c[0];
			sqdoc2 += c[1]*c[1];
		}
		
		return denominator / Math.sqrt(sqdoc1*sqdoc2);
	}
}
