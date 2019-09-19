/**
 * 
 */
package com.sas.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.exception.OCRException;

/**
 * @author Administrator
 *
 */
public class OCRUtil {
	
	//默认输出输出文件的文件名
	private static final  String OUTPUT_FILE_NAME_BASE = "ocr_result";  
	
	//语言的参数
	private static final  String LANG_OPTION = "-l";  
	
	//换行分隔符
	private static final  String EOL = System.getProperty("line.separator");  
	
	/** 
	 * 文件位置我防止在，项目同一路径 
	 */  
	private static String tessPath = "D:\\Tesseract-OCR"; //new File("tesseract").getAbsolutePath(); 
	
	/*************
	 * 初始化文件位置
	 * @param tessDir
	 */
	public static final void init(final String tessDir){
		tessPath = tessDir;
	}
	  
   /** 
    * 识别图片中的文字
	* @param imageFile 传入的图像文件 
	* @param imageFormat 传入的图像格式 
	* @return 识别后的字符串 
	*/  
	public static final String recognizeText(File imageFile) throws Exception  
	{  
	   /** 
	    * 设置输出文件的保存的文件目录 
	    */
		final File outputFile = new File(imageFile.getParentFile(), OUTPUT_FILE_NAME_BASE + ".txt");
		if(outputFile.exists()){
			outputFile.delete();
		}
		/**********
		 * tesseract imagename outputbase [-l lang] [-psm pagesegmode] [configfile...]
		 * tesseract    图片名  输出文件名 -l 字库文件 -psm pagesegmode 配置文件
		 * 例如：tesseract code.jpg result  -l chi_sim -psm 7 nobatch -l chi_sim 表示用简体中文字库（需要下载中文字库文件，
		 * 解压后，存放到tessdata目录下去,字库文件扩展名为  .raineddata 简体中文字库文件名为:  chi_sim.traineddata）
		 * -psm 7 表示告诉tesseract code.jpg图片是一行文本  这个参数可以减少识别错误率.  默认为 3
		 * configfile 参数值为tessdata\configs 和  tessdata\tessconfigs 目录下的文件名
		 */
	    final List<String> cmd = new LinkedList<String>();  
	    if (OCRUtil.isWindowOS()){
	    	cmd.add(tessPath + "\\tesseract");  
	    } else {  
	       cmd.add("tesseract");  
	    }
	    cmd.add("");  
	    cmd.add(OUTPUT_FILE_NAME_BASE);  
	    cmd.add(LANG_OPTION);  
	    //cmd.add("chi_sim");  
	    cmd.add("eng");
	  
	    //创建进程
	    final ProcessBuilder processBuilder = new ProcessBuilder();
	    
	    /** 
	     *Sets this process builder's working directory. 
	     */  
	    processBuilder.directory(imageFile.getParentFile());  
	    cmd.set(1, imageFile.getName());  
	    processBuilder.command(cmd);  
	    processBuilder.redirectErrorStream(true);  
	    final Process process = processBuilder.start();  
	    // tesseract.exe 1.jpg 1 -l chi_sim  
	    // Runtime.getRuntime().exec("tesseract.exe 1.jpg 1 -l chi_sim");  
	    //the exit value of the process. By convention, 0 indicates normal termination.
	    //System.out.println(cmd.toString());  
	    final int state = process.waitFor();  
	    if (state != 0) { // 0代表正常退出
	    	switch (state)  
	    	{  
	            case 1:  
	            	throw new OCRException("Errors accessing file " +imageFile.getAbsolutePath()+ "， There may be spaces in your image's filename."); 
	            case 29:  
	            	throw new OCRException("Cannot recognize the image " +imageFile.getAbsolutePath()+ " or its selected region."); 
	            case 31:  
	            	throw new OCRException("Unsupported image format： " +imageFile.getAbsolutePath()); 
	            default:  
	            	throw new OCRException("Errors occurred， state=" + state); 
	        }
	    }
	    final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), Encoding.UTF8.type));
	    final StringBuilder result = new StringBuilder("");  
	    String str = null;
	    while ((str = in.readLine()) != null) {	
	      	result.append(str).append(EOL);  
	    }
	    in.close();  
	    outputFile.delete();  
	    return result.toString().replaceAll("\\s*", "");
	 }
	
	
	/*************
	 * 是否是windows的操作系统
	 * @return
	 */
	public static final boolean isWindowOS()
	{
		final Properties prop = System.getProperties();
		final String os = prop.getProperty("os.name");
		return os != null && (os.startsWith("win") || os.startsWith("Win"));
	}
	
	public static void main(String[] args)  
    {  
		String recognizeText;
		try {
			recognizeText = OCRUtil.recognizeText(new File("D:\\Tesseract-OCR\\2.jpg"));
	        System.out.print(recognizeText+"\t"); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
  
    }  
	
}
