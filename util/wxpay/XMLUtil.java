package com.sas.core.util.wxpay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.util.IOUtil;

public class XMLUtil {
	
	/**
	 * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
	 * 
	 * @param strxml
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static final Map<String, String> doXMLParse(String strxml) throws JDOMException, IOException
	{
		strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
		if (StringUtils.isBlank(strxml)) {
			return null;
		}
		final Map<String, String> map = new HashMap<String, String>();
		final InputStream in = new ByteArrayInputStream(strxml.getBytes(Encoding.UTF8.type));
		final SAXBuilder builder = new SAXBuilder();
		final Document doc = builder.build(in);
		final Element root = doc.getRootElement();
		final List<Element> list = root.getChildren();
		for (final Element e : list) 
		{
			String v = "";
			final List<Element> children = e.getChildren();
			if (children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = XMLUtil.getChildrenText(children);
			}
			map.put(e.getName(), v);
		}
		// 关闭流
		IOUtil.closeStreamWithoutException(in, null);
		return map;
	}

	/**
	 * 获取子结点的xml
	 * @param children
	 * @return String
	 */
	public static String getChildrenText(final List<Element> children) 
	{
		final StringBuilder sb = new StringBuilder();
		if (!children.isEmpty()) {
			for (final Element e : children) 
			{
				final String name = e.getName();
				final String value = e.getTextNormalize();
				final List<Element> list = e.getChildren();
				sb.append("<" + name + ">");
				if (!list.isEmpty()) {
					sb.append(XMLUtil.getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}
		return sb.toString();
	}
}