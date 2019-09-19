/**
 * 
 */
package com.sas.core.util.keywordsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sas.core.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author zhullm
 * 和关键字相关， 只需在添加和设置关键字之后，调用buildAlgorithm，之后可以无数次多线程调用search方法
 */
public final class SearchAlgorithm{
	
	protected static final Logger logger = Logger.getLogger(SearchAlgorithm.class);
		
	private NodeState startState;
	private Map<String, Integer> keywords = new HashMap<String,Integer>();
	private boolean needSearchAllText = true;  //只需要发现脏数据即返回，还是搜索全部text
	
	public SearchAlgorithm(final boolean needSearchAllTest) {
		this. startState = new NodeState(null);
        this.startState.nodeFailure = startState;
        this.needSearchAllText = needSearchAllTest;
    }

	/****
	 * release resource
	 */
    public void close() {
    	startState = null;
    	keywords = new HashMap<String,Integer>();
    }

    /****
     * add key words, please call buildAlgorithm after finished adding words
     * @param keyword
     */
    public void addKeyword(String keyword) {
    	if(StringUtils.isNotBlank(keyword)){
    		keywords.put(keyword, 0);
    	}
    }
    
    public void setKeywords(List<String> words){
    	if(CollectionUtils.isNotEmpty(words)){
    		this.setKeywords(words.toArray(new String[words.size()]));
    	}
    }
    
    final public void setKeywords(String[] words){
    	final Map<String, Integer> tmpKeywords = new HashMap<String,Integer>();
    	if(words != null){
    		for(final String word: words){
    			if(StringUtils.isNotBlank(word)){
    				tmpKeywords.put(word, 0);
    			}
    		}
    	}  
    	keywords = tmpKeywords;
    }
    
    public void deleteKeyword(String keyword){
    	keywords.remove(keyword);
    }
    
    /***
     * build the algorithm after add/deleting words
     */    
    public void buildAlgorithm() {
    	startState = new NodeState(null);
        startState.nodeFailure = startState;
        // add all keywords
        for (String key : keywords.keySet()) {
    	   processSuccessPath(key);
        }      
        processFailurePath();
    }

    /****
     * process normal success and failure path
     * @param keyword
     */
    private void processSuccessPath(String keyword){
    	NodeState curState = startState;
        for(int i = 0; i < keyword.length(); i++ ) {
            curState = curState.addChildren(keyword.charAt(i));
        }
        curState.addKeyWord(keyword);
    }
    
    private void processFailurePath(){      
    	LinkedList<NodeState> queue = new LinkedList<NodeState>();
    	//first layer, the failure node is the root
        for (final char c : startState.nodeChildren.keySet()) {
        	NodeState child = startState.nodeChildren.get(c);
        	queue.add(child);
        	child.nodeFailure = startState;
		}        
        //process other nodes
        while(!queue.isEmpty() ) 
        {
        	NodeState currentState = queue.getFirst();        	
        	queue.remove();
        	NodeState failureState = null;
        	//add children
            for (char c : currentState.nodeChildren.keySet()) 
            {
            	NodeState currentChild = currentState.nodeChildren.get(c);
            	queue.add(currentChild);
            	failureState = currentState.nodeFailure;
            	while(failureState.nodeChildren.containsKey(c)== false) {
            		failureState = failureState.nodeFailure;
                    if(failureState == startState ) {
                    	break;
                    }
                }
            	if(failureState.nodeChildren.containsKey(c)) {
            		NodeState childFailureNode = failureState.nodeChildren.get(c);
            		currentChild.nodeFailure = childFailureNode;
            		List<String> failureNodeWords = childFailureNode.getAllKeyWords();
            		for (String word: failureNodeWords) {
            			currentChild.addKeyWord(word);
                      }
            	} else {
            		currentChild.nodeFailure = startState;
            	}
            } 
        }
    }
    
    /*****
     * process the word seach
     * @param text
     * @param outputCallback
     */
    public List<String> SearchKeyWords(String text) 
    {
    	if(StringUtils.isBlank(text)){
    		return new ArrayList<String>(0);
    	}
    	final List<String> result = new LinkedList<String>();
    	NodeState curState = startState;
        for(int i = 0; i < text.length(); ++i ) 
        {
        	char ch = text.charAt(i);
        	while(curState != null && curState.nodeChildren != null && curState.nodeChildren.containsKey(ch)== false) {
                  if(curState.nodeFailure != startState ) {
                	  if (curState == curState.nodeFailure){
                		  logger.error("searchAlgorithm Failure; a self-loop exists!");
                		  break;
                	  }
                      curState = curState.nodeFailure;
                  } else {
                      curState = startState;
                      break;
                  }
              }        	
        	if(curState != null && curState.nodeChildren != null && curState.nodeChildren.containsKey(ch)){
                curState = curState.nodeChildren.get(ch);
                if(curState.containKeywords()) {
                	result.add(curState.getMatchedKeyWord(text, i));
                	if(!this.needSearchAllText){
                		return result;
                	}
                }
            }
        }
        return result;
    } 
}
