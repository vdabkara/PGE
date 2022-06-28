package com.pge.dataload.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

public class PrintDocumentReportsImpl {
	
	private static Logger logger = Logger.getLogger(PrintDocumentReportsImpl.class);
	
	public static void main(String[] args) {
		// initialize loggers
		File f = new File(PrintDocumentReportsImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		PropertyConfigurator.configure(f.getParentFile().getAbsolutePath()+"/log.properties");
		f=  null;
		try
		{
			Connection conn = DBConnector.getDestinationConnection();
			String sql = "SELECT *, CAST(KA_ERROR_MESSAGE AS CHAR(8000)) AS OSVC_ERRORS FROM DOCUMENT_DETAILS where (REC_CREATION_TMSTP>='2022-06-20' or REC_MODIFIED_TMSPT>='2022-06-20') ";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			String headers="VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TITLE,VA_PUB_START_DATE,VA_PUB_END_DATE,VA_PUB_BY,VA_OWNER_ID,VA_LAST_MOD_DATE,VA_LAST_MOD_BY,VA_MAJOR_VERSION,VA_MINOR_VERSION,VA_WEIGHT_VALUE,KA_CONTENT_PATTERN,KA_CHANNEL_REF_KEY,KA_RECORD_ID,KA_DOCUMENT_ID,KA_ARTICLE_ID,KA_VERSION_ID,KA_VERSION,KA_PUB_STATUS,ALL_UG_MAPPED,ALL_CAT_MAPPED,ALL_PROD_MAPPED,ALL_INNERLINKS_MAPPED,ALL_IMAGES_MAPPED,ALL_REL_LINKS_MAPPED,ALL_ATTACHMENTS_MAPPED,KA_PROCESSING_STATUS,KA_ERROR_CODE,KA_DATA_ERRORS,OSVC_ERRORS";
			DocumentDetails details = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			List<DocumentDetails> list = new ArrayList<DocumentDetails>();
			while(rs.next())
			{
				details = new DocumentDetails();
				details.setTypeArticleId(rs.getString("VA_TYPE_ID"));
				details.setDynamicEntityId(rs.getString("VA_DYNAMIC_ENTITY_ID"));
				details.setLocale(rs.getString("VA_LOCALE"));
				details.setChannelRegion(rs.getString("VA_REG_CHANNEL"));
				details.setTitle(rs.getString("VA_TITLE"));
				details.setPublishStartDate(rs.getTimestamp("VA_PUB_START_DATE"));
				details.setPublishEndDate(rs.getTimestamp("VA_PUB_END_DATE"));
				details.setPublishedBy(rs.getString("VA_PUB_BY"));
				details.setOwnerId(rs.getString("VA_OWNER_ID"));
				details.setLastModifiedDate(rs.getTimestamp("VA_LAST_MOD_DATE"));
				details.setLastModifiedBy(rs.getString("VA_LAST_MOD_BY"));
				details.setMajorVersion(rs.getString("VA_MAJOR_VERSION"));
				details.setMinorVersion(rs.getString("VA_MINOR_VERSION"));
				details.setWeightValue(rs.getString("VA_WEIGHT_VALUE"));
				details.setKaContentPattern(rs.getString("KA_CONTENT_PATTERN"));
				details.setKaChannelRefKey(rs.getString("KA_CHANNEL_REF_KEY"));
				details.setKaRecordId(rs.getString("KA_RECORD_ID"));
				details.setKaDocumentId(rs.getString("KA_DOCUMENT_ID"));
				details.setKaArticleId(rs.getString("KA_ARTICLE_ID"));
				details.setKaVersionId(rs.getString("KA_VERSION_ID"));
				details.setKaVersion(rs.getString("KA_VERSION"));
				details.setKaPublishStatus(rs.getString("KA_PUB_STATUS"));
				details.setAllUserGroupsMapped(rs.getString("ALL_UG_MAPPED"));
				details.setAllCategoriesMapped(rs.getString("ALL_CAT_MAPPED"));
				details.setAllProductsMapped(rs.getString("ALL_PROD_MAPPED"));
				details.setAllInnerLinksMapped(rs.getString("ALL_INNERLINKS_MAPPED"));
				details.setAllImagesMapped(rs.getString("ALL_IMAGES_MAPPED"));
				details.setAllRelatedLinksMapped(rs.getString("ALL_REL_LINKS_MAPPED"));
				details.setAllAttachmentsMapped(rs.getString("ALL_ATTACHMENTS_MAPPED"));
				details.setProcessingStatus(rs.getString("KA_PROCESSING_STATUS"));
				details.setErrorCodes(rs.getString("KA_ERROR_CODE"));
				details.setDataErrors(rs.getString("KA_DATA_ERRORS"));
				details.setErrorMessage(rs.getString("OSVC_ERRORS"));
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
			Utilities.printStackTraceToLogs(PrintDocumentReportsImpl.class.getName(), "main()", e);
		}
	}

	private static void printReports(List<DocumentDetails> list, String headers)
	{
		try
		{
			logger.info("printReports :: Failure List : >"+ list.size());
			if(null!=list && list.size()>0)
			{
				String path = "\\\\jwtcvpxprf02\\profiles\\E77645\\Downloads\\PGE_WD\\Reports_PROD_DELTA";
				String fName = "\\DOCUMENT_DETAILS.xlsx";

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
				 * VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TITLE,VA_PUB_START_DATE,
				 * VA_PUB_END_DATE,VA_PUB_BY,VA_OWNER_ID,VA_LAST_MOD_DATE,VA_LAST_MOD_BY,VA_MAJOR_VERSION,
				 * VA_MINOR_VERSION,VA_WEIGHT_VALUE,
				 * KA_CONTENT_PATTERN,KA_CHANNEL_REF_KEY,KA_RECORD_ID,KA_DOCUMENT_ID,KA_ARTICLE_ID,
				 * KA_VERSION_ID,KA_VERSION,KA_PUB_STATUS,ALL_UG_MAPPED,ALL_CAT_MAPPED,ALL_PROD_MAPPED,
				 * ALL_INNERLINKS_MAPPED,ALL_IMAGES_MAPPED,ALL_REL_LINKS_MAPPED,ALL_ATTACHMENTS_MAPPED,KA_PROCESSING_STATUS,
				 * KA_ERROR_CODE,KA_DATA_ERRORS,OSVC_ERRORS
				 */
				DocumentDetails details = null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				for(int a=0;a<list.size();a++)
				{
					details = (DocumentDetails)list.get(a);
					dataRow=details.getTypeArticleId()+"<TOK_SEPARATOR>"+details.getDynamicEntityId()+"<TOK_SEPARATOR>"+details.getLocale()+"<TOK_SEPARATOR>";
					dataRow+=details.getChannelRegion()+"<TOK_SEPARATOR>"+details.getTitle()+"<TOK_SEPARATOR>";
					if(null!=details.getPublishStartDate())
					{
						dataRow+=sdf.format(details.getPublishStartDate());
					}
					dataRow+="<TOK_SEPARATOR>";
					if(null!=details.getPublishEndDate())
					{
						dataRow+=sdf.format(details.getPublishEndDate());
					}
					dataRow+="<TOK_SEPARATOR>";
					dataRow+=details.getPublishedBy()+"<TOK_SEPARATOR>"+details.getOwnerId()+"<TOK_SEPARATOR>";
					if(null!=details.getLastModifiedDate())
					{
						dataRow+=sdf.format(details.getLastModifiedDate());
					}
					dataRow+="<TOK_SEPARATOR>";
					dataRow+=details.getLastModifiedBy()+"<TOK_SEPARATOR>"+details.getMajorVersion()+"<TOK_SEPARATOR>";
					dataRow+=details.getMinorVersion()+"<TOK_SEPARATOR>"+details.getWeightValue()+"<TOK_SEPARATOR>"+details.getKaContentPattern()+"<TOK_SEPARATOR>";
					dataRow+=details.getKaChannelRefKey()+"<TOK_SEPARATOR>"+details.getKaRecordId()+"<TOK_SEPARATOR>";
					dataRow+=details.getKaDocumentId()+"<TOK_SEPARATOR>"+details.getKaArticleId()+"<TOK_SEPARATOR>"+details.getKaVersionId()+"<TOK_SEPARATOR>";
					dataRow+=details.getKaVersion()+"<TOK_SEPARATOR>"+details.getKaPublishStatus()+"<TOK_SEPARATOR>";


					dataRow+=details.getAllUserGroupsMapped()+"<TOK_SEPARATOR>"+details.getAllCategoriesMapped()+"<TOK_SEPARATOR>";
					dataRow+=details.getAllProductsMapped()+"<TOK_SEPARATOR>"+details.getAllInnerLinksMapped()+"<TOK_SEPARATOR>"+details.getAllImagesMapped()+"<TOK_SEPARATOR>";
					dataRow+=details.getAllRelatedLinksMapped()+"<TOK_SEPARATOR>"+details.getAllAttachmentsMapped()+"<TOK_SEPARATOR>";
					dataRow+=details.getProcessingStatus()+"<TOK_SEPARATOR>"+details.getErrorCodes()+"<TOK_SEPARATOR>"+details.getDataErrors()+"<TOK_SEPARATOR>";
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
			Utilities.printStackTraceToLogs(PrintDocumentReportsImpl.class.getName(),"printReports()" , e);
		}
	}

	
}
