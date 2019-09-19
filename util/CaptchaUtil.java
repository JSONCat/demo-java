/**
 * @Title: CaptchaUtil.java
 * @Package com.sas.core.util
 * @author yunshang_734@163.com
 * @date Dec 20, 2014 9:21:34 AM
 * @version V1.0
 */
package com.sas.core.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.octo.captcha.CaptchaFactory;
import com.octo.captcha.component.image.backgroundgenerator.FileReaderRandomBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomRangeColorGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformation;
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.BaffleTextDecorator;
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.gimpy.SimpleListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;
import com.sas.core.constant.SasConstant.SasLanguage;
import com.sas.core.meta.Sas;

/**
 * @ClassName: CaptchaUtil
 * @Description: 验证码生成工具，获取图片验证码生成服务
 * @author yunshang_734@163.com
 * @date Dec 20, 2014 9:21:34 AM
 */
public class CaptchaUtil {
	private static final Logger logger = Logger.getLogger(CaptchaUtil.class);

	private static final String ChineseLetters = "\u7684\u4E00\u4E86\u662F\u6211\u4E0D\u5728\u4EBA\u4EEC\u6709\u6765\u4ED6"
			+ "\u8FD9\u4E0A\u7740\u4E2A\u5730\u5230\u5927\u91CC\u8BF4\u5C31\u53BB\u5B50\u5F97\u4E5F"
			+ "\u548C\u90A3\u8981\u4E0B\u770B\u5929\u65F6\u8FC7\u51FA\u5C0F\u4E48\u8D77\u4F60\u90FD"
			+ "\u628A\u597D\u8FD8\u591A\u6CA1\u4E3A\u53C8\u53EF\u5BB6\u5B66\u53EA\u4EE5\u4E3B\u4F1A"
			+ "\u6837\u5E74\u60F3\u751F\u540C\u8001\u4E2D\u5341\u4ECE\u81EA\u9762\u524D\u5934\u9053"
			+ "\u5B83\u540E\u7136\u8D70\u5F88\u50CF\u89C1\u4E24\u7528\u5979\u56FD\u52A8\u8FDB\u6210"
			+ "\u56DE\u4EC0\u8FB9\u4F5C\u5BF9\u5F00\u800C\u5DF1\u4E9B\u73B0\u5C71\u6C11\u5019\u7ECF"
			+ "\u53D1\u5DE5\u5411\u4E8B\u547D\u7ED9\u957F\u6C34\u51E0\u4E49\u4E09\u58F0\u4E8E\u9AD8"
			+ "\u624B\u77E5\u7406\u773C\u5FD7\u70B9\u5FC3\u6218\u4E8C\u95EE\u4F46\u8EAB\u65B9\u5B9E"
			+ "\u5403\u505A\u53EB\u5F53\u4F4F\u542C\u9769\u6253\u5462\u771F\u5168\u624D\u56DB\u5DF2"
			+ "\u6240\u654C\u4E4B\u6700\u5149\u4EA7\u60C5\u8DEF\u5206\u603B\u6761\u767D\u8BDD\u4E1C"
			+ "\u5E2D\u6B21\u4EB2\u5982\u88AB\u82B1\u53E3\u653E\u513F\u5E38\u6C14\u4E94\u7B2C\u4F7F"
			+ "\u5199\u519B\u5427\u6587\u8FD0\u518D\u679C\u600E\u5B9A\u8BB8\u5FEB\u660E\u884C\u56E0"
			+ "\u522B\u98DE\u5916\u6811\u7269\u6D3B\u90E8\u95E8\u65E0\u5F80\u8239\u671B\u65B0\u5E26"
			+ "\u961F\u5148\u529B\u5B8C\u5374\u7AD9\u4EE3\u5458\u673A\u66F4\u4E5D\u60A8\u6BCF\u98CE"
			+ "\u7EA7\u8DDF\u7B11\u554A\u5B69\u4E07\u5C11\u76F4\u610F\u591C\u6BD4\u9636\u8FDE\u8F66"
			+ "\u91CD\u4FBF\u6597\u9A6C\u54EA\u5316\u592A\u6307\u53D8\u793E\u4F3C\u58EB\u8005\u5E72"
			+ "\u77F3\u6EE1\u65E5\u51B3\u767E\u539F\u62FF\u7FA4\u7A76\u5404\u516D\u672C\u601D\u89E3"
			+ "\u7ACB\u6CB3\u6751\u516B\u96BE\u65E9\u8BBA\u5417\u6839\u5171\u8BA9\u76F8\u7814\u4ECA"
			+ "\u5176\u4E66\u5750\u63A5\u5E94\u5173\u4FE1\u89C9\u6B65\u53CD\u5904\u8BB0\u5C06\u5343"
			+ "\u627E\u4E89\u9886\u6216\u5E08\u7ED3\u5757\u8DD1\u8C01\u8349\u8D8A\u5B57\u52A0\u811A"
			+ "\u7D27\u7231\u7B49\u4E60\u9635\u6015\u6708\u9752\u534A\u706B\u6CD5\u9898\u5EFA\u8D76"
			+ "\u4F4D\u5531\u6D77\u4E03\u5973\u4EFB\u4EF6\u611F\u51C6\u5F20\u56E2\u5C4B\u79BB\u8272"
			+ "\u8138\u7247\u79D1\u5012\u775B\u5229\u4E16\u521A\u4E14\u7531\u9001\u5207\u661F\u5BFC"
			+ "\u665A\u8868\u591F\u6574\u8BA4\u54CD\u96EA\u6D41\u672A\u573A\u8BE5\u5E76\u5E95\u6DF1"
			+ "\u523B\u5E73\u4F1F\u5FD9\u63D0\u786E\u8FD1\u4EAE\u8F7B\u8BB2\u519C\u53E4\u9ED1\u544A"
			+ "\u754C\u62C9\u540D\u5440\u571F\u6E05\u9633\u7167\u529E\u53F2\u6539\u5386\u8F6C\u753B"
			+ "\u9020\u5634\u6B64\u6CBB\u5317\u5FC5\u670D\u96E8\u7A7F\u5185\u8BC6\u9A8C\u4F20\u4E1A"
			+ "\u83DC\u722C\u7761\u5174\u5F62\u91CF\u54B1\u89C2\u82E6\u4F53\u4F17\u901A\u51B2\u5408"
			+ "\u7834\u53CB\u5EA6\u672F\u996D\u516C\u65C1\u623F\u6781\u5357\u67AA\u8BFB\u6C99\u5C81"
			+ "\u7EBF\u91CE\u575A\u7A7A\u6536\u7B97\u81F3\u653F\u57CE\u52B3\u843D\u94B1\u7279\u56F4"
			+ "\u5F1F\u80DC\u6559\u70ED\u5C55\u5305\u6B4C\u7C7B\u6E10\u5F3A\u6570\u4E61\u547C\u6027"
			+ "\u97F3\u7B54\u54E5\u9645\u65E7\u795E\u5EA7\u7AE0\u5E2E\u5566\u53D7\u7CFB\u4EE4\u8DF3"
			+ "\u975E\u4F55\u725B\u53D6\u5165\u5CB8\u6562\u6389\u5FFD\u79CD\u88C5\u9876\u6025\u6797"
			+ "\u505C\u606F\u53E5\u533A\u8863\u822C\u62A5\u53F6\u538B\u6162\u53D4\u80CC\u7EC6";
	
	private static final String EnglishDigitalLetters = "0123456789abcdefghijklmnopqrstuvwxyz"; //用于英文版
	
	/**
	 * 初始化图片验证码生成服务
	 */
	public final static ImageCaptchaService chineseCaptchaInstance = getInitializeService(false);
	
	public final static ImageCaptchaService englishCaptchaInstance = getInitializeService(true);

	/**
	 * 错误控制，验证码生成器必须为静态唯一！
	 * @return
	 */
	private final static ImageCaptchaService getInitializeService(final boolean isEnglish) {
		try {
			return initializeService(isEnglish);
		} catch (Exception e) {
			logger.error("Failed to init captcha! Exception is " + e + ", isEnglishLanguage:" + isEnglish, e);
			return null;
		}
	}
	
	/**
	 * @Title: valid
	 * @Description: 通过sessionId和code验证验证码是否有效
	 */
	public final static boolean valid(final String sid, final String code) {
		try {
			// 通過sessionId和用戶輸入的內容，驗證用戶輸入是否正確
			if (StringUtils.isBlank(code) || StringUtils.isBlank(sid)) {
				return false;
			}
			final char ch = code.charAt(0);
			if((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')){
				return englishCaptchaInstance.validateResponseForID(sid, code);
			}else{
				return chineseCaptchaInstance.validateResponseForID(sid, code);
			}
		} catch (CaptchaServiceException e) {
			logger.error("Failed to valid captcha code! Exception : " + e, e);
			return false;
		}
	}

	/************
	 * 生成验证码图片
	 * @param request
	 * @param sas
	 * @param sessionId
	 * @return
	 */
	public static final BufferedImage generateImageCaptchaCode(final HttpServletRequest request, final Sas sas, String sessionId){
		if(sas == null || sas.getLanguageSupport() != SasLanguage.EnglishOnly.type){
			return chineseCaptchaInstance.getImageChallengeForID(sessionId, request.getLocale());
		}else{
			return englishCaptchaInstance.getImageChallengeForID(sessionId, request.getLocale());
		}
	}
	
	/**
	 * 错误控制，验证码生成器必须为静态唯一！
	 * 图片验证码生成器
	 * 
	 * @return
	 */
	private final static ImageCaptchaService initializeService(final boolean isEnglish)
	{
		// 设置生成字体大小，该方法会生成扭曲、倾斜的字体
		Font[] fontsList = new Font[] { Font.decode("Arial"),
				Font.decode("Tahoma"), Font.decode("Verdana") };
		// new Font("Helvetica", 0, 15), new Font("宋体", 0, 15),
		// new Font("黑体", 0, 15), new Font("幼圆", 0, 15) };
		fontsList = new Font[] { Font.decode("微软雅黑") };// 可以使用中文验证码，另外汉字宽度比较大，要重新调整一下字体大小,不然会出现异常

		// 字体随机生成
		// 第一个参数为最小字体，第二个为最大字体，第三个为字体类型集合
		final RandomFontGenerator fonts = new RandomFontGenerator(new Integer(
				20), new Integer(22), fontsList);

		// // 设置背景图片路径
		final FileReaderRandomBackgroundGenerator background = new FileReaderRandomBackgroundGenerator(
				100, 36, "/core/captcha");

		// 背景颜色随机生成
		// 验证码的颜色-使用随机颜色器new Integer[]{0,100},new Integer[]{0,100}, new
		// Integer[]{0,100}
		RandomRangeColorGenerator cgen = null;

		// 第一个参数为取词最小个数，第二个为取词最大个数，第三个为是装饰数组，第四个为是否有干扰线，第五个为设置槽点个数与字体上面槽点的颜色
		TextPaster textPaster = null;
		if(isEnglish){
			cgen=new RandomRangeColorGenerator(
					new int[] { 0, 150 }, 
					new int[] { 0, 150 },
					new int[] { 0, 150 });
			textPaster = new DecoratedRandomTextPaster(new Integer(3),
					new Integer(4), cgen, true,
					new TextDecorator[] { new BaffleTextDecorator(new Integer(3),Color.lightGray) });
		}else{	
			cgen=new RandomRangeColorGenerator(
					new int[] { 0, 180 }, 
					new int[] { 0, 180 },
					new int[] { 0, 180 });
			textPaster = new DecoratedRandomTextPaster(new Integer(2),
				new Integer(2), cgen, true,
				new TextDecorator[] { new BaffleTextDecorator(new Integer(2),Color.lightGray) });
		}
		// 创建过滤器
		ImageDeformation noneDeformation = new ImageDeformationByFilters(
				new ImageFilter[] {});

		ImageDeformation filters = new ImageDeformationByFilters(
				new ImageFilter[] {});
		DeformedComposedWordToImage cwti = new DeformedComposedWordToImage(
				fonts, background, textPaster, noneDeformation, filters,
				noneDeformation);
		// 设置随机取词范围
		WordGenerator words = null;
		if(isEnglish){
			words = new RandomWordGenerator(CaptchaUtil.EnglishDigitalLetters);
		}else{
			words = new RandomWordGenerator(CaptchaUtil.ChineseLetters);
		}
		final GimpyFactory gimpy = new GimpyFactory(words, cwti);
		// 这个类,默认的添加了一种生成文字的工厂类，是它生成文字模板
		final SimpleListImageCaptchaEngine engine = new SimpleListImageCaptchaEngine();
		engine.setFactories(new CaptchaFactory[] { gimpy });
		FastHashMapCaptchaStore captchaStore = new FastHashMapCaptchaStore();
		// 第一个是存储器,存储生成的文本,最終等待User输入正确的验证码,验证是否正确
		// 第二个生成图片的引擎
		// 第三个最小保证生成存储的时间，单位是秒
		// 第四个最大的存储大小（生成图片的最大使用的大小）
		// 第五个Captcha在存储安装前的数量（未明白）
		DefaultManageableImageCaptchaService defaultService = new DefaultManageableImageCaptchaService(
				captchaStore, engine, 180, 100000, 75000);
		return defaultService;
	}
}