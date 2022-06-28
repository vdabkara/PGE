package com.pge.dataload.utils;

import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pge.dataload.vo.AttachmentDetails;
import com.pge.dataload.vo.DocumentDetails;
import com.pge.dataload.vo.SchemaFieldDetails;
import com.pge.dataload.vo.TagDetails;

public class ContentUtils {

	public static JSONObject createPayLoad(DocumentDetails details)
	{
		JSONObject payloadObj = new JSONObject();
		try
		{
			JSONObject obj = null;
			// add Locale Obj
			obj = new JSONObject();
			obj.put("recordId", details.getLocale().replace("-", "_"));
			payloadObj.put("locale", obj);
			obj = null;

			// add contentType Obj
			obj = new JSONObject();
			obj.put("recordId", details.getKaChannelRecordId());
			obj.put("referenceKey", details.getKaChannelRefKey());
			obj.put("name", details.getKaChannelName());
			payloadObj.put("contentType", obj);
			obj = null;

			/*
			 *  TO DO FOR OWNER
			 */

			// published
			payloadObj.put("published", false);
			// suppressNotification true
			payloadObj.put("suppressNotification", true);
			// SCHEMA XML 
			String xml = getChannelSchema(details);
			payloadObj.put("xml", xml);
			xml = null;
			// metaDataXml
			payloadObj.put("metaDataXml", JSONObject.NULL);
			// isForEdit
			payloadObj.put("isForEdit", true);

			TagDetails tags = null;
			JSONArray catArray = new JSONArray();
			JSONArray ugArray = new JSONArray();
			// Categories - Topics
			if(null!=details.getTopicsList() && details.getTopicsList().size()>0)
			{
				for(int a=0;a<details.getTopicsList().size();a++)
				{
					tags =  (TagDetails)details.getTopicsList().get(a);
					if(null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
							null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
							null!=tags.getKaItemName() && !"".equals(tags.getKaItemName()))
					{
						obj = new JSONObject();
						obj.put("recordId", tags.getKaRecordId());
						obj.put("referenceKey", tags.getKaItemRefKey());
						obj.put("name", tags.getKaItemName());
						catArray.put(obj);
						obj = null;
					}
					tags = null;
				}
			}
			// Products - Process Owners
			if(null!=details.getProductList() && details.getProductList().size()>0)
			{
				for(int a=0;a<details.getProductList().size();a++)
				{
					tags =  (TagDetails)details.getProductList().get(a);
					if(null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
							null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
							null!=tags.getKaItemName() && !"".equals(tags.getKaItemName()))
					{
						obj = new JSONObject();
						obj.put("recordId", tags.getKaRecordId());
						obj.put("referenceKey", tags.getKaItemRefKey());
						obj.put("name", tags.getKaItemName());
						catArray.put(obj);
						obj = null;
					}
					tags = null;
				}
			}
			// Departments - Category
			if(null!=details.getDepartmentCategoryList() && details.getDepartmentCategoryList().size()>0)
			{
				for(int a=0;a<details.getDepartmentCategoryList().size();a++)
				{
					tags =  (TagDetails)details.getDepartmentCategoryList().get(a);
					if(null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
							null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
							null!=tags.getKaItemName() && !"".equals(tags.getKaItemName()))
					{
						obj = new JSONObject();
						obj.put("recordId", tags.getKaRecordId());
						obj.put("referenceKey", tags.getKaItemRefKey());
						obj.put("name", tags.getKaItemName());
						catArray.put(obj);
						obj = null;
					}
					tags = null;
				}
			}
			// add Categories Node
			//			System.out.println("--- items size :: >"+ catArray.length());
			payloadObj.put("categories", catArray);
			tags = null;
			catArray = null;

			// User Groups
			/*
			 * IF ANY OF THE USER GROUP IS residential / Residential Services
			 * do not map User Groups
			 */
			ugArray = new JSONArray();
			boolean doNotMap = false;
			if(null!=details.getDepartmentUserGroupList() && details.getDepartmentUserGroupList().size()>0)
			{
				tags = null;
				for(int a=0;a<details.getDepartmentUserGroupList().size();a++)
				{
					tags =  (TagDetails)details.getDepartmentUserGroupList().get(a);
					if(null!=tags.getVaItemName() && (tags.getVaItemName().trim().toLowerCase().startsWith("residential")))
					{
						// do not map any user group
						// set items to blank
						ugArray = new JSONArray();
						doNotMap = true;
						break;
					}
					tags = null;
				}
			}

			if(doNotMap==false)
			{
				// proceed for mapping available UserGroups
				if(null!=details.getDepartmentUserGroupList() && details.getDepartmentUserGroupList().size()>0)
				{
					tags = null;
					for(int a=0;a<details.getDepartmentUserGroupList().size();a++)
					{
						tags =  (TagDetails)details.getDepartmentUserGroupList().get(a);
						if(null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
								null!=tags.getKaItemRefKey() && !"".equals(tags.getKaItemRefKey()) && 
								null!=tags.getKaItemName() && !"".equals(tags.getKaItemName()))
						{
							obj = new JSONObject();
							obj.put("recordId", tags.getKaRecordId());
							obj.put("referenceKey", tags.getKaItemRefKey());
							obj.put("name", tags.getKaItemName());
							ugArray.put(obj);
							obj = null;
						}
						tags = null;
					}
				}
			}

			// set userGroups
			payloadObj.put("userGroups", ugArray);
			ugArray=  null;


			// set Views
			JSONArray items = new JSONArray();
			obj = new JSONObject();
			obj.put("recordId", ApplicationProperties.getProperty("KA_VIEW_RECORD_ID"));
			obj.put("referenceKey", ApplicationProperties.getProperty("KA_VIEW_REF_KEY"));
			obj.put("name", ApplicationProperties.getProperty("KA_VIEW_NAME"));
			items.put(obj);
			obj = null;

			payloadObj.put("views", items);
			items = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentUtils.class.getName(), "createPayLoad()", e);
		}
		return payloadObj;
	}


	private static String getChannelSchema(DocumentDetails details)
	{
		String xml ="";
		if(details.getKaChannelRefKey().equals("ALERT"))
		{
			xml = alertChannelSchema(details);
		}
		else if(details.getKaChannelRefKey().equals("PROCEDURE"))
		{
			xml = procedureChannelSchema(details);
		}
		else if(details.getKaChannelRefKey().equals("JOB_AID"))
		{
			xml = jobAidChannelSchema(details);
		}
		else if(details.getKaChannelRefKey().equals("FAQ"))
		{
			xml = faqChannelSchema(details);
		}
		else if(details.getKaChannelRefKey().equals("REFERENCE"))
		{
			xml = referenceChannelSchema(details);
		}
		return xml;
	}

	private static String alertChannelSchema(DocumentDetails details)
	{
		String xml= "";
		try
		{
			StringBuilder str=  new StringBuilder();
			str.append("<"+details.getKaChannelRefKey()+">");
			// add TITLE
			str.append("<TITLE><![CDATA["+details.getTitle()+"]]></TITLE>");

			// add BACKGROUND
			if(null!=details.getSchemaDetails().getBackground() && !"".equals(details.getSchemaDetails().getBackground()))
			{
				str.append("<BACKGROUND><![CDATA[");
				str.append(details.getSchemaDetails().getBackground());
				str.append("]]></BACKGROUND>");
			}

			// add DETAILS NODE
			str = addDetailsNodeList(details, str);

			// add QUESTIONS
			if(null!=details.getSchemaDetails().getQuestion() && !"".equals(details.getSchemaDetails().getQuestion()))
			{
				str.append("<QUESTIONS><![CDATA[");
				str.append(details.getSchemaDetails().getQuestion());
				str.append("]]></QUESTIONS>");
			}
			// add ATTACHMENTS
			str = addAttachmentFields(details, str);

			// add KNOWLEDGE_ALERT_DESCRIPTION 
			if(null!=details.getSchemaDetails().getKnowledgeAlertDescription() && !"".equals(details.getSchemaDetails().getKnowledgeAlertDescription()))
			{
				str.append("<KNOWLEDGE_ALERT_DESCRIPTION><![CDATA[");
				str.append(details.getSchemaDetails().getKnowledgeAlertDescription());
				str.append("]]></KNOWLEDGE_ALERT_DESCRIPTION>");
			}

			/*
			 * ADD COMMON FIELD
			 */
			str = addCommonFields(details, str);

			str.append("</"+details.getKaChannelRefKey()+">");

			xml = str.toString();
			str = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentUtils.class.getName(), "alertChannelSchema()", e);
		}
		return xml;
	}

	private static String faqChannelSchema(DocumentDetails details)
	{
		String xml= "";
		try
		{
			StringBuilder str=  new StringBuilder();
			str.append("<"+details.getKaChannelRefKey()+">");
			// add TITLE
			str.append("<TITLE><![CDATA["+details.getTitle()+"]]></TITLE>");

			// add SUMMARY
			if(null!=details.getSchemaDetails().getSummary() && !"".equals(details.getSchemaDetails().getSummary()))
			{
				str.append("<SUMMARY><![CDATA[");
				str.append(details.getSchemaDetails().getSummary());
				str.append("]]></SUMMARY>");
			}

			// add QA NODE
			str = addQuestionAnswerNodeList(details, str);

			// add ATTACHMENTS
			str = addAttachmentFields(details, str);

			/*
			 * ADD COMMON FIELD
			 */
			str = addCommonFields(details, str);

			str.append("</"+details.getKaChannelRefKey()+">");

			xml = str.toString();
			str = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentUtils.class.getName(), "faqChannelSchema()", e);
		}
		return xml;
	}

	private static String jobAidChannelSchema(DocumentDetails details)
	{
		String xml= "";
		try
		{
			StringBuilder str=  new StringBuilder();
			str.append("<"+details.getKaChannelRefKey()+">");
			// add TITLE
			str.append("<TITLE><![CDATA["+details.getTitle()+"]]></TITLE>");

			// add SUMMARY
			if(null!=details.getSchemaDetails().getSummary() && !"".equals(details.getSchemaDetails().getSummary()))
			{
				str.append("<SUMMARY><![CDATA[");
				str.append(details.getSchemaDetails().getSummary());
				str.append("]]></SUMMARY>");
			}

			// add ATTACHMENTS
			str = addAttachmentFields(details, str);
			/*
			 * ADD COMMON FIELD
			 */
			str = addCommonFields(details, str);

			str.append("</"+details.getKaChannelRefKey()+">");

			xml = str.toString();
			str = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentUtils.class.getName(), "jobAidChannelSchema()", e);
		}
		return xml;
	}

	private static String procedureChannelSchema(DocumentDetails details)
	{
		String xml= "";
		try
		{
			StringBuilder str=  new StringBuilder();
			str.append("<"+details.getKaChannelRefKey()+">");
			// add TITLE
			str.append("<TITLE><![CDATA["+details.getTitle()+"]]></TITLE>");

			// add SUMMARY
			if(null!=details.getSchemaDetails().getSummary() && !"".equals(details.getSchemaDetails().getSummary()))
			{
				str.append("<SUMMARY><![CDATA[");
				str.append(details.getSchemaDetails().getSummary());
				str.append("]]></SUMMARY>");
			}

			// add HIGH_LEVEL_PROCESS
			if(null!=details.getSchemaDetails().getHighLevelProcess() && !"".equals(details.getSchemaDetails().getHighLevelProcess()))
			{
				str.append("<HIGH_LEVEL_PROCESS><![CDATA[");
				str.append(details.getSchemaDetails().getHighLevelProcess());
				str.append("]]></HIGH_LEVEL_PROCESS>");
			}

			// add DETAILED_PROCESS
			if(null!=details.getSchemaDetails().getDetailedProcess() && !"".equals(details.getSchemaDetails().getDetailedProcess()))
			{
				str.append("<DETAILED_PROCESS><![CDATA[");
				str.append(details.getSchemaDetails().getDetailedProcess());
				str.append("]]></DETAILED_PROCESS>");
			}

			// add DETAILS NODE
			str = addDetailsNodeList(details, str);

			// add ATTACHMENTS
			str = addAttachmentFields(details, str);

			/*
			 * ADD COMMON FIELD
			 */
			str = addCommonFields(details, str);

			str.append("</"+details.getKaChannelRefKey()+">");

			xml = str.toString();
			str = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentUtils.class.getName(), "procedureChannelSchema()", e);
		}
		return xml;
	}

	private static String referenceChannelSchema(DocumentDetails details)
	{
		String xml= "";
		try
		{
			StringBuilder str=  new StringBuilder();
			str.append("<"+details.getKaChannelRefKey()+">");
			// add TITLE
			str.append("<TITLE><![CDATA["+details.getTitle()+"]]></TITLE>");

			// add SUMMARY
			if(null!=details.getSchemaDetails().getSummary() && !"".equals(details.getSchemaDetails().getSummary()))
			{
				str.append("<SUMMARY><![CDATA[");
				str.append(details.getSchemaDetails().getSummary());
				str.append("]]></SUMMARY>");
			}

			// add HIGH_LEVEL_PROCESS
			if(null!=details.getSchemaDetails().getDetailsTextArea() && !"".equals(details.getSchemaDetails().getDetailsTextArea()))
			{
				str.append("<DETAILS><![CDATA[");
				str.append(details.getSchemaDetails().getDetailsTextArea());
				str.append("]]></DETAILS>");
			}

			// add DETAIL NODE
			str = addDetailsNodeList(details, str);

			// add ATTACHMENTS
			str = addAttachmentFields(details, str);

			/*
			 * ADD COMMON FIELD
			 */
			str = addCommonFields(details, str);

			str.append("</"+details.getKaChannelRefKey()+">");

			xml = str.toString();
			str = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentUtils.class.getName(), "procedureChannelSchema()", e);
		}
		return xml;
	}

	private static StringBuilder addCommonFields(DocumentDetails details, StringBuilder str)
	{
		// add VERINT_RELATED_CONTENT_LINK 
		if(null!=details.getRelatedLinkContent() && !"".equals(details.getRelatedLinkContent()))
		{
			str.append("<VERINT_RELATED_CONTENT_LINK><![CDATA[");
			str.append(details.getRelatedLinkContent());
			str.append("]]></VERINT_RELATED_CONTENT_LINK>");
		}


		if(!details.getKaChannelRefKey().equals("JOB_AID"))
		{
			// add ONESOURCE_CONTENT 
			if(null!=details.getSchemaDetails().getOneSourceContent() && !"".equals(details.getSchemaDetails().getOneSourceContent()))
			{
				str.append("<ONESOURCE_CONTENT><![CDATA[");
				str.append(details.getSchemaDetails().getOneSourceContent());
				str.append("]]></ONESOURCE_CONTENT>");
			}
		}
	
		// add ONESOURCE_ARTICLE_ID 
		str.append("<ONESOURCE_ARTICLE_ID><![CDATA[");
		str.append("KM"+details.getTypeArticleId());
		str.append("]]></ONESOURCE_ARTICLE_ID>");

		// add ONESOURCE_LAST_MODIFIED_DATE
		if(null!=details.getLastModifiedDate())
		{
			//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			// 2022-03-07 05:00:00 Etc/GMT
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// add ONESOURCE_ARTICLE_ID 
			str.append("<ONESOURCE_LAST_MODIFIED_DATE><![CDATA[");
			str.append(sdf.format(details.getLastModifiedDate())+" Etc/GMT");
			str.append("]]></ONESOURCE_LAST_MODIFIED_DATE>");
			sdf = null;
		}

		// add ONESOURCE_LAST_MODIFIED_BY 
		str.append("<ONESOURCE_LAST_MODIFIED_BY><![CDATA[");
		str.append(details.getLastModifiedBy());
		str.append("]]></ONESOURCE_LAST_MODIFIED_BY>");

		// add ONESOURCE_ARTICLE_VERSION 
		str.append("<ONESOURCE_ARTICLE_VERSION><![CDATA[");
		str.append(details.getMajorVersion()+"."+details.getMinorVersion());
		str.append("]]></ONESOURCE_ARTICLE_VERSION>");
		return str;
	}

	private static StringBuilder addAttachmentFields(DocumentDetails details, StringBuilder str)
	{
		if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
		{
			AttachmentDetails attachDetails= null;
			String name=null;
			String ext=null;
			for(int a=0;a<details.getAttachmentsList().size();a++)
			{
				attachDetails = (AttachmentDetails)details.getAttachmentsList().get(a);
				if(null!=attachDetails.getProcessingStatus() && attachDetails.getProcessingStatus().equals("SUCCESS"))
				{
					str.append("<ATTACHMENT>");
					if(null!=attachDetails.getAttachmentTitle() && !"".equals(attachDetails.getAttachmentTitle()))
					{
						str.append("<ATTACHMENT_FILE_NAME><![CDATA[");
						str.append(attachDetails.getAttachmentTitle());
						str.append("]]></ATTACHMENT_FILE_NAME>");
					}
					if(null!=attachDetails.getAttachmentName() && !"".equals(attachDetails.getAttachmentName()))
					{
						/*
						 * CHECK HERE IF ATTACHMENT NAME IS MORE THEN 100 CHARS, 
						 * THEN TRIM THE NAME TO MAKE IT OF 100 CHARS LENGTH
						 */
						name = attachDetails.getAttachmentName();
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
						str.append("<FILE_ATTACHMENT SIZE=\""+attachDetails.getAttachmentSize()+"\"><![CDATA[");
						str.append(name);
						str.append("]]></FILE_ATTACHMENT>");
						name=  null;
					}
					str.append("</ATTACHMENT>");
				}
				attachDetails = null;
			}
		}
		return str;
	}

	private static StringBuilder addDetailsNodeList(DocumentDetails details, StringBuilder str)
	{
		if(null!=details.getSchemaDetails().getDetailsNodeList() && details.getSchemaDetails().getDetailsNodeList().size()>0)
		{
			SchemaFieldDetails schDetails = null;
			for(int a=0;a<details.getSchemaDetails().getDetailsNodeList().size();a++)
			{
				schDetails = (SchemaFieldDetails)details.getSchemaDetails().getDetailsNodeList().get(a);
				if(details.getKaChannelRefKey().equals("REFERENCE"))
				{
					str.append("<DETAIL>");
				}
				else
				{
					str.append("<DETAILS>");
				}
				if(null!=schDetails.getContentHeader() && !"".equals(schDetails.getContentHeader()))
				{
					str.append("<CONTENT_HEADER><![CDATA[");
					str.append(schDetails.getContentHeader());
					str.append("]]></CONTENT_HEADER>");
				}
				if(null!=schDetails.getDescription() && !"".equals(schDetails.getDescription()))
				{
					str.append("<DESCRIPTION><![CDATA[");
					str.append(schDetails.getDescription());
					str.append("]]></DESCRIPTION>");
				}
				if(details.getKaChannelRefKey().equals("REFERENCE"))
				{
					str.append("</DETAIL>");
				}
				else
				{
					str.append("</DETAILS>");
				}

				schDetails = null;
			}
		}
		return str;
	}

	private static StringBuilder addQuestionAnswerNodeList(DocumentDetails details, StringBuilder str)
	{
		if(null!=details.getSchemaDetails().getQuestionAnswerList() && details.getSchemaDetails().getQuestionAnswerList().size()>0)
		{
			SchemaFieldDetails schDetails = null;
			for(int a=0;a<details.getSchemaDetails().getQuestionAnswerList().size();a++)
			{
				schDetails = (SchemaFieldDetails)details.getSchemaDetails().getQuestionAnswerList().get(a);
				str.append("<QA>");
				if(null!=schDetails.getQuestion() && !"".equals(schDetails.getQuestion()))
				{
					str.append("<QUESTION><![CDATA[");
					str.append(schDetails.getQuestion());
					str.append("]]></QUESTION>");
				}
				if(null!=schDetails.getAnswer() && !"".equals(schDetails.getAnswer()))
				{
					str.append("<ANSWER><![CDATA[");
					str.append(schDetails.getAnswer());
					str.append("]]></ANSWER>");
				}
				str.append("</QA>");
				schDetails = null;
			}
		}
		return str;
	}
}
