/**
 * @Title: ExcelUtil.java
 * @Package com.sas.core.util
 * @author yunshang_734@163.com
 * @date Jan 15, 2015 5:04:16 PM
 * @version V1.0
 */
package com.sas.core.util;

import com.sas.core.constant.ActivityScoreConstant.SectionScoreType;
import com.sas.core.constant.CommonConstant.BinaryState;
import com.sas.core.constant.SasConstant.SasSwitch;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.constant.UserConstant.IdentityCardType;
import com.sas.core.constant.UserConstant.SexType;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.SasImportGood;
import com.sas.core.dto.UserDetail;
import com.sas.core.meta.*;
import com.sas.core.util.meta.SasActivityScoreUtil;
import com.sas.core.util.meta.UserUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName: ExcelUtil
 * @author yunshang_734@163.com
 * @date Jan 15, 2015 5:04:16 PM
 */
public class ExcelUtil {

	private static final Logger logger = Logger.getLogger(ExcelUtil.class);

	public final static int DefaultCellStyleID = 0;
	private final static int TitleCellStyleID = 1;
	private final static int SubTitleCellStyleID = 2;
	private final static int TableHeaderCellStyleID = 3;
	private final static int TableHeaderCellBGStyleID = 4;
	private final static int NOMAL_YAHEI_FONT11 = 5;
	private final static int BOLD_YAHEI_FONT11 = 6;
	private final static int BOLD_YAHEI_FONT11WHITE_ORANAGE = 7;
	private final static int BOLD_YAHEI_FONT11WHITE_BLUE = 8;

	/**
	 * @Description 获取单元格格式
	 * @Date May 22, 2015
	 * @Time 8:08:26 PM
	 * @param workbook
	 * @param cellStyleID
	 * @return
	 */
	public final static HSSFCellStyle getHSSFCellStyle(final HSSFWorkbook workbook, final int cellStyleID) {
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setFontName("Msyh");
		if (cellStyleID == TitleCellStyleID) {
			font.setColor(HSSFFont.COLOR_NORMAL);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			font.setFontHeightInPoints((short) 16);
		} else if (cellStyleID == SubTitleCellStyleID) {
			font.setColor(HSSFFont.COLOR_NORMAL);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			font.setFontHeightInPoints((short) 12);
		} else if (cellStyleID == TableHeaderCellStyleID) {
			font.setColor(HSSFFont.COLOR_NORMAL);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			font.setFontHeightInPoints((short) 12);
		} else if (cellStyleID == TableHeaderCellBGStyleID) {
			font.setColor(HSSFFont.COLOR_NORMAL);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			font.setFontHeightInPoints((short) 16);
			cellStyle.setFillBackgroundColor(HSSFColor.BLUE.index);
			cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		} else if (cellStyleID == NOMAL_YAHEI_FONT11) {
			font.setColor(HSSFFont.COLOR_NORMAL);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			font.setFontHeightInPoints((short) 12);
		} else if (cellStyleID == BOLD_YAHEI_FONT11) {
			font.setColor(HSSFFont.COLOR_NORMAL);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			font.setFontHeightInPoints((short) 12);
		} else if (cellStyleID == BOLD_YAHEI_FONT11WHITE_ORANAGE) {
			font.setColor(HSSFColor.WHITE.index);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			//自定义颜色
			HSSFPalette palette = workbook.getCustomPalette();
			palette.setColorAtIndex((short)8, (byte) (198), (byte) (89), (byte) (17));
			cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			cellStyle.setFillForegroundColor((short)8);
			font.setFontHeightInPoints((short) 12);
		} else if (cellStyleID == BOLD_YAHEI_FONT11WHITE_BLUE) {
			font.setColor(HSSFColor.WHITE.index);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			//自定义颜色
			HSSFPalette palette = workbook.getCustomPalette();
			palette.setColorAtIndex((short)10, (byte) (48), (byte) (84), (byte) (150));
			cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			cellStyle.setFillForegroundColor((short)10);
			font.setFontHeightInPoints((short) 12);
		} else {
			font.setColor(HSSFFont.COLOR_NORMAL);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			font.setFontHeightInPoints((short) 12);
		}
		cellStyle.setFont(font);
		return cellStyle;
	}
	
	/**
	 * @Title: parseUserFromExcel
	 * @Description: 从Excel里面批量导入用户
	 * @param workbook
	 * @return
	 * @throws
	 */
	public static List<UserDetail> parseUserFromXls(final InputStream in, final boolean is2007XlsxFile) throws IOException{
		Workbook workbook = null;	
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		Row row = null;
		final Sheet sheet = workbook.getSheetAt(0);
		List<UserDetail> userDetailList = new LinkedList<UserDetail>();
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return userDetailList;
		}
		for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
			row = sheet.getRow(r);
			if (isBlankLine(row)) {
				continue;
			}
			final Cell cell0 = row.getCell(0);
			final Cell cell1 = row.getCell(1);
			final Cell cell2 = row.getCell(2);
			final Cell cell3 = row.getCell(3);
			final Cell cell4 = row.getCell(4);
			final Cell cell5 = row.getCell(5);
			if (cell0 == null || cell1 == null || cell2 == null || cell3 == null || cell4 == null) {
				return userDetailList;
			}
			cell0.setCellType(Cell.CELL_TYPE_STRING);
			cell1.setCellType(Cell.CELL_TYPE_STRING);
			cell2.setCellType(Cell.CELL_TYPE_STRING);
			cell3.setCellType(Cell.CELL_TYPE_STRING);
			cell4.setCellType(Cell.CELL_TYPE_STRING);
			if(cell5 != null){
				cell5.setCellType(Cell.CELL_TYPE_STRING);
			}
			String nickname = cell0.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
			String email = cell1.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
			String phone = cell2.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
			String trueName = cell3.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
			String identityCode = cell4.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "").replaceAll("x|ｘ|Ｘ", "X");
			final long intergal = cell5 == null ? -1L : IdUtil.convertTolong(cell5.getStringCellValue(), -1L);
			if ((StringUtils.isNotBlank(email) && UserUtil.isUserEmailVaild(email))
					|| (StringUtils.isNotBlank(phone) && UserUtil.isUserPhoneValid(phone))) 
			{
				final User user = new User((StringUtils.isNotBlank(nickname) && UserUtil.isUserNicknameVaild(nickname)) ? XSSUtil.filter(nickname, false) : "", 
						(StringUtils.isNotBlank(email) && UserUtil.isUserEmailVaild(email)) ? XSSUtil.filter(email, false)	: "", 
						(StringUtils.isNotBlank(phone) && UserUtil.isUserPhoneValid(phone)) ? XSSUtil.filter(phone, false) : "");
				final UserExt userExt = new UserExt(StringUtils.isNotBlank(trueName) ? XSSUtil.filter(trueName, false) : "", 
						(StringUtils.isNotBlank(identityCode) && ValidatorUtil.cardNoValidate(identityCode)) ? XSSUtil.filter(identityCode, false) : "");
				final UserDetail detail = new UserDetail(user, userExt,	new UserStatistic());
				if(intergal > 0){
					final SasUserStatistic sus = new SasUserStatistic();
					sus.setCreditPointBalance(intergal);
					detail.setSasUserStatistic(sus);
				}
				userDetailList.add(detail);
			}
		}
		return userDetailList;
	}

	/**
	 * @Title: parseProvidersFromXls
	 * @Description: 从Excel里面批量导入供应商
	 */
	public static final BinaryEntry<Set<Integer>, List<SasERPProvider>> parseProvidersFromXls(final long sasId, final InputStream in, final boolean is2007XlsxFile) throws IOException
	{
		final Set<Integer> ingoredRowSet = new HashSet<Integer>();
		Workbook workbook = null;	
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		Row row = null;
		final Sheet sheet = workbook.getSheetAt(0);
		final List<SasERPProvider> result = new LinkedList<SasERPProvider>();
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return new BinaryEntry<Set<Integer>, List<SasERPProvider>>(ingoredRowSet, result);
		}
		for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++)
		{
			row = sheet.getRow(r);
			if (isBlankLine(row) || r == sheet.getFirstRowNum()) {
				continue;
			}
			final Cell[] cells = new Cell[12];
			for(int i=0; i<12; i++){
				cells[i] = row.getCell(i);
				if(cells[i] != null){
					cells[i].setCellType(Cell.CELL_TYPE_STRING);	
				}
			}
			if (cells[0] == null && cells[2] == null && cells[3] == null) {
				return new BinaryEntry<Set<Integer>, List<SasERPProvider>>(ingoredRowSet, result);
			}
			//解析信息
			final String providerName = cells[0] == null ? "" : XSSUtil.filter(cells[0].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String telephone = cells[1] == null ? "" : XSSUtil.filter(cells[1].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String contactName = cells[2] == null ? "" : XSSUtil.filter(cells[2].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String contactPhone = cells[3] == null ? "" : UserUtil.processUserPhone(cells[3].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""));
			final String email = cells[4] == null ? "" : XSSUtil.filter(cells[4].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String fax = cells[5] == null ? "" : XSSUtil.filter(cells[5].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String qq = cells[6] == null ? "" : XSSUtil.filter(cells[6].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			String province = cells[7] == null ? "" : XSSUtil.filter(cells[7].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).replaceAll("省", "");
			String city = cells[8] == null ? "" : XSSUtil.filter(cells[8].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).replaceAll("市", "");
			String address = cells[9] == null ? "" : XSSUtil.filter(cells[9].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final double initArrears = cells[10] == null ? 0 : IdUtil.convertToDouble(cells[10].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg + "|(￥)+", ""), 0);
			final String remark = cells[11] == null ? "" : XSSUtil.filter(cells[11].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			//必填字段是否输入
			if (StringUtils.isNotBlank(providerName) && StringUtils.isNotBlank(contactName)
					&& ValidatorUtil.mobileValidate(contactPhone))
			{
				if(province.contains("台湾") || province.contains("香港") || province.contains("澳门")){
					address = city + address;
					city = province;
					province = "港澳台";
				}
				final SasERPProvider provider = new SasERPProvider(0, providerName, sasId, 0L,
						contactName, "", contactName,
						contactPhone, email, qq, province, city, address,
						remark, new BigDecimal(0), new BigDecimal(0),new BigDecimal(0),
						0, BinaryState.Yes.state, fax, telephone);
				provider.setInitArrears(initArrears < 0 ? new BigDecimal(0) : new BigDecimal(initArrears).setScale(2, BigDecimal.ROUND_HALF_UP));
				provider.setExcelRowIndex(r);
				result.add(provider);
			}else{
				ingoredRowSet.add(r);
			}
		}
		return new BinaryEntry<Set<Integer>, List<SasERPProvider>>(ingoredRowSet, result);
	}

	/**
	 * @Title: parseCustomersFromXls
	 * @Description: 从Excel里面批量导入客户
	 */
	public static BinaryEntry<Set<Integer>, List<SasERPCustomer>> parseCustomersFromXls(final long sasId,
			final InputStream in, final boolean is2007XlsxFile) throws IOException
	{
		final Set<Integer> ingoredRowSet = new HashSet<Integer>();
		Workbook workbook = null;	
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		Row row = null;
		final Sheet sheet = workbook.getSheetAt(0);
		final List<SasERPCustomer> result = new LinkedList<SasERPCustomer>();
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return new BinaryEntry<Set<Integer>, List<SasERPCustomer>>(ingoredRowSet, result);
		}
		for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++)
		{
			row = sheet.getRow(r);
			if (isBlankLine(row) || r == sheet.getFirstRowNum()) {
				continue;
			}
			final Cell[] cells = new Cell[12];
			for(int i=0; i<12; i++){
				cells[i] = row.getCell(i);
				if(cells[i] != null){
					cells[i].setCellType(Cell.CELL_TYPE_STRING);	
				}
			}
			if (cells[0] == null && cells[2] == null && cells[3] == null && cells[7] == null && cells[8] == null) {
				return new BinaryEntry<Set<Integer>, List<SasERPCustomer>>(ingoredRowSet, result);
			}		
			//解析信息
			final String customerName = cells[0] == null ? "" : XSSUtil.filter(cells[0].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String telephone = cells[1] == null ? "" : XSSUtil.filter(cells[1].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String contactName = cells[2] == null ? "" : XSSUtil.filter(cells[2].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String contactPhone = cells[3] == null ? "" : UserUtil.processUserPhone(cells[3].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""));
			final String email = cells[4] == null ? "" : XSSUtil.filter(cells[4].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String fax = cells[5] == null ? "" : XSSUtil.filter(cells[5].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final String qq = cells[6] == null ? "" : XSSUtil.filter(cells[6].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			String province = cells[7] == null ? "" : XSSUtil.filter(cells[7].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).replaceAll("省", "");
			String city = cells[8] == null ? "" : XSSUtil.filter(cells[8].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).replaceAll("市", "");
			String address = cells[9] == null ? "" : XSSUtil.filter(cells[9].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			final double initArrears = cells[10] == null ? 0 : IdUtil.convertToDouble(cells[10].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg + "|(￥)+", ""), 0);
			final String remark = cells[11] == null ? "" : XSSUtil.filter(cells[11].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			//必填字段是否输入
			if (StringUtils.isNotBlank(customerName) && StringUtils.isNotBlank(contactName)
					&& StringUtils.isNotBlank(province)
					&& StringUtils.isNotBlank(city)
					&& StringUtils.isNotBlank(address)
					&& ValidatorUtil.mobileValidate(contactPhone))
			{
				if(province.contains("台湾") || province.contains("香港") || province.contains("澳门")){
					address = city + address;
					city = province;
					province = "港澳台";
				}
				final SasERPCustomer customer = new SasERPCustomer(0, customerName, sasId, 0L,
						contactName, "", contactName,
						contactPhone, email, qq, province, city, address,
						remark, new BigDecimal(0), new BigDecimal(0),new BigDecimal(0),
						0, BinaryState.Yes, fax, telephone, BinaryState.No);
				customer.setInitArrears(initArrears < 0 ? new BigDecimal(0) : new BigDecimal(initArrears).setScale(2, BigDecimal.ROUND_HALF_UP));
				customer.setExcelRowIndex(r);
				result.add(customer);
			}else{
				ingoredRowSet.add(r);
			}
		}
		return new BinaryEntry<Set<Integer>, List<SasERPCustomer>>(ingoredRowSet, result);
	}
	
	/**
	 * @Title: isBlankLine
	 * @Description: 判断Excel某行是否为空行
	 * @param row
	 * @return
	 * @throws
	 */
	public static boolean isBlankLine(Row row) {
		if (row == null) {
			return true;
		}
		boolean isBlankLine = true;
		for (int j = 0; j < row.getLastCellNum(); j++) {
			Cell cell = row.getCell(j);
			if (cell == null) {
				row.createCell(j);
				cell = row.getCell(j);
			}
			row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
			if (row.getCell(j) != null && StringUtils.isNotBlank(row.getCell(j).getStringCellValue())) {
				isBlankLine = false;
				break;
			}
		}
		return isBlankLine;
	}

	/**
	 * @Description 导出时生成标题、副标题、表头
	 * @Date May 22, 2015
	 * @Time 8:08:57 PM
	 * @param workbook
	 * @param sheet
	 * @param title
	 * @param subTitleList
	 * @param tableHeaders
	 * @param rowIndex
	 * @return
	 */
	public final static int createTitlesAndHeadersWhileExport(final HSSFWorkbook workbook, final HSSFSheet sheet,
			final String title, final List<String> subTitleList, final String[] tableHeaders, int rowIndex) {
		sheet.setDefaultColumnWidth(30);
		// 获取Excel表行列
		sheet.createRow(rowIndex);
		HSSFRow row = sheet.getRow(rowIndex);
		// 设置标题
		sheet.createRow(rowIndex);
		row = sheet.getRow(rowIndex);
		row.setHeightInPoints((short) 20);
		setTitleCell(title, getHSSFCellStyle(workbook, TitleCellStyleID), row);
		rowIndex++;
		// 设置副标题
		if(CollectionUtils.isNotEmpty(subTitleList)){
			for(final String subTitle : subTitleList){
				if(StringUtils.isBlank(subTitle)){
					continue;
				}
				sheet.createRow(rowIndex);
				row = sheet.getRow(rowIndex);
				row.setHeightInPoints((short) 16);
				setTitleCell(subTitle, getHSSFCellStyle(workbook, SubTitleCellStyleID), row);
				rowIndex++;
			}			
		}
		rowIndex += 3;
//		// 设置副标题
//		sheet.createRow(rowIndex);
//		row = sheet.getRow(rowIndex);
//		row.setHeightInPoints((short) 16);
//		setSubTitle(subTitleList, getHSSFCellStyle(workbook, SubTitleCellStyleID), row);
//		rowIndex++;		
		// 设置空行
		sheet.createRow(rowIndex);
		row = sheet.getRow(rowIndex);
		row.setHeightInPoints((short) 16);
		rowIndex++;
		// 设置表头
		sheet.createRow(rowIndex);
		row = sheet.getRow(rowIndex);
		row.setHeightInPoints((short) 16);
		setRowByStringArray(tableHeaders, getHSSFCellStyle(workbook, TableHeaderCellStyleID), row);
		rowIndex++;
		return rowIndex;
	}
  
  /**
   *  在线表单标题格式
   * @param workbook
   * @param sheet
   * @param title
   * @param subTitleList
   * @param tableHeaders
   * @param rowIndex
   * @return
   */
	public final static int createQuestionaryDataExport(final HSSFWorkbook workbook, final HSSFSheet sheet,
			final List<String> titleList, final List<String> subTitleList, final List<String> tableHerders, int rowIndex) {
		//sheet.setDefaultColumnWidth(30);
		//sheet.autoSizeColumn()

		// 获取Excel表行列
		sheet.createRow(rowIndex);
		HSSFRow row = sheet.getRow(rowIndex);
		// 设置主标题
		sheet.createRow(rowIndex);
    row = sheet.getRow(rowIndex);
    row.setHeightInPoints(14.5f);
    
    final List<HSSFCellStyle> cellStyle = new ArrayList<HSSFCellStyle>();
    cellStyle.add(getHSSFCellStyle(workbook, BOLD_YAHEI_FONT11));
    cellStyle.add(getHSSFCellStyle(workbook, NOMAL_YAHEI_FONT11));
    setRowByStringListByStyle(titleList, cellStyle, row);
    rowIndex++;
    
		// 设置副标题
    sheet.createRow(rowIndex);
    row = sheet.getRow(rowIndex);
    row.setHeightInPoints(14.5f);
    setRowByStringListByStyle(subTitleList, cellStyle, row);
    rowIndex++;

		// 设置表头
		sheet.createRow(rowIndex);
    row = sheet.getRow(rowIndex);
    row.setHeightInPoints(14.5f);
    final List<HSSFCellStyle> headersCellStyle = new ArrayList<HSSFCellStyle>();
    headersCellStyle.add(getHSSFCellStyle(workbook, BOLD_YAHEI_FONT11WHITE_ORANAGE));
    headersCellStyle.add(getHSSFCellStyle(workbook, BOLD_YAHEI_FONT11WHITE_ORANAGE));
    for(int i = 0;i < (tableHerders.size()-2);i++){
      headersCellStyle.add(getHSSFCellStyle(workbook, BOLD_YAHEI_FONT11WHITE_BLUE));
    }
		setRowByStringListByStyle(tableHerders, headersCellStyle, row);
		rowIndex++;
		return rowIndex;
	}
	/************
	 * 自适应宽度(中文支持)
	 * @date 2018/9/20
	 * @param sheet
	 * @param size
	 * @param offset
	 * @return void
	 **/
	public final static void setSizeColumn(HSSFSheet sheet, int size, int offset) {
			for (int columnNum = 0; columnNum < size; columnNum++) {
					int columnWidth = sheet.getColumnWidth(columnNum) / 256;
					for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
							HSSFRow currentRow;
							//当前行未被使用过
							if (sheet.getRow(rowNum) == null) {
									currentRow = sheet.createRow(rowNum);
							} else {
									currentRow = sheet.getRow(rowNum);
							}

							if (currentRow.getCell(columnNum) != null) {
									HSSFCell currentCell = currentRow.getCell(columnNum);
									if (currentCell.getCellType() ==HSSFCell.CELL_TYPE_STRING) {
											int length = currentCell.getStringCellValue().getBytes().length;
											if (columnWidth < length) {
													columnWidth = length;
											}
									}
							}
					}
					//excel 最大单元格宽度255
					sheet.setColumnWidth(columnNum, (columnWidth>100?100:columnWidth) * 256 + offset);
			}
	}

	public final static void createSubTitlesExport(final HSSFWorkbook workbook, final HSSFSheet sheet,
                                                 final List<String> subTitleList,  int rowIndex) {
	  sheet.setDefaultColumnWidth(30);
		// 获取Excel表行列
		sheet.createRow(rowIndex);
		HSSFRow row = sheet.getRow(rowIndex);
		if(CollectionUtils.isNotEmpty(subTitleList)){
			for(final String subTitle : subTitleList){
				if(StringUtils.isBlank(subTitle)){
					continue;
				}
				sheet.createRow(rowIndex);
				row = sheet.getRow(rowIndex);
				row.setHeightInPoints((short) 16);
				setTitleCell(subTitle, getHSSFCellStyle(workbook, SubTitleCellStyleID), row);
				rowIndex++;
			}
		}
  }
	/**
	 * @Description 将报名人列表插入表格
	 * @Date May 22, 2015
	 * @Time 8:09:37 PM
	 * @param outputStream
	 * @param rowsList
	 * @param rowIndex
	 * @param workbook
	 * @param sheet
	 * @param cellStyle
	 * @return
	 * @throws IOException
	 */
	public final static int exportExcelWithOrderApplierDetailList(final OutputStream outputStream,
			final List<String[]> rowsList, int rowIndex, final HSSFSheet sheet,
			final HSSFCellStyle cellStyle) throws IOException {
		HSSFRow row = sheet.getRow(rowIndex);
		int rowAt = 0; 
		for (final String[] rowValues :  rowsList) {
			sheet.createRow(rowIndex + rowAt);
			row = sheet.getRow(rowIndex + rowAt);
			row.setHeightInPoints((short) 16);
			setRowByStringArray(rowValues, cellStyle, row);
			rowAt++;
		}
		return rowAt;
	}
	
	/************
	 * 插入在线表单用户提交数据(可设置行高)
	 * @date 2018/9/20
	 * @param outputStream
	 * @param rowsList
	 * @param rowIndex
	 * @param sheet
	 * @param cellStyle
	 * @return
	 * @throws IOException
	 **/
	public final static int exportQuastionaryDetailList(final OutputStream outputStream,
			final List<String[]> rowsList, int rowIndex,final float rowHeight, final HSSFSheet sheet,
			final HSSFCellStyle cellStyle) throws IOException {
		HSSFRow row = sheet.getRow(rowIndex);
		int rowAt = 0;
		for (final String[] rowValues :  rowsList) {
			sheet.createRow(rowIndex + rowAt);
			row = sheet.getRow(rowIndex + rowAt);
			row.setHeightInPoints(rowHeight);
			setRowByStringArray(rowValues, cellStyle, row);
			rowAt++;
		}
		return rowAt;
	}
	
	public final static int exportExcelWithOrderApplierDetailList(final OutputStream outputStream,
			final String[] oneRowData, int rowIndex, final HSSFSheet sheet,
			final HSSFCellStyle cellStyle, final short heightInPoints) throws IOException {
		HSSFRow row = sheet.getRow(rowIndex);
		sheet.createRow(rowIndex);
		row = sheet.getRow(rowIndex);
		row.setHeightInPoints(heightInPoints);
		setRowByStringArray(oneRowData, cellStyle, row);
		return 1;
	}

	/**
	 * @Title: setTitleCell
	 * @Description: 设置标题
	 * @param workbook
	 * @param title
	 * @param cell
	 * @throws
	 */
	public final static void setTitleCell(final String title, final HSSFCellStyle cellStyle, HSSFRow row) {
		row.createCell(0);
		HSSFCell cell = row.getCell(0);
		cell.setCellStyle(cellStyle);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(title);
	}

	/**
	 * @Title: setSubTitle
	 * @Description: 设置副标题
	 * @param workbook
	 * @param subTitleList
	 * @param row
	 * @throws
	 */
	public final static void setSubTitle(final List<String> subTitleList, final HSSFCellStyle cellStyle, HSSFRow row) {
		for (int index = 0; index < subTitleList.size(); index++) {
			row.createCell(index);
			HSSFCell cell = row.getCell(index);
			cell.setCellStyle(cellStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(subTitleList.get(index));
		}
	}

	/**
	 * @Title: setRowByStringArray
	 * @Description: 用数组填充行
	 * @param workbook
	 * @param tableHeaders
	 * @param row
	 * @throws
	 */
	public final static void setRowByStringArray(final String[] strings, final HSSFCellStyle cellStyle, HSSFRow row) {
		for (int index = 0; index < strings.length + 0; index++) {
			row.createCell(index);
			HSSFCell valueCell = row.getCell(index);
			valueCell.setCellStyle(cellStyle);
			valueCell.setCellType(HSSFCell.CELL_TYPE_STRING);
			valueCell.setCellValue(strings[index]);
		}
	}

	/**
	 * @Title: setRowByStringList
	 * @Description: 用列表填充行
	 * @param workbook
	 * @param stringList
	 * @param row
	 * @throws
	 */
	public final static void setRowByStringList(final List<String> stringList, final HSSFCellStyle cellStyle,
			HSSFRow row) {
		for (int index = 0; index < stringList.size(); index++) {
			row.createCell(index);
			HSSFCell cell = row.getCell(index);
			cell.setCellStyle(cellStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(stringList.get(index));
		}
	}
	/**
	 * @Title: setRowByStringList
	 * @Description: 用列表填充行(可以指定格式)
	 * @param workbook
	 * @param stringList
	 * @param row
	 * @throws
	 */
	public final static void setRowByStringListByStyle(final List<String> stringList, final List<HSSFCellStyle> cellStyle, HSSFRow row) {
		for (int index = 0; index < stringList.size(); index++) {
			row.createCell(index);
			HSSFCell cell = row.getCell(index);
			cell.setCellStyle(cellStyle.get(index));
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(stringList.get(index));
		}
	}

	/**
	 * @Description:
	 * @Date: May 29, 2015
	 * @Time: 9:58:39 AM
	 * @param sasId
	 * @param menuId
	 * @param activityId
	 * @param workbook
	 * @return
	 */
	public static final List<SasMenuActivityScore> parseScoreListFromXls(final long sasId, final long menuId,
			final long activityId, final InputStream in, final boolean is2007XlsxFile, final boolean isSupportMiliseconds)  throws IOException{
		Workbook workbook = null;		
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		Row row = null;
		final Sheet sheet = workbook.getSheetAt(0);
		List<SasMenuActivityScore> scoreList = new LinkedList<SasMenuActivityScore>();
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return scoreList;
		}
		for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
			row = sheet.getRow(r);
			if (isBlankLine(row)) {
				continue;
			}
			Cell cell0 = row.getCell(0);
			if (cell0 == null) {
				row.createCell(0);
				cell0 = row.getCell(0);
			}
			Cell cell1 = row.getCell(1);
			if (cell1 == null) {
				row.createCell(1);
				cell1 = row.getCell(1);
			}
			Cell cell2 = row.getCell(2);
			if (cell2 == null) {
				row.createCell(2);
				cell2 = row.getCell(2);
			}
			Cell cell3 = row.getCell(3);
			if (cell3 == null) {
				row.createCell(3);
				cell3 = row.getCell(3);
			}
			Cell cell4 = row.getCell(4);
			if (cell4 == null) {
				row.createCell(4);
				cell4 = row.getCell(4);
			}
			Cell cell5 = row.getCell(5);
			if (cell5 == null) {
				row.createCell(5);
				cell5 = row.getCell(5);
			}
			Cell cell6 = row.getCell(6);
			if (cell6 == null) {
				row.createCell(6);
				cell6 = row.getCell(6);
			}
			Cell cell7 = row.getCell(7);
			if (cell7 == null) {
				row.createCell(7);
				cell7 = row.getCell(7);
			}
			if (cell0 == null || cell1 == null || cell2 == null || cell3 == null || cell4 == null || cell5 == null || cell6 == null || cell7 == null) {
				return scoreList;
			}
			cell0.setCellType(Cell.CELL_TYPE_STRING);
			cell1.setCellType(Cell.CELL_TYPE_STRING);
			cell2.setCellType(Cell.CELL_TYPE_STRING);
			cell3.setCellType(Cell.CELL_TYPE_STRING);
			cell4.setCellType(Cell.CELL_TYPE_STRING);
			cell5.setCellType(Cell.CELL_TYPE_STRING);
			cell6.setCellType(Cell.CELL_TYPE_STRING);
			cell7.setCellType(Cell.CELL_TYPE_STRING);
			final String userMatchNumber = XSSUtil.filter(HtmlUtil.filterChineseDigitalsOrLetters(cell0.getStringCellValue()), true);
			final String userTrueName = XSSUtil.filter(cell1.getStringCellValue(), true);
			final String userIdentityCode = XSSUtil.filter(cell2.getStringCellValue(), true).replaceAll("x|ｘ|Ｘ", "X");
			final String userMobile = XSSUtil.filter(cell3.getStringCellValue(), true);
			final String userClothSize = XSSUtil.filter(cell4.getStringCellValue(), true);
			final String userSex = SasActivityScoreUtil.parseScoreSexTypeI18N(cell5.getStringCellValue()).name;
			final String userGroupName = XSSUtil.filter(cell6.getStringCellValue(), true);
			final String remark = XSSUtil.filter(cell7.getStringCellValue(), true);
			if (StringUtils.isNotBlank(userMatchNumber)) {
				scoreList.add(new SasMenuActivityScore(sasId, menuId, activityId, userMatchNumber, userTrueName,
						userIdentityCode, userMobile, userSex, userClothSize, userGroupName, remark, System.currentTimeMillis(),
						System.currentTimeMillis()));
			} else {
				continue;
			}
		}
		return scoreList;
	}

	public static final List<SasMenuActivityScore> parseUnremarkScoreListFromXls(final long sasId, final long menuId,
			final long activityId, final InputStream in, final boolean is2007XlsxFile, final boolean isSupportMiliseconds) throws IOException {
		Workbook workbook = null;		
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		Row row = null;
		Sheet sheet = workbook.getSheetAt(0);
		List<SasMenuActivityScore> scoreList = new LinkedList<SasMenuActivityScore>();
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return scoreList;
		}
		for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
			row = sheet.getRow(r);
			if (isBlankLine(row)) {
				continue;
			}
			Cell cell0 = row.getCell(0);
			if (cell0 == null) {
				row.createCell(0);
				cell0 = row.getCell(0);
			}
			Cell cell1 = row.getCell(1);
			if (cell1 == null) {
				row.createCell(1);
				cell1 = row.getCell(1);
			}
			Cell cell2 = row.getCell(2);
			if (cell2 == null) {
				row.createCell(2);
				cell2 = row.getCell(2);
			}
			Cell cell3 = row.getCell(3);
			if (cell3 == null) {
				row.createCell(3);
				cell3 = row.getCell(3);
			}
			Cell cell4 = row.getCell(4);
			if (cell4 == null) {
				row.createCell(4);
				cell4 = row.getCell(4);
			}
			Cell cell5 = row.getCell(5);
			if (cell5 == null) {
				row.createCell(5);
				cell5 = row.getCell(5);
			}
			Cell cell6 = row.getCell(6);
			if (cell6 == null) {
				row.createCell(6);
				cell6 = row.getCell(6);
			}
			if (cell0 == null || cell1 == null || cell2 == null || cell3 == null || cell4 == null || cell5 == null || cell6 == null) {
				return scoreList;
			}
			cell0.setCellType(Cell.CELL_TYPE_STRING);
			cell1.setCellType(Cell.CELL_TYPE_STRING);
			cell2.setCellType(Cell.CELL_TYPE_STRING);
			cell3.setCellType(Cell.CELL_TYPE_STRING);
			cell4.setCellType(Cell.CELL_TYPE_STRING);
			cell5.setCellType(Cell.CELL_TYPE_STRING);
			cell6.setCellType(Cell.CELL_TYPE_STRING);
			final String userTrueName = XSSUtil.filter(cell0.getStringCellValue(), true);
			final String userIdentityCode = XSSUtil.filter(cell1.getStringCellValue(), true);
			final String userMobile = XSSUtil.filter(cell2.getStringCellValue(), true);
			final String userClothSize = XSSUtil.filter(cell3.getStringCellValue(), true);
			final String userSex = SasActivityScoreUtil.parseScoreSexType(cell4.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "")).name;
			final String userGroupName = XSSUtil.filter(cell5.getStringCellValue(), true);
			final String remark = XSSUtil.filter(cell6.getStringCellValue(), true);
			scoreList.add(new SasMenuActivityScore(sasId, menuId, activityId, "", userTrueName, userIdentityCode,
						userMobile, userSex, userClothSize, userGroupName, remark,
						System.currentTimeMillis(), System.currentTimeMillis()));
		}
		return scoreList;
	}

	/**
	 * @Description:
	 * @Date: May 29, 2015
	 * @Time: 9:58:45 AM
	 * @param setting
	 * @param field
	 * @param workbook
	 * @return
	 */
	public static final BinaryEntry<SectionScoreType, Map<String, Long>> parseScoreFieldValueListFromXls(final SasMenuActivityScoreSetting setting,
			final InputStream in, final boolean is2007XlsxFile, final boolean isSupportMiliseconds) throws IOException
	{
		Workbook workbook = null;		
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		Row row = null;
		final Sheet sheet = workbook.getSheetAt(0);
		final Map<String, Long> scoreMap = new HashMap<String, Long>();
		SectionScoreType scoreType = SectionScoreType.SectionTime;
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return new BinaryEntry<SectionScoreType, Map<String, Long>>(scoreType, scoreMap);
		}
		row = sheet.getRow(sheet.getFirstRowNum());
		Cell titleCell1 = row.getCell(1);
		titleCell1.setCellType(Cell.CELL_TYPE_STRING);
		String titleBeginString = titleCell1.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
		if (titleBeginString.contains("年"))
		{//时间点
			for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
				row = sheet.getRow(r);
				if (isBlankLine(row)) {
					continue;
				}
				Cell cell0 = row.getCell(0);
				if (cell0 == null) {
					row.createCell(0);
					cell0 = row.getCell(0);
				}
				Cell cell1 = row.getCell(1);
				if (cell1 == null) {
					row.createCell(1);
					cell1 = row.getCell(1);
				}
				Cell cell2 = row.getCell(2);
				if (cell2 == null) {
					row.createCell(2);
					cell2 = row.getCell(2);
				}
				Cell cell3 = row.getCell(3);
				if (cell3 == null) {
					row.createCell(3);
					cell3 = row.getCell(3);
				}
				Cell cell4 = row.getCell(4);
				if (cell4 == null) {
					row.createCell(4);
					cell4 = row.getCell(4);
				}
				Cell cell5 = row.getCell(5);
				if (cell5 == null) {
					row.createCell(5);
					cell5 = row.getCell(5);
				}
				Cell cell6 = row.getCell(6);
				if (cell6 == null) {
					row.createCell(6);
					cell6 = row.getCell(6);
				}
				if (cell0 == null || cell1 == null || cell2 == null || cell3 == null || cell4 == null || cell5 == null
						|| cell6 == null) {
					return new BinaryEntry<SectionScoreType, Map<String, Long>>(scoreType, scoreMap);
				}
				cell0.setCellType(Cell.CELL_TYPE_STRING);
				cell1.setCellType(Cell.CELL_TYPE_STRING);
				cell2.setCellType(Cell.CELL_TYPE_STRING);
				cell3.setCellType(Cell.CELL_TYPE_STRING);
				cell4.setCellType(Cell.CELL_TYPE_STRING);
				cell5.setCellType(Cell.CELL_TYPE_STRING);
				cell6.setCellType(Cell.CELL_TYPE_STRING);
				int miliseconds = 0;
				if(isSupportMiliseconds){//是否支持毫秒
					final Cell cell7 = row.getCell(7);
					if(cell7 != null)
					{
						cell7.setCellType(Cell.CELL_TYPE_STRING);
						miliseconds = IdUtil.convertToInteger(cell7.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0);
					}
				}
				final String userMatchNumber = HtmlUtil.filterChineseDigitalsOrLetters(cell0.getStringCellValue()
						.replaceAll(HtmlUtil.WhiteSpaceReg, " ").trim());
				if (StringUtils.isNotBlank(userMatchNumber)) {
					final int year = IdUtil.convertToInteger(cell1.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0);
					final int month = IdUtil.convertToInteger(cell2.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""),
							0);
					final int day = IdUtil.convertToInteger(cell3.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0);
					final int hours = IdUtil.convertToInteger(cell4.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""),
							0);
					final int minutes = IdUtil.convertToInteger(cell5.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0);
					final int seconds = IdUtil.convertToInteger(cell6.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0);
					long sectionTime = SasActivityScoreUtil.generateSectionTimeScoreFieldValue(setting,
							SectionScoreType.SectionTime, year, month, day, hours, minutes, seconds, miliseconds);
					scoreMap.put(userMatchNumber, sectionTime);
				}
			}
		} else {//耗时
			scoreType = titleBeginString.contains("耗时") ? SectionScoreType.SectionCostTime : SectionScoreType.SectionTime;
			for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
				row = sheet.getRow(r);
				if (isBlankLine(row)) {
					continue;
				}
				Cell cell0 = row.getCell(0);
				if (cell0 == null) {
					row.createCell(0);
					cell0 = row.getCell(0);
				}
				Cell cell1 = row.getCell(1);
				if (cell1 == null) {
					row.createCell(1);
					cell1 = row.getCell(1);
				}
				if (cell0 == null || cell1 == null) {
					return new BinaryEntry<SectionScoreType, Map<String, Long>>(SectionScoreType.SectionCostTime, scoreMap);
				}
				cell0.setCellType(Cell.CELL_TYPE_STRING);
				final String userMatchNumber = cell0.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
				if(StringUtils.isBlank(userMatchNumber)){
					continue;
				}
				int hours = 0, minutes = 0, seconds = 0, miliseconds=0;
				if (cell1.getCellType() == Cell.CELL_TYPE_STRING) {
					final String hhmmss = cell1.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
					String[] times = hhmmss.split("(:)|(：)|(/)|(\")|(”)|(时)|(分)|(秒)|(h)|(m)|(s)|(H)|(M)|(S)");
					if (times == null || times.length < 1) {
						continue;
					}
					//可能最后那个包含了毫秒
					final String[] lastTimeParts = (times[times.length-1]).split("(。)|(．)|(\\.)");
					if(lastTimeParts.length > 1){//应该当做秒和毫秒处理
						if(isSupportMiliseconds){
							miliseconds = IdUtil.convertToInteger(lastTimeParts[1], 0);
						}
						if(times.length < 2){
							times = new String[]{"0", "0", lastTimeParts[0]};
						}else if(times.length < 3){
							times = new String[]{"0", times[0], lastTimeParts[0]};
						}else{
							times[times.length-1] = lastTimeParts[0];
						}
					}
					if(times.length > 2){//hh:mm:ss
						hours = IdUtil.convertToInteger(times[0], 0);
						minutes = IdUtil.convertToInteger(times[1], 0);
						seconds = IdUtil.convertToInteger(times[2], 0);
					}else{
						if(scoreType == SectionScoreType.SectionCostTime){
							if(times.length > 1){//mm:ss
								minutes = IdUtil.convertToInteger(times[0], 0);
								seconds = IdUtil.convertToInteger(times[1], 0);
							}else{//ss
								seconds = IdUtil.convertToInteger(times[0], 0);
							}	
						}else{
							if(times.length > 1){//hh:mm
								hours = IdUtil.convertToInteger(times[0], 0) % 24;
								minutes = IdUtil.convertToInteger(times[1], 0) % 60;
							}else{//hh
								hours = IdUtil.convertToInteger(times[0], 0) % 24;
							}	
						}
					}
				} else {
					Date date = null;
					try {
						if (cell1.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) { // 数值型
							if (HSSFDateUtil.isCellDateFormatted(cell1)) { // 如果是date类型则获取该cell的date值
								date = HSSFDateUtil.getJavaDate(cell1.getNumericCellValue());
							} else { // 纯数字
								final double value = cell1.getNumericCellValue();
								hours = ((int) (value / 10000)) % 480;
								minutes = ((int) (value / 100)) % 100;
								seconds = ((int) value) % 100;
							}
						} else {
							date = cell1.getDateCellValue();
						}
						if (date != null) {
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							hours = calendar.get(Calendar.HOUR_OF_DAY);
							minutes = calendar.get(Calendar.MINUTE);
							seconds = calendar.get(Calendar.SECOND);
						}
					} catch (Exception ex) {
						logger.error("Fail to import scores , rowsIndex=" + cell1.getRowIndex(), ex);
					}
				}
				final long sectionTime = SasActivityScoreUtil.generateSectionTimeScoreFieldValue(setting,
							scoreType, 0, 0, 0, hours, minutes, seconds, miliseconds);
				scoreMap.put(userMatchNumber, sectionTime);
			}
		}
		return new BinaryEntry<SectionScoreType, Map<String, Long>>(scoreType, scoreMap);
	}

	/**
	 * @Description: 从Excel表格解析成绩得分
	 * @Date: Jul 15, 2015
	 * @Time: 10:27:08 AM
	 * @param workbook
	 * @return
	 * @throws IOException 
	 */
	public static final Map<String, Long> parseScorePointsMapFromXls(final InputStream in, final boolean is2007XlsxFile, 
			final boolean isSupportMiliseconds) throws IOException {
		Workbook workbook = null;	
		if(is2007XlsxFile){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		final Sheet sheet = workbook.getSheetAt(0);
		Map<String, Long> scoreMap = new HashMap<String, Long>();
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return scoreMap;
		}
		for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
			final Row row = sheet.getRow(r);
			if (isBlankLine(row)) {
				continue;
			}
			Cell cell0 = row.getCell(0);
			if (cell0 == null) {
				row.createCell(0);
				cell0 = row.getCell(0);
			}
			Cell cell1 = row.getCell(1);
			if (cell1 == null) {
				row.createCell(1);
				cell1 = row.getCell(1);
			}
			if (cell0 == null || cell1 == null) {
				return scoreMap;
			}
			cell0.setCellType(Cell.CELL_TYPE_STRING);
			cell1.setCellType(Cell.CELL_TYPE_STRING);
			String userMatchNumber = cell0.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, "");
			final long points = IdUtil.convertTolong(cell1.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0);
			if (StringUtils.isNotBlank(userMatchNumber)) {
				scoreMap.put(userMatchNumber, points);
			}
		}
		return scoreMap;
	}

	/******************
	 * 生成活动团队报名的excel模板
	 * 
	 * @param necessaryFieldNames
	 * @param necessaryFieldDescs
	 * @param necessaryFieldNames
	 * @param necessaryFieldDescs
	 * @param out
	 * @throws IOException
	 */
	public static void generateActivityTeamApplierTemplate(final String[] fieldNames, final String[] fieldDescs,
			final OutputStream out) throws IOException {
		final HSSFWorkbook workbook = new HSSFWorkbook();
		final HSSFSheet sheet = workbook.createSheet();
		for (int i = 0; i < fieldNames.length; i++) {
			sheet.setColumnWidth(i, 6000);
			sheet.setDefaultRowHeight((short)500);
		}
		final HSSFRow row = sheet.createRow((short) 0);// 创建第一行
		for (int i = 0; i < fieldNames.length; i++) {
			final HSSFCell cell1 = row.createCell(i);
			HSSFCellStyle cellStyle = workbook.createCellStyle();
			HSSFDataFormat format = workbook.createDataFormat();
			cellStyle.setDataFormat(format.getFormat("@"));
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(fieldNames[i]);
			cell1.setCellType(HSSFCell.CELL_TYPE_STRING);
		}
		if (ArrayUtils.isNotEmpty(fieldDescs)) {
			final HSSFRow row2 = sheet.createRow((short) 1);// 创建第二行
			for (int i = 0; i < fieldDescs.length; i++) {
				final HSSFCell cell2 = row2.createCell(i);
				HSSFCellStyle cellStyle2 = workbook.createCellStyle();
				HSSFDataFormat format = workbook.createDataFormat();
				cellStyle2.setDataFormat(format.getFormat("@"));
				cell2.setCellStyle(cellStyle2);
				cell2.setCellValue(fieldDescs[i]);
				cell2.setCellType(HSSFCell.CELL_TYPE_STRING);
			}
			for (int i = 2; i < 50; i++) {
				HSSFRow row3 = sheet.createRow((short) i);
				for (int j = 0; j < fieldNames.length; j++) {
					final HSSFCell cell3 = row3.createCell(j);
					HSSFCellStyle cellStyle2 = workbook.createCellStyle();
					HSSFDataFormat format = workbook.createDataFormat();
					cellStyle2.setDataFormat(format.getFormat("@"));
					cell3.setCellStyle(cellStyle2);
					cell3.setCellType(HSSFCell.CELL_TYPE_STRING);
				}
			}
		} else {
			for (int i = 1; i < 50; i++) {
				HSSFRow row3 = sheet.createRow((short) i);
				for (int j = 0; j < fieldNames.length; j++) {
					final HSSFCell cell3 = row3.createCell(j);
					HSSFCellStyle cellStyle2 = workbook.createCellStyle();
					HSSFDataFormat format = workbook.createDataFormat();
					cellStyle2.setDataFormat(format.getFormat("@"));
					cell3.setCellStyle(cellStyle2);
					cell3.setCellType(HSSFCell.CELL_TYPE_STRING);
				}
			}
		}
		workbook.write(out);
	}

	/****************
	 * 读取团队报名时的团队成员信息
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static final List<String[]> readTeamApplierFromExcel(final InputStream in, final boolean is2007XlsxFile) throws IOException{
		final List<String[]> applerlist = new LinkedList<String[]>();
		Workbook workbook = null;
		if(is2007XlsxFile){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		final Sheet sheet = workbook.getSheetAt(0);
		Row row = null;
		Row oneRow = sheet.getRow(0);
		final int firstrow = sheet.getFirstRowNum();
		final int lastrow = sheet.getLastRowNum();
		int rowcount = 0;
		for (int i = 0; i < lastrow; i++) {
			if (isBlankLine(sheet.getRow(i))) {
				break;
			}
			rowcount = i + 1; // 获取EXCLE表一共有几行数据
		}
		if (rowcount < 1) {
			return applerlist;
		}
		int columncount = 0;
		for (int i = 0;; i++) {
			final String cellValue = (oneRow.getCell(i) == null) ? null : oneRow.getCell(i).getStringCellValue();
			if (StringUtils.isBlank(cellValue)) {
				break;
			}
			columncount++; // 获取EXCLE表一共有几列数据
		}
		if (columncount < 1) {
			return applerlist;
		}
		Cell[] cell = new Cell[columncount];
		boolean isFirstRow = true;
		for (int i = firstrow; i <= rowcount; i++)
		{
			final String[] field = new String[columncount];
			row = sheet.getRow(i);
			if (ExcelUtil.isBlankLine(row)) {
				continue;
			}
			for (int j = 0; j < columncount; j++) {
				cell[j] = row.getCell(j);
				field[j] = "";
				if (cell[j] != null) {
					if (cell[j].getCellType() == HSSFCell.CELL_TYPE_STRING) {
						if(isFirstRow){
							field[j] = HtmlUtil.filterChineseDigitalsOrLetters(cell[j].getStringCellValue());
						}else{
							field[j] = HtmlUtil.filterChineseDigitalsOrLetters(cell[j].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""));
						}
					} else {
						try {
							if (HSSFDateUtil.isCellDateFormatted(cell[j])) { // 如果是date类型则获取该cell的date值
								field[j] = String.valueOf(cell[j].getNumericCellValue());
							} else { // 纯数字
								cell[j].setCellType(HSSFCell.CELL_TYPE_STRING);
								if(isFirstRow){
									field[j] = HtmlUtil.filterChineseDigitalsOrLetters(cell[j].getStringCellValue());									
								}else{
									field[j] = HtmlUtil.filterChineseDigitalsOrLetters(cell[j].getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""));
								}
							}
						} catch (Exception ex) {
							logger.error("Fail to import scores , rowsIndex=" + cell[j].getRowIndex(), ex);
						}
					}
				}
			}
			isFirstRow = false;
			applerlist.add(field);
		}
		return applerlist;
	}
	
	/**********************************解析手机号码*************************************
	 * 解析EXCEL手机号码
	 */
	public static final List<String> parsePhonesFromXls(final InputStream in, final boolean is2007XlsxFile) throws IOException
	{	
		Workbook workbook = null;		
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		final Sheet sheet = workbook.getSheetAt(0);
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return new ArrayList<String>(0);
		}
		final Set<String> phones = new HashSet<String>();
		for (int r = sheet.getFirstRowNum() ; r <= sheet.getLastRowNum(); r++) {
			final Row row = sheet.getRow(r);
			if (isBlankLine(row)) {
				continue;
			}
			Cell cell0 = row.getCell(0);
			if (cell0 == null) {
					continue;
			}
			cell0.setCellType(Cell.CELL_TYPE_STRING);
			String phone = cell0.getStringCellValue();
			if(StringUtils.isBlank(phone)){
				continue;
			}
			phone = phone.trim();
			if(phone.length() < 11){
				continue;
			}
			//可能是参数化短信
			if(ValidatorUtil.mobileValidate(phone) || phone.indexOf('=') > 0){
				phones.add(phone);
			}
		}
		return CollectionUtils.toList(phones);
	}
	
	/**********
	 * 解析文件， 读取被保险人信息，同时把被保人信息给前端， 文件内容中的列有英文名和非英文名区别
	 * @param in
	 * @param is2007XlsxFile
	 * @throws IOException
	 */
	public static final List<SasInsurranceOrderApplier> parseInsurranceAppliersFromXls(final InputStream in, final boolean is2007XlsxFile) throws IOException
	{	
		Workbook workbook = null;		
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}
		List<SasInsurranceOrderApplier> appliers = new LinkedList<SasInsurranceOrderApplier>();		
		Row row = null;
		final Sheet sheet = workbook.getSheetAt(0);
		final int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return appliers;
		}
		boolean hasEnglishNameColumn = false;
		boolean hasReadTableHeaderRow = false;
		int maxColumnCount = 7;
		for (int r = sheet.getFirstRowNum(); r <= totalRows; r++)
		{
			row = sheet.getRow(r);
			if (isBlankLine(row)) {
				continue;
			}
			final Cell firstCell = row.getCell(0);
			if(firstCell == null){
				continue;
			}
			firstCell.setCellType(Cell.CELL_TYPE_STRING);	
			String cellValue = XSSUtil.filter(firstCell.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
			if(cellValue.contains("序号"))
			{
				hasReadTableHeaderRow = true;
				appliers = new LinkedList<SasInsurranceOrderApplier>();	//说明可能之前读取了样例数据，丢弃之
				final Cell englishNameCell = row.getCell(2);
				if(englishNameCell != null)
				{
					englishNameCell.setCellType(Cell.CELL_TYPE_STRING);	
					cellValue = XSSUtil.filter(englishNameCell.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
					if(cellValue.contains("英文名"))
					{
						hasEnglishNameColumn = true;
						maxColumnCount = 8;
					}
				}
				continue;
			}
			if(!hasReadTableHeaderRow){//尚未读取表头的列名信息
				continue;
			}
			//说明是数据列
			final String[] cellValues = new String[maxColumnCount];
			for(int i=0; i<maxColumnCount; i++)
			{
				final Cell cell = row.getCell(i);
				if(cell != null)
				{
					if(i >= 4){
						if(StringUtils.isBlank(cellValues[1]) && 
								((hasEnglishNameColumn && StringUtils.isBlank(cellValues[2]) && StringUtils.isBlank(cellValues[4]))
										||(!hasEnglishNameColumn && StringUtils.isBlank(cellValues[3])))){
							continue;
						}
					}
					//日期
					if((hasEnglishNameColumn && i==5) || (!hasEnglishNameColumn && i==4)){						
						try{
							cell.setCellType(Cell.CELL_TYPE_STRING);
							cellValues[i] = XSSUtil.filter(cell.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).replaceAll("(\\-)|(－)", "/");
							if(cellValues[i].contains("/")){
								cellValues[i] = String.valueOf(TimeUtil.parseDate(cellValues[i], TimeFormat.yyyyMMDD).getTime());
							}else{
								cellValues[i] = "0";
							}
						}catch(Exception ex){
							logger.error("Fail to parse insurrance user: column=" + i, ex);
							cellValues[i] = "0";
						}
					}else{
						try{
							cellValues[i] = XSSUtil.filter(cell.getStringCellValue().replaceAll(HtmlUtil.WhiteSpaceReg, ""), true);
						}catch(Exception ex){
							logger.error("Fail to parse insurrance user2: column=" + i, ex);
						}
					}
				}
			}
			SasInsurranceOrderApplier applier = new SasInsurranceOrderApplier();
			applier.setUserName(cellValues[1] == null ? "" : cellValues[1]);
			if(hasEnglishNameColumn){
				applier.setEnglishUserName(cellValues[2]);
				if(cellValues[3] == null || cellValues[3].contains("1")){
					applier.setIdentityCardType(IdentityCardType.IdCard.type);
				}else if(cellValues[3].contains("2")){
					applier.setIdentityCardType(IdentityCardType.Passport.type);
				}else{
					applier.setIdentityCardType(IdentityCardType.Other.type);
				}				
				applier.setIdentityCardNum(cellValues[4] == null ? "" : cellValues[4].replaceAll("x|ｘ|Ｘ", "X"));
				applier.setBirthday(IdUtil.convertTolong(cellValues[5], 0L));
				applier.setSex(cellValues[6] != null && cellValues[6].contains("女") ? SexType.Female.type : SexType.Male.type);
				applier.setPhone(cellValues[7] == null ? "" : cellValues[7]); //不判断手机号码有效性，等待提交保单时在判断
			}else{
				if(cellValues[2] == null || cellValues[2].contains("1")){
					applier.setIdentityCardType(IdentityCardType.IdCard.type);
				}else if(cellValues[2].contains("2")){
					applier.setIdentityCardType(IdentityCardType.Passport.type);
				}else{
					applier.setIdentityCardType(IdentityCardType.Other.type);
				}				
				applier.setIdentityCardNum(cellValues[3] == null ? "" : cellValues[3].replaceAll("x|ｘ|Ｘ", "X"));
				applier.setBirthday(IdUtil.convertTolong(cellValues[4], 0L));
				applier.setSex(cellValues[5] != null && cellValues[5].contains("女") ? SexType.Female.type : SexType.Male.type);
				applier.setPhone(cellValues[6] == null ? "" : cellValues[6]); //不判断手机号码有效性，等待提交保单时在判断
			}
			if(StringUtils.isNotBlank(applier.getUserName()) || StringUtils.isNotBlank(applier.getIdentityCardNum())
					|| StringUtils.isNotBlank(applier.getEnglishUserName())){
				appliers.add(applier);
			}
		}
		return appliers;
	}

	/**
	 * @Title: parseGoodsFromXls
	 * @Description: 从Excel里面批量导入商品
	 * @return
	 * @throws
	 */
	public static BinaryEntry<Set<Integer>, List<SasImportGood>> parseImportGoodsFromXls(final Sas sas, final SasUser sasUser, final InputStream in,
			final boolean is2007XlsxFile) throws IOException
	{
		final Set<Integer> ingoredRowSet = new HashSet<Integer>();
		Workbook workbook = null;	
		if(is2007XlsxFile){ // || POIFSFileSystem.hasPOIFSHeader(in)){
			workbook = new XSSFWorkbook(in);
		}else{
			workbook = new HSSFWorkbook(in);
		}	
		Row row = null;
		final Sheet sheet = workbook.getSheetAt(0);
		final List<SasImportGood> result = new LinkedList<SasImportGood>();
		int totalRows = sheet.getLastRowNum();
		if (totalRows < 1) {
			return new BinaryEntry<Set<Integer>, List<SasImportGood>>(ingoredRowSet, result);
		}
		final int priceBits = SasSwitch.SasGoodPrice4Bits.isMe(sas.getSwitchState()) ? 4 : 2;
		boolean isFirstRowSkuCode = false;
		for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) 
		{
			row = sheet.getRow(r);
			if (isBlankLine(row)) {
				continue;
			}
			//第一行
			if(r == sheet.getFirstRowNum()){
				final Cell cell0 = row.getCell(0);
				cell0.setCellType(Cell.CELL_TYPE_STRING);
				isFirstRowSkuCode = cell0.getStringCellValue().contains("编号");
				continue;
			}
			//第二行
			final String[] cells = new String[8];
			for(int i=0; i<cells.length; i++)
			{
				cells[i] = "";
				if(i > 5 && !isFirstRowSkuCode){
					break;
				}
				final Cell cell = row.getCell(i);
				if (cell != null) {//最后一列可选的
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cells[i] = cell.getStringCellValue();
				}				
			}
			SasImportGood good = null;
			if(isFirstRowSkuCode){				
				good = new SasImportGood(XSSUtil.filter(cells[0].replaceAll(HtmlUtil.WhiteSpaceReg, "").toUpperCase(), true).trim(), 
						XSSUtil.filter(cells[1].replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).trim(),
						XSSUtil.filter(cells[2].replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).trim(),
						XSSUtil.filter(cells[3].replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).trim(),
						XSSUtil.filter(cells[4].replaceAll(HtmlUtil.WhiteSpaceReg, "件"), true).trim(),
						new BigDecimal(IdUtil.convertToDouble(XSSUtil.filter(cells[6].replaceAll(HtmlUtil.WhiteSpaceReg, ""),
								true).trim(), 0)).setScale(priceBits, BigDecimal.ROUND_HALF_UP),
						new BigDecimal(IdUtil.convertToDouble(XSSUtil.filter(cells[5].replaceAll(HtmlUtil.WhiteSpaceReg, ""),
								true).trim(), 0)).setScale(priceBits, BigDecimal.ROUND_HALF_UP),
						(cells[7] == null ? 0 : IdUtil.convertToDouble(cells[7].replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0)),
						r);	
			}else{
				good = new SasImportGood("", XSSUtil.filter(cells[0].replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).trim(),
						XSSUtil.filter(cells[1].replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).trim(),
						XSSUtil.filter(cells[2].replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).trim(),
						XSSUtil.filter(cells[3].replaceAll(HtmlUtil.WhiteSpaceReg, "件"), true).trim(),
						new BigDecimal(IdUtil.convertToDouble(XSSUtil.filter(cells[4].replaceAll(HtmlUtil.WhiteSpaceReg, ""), true).trim(), 0)).setScale(priceBits, BigDecimal.ROUND_HALF_UP),
						new BigDecimal(0), 
						(cells[5] == null ? 0 : IdUtil.convertToDouble(cells[5].replaceAll(HtmlUtil.WhiteSpaceReg, ""), 0)),
						r);	
				good.setStockPrice(good.getSalePrice());
			}
			if(StringUtils.isBlank(good.getUnit())){
				good.setUnit("件");
			}
			if(StringUtils.isBlank(good.getTitle())
					|| StringUtils.isBlank(good.getStyleClass()) || StringUtils.isBlank(good.getSizeClass())){
				ingoredRowSet.add(r);
				continue;
			}
			result.add(good);
		}
		return new BinaryEntry<Set<Integer>, List<SasImportGood>>(ingoredRowSet, result);
	}
	
	
	
	public static final void main(String[] args)
	{
		try {
			final List<SasInsurranceOrderApplier> list = ExcelUtil.parseInsurranceAppliersFromXls(new FileInputStream(new File("C:/保险录入模板_英文名.xls")), false);
			for(final SasInsurranceOrderApplier applier : list){
				System.out.println(applier);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}