/**
 * 
 */
package com.sas.core.util.keywordsearch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author zhulm
 *
 */
public final class NodeState 
{
   public Map<Character,NodeState> nodeChildren = new HashMap<Character,NodeState>();
   
   public NodeState nodeFailure;
   private NodeState nodeParent;
   
   private List<String> nodeWords = new LinkedList<String>();   
   private boolean containKeywords = false;
   
   public NodeState(NodeState parent) {
       this.nodeParent = parent;
       this.nodeFailure = null;
   }
   
   /*****
    * add the children into this node
    * @param c
    * @return
    */
   NodeState addChildren(char c) 
   {    
	   //not in the goto table
	   if (!nodeChildren.containsKey(c)) {
		   NodeState newState = new NodeState(this);
		   nodeChildren.put(c, newState);
		   return newState;
		} else {
			return nodeChildren.get(c);
		}
   }

   /*****
    * check this node contains keywords
    * @return
    */
   public boolean containKeywords(){
	   return containKeywords;
   }   
   
   /****
    * add key words into this node
    * @param word
    */
   public void addKeyWord(String word){
	   if(StringUtils.isNotBlank(word)){
		   nodeWords.add(word);
		   containKeywords = true;
	   }
   }
   
   /****
    * get the keyword: all and matched
    * @return
    */
   public List<String> getAllKeyWords(){
	   return this.nodeWords;
   }   
   public String getMatchedKeyWord(String text, int lastMatchedIndex){	   
	   for(final String keyword : nodeWords){
		   int len = keyword.length();
		   final String subStr = text.substring(lastMatchedIndex-len+1, lastMatchedIndex+1);
		   if(subStr.equals(keyword)){
			   return keyword;
		   }
	   }
	   return null;
   }

   /****
    * get the parent node
    * @return
    */
   public NodeState getNodeParent() {
		return nodeParent;
   }
   
   
}
