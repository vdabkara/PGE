package com.pge.dataload.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.pge.dataload.utils.DBConnector;
import com.pge.dataload.utils.Utilities;
import com.pge.dataload.vo.AttachmentDetails;
import com.pge.dataload.vo.DocumentDetails;
import com.pge.dataload.vo.InlineImageDetails;
import com.pge.dataload.vo.RelatedManualLinkDetails;
import com.pge.dataload.vo.TagDetails;

public class SourceDocumentsDataDAO extends DBConnector{

	private Logger logger = Logger.getLogger(SourceDocumentsDataDAO.class);
	
	public Connection conn = null;
	
	public List<DocumentDetails> getDocumentsList(Timestamp startTime, Timestamp endTime)
	{
		List<DocumentDetails> documentsList = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			try
			{
				if(null==conn || conn.isClosed()==true)
				{
					conn = getSorceConnection();
				}
			}
			catch(Exception eq)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentsList()", eq);
			}
			
			
			String sql="SELECT TYPE_ID,EVA_DYNAMIC_ENTITY_ID,LOCALE,PUBLISHED_START_DATE,PUBLISHED_END_DATE," + 
					"OWNER_ID,MAJOR_VERSION,MINOR_VERSION,LAST_MODIFIED_DATE,LAST_MODIFIED_BY," + 
					"PUBLISHED_BY,WEIGHT_VALUE FROM AKDC_DYNAMIC_CONTENT WHERE " + 
					"IS_DELETED='N' AND STATUS='PUBLISHED'   ";
			if(null!=startTime && null!=endTime)
			{
				sql = sql+" AND (LAST_MODIFIED_DATE BETWEEN "+startTime.getTime()+" and "+endTime.getTime()+")";
			}
			logger.info("getDocumentsList :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			DocumentDetails details = null;
			Timestamp ts = null;
			while(rs.next())
			{
				details=  new DocumentDetails();
				details.setTypeArticleId(String.valueOf(rs.getInt("TYPE_ID")));
				details.setDynamicEntityId(String.valueOf(rs.getInt("EVA_DYNAMIC_ENTITY_ID")));
				details.setLocale(rs.getString("LOCALE"));
				if(rs.getLong("PUBLISHED_START_DATE")>0)
				{
					ts = new Timestamp(rs.getLong("PUBLISHED_START_DATE"));
					if(null!=ts)
					{
						details.setPublishStartDate(ts);
					}
					ts = null;
				}
				if(rs.getLong("PUBLISHED_END_DATE")>0)
				{
					ts = new Timestamp(rs.getLong("PUBLISHED_END_DATE"));
					if(null!=ts)
					{
						details.setPublishEndDate(ts);
					}
					ts = null;
				}
				if(rs.getInt("OWNER_ID")>0)
				{
					details.setOwnerId(String.valueOf(rs.getInt("OWNER_ID")));
				}
				details.setMajorVersion(String.valueOf(rs.getInt("MAJOR_VERSION")));
				details.setMinorVersion(String.valueOf(rs.getInt("MINOR_VERSION")));
				if(rs.getLong("LAST_MODIFIED_DATE")>0)
				{
					ts = new Timestamp(rs.getLong("LAST_MODIFIED_DATE"));
					if(null!=ts)
					{
						details.setLastModifiedDate(ts);
					}
					ts = null;
				}
				details.setLastModifiedBy(rs.getString("LAST_MODIFIED_BY"));
				details.setPublishedBy(rs.getString("PUBLISHED_BY"));
				details.setWeightValue(rs.getString("WEIGHT_VALUE"));
				if(null==documentsList || documentsList.size()<=0)
				{
					documentsList = new ArrayList<DocumentDetails>();
				}
				documentsList.add(details);
				details = null;
			}
		}
		catch(Exception eq)
		{
			Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentsList()", eq);
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentsList()", e);
			}
			startTime=null;
			endTime=null;
		}
		return documentsList;
	}
	
	public String getTitle(String dynamicEntityId)
	{
		String title=null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			try
			{
				if(null==conn || conn.isClosed()==true)
				{
					conn = getSorceConnection();
				}
			}
			catch(Exception eq)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getTitle()", eq);
			}
			/*
			 * ENTITY FIELD ID CANE BE ANYTHING FROM BELOW
			 * 1000,1016,1024,1050,1000037,1000075,1000130,1000227,1000185 
			 */
			String sql="SELECT FIELD_VALUE FROM EVA_DYNAMIC_ENTITY_FIELD_VALUE where EVA_DYNAMIC_ENTITY_ID = "+dynamicEntityId+" and DYN_ENTITY_FIELD_ID IN (1000,1016,1024,1050,1000037,1000075,1000130,1000227,1000185)";
			logger.info("getTitle :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				if(null!=rs.getString("FIELD_VALUE") && !"".equals(rs.getString("FIELD_VALUE")))
				{
					title=rs.getString("FIELD_VALUE");
					break;
				}
			}
		}
		catch(Exception eq)
		{
			Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getTitle()", eq);
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getTitle()", e);
			}
			dynamicEntityId=null;
		}
		return title;
	}

	public DocumentDetails getDocumentTagsDetails(DocumentDetails details)
	{
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			try
			{
				if(null==conn || conn.isClosed()==true)
				{
					conn = getSorceConnection();
				}
			}
			catch(Exception eq)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentTagsDetails()", eq);
			}
			
			String sql="SELECT B.TAG_NAME,B.TAGSET_NAME FROM EVA_ENTITY__TAGSET_SELECTION A,EVA_TAGSET_SELECTION B where A.ENTITY_ID ="+details.getDynamicEntityId()+" AND A.TAG_SELECTION=B.ID";
			logger.info("getDocumentTagsDetails :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			String value=null;
			String tagSetName=null;
			TagDetails tagDetails = null;
			while(rs.next())
			{
				if(null!=rs.getString("TAG_NAME") && !"".equals(rs.getString("TAG_NAME")))
				{
					value = rs.getString("TAG_NAME");
					if(value.indexOf("_")!=-1)
					{
						value = value.substring(value.indexOf("_")+1,value.length());
					}
				}
				
				if(null!=rs.getString("TAGSET_NAME") && !"".equals(rs.getString("TAGSET_NAME")))
				{
					tagSetName=rs.getString("TAGSET_NAME");
				}
				
				if(null!=value && !"".equals(value) && null!=tagSetName && !"".equals(tagSetName))
				{
					if(tagSetName.trim().toLowerCase().equals("region"))
					{
						// CHANNEL
						details.setChannelRegion(value);
					}
					else
					{
						// set Tag Value
						tagDetails = new TagDetails();
						tagDetails.setVaItemName(value);
						// check for Tag Type
						if(tagSetName.trim().toLowerCase().equals("topic"))
						{
							// TOPIC AS CATEGORY
							if(null==details.getTopicsList() || details.getTopicsList().size()<=0)
							{
								details.setTopicsList(new ArrayList<TagDetails>());
							}
							details.getTopicsList().add(tagDetails);
						}
						else if(tagSetName.trim().toLowerCase().equals("product"))
						{
							// PRDOCUT 
							if(null==details.getProductList() || details.getProductList().size()<=0)
							{
								details.setProductList(new ArrayList<TagDetails>());
							}
							details.getProductList().add(tagDetails);
						}
						else if(tagSetName.trim().toLowerCase().equals("kbase"))
						{
							// DEPARTMENT AS USER GROUP & CATEGORY
							if(null==details.getDepartmentUserGroupList() || details.getDepartmentUserGroupList().size()<=0)
							{
								details.setDepartmentUserGroupList(new ArrayList<TagDetails>());
							}
							details.getDepartmentUserGroupList().add(tagDetails);
						}
						tagDetails = null;
					}
				}
				value  =null;
				tagSetName = null;
			}
			sql = null;
		}
		catch(Exception eq)
		{
			Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentTagsDetails()", eq);
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentTagsDetails()", e);
			}
		}
		return details;
	}

	public String getDocumentContent(String dynamicEntityId)
	{
		String content=null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			try
			{
				if(null==conn || conn.isClosed()==true)
				{
					conn = getSorceConnection();
				}
			}
			catch(Exception eq)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentContent()", eq);
			}
			
			/*
			 * ENTITY FIELD ID CAN BE ANYTHING FROM BELOW
			 * 1000180,1000181,1000182,1000125,1000126,1000127,1000070,1000071,1000072,1055,1057,1059,1032,1037,1042,1002,1010,1018
			 */
			List<String> fieldValueList = new ArrayList<String>();
			String sql="SELECT FIELD_VALUE FROM EVA_DYNAMIC_ENTITY_FIELD_VALUE where "
					+ "EVA_DYNAMIC_ENTITY_ID = "+dynamicEntityId+" and DYN_ENTITY_FIELD_ID IN (1000180,1000181,1000182,1000125,1000126,1000127,1000070,1000071,1000072,1055,1057,1059,1032,1037,1042,1002,1010,1018) ";
			String fieldValue=null;
			logger.info("getDocumentContent :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				if(null!=rs.getString("FIELD_VALUE") && !"".equals(rs.getString("FIELD_VALUE")))
				{
					fieldValueList.add(rs.getString("FIELD_VALUE"));
				}
			}
			sql =null;
			pstmt.close();pstmt=null;
			rs.close();rs=null;
			
			if(null!=fieldValueList && fieldValueList.size()>0)
			{
				fieldValue= "";
				String temp = "";
				for(int a=0;a<fieldValueList.size();a++)
				{
					temp = fieldValueList.get(a).toString();
					if(null!=temp && !"".equals(temp))
					{
						// fieldValue will be in format - [:[1001203]:], read the integer values from it
						temp = temp.replace("[:[", "");
						temp = temp.replace("]:]", "");
						
						if(null!=temp && !"".equals(temp))
						{
							fieldValue+=temp+",";
						}
					}
				}
			}
			
			if(null!=fieldValue && !"".equals(fieldValue))
			{
				if(fieldValue.endsWith(","))
				{
					fieldValue = fieldValue.substring(0, fieldValue.length()-1);
				}
				logger.info("getDocumentContent :: Field Value as Integer for fetching HTML Content, read is :: >" + fieldValue);
				
				if(null!=fieldValue && !"".equals(fieldValue))
				{
					// proceed for fetching actual content
					sql="SELECT VALUE FROM EVA_CLOB WHERE ID IN ("+fieldValue+")";
					logger.info("getDocumentContent :: getContentSQL :: >"+ sql);
					pstmt  = conn.prepareStatement(sql);
					rs= pstmt.executeQuery();
					if(rs.next())
					{
						if(null!=rs.getString("VALUE") && !"".equals(rs.getString("VALUE")))
						{
							content = rs.getString("VALUE");
						}
					}
					sql=null;
				}
			}
			fieldValue = null;
		}
		catch(Exception eq)
		{
			Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentContent()", eq);
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getDocumentContent()", e);
			}
			dynamicEntityId=null;
		}
		return content;
	}

	public List<RelatedManualLinkDetails> getRelatedManualsList(String dynamicEntityId)
	{
		List<RelatedManualLinkDetails> manualsList = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			try
			{
				if(null==conn || conn.isClosed()==true)
				{
					conn = getSorceConnection();
				}
			}
			catch(Exception eq)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getRelatedManualsList()", eq);
			}
			
			
			String sql="SELECT A.MIGRATABLE_REFERENCE,B.TITLE,C.ENTITY_ID,D.TYPE_ID FROM AKDC_DYNAMIC_CONTENT__CONTENT A, "
					+ "AKCB_CONTENT B, EVA_MIGRATABLE C, AKDC_DYNAMIC_CONTENT D WHERE A.CONTENT_ID = "+dynamicEntityId+" AND "
					+ "A.ASSOCIATION_TYPE = 'Related' AND A.MIGRATABLE_REFERENCE=B.CONTENT_ID AND C.IS_DELETED = 'N' "
					+ "AND C.MIGRATABLE_REFERENCE=A.MIGRATABLE_REFERENCE AND D.IS_DELETED='N' AND D.STATUS='Published' "
					+ "AND C.ENTITY_ID=D.ID ";
			logger.info("getRelatedManualsList :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			RelatedManualLinkDetails details = null;
			while(rs.next())
			{
				details=  new RelatedManualLinkDetails();
				details.setMigratableReference(rs.getString("MIGRATABLE_REFERENCE"));
				details.setRmLinkTitle(rs.getString("TITLE"));
				details.setRmEntityId(String.valueOf(rs.getInt("ENTITY_ID")));
				details.setRmTypeArticleId(String.valueOf(rs.getInt("TYPE_ID")));
				if(null==manualsList || manualsList.size()<=0)
				{
					manualsList = new ArrayList<RelatedManualLinkDetails>();
				}
				manualsList.add(details);
				details = null;
			}
		}
		catch(Exception eq)
		{
			Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getRelatedManualsList()", eq);
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getRelatedManualsList()", e);
			}
			dynamicEntityId = null;
		}
		return manualsList;
	}
	
	public List<InlineImageDetails> getAssetEDImageNames(List<InlineImageDetails> assetEDImagesList)
	{
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			try
			{
				if(null==conn || conn.isClosed()==true)
				{
					conn = getSorceConnection();
				}
			}
			catch(Exception eq)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getAssetEDImageNames()", eq);
			}
			
			String migratableRefKey="";
			InlineImageDetails details = null;
			for(int a=0;a<assetEDImagesList.size();a++)
			{
				details = (InlineImageDetails)assetEDImagesList.get(a);
				migratableRefKey+= "'"+details.getAssetId()+"'";
				if(a!=assetEDImagesList.size()-1)
				{
					migratableRefKey+=",";
				}
				details = null;
			}
			
			String sql="SELECT A.MIGRATABLE_REFERENCE,B.NAME FROM EVA_MIGRATABLE A,CA_ASSET_LOC B WHERE A.IS_DELETED = 'N' AND " + 
					"A.TYPE_NAME LIKE 'AssetED%' AND A.MIGRATABLE_REFERENCE IN ("+migratableRefKey+")  AND A.ENTITY_ID=B.ID ";
			
			logger.info("getAssetEDImageNames :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			details = null;
			while(rs.next())
			{
				if(null!=rs.getString("NAME") && !"".equals(rs.getString("NAME")) && 
						null!=rs.getString("MIGRATABLE_REFERENCE") && !"".equals(rs.getString("MIGRATABLE_REFERENCE")))
				{
					details = null;
					for(int a=0;a<assetEDImagesList.size();a++)
					{
						details = (InlineImageDetails)assetEDImagesList.get(a);
						if(details.getAssetId().equalsIgnoreCase(rs.getString("MIGRATABLE_REFERENCE")))
						{
							details.setImageName(rs.getString("NAME"));
							break;
						}
						details = null;
					}
				}
			}
			sql  =null;
			migratableRefKey = null;
		}
		catch(Exception eq)
		{
			Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getAssetEDImageNames()", eq);
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getAssetEDImageNames()", e);
			}
		}
		return assetEDImagesList;
	}

	public List<AttachmentDetails> getAttachmentDetails(String typeArticleId, String dynamicEntityId)
	{
		List<AttachmentDetails> attachmentsList = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			try
			{
				if(null==conn || conn.isClosed()==true)
				{
					conn = getSorceConnection();
				}
			}
			catch(Exception eq)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getAttachmentDetails()", eq);
			}
			
			AttachmentDetails details = null;
			
			String sql="Select LOGICAL from gtcc_attachment where ATTACH_ID IN ( " + 
					" Select entity_id from AKDC_DYNAMIC_CONTENT__ASSOC " + 
					" where  content_id IN (Select id from akdc_dynamic_content where is_deleted = 'N' and status= 'PUBLISHED'" + 
					" and Type_id = '"+typeArticleId+"')) ";
			
			logger.info("getAttachmentDetails :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			details = null;
			while(rs.next())
			{
				if(null!=rs.getString("LOGICAL") && !"".equals(rs.getString("LOGICAL")))
				{
					details = new AttachmentDetails();
					details.setAttachmentType("DOC_ATTACHMENT");
					details.setAttachmentTitle(rs.getString("LOGICAL"));
					if(null==attachmentsList || attachmentsList.size()<=0)
					{
						attachmentsList = new ArrayList<AttachmentDetails>();
					}
					attachmentsList.add(details);
					details = null;
				}
			}
			sql  =null;
			pstmt.close();pstmt=null;
			rs.close();rs=null;
			
			/*
			 * CHECK FOR INLINE ATTACHMENTS
			 * USING DYNAMIC ENTITY FIELD ID=1000229 & 1000230
			 */
			sql="SELECT DYN_ENTITY_FIELD_ID,FIELD_VALUE FROM EVA_DYNAMIC_ENTITY_FIELD_VALUE where EVA_DYNAMIC_ENTITY_ID = "+dynamicEntityId+""
					+ " and DYN_ENTITY_FIELD_ID IN (1000229,1000230)  ";
			
			logger.info("getAttachmentDetails :: Inline Attachments :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			details = new AttachmentDetails();
			details.setAttachmentType("INLINE_ATTACHMENT");
			while(rs.next())
			{
				if(rs.getInt("DYN_ENTITY_FIELD_ID") > 0 && 
						null!=rs.getString("FIELD_VALUE") && !"".equals(rs.getString("FIELD_VALUE")))
				{
					if(rs.getInt("DYN_ENTITY_FIELD_ID")==1000229)
					{
						// VA SYSTEM INLINE ATTACHMENT URL
						details.setInlineAttachmentPath(rs.getString("FIELD_VALUE"));
					}
					else if(rs.getInt("DYN_ENTITY_FIELD_ID")==1000230)
					{
						// VA SYSTEM INLINE ATTACHMENT NAME
						details.setAttachmentTitle(rs.getString("FIELD_VALUE"));
					}
				}
			}
			
			/*
			 * add a condition if attachment title is NULL
			 * Get Attachment name without extension and set as attachment title
			 */
			if(null==details.getAttachmentTitle() || "".equals(details.getAttachmentTitle()))
			{
				String fName = "";
				if(null!=details.getInlineAttachmentPath() && !"".equals(details.getInlineAttachmentPath()))
				{
					if(details.getInlineAttachmentPath().lastIndexOf("/")!=-1)
					{
						fName=  details.getInlineAttachmentPath().substring(details.getInlineAttachmentPath().lastIndexOf("/")+1, details.getInlineAttachmentPath().length());
						// remove extension as well
						if(null!=fName && !"".equals(fName))
						{
							if(fName.lastIndexOf(".")!=-1)
							{
								fName = fName.substring(0, fName.lastIndexOf("."));
							}
						}
					}
				}
				details.setAttachmentTitle(fName);
				fName  =null;
			}
			
			// add to attachmentsList only when Title & InlineAttachment Path is not null
			if(null!=details.getAttachmentTitle() && !"".equals(details.getAttachmentTitle()) && 
					null!=details.getInlineAttachmentPath() && !"".equals(details.getInlineAttachmentPath()))
			{
				if(null==attachmentsList || attachmentsList.size()<=0)
				{
					attachmentsList = new ArrayList<AttachmentDetails>();
				}
				attachmentsList.add(details);
			}
			details = null;
			sql  =null;
		}
		catch(Exception eq)
		{
			Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getAttachmentDetails()", eq);
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(SourceDocumentsDataDAO.class.getName(), "getAttachmentDetails()", e);
			}
			typeArticleId = null;
		}
		return attachmentsList;
	}

	
}
