package com.sas.core.util.wxpay;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * User: rizenguo Date: 2014/11/1 Time: 14:06
 */
public class XMLParser {

	public static Map<String, String> getMapFromXML(String xmlString) throws ParserConfigurationException, IOException, SAXException {
		// 这里用Dom的方式解析回包的最主要目的是防止API新增回包字段
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final InputStream is = WXUtil.getStringStream(xmlString);
		final Document document = builder.parse(is);
		// 获取到document里面的全部结点
		NodeList allNodes = document.getFirstChild().getChildNodes();
		Node node = null;
		final Map<String, String> map = new HashMap<String, String>();
		int i = 0;
		while (i < allNodes.getLength()) {
			node = allNodes.item(i);
			if (node instanceof Element) {
				map.put(node.getNodeName(), node.getTextContent());
			}
			i++;
		}
		is.close();
		return map;
	}
}