package com.pge.dataload.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.pge.dataload.utils.DBConnector;
import com.pge.dataload.utils.Utilities;
import com.pge.dataload.vo.DocumentDetails;
import com.pge.dataload.vo.InnerLinkDetails;

public class PrintInnerLinkReportsImpl {
	
	private static Logger logger = Logger.getLogger(PrintInnerLinkReportsImpl.class);
	
	public static void main(String[] args) {
		// initialize loggers
		File f = new File(PrintInnerLinkReportsImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		PropertyConfigurator.configure(f.getParentFile().getAbsolutePath()+"/log.properties");
		f=  null;
		try
		{
			Connection conn = DBConnector.getDestinationConnection();
			String sql = "SELECT B.KA_DOCUMENT_ID,A.* FROM inline_inrlnk_details A LEFT OUTER JOIN DOCUMENT_DETAILS B ON A.VA_TYPE_ID=B.VA_TYPE_ID where (A.REC_CREATION_TMSTP>='2022-06-20' or A.REC_MODIFIED_TMSPT>='2022-06-20')  ORDER BY A.ID ASC ";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			String headers="KA DOCUMENT ID,VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TAG_CONTENT,VA_INRLNK_TYPE,VA_INRLNK_TITLE,VA_INRLNK_TYPE_ID,"
					+ "VA_HREF_VALUE,KA_ANSWER_ID,KA_INRLNK_URL,KA_PROCESSING_STATUS,KA_ERROR_MESSAGE";
			InnerLinkDetails details = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			List<InnerLinkDetails> list = new ArrayList<InnerLinkDetails>();
			while(rs.next())
			{
				details = new InnerLinkDetails();
				details.setDocumentDetails(new DocumentDetails());
				details.getDocumentDetails().setKaDocumentId(rs.getString("KA_DOCUMENT_ID"));
				details.getDocumentDetails().setTypeArticleId(rs.getString("VA_TYPE_ID"));
				details.getDocumentDetails().setDynamicEntityId(rs.getString("VA_DYNAMIC_ENTITY_ID"));
				details.getDocumentDetails().setLocale(rs.getString("VA_LOCALE"));
				details.getDocumentDetails().setChannelRegion(rs.getString("VA_REG_CHANNEL"));
				details.setTagContent(rs.getString("VA_TAG_CONTENT"));
				details.setInnerLinkType(rs.getString("VA_INRLNK_TYPE"));
				details.setInnerLinkTitle(rs.getString("VA_INRLNK_TITLE"));
				details.setInnerLinkTypeId(rs.getString("VA_INRLNK_TYPE_ID"));
				details.setHrefAttributeValue(rs.getString("VA_HREF_VALUE"));
				details.setKaDocumentId(rs.getString("KA_DOCUMENT_ID"));
				details.setKaInnerLinkURL(rs.getString("KA_INRLK_URL"));
				details.setProcessingStatus(rs.getString("KA_PROCESSING_STATUS"));
				details.setErrorMessage(rs.getString("KA_ERROR_MESSAGE"));
				list.add(details);
				details = null;
			}
			
			rs.close();rs= null;
			stmt.close();stmt=null;
			conn.close();conn = null;
			sql  =null;
			
			if(null!=list && list.size()>0)
			{
				printReports(list, headers);
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(PrintInnerLinkReportsImpl.class.getName(), "main()", e);
		}
	}

	private static void printReports(List<InnerLinkDetails> list, String headers)
	{
		try
		{
			logger.info("printReports :: InnerLinks List : >"+ list.size());
			if(null!=list && list.size()>0)
			{
				String path = "\\\\jwtcvpxprf02\\profiles\\E77645\\Downloads\\PGE_WD\\Reports_PROD_DELTA";
				String fName = "\\INLINE_INRLNK_DETAILS.xlsx";

				File myFile = new File(path + fName);
				fName = null;
				// Create the workbook instance for XLSX file, KEEP 100 ROWS IN MEMMORY AND RET ON DISK
				@SuppressWarnings("resource")
				SXSSFWorkbook myWorkBook = new SXSSFWorkbook(100);

				// Create a new sheet
				Sheet mySheet = myWorkBook.createSheet("Details");
				/*
				 * Add Header Row
				 */
				Row headerRow = mySheet.createRow(0);
				Cell headerCell = null;

				String[] tokens=headers.split(",");
				if(null!=tokens && tokens.length>0)
				{
					for(int a=0;a<tokens.length;a++)
					{
						headerCell = headerRow.createCell(a);
						headerCell.setCellValue(tokens[a].replace("_", " "));
						headerCell  = null;
					}
				}
				tokens = null;
				headerCell=null;
				headerRow=null;
				headers=null;

				int rowCount = 0;
				/*
				 * GENERATE MULTIPLE ROWS SUCH THAT
				 */
				String dataRow="";
				Row row=null;
				Cell dataCell=null;

				/*
				 * KA_DOCUMENT_ID,VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TAG_CONTENT,VA_INRLNK_TYPE,VA_INRLNK_TITLE,VA_INRLNK_TYPE_ID,
					VA_HREF_VALUE,KA_ANSWER_ID,KA_INRLNK_URL,KA_PROCESSING_STATUS,KA_ERROR_MESSAGE
				 */
				InnerLinkDetails details = null;
				for(int a=0;a<list.size();a++)
				{
					details = (InnerLinkDetails)list.get(a);
					dataRow=details.getDocumentDetails().getKaDocumentId()+"<TOK_SEPARATOR>"+details.getDocumentDetails().getTypeArticleId()+"<TOK_SEPARATOR>"+details.getDocumentDetails().getDynamicEntityId()+"<TOK_SEPARATOR>"+details.getDocumentDetails().getLocale()+"<TOK_SEPARATOR>";
					dataRow+=details.getDocumentDetails().getChannelRegion()+"<TOK_SEPARATOR>"+details.getTagContent()+"<TOK_SEPARATOR>";
					dataRow+=details.getInnerLinkType()+"<TOK_SEPARATOR>"+details.getInnerLinkTitle()+"<TOK_SEPARATOR>";
					dataRow+=details.getInnerLinkTypeId()+"<TOK_SEPARATOR>"+details.getHrefAttributeValue()+"<TOK_SEPARATOR>";
					dataRow+=details.getKaDocumentId()+"<TOK_SEPARATOR>"+details.getKaInnerLinkURL()+"<TOK_SEPARATOR>"+details.getProcessingStatus()+"<TOK_SEPARATOR>";
					dataRow+=details.getErrorMessage();

					// increment rowCount by 1
					rowCount++;
					// Create a new Row
					row = mySheet.createRow(rowCount);
					tokens = dataRow.split("<TOK_SEPARATOR>");
					if(null!=tokens && tokens.length>0)
					{
						for(int e=0;e<tokens.length;e++)
						{
							dataCell = row.createCell(e);
							dataCell.setCellValue("");
							if(null!=tokens[e] && !"".equals(tokens[e]) && !"null".equals(tokens[e].trim().toLowerCase()))
							{
								dataCell.setCellValue(tokens[e].trim());
							}
							dataCell =null;
						}
					}
					tokens = null;
					row=null;
					dataRow = null;
					dataCell = null;

					details = null;
				}

				headerRow = null;

				FileOutputStream os = new FileOutputStream(myFile);
				myWorkBook.write(os);
				logger.info("Writing on DOCUMENT REPORT XLSX file Finished ...");
				os.flush();
				os.close();

				// set mySheet to null
				mySheet = null;
				// set myWorkBook to null
				myWorkBook = null;
				// set path to null
				path = null;
				// set myFile to null
				myFile = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(PrintInnerLinkReportsImpl.class.getName(),"printReports()" , e);
		}
	}

	
}
