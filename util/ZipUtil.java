package com.sas.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.ThirdPartConstant.FileType;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.SasUser;
import com.sas.core.util.ThumbnailUtil.ImageSizeType;

public final class ZipUtil {
	
	private static final Logger logger = Logger.getLogger(ZipUtil.class);

    /** 
     * 执行压缩操作 
     * @param srcPathName 被压缩的文件/文件夹 
     */  
    public static final void compressUrls(final OutputStream out, final List<String> urls) { 
    	final List<BinaryEntry<String, byte[]>> fileDatas = new LinkedList<BinaryEntry<String, byte[]>>();
    	for(final String url : urls){
    		if(ValidatorUtil.URLValidate(url))
    		{
          final BinaryEntry<String, byte[]> entry = IOUtil.readBinaryEntryFromHttpURL(url, 3);
          if (entry == null){
            continue;
          }
          fileDatas.add(entry);
    		}
    	}
    	ZipUtil.compress(out, fileDatas);    	
    }
    
    /*************
     * 读取网络资源，压缩文件
     * @param out
     * @param urlEntries: 每个对象key为url， value为文件名信息， value可为空
     */
    public static final void compressUrlEntries(final OutputStream out, final List<BinaryEntry<String, String>> urlEntries) { 
    	final List<BinaryEntry<String, byte[]>> fileDatas = new LinkedList<BinaryEntry<String, byte[]>>();
    	int i = 0;
    	for(final BinaryEntry<String, String> urlEntry : urlEntries){
    		if(ValidatorUtil.URLValidate(urlEntry.key))
    		{
    			final byte[] data = IOUtil.readFromHttpURL(null, false, urlEntry.key, 3);
        		if(ArrayUtils.isEmpty(data)){
        			continue;
        		}
        		String fileName = null;
        		if(StringUtils.isBlank(urlEntry.value)){
        			fileName = String.valueOf(++i) + IOUtil.parseFileNameFromURL(urlEntry.key);
        		}else{
        			fileName = String.valueOf(++i) + urlEntry.value;
        		}
        		fileDatas.add(new BinaryEntry<String, byte[]>(fileName, data));
    		}
    	}
    	ZipUtil.compress(out, fileDatas);    	
    }
    
    /***********
     * 压缩文件数据 
     * @param out
     * @param files
     */
    public static final void compressFiles(final OutputStream out, final List<File> files) {
        if (out == null || CollectionUtils.isEmpty(files)){  
            return; 
        }   
    	 try {       
             //CheckedOutputStream cos = new CheckedOutputStream(out,new CRC32());    
             //ZipOutputStream zos = new ZipOutputStream(cos);  
         	 final ZipOutputStream zos = new ZipOutputStream(out);  
             for (File file : files) {  
            	 ZipUtil.compressFile(file, zos, "");    
             }   
             zos.closeEntry(); 
             zos.finish();
             zos.close();
         } catch (Exception e) {  
             logger.error("fail to compressFiles:"+e.getMessage(), e);  
             throw new RuntimeException("fail to compressFiles:"+e.getMessage(), e);    
         }  
    }
    /** 
     * 执行压缩操作 
     * @param srcPathName 被压缩的文件/文件夹 
     */  
    public static final void compress(final OutputStream out, final List<BinaryEntry<String, byte[]>> fileDatas) { 
        if (out == null || CollectionUtils.isEmpty(fileDatas)){  
            return; 
        }  
        try {
            //CheckedOutputStream cos = new CheckedOutputStream(out, new CRC32());    
            //ZipOutputStream zos = new ZipOutputStream(cos);
        	final ZipOutputStream zos = new ZipOutputStream(out);  
            for (BinaryEntry<String, byte[]> fileData : fileDatas) {  
            	ZipUtil.compressFileData(fileData.value, fileData.key, zos);    
            }     
            zos.closeEntry(); 
            zos.finish();
            zos.close();
        } catch (Exception e) {  
            logger.error("fail to compress:"+e.getMessage(), e);  
            throw new RuntimeException(e);    
        }    
    }    
    
	/** 
     * 判断是目录还是文件，根据类型（文件/文件夹）执行不同的压缩方法 
     * @param file  
     * @param out 
     * @param basedir 
     */  
    private static void compressByType(File file, ZipOutputStream out, String basedir) {    
        /* 判断是目录还是文件 */    
        if (file.isDirectory()) {    
            //logger.info("压缩：" + basedir + file.getName());    
        	ZipUtil.compressDirectory(file, out, basedir);    
        } else {    
           // logger.info("压缩：" + basedir + file.getName());    
            ZipUtil.compressFile(file, out, basedir);    
        }    
    }    
    
    /** 
     * 压缩一个文件 
     * @param file 
     * @param out 
     * @param basedir 
     */  
    private static void compressFile(File file, ZipOutputStream out, String basedir) {    
        if (!file.exists()) {    
            return;    
        }    
        try {     
            final ZipEntry entry = new ZipEntry(basedir + file.getName());    
            out.putNextEntry(entry);    
            out.write(IOUtil.outputFromInputStream(new FileInputStream(file), false));
        } catch (Exception e) {    
            throw new RuntimeException("fail to exe compressFile, err="+e.getMessage(), e);    
        }    
    } 
    
    private static void compressFileData(byte[] data, String filePath, ZipOutputStream out) {    
        if (ArrayUtils.isEmpty(data)) {    
            return;    
        }    
        try {        
        	final ZipEntry entry = new ZipEntry(filePath);    
            out.putNextEntry(entry);  
            out.write(data);   
        } catch (Exception e) {    
            throw new RuntimeException("fail to exe compressFileData, err="+e.getMessage(), e);    
        }    
    } 
    
    /** 
     * 压缩一个目录 
     * @param dir 
     * @param out 
     * @param basedir 
     */  
    private static void compressDirectory(File dir, ZipOutputStream out, String basedir) {    
        if (!dir.exists()){  
             return;    
        }  
             
        File[] files = dir.listFiles();    
        for (int i = 0; i < files.length; i++) {    
            /* 递归 */    
        	ZipUtil.compressByType(files[i], out, basedir + dir.getName() + "/");    
        }    
    }
    
    /****************
     * 解压导入商品的压缩包里面的图片文件，每个商品一个文件夹，里面是商品的图片列表， 并上传到七牛，返回文件名和图片url的对应关系MAP
     * @return
     */
    public static Map<String, List<BinaryEntry<String, String>>> decompressAndUploadGoodImportZipPictures(final SasUser su, final InputStream is) 
    {
    	final long sasId = su == null ? 0L : su.getSasId();
    	final long userId = su == null ? 0L : su.getUserId();
    	final Map<String, List<BinaryEntry<String, String>>> result = new HashMap<String, List<BinaryEntry<String, String>>>();
    	ZipArchiveInputStream zais = null;
    	try {
    		zais = (ZipArchiveInputStream)(new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, is));  
    		ArchiveEntry entry = null;
    		//把zip包中的每个文件读取出来
    		//然后把文件写到指定的文件夹
    		while((entry = zais.getNextEntry()) != null)
    		{
    			if(entry.isDirectory()){
    				continue;
    			}
    			//获取文件名
    			final String entryFileName = entry.getName();
				final String[] filePathNames = entryFileName.split("/");
				if(filePathNames.length < 2 || StringUtils.isBlank(filePathNames[0])){
					continue;
				}
    			//构造解压出来的文件存放路径
    			final byte[] imageData = new byte[(int) entry.getSize()];
    			zais.read(imageData);  
           		if(ArrayUtils.isEmpty(imageData)){
           			continue;
           		}
           		// 生成图片KEY
				final String fileKey = QiNiuUtil.generateUploadFileKey(sasId, userId,
						FileType.Image,  null, ImageSizeType.Large);
				final String newUrl = QiNiuUtil.uploadFile2QiNiu(QiNiuSpace.SaihuitongImage,
						fileKey,  imageData, 4);
				//如果无法上传完成则不替换, 上传失败则重试， 概率很低
				if(StringUtils.isBlank(newUrl)){	
					continue;
				}	
           		//存入到MAP
				final String folderName = filePathNames[filePathNames.length - 2].replaceAll(HtmlUtil.WhiteSpaceReg, "").toLowerCase();
				final String fileName = filePathNames[filePathNames.length - 1].replaceAll(HtmlUtil.WhiteSpaceReg, "").toLowerCase();
				List<BinaryEntry<String, String>> list = result.get(folderName);
				if(list == null){
					list = new LinkedList<BinaryEntry<String, String>>();
				}
				list.add(new BinaryEntry<String, String>(QiNiuUtil.removeFileNameExtension(fileName, fileName), newUrl));
				result.put(folderName, list);
    		}
    	}catch(Exception e) {
    		throw new RuntimeException(e);
    	}finally {
    		IOUtil.closeStreamWithoutException(zais, null);
    		IOUtil.closeStreamWithoutException(is, null);
    	}
    	return result;
    }
    	     
	/*****************
	 * 是否是zip file
	 * @param url
	 * @return
	 */
	public static final boolean isZipFile(String url)
	{
		if(url == null){
			return false;
		}
		url = url.toLowerCase();
		return url.endsWith(".zip") || url.endsWith(".rar");
	}
    
    public static void main(String[] args) throws Exception {  
    	final File zipFile = new File("C:/sas.zip");  
    	FileInputStream in = new FileInputStream(zipFile);  
    	final Map<String, List<BinaryEntry<String, String>>> map = ZipUtil.decompressAndUploadGoodImportZipPictures(null, in);
    	for(Entry<String, List<BinaryEntry<String, String>>> entry : map.entrySet()){
    		System.out.println("--------" + entry.getKey());
    		for(final BinaryEntry<String, String> v : entry.getValue()){
    			System.out.print(v.key + ",");
    		}
    		System.out.println();
    	}
    }  
}
