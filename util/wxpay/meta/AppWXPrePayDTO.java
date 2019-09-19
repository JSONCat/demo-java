package com.sas.core.util.wxpay.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class AppWXPrePayDTO {

	private final String prepayId;
	
	private final Map<String, String> params;

	public AppWXPrePayDTO(String prepayId, Map<String, String> params) {
		super();
		this.prepayId = prepayId;
		this.params = params;
	}

	public String getPrepayId() {
		return prepayId;
	}

	public Map<String, String> getParams() {
		return params;
	}
	
	public final String getParamURL()
	{
    	final List<String> list = new ArrayList<String>(params.size());
        for(Map.Entry<String,String> entry: params.entrySet()){
            if(StringUtils.isNotBlank(entry.getValue())){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        final int size = list.size();
        final String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        final StringBuilder sb = new StringBuilder("");
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        return sb.toString();
	}
	
}
