/**
 * 
 */
package com.sas.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xerces.internal.xs.StringList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.sas.core.constant.SasConstant.CopyRight;
import com.sas.core.dto.SasReportGoodOrderRowDetail;

/**
 * itext pdf util
 * 
 * @author zhuliming
 * 
 */
public class ITextUtil {

	private static final Logger logger = Logger.getLogger(ITextUtil.class);

	private final boolean needSequenceNumber;
	private BaseFont chineseBaseFont = null;
	private Font pdf8Font = null; // 一般都用来做页眉页脚
	private Font pdfTextWaterFont = null; // 一般用来做水印
	private Font pdf12Font = null; // 一般用来做内容
	private Font pdf12BoldFont = null; // 一般用来做内容
	private Document pdfDocument = null;
	private PdfPTable pdfTable = null;
	private Rectangle pageSize = PageSize.A2;
	private String author = CopyRight.Author.text;
	private boolean needPageFoot = true;

	public ITextUtil() {
		needSequenceNumber = false;
	}

	public ITextUtil(final boolean _needSequenceNumber) {
		this.needSequenceNumber = _needSequenceNumber;
	}

	public ITextUtil(final boolean _needSequenceNumber, final boolean needPageFoot) {
		this.needSequenceNumber = _needSequenceNumber;
		this.needPageFoot = needPageFoot;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final String[] tableContent = new String[] { "姓名", "年龄", "职业", "籍贯", "学历", "单位名称", "联系电话", "联系电话" };
		final List<String> subTitles = new ArrayList<String>(3);
		subTitles.add("项目批次：全程马拉松，半程马拉松");
		subTitles.add("订单状态：已付款");
		subTitles.add("报名人数：100");

		final File file = new File("E:/" + System.currentTimeMillis() + ".pdf");
		file.createNewFile();
		final FileOutputStream out = new FileOutputStream(file);

		final ITextUtil itext = new ITextUtil(true);
		itext.startPDFDocument(out, "杭州西湖马拉松-报名表", subTitles, null, tableContent);

		int offset = 0;
		for (int i = 0; i < 10; i++) {
			final List<String[]> table = new ArrayList<String[]>(100);
			for (int j = 0; j < 100; j++) {
				table.add(new String[] { "许果", "31", "软件工程师", "浙江杭州", "大学本科", "浙江水果大王信息技术有限公司", "18905710571", "联系电话" });
				offset++;
			}
			itext.addPDFDocumentRows(table, offset);
		}
		itext.endPDFDocument(out);
	}

	/**
	 * 设置PDF创建者信息
	 * 
	 * @param pdfDocument
	 */
	public static final Document setCreatorInfo(Document pdfDocument, final String author, final String title) {
		if (pdfDocument == null) {
			return null;
		}
		// 文档属性
		if (StringUtils.isNotBlank(author)) {
			pdfDocument.addAuthor(author);
		}
		if (StringUtils.isNotBlank(title)) {
			pdfDocument.addTitle(title);
			pdfDocument.addSubject(title);
			pdfDocument.addKeywords(title);// 文档关键字信息
		}
		pdfDocument.addCreator(CopyRight.Creator.text);// 应用程序名称
		return pdfDocument;
	}

	/**
	 * 设置成只读权限
	 * 
	 * @param pdfWriter
	 */
	public static final PdfWriter setReadOnlyPDFFile(PdfWriter pdfWriter) throws DocumentException {
		pdfWriter.setEncryption(null, null, PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_128);
		return pdfWriter;
	}

	/**
	 * 变更一个图片对象的展示位置和角度信息
	 * 
	 * @param waterMarkImage
	 * @param xPosition
	 * @param yPosition
	 * @return
	 */
	public final static Image getWaterMarkImage(Image waterMarkImage, float xPosition, float yPosition) {
		waterMarkImage.setAbsolutePosition(xPosition, yPosition);// 坐标
		waterMarkImage.setRotation(-20);// 旋转 弧度
		waterMarkImage.setRotationDegrees(-45);// 旋转 角度
		waterMarkImage.scalePercent(100);// 依照比例缩放
		return waterMarkImage;
	}

	/********************
	 * 从数据库中导出数据并以PDF文件形式存储 列信息较多，行信息可能超过100万 文档仅有只读权限，设置文档作者信息 在文档页头设置公司信息版权信息
	 * 添加公司的文字和图片水印信息
	 * 
	 * @param pdfOutputStream
	 *            ：输出流， 一般从response中获得
	 * @param title
	 *            ：文档标题
	 * @param:subTitles, 副标题，例如“项目批次： 全程马拉松，半程马拉松"
	 * @param waterText
	 *            : 水印文字， 如果为空， 则无水印
	 * @param tableHeaders
	 *            : 表头的数组
	 * @return
	 */
	public synchronized boolean startPDFDocument(final OutputStream pdfOutputStream, final String title,
			final List<String> subTitles, final String waterText, final String[] tableHeaders) {
		return this.startPDFDocument(pdfOutputStream, PageSize.A2, CopyRight.Author.text, title, subTitles, waterText,
				tableHeaders);
	}

	public synchronized boolean startPDFDocument(final OutputStream pdfOutputStream, final Rectangle pageSize,
			final String author, final String title, final List<String> subTitles, final String waterText,
			final String[] tableHeaders) {
		this.pageSize = pageSize;
		this.pdfDocument = new Document(this.pageSize, 50, 50, 50, 50);
		try {
			final PdfWriter pdfWriter = PdfWriter.getInstance(pdfDocument, pdfOutputStream);
			// 设置作者信息
			this.author = author;
			ITextUtil.setCreatorInfo(pdfDocument, author, title);
			// 设置文件只读权限
			// ITextUtil.setReadOnlyPDFFile(pdfWriter);
			// 通过PDF页面事件模式添加文字水印功能
			if (StringUtils.isNotBlank(waterText)) {
				pdfWriter.setPageEvent(this.new TextWaterMarkPdfPageEvent(waterText));
			}
			// 通过PDF页面事件模式添加图片水印功能
			// String waterMarkFullFilePath = "F:/1.jpg";//水印图片
			// pdfWriter.setPageEvent(this.new
			// PictureWaterMarkPdfPageEvent(waterMarkFullFilePath));
			// 通过PDF页面事件模式添加页头和页脚信息功能
			pdfWriter.setPageEvent(this.new HeadFootInfoPdfPageEvent());
			// 打开PDF文件流
			pdfDocument.open();
			// 添加标题
			if (StringUtils.isNotBlank(title)) {
				final Font titleFont = new Font(this.getChineseBaseFont(), 20, Font.BOLD, BaseColor.BLACK);
				final Paragraph titlePart = new Paragraph(title, titleFont);
				titlePart.setAlignment(Paragraph.ALIGN_CENTER);
				pdfDocument.add(titlePart);
				pdfDocument.add(Chunk.NEWLINE);
			}
			// 添加顶部副标题， 左对齐
			if (CollectionUtils.isNotEmpty(subTitles)) {
				final Font subTitleFont = new Font(this.getChineseBaseFont(), 14, Font.NORMAL, BaseColor.BLACK);
				for (final String subtitle : subTitles) {
					final Phrase subTitlePart = new Phrase(subtitle, subTitleFont);
					pdfDocument.add(subTitlePart);
					pdfDocument.add(Chunk.NEWLINE);
				}
			}
			// 插入表格头部
			if (ArrayUtils.isEmpty(tableHeaders)) {
				return true;
			}
			// 创建一个N列的表格控件
			this.pdfTable = new PdfPTable(this.needSequenceNumber ? (tableHeaders.length + 1) : tableHeaders.length);
			// 设置表格占PDF文档100%宽度
			pdfTable.setWidthPercentage(100);
			// 如果有序号， 则需要固定宽度
			if (needSequenceNumber) {
				final float[] widthPercent = new float[tableHeaders.length + 1];
				widthPercent[0] = 0.06f;
				final float stepWidthPercent = (1 - widthPercent[0]) / tableHeaders.length;
				widthPercent[widthPercent.length - 1] = 1 - widthPercent[0];
				for (int i = 1; i < widthPercent.length - 1; i++) {
					widthPercent[i] = stepWidthPercent;
					widthPercent[widthPercent.length - 1] = widthPercent[widthPercent.length - 1] - stepWidthPercent;
				}
				pdfTable.setWidths(widthPercent);
			}
			// 水平方向表格控件左对齐
			pdfTable.setHorizontalAlignment(PdfPTable.ALIGN_JUSTIFIED);
			// 创建一个表格的表头单元格
			final PdfPCell pdfTableHeaderCell = new PdfPCell();
			// 设置表格的表头单元格颜色
			pdfTableHeaderCell.setBackgroundColor(new BaseColor(250, 250, 250));
			pdfTableHeaderCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			pdfTableHeaderCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			if (needSequenceNumber) {
				pdfTableHeaderCell.setPhrase(new Paragraph("序号", this.getChinese14Font(true)));
				pdfTable.addCell(pdfTableHeaderCell);
			}
			for (final String header : tableHeaders) {
				pdfTableHeaderCell.setPhrase(new Paragraph(header, this.getChinese14Font(true)));
				pdfTable.addCell(pdfTableHeaderCell);
			}
			return true;
		} catch (Exception ex) {
			logger.error("fail to generate pdf report: " + ex.getMessage(), ex);
			return false;
		}
	}
	
	public synchronized void createApplierCountPDFDocument(List<String> subTitles){
    try {
     if (CollectionUtils.isNotEmpty(subTitles)) {
				final Font subTitleFont = new Font(this.getChineseBaseFont(), 14, Font.NORMAL, BaseColor.BLACK);
				for (final String subtitle : subTitles) {
					final Phrase subTitlePart = new Phrase(subtitle, subTitleFont);
					pdfDocument.add(subTitlePart);
					pdfDocument.add(Chunk.NEWLINE);
				}
			}
    } catch (Exception ex) {
      logger.error("fail to generate pdf report: " + ex.getMessage(), ex);
    }
  }

	/*********************
	 * 往pdf添加一批行， 每一行rows必须和表头长度相等
	 * 
	 * @param rowsList
	 */
	public synchronized boolean addPDFDocumentRows(final List<String[]> rowsList, final int currentOffset) {
		if (CollectionUtils.isEmpty(rowsList)) {
			return true;
		}
		try {
			// 创建一个表格的正文内容单元格
			final PdfPCell pdfTableContentCell = new PdfPCell();
			pdfTableContentCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			pdfTableContentCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			// 表格内容行数的填充
			int offset = currentOffset;
			for (final String[] row : rowsList) {
				if (needSequenceNumber) {
					pdfTableContentCell
							.setPhrase(new Paragraph(String.valueOf(++offset), this.getChinese14Font(false)));
					pdfTable.addCell(pdfTableContentCell);
				}
				for (final String cellValue : row) {
					pdfTableContentCell.setPhrase(new Paragraph(cellValue, this.getChinese14Font(false)));
					pdfTable.addCell(pdfTableContentCell);
				}
			}
			// 表格内容每写满某个数字的行数时，其内容一方面写入物理文件，另一方面释放内存中存留的内容。
			this.pdfDocument.add(pdfTable);
			this.pdfTable.deleteBodyRows();
			return true;
		} catch (Exception ex) {
			logger.error("fail to generate pdf report: " + ex.getMessage(), ex);
			return false;
		}
	}

	public synchronized boolean addPDFDocumentGoodOrderRows(final List<SasReportGoodOrderRowDetail> rowsList, 
			final int currentOffset) {
		if (CollectionUtils.isEmpty(rowsList)) {
			return true;
		}
		try {
			// 创建一个表格的正文内容单元格
			final PdfPCell pdfTableContentCell = new PdfPCell();
			pdfTableContentCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			pdfTableContentCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			// 表格内容行数的填充
			int offset = currentOffset;
			for (final SasReportGoodOrderRowDetail rowDetail : rowsList) {
				if (needSequenceNumber) {
					pdfTableContentCell
							.setPhrase(new Paragraph(String.valueOf(++offset), this.getChinese14Font(false)));
					pdfTable.addCell(pdfTableContentCell);
				}
				for (final String cellValue : rowDetail.orderValues) {
					pdfTableContentCell.setPhrase(new Paragraph(cellValue, this.getChinese14Font(false)));
					pdfTable.addCell(pdfTableContentCell);
				}
				//打印商品
				if(CollectionUtils.isEmpty(rowDetail.orderItemValues)){
					continue;
				}
				for(final String[] subItems : rowDetail.orderItemValues)
				{
					if (needSequenceNumber) {
						pdfTableContentCell
								.setPhrase(new Paragraph("", this.getChinese14Font(false)));
						pdfTable.addCell(pdfTableContentCell);
					}
					for (final String cellValue : subItems) {
						pdfTableContentCell.setPhrase(new Paragraph(cellValue, this.getChinese14Font(false)));
						pdfTable.addCell(pdfTableContentCell);
					}
				}
			}
			// 表格内容每写满某个数字的行数时，其内容一方面写入物理文件，另一方面释放内存中存留的内容。
			this.pdfDocument.add(pdfTable);
			this.pdfTable.deleteBodyRows();
			return true;
		} catch (Exception ex) {
			logger.error("fail to generate pdf report: " + ex.getMessage(), ex);
			return false;
		}
	}
	
	/******************
	 * 添加图片
	 * 
	 * @param image
	 * @return
	 */
	public synchronized boolean addPDFDocumentImage(final byte[] onePageImage) {
		try {
			pdfDocument.newPage();
			pdfDocument.setPageSize(pageSize);
			final Image image = Image.getInstance(onePageImage);
			if (image.getWidth() > pageSize.getWidth()) {
				image.scalePercent(pageSize.getWidth() / image.getWidth() * 100);
			} else if (image.getHeight() * (pageSize.getWidth() / image.getWidth()) > pageSize.getHeight()) {
				image.scalePercent(pageSize.getHeight() / image.getHeight() * 100);
			}
			image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_MIDDLE);
			// image.scalePercent(60);//依照比例缩放，如50即为原图的50%
			// image.scaleToFit(this.pageSize.getHeight(),
			// this.pageSize.getWidth());
			pdfDocument.add(image);
			return true;
		} catch (Exception ex) {
			logger.error("Fail to add picture to document, ex=" + ex.getMessage(), ex);
			return false;
		}
	}

	/*****************
	 * 完成文档的生成， 并关闭流
	 */
	public synchronized void endPDFDocument(final OutputStream pdfOutputStream) {
		if (pdfDocument != null) {
			pdfDocument.close();
		}
		if (pdfOutputStream != null) {
			try {
				pdfOutputStream.flush();
				pdfOutputStream.close();
			} catch (Exception e) {
				logger.error("Fail to close pdfOutputStream, err=" + e.getMessage(), e);
			}
		}
	}

	/*********
	 * 获取中文基础字体
	 * 
	 * @return
	 * @throws Exception
	 */
	private BaseFont getChineseBaseFont() throws Exception {
		if (chineseBaseFont == null) {
			chineseBaseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			// FontFactory.HELVETICA, "UTF-8",
		}
		return chineseBaseFont;
	}

	/**
	 * 获取中文字符集且是8号字体，常用作表格页眉页脚的字体格式
	 * 
	 * @param fullFilePath
	 */
	public final Font getChinese8Font() throws Exception {
		if (pdf8Font == null) {
			// 设置中文字体和字体样式
			pdf8Font = new Font(this.getChineseBaseFont(), 10, Font.NORMAL);
		}
		return pdf8Font;
	}

	/**
	 * 获取中文字符集且是12号字体，常用作表格内容的字体格式
	 * 
	 * @param fullFilePath
	 */
	public final Font getChinese14Font(final boolean isBold) throws Exception {
		if (isBold) {
			if (pdf12BoldFont == null) { // 设置中文字体和字体样式
				pdf12BoldFont = new Font(this.getChineseBaseFont(), 14, Font.BOLD);
			}
			return pdf12BoldFont;
		} else {
			if (pdf12Font == null) { // 设置中文字体和字体样式
				pdf12Font = new Font(this.getChineseBaseFont(), 14, Font.NORMAL);
			}
			return pdf12Font;
		}

	}

	/**
	 * 获取中文字符集且是8号字体，常用作文字水印信息
	 * 
	 * @param fullFilePath
	 */
	public final Font getTextWaterFont() throws Exception {
		if (pdfTextWaterFont == null) {
			// 设置中文字体和字体样式
			pdfTextWaterFont = new Font(this.getChineseBaseFont(), 20, Font.BOLD, new BaseColor(240, 240, 240));
		}
		return pdfTextWaterFont;
	}

	/**
	 * 为PDF分页时创建添加文本水印的事件信息
	 */
	public final class TextWaterMarkPdfPageEvent extends PdfPageEventHelper {

		private final String waterMarkText; // 水印文本

		public TextWaterMarkPdfPageEvent(String waterMarkText) {
			this.waterMarkText = waterMarkText;
		}

		public void onEndPage(PdfWriter writer, Document document) {
			try {
				float pageWidth = document.right() + document.left();// 获取pdf内容正文页面宽度
				float pageHeight = document.top() + document.bottom();// 获取pdf内容正文页面高度
				// 设置水印字体格式
				PdfContentByte waterMarkPdfContent = writer.getDirectContentUnder();
				Phrase phrase = new Phrase(waterMarkText, getTextWaterFont());
				ColumnText.showTextAligned(waterMarkPdfContent, Element.ALIGN_CENTER, phrase, pageWidth * 0.25f,
						pageHeight * 0.2f, 45);
				ColumnText.showTextAligned(waterMarkPdfContent, Element.ALIGN_CENTER, phrase, pageWidth * 0.25f,
						pageHeight * 0.5f, 45);
				ColumnText.showTextAligned(waterMarkPdfContent, Element.ALIGN_CENTER, phrase, pageWidth * 0.25f,
						pageHeight * 0.8f, 45);
				ColumnText.showTextAligned(waterMarkPdfContent, Element.ALIGN_CENTER, phrase, pageWidth * 0.65f,
						pageHeight * 0.2f, 45);
				ColumnText.showTextAligned(waterMarkPdfContent, Element.ALIGN_CENTER, phrase, pageWidth * 0.65f,
						pageHeight * 0.5f, 45);
				ColumnText.showTextAligned(waterMarkPdfContent, Element.ALIGN_CENTER, phrase, pageWidth * 0.65f,
						pageHeight * 0.8f, 45);
			} catch (Exception ex) {
				logger.error("pdf watermark font: " + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * 为PDF分页时创建添加图片水印的事件信息
	 */
	public final class PictureWaterMarkPdfPageEvent extends PdfPageEventHelper {

		private final String waterMarkFullFilePath;
		private Image waterMarkImage;

		public PictureWaterMarkPdfPageEvent(String waterMarkFullFilePath) {
			this.waterMarkFullFilePath = waterMarkFullFilePath;
		}

		public void onEndPage(PdfWriter writer, Document document) {
			try {
				float pageWidth = document.right() + document.left();// 获取pdf内容正文页面宽度
				float pageHeight = document.top() + document.bottom();// 获取pdf内容正文页面高度
				PdfContentByte waterMarkPdfContent = writer.getDirectContentUnder();
				// 仅设置一个图片实例对象，整个PDF文档只应用一个图片对象，极大减少因为增加图片水印导致PDF文档大小增加
				if (waterMarkImage == null) {
					waterMarkImage = Image.getInstance(waterMarkFullFilePath);
				}
				// 添加水印图片，文档正文内容采用横向三列，竖向两列模式增加图片水印
				waterMarkPdfContent.addImage(getWaterMarkImage(waterMarkImage, pageWidth * 0.2f, pageHeight * 0.1f));
				waterMarkPdfContent.addImage(getWaterMarkImage(waterMarkImage, pageWidth * 0.2f, pageHeight * 0.4f));
				waterMarkPdfContent.addImage(getWaterMarkImage(waterMarkImage, pageWidth * 0.2f, pageHeight * 0.7f));
				waterMarkPdfContent.addImage(getWaterMarkImage(waterMarkImage, pageWidth * 0.6f, pageHeight * 0.1f));
				waterMarkPdfContent.addImage(getWaterMarkImage(waterMarkImage, pageWidth * 0.6f, pageHeight * 0.4f));
				waterMarkPdfContent.addImage(getWaterMarkImage(waterMarkImage, pageWidth * 0.6f, pageHeight * 0.7f));
				PdfGState gs = new PdfGState();
				gs.setFillOpacity(0.2f);// 设置透明度为0.2
				waterMarkPdfContent.setGState(gs);
			} catch (Exception ex) {
				logger.error("PictureWaterMarkPdfPageEvent error, err=" + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * 为PDF分页时创建添加header和footer信息的事件信息
	 */
	public final class HeadFootInfoPdfPageEvent extends PdfPageEventHelper {

		private BaseFont font;

		protected BaseFont getBaseFont() throws Exception {
			if (font == null) {
				font = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			}
			return font;
		}

		public void onEndPage(PdfWriter writer, Document document) {
			if (!needPageFoot) {
				return;
			}
			try {
				PdfContentByte headAndFootPdfContent = writer.getDirectContent();
				headAndFootPdfContent.saveState();
				headAndFootPdfContent.beginText();
				headAndFootPdfContent.setFontAndSize(this.getBaseFont(), 10);
				final String pageInformation = "第" + writer.getPageNumber() + "页";
				// 文档页头信息设置
				float x = document.top(-20);
				// 页头信息左面
				headAndFootPdfContent
						.showTextAligned(PdfContentByte.ALIGN_LEFT, pageInformation, document.left(), x, 0);
				// 页头信息中间
				headAndFootPdfContent.showTextAligned(PdfContentByte.ALIGN_CENTER, author,
						(document.right() + document.left()) / 2, x, 0);
				// 页头信息右面
				headAndFootPdfContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, pageInformation, document.right(), x,
						0);
				// 文档页脚信息设置
				float y = document.bottom(-20);
				// 页脚信息左面
				headAndFootPdfContent
						.showTextAligned(PdfContentByte.ALIGN_LEFT, pageInformation, document.left(), y, 0);
				// 页脚信息中间
				headAndFootPdfContent.showTextAligned(PdfContentByte.ALIGN_CENTER, author,
						(document.right() + document.left()) / 2, y, 0);
				// 页脚信息右面
				headAndFootPdfContent.showTextAligned(PdfContentByte.ALIGN_RIGHT, pageInformation, document.right(), y,
						0);
				headAndFootPdfContent.endText();
				headAndFootPdfContent.restoreState();
			} catch (Exception ex) {
				logger.error("HeadFootInfoPdfPageEvent error, err=" + ex.getMessage(), ex);
			}
		}
	}
}