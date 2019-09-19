package com.sas.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.sas.core.constant.FullTextSearchConstant;
import com.sas.core.constant.FullTextSearchConstant.FullTextData;
import com.sas.core.constant.FullTextSearchConstant.FullTextField;
import com.sas.core.dto.PageData;
import com.sas.core.dto.Paginator;
import com.sas.core.exception.ServerUnknownException;
import com.sas.core.service.fulltext.QueryCreator;

/******************
 * lucence相关的api
 * @author zhuliming
 *
 */
public class LuceneUtil {

	private static final Logger logger = Logger.getLogger(LuceneUtil.class);
	
    //关键字日志
    public static final Logger keywordLogger = Logger.getLogger("com.logger.Keyword");
    
	//version
	public static final Version LucenceVersion = Version.LUCENE_48;
	
	public static final LuceneUtil instance = new LuceneUtil();
	
	private LuceneUtil(){}
	
	private File baseDir = null;
	private Map<FullTextData, String> indexDirPaths = null; //所有的索引目录路径
	
	/***********
	 * 全文检索变量, 都是单例， 通过get方法获取
	 */	
	private final Map<FullTextData, Directory> indexDirectoryMap = new HashMap<FullTextData,Directory>();
	private final Map<FullTextData, IndexReader> indexReaderMap =  new HashMap<FullTextData,IndexReader>();
	private final Map<FullTextData, IndexWriter> indexWriterMap = new HashMap<FullTextData,IndexWriter>();
	private final Map<FullTextData, IndexSearcher> indexSearcherMap =  new HashMap<FullTextData,IndexSearcher>();
	private final Map<FullTextData, ReadWriteLock> IndexSearcherReadWriterLockMap= new HashMap<FullTextData, ReadWriteLock>();
	//private final ConcurrentHashMap<FullTextData, Analyzer> analyzerMap = new ConcurrentHashMap<FullTextData, Analyzer>();
	
	/******************
	 * 初始化全文检索
	 * @param _indexDirPaths
	 */
	public final synchronized void init(String baseDirName)
	{
		baseDirName = baseDirName.trim();
		baseDir = new File(baseDirName);
		if(!(baseDir.exists() && baseDir.isDirectory())){
			baseDir.mkdirs();
		}	
		//创建所有子目录
		final boolean isEndWithSlash = baseDirName.endsWith("/");
		indexDirPaths = new HashMap<FullTextData, String>();
		for(final FullTextData ft : FullTextData.values()){
			String subDir = null;
			if(isEndWithSlash){
				subDir = baseDirName + ft.dirName;
			}else{
				subDir = baseDirName + "/" + ft.dirName;
			}
			indexDirPaths.put(ft, subDir);
			IOUtil.makeSureDirectoryExists(new File(subDir));
		}
	}

	/*******FilteredQuery
	 * 获取Directory
	 */
	private final Analyzer getAnalyzer(final FullTextData type){
		/*Analyzer indexAnalyzer =  analyzerMap.get(type);
		if(indexAnalyzer == null){
			if(type == FullTextData.Shoe){
				//使用一元分词索引+分词查询，结果是尽可能多地返回数据，需要评估
				indexAnalyzer = new StandardAnalyzer(LucenceVersion);
			}if(type == FullTextData.Photo){
				//使用一元分词索引+分词查询，结果是尽可能多地返回数据，需要评估
				indexAnalyzer = new CJKAnalyzer(LucenceVersion);
			}else{
				//使用一元分词索引+分词查询，结果是尽可能多地返回数据，需要评估
				indexAnalyzer = new StandardAnalyzer(LucenceVersion);
                //二元分词+分词查询，能解决较长搜索词匹配不到的问题，但不能解决单个字的匹配问题，单个字的匹配应该需要修改相应词库
				//indexAnalyzer = new CJKAnalyzer(LucenceVersion); //new SmartChineseAnalyzer(LucenceVersion);
				//new PaodingAnalyzer();new CJKAnalyzer(LucenceVersion); //
				// CJKAnalyzer //二元分词, 升级版IK_CAnalyzer
				// StandardAnalyzer //一元分词				
			}
			analyzerMap.put(type, indexAnalyzer);
		}
		return indexAnalyzer;*/
		return new WhitespaceAnalyzer(LucenceVersion);
	}
	/*******FilteredQuery
	 * 获取Directory
	 */
	private final synchronized Directory getIndexDirectory(final FullTextData type){
		Directory indexDirectory = indexDirectoryMap.get(type);
		if(indexDirectory != null){
			return indexDirectory;
		}		
		final String fullTextDirectory = indexDirPaths.get(type);	
		if(fullTextDirectory == null){
			logger.fatal("System critical error: fail to create ft index of " + type.name() + ", please config directory!");
			throw new ServerUnknownException("System critical error: fail to create ft index of " + type.name() 
					+ ", please config directory!");
		}
		File dir = new File(fullTextDirectory);
		if(!dir.exists()){
			dir.mkdirs();
		}
		try {
			indexDirectory = FSDirectory.open(dir);
			indexDirectoryMap.put(type, indexDirectory);
			return indexDirectory;
		} catch (IOException e) {
			logger.fatal("Fail to getIndexDirectory of "+type.name()+", ex=" + e.getMessage(), e);
			throw new ServerUnknownException("Fail to getIndexDirectory of "+type.name()+", ex=" + e.getMessage(), e);
		}		
	}
	
	/****************
	 * 创建新的IndexWriter
	 * @throws ServerUnknownException
	 */
	private final synchronized IndexWriter getIndexWriter(final FullTextData type) throws ServerUnknownException{
		IndexWriter indexWriter = indexWriterMap.get(type);
		if(indexWriter != null){
			return indexWriter;
		}
		try {//创建index
			IndexWriterConfig iwc = new IndexWriterConfig(LucenceVersion, this.getAnalyzer(type));
			iwc.setMaxBufferedDocs(IndexWriterConfig.DISABLE_AUTO_FLUSH);
			iwc.setRAMBufferSizeMB(256);//256m buffer
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);			
			indexWriter = new IndexWriter(getIndexDirectory(type), iwc);  		
			indexWriterMap.put(type, indexWriter);
			return indexWriter;
		} catch (Exception e) {
			logger.fatal("Fail to getIndexWriter of "+type.name()+", ex=" + e.getMessage(), e);
			throw new ServerUnknownException("Fail to getIndexWriter of "+type.name()+", ex=" + e.getMessage(), e);
		}  
	} 

	/************
	 * 创建新的IndexSearcher
	 * @return
	 */
	public final void notifyFulltextHasUpdated(final FullTextData type){		
		IndexReader indexReader = null;
		synchronized(this){
			this.indexSearcherMap.remove(type);
			indexReader = this.indexReaderMap.remove(type);
		}
		if(indexReader == null){
			return;
		}
		final Lock writeLock = this.getIndexSearcherReadWriteLock(type).writeLock();
		writeLock.lock();
		try{
			indexReader.close();
		}catch (IOException e) {
			logger.fatal("Fail to closeIndexSearcher of "+type.name()+", ex=" + e.getMessage(), e);
		}finally{
			writeLock.unlock();
		}
	}
	
	
	/************
	 * 创建新的IndexSearcher
	 * @return
	 */
	private final synchronized IndexSearcher getIndexSearcher(FullTextData type){
		IndexSearcher indexSearcher = this.indexSearcherMap.get(type);
		if(indexSearcher != null){
			return indexSearcher;
		}
		try{
			IndexReader indexReader = this.indexReaderMap.get(type);
			if(indexReader == null){
				indexReader = DirectoryReader.open(this.getIndexDirectory(type));
				this.indexReaderMap.put(type, indexReader);
			}
			indexSearcher = new IndexSearcher(indexReader);
			this.indexSearcherMap.put(type, indexSearcher);
			return indexSearcher;
		} catch (Exception e) {
			logger.fatal("Fail to getIndexSearcher of "+type.name()+", ex=" + e.getMessage(), e);
			throw new ServerUnknownException("Fail to getIndexSearcher of "+type.name()+", ex=" + e.getMessage(), e);
		}
	}

	
	/************
	 * 读写锁
	 * @param type
	 * @return
	 */
	private final synchronized ReadWriteLock getIndexSearcherReadWriteLock(FullTextData type){
		ReadWriteLock lock = IndexSearcherReadWriterLockMap.get(type);
		if(lock == null){
			lock = new ReentrantReadWriteLock();
			IndexSearcherReadWriterLockMap.put(type, lock);
		}
		return lock;		
	}
	
	/************************************真正的全文检索底层更新函数***************************************************
	 * 删除索引数据
	 * @param type
	 * @param idsToDelete
	 * @param docsToAdd
	 * @throws Exception
	 */
	public final void deleteDocuments(final FullTextData type, final Long... idsToDelete)  throws Exception
	{
		if(ArrayUtils.isEmpty(idsToDelete)){
			return;
		}
		final IndexWriter writer = this.getIndexWriter(type);
		synchronized(writer){
			try{
				//delete all documents				
				final Term[] terms = new Term[idsToDelete.length];
				for(int index=0; index<idsToDelete.length; index++){
					terms[index] = new Term(FullTextField.Id.name, String.valueOf(idsToDelete[index]));
				}
				writer.deleteDocuments(terms);
			}finally{
				writer.commit();
			}
		}
	}
	
	/**************
	 * 新增文档到索引库
	 * @param type
	 * @param docsToAdd
	 * @throws Exception
	 */
	public final void addDocuments(final FullTextData type, final List<Document> docs)  throws Exception
	{
		if(CollectionUtils.isEmpty(docs)){
			return;
		}
		final IndexWriter writer = this.getIndexWriter(type);
		synchronized(writer){
			try{
				writer.addDocuments(docs, this.getAnalyzer(type));
			}finally{
				writer.commit();
			}
		}
	}
	
	/*************
	 * 更新一个 文档
	 * @param type
	 * @param id
	 * @param doc
	 * @throws Exception
	 */
	public final void updateDocuments(final FullTextData type, final List<Long> idsToDelete, final List<Document> docs) throws Exception{
		//构造删除的哦terms
		Term[] terms = null;
		if(CollectionUtils.isNotEmpty(idsToDelete)){
			terms = new Term[idsToDelete.size()];
			int index  = -1;
			for(final Long id: idsToDelete){
				terms[++index] = new Term(FullTextField.Id.name, String.valueOf(id));
			}
		}	
		//更新索引库
		final IndexWriter writer = this.getIndexWriter(type);
		synchronized(writer){
			try{
				//delete all documents	
				if(ArrayUtils.isNotEmpty(terms)){
					writer.deleteDocuments(terms);
				}				
				if(CollectionUtils.isNotEmpty(docs)){
					writer.addDocuments(docs, this.getAnalyzer(type));
				}
			}finally{
				writer.commit();
			}
		}
	}
	
	public final void updateDocumentWithStringIds(final FullTextData type, final List<String> idsToDelete, final List<Document> docs) throws Exception {
		//构造删除的哦terms
		Term[] terms = null;
		if(CollectionUtils.isNotEmpty(idsToDelete)){
			terms = new Term[idsToDelete.size()];
			int index  = -1;
			for(final String id: idsToDelete){
				terms[++index] = new Term(FullTextField.Id.name, id);
			}
		}	
		//更新索引库
		final IndexWriter writer = this.getIndexWriter(type);
		synchronized(writer){
			try{
				//delete all documents	
				if(ArrayUtils.isNotEmpty(terms)){
					writer.deleteDocuments(terms);
				}				
				if(CollectionUtils.isNotEmpty(docs)){
					writer.addDocuments(docs, this.getAnalyzer(type));
				}
			}finally{
				writer.commit();
			}
		}
	}
	/*******************
	 * 删除大批量的文档
	 * @param type
	 * @param queryCreator
	 * @throws Exception
	 */
	public final void deleteDocumentsByTask(final FullTextData type, final QueryCreator queryCreator) throws Exception{
		final Query query = queryCreator.getQuery(this.getAnalyzer(type));
		if(query == null){
			return;
		}
		//更新索引库
		final IndexWriter writer = this.getIndexWriter(type);
		synchronized(writer){
			try{	
				writer.deleteDocuments(query);
			}finally{
				writer.commit();
			}
		}
	}
	
	/********************
	 * 公用的底层搜索方法, 过滤器和sort都可以为空
	 * @param searcher
	 * @param query
	 * @param page
	 * @param hitsPerPage:每页数量
	 * @return
	 * @throws Exception
	 */
	public final PageData<Long> searchDocuments(final FullTextData type, final QueryCreator queryCreator,
			final int page, final int hitsPerPage) throws Exception
	{
		final Query query = queryCreator.getQuery(this.getAnalyzer(type));
		final Filter filter = queryCreator.getFilter();
		final Sort sort = queryCreator.getSort();
		if(query == null){
			return new PageData<Long>();
		}
		final Lock readLock = this.getIndexSearcherReadWriteLock(type).readLock();
		readLock.lock();
		try{
			final IndexSearcher searcher = this.getIndexSearcher(type);				
			TopDocs results = null;
			if(sort == null && filter == null){
				results = searcher.search(query, FullTextSearchConstant.SearchResultMaxCount);
			}else{
				if(sort == null){
					results = searcher.search(query, filter, FullTextSearchConstant.SearchResultMaxCount, sort);
				}else if(filter == null){
					results = searcher.search(query, FullTextSearchConstant.SearchResultMaxCount, sort);
				}else{
					results = searcher.search(query, filter, FullTextSearchConstant.SearchResultMaxCount, sort);
				}
			}
	        final ScoreDoc[] hits = results.scoreDocs;  
	        final List<Long> list = new ArrayList<Long>(hitsPerPage);
	        final Paginator paginator = new Paginator(hitsPerPage, hits.length);
			paginator.setPage(page);
	        if(ArrayUtils.isNotEmpty(hits) && hits.length > ((page-1) * hitsPerPage))
	        {
				for(int i=paginator.getBeginIndex()-1 ; i< paginator.getEndIndex(); i++){
					list.add(IdUtil.convertTolong(searcher.doc(hits[i].doc).get(FullTextField.Id.name), 0));
				}
	        }
			return new PageData<Long>(list, paginator);
		}finally{
			readLock.unlock();
		}
	}
	
	/**************
	 * 记录搜索关键字
	 * @param keyword
	 */
	public static final void logKeywords(final String keyword){
//		if(StringUtils.isBlank(keyword)){
//			return;
//		}
		keywordLogger.fatal(keyword);
	}
	
	public static final void logKeywords(final String prefix , final String keyword){
//		if(StringUtils.isBlank(keyword)){
//			return;
//		}
		keywordLogger.fatal(prefix + ":" + keyword);
	}
	
	
	 public static void main(String[] args) {  
	        try {  
	            // 要处理的文本  
	            // "lucene分析器使用分词器和过滤器构成一个“管道”，文本在流经这个管道后成为可以进入索引的最小单位，因此，一个标准的分析器有两个部分组成，一个是分词器tokenizer,它用于将文本按照规则切分为一个个可以进入索引的最小单位。另外一个是TokenFilter，它主要作用是对切出来的词进行进一步的处理（如去掉敏感词、英文大小写转换、单复数处理）等。lucene中的Tokenstram方法首先创建一个tokenizer对象处理Reader对象中的流式文本，然后利用TokenFilter对输出流进行过滤处理";  
	            String text = "1 2 3 4 5 6 7 7 1321 2 空格分词器 H123 AZhe Apache4.4. HB123 helloworld";  
	            
	            // 空格分词器(以空格作为切词标准，不对语汇单元进行其他规范化处理)  
	            Analyzer wsa = new WhitespaceAnalyzer(LucenceVersion); 
	  
	            TokenStream ts = wsa.tokenStream("field", LuceneAnalyzerUtil.instance.splitContents2JoinWhiteSpace(text));  
	            CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);  
	  
	            ts.reset();  
	            while (ts.incrementToken()) {  
	                System.out.println(ch.toString());  
	            }  
	            ts.end();  
	            ts.close();  
	        } catch (Exception ex) {  
	            ex.printStackTrace();  
	        }  
	  
	    }  
}
