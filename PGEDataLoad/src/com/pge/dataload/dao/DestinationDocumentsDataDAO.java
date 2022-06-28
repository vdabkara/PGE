package com.pge.dataload.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.pge.dataload.utils.DBConnector;
import com.pge.dataload.utils.Utilities;
import com.pge.dataload.vo.AttachmentDetails;
import com.pge.dataload.vo.DocumentDetails;
import com.pge.dataload.vo.InlineImageDetails;
import com.pge.dataload.vo.InnerLinkDetails;
import com.pge.dataload.vo.RelatedManualLinkDetails;
import com.pge.dataload.vo.TagDetails;

public class DestinationDocumentsDataDAO extends DBConnector{

	private Logger logger = Logger.getLogger(DestinationDocumentsDataDAO.class);
	
	public void saveDocumentDetails(DocumentDetails details)
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long autoId = 0;
		Connection conn = null;
		try
		{
			conn = getDestinationConnection();
			String sql = "SELECT ID FROM DOCUMENT_DETAILS WHERE VA_TYPE_ID = "+ details.getTypeArticleId();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				autoId = rs.getLong("ID");
			}
			sql = null;
			
			
			if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
			{
				if(null!=details.getProcessingStatus() && details.getProcessingStatus().equals("SUCCESS"))
				{
					// SET STATUS AS SUCCESS_WITH_ERRORS
					details.setProcessingStatus("SUCCESS_WITH_ERRORS");
				}
			}
			
			if(autoId > 0)
			{
				// perform update operation
				updateOperation(details, conn, autoId);
			}
			else
			{
				// perform create operation
				createOperation(details, conn);
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "saveDocumentDetails()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
				{
					conn.close();conn = null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
				if(null!=pstmt)
				{
					pstmt.close();pstmt = null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "saveDocumentDetails()", e);
			}
		}
	}
	
	private void createOperation(DocumentDetails details, Connection conn)
	{
		PreparedStatement pstmt=null;
		try
		{
			// set Auto Commit to False
			conn.setAutoCommit(false);
			
			
			byte[] remarksData = null;
			InputStream is = null;
			String remarks="";
			if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
			{
				remarks = details.getErrorMessage();
			}
			remarksData=  remarks.getBytes();
			is = new ByteArrayInputStream(remarksData);
			
			// INSERT INTO DOCUMENT DETAILS TABLE
			String sql="INSERT INTO DOCUMENT_DETAILS(VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TITLE,VA_PUB_START_DATE,VA_PUB_END_DATE,VA_PUB_BY,VA_OWNER_ID,VA_LAST_MOD_DATE,VA_LAST_MOD_BY,VA_MAJOR_VERSION,VA_MINOR_VERSION,VA_WEIGHT_VALUE,REC_CREATION_TMSTP,KA_CONTENT_PATTERN,KA_CHANNEL_REF_KEY,KA_RECORD_ID,KA_DOCUMENT_ID,KA_ARTICLE_ID,KA_VERSION,KA_PUB_STATUS,ALL_UG_MAPPED,ALL_CAT_MAPPED,ALL_PROD_MAPPED,ALL_INNERLINKS_MAPPED,ALL_IMAGES_MAPPED,ALL_REL_LINKS_MAPPED,KA_PROCESSING_STATUS,KA_ERROR_CODE,KA_ERROR_MESSAGE,KA_DATA_ERRORS,ALL_ATTACHMENTS_MAPPED,KA_VERSION_ID) "
					+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, details.getTypeArticleId());
			pstmt.setString(2, details.getDynamicEntityId());
			pstmt.setString(3, details.getLocale());
			pstmt.setString(4, details.getChannelRegion());
			pstmt.setString(5, details.getTitle());
			pstmt.setTimestamp(6, details.getPublishStartDate());
			pstmt.setTimestamp(7, details.getPublishEndDate());
			pstmt.setString(8, details.getPublishedBy());
			pstmt.setString(9, details.getOwnerId());
			pstmt.setTimestamp(10, details.getLastModifiedDate());
			pstmt.setString(11, details.getLastModifiedBy());
			pstmt.setString(12, details.getMajorVersion());
			pstmt.setString(13, details.getMinorVersion());
			pstmt.setString(14, details.getWeightValue());
			pstmt.setTimestamp(15, new Timestamp(new Date().getTime()));
			pstmt.setString(16, details.getKaContentPattern());
			pstmt.setString(17, details.getKaChannelRefKey());
			pstmt.setString(18, details.getKaRecordId());
			pstmt.setString(19, details.getKaDocumentId());
			pstmt.setString(20, details.getKaArticleId());
			pstmt.setString(21, details.getKaVersion());
			pstmt.setString(22, details.getKaPublishStatus());
			pstmt.setString(23, details.getAllUserGroupsMapped());
			pstmt.setString(24, details.getAllCategoriesMapped());
			pstmt.setString(25, details.getAllProductsMapped());
			pstmt.setString(26, details.getAllInnerLinksMapped());
			pstmt.setString(27, details.getAllImagesMapped());
			pstmt.setString(28, details.getAllRelatedLinksMapped());
			pstmt.setString(29, details.getProcessingStatus());
			pstmt.setString(30, details.getErrorCodes());
			pstmt.setBinaryStream(31, is, remarksData.length);
			pstmt.setString(32, details.getDataErrors());
			pstmt.setString(33, details.getAllAttachmentsMapped());
			pstmt.setString(34, details.getKaVersionId());
			pstmt.executeUpdate();
			pstmt.close();pstmt=null;
			is.close();is = null;
			remarks=  null;
			remarksData = null;
			sql = null;
			
			/*
			 * PERFORM OTHER CHILD OPERATIONS
			 */
			insertChildOperations(details, conn);
			
			// COMMIT TRANSACTION 
			conn.commit();
			// SET AUTOCOMMIT TO TRUE
			conn.setAutoCommit(true);
			
			logger.info("createOperation :: Document Details for "+ details.getTypeArticleId()+" Saved Successfully.");
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "createOperation()", e);
			// rollBack Connection
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "createOperation()", e1);
			}
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt = null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "createOperation()", e);
			}
		}
	}
	
	private void updateOperation(DocumentDetails details, Connection conn, long autoId)
	{
		PreparedStatement pstmt=null;
		try
		{
			// set Auto Commit to False
			conn.setAutoCommit(false);
			
			
			byte[] remarksData = null;
			InputStream is = null;
			String remarks="";
			if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
			{
				remarks = details.getErrorMessage();
			}
			remarksData=  remarks.getBytes();
			is = new ByteArrayInputStream(remarksData);
			
			// INSERT INTO DOCUMENT DETAILS TABLE
			String sql="UPDATE DOCUMENT_DETAILS SET VA_TYPE_ID=?,VA_DYNAMIC_ENTITY_ID=?,VA_LOCALE=?,"
					+ "VA_REG_CHANNEL=?,VA_TITLE=?,VA_PUB_START_DATE=?,VA_PUB_END_DATE=?,VA_PUB_BY=?,"
					+ "VA_OWNER_ID=?,VA_LAST_MOD_DATE=?,VA_LAST_MOD_BY=?,VA_MAJOR_VERSION=?,"
					+ "VA_MINOR_VERSION=?,VA_WEIGHT_VALUE=?,REC_MODIFIED_TMSPT=?,KA_CONTENT_PATTERN=?,"
					+ "KA_CHANNEL_REF_KEY=?,ALL_UG_MAPPED=?,ALL_CAT_MAPPED=?,ALL_PROD_MAPPED=?,ALL_INNERLINKS_MAPPED=?,"
					+ "ALL_IMAGES_MAPPED=?,ALL_REL_LINKS_MAPPED=?,KA_PROCESSING_STATUS=?,KA_ERROR_CODE=?,"
					+ "KA_ERROR_MESSAGE=?,KA_DATA_ERRORS=?,ALL_ATTACHMENTS_MAPPED = ?  ";
			if(null!=details.getKaRecordId() && !"".equals(details.getKaRecordId()))
			{
				sql = sql+",KA_RECORD_ID=?,KA_DOCUMENT_ID=?,KA_ARTICLE_ID=?,KA_VERSION=?,KA_PUB_STATUS=?,KA_VERSION_ID = ? WHERE ID=?";
			}
			else
			{
				sql = sql+" WHERE ID=?";
			}
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, details.getTypeArticleId());
			pstmt.setString(2, details.getDynamicEntityId());
			pstmt.setString(3, details.getLocale());
			pstmt.setString(4, details.getChannelRegion());
			pstmt.setString(5, details.getTitle());
			pstmt.setTimestamp(6, details.getPublishStartDate());
			pstmt.setTimestamp(7, details.getPublishEndDate());
			pstmt.setString(8, details.getPublishedBy());
			pstmt.setString(9, details.getOwnerId());
			pstmt.setTimestamp(10, details.getLastModifiedDate());
			pstmt.setString(11, details.getLastModifiedBy());
			pstmt.setString(12, details.getMajorVersion());
			pstmt.setString(13, details.getMinorVersion());
			pstmt.setString(14, details.getWeightValue());
			pstmt.setTimestamp(15, new Timestamp(new Date().getTime()));
			pstmt.setString(16, details.getKaContentPattern());
			pstmt.setString(17, details.getKaChannelRefKey());
			pstmt.setString(18, details.getAllUserGroupsMapped());
			pstmt.setString(19, details.getAllCategoriesMapped());
			pstmt.setString(20, details.getAllProductsMapped());
			pstmt.setString(21, details.getAllInnerLinksMapped());
			pstmt.setString(22, details.getAllImagesMapped());
			pstmt.setString(23, details.getAllRelatedLinksMapped());
			pstmt.setString(24, details.getProcessingStatus());
			pstmt.setString(25, details.getErrorCodes());
			pstmt.setBinaryStream(26, is, remarksData.length);
			pstmt.setString(27, details.getDataErrors());
			pstmt.setString(28, details.getAllAttachmentsMapped());
			if(null!=details.getKaRecordId() && !"".equals(details.getKaRecordId()))
			{
				//  during update no error occurred
				pstmt.setString(29, details.getKaRecordId());
				pstmt.setString(30, details.getKaDocumentId());
				pstmt.setString(31, details.getKaArticleId());
				pstmt.setString(32, details.getKaVersion());
				pstmt.setString(33, details.getKaPublishStatus());
				pstmt.setString(34, details.getKaVersionId());
				pstmt.setLong(35, autoId);
			}
			else
			{
				// during update error occurred. update errors
				pstmt.setLong(29, autoId);
			}
			
			pstmt.executeUpdate();
			pstmt.close();pstmt=null;
			is.close();is = null;
			remarks=  null;
			remarksData = null;
			sql = null;
			
			/*
			 * DELETE DATA FROM ALL CHILD TABLES
			 */
			deleteAllChildInformation(conn, details.getTypeArticleId());
			
			/*
			 * PERFORM OTHER CHILD OPERATIONS
			 */
			insertChildOperations(details, conn);
			
			// COMMIT TRANSACTION 
			conn.commit();
			// SET AUTOCOMMIT TO TRUE
			conn.setAutoCommit(true);
			logger.info("updateOperation :: Document Details for "+ details.getTypeArticleId()+" Updated Successfully.");
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "updateOperation()", e);
			// rollBack Connection
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "updateOperation()", e1);
			}
		}
		finally
		{
			try
			{
				if(null!=pstmt)
				{
					pstmt.close();pstmt = null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "updateOperation()", e);
			}
		}
	}
	
	private void insertChildOperations(DocumentDetails details, Connection conn) throws SQLException, IOException
	{
		String sql =null;
		/*
		 * NOW INSERT DATA IN DEPT_CAT_DETAILS
		 */
		if(null!=details.getDepartmentCategoryList() && details.getDepartmentCategoryList().size()>0)
		{
			sql = "INSERT INTO DEPT_CAT_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_DEPT_CAT_NAME,KA_CATEGORY_REF_KEY,REC_CREATION_TMSTP,KA_PROCESSING_STATUS,KA_ERROR_CODE,KA_ERROR_MESSAGE,KA_DATA_ERRORS) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
			addTagsData(details.getDepartmentCategoryList(), conn, sql, details);
		}
		sql = null;
		
		/*
		 * NOW INSERT DATA IN DEPT_UG_DETAILS
		 */
		if(null!=details.getDepartmentUserGroupList() && details.getDepartmentUserGroupList().size()>0)
		{
			sql = "INSERT INTO DEPT_UG_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_DEPT_UG_NAME,KA_USERGROUP_REF_KEY,REC_CREATION_TMSTP,KA_PROCESSING_STATUS,KA_ERROR_CODE,KA_ERROR_MESSAGE,KA_DATA_ERRORS) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
			addTagsData(details.getDepartmentUserGroupList(), conn, sql, details);
		}
		sql = null;
		
		/*
		 * NOW INSERT DATA IN PRODUCT_DETAILS
		 */
		if(null!=details.getProductList() && details.getProductList().size()>0)
		{
			sql = "INSERT INTO PRODUCT_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_PRODUCT_NAME,KA_PRODUCT_REF_KEY,REC_CREATION_TMSTP,KA_PROCESSING_STATUS,KA_ERROR_CODE,KA_ERROR_MESSAGE,KA_DATA_ERRORS) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
			addTagsData(details.getProductList(), conn, sql, details);
		}
		sql = null;
		
		/*
		 * NOW INSERT DATA IN TOPIC_DETAILS
		 */
		if(null!=details.getTopicsList() && details.getTopicsList().size()>0)
		{
			sql = "INSERT INTO TOPIC_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TOPIC_NAME,KA_CATEGORY_REF_KEY,REC_CREATION_TMSTP,KA_PROCESSING_STATUS,KA_ERROR_CODE,KA_ERROR_MESSAGE,KA_DATA_ERRORS) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
			addTagsData(details.getTopicsList(), conn, sql, details);
		}
		sql = null;
		
		/*
		 * NOW INSERT INLINE IMAGES-  ASSETS
		 */
		if(null!=details.getAssetEDImagesList() && details.getAssetEDImagesList().size()>0)
		{
			addInlineImagesData(details.getAssetEDImagesList(), conn, details);
		}
		if(null!=details.getOtherInlineImages() && details.getOtherInlineImages().size()>0)
		{
			addInlineImagesData(details.getOtherInlineImages(), conn, details);
		}
		
		/*
		 * NOW INSERT INLINE INNER LINKS DATA
		 */
		if(null!=details.getContentEDInnerLinksList() && details.getContentEDInnerLinksList().size()>0)
		{
			addInnerLinksData(details.getContentEDInnerLinksList(), conn, details);
		}
		if(null!=details.getOtherInnerLinksList() && details.getOtherInnerLinksList().size()>0)
		{
			addInnerLinksData(details.getOtherInnerLinksList(), conn, details);
		}
		
		/*
		 * NOW INSERT RELATED LINKS DATA
		 */
		if(null!=details.getRelatedManualLinksList() && details.getRelatedManualLinksList().size()>0)
		{
			addRelatedLinksData(details.getRelatedManualLinksList(), conn, details);
		}
		
		/*
		 * NOW INSERT ATTACHMENTS DATA
		 */
		if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
		{
			addAttachmentsData(details.getAttachmentsList(), conn, details);
		}
	}
	
	private void addTagsData(List<TagDetails> list, Connection conn, String query, DocumentDetails details) throws SQLException, IOException
	{
		PreparedStatement pstmt=null;
		InputStream is = null;
		String remarks=null;
		byte[] remarksData=null;
		if(null!=list && list.size()>0)
		{
			TagDetails tagDetails = null;
			pstmt = conn.prepareStatement(query);
			for(int a=0;a<list.size();a++)
			{
				tagDetails = (TagDetails)list.get(a);
				
				pstmt.setString(1, details.getTypeArticleId());
				pstmt.setString(2, details.getDynamicEntityId());
				pstmt.setString(3, details.getLocale());
				pstmt.setString(4, details.getChannelRegion());
				pstmt.setString(5, tagDetails.getVaItemName());
				pstmt.setString(6, tagDetails.getKaItemRefKey());
				pstmt.setTimestamp(7, new Timestamp(new Date().getTime()));
				pstmt.setString(8, tagDetails.getProcessingStatus());
				pstmt.setString(9, tagDetails.getErrorCodes());
				remarks = "";
				if(null!=tagDetails.getErrorMessage() && !"".equals(tagDetails.getErrorMessage()))
				{
					remarks = tagDetails.getErrorMessage();
				}
				remarksData = remarks.getBytes();
				is = new ByteArrayInputStream(remarksData);
				
				pstmt.setBinaryStream(10, is, remarksData.length);
				pstmt.setString(11, tagDetails.getDataErrors());
				pstmt.addBatch();
				
				is.close();is=null;
				remarks = null;
				remarksData = null;
				tagDetails=  null;
			}
			pstmt.executeBatch();
			pstmt.close();pstmt=null;
		}
	}

	private void addInlineImagesData(List<InlineImageDetails> list, Connection conn, DocumentDetails details) throws SQLException
	{
		InlineImageDetails imgDetails = null;
		String sql = "INSERT INTO INLINE_IMAGES_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_TAG_CONTENT,VA_ASSET_ID,VA_IMAGE_NAME,VA_IMAGE_WIDTH,VA_IMAGE_HEIGHT,VA_IMAGE_TYPE,VA_IMAGE_PATH,VA_SRC_VALUE,REC_CREATION_TMSTP,KA_PROCESSING_STATUS,KA_ERROR_MESSAGE) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for(int a=0;a<list.size();a++)
		{
			imgDetails = (InlineImageDetails)list.get(a);
			pstmt.setString(1, details.getTypeArticleId());
			pstmt.setString(2, details.getDynamicEntityId());
			pstmt.setString(3, details.getLocale());
			pstmt.setString(4, details.getChannelRegion());
			pstmt.setString(5, imgDetails.getTagContent());
			pstmt.setString(6, imgDetails.getAssetId());
			pstmt.setString(7, imgDetails.getImageName());
			pstmt.setString(8, imgDetails.getWidth());
			pstmt.setString(9, imgDetails.getHeight());
			pstmt.setString(10, imgDetails.getImageType());
			pstmt.setString(11, imgDetails.getImagePath());
			pstmt.setString(12, imgDetails.getSrcAttributeValue());
			pstmt.setTimestamp(13, new Timestamp(new Date().getTime()));
			pstmt.setString(14, imgDetails.getProcessingStatus());
			pstmt.setString(15, imgDetails.getErrorMessage());
			pstmt.addBatch();
			imgDetails = null;
		}
		pstmt.executeBatch();
		pstmt.close();pstmt=null;
	}

	private void addInnerLinksData(List<InnerLinkDetails> list, Connection conn, DocumentDetails details) throws SQLException
	{
		String sql = "INSERT INTO INLINE_INRLNK_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_HREF_VALUE,KA_DOCUMENT_ID,KA_INRLK_URL,REC_CREATION_TMSTP,KA_PROCESSING_STATUS,KA_ERROR_MESSAGE,VA_TAG_CONTENT,VA_INRLNK_TYPE,VA_INRLNK_TITLE,VA_INRLNK_TYPE_ID)"
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt =conn.prepareStatement(sql);
		InnerLinkDetails inrlkDetails = null;
		for(int a=0;a<list.size();a++)
		{
			inrlkDetails = (InnerLinkDetails)list.get(a);
			pstmt.setString(1, details.getTypeArticleId());
			pstmt.setString(2, details.getDynamicEntityId());
			pstmt.setString(3, details.getLocale());
			pstmt.setString(4, details.getChannelRegion());
			pstmt.setString(5, inrlkDetails.getHrefAttributeValue());
			pstmt.setString(6, inrlkDetails.getKaDocumentId());
			pstmt.setString(7, inrlkDetails.getKaInnerLinkURL());
			pstmt.setTimestamp(8, new Timestamp(new Date().getTime()));
			pstmt.setString(9, inrlkDetails.getProcessingStatus());
			pstmt.setString(10, inrlkDetails.getErrorMessage());
			pstmt.setString(11, inrlkDetails.getTagContent());
			pstmt.setString(12, inrlkDetails.getInnerLinkType());
			pstmt.setString(13, inrlkDetails.getInnerLinkTitle());
			pstmt.setString(14, inrlkDetails.getInnerLinkTypeId());
			
			pstmt.addBatch();
			inrlkDetails = null;
		}
		pstmt.executeBatch();
		pstmt.close();pstmt=null;
		sql = null;
	}
	
	private void addAttachmentsData(List<AttachmentDetails> list, Connection conn, DocumentDetails details) throws SQLException
	{
		String sql = "INSERT INTO ATTACHMENT_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_ATTACHMENT_NAME,VA_ATTACHMENT_TITLE,VA_ATTACHMENT_PATH,VA_ATTACHMENT_EXT,REC_CREATION_TMSTP,KA_PROCESSING_STATUS,KA_ERROR_MESSAGE,VA_ATTACHMENT_TYPE,VA_INLINE_ATTACHMENT_PATH)"
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt =conn.prepareStatement(sql);
		AttachmentDetails attachDetails = null;
		for(int a=0;a<list.size();a++)
		{
			attachDetails = (AttachmentDetails)list.get(a);
			pstmt.setString(1, details.getTypeArticleId());
			pstmt.setString(2, details.getDynamicEntityId());
			pstmt.setString(3, details.getLocale());
			pstmt.setString(4, details.getChannelRegion());
			pstmt.setString(5, attachDetails.getAttachmentName());
			pstmt.setString(6, attachDetails.getAttachmentTitle());
			pstmt.setString(7, attachDetails.getAttachmentPath());
			pstmt.setString(8, attachDetails.getExtension());
			pstmt.setTimestamp(9, new Timestamp(new Date().getTime()));
			pstmt.setString(10, attachDetails.getProcessingStatus());
			pstmt.setString(11, attachDetails.getErrorMessage());
			pstmt.setString(12, attachDetails.getAttachmentType());
			pstmt.setString(13, attachDetails.getInlineAttachmentPath());
			pstmt.addBatch();
			attachDetails = null;
		}
		pstmt.executeBatch();
		pstmt.close();pstmt=null;
		sql = null;
	}
	
	private void addRelatedLinksData(List<RelatedManualLinkDetails> list, Connection conn, DocumentDetails details) throws SQLException
	{
		String sql = "INSERT INTO RELATED_LINK_DETAILS (VA_TYPE_ID,VA_DYNAMIC_ENTITY_ID,VA_LOCALE,VA_REG_CHANNEL,VA_MIGRATABLE_REFERENCE,VA_RM_TITLE,VA_RM_ENTITY_ID,VA_RM_TYPE_ID,REC_CREATION_TMSTP,KA_ARTICLE_ID,KA_PROCESSING_STATUS,KA_ERROR_MESSAGE)"
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt =conn.prepareStatement(sql);
		RelatedManualLinkDetails rlDetails = null;
		for(int a=0;a<list.size();a++)
		{
			rlDetails = (RelatedManualLinkDetails)list.get(a);
			pstmt.setString(1, details.getTypeArticleId());
			pstmt.setString(2, details.getDynamicEntityId());
			pstmt.setString(3, details.getLocale());
			pstmt.setString(4, details.getChannelRegion());
			pstmt.setString(5, rlDetails.getMigratableReference());
			pstmt.setString(6, rlDetails.getRmLinkTitle());
			pstmt.setString(7, rlDetails.getRmEntityId());
			pstmt.setString(8, rlDetails.getRmTypeArticleId());
			pstmt.setTimestamp(9, new Timestamp(new Date().getTime()));
			pstmt.setString(10, rlDetails.getKaArticleId());
			pstmt.setString(11, rlDetails.getProcessingStatus());
			pstmt.setString(12, rlDetails.getErrorMessage());
			
			pstmt.addBatch();
			rlDetails = null;
		}
		pstmt.executeBatch();
		pstmt.close();pstmt=null;
		sql = null;
	}
	
	private void deleteAllChildInformation(Connection conn, String typeArticleId)throws SQLException
	{
		String[] tables = "DEPT_CAT_DETAILS,DEPT_UG_DETAILS,INLINE_IMAGES_DETAILS,INLINE_INRLNK_DETAILS,PRODUCT_DETAILS,RELATED_LINK_DETAILS,TOPIC_DETAILS,ATTACHMENT_DETAILS".split(",");
		PreparedStatement pstmt=null;
		String sql =null;
		for(int a=0;a<tables.length;a++)
		{
			sql = "DELETE FROM "+ tables[a]+ " WHERE VA_TYPE_ID = "+typeArticleId;
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			pstmt.close();pstmt=null;
			sql = null;
		}
		tables = null;
	}
	
	public List<TagDetails> getTagsRefKeys(List<TagDetails> tagsList, String tagType)
	{
		List<TagDetails> newTagsList = new ArrayList<TagDetails>();
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			if(null!=tagsList && tagsList.size()>0)
			{
				conn = getDestinationConnection();
				TagDetails details = null;
				String sql=null;
				TagDetails ntDetails = null;
				for(int a=0;a<tagsList.size();a++)
				{
					details = (TagDetails)tagsList.get(a);
					ntDetails = new TagDetails();
					ntDetails.setVaItemName(details.getVaItemName());
					
					sql="SELECT KA_REF_KEY,KA_RECORD_ID,KA_ITEM_NAME FROM TAGS_VS_KA_REFERENCE WHERE "
							+ "LOWER(VA_ITEM_NAME)=? AND ITEM_TYPE=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, details.getVaItemName().trim().toLowerCase());
					pstmt.setString(2, tagType);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						if(null!=rs.getString("KA_REF_KEY") && !"".equals(rs.getString("KA_REF_KEY")) && 
								null!=rs.getString("KA_RECORD_ID") && !"".equals(rs.getString("KA_RECORD_ID")) && 
								null!=rs.getString("KA_ITEM_NAME") && !"".equals(rs.getString("KA_ITEM_NAME")))
						{
							ntDetails.setKaItemRefKey(rs.getString("KA_REF_KEY"));
							ntDetails.setKaRecordId(rs.getString("KA_RECORD_ID"));
							ntDetails.setKaItemName(rs.getString("KA_ITEM_NAME"));
							ntDetails.setProcessingStatus("SUCCESS");
						}
						else
						{
							ntDetails.setProcessingStatus("FAILURE");
							ntDetails.setDataErrors("FAILED TO IDENTIFY REF KEY IN MASTER DATA.");
						}
					}
					sql  =null;
					pstmt.close();pstmt=null;
					rs.close();rs=null;
					details = null;
					// add to new List
					newTagsList.add(ntDetails);
					ntDetails = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getTagsRefKeys()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
				{
					conn.close();conn=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getTagsRefKeys()", e);
			}
		}
		return newTagsList;
	}
	
	public String getLinksKAAnswerId(String typeId)
	{
		String kaAnswerId=null;
		Connection  conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			conn = getDestinationConnection();
			String sql="SELECT KA_ARTICLE_ID FROM DOCUMENT_DETAILS WHERE VA_TYPE_ID = "+typeId;
			logger.info("getLinksKAAnswerId :: Sql :: >"+ sql);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				if(null!=rs.getString("KA_ARTICLE_ID") && !"".equals(rs.getString("KA_ARTICLE_ID")))
				{
					kaAnswerId = rs.getString("KA_ARTICLE_ID");
				}
			}
			sql = null;	
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getLinksKAAnswerId()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
				{
					conn.close();conn=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getLinksKAAnswerId()", e);
			}
		}
		return kaAnswerId;
	}
	
	public String getKARecordId(String typeId)
	{
		String recordId=null;
		Connection  conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			conn = getDestinationConnection();
			String sql="SELECT KA_RECORD_ID FROM DOCUMENT_DETAILS WHERE VA_TYPE_ID = "+typeId;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				if(null!=rs.getString("KA_RECORD_ID") && !"".equals(rs.getString("KA_RECORD_ID")))
				{
					recordId = rs.getString("KA_RECORD_ID");
				}
			}
			sql = null;	
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getKARecordId()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
				{
					conn.close();conn=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getKARecordId()", e);
			}
		}
		return recordId;
	}
	
	public List<DocumentDetails> getDocumentsDataForReport()
	{
		List<DocumentDetails> list = new ArrayList<DocumentDetails>();
		Connection  conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try
		{
			conn = getDestinationConnection();
			String sql="SELECT * FROM DOCUMENT_DETAILS ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			DocumentDetails tails = null;
			while(rs.next())
			{
				tails = new DocumentDetails();
				tails.setTypeArticleId(rs.getString("VA_TYPE_ID"));
				tails.setDynamicEntityId(rs.getString("VA_DYNAMIC_ENTITY_ID"));
				tails.setLocale(rs.getString("VA_LOCALE"));
				tails.setChannelRegion(rs.getString("VA_REG_CHANNEL"));
				tails.setTitle(rs.getString("VA_TITLE"));
				tails.setPublishStartDate(rs.getTimestamp("VA_PUB_START_DATE"));
				tails.setPublishEndDate(rs.getTimestamp("VA_PUB_END_DATE"));
				tails.setPublishedBy(rs.getString("VA_PUB_BY"));
				tails.setOwnerId(rs.getString("VA_OWNER_ID"));
				tails.setLastModifiedDate(rs.getTimestamp("VA_LAST_MOD_DATE"));
				tails.setLastModifiedBy(rs.getString("VA_LAST_MOD_BY"));
				tails.setMajorVersion(rs.getString("VA_MAJOR_VERSION"));
				tails.setMinorVersion(rs.getString("VA_MINOR_VERSION"));
				tails.setWeightValue(rs.getString("VA_WEIGHT_VALUE"));
				tails.setKaContentPattern(rs.getString("KA_CONTENT_PATTERN"));
				tails.setKaChannelRefKey(rs.getString("KA_CHANNEL_REF_KEY"));
				tails.setKaRecordId(rs.getString("KA_RECORD_ID"));
				tails.setKaDocumentId(rs.getString("KA_DOCUMENT_ID"));
				tails.setKaArticleId(rs.getString("KA_ARTICLE_ID"));
				tails.setKaVersionId(rs.getString("KA_VERSION_ID"));
				tails.setKaVersion(rs.getString("KA_VERSION"));
				tails.setKaPublishStatus(rs.getString("KA_PUB_STATUS"));
				tails.setAllUserGroupsMapped(rs.getString("ALL_UG_MAPPED"));
				tails.setAllCategoriesMapped(rs.getString("ALL_CAT_MAPPED"));
				tails.setAllProductsMapped(rs.getString("ALL_PROD_MAPPED"));
				tails.setAllInnerLinksMapped(rs.getString("ALL_INNERLINKS_MAPPED"));
				tails.setAllImagesMapped(rs.getString("ALL_IMAGES_MAPPED"));
				tails.setAllRelatedLinksMapped(rs.getString("ALL_REL_LINKS_MAPPED"));
				tails.setAllAttachmentsMapped(rs.getString("ALL_ATTACHMENTS_MAPPED"));
				tails.setProcessingStatus(rs.getString("KA_PROCESSING_STATUS"));
				if(null!=rs.getBytes("KA_ERROR_MESSAGE"))
				{
					tails.setErrorMessage(new String(rs.getBytes("KA_ERROR_MESSAGE")));
				}
				tails.setDataErrors(rs.getString("KA_DATA_ERRORS"));
				list.add(tails);
				tails = null;
			}
			sql = null;	
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getDocumentsDataForReport()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
				{
					conn.close();conn=null;
				}
				if(null!=rs)
				{
					rs.close();rs=null;
				}
				if(null!=pstmt)
				{
					pstmt.close();pstmt=null;
				}
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(DestinationDocumentsDataDAO.class.getName(), "getDocumentsDataForReport()", e);
			}
		}
		return list;
	}
}
