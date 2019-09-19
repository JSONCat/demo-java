package com.sas.core.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.sas.core.constant.FullTextSearchConstant.FullTextField;

/******************
 * lucence相关的api
 * @author zhuliming
 *
 */
public class LuceneAnalyzerUtil {

	private final Logger logger = Logger.getLogger(LuceneAnalyzerUtil.class);
	
	public static final LuceneAnalyzerUtil instance = new LuceneAnalyzerUtil();
	
	private Analyzer paodingAnalyzer = null;
	private Analyzer cjkAnalyzer = null;
	
	//自定义的分词算法，单字分词 以及 数字和符合联合, 主要是商品包含了多个型号， 如：S12*12/12
	private Set<String> splitBySelfDefinedAnalyzer(final String content) throws IOException
	{
		final Set<String> result = new HashSet<String>();
		StringBuilder preNumberAndChars = new StringBuilder("");
		StringBuilder preNumberAndCharsAndSymbols = new StringBuilder(""); 
		//boolean hasSymbolChars = false; 
		for(final char ch : content.toCharArray())
		{
			if(HtmlUtil.isDigit(ch) || HtmlUtil.isEnglishLetter(ch) || ch == '.' ){
				preNumberAndCharsAndSymbols.append(ch);
				preNumberAndChars.append(ch);
			}else{
				if(preNumberAndCharsAndSymbols.length() > 0){
					result.add(preNumberAndCharsAndSymbols.toString());
				}
				if(preNumberAndChars.length() > 0){
					result.add(preNumberAndChars.toString());
				}
				if(ch == '~' || ch == '!' || ch == '@' || ch == '#' || ch == '$' 
						|| ch == '%' || ch == '^' || ch == '&' || ch == '*' || ch == '('
						|| ch == ')' || ch == '-' || ch == '+' || ch == '=' || ch == '_' 
						|| ch == '<' || ch == '>' || ch == '/' || ch == '?')
				{
					//if(hasSymbolChars){
					//	preNumberAndCharsAndSymbols = preNumberAndChars;
					//}
					preNumberAndCharsAndSymbols.append(ch);
					//hasSymbolChars = true;
				}else{
					if(HtmlUtil.isChineseLetter(ch)){
						result.add(String.valueOf(ch));
					}
					if(preNumberAndCharsAndSymbols.length() > 0){
						preNumberAndCharsAndSymbols = new StringBuilder("");
					}
				}
				if(preNumberAndChars.length() > 0){
					preNumberAndChars = new StringBuilder("");
				}
			}
		}
		if(preNumberAndChars.length() > 0){
			result.add(preNumberAndChars.toString());
		}
		if(preNumberAndCharsAndSymbols.length() > 0){
			result.add(preNumberAndCharsAndSymbols.toString());
		}
		return result;
	}
	
	//庖丁分词器
	private synchronized Set<String> splitByPaodingAnalyzer(final String content) throws IOException
	{
		if(paodingAnalyzer == null){
			paodingAnalyzer = new PaodingAnalyzer();
		}
		final Set<String> result = new HashSet<String>();
		final TokenStream ts = this.paodingAnalyzer.tokenStream(FullTextField.SearchData.name, content);  
        final CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);  
        ts.reset();  
        while (ts.incrementToken()) {
        	if(ch.length() > 1){
        		result.add(ch.toString());
        	}
        }  
        ts.end();  
        ts.close();
        return result;
	}
	
	//CJK分词器
	private synchronized Set<String> splitByCJKAnalyzer(final String content) throws IOException
	{
		if(cjkAnalyzer == null){
			cjkAnalyzer = new CJKAnalyzer(LuceneUtil.LucenceVersion);
		}
		final Set<String> result = new HashSet<String>();
		final TokenStream ts = this.cjkAnalyzer.tokenStream(FullTextField.SearchData.name, content);  
        final CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);  
        ts.reset();  
        while (ts.incrementToken()) { 
        	if(ch.length() > 1){
        		result.add(ch.toString());
        	}
        }  
        ts.end();  
        ts.close();
        return result;
	}
	
	/*****************
	 * 将一个文本分成一个个词
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public final Set<String> splitContentsAsSet(final String content) throws IOException
	{
		final Set<String> result = new HashSet<String>();
		if(content == null){
			return result;
		}
		//采用多个分词器进行分词
		result.addAll(this.splitBySelfDefinedAnalyzer(content));
		result.addAll(this.splitByCJKAnalyzer(content));
		result.addAll(this.splitByPaodingAnalyzer(content));
        return result;
	}
	
	public final String[] splitContentsAsArray(final String content) throws IOException
	{
		final Set<String> keywords = this.splitContentsAsSet(content);
        return keywords.toArray(new String[keywords.size()]);
	}
	
	public final String splitContents2JoinWhiteSpace(final String content) throws IOException
	{
		if(content == null){
			return "";
		}
		final Set<String> keywords = this.splitContentsAsSet(content);
		final StringBuilder result = new StringBuilder("");
		for(final String k : keywords){
			if(result.length() > 0){
				result.append(" ");
			}
			result.append(k);
		}
        return result.toString();
	}

	/***********
	 * 解决能搜出s_lab但是搜不出lab的情况
	 * @param text
	 * @return
	 */
	public static final String splitContentsByWhiteSpace2JoinWhiteSpace(final String text)
	{
		final String[] array = text.split("(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)");
		final StringBuilder result = new StringBuilder("");
		for(final String str : array)
		{
			if(StringUtils.isBlank(str)){
				continue;
			}
			if(result.length() > 0){
				result.append(" ");
			}
			result.append(str);
		}
		return result.toString();		
	}
	
	public static final String[] splitContentsByWhileSpace(final String text)
	{
		return text.split("(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)");
	}
	
	
	public static final void main(String[] args) throws IOException{
		final Set<String> arr = LuceneAnalyzerUtil.instance.splitBySelfDefinedAnalyzer("185*170*2.5*170~123我的好12.12*12.1*212-12");
		for(final String a : arr){
			System.out.println(a);
		}
	}
}
