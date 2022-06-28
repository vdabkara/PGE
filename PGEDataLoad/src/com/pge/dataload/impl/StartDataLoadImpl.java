package com.pge.dataload.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import com.pge.dataload.dao.CreateTableDAO;
import com.pge.dataload.dao.DestinationDocumentsDataDAO;
import com.pge.dataload.dao.SourceDocumentsDataDAO;
import com.pge.dataload.osvc.OSVCandKAWebServiceCaller;
import com.pge.dataload.utils.ApplicationProperties;
import com.pge.dataload.utils.ContentSchemaUtils;
import com.pge.dataload.utils.ContentUtils;
import com.pge.dataload.utils.CustomUtils;
import com.pge.dataload.utils.Utilities;
import com.pge.dataload.vo.AttachmentDetails;
import com.pge.dataload.vo.DocumentDetails;
import com.pge.dataload.vo.InlineImageDetails;
import com.pge.dataload.vo.InnerLinkDetails;
import com.pge.dataload.vo.RelatedManualLinkDetails;
import com.pge.dataload.vo.SchemaFieldDetails;
import com.pge.dataload.vo.TagDetails;

public class StartDataLoadImpl {

	private Logger logger = Logger.getLogger(StartDataLoadImpl.class);

	private SourceDocumentsDataDAO sourceDocumentsDataDAO = null;

	private DestinationDocumentsDataDAO destinationDocumentsDataDAO = null;

	private OSVCandKAWebServiceCaller osvcCaller = null;

	public static void main(String[] args) {
		// initialize loggers
		File f = new File(StartDataLoadImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		PropertyConfigurator.configure(f.getParentFile().getAbsolutePath()+"/log.properties");
		f=  null;
		try
		{
			boolean allGood = true;
			String[] tables="DOCUMENT_DETAILS,PRODUCT_DETAILS,TOPIC_DETAILS,DEPT_CAT_DETAILS,DEPT_UG_DETAILS,INLINE_IMAGES_DETAILS,INLINE_INRLNK_DETAILS,RELATED_LINK_DETAILS,ATTACHMENT_DETAILS".split(",");
			if(null!=tables && tables.length>0)
			{
				for(int a=0;a<tables.length;a++)
				{
					allGood = CreateTableDAO.checkTableExists(tables[a], tables[a]);
					if(allGood==false)
					{
						break;
					}
				}
			}

			if(allGood==true)
			{
				StartDataLoadImpl impl = new StartDataLoadImpl();
				impl.startProcessing();
				impl = null;
			}
			tables = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "main()", e);
		}
	}

	private void startProcessing()
	{
		// get current Time
		Timestamp currentTime = new Timestamp(new Date().getTime());
		try
		{
			sourceDocumentsDataDAO = new SourceDocumentsDataDAO();
			destinationDocumentsDataDAO = new DestinationDocumentsDataDAO();
			osvcCaller =  new OSVCandKAWebServiceCaller();

			// check for last Processed time
			Timestamp lastProcessedTime = Utilities.readLastProcessedTime();
			if(null!=lastProcessedTime)
			{
				logger.info("startProcessing :: Last Processed Time is Not null, Proceed for processing Delta Data in between "+ lastProcessedTime+" and "+ currentTime);
				// read delta Data
			}
			else
			{
				// read complete Data
				logger.info("startProcessing :: Last Processed Time is null, Proceed for processing complete Data.");
			}

			List<DocumentDetails> documentsList = sourceDocumentsDataDAO.getDocumentsList(lastProcessedTime, currentTime);
			if(null!=documentsList && documentsList.size()>0)
			{
				logger.info("startProcessing :: Total Documents Found for Processing are :: >"+ documentsList.size());
				/*
				 * START ITERATING DOCUMENTS LIST, FETCH OTHER SET OF INFORMATION FOR EACH DOCUMENT
				 * PROCESS THEM IN KA
				 */
				DocumentDetails details = null;
				for(int a=0;a<documentsList.size();a++)
				{
					details = (DocumentDetails)documentsList.get(a);
					logger.info("###############################################################");
					logger.info("START :: PROCESSING "+ (a+1) +"/"+ documentsList.size()+" for Type Id :: >"+ details.getTypeArticleId());
					logger.info("###############################################################");
					// fetch tags associated with each document
					details = sourceDocumentsDataDAO.getDocumentTagsDetails(details);
					// SET DEPARTMENT LIST AS CATEGORY TOO
					if(null!=details.getDepartmentUserGroupList() && details.getDepartmentUserGroupList().size()>0)
					{
						details.setDepartmentCategoryList(new ArrayList<>());
						details.setDepartmentCategoryList(details.getDepartmentUserGroupList());
					}
					if(null!=details.getChannelRegion() && !"".equals(details.getChannelRegion()) && 
							!"tools".equalsIgnoreCase(details.getChannelRegion()) && !"forms".equalsIgnoreCase(details.getChannelRegion()))
					{
						// fetch documentTitle
						details.setTitle(sourceDocumentsDataDAO.getTitle(details.getDynamicEntityId()));
						// fetch htmlContent
						details.setHtmlContent(sourceDocumentsDataDAO.getDocumentContent(details.getDynamicEntityId()));
						if(null!=details.getHtmlContent())
						{
							// call function to remove blank style attributes form any element
							details.setHtmlContent(CustomUtils.removeEmptyStyleAttributes(details.getHtmlContent()));
							
							// call function to write HTML File to a physical location
							CustomUtils.writeHTMLContent(details.getHtmlContent(), details.getTypeArticleId(), details.getChannelRegion());

							// remove Back to Top Link
							details.setHtmlContent(CustomUtils.removeBackTotopLink(details.getHtmlContent()));
							/*
							 * identify Inner Links and Images from the Content
							 */
							details.setAssetEDImagesList(CustomUtils.getAssetEDImages(details.getHtmlContent(), details.getAssetEDImagesList()));
							if(null!=details.getAssetEDImagesList() && details.getAssetEDImagesList().size()>0)
							{
								// if Assets List is not null = get Assets Names
								details.setAssetEDImagesList(sourceDocumentsDataDAO.getAssetEDImageNames(details.getAssetEDImagesList()));
							}
							// read other Inline Images - <img tags
							details.setOtherInlineImages(CustomUtils.getOtherInlineImages(details.getHtmlContent()));

							// get Inline InnerLinks Details - CONTENTED
							details.setContentEDInnerLinksList(CustomUtils.getContentEDLinks(details.getHtmlContent(), details.getContentEDInnerLinksList()));
							// get Other Inline InnerLink Details
							details.setOtherInnerLinksList(CustomUtils.getOtherInnerLinksDetails(details.getHtmlContent()));
						}
						// fetch relatedLinks
						details.setRelatedManualLinksList(sourceDocumentsDataDAO.getRelatedManualsList(details.getDynamicEntityId()));

						// fetch attachments
						details.setAttachmentsList(sourceDocumentsDataDAO.getAttachmentDetails(details.getTypeArticleId(),details.getDynamicEntityId()));

						/*
						 * PERFORM KA OPERATION
						 *  READ CHANNEL REF KEY
						 */
						details = getChannelRefKey(details);
						/*
						 * GET TAGS KA REF KEYS FOR MAPPING
						 * FOR DEPARTMENT_UG, DEPARTMENT_CAT,TOPI,PRODUCT
						 */
						details.setTopicsList(destinationDocumentsDataDAO.getTagsRefKeys(details.getTopicsList(), "CATEGORY"));
						details.setProductList(destinationDocumentsDataDAO.getTagsRefKeys(details.getProductList(), "PRODUCT"));
						details.setDepartmentCategoryList(destinationDocumentsDataDAO.getTagsRefKeys(details.getDepartmentCategoryList(), "DEPARTMENT_CAT"));
						details.setDepartmentUserGroupList(destinationDocumentsDataDAO.getTagsRefKeys(details.getDepartmentUserGroupList(), "DEPARTMENT_UG"));

						/*
						 * PROCESS ATTACHMENTS SEARCH FILE OPERATION
						 */
						details = CustomUtils.processAttachmentsForKA(details);

						/*
						 * PROCESS INLINE IMAGES
						 */
						details = CustomUtils.processInlineImagesForKA(details);
						
						/*
						 * PROCESS OTHER INLINE IMAGES
						 */
						details = CustomUtils.processOtherInlineImagesForKA(details);
						
						/*
						 * PROCESS INNER LINKS
						 */
						details = CustomUtils.processInnerlinksForKA(details);

						/*
						 * PROCESS MANUAL LINKS
						 * & PREPARE CONTENT
						 */
						details = CustomUtils.processRelatedlinksForKA(details);

						/*
						 *  INTIALIZE SCHEMA DETAILS OBJECT
						 *  AND SET VALUES FOR EACH SCHEMA
						 *  IDENTIFY CONTENT PATTERN
						 */
						details.setSchemaDetails(new SchemaFieldDetails());
						details = ContentSchemaUtils.preparedChannelSchemaDetails(details);
						/*
						 * PROCEED FOR CREATING / MODIFYING CONTENT OPERATION
						 */
						String kaRecordId = destinationDocumentsDataDAO.getKARecordId(details.getTypeArticleId());
						if(null!=kaRecordId && !"".equals(kaRecordId))
						{
							logger.info("startProcessing :: KA Document Already Exists for Type Id "+ details.getTypeArticleId()+". Proceed for Updating Document.");
							details = updateContentOperation(details, kaRecordId);
						}
						else
						{
							logger.info("startProcessing :: NO KA Document Exists for Type Id "+ details.getTypeArticleId()+". Proceed for Creating Document.");
							details = createContentOperation(details);
						}
						kaRecordId = null;
						/*
						 * SET ALL FLAGS
						 */
						details = setAllTagsFlagsOperation(details);
					}
					else
					{
						logger.info("startProcessing :: For Type Id "+ details.getTypeArticleId()+" Region (Channel) is :: >"+ details.getChannelRegion()+". Skipping.");

						// set Document Status AS SKIPPED
						details.setProcessingStatus("SKIPPED");
						details.setDataErrors("EITHER NO REGION MAPPED OR REGION IS TOOLS / FORMS. SKIP PROCESSING.");
					}

					/*
					 * PROCEED FOR SAVING DETAILS IN DESTINATION DATABASE
					 */
					destinationDocumentsDataDAO.saveDocumentDetails(details);

					logger.info("###############################################################");
					logger.info("END :: PROCESSING "+ (a+1) +"/"+ documentsList.size()+" for Type Id :: >"+ details.getTypeArticleId());
					logger.info("###############################################################");
					details = null;
				}
			}
			else
			{
				logger.info("startProcessing :: No Documents Found for Processing. Exiting.");
			}
			documentsList = null;
			lastProcessedTime = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "startProcessing()", e);
		}
		finally
		{
			if(null!=sourceDocumentsDataDAO.conn)
			{
				try 
				{
					sourceDocumentsDataDAO.conn.close();
				} 
				catch (SQLException e) 
				{
					Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "startProcessing()", e);
				}
				sourceDocumentsDataDAO.conn=null;
			}
			sourceDocumentsDataDAO = null;
			destinationDocumentsDataDAO = null;
			osvcCaller=  null;
		}

		/*
		 * PROCEED FOR WRITING LAST PROCESSED TIME
		 */
		//		try {
		//			Utilities.writeLastProcessedTime(currentTime);
		//		} catch (IOException e) {
		//			Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "startProcessing()", e);
		//		}
		currentTime =null;
	}

	private void startProcessing_htmls()
	{
		// get current Time
		Timestamp currentTime = new Timestamp(new Date().getTime());
		try
		{
			sourceDocumentsDataDAO = new SourceDocumentsDataDAO();
			destinationDocumentsDataDAO = new DestinationDocumentsDataDAO();
			osvcCaller =  new OSVCandKAWebServiceCaller();

			// check for last Processed time
			Timestamp lastProcessedTime = Utilities.readLastProcessedTime();
			if(null!=lastProcessedTime)
			{
				logger.info("startProcessing :: Last Processed Time is Not null, Proceed for processing Delta Data in between "+ lastProcessedTime+" and "+ currentTime);
				// read delta Data
			}
			else
			{
				// read complete Data
				logger.info("startProcessing :: Last Processed Time is null, Proceed for processing complete Data.");
			}

			List<DocumentDetails> documentsList = sourceDocumentsDataDAO.getDocumentsList(lastProcessedTime, currentTime);
			if(null!=documentsList && documentsList.size()>0)
			{
				logger.info("startProcessing :: Total Documents Found for Processing are :: >"+ documentsList.size());
				/*
				 * START ITERATING DOCUMENTS LIST, FETCH OTHER SET OF INFORMATION FOR EACH DOCUMENT
				 * PROCESS THEM IN KA
				 */
				DocumentDetails details = null;
				for(int a=0;a<documentsList.size();a++)
				{
					details = (DocumentDetails)documentsList.get(a);
					logger.info("###############################################################");
					logger.info("START :: PROCESSING "+ (a+1) +"/"+ documentsList.size()+" for Type Id :: >"+ details.getTypeArticleId());
					logger.info("###############################################################");
					// fetch tags associated with each document
					details = sourceDocumentsDataDAO.getDocumentTagsDetails(details);

					if(null!=details.getChannelRegion() && !"".equals(details.getChannelRegion()) && 
							!"tools".equalsIgnoreCase(details.getChannelRegion()) && !"forms".equalsIgnoreCase(details.getChannelRegion()))
					{
						// fetch documentTitle
						details.setTitle(sourceDocumentsDataDAO.getTitle(details.getDynamicEntityId()));
						// fetch htmlContent
						details.setHtmlContent(sourceDocumentsDataDAO.getDocumentContent(details.getDynamicEntityId()));
						logger.info("title :: >"+ details.getTitle());
						logger.info("html :: >"+ details.getHtmlContent());
						if(null!=details.getHtmlContent())
						{
							File file = new File("//jwtcvpxprf02/profiles/E77645/Downloads/PGE_WD/htmls/"+details.getTypeArticleId()+".html");
							FileOutputStream fos = new FileOutputStream(file);
							fos.write(details.getHtmlContent().getBytes("utf-8"));
							fos.flush();
							fos.close();
							fos = null;
							file = null;

						}

					}
					else
					{
						logger.info("startProcessing :: For Type Id "+ details.getTypeArticleId()+" Region (Channel) is :: >"+ details.getChannelRegion()+". Skipping.");

						// set Document Status AS SKIPPED
						details.setProcessingStatus("SKIPPED");
						details.setDataErrors("EITHER NO REGION MAPPED OR REGION IS TOOLS / FORMS. SKIP PROCESSING.");
					}

					/*
					 * PROCEED FOR SAVING DETAILS IN DESTINATION DATABASE
					 */

					logger.info("###############################################################");
					logger.info("END :: PROCESSING "+ (a+1) +"/"+ documentsList.size()+" for Type Id :: >"+ details.getTypeArticleId());
					logger.info("###############################################################");
					details = null;
				}
			}
			else
			{
				logger.info("startProcessing :: No Documents Found for Processing. Exiting.");
			}
			documentsList = null;
			lastProcessedTime = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "startProcessing()", e);
		}
		finally
		{
			if(null!=sourceDocumentsDataDAO.conn)
			{
				try 
				{
					sourceDocumentsDataDAO.conn.close();
				} 
				catch (SQLException e) 
				{
					Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "startProcessing()", e);
				}
				sourceDocumentsDataDAO.conn=null;
			}
			sourceDocumentsDataDAO = null;
			destinationDocumentsDataDAO = null;
			osvcCaller=  null;
		}

		/*
		 * PROCEED FOR WRITING LAST PROCESSED TIME
		 */
		//		try {
		//			Utilities.writeLastProcessedTime(currentTime);
		//		} catch (IOException e) {
		//			Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "startProcessing()", e);
		//		}
		currentTime =null;
	}



	private DocumentDetails getChannelRefKey(DocumentDetails details)
	{
		StringTokenizer strLabel =new StringTokenizer(ApplicationProperties.getProperty("VA_CHANNEL_NAME"),",");
		StringTokenizer strValue = new StringTokenizer(ApplicationProperties.getProperty("KA_CHANNEL_REF_KEY"),",");
		StringTokenizer strRecId = new StringTokenizer(ApplicationProperties.getProperty("KA_CHANNEL_RECORD_ID"),",");
		StringTokenizer strName = new StringTokenizer(ApplicationProperties.getProperty("KA_CHANNEL_NAME"),",");

		String label=null;
		String value=  null;
		String recordId = null;
		String name = null;
		while(strLabel.hasMoreTokens() && strValue.hasMoreTokens() && strRecId.hasMoreTokens() && strName.hasMoreTokens())
		{
			label = strLabel.nextToken();
			value= strValue.nextToken();
			recordId = strRecId.nextToken();
			name = strName.nextToken();
			if(label.equalsIgnoreCase(details.getChannelRegion()))
			{
				details.setKaChannelRefKey(value);
				details.setKaChannelRecordId(recordId);
				details.setKaChannelName(name);
				break;
			}
			label = null;
			value=  null;
			name = null;
			recordId = null;
		}
		strLabel =null;
		strValue=  null;
		strRecId =null;
		strName=  null;
		return details;
	}

	private DocumentDetails setAllTagsFlagsOperation(DocumentDetails details)
	{
		// CHECK FOR TOPICS (CATEGORY)
		if(null!=details.getTopicsList() && details.getTopicsList().size()>0)
		{
			// SET ALL TOPICS (CATEGORY) MAPPED TO Y
			details.setAllCategoriesMapped("Y");
			TagDetails tags =null;
			for(int r=0;r<details.getTopicsList().size();r++)
			{
				tags = (TagDetails)details.getTopicsList().get(r);
				if(null==tags.getProcessingStatus() || "".equals(tags.getProcessingStatus()) || 
						(null!=tags.getProcessingStatus() && tags.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL TOPICS (CATEGORY) MAPPED TO N
					details.setAllCategoriesMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO FIND KA_REF_KEYS FOR SOME TOPICS IN MASTER DATA.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO FIND KA_REF_KEYS FOR SOME TOPICS IN MASTER DATA.\n");
					}
					break;
				}
				tags = null;
			}
		}

		// CHECK FOR PRODUCTS
		if(null!=details.getProductList() && details.getProductList().size()>0)
		{
			// SET ALL PRODUCTS MAPPED TO Y
			details.setAllProductsMapped("Y");
			TagDetails tags =null;
			for(int r=0;r<details.getProductList().size();r++)
			{
				tags = (TagDetails)details.getProductList().get(r);
				if(null==tags.getProcessingStatus() || "".equals(tags.getProcessingStatus()) || 
						(null!=tags.getProcessingStatus() && tags.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL PRODUCTS MAPPED TO N
					details.setAllProductsMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO FIND KA_REF_KEYS FOR SOME PRODUCTS IN MASTER DATA.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO FIND KA_REF_KEYS FOR SOME PRODUCTS IN MASTER DATA.\n");
					}
					break;
				}
				tags = null;
			}
		}

		// CHECK FOR DEPARTMENT_CAT
		if(null!=details.getDepartmentCategoryList() && details.getDepartmentCategoryList().size()>0)
		{
			// SET ALL (CATEGORY) MAPPED TO Y
			details.setAllCategoriesMapped("Y");
			TagDetails tags =null;
			for(int r=0;r<details.getDepartmentCategoryList().size();r++)
			{
				tags = (TagDetails)details.getDepartmentCategoryList().get(r);
				if(null==tags.getProcessingStatus() || "".equals(tags.getProcessingStatus()) || 
						(null!=tags.getProcessingStatus() && tags.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL (CATEGORY) MAPPED TO N
					details.setAllCategoriesMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO FIND KA_REF_KEYS FOR SOME DEPARTMENT AS CATEGORY IN MASTER DATA.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO FIND KA_REF_KEYS FOR SOME DEPARTMENT AS CATEGORY IN MASTER DATA.\n");
					}
					break;
				}
				tags = null;
			}
		}

		// CHECK FOR DEPARTMENT_UG
		if(null!=details.getDepartmentUserGroupList() && details.getDepartmentUserGroupList().size()>0)
		{
			// SET ALL (USERGROUP) MAPPED TO Y
			details.setAllUserGroupsMapped("Y");
			TagDetails tags =null;
			for(int r=0;r<details.getDepartmentUserGroupList().size();r++)
			{
				tags = (TagDetails)details.getDepartmentUserGroupList().get(r);
				if(null==tags.getProcessingStatus() || "".equals(tags.getProcessingStatus()) || 
						(null!=tags.getProcessingStatus() && tags.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL (USERGROUP) MAPPED TO N
					details.setAllUserGroupsMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO FIND KA_REF_KEYS FOR SOME DEPARTMENT AS USERGROUP IN MASTER DATA.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO FIND KA_REF_KEYS FOR SOME DEPARTMENT AS USERGROUP IN MASTER DATA.\n");
					}
					break;
				}
				tags = null;
			}
		}

		// CHECK FOR INNER LINKS
		if(null!=details.getContentEDInnerLinksList() && details.getContentEDInnerLinksList().size()>0)
		{
			// SET ALL INNERLINKS MAPPED TO Y
			details.setAllInnerLinksMapped("Y");
			InnerLinkDetails linkDetails = null;
			for(int r=0;r<details.getContentEDInnerLinksList().size();r++)
			{
				linkDetails=  (InnerLinkDetails)details.getContentEDInnerLinksList().get(r);
				if(null==linkDetails.getProcessingStatus() || "".equals(linkDetails.getProcessingStatus()) || 
						(null!=linkDetails.getProcessingStatus() && linkDetails.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL INNERLINKS MAPPED TO N
					details.setAllInnerLinksMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO FIND KA_ANSWER_IDS FOR SOME INNERLINKS.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO FIND KA_ANSWER_IDS FOR SOME INNERLINKS.\n");
					}
					break;
				}
				linkDetails = null;
			}
		}

		// CHECK FOR INLINE IMAGES
		if(null!=details.getAssetEDImagesList() && details.getAssetEDImagesList().size()>0)
		{
			// SET ALL IMAGES MAPPED TO Y
			details.setAllImagesMapped("Y");
			InlineImageDetails images = null;
			for(int r=0;r<details.getAssetEDImagesList().size();r++)
			{
				images=  (InlineImageDetails)details.getAssetEDImagesList().get(r);
				if(null==images.getProcessingStatus() || "".equals(images.getProcessingStatus()) || 
						(null!=images.getProcessingStatus() && images.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL IMAGES MAPPED TO N
					details.setAllImagesMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO TRANSFORM SOME INLINE IMAGES TO BASE64.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO TRANSFORM SOME INLINE IMAGES T0 BASE64.\n");
					}
					break;
				}
				images = null;
			}
		}

		// CHECK FOR OTHER INLINE IMAGES
		if(null!=details.getOtherInlineImages() && details.getOtherInlineImages().size()>0)
		{
			// SET ALL IMAGES MAPPED TO Y - DO NOT SET FLAG EXPLICITYLY AS IT CAN BE Y / N BASED ON ASSET ED LIST
			// SET it as Y only when setAllImagesMapped is NULL
			if(null==details.getAllImagesMapped() || "".equals(details.getAllImagesMapped()))
			{
				// e.g. no ASSET ED IMAGES FOUND
				details.setAllImagesMapped("Y");
			}
			InlineImageDetails images = null;
			for(int r=0;r<details.getOtherInlineImages().size();r++)
			{
				images=  (InlineImageDetails)details.getOtherInlineImages().get(r);
				if(null==images.getProcessingStatus() || "".equals(images.getProcessingStatus()) || 
						(null!=images.getProcessingStatus() && images.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL IMAGES MAPPED TO N
					details.setAllImagesMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO TRANSFORM SOME OTHER TYPE INLINE IMAGES TO BASE64.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO TRANSFORM SOME OTHER TYPE INLINE IMAGES T0 BASE64.\n");
					}
					break;
				}
				images = null;
			}
		}

		// CHECK FOR ATTACHMENTS
		if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
		{
			// SET ALL ATTACHMENTS TO Y
			details.setAllAttachmentsMapped("Y");
			AttachmentDetails attachDetails = null;
			for(int a=0;a<details.getAttachmentsList().size();a++)
			{
				attachDetails = (AttachmentDetails)details.getAttachmentsList().get(a);
				if(null==attachDetails.getProcessingStatus() || "".equals(attachDetails.getProcessingStatus()) || 
						(null!=attachDetails.getProcessingStatus() && attachDetails.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL ATTACHMENTS MAPPED TO N
					details.setAllAttachmentsMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO IDENTIFY SOME ATTACHMENTS FOR THE DOCUMENT.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO IDENTIFY SOME ATTACHMENTS FOR THE DOCUMENT.\n");
					}
					break;
				}
				attachDetails = null;
			}
		}

		// CHECK FOR RELATED LINKS
		if(null!=details.getRelatedManualLinksList() && details.getRelatedManualLinksList().size()>0)
		{
			// SET ALL RELATED LINKS TO Y
			details.setAllRelatedLinksMapped("Y");
			RelatedManualLinkDetails rlDetails = null;
			for(int a=0;a<details.getRelatedManualLinksList().size();a++)
			{
				rlDetails = (RelatedManualLinkDetails)details.getRelatedManualLinksList().get(a);
				if(null==rlDetails.getProcessingStatus() || "".equals(rlDetails.getProcessingStatus()) || 
						(null!=rlDetails.getProcessingStatus() && rlDetails.getProcessingStatus().equals("FAILURE")))
				{
					// SET ALL RELATED LINKS MAPPED TO N
					details.setAllRelatedLinksMapped("N");
					// SET CUSTOM MESSAGE
					if(null!=details.getDataErrors() && !"".equals(details.getDataErrors()))
					{
						details.setDataErrors(details.getDataErrors()+"FAILED TO IDENTIFY SOME RELATED LINKS FOR THE DOCUMENT.\n");
					}
					else
					{
						details.setDataErrors("FAILED TO IDENTIFY SOME RELATED LINKS FOR THE DOCUMENT.\n");
					}
					break;
				}
				rlDetails = null;
			}
		}
		return details;
	}

	private DocumentDetails createContentOperation(DocumentDetails details)
	{
		try
		{
			/*
			 * CREATE SCHEMA PAYLOAD
			 */
			JSONObject payloadObj = ContentUtils.createPayLoad(details);
			logger.info("Create Content :: >"+ payloadObj.toString());
			List<File> uploadFiles = null;
			if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
			{
				AttachmentDetails attach =null;
				File upFile = null;
				for(int a=0;a<details.getAttachmentsList().size();a++)
				{
					attach = (AttachmentDetails)details.getAttachmentsList().get(a);
					if(null!=attach.getProcessingStatus() && attach.getProcessingStatus().equals("SUCCESS") && 
							null!=attach.getAttachmentPath() && !"".equals(attach.getAttachmentPath()))
					{
						upFile = new File(attach.getAttachmentPath());
						if(upFile.exists() && upFile.isFile())
						{
							if(null==uploadFiles || uploadFiles.size()<=0)
							{
								uploadFiles = new ArrayList<File>();
							}
							uploadFiles.add(upFile);
						}
						upFile= null;
					}
					attach = null;
				}
			}
			// INVOKE CREATE CONTENT SERVICE
			JSONObject responseObj = osvcCaller.createOperation(payloadObj.toString(), uploadFiles, "content");
			if(null!=responseObj)
			{
				if(null!=responseObj.get("CALL_STATUS") && responseObj.get("CALL_STATUS").equals("SUCCESS"))
				{
					logger.info("createContentOperation :: KA Document created Successfully for Type Id ::> "+ details.getTypeArticleId());
					JSONObject successObj = null;
					try
					{
						successObj =responseObj.getJSONObject("FINAL_DATA");
						if(null!=successObj)
						{
							// set recordId
							details.setKaRecordId(successObj.getString("recordId"));
							// set versionId
							details.setKaVersionId(successObj.getString("versionId"));
							// set documentId
							details.setKaDocumentId(successObj.getString("documentId"));
							// set version
							details.setKaVersion(successObj.getString("version"));
							// set answerId
							details.setKaArticleId(successObj.getString("answerId"));
							// set publishStatus
							details.setKaPublishStatus("FALSE");
						}
					}catch(Exception e) {}

					if(null!=details.getKaRecordId() && !"".equals(details.getKaRecordId()) && 
							null!=details.getKaVersionId() && !"".equals(details.getKaVersionId()))
					{
						try
						{
							// PROCEED FOR PUBLISHING DOCUMENT
							JSONObject pubResponseObj= osvcCaller.callKAWebservice("content/versions/"+details.getKaVersionId()+"/publishThisVersion","POST");
							if(null!=pubResponseObj)
							{
								if(null!=pubResponseObj.get("CALL_STATUS") && pubResponseObj.get("CALL_STATUS").equals("SUCCESS"))
								{
									JSONObject pubSuccessObj = null;
									boolean anyException=false;
									try
									{
										pubSuccessObj = pubResponseObj.getJSONObject("FINAL_DATA");
										// set publishedVersionId
										details.setKaVersionId(pubSuccessObj.getString("versionId"));
										// set publishedVersion
										details.setKaVersion(pubSuccessObj.getString("version"));
										// set publishStatus
										details.setKaPublishStatus("TRUE");
										// set PROCESSING STATUS TO SUCCESS
										details.setProcessingStatus("SUCCESS");
									}catch(Exception e) {anyException=true;}

									if(anyException==true)
									{
										// set PROCESSING STATUS TO FAILURE
										details.setProcessingStatus("FAILURE");
										logger.info("createContentOperation :: Failed to read KA Response Object from Publish Content API Call and retrieve Latest Vesion Details.");
										details.setErrorMessage("Failed to read KA Response Object from Publish Content API Call and retrieve Latest Vesion Details for KA Document.");
									}
								}
								else
								{
									logger.info("createContentOperation :: Failed to perform Publish Content Operation in KA for Type Id ::> "+ details.getTypeArticleId());
									// set PROCESSING STATUS TO FAILURE
									details.setProcessingStatus("FAILURE");
									try
									{
										if(null!=pubResponseObj.getJSONObject("ERROR_DATA"))
										{
											details.setErrorMessage(pubResponseObj.getJSONObject("ERROR_DATA").toString());
										}
									}
									catch(Exception e) {}

									if(null!=pubResponseObj.get("ERROR_MESSAGE") && !"".equals(pubResponseObj.get("ERROR_MESSAGE")))
									{
										if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
										{
											details.setErrorMessage(details.getErrorMessage()+"\n"+pubResponseObj.get("ERROR_MESSAGE").toString());
										}
										else
										{
											details.setErrorMessage(pubResponseObj.get("ERROR_MESSAGE").toString());
										}
									}
								}
							}
							else
							{
								// set PROCESSING STATUS TO FAILURE
								details.setProcessingStatus("FAILURE");
								logger.info("createContentOperation :: Failed to read KA Response Object from Publish Content API Call.");
								details.setErrorMessage("Failed to read KA Response Object from Publish Content API Call for KA Document.");
							}
							pubResponseObj = null;
						}
						catch(Exception e)
						{
							// set PROCESSING STATUS TO FAILURE
							Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "createContentOperation()", e);
							details.setProcessingStatus("FAILURE");
							details.setErrorMessage("Failed to perform Publish Content Operation. Exception :: >" + e.getMessage());
						}
					}
					else
					{
						// set PROCESSING STATUS TO FAILURE
						details.setProcessingStatus("FAILURE");
						logger.info("createContentOperation :: Failed to read KA Response Object from Create Content API Call.");
						details.setErrorMessage("Failed to read KA Response Object from Create Content API Call and retrieve KA Document details.");
					}
					successObj = null;
				}
				else
				{
					logger.info("createContentOperation :: Failed to perform Create Content Operation in KA for Type Id ::> "+ details.getTypeArticleId());
					// set PROCESSING STATUS TO FAILURE
					details.setProcessingStatus("FAILURE");
					try
					{
						if(null!=responseObj.getJSONObject("ERROR_DATA"))
						{
							details.setErrorMessage(responseObj.getJSONObject("ERROR_DATA").toString());
						}
					}
					catch(Exception e) {}

					if(null!=responseObj.get("ERROR_MESSAGE") && !"".equals(responseObj.get("ERROR_MESSAGE")))
					{
						if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
						{
							details.setErrorMessage(details.getErrorMessage()+"\n"+responseObj.get("ERROR_MESSAGE").toString());
						}
						else
						{
							details.setErrorMessage(responseObj.get("ERROR_MESSAGE").toString());
						}
					}
				}
			}
			else
			{
				logger.info("createContentOperation :: Response Obj is null. Set Document Processing status to FAILURE.");
				details.setProcessingStatus("FAILURE");
				details.setErrorMessage("Failed to perform Create Content Operation. Response received from API Call is NULL.");
			}
			payloadObj = null;
			uploadFiles = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "createContentOperation()", e);
			details.setProcessingStatus("FAILURE");
			details.setErrorMessage("Failed to perform Create Content Operation. Exception :: >" + e.getMessage());
		}
		return details;
	}

	private DocumentDetails updateContentOperation(DocumentDetails details,String kaRecordId)
	{
		try
		{
			/*
			 * CREATE SCHEMA PAYLOAD
			 */
			JSONObject payloadObj = ContentUtils.createPayLoad(details);
			logger.info("Update Content :: >"+ payloadObj.toString());
			List<File> uploadFiles = null;
			if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
			{
				AttachmentDetails attach =null;
				File upFile = null;
				for(int a=0;a<details.getAttachmentsList().size();a++)
				{
					attach = (AttachmentDetails)details.getAttachmentsList().get(a);
					if(null!=attach.getProcessingStatus() && attach.getProcessingStatus().equals("SUCCESS") && 
							null!=attach.getAttachmentPath() && !"".equals(attach.getAttachmentPath()))
					{
						upFile = new File(attach.getAttachmentPath());
						if(upFile.exists() && upFile.isFile())
						{
							if(null==uploadFiles || uploadFiles.size()<=0)
							{
								uploadFiles = new ArrayList<File>();
							}
							uploadFiles.add(upFile);
						}
						upFile= null;
					}
					attach = null;
				}
			}

			/*
			 * FETCH DOCUMENT DATA FROM KA FOR RECORD ID TO GET BELOW DETAILS
			 * recordId
			 * versionId
			 * documentId
			 * version
			 * answerId
			 * dateModified
			 * dateAdded
			 * createDate
			 * as these will be needed in PayLoad for performing Update Operation
			 */
			boolean proceedFurther = false;
			JSONObject getDocResponseObj = osvcCaller.callKAWebservice("content/"+kaRecordId+"?contentState=LATESTVALID","GET");
			try
			{
				if(null!=getDocResponseObj)
				{
					if(null!=getDocResponseObj.get("CALL_STATUS") && getDocResponseObj.get("CALL_STATUS").equals("SUCCESS"))
					{
						JSONObject getDocSuccessObj = null;
						boolean allRequiredDetailsRead = true;
						try
						{
							getDocSuccessObj = getDocResponseObj.getJSONObject("FINAL_DATA");
							// set recordId
							payloadObj.put("recordId", getDocSuccessObj.get("recordId"));
							// set versionId
							payloadObj.put("versionId", getDocSuccessObj.get("versionId"));
							// set version
							payloadObj.put("version", getDocSuccessObj.get("version"));
							// set documentId
							payloadObj.put("documentId", getDocSuccessObj.get("documentId"));
							// set answerId
							payloadObj.put("answerId", getDocSuccessObj.get("answerId"));
							// set dateModified
							payloadObj.put("dateModified", getDocSuccessObj.get("dateModified"));
							// set dateAdded
							payloadObj.put("dateAdded", getDocSuccessObj.get("dateAdded"));
							// set createDate
							payloadObj.put("createDate", getDocSuccessObj.get("createDate"));

							// set proceedFurther to true
							proceedFurther = true;
						}catch(Exception e) {allRequiredDetailsRead=false;}

						if(allRequiredDetailsRead==false)
						{
							logger.info("updateContentOperation :: Failed to read required details for Updating Document from GET Document Response.");
							details.setProcessingStatus("FAILURE");
							details.setErrorMessage("Failed to read required details for Updating Document from GET Document Response.");
						}
						getDocSuccessObj = null;
					}
					else
					{
						logger.info("updateContentOperation :: Failed to GET Document while performing Update Content Operation in KA for Type Id ::> "+ details.getTypeArticleId());
						// set PROCESSING STATUS TO FAILURE
						details.setProcessingStatus("FAILURE");
						try
						{
							if(null!=getDocResponseObj.getJSONObject("ERROR_DATA"))
							{
								details.setErrorMessage(getDocResponseObj.getJSONObject("ERROR_DATA").toString());
							}
						}
						catch(Exception e) {}

						if(null!=getDocResponseObj.get("ERROR_MESSAGE") && !"".equals(getDocResponseObj.get("ERROR_MESSAGE")))
						{
							if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
							{
								details.setErrorMessage(details.getErrorMessage()+"\n"+getDocResponseObj.get("ERROR_MESSAGE").toString());
							}
							else
							{
								details.setErrorMessage(getDocResponseObj.get("ERROR_MESSAGE").toString());
							}
						}
					}
				}
				else
				{
					logger.info("updateContentOperation :: Response Obj is null. Set Document Processing status to FAILURE.");
					details.setProcessingStatus("FAILURE");
					details.setErrorMessage("Failed to GET Document while performing Update Content Operation. Response received from API Call is NULL.");
				}
			}
			catch(Exception e)
			{
				Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "updateContentOperation()", e);
				details.setProcessingStatus("FAILURE");
				details.setErrorMessage("Failed to GET Document while performing Update Content Operation in KA. Exception :: >" + e.getMessage());
			}
			getDocResponseObj = null;


			if(proceedFurther==true)
			{
				/*
				 * PROCEED FOR UPDATE OPERATION
				 */
				// INVOKE UPDATE CONTENT SERVICE
				JSONObject responseObj = osvcCaller.updateOperation(payloadObj.toString(), uploadFiles, "content/"+kaRecordId);
				if(null!=responseObj)
				{
					if(null!=responseObj.get("CALL_STATUS") && responseObj.get("CALL_STATUS").equals("SUCCESS"))
					{
						logger.info("updateContentOperation :: KA Document updated Successfully for Type Id ::> "+ details.getTypeArticleId());
						JSONObject successObj = null;
						try
						{
							successObj =responseObj.getJSONObject("FINAL_DATA");
							if(null!=successObj)
							{
								// set recordId
								details.setKaRecordId(successObj.getString("recordId"));
								// set versionId
								details.setKaVersionId(successObj.getString("versionId"));
								// set documentId
								details.setKaDocumentId(successObj.getString("documentId"));
								// set version
								details.setKaVersion(successObj.getString("version"));
								// set answerId
								details.setKaArticleId(successObj.getString("answerId"));
								// set publishStatus
								details.setKaPublishStatus("FALSE");
							}
						}catch(Exception e) {}

						if(null!=details.getKaRecordId() && !"".equals(details.getKaRecordId()) && 
								null!=details.getKaVersionId() && !"".equals(details.getKaVersionId()))
						{
							try
							{
								// PROCEED FOR PUBLISHING DOCUMENT
								JSONObject pubResponseObj= osvcCaller.callKAWebservice("content/versions/"+details.getKaVersionId()+"/publishThisVersion","POST");
								if(null!=pubResponseObj)
								{
									if(null!=pubResponseObj.get("CALL_STATUS") && pubResponseObj.get("CALL_STATUS").equals("SUCCESS"))
									{
										JSONObject pubSuccessObj = null;
										boolean anyException=false;
										try
										{
											pubSuccessObj = pubResponseObj.getJSONObject("FINAL_DATA");
											// set publishedVersionId
											details.setKaVersionId(pubSuccessObj.getString("versionId"));
											// set publishedVersion
											details.setKaVersion(pubSuccessObj.getString("version"));
											// set publishStatus
											details.setKaPublishStatus("TRUE");
											// set PROCESSING STATUS TO SUCCESS
											details.setProcessingStatus("SUCCESS");
										}catch(Exception e) {anyException=true;}

										if(anyException==true)
										{
											// set PROCESSING STATUS TO FAILURE
											details.setProcessingStatus("FAILURE");
											logger.info("updateContentOperation :: Failed to read KA Response Object from Publish Content API Call and retrieve Latest Vesion Details.");
											details.setErrorMessage("Failed to read KA Response Object from Publish Content API Call and retrieve Latest Vesion Details for KA Document.");
										}
									}
									else
									{
										logger.info("updateContentOperation :: Failed to perform Publish Content Operation in KA for Type Id ::> "+ details.getTypeArticleId());
										// set PROCESSING STATUS TO FAILURE
										details.setProcessingStatus("FAILURE");
										try
										{
											if(null!=pubResponseObj.getJSONObject("ERROR_DATA"))
											{
												details.setErrorMessage(pubResponseObj.getJSONObject("ERROR_DATA").toString());
											}
										}
										catch(Exception e) {}

										if(null!=pubResponseObj.get("ERROR_MESSAGE") && !"".equals(pubResponseObj.get("ERROR_MESSAGE")))
										{
											if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
											{
												details.setErrorMessage(details.getErrorMessage()+"\n"+pubResponseObj.get("ERROR_MESSAGE").toString());
											}
											else
											{
												details.setErrorMessage(pubResponseObj.get("ERROR_MESSAGE").toString());
											}
										}
									}
								}
								else
								{
									// set PROCESSING STATUS TO FAILURE
									details.setProcessingStatus("FAILURE");
									logger.info("updateContentOperation :: Failed to read KA Response Object from Publish Content API Call.");
									details.setErrorMessage("Failed to read KA Response Object from Publish Content API Call for KA Document.");
								}
								pubResponseObj = null;
							}
							catch(Exception e)
							{
								// set PROCESSING STATUS TO FAILURE
								Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "updateContentOperation()", e);
								details.setProcessingStatus("FAILURE");
								details.setErrorMessage("Failed to perform Publish Content Operation. Exception :: >" + e.getMessage());
							}
						}
						else
						{
							// set PROCESSING STATUS TO FAILURE
							details.setProcessingStatus("FAILURE");
							logger.info("updateContentOperation :: Failed to read KA Response Object from Update Content API Call.");
							details.setErrorMessage("Failed to read KA Response Object from Update Content API Call and retrieve KA Document details.");
						}
						successObj = null;
					}
					else
					{
						logger.info("createContentOperation :: Failed to perform Update Content Operation in KA for Type Id ::> "+ details.getTypeArticleId());
						// set PROCESSING STATUS TO FAILURE
						details.setProcessingStatus("FAILURE");
						try
						{
							if(null!=responseObj.getJSONObject("ERROR_DATA"))
							{
								details.setErrorMessage(responseObj.getJSONObject("ERROR_DATA").toString());
							}
						}
						catch(Exception e) {}

						if(null!=responseObj.get("ERROR_MESSAGE") && !"".equals(responseObj.get("ERROR_MESSAGE")))
						{
							if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
							{
								details.setErrorMessage(details.getErrorMessage()+"\n"+responseObj.get("ERROR_MESSAGE").toString());
							}
							else
							{
								details.setErrorMessage(responseObj.get("ERROR_MESSAGE").toString());
							}
						}
					}
				}
				else
				{
					logger.info("createContentOperation :: Response Obj is null. Set Document Processing status to FAILURE.");
					details.setProcessingStatus("FAILURE");
					details.setErrorMessage("Failed to perform Update Content Operation. Response received from API Call is NULL.");
				}
			}

			payloadObj = null;
			uploadFiles = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartDataLoadImpl.class.getName(), "updateContentOperation()", e);
			details.setProcessingStatus("FAILURE");
			details.setErrorMessage("Failed to perform Update Content Operation. Exception :: >" + e.getMessage());
		}
		return details;
	}

}
