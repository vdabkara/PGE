package com.pge.dataload.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONObject;

import com.pge.dataload.dao.DestinationDocumentsDataDAO;
import com.pge.dataload.utils.Utilities;
import com.pge.dataload.vo.DocumentDetails;

public class Test {

	private static Logger logger = Logger.getLogger(Test.class);
	
	public static void main(String[] args) {
		String srcValue = "https://csokm.corp.dom/GTConnect/sso/PGE.Main?gtxResource=/asset/images/rcat_finesse_unparktask_072618_Time1532626688308.JPG&gtxResourceFileName=undefined&mode=download";
		String gtxResValue = srcValue.substring(srcValue.toLowerCase().indexOf("gtxResource=".toLowerCase())+"gtxResource=".length()+1, srcValue.length());
		System.out.println( gtxResValue);
		if(null!=gtxResValue && !"".equals(gtxResValue))
		{
			if(gtxResValue.lastIndexOf("/")!=-1)
			{
				gtxResValue = gtxResValue.substring(gtxResValue.lastIndexOf("/")+1, gtxResValue.indexOf("&"));
			}
			else
			{
				gtxResValue = gtxResValue.substring(0, gtxResValue.indexOf("&"));
			}
		}
		System.out.println("--- name :: >"+ gtxResValue);
		
		String name="Direct_Access_-_Historical_Usage_Authorization_and_Request_Process_Flow_Diagram_Time1521479914262.pdf";
		String ext = null;
		if(name.length()>100)
		{
			if(name.lastIndexOf(".")!=-1)
			{
				ext = name.substring(name.lastIndexOf("."),name.length());
				name = name.substring(0, name.lastIndexOf("."));
				//now trim
				name =name.substring(0,(100 - ext.length()));
				// add ext
				name = name+ext;
				ext = null;
			}
		}
		System.out.println(name+" :: >"+ name.length());
		
		
		
		
		
//		// initialize loggers
		File f = new File(StartDataLoadImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		System.err.println(f.getParentFile().getAbsolutePath());
//		PropertyConfigurator.configure(f.getParentFile().getAbsolutePath()+"/log.properties");
//		f=  null;
//
//		DestinationDocumentsDataDAO dao = new DestinationDocumentsDataDAO();
//		List<DocumentDetails> list = dao.getDocumentsDataForReport();
//		printReports(list);
//		list = null;
	}
	
	public static void main_1(String[] args) {
		// initialize loggers
				File f = new File(StartDataLoadImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
				PropertyConfigurator.configure(f.getParentFile().getAbsolutePath()+"/log.properties");
				f=  null;
				String contentxml = getContentJSON();
		JSONObject response_object = new JSONObject();
		logger.info("createContentWithFiles called with content xml " + contentxml);
		try {
			List<File> uploadFiles = new ArrayList<File>();
			File file = new File("C:\\Users\\Vishal\\Downloads\\Work_Expectations_2020_Time1598931439674.docx");
			uploadFiles.add(file);
			file = null;
			String urlStr = "https://portlandgeneral--tst1-irs.custhelp.com/km/api/latest/content";
			logger.info("connecting to content url  " + urlStr);
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpRequest = new HttpPost(urlStr);
			String authToken = "{\"siteName\":\"portlandgeneral__tst1\",\"localeId\":\"en_US\",\"integrationUserToken\":\"cG9ydGxhbmRnZW5lcmFsX190c3QxOm5wZjZLUnVLT2toV1hWVEN1RHVKaEtPV3NhRkg1K0FQNjhtanhMMDd4NzZFdzJSQ3NCOEdFTDFINlJJRHRDZWpUY002SlhGWURaRTh3UU5Db2Z0b2VGbkFQdWp4Z0VpSTk1c3NnNGJySEs1Q0I2QWd3aTIwR2x2TDh3bS84UlJEMG0yNzU5L3h0dDlBY29FWlBITDByUWxFR21ialRRaW1mWnVqRUQyb1YxMD0=\",\"userToken\":\"OH6ZTKb/87eYji6Hldtso88BIesSsXUpMYAYza2TesXzCznkLLO2JU1nx2ooGueeq20XBvEE0HoE+pcFdMFYXOpe3/xJVBJ/Y97b2GlPr1SMqjjqVrbuMzvWSdg4zc9FcUXRbAq90YLje2lQ36WVPw==\"}";
			if (authToken != null)
				httpRequest.addHeader("kmauthtoken", authToken);
			if (uploadFiles.size() > 0) {
				logger.info("create content call execution WITH file attachments");
				httpRequest.addHeader("Accept", "application/json");
				httpRequest.addHeader("Connection", "keep-alive");
				httpRequest.addHeader("Accept-Charset", "UTF-8");
				MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
				for (File uploadFile : uploadFiles) {
					multiPartEntityBuilder.setCharset(Charset.forName("UTF-8"));
					StringBody sBody = new StringBody(contentxml, ContentType.APPLICATION_JSON);
					multiPartEntityBuilder.addPart("contentBO", sBody);
					if (logger.isDebugEnabled())
						logger.info("Content.createContentWithFiles Uploading file " + uploadFile.getAbsolutePath());
					FileBody fileBody = new FileBody(uploadFile);
					multiPartEntityBuilder.addPart("filesToUpload", fileBody);
				}
				HttpEntity reqEntity = multiPartEntityBuilder.build();
					logger.info("Content.createContentWithFiles entity with files = " + reqEntity.toString());
				httpRequest.setEntity(reqEntity);
			} else {
				logger.info("create content call execution WITHOUT file attachments");
				httpRequest.addHeader("Accept", "application/json");
				httpRequest.addHeader("Connection", "keep-alive");
				httpRequest.addHeader("Content-Type", "application/json");
				httpRequest.addHeader("Accept-Charset", "UTF-8");
				httpRequest.setEntity(new StringEntity(contentxml));
			}
				logger.info("Content.createContentWithFiles entity with http_request = " + httpRequest.toString());

			HttpResponse http_response = httpClient.execute(httpRequest);
			int statusCode = http_response.getStatusLine().getStatusCode();
			logger.info("create content service response code " + statusCode);
			HttpEntity entity = http_response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
				logger.info("Content.createContentWithFiles responseString " + responseString);
		} catch (Exception ex) {
			logger.info("Exception while saving content : \n", ex);
		}
		logger.info("create content service returning json response  " + response_object);
	}

	
	private static String getContentXML()
	{
		String contentXML="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<Content>\r\n" + 
				"   <locale>\r\n" + 
				"      <recordId>en_US</recordId>\r\n" + 
				"   </locale>\r\n" + 
				"   <contentType>\r\n" + 
				"      <recordId>01024103f390160017eeb4e336e007fe6</recordId>\r\n" + 
				"      <referenceKey>ALERT</referenceKey>\r\n" + 
				"      <name>Alert</name>\r\n" + 
				"   </contentType>\r\n" + 
				"   <owner>\r\n" + 
				"      <recordId>6BAAE06AE6204288913AD52DDBFD5B1E</recordId>\r\n" + 
				"   </owner>\r\n" + 
				"   <published>false</published>\r\n" + 
				"   <displayPosition>HISTORICAL_USEFULNESS</displayPosition>\r\n" + 
				"   <displayStartDate>2022-02-18T06:24:00-0500</displayStartDate>\r\n" + 
				"   <displayEndDate>9999-12-31T00:00:00-0500</displayEndDate>\r\n" + 
				"   <suppressNotification>false</suppressNotification>\r\n" + 
				"   <xml>\r\n" + 
				"      <ALERT><TITLE><![CDATA[Testing Document From API with Attachment]]></TITLE><BACKGROUND><![CDATA[Some background value]]></BACKGROUND><DETAILS><CONTENT_HEADER><![CDATA[Link with other article ID]]></CONTENT_HEADER><DESCRIPTION><![CDATA[Desc]]></DESCRIPTION></DETAILS><QUESTIONS><![CDATA[question value]]></QUESTIONS><ATTACHMENT><FILE_ATTACHMENT SIZE=\"38912\"><![CDATA[Work_Expectations_2020_Time1598931439674.docx]]></FILE_ATTACHMENT></ATTACHMENT></ALERT>\r\n" + 
				"   </xml>\r\n" + 
				"   <metaDataXml>\r\n" + 
				"      <META />\r\n" + 
				"   </metaDataXml>\r\n" + 
				"   <isForEdit>true</isForEdit>\r\n" + 
				"   <categories>\r\n" + 
				"		<CategoryKey>\r\n" + 
				"			<recordId>093E51C00A994051A339F3B8BC021CE6</recordId>\r\n" + 
				"			<referenceKey>RN_CATEGORY_52</referenceKey>\r\n" + 
				"			<name>Close</name>\r\n" + 
				"		</CategoryKey>\r\n" + 
				"		<CategoryKey>\r\n" + 
				"			<recordId>DB0EDD4B362B496DA237D095F2BA9F02</recordId>\r\n" + 
				"			<referenceKey>RN_PRODUCT_76</referenceKey>\r\n" + 
				"			<name>Energy Efficiency Audits</name>\r\n" + 
				"		</CategoryKey>\r\n" + 
				"	</categories>\r\n" + 
				"	<userGroups>\r\n" + 
				"      <UserGroupKey>\r\n" + 
				"         <recordId>0102390606bbdde017ea3f68f7b007ef4</recordId>\r\n" + 
				"         <referenceKey>RN_ACCESS_LEVEL_4</referenceKey>\r\n" + 
				"         <name>Billing</name>\r\n" + 
				"	  </UserGroupKey>	 \r\n" + 
				"	</userGroups>\r\n" + 
				"   <views>\r\n" + 
				"      <ViewKey>\r\n" + 
				"         <recordId>0102390606bbdde017ea3f68f7b007f03</recordId>\r\n" + 
				"         <referenceKey>PGE</referenceKey>\r\n" + 
				"         <name>PGE</name>\r\n" + 
				"      </ViewKey>\r\n" + 
				"   </views>\r\n" + 
				"</Content>";
		return contentXML;
	}

	private static String getContentJSON()
	{
		String json="{\"locale\": {\"recordId\": \"en_US\"},\"contentType\": {\"recordId\":\"01024103f390160017eeb4e336e007fe6\", \"referenceKey\":\"ALERT\", \"name\":\"Alert\"},\"displayStartDate\": \"2022-02-18T06:24:00-0500\",\"displayEndDate\": \"9999-12-31T00:00:00-0500\",\"owner\": {\"recordId\": \"6BAAE06AE6204288913AD52DDBFD5B1E\"},\"published\": false,\"suppressNotification\": false,\"xml\": \"<ALERT><TITLE><![CDATA[Testing Document From API]]></TITLE><BACKGROUND><![CDATA[Some background value]]></BACKGROUND><DETAILS><CONTENT_HEADER><![CDATA[Link with other article ID]]></CONTENT_HEADER><DESCRIPTION><![CDATA[Desc]]></DESCRIPTION></DETAILS><QUESTIONS><![CDATA[question value]]></QUESTIONS><ATTACHMENT><FILE_ATTACHMENT SIZE=\\\"38912\\\"><![CDATA[Work_Expectations_2020_Time1598931439674.docx]]></FILE_ATTACHMENT></ATTACHMENT></ALERT>\",\"metaDataXml\": \"<META/>\",\"isForEdit\": true,\"categories\": [{ \"recordId\": \"27F95D78C19D471191FAF1CA143E83B8\",\"referenceKey\": \"RN_CATEGORY_68\",\"name\": \"Reports\"}],\"userGroups\": [{ \"referenceKey\": \"RN_ACCESS_LEVEL_4\",\"recordId\":\"0102390606bbdde017ea3f68f7b007ef4\",\"name\":\"Billing\"}],\"views\": [{ \"recordId\":\"0102390606bbdde017ea3f68f7b007f03\", \"referenceKey\":\"PGE\", \"name\":\"PGE\"}] }";
		return json;
	}
	
	private static void printReports(List<DocumentDetails> documentsList)
	{
		try
		{
			if(null!=documentsList && documentsList.size()>0)
			{
				String path = "//jwtcvpxprf02/profiles/E77645/Downloads/PGE_WD/Reports";
				String fName = "/DOCUMENT_DETAILS.xlsx";

				File myFile = new File(path + fName);
				fName = null;
				// Create the workbook instance for XLSX file, KEEP 100 ROWS IN MEMMORY AND RET ON DISK
				@SuppressWarnings("resource")
				SXSSFWorkbook myWorkBook = new SXSSFWorkbook(100);

				// Create a new sheet
				Sheet mySheet = myWorkBook.createSheet("Images_Details");
				/*
				 * Add Header Row
				 */
				Row headerRow = mySheet.createRow(0);
				Cell headerCell = null;

				String headers="VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TITLE,VA_PUB_START_DATE,VA_PUB_END_DATE,VA_PUB_BY,VA_OWNER_ID,VA_LAST_MOD_DATE,VA_LAST_MOD_BY,VA_MAJOR_VERSION,VA_MINOR_VERSION,VA_WEIGHT_VALUE,KA_CONTENT_PATTERN,KA_CHANNEL_REF_KEY,KA_RECORD_ID,KA_DOCUMENT_ID,KA_ARTICLE_ID,KA_VERSION_ID,KA_VERSION,KA_PUB_STATUS,ALL_UG_MAPPED,ALL_CAT_MAPPED,ALL_PROD_MAPPED,ALL_INNERLINKS_MAPPED,ALL_IMAGES_MAPPED,ALL_REL_LINKS_MAPPED,ALL_ATTACHMENTS_MAPPED,KA_PROCESSING_STATUS,KA_ERROR_MESSAGE,KA_DATA_ERRORS";
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

				DocumentDetails details = null;
				for(int a=0;a<documentsList.size();a++)
				{
					details = (DocumentDetails)documentsList.get(a);
					dataRow=details.getTypeArticleId()+"<TOK_SEPARATOR>"+details.getDynamicEntityId()+"<TOK_SEPARATOR>"+details.getLocale()+"<TOK_SEPARATOR>";
					dataRow+=details.getChannelRegion()+"<TOK_SEPARATOR>"+details.getTitle()+"<TOK_SEPARATOR>"+details.getPublishStartDate()+"<TOK_SEPARATOR>";
					dataRow+=details.getPublishEndDate()+"<TOK_SEPARATOR>"+details.getPublishedBy()+"<TOK_SEPARATOR>"+details.getOwnerId()+"<TOK_SEPARATOR>";
					dataRow+=details.getLastModifiedDate()+"<TOK_SEPARATOR>"+details.getLastModifiedBy()+"<TOK_SEPARATOR>";
					dataRow+=details.getMajorVersion()+"<TOK_SEPARATOR>"+details.getMinorVersion()+"<TOK_SEPARATOR>"+details.getWeightValue()+"<TOK_SEPARATOR>";
					dataRow+=details.getKaContentPattern()+"<TOK_SEPARATOR>"+details.getKaChannelRefKey()+"<TOK_SEPARATOR>";
					
					dataRow+=details.getKaRecordId()+"<TOK_SEPARATOR>"+details.getKaDocumentId()+"<TOK_SEPARATOR>"+details.getKaArticleId()+"<TOK_SEPARATOR>";
					dataRow+=details.getKaVersionId()+"<TOK_SEPARATOR>"+details.getKaVersion()+"<TOK_SEPARATOR>";
					dataRow+=details.getKaPublishStatus()+"<TOK_SEPARATOR>"+details.getAllUserGroupsMapped()+"<TOK_SEPARATOR>"+details.getAllCategoriesMapped()+"<TOK_SEPARATOR>";
					dataRow+=details.getAllProductsMapped()+"<TOK_SEPARATOR>"+details.getAllInnerLinksMapped()+"<TOK_SEPARATOR>";
					dataRow+=details.getAllImagesMapped()+"<TOK_SEPARATOR>"+details.getAllRelatedLinksMapped()+"<TOK_SEPARATOR>"+details.getAllAttachmentsMapped()+"<TOK_SEPARATOR>";
					dataRow+=details.getProcessingStatus()+"<TOK_SEPARATOR>"+details.getErrorMessage()+"<TOK_SEPARATOR>"+details.getDataErrors();

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
			Utilities.printStackTraceToLogs(Test.class.getName(),"printReports()" , e);
		}
	}
}
