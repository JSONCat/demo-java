/**
 * 
 */
package com.sas.core.util;

import java.math.BigDecimal;

/**
 * 计算相关的util
 * @author zhuliming
 *
 */
public class MathUtil {

	/************
	 * 取最大值
	 * @param values
	 * @return
	 */
	public static final int max(final int... values){
		int max = values[0];
		for(int i=1; i<values.length; i++){
			if(max < values[i]){
				max = values[i];
			}
		}
		return max;
	}
	
	public static final long maxLong(final long... values){
		long max = values[0];
		for(int i=1; i<values.length; i++){
			if(max < values[i]){
				max = values[i];
			}
		}
		return max;
	}
	
	/**************
	 * 取最小值
	 * @param values
	 * @return
	 */
	public static final long minLong(final long... values){
		long min = values[0];
		for(int i=1; i<values.length; i++){
			if(min > values[i]){
				min = values[i];
			}
		}
		return min;
		
	}
	
	public static final int minInt(final int... values){
		int min = values[0];
		for(int i=1; i<values.length; i++){
			if(min > values[i]){
				min = values[i];
			}
		}
		return min;
		
	}
	
	public static final double minDouble(final double... values){
		double min = values[0];
		for(int i=1; i<values.length; i++){
			if(min > values[i]){
				min = values[i];
			}
		}
		return min;		
	}
	
	public static final BigDecimal min(final BigDecimal... values){
		BigDecimal min = values[0];
		for(int i=1; i<values.length; i++){
			if(min.doubleValue() > values[i].doubleValue()){
				min = values[i];
			}
		}
		return min;		
	}
	
	public static final boolean isWithinLongRange(final long value, final long min, final long max){
		return value >= min && value <= max;
	}
	
	public static final boolean isWithinIntRange(final int value, final int min, final int max){
		return value >= min && value <= max;
	}
	
	/***********
	 * 差值是否在范围内
	 * @param v1
	 * @param v2
	 * @param balance
	 * @return
	 */
	public static final boolean isBalanceWithinRange(final long v1, final long v2, final long balance){
		if(v1 > v2){
			return (v1 - v2) <= balance;
		}else{
			return (v2 - v1) <= balance;
		}
	}
	
	public static void main(String[] args){
		   double i=2, j=2.1, k=2.5, m=2.9;
		   System.out.println("舍掉小数取整:Math.floor(2)=" + (int)Math.floor(i));
		   System.out.println("舍掉小数取整:Math.floor(2.1)=" + (int)Math.floor(j));
		   System.out.println("舍掉小数取整:Math.floor(2.5)=" + (int)Math.floor(k));
		   System.out.println("舍掉小数取整:Math.floor(2.9)=" + (int)Math.floor(m));
		                                       
		   /* 这段被注释的代码不能正确的实现四舍五入取整
		   System.out.println("四舍五入取整:Math.rint(2)=" + (int)Math.rint(i));
		   System.out.println("四舍五入取整:Math.rint(2.1)=" + (int)Math.rint(j));
		   System.out.println("四舍五入取整:Math.rint(2.5)=" + (int)Math.rint(k));
		   System.out.println("四舍五入取整:Math.rint(2.9)=" + (int)Math.rint(m));
		  
		   System.out.println("四舍五入取整:(2)=" + new DecimalFormat("0").format(i));
		   System.out.println("四舍五入取整:(2.1)=" + new DecimalFormat("0").format(i));
		   System.out.println("四舍五入取整:(2.5)=" + new DecimalFormat("0").format(i));
		   System.out.println("四舍五入取整:(2.9)=" + new DecimalFormat("0").format(i));
		   */
		  
		   System.out.println("四舍五入取整:(2)=" + new BigDecimal("2").setScale(0, BigDecimal.ROUND_HALF_UP));
		   System.out.println("四舍五入取整:(2.1)=" + new BigDecimal("2.1").setScale(0, BigDecimal.ROUND_HALF_UP));
		   System.out.println("四舍五入取整qq:(2.5)=" + new BigDecimal("2.5").setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
		   System.out.println("四舍五入取整:(2.9)=" + new BigDecimal("2.9").setScale(0, BigDecimal.ROUND_HALF_UP));

		   System.out.println("凑整:Math.ceil(2)=" + (int)Math.ceil(i));
		   System.out.println("凑整:Math.ceil(2.1)=" + (int)Math.ceil(j));
		   System.out.println("凑整:Math.ceil(2.5)=" + (int)Math.ceil(k));
		   System.out.println("凑整:Math.ceil(2.9)=" + (int)Math.ceil(m));

		   System.out.println("舍掉小数取整:Math.floor(-2)=" + (int)Math.floor(-i));
		   System.out.println("舍掉小数取整:Math.floor(-2.1)=" + (int)Math.floor(-j));
		   System.out.println("舍掉小数取整:Math.floor(-2.5)=" + (int)Math.floor(-k));
		   System.out.println("舍掉小数取整:Math.floor(-2.9)=" + (int)Math.floor(-m));
		  
		   System.out.println("四舍五入取整:(-2)=" + new BigDecimal("-2").setScale(0, BigDecimal.ROUND_HALF_UP));
		   System.out.println("四舍五入取整:(-2.1)=" + new BigDecimal("-2.1").setScale(0, BigDecimal.ROUND_HALF_UP));
		   System.out.println("四舍五入取整:(-2.5)=" + new BigDecimal("-2.5").setScale(0, BigDecimal.ROUND_HALF_UP));
		   System.out.println("四舍五入取整:(-2.9)=" + new BigDecimal("-2.9").setScale(0, BigDecimal.ROUND_HALF_UP));

		   System.out.println("凑整:Math.ceil(-2)=" + (int)Math.ceil(-i));
		   System.out.println("凑整:Math.ceil(-2.1)=" + (int)Math.ceil(-j));
		   System.out.println("凑整:Math.ceil(-2.5)=" + (int)Math.ceil(-k));
		   System.out.println("凑整:Math.ceil(-2.9)=" + (int)Math.ceil(-m));
		   } 
}
