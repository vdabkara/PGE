package com.pge.dataload.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pge.dataload.dao.DestinationDocumentsDataDAO;
import com.pge.dataload.vo.AttachmentDetails;
import com.pge.dataload.vo.DocumentDetails;
import com.pge.dataload.vo.InlineImageDetails;
import com.pge.dataload.vo.InnerLinkDetails;
import com.pge.dataload.vo.RelatedManualLinkDetails;


public class CustomUtils {

	public static List<InlineImageDetails> getAssetEDImages(String content, List<InlineImageDetails> imagesList)
	{
		try
		{
			if(null!=content && !"".equals(content))
			{
				InlineImageDetails details = null;
				String temp=null;
				String imageTag  =null;
				String contentAfter=null;
				while(content.indexOf("[[[[AssetED")!=-1)
				{
					imageTag = content.substring(content.indexOf("[[[[AssetED"),content.indexOf("]]]]")+4);
					imageTag = imageTag.replace("&quot;", "\"");
					content = content.substring(content.indexOf("]]]]")+4,content.length());
					if(null!=imageTag && !"".equals(imageTag))
					{
						boolean addToList = true;
						if(null!=imagesList && imagesList.size()>0)
						{
							for(int a=0;a<imagesList.size();a++)
							{
								if(imagesList.get(a).toString().equalsIgnoreCase(imageTag))
								{
									// image already added
									addToList = false;
									break;
								}
							}
						}

						if(addToList==true)
						{
							details = new InlineImageDetails();
							details.setTagContent(imageTag);
							// get AEEST ED VALUE
							temp = imageTag;
							temp = temp.replace("[[[[AssetED.", "");
							temp = temp.replace("]]]]", "");
							if(null!=temp)
							{
								temp = temp.trim();
								if(temp.indexOf(" ")!=-1)
								{
									details.setAssetId(temp.substring(0, temp.indexOf(" ")));
								}
								else
								{
									details.setAssetId(temp);
								}
							}
							temp  =null;
							// get width
							if(imageTag.indexOf("width=\"")!=-1)
							{
								contentAfter =imageTag.substring(imageTag.indexOf("width=\"")+7, imageTag.length());
								details.setWidth(contentAfter.substring(0, contentAfter.indexOf("\"")));
								contentAfter = null;
							}
							// get height
							if(imageTag.indexOf("height=\"")!=-1)
							{
								contentAfter =imageTag.substring(imageTag.indexOf("height=\"")+8, imageTag.length());
								details.setHeight(contentAfter.substring(0, contentAfter.indexOf("\"")));
								contentAfter = null;
							}
							// set Image Type as ASSETED
							details.setImageType("ASSETED");

							if(null!=details.getAssetId() && !"".equals(details.getAssetId()))
							{
								if(null==imagesList || imagesList.size()<=0)
								{
									imagesList = new ArrayList<InlineImageDetails>();
								}
								imagesList.add(details);
							}
							details = null;
						}
					}
					imageTag = null;
				}
			}

		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "getAssetEDImages()", e);
		}
		return imagesList;
	}

	public static List<InlineImageDetails> getOtherInlineImages(String htmlContent)
	{
		List<InlineImageDetails> imagesList = null;
		try
		{
			Document document = Jsoup.parse(htmlContent);
			Elements imgTagsList = document.select("img");
			if(null!=imgTagsList && imgTagsList.size()>0)
			{
				Element element = null;
				String srcValue = null;
				InlineImageDetails details = null;
				for(int a=0;a<imgTagsList.size();a++)
				{
					element =(Element)imgTagsList.get(a);
					srcValue = element.attr("src");
					details = new InlineImageDetails();
					// set Tag Content
					details.setTagContent(element.outerHtml());
					// set srcValue
					details.setSrcAttributeValue(srcValue);
					// check for width
					details.setWidth(element.attr("width"));
					details.setHeight(element.attr("height"));
					// set type as OTHER_IMAGES
					details.setImageType("OTHER_IMAGES");
					// set Name
					/*
					 * CHECK IF SRC CONTAINS gtxResource=
					 */
					if(srcValue.toLowerCase().indexOf("gtxResource=".toLowerCase())!=-1)
					{
						String gtxResValue = srcValue.substring(srcValue.toLowerCase().indexOf("gtxResource=".toLowerCase())+"gtxResource=".length()+1, srcValue.length());
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
						
						// set Image Name
						if(null!=gtxResValue && !"".equals(gtxResValue))
						{
							details.setImageName(gtxResValue);
						}
						gtxResValue = null;
						// NO NEED OF SETTING PROCESSING STATUS HERE
					}
					else
					{
						if(srcValue.lastIndexOf("/")!=-1)
						{
							details.setImageName(srcValue.substring(srcValue.lastIndexOf("/")+1, srcValue.length()));
						}
						else
						{
							details.setImageName(srcValue);
						}
						// set processing Status as AS_IS for all these
						details.setProcessingStatus("AS_IS");
					}
					
					if(null==imagesList || imagesList.size()<=0)
					{
						imagesList = new ArrayList<InlineImageDetails>();
					}
					imagesList.add(details);
					details = null;
					srcValue = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "getOtherInlineImages()", e);
		}
		return imagesList;
	}

	public static List<InnerLinkDetails> getContentEDLinks(String content, List<InnerLinkDetails> innerlinksList)
	{
		try
		{
			if(null!=content && !"".equals(content))
			{
				InnerLinkDetails details = null;
				String temp=null;
				String innerLinkTag  =null;
				String[] tok = null;
				while(content.toLowerCase().indexOf("[[--ContentED".toLowerCase())!=-1)
				{
					innerLinkTag = content.substring(content.toLowerCase().indexOf("[[--ContentED".toLowerCase()),content.indexOf("--]]")+4);
					innerLinkTag = innerLinkTag.replace("&quot;", "\"");
					content = content.substring(content.indexOf("--]]")+4,content.length());
					if(null!=innerLinkTag && !"".equals(innerLinkTag))
					{
						boolean addToList = true;
						if(null!=innerlinksList && innerlinksList.size()>0)
						{
							for(int a=0;a<innerlinksList.size();a++)
							{
								if(innerlinksList.get(a).toString().equalsIgnoreCase(innerLinkTag))
								{
									// innerLink already added
									addToList = false;
									break;
								}
							}
						}

						if(addToList==true)
						{
							details = new InnerLinkDetails();
							details.setTagContent(innerLinkTag);
							// get CONTENT ED VALUE
							temp = innerLinkTag;
							temp = temp.replace("[[--ContentED.", "");
							temp = temp.replace("[[--contented.", "");
							temp = temp.replace("[[--CONTENTED.", "");
							temp = temp.replace("[[--Contented.", "");
							
							temp = temp.replace("--]]", "");
							if(null!=temp)
							{
								temp = temp.trim();
								tok = temp.split("\\|\\|");
								if(null!=tok && tok.length>=3)
								{
									details.setInnerLinkTitle(tok[1]);
									details.setInnerLinkTypeId(tok[2]);
									details.setInnerLinkTypeId(details.getInnerLinkTypeId().replace("KM", ""));
								}
								tok = null;
							}
							temp  =null;

							// set Image Type as ASSETED
							details.setInnerLinkType("CONTENTED");

							if(null!=details.getInnerLinkTypeId() && !"".equals(details.getInnerLinkTypeId()))
							{
								if(null==innerlinksList || innerlinksList.size()<=0)
								{
									innerlinksList = new ArrayList<InnerLinkDetails>();
								}
								innerlinksList.add(details);
							}
							details = null;
						}
					}
					innerLinkTag = null;
				}
			}

		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "getContentEDLinks()", e);
		}
		return innerlinksList;
	}

	public static List<InnerLinkDetails> getOtherInnerLinksDetails(String htmlContent)
	{
		List<InnerLinkDetails> innerLinksList = null;
		try
		{
			Document document = Jsoup.parse(htmlContent);
			Elements aTagsList = document.select("a");
			if(null!=aTagsList && aTagsList.size()>0)
			{
				Element element = null;
				String hrefValue = null;
				InnerLinkDetails details = null;
				for(int a=0;a<aTagsList.size();a++)
				{
					element =(Element)aTagsList.get(a);
					hrefValue = element.attr("href");
					details = new InnerLinkDetails();
					details.setHrefAttributeValue(hrefValue);
					details.setInnerLinkType("OTHER_LINKS");
					// set Processing status as AS_IS for all these
					details.setProcessingStatus("AS_IS");
					if(null==innerLinksList || innerLinksList.size()<=0)
					{
						innerLinksList = new ArrayList<InnerLinkDetails>();
					}
					innerLinksList.add(details);
					details = null;
					hrefValue = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "getOtherInnerLinksDetails()", e);
		}
		return innerLinksList;
	}

	public static DocumentDetails processInnerlinksForKA(DocumentDetails details)
	{
		DestinationDocumentsDataDAO destDao = new DestinationDocumentsDataDAO();
		try
		{
			if(null!=details.getContentEDInnerLinksList() && details.getContentEDInnerLinksList().size()>0)
			{
				InnerLinkDetails linkDetails = null;
				for(int a=0;a<details.getContentEDInnerLinksList().size();a++)
				{
					linkDetails=  (InnerLinkDetails)details.getContentEDInnerLinksList().get(a);
					// fetch Answer Id for the InnerLink
					if(null!=linkDetails.getInnerLinkTypeId() && !"".equals(linkDetails.getInnerLinkTypeId()))
					{
						linkDetails.setKaDocumentId(destDao.getLinksKAAnswerId(linkDetails.getInnerLinkTypeId()));
					}
					if(null!=linkDetails.getKaDocumentId() && !"".equals(linkDetails.getKaDocumentId()))
					{
						// PREPARE KA DOCUMENT URL
						if(null==linkDetails.getInnerLinkTitle())
						{
							linkDetails.setInnerLinkTitle("");
						}
//						linkDetails.setKaInnerLinkURL("<span data-answerid=\""+linkDetails.getKaDocumentId()+"\" data-contents=\""+linkDetails.getInnerLinkTitle()+"\" data-linktype=\"1\" title=\""+linkDetails.getInnerLinkTitle()+"\">"+linkDetails.getInnerLinkTitle()+"</span>");
						linkDetails.setKaInnerLinkURL("<a href=\"/app/answers/answer_view/a_id/"+linkDetails.getKaDocumentId()+"\">"+linkDetails.getInnerLinkTitle()+"</a>");
						// set InnerLink Processing status as SUCCESS
						linkDetails.setProcessingStatus("SUCCESS");
					}
					else
					{
						// PREPARE KA DOCUMENT URL
						if(null==linkDetails.getInnerLinkTitle())
						{
							linkDetails.setInnerLinkTitle("");
						}
//						linkDetails.setKaInnerLinkURL("<span data-answerid=\"\" data-contents=\""+linkDetails.getInnerLinkTitle()+"\" data-linktype=\"1\" title=\""+linkDetails.getInnerLinkTitle()+"\">"+linkDetails.getInnerLinkTitle()+"</span>");
						linkDetails.setKaInnerLinkURL("<a href=\"#\">"+linkDetails.getInnerLinkTitle()+"</a>");
						// set InnerLink Processing status as FAILURE
						linkDetails.setProcessingStatus("FAILURE");
						linkDetails.setErrorMessage("FAILED TO IDENTIFY KA ANSWER ID FOR THE INNERLINK TYPE ID.");
					}

					//  replace &quot; by " in content
					details.setHtmlContent(details.getHtmlContent().replace("&quot;", "\""));
					// replace with CONTENT ED TAG IN HTML CONTENT
					details.setHtmlContent(details.getHtmlContent().replace(linkDetails.getTagContent(), linkDetails.getKaInnerLinkURL()));
					linkDetails = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processInnerlinksForKA()", e);
		}
		finally
		{
			destDao = null;
		}
		return details;
	}

	public static DocumentDetails processInlineImagesForKA(DocumentDetails details)
	{
		try
		{
			if(null!=details.getAssetEDImagesList() && details.getAssetEDImagesList().size()>0)
			{
				File imageFile = null;
				String base64String=null;
				byte[] fileContent=null;
				String replaceTag=null;
				InlineImageDetails imageDetails = null;
				for(int a=0;a<details.getAssetEDImagesList().size();a++)
				{
					imageDetails=  (InlineImageDetails)details.getAssetEDImagesList().get(a);
					// start preparing image tag for replace
					replaceTag="<img ";
					// add width
					if(null!=imageDetails.getWidth() && !"".equals(imageDetails.getWidth()))
					{
						replaceTag+=" width=\""+imageDetails.getWidth()+"\"";
					}
					// add height
					if(null!=imageDetails.getHeight() && !"".equals(imageDetails.getHeight()))
					{
						replaceTag+=" height=\""+imageDetails.getHeight()+"\"";
					}
					if(null!=imageDetails.getImageName() && !"".equals(imageDetails.getImageName()))
					{
						// CHECK FOR THE IMAGE FILE IF EXISTS AT SOURCE LOCATION OR NOT
						imageFile = searchImageFile(imageDetails.getImageName());
						if(null!=imageFile && imageFile.isFile() && imageFile.exists())
						{
							// add Image Name to Image Path
							imageDetails.setImagePath(imageFile.getAbsolutePath());
							
							// CONVERT IMAGE FILE TO BASE 64 STRING
							try
							{
								fileContent = FileUtils.readFileToByteArray(imageFile);
								base64String = Base64.getEncoder().encodeToString(fileContent);
							}
							catch(Exception e)
							{
								Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processInlineImagesForKA()", e);	
							}

							// CHECK FOR BASE64 STRING
							if(null!=base64String && !"".equals(base64String))
							{
								replaceTag+="src=\"";
								if(imageDetails.getImageName().trim().toLowerCase().endsWith(".jpg"))
								{
									replaceTag+="data:image/jpg;base64,";
								}
								else if(imageDetails.getImageName().trim().toLowerCase().endsWith(".png"))
								{
									replaceTag+="data:image/png;base64,";
								}
								else if(imageDetails.getImageName().trim().toLowerCase().endsWith(".bmp"))
								{
									replaceTag+="data:image/bmp;base64,";
								}
								replaceTag+=" "+base64String+"\"";
								// set PORCESSING STATUS AS SUCCESS
								imageDetails.setProcessingStatus("SUCCESS");
							}
							else
							{
								replaceTag+=" src=\"\"";
								// set PROCESSING STATUS AS FAILURE
								imageDetails.setProcessingStatus("FAILURE");
								imageDetails.setErrorMessage("FAILED TO CONVERT IMAGE TO BASE64 STRING.");
							}
						}
						else
						{
							replaceTag+=" src=\"\"";
							// set PROCESSING STATUS AS FAILURE
							imageDetails.setProcessingStatus("FAILURE");
							imageDetails.setErrorMessage("IMAGE DOES NOT EXIST AT SOURCE LOCATION.");
						}
					}
					else
					{
						replaceTag+=" src=\"\"";
						// set PROCESSING STATUS AS FAILURE
						imageDetails.setProcessingStatus("FAILURE");
						imageDetails.setErrorMessage("IMAGE NAME NOT FOUND FOR THE ASSET ID.");
					}
					replaceTag+=" >";

					//  replace &quot; by " in content
					details.setHtmlContent(details.getHtmlContent().replace("&quot;", "\""));
					// replace ASSET TAG IN HTML CONTENT
					details.setHtmlContent(details.getHtmlContent().replace(imageDetails.getTagContent(), replaceTag));
					imageDetails = null;
					replaceTag = null;
					fileContent = null;
					base64String = null;
					imageFile = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processInlineImagesForKA()", e);
		}
		return details;
	}

	public static DocumentDetails processOtherInlineImagesForKA(DocumentDetails details)
	{
		try
		{
			if(null!=details.getOtherInlineImages() && details.getOtherInlineImages().size()>0)
			{
				File imageFile = null;
				String base64String=null;
				byte[] fileContent=null;
				String replaceTag=null;
				InlineImageDetails imageDetails = null;
				for(int a=0;a<details.getOtherInlineImages().size();a++)
				{
					imageDetails=  (InlineImageDetails)details.getOtherInlineImages().get(a);
					// CHECK ONLY FOR IMAGES WHICH ARE NOT TO BE PASSED AS - AS_IS
					if(null==imageDetails.getProcessingStatus() || "".equals(imageDetails.getProcessingStatus()) 
							&& (null!=imageDetails.getProcessingStatus() && !"AS_IS".equals(imageDetails.getProcessingStatus())))
					{
						// start preparing image tag for replace
						replaceTag="<img ";
						// add width
						if(null!=imageDetails.getWidth() && !"".equals(imageDetails.getWidth()))
						{
							replaceTag+=" width=\""+imageDetails.getWidth()+"\"";
						}
						// add height
						if(null!=imageDetails.getHeight() && !"".equals(imageDetails.getHeight()))
						{
							replaceTag+=" height=\""+imageDetails.getHeight()+"\"";
						}
						if(null!=imageDetails.getImageName() && !"".equals(imageDetails.getImageName()))
						{
							// CHECK FOR THE IMAGE FILE IF EXISTS AT SOURCE LOCATION OR NOT
							imageFile = searchImageFileForOtherImages(imageDetails.getImageName());
							if(null!=imageFile && imageFile.isFile() && imageFile.exists())
							{
								// add Image Name to Image Path
								imageDetails.setImagePath(imageFile.getAbsolutePath());
								
								// CONVERT IMAGE FILE TO BASE 64 STRING
								try
								{
									fileContent = FileUtils.readFileToByteArray(imageFile);
									base64String = Base64.getEncoder().encodeToString(fileContent);
								}
								catch(Exception e)
								{
									Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processOtherInlineImagesForKA()", e);	
								}

								// CHECK FOR BASE64 STRING
								if(null!=base64String && !"".equals(base64String))
								{
									replaceTag+="src=\"";
									if(imageDetails.getImageName().trim().toLowerCase().endsWith(".jpg"))
									{
										replaceTag+="data:image/jpg;base64,";
									}
									else if(imageDetails.getImageName().trim().toLowerCase().endsWith(".png"))
									{
										replaceTag+="data:image/png;base64,";
									}
									else if(imageDetails.getImageName().trim().toLowerCase().endsWith(".bmp"))
									{
										replaceTag+="data:image/bmp;base64,";
									}
									replaceTag+=" "+base64String+"\"";
									// set PORCESSING STATUS AS SUCCESS
									imageDetails.setProcessingStatus("SUCCESS");
								}
								else
								{
									replaceTag+=" src=\"\"";
									// set PROCESSING STATUS AS FAILURE
									imageDetails.setProcessingStatus("FAILURE");
									imageDetails.setErrorMessage("FAILED TO CONVERT IMAGE TO BASE64 STRING.");
								}
							}
							else
							{
								replaceTag+=" src=\"\"";
								// set PROCESSING STATUS AS FAILURE
								imageDetails.setProcessingStatus("FAILURE");
								imageDetails.setErrorMessage("IMAGE DOES NOT EXIST AT SOURCE LOCATION.");
							}
						}
						else
						{
							replaceTag+=" src=\"\"";
							// set PROCESSING STATUS AS FAILURE
							imageDetails.setProcessingStatus("FAILURE");
							imageDetails.setErrorMessage("IMAGE NAME NOT FOUND FROM SRC VALUE.");
						}
						replaceTag+=" >";

						//  replace &quot; by " in content
						details.setHtmlContent(details.getHtmlContent().replace("&quot;", "\""));
						// replace ASSET TAG IN HTML CONTENT
						details.setHtmlContent(details.getHtmlContent().replace(imageDetails.getTagContent(), replaceTag));
					}
					imageDetails = null;
					replaceTag = null;
					fileContent = null;
					base64String = null;
					imageFile = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processOtherInlineImagesForKA()", e);
		}
		return details;
	}

	
	private static File searchImageFile(String imageName)
	{
		File imageFile = null;
		try
		{
			String sourceLocation = ApplicationProperties.getProperty("INLINE.IMAGES.SOURCE.LOCATION");
			String onlyName = imageName.substring(0, imageName.lastIndexOf("."));
			String extension = imageName.substring(imageName.lastIndexOf("."),imageName.length());
			File dir = new File(sourceLocation);
			File[] listFiles = dir.listFiles();
			File childFile = null;
			if(null!=listFiles && listFiles.length>0)
			{
				/*
				 * FIRST CHECK WITH IMAGE NAME IN THUMBS FOLDER
				 * WHERE PARTTERN WILL BE
				 * thumb_IMAGENAME 
				 * 
				 * LOGIC CHANGED
				 * NOW IF IMAGE NAME STARTS WITH THUMB_ then only check in THUMB FOLDER
				 */
				
				if(imageName.toLowerCase().startsWith("thumb_"))
				{
					File[] childList = null;
					for(int a=0;a<listFiles.length;a++)
					{
						childFile= (File)listFiles[a];
						if(childFile.isDirectory())
						{
							childList = childFile.listFiles();
							if(null!=childList && childList.length>0)
							{
								for(int b=0;b<childList.length;b++)
								{
									if(childList[b].isFile() && childList[b].exists())
									{
										if(childList[b].getName().trim().toLowerCase().equals("thumb_"+imageName.trim().toLowerCase()))
										{
											// FILE FOUND - IT'S A THUMB NAIL FILE
											imageFile = childList[b];
											break;
										}
									}
								}
							}
							childList = null;
						}
						childFile = null;
					}
					childList = null;
				}
				else
				{
					/*
					 * CHECK IF FILE IS STILL NOT FOUND IN THUMBNAILS - THEN LOOK FOR FILES IN IMAGES
					 * DIRECTORY USING IMGE NAME AS PATTERN, REPLACE ALL SPACES WITH _ AND CHECK EXTENSION MATCHES OR NOT
					 */
					for(int a=0;a<listFiles.length;a++)
					{
						childFile= (File)listFiles[a];
						if(childFile.isFile() && childFile.exists())
						{
							if(childFile.getName().toLowerCase().trim().contains(onlyName.trim().toLowerCase()) && 
									childFile.getName().toLowerCase().trim().endsWith(extension.trim().toLowerCase()))
							{
								imageFile = childFile;
								break;
							}
						}
						childFile = null;
					}
				}
			}
			listFiles = null;
			onlyName = null;
			extension  =null;
			sourceLocation = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "searchImageFile()", e);
		}
		return imageFile;
	}

	private static File searchImageFileForOtherImages(String imageName)
	{
		File imageFile = null;
		try
		{
			boolean fileFound = false;
			String sourceLocation = ApplicationProperties.getProperty("INLINE.IMAGES.SOURCE.LOCATION");
			File dir = new File(sourceLocation);
			File[] listFiles = dir.listFiles();
			File childFile = null;
			if(null!=listFiles && listFiles.length>0)
			{
				/*
				 * FIRST CHECK WITH IMAGE NAME IN THUMBS FOLDER
				 * WHERE PARTTERN WILL BE
				 * thumb_IMAGENAME 
				 */
				File[] childList = null;
				for(int a=0;a<listFiles.length;a++)
				{
					childFile= (File)listFiles[a];
					if(childFile.isDirectory())
					{
						childList = childFile.listFiles();
						if(null!=childList && childList.length>0)
						{
							for(int b=0;b<childList.length;b++)
							{
								if(childList[b].isFile() && childList[b].exists())
								{
									if(childList[b].getName().trim().toLowerCase().equals(imageName.trim().toLowerCase()))
									{
										// FILE FOUND - IT'S A THUMB NAIL FILE
										imageFile = childList[b];
										fileFound = true;
										break;
									}
								}
							}
						}
						childList = null;
					}
					childFile = null;
				}
				childList = null;

				/*
				 * CHECK IF FILE IS STILL NOT FOUND IN THUMBNAILS - THEN LOOK FOR FILES IN IMAGES
				 */
				if(fileFound==false)
				{
					for(int a=0;a<listFiles.length;a++)
					{
						childFile= (File)listFiles[a];
						if(childFile.isFile() && childFile.exists())
						{
							if(childFile.getName().toLowerCase().trim().equals(imageName.trim().toLowerCase()))
							{
								imageFile = childFile;
								break;
							}
						}
						childFile = null;
					}
				}
			}
			listFiles = null;
			sourceLocation = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "searchImageFileForOtherImages()", e);
		}
		return imageFile;
	}

	
	public static DocumentDetails processAttachmentsForKA(DocumentDetails details)
	{
		try
		{
			if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
			{
				File attachmentFile = null;
				File renamedFile = null;
				AttachmentDetails attachDetails = null;
				String fileName = null;
				String name=null;
				String ext=null;
				for(int a=0;a<details.getAttachmentsList().size();a++)
				{
					attachDetails=  (AttachmentDetails)details.getAttachmentsList().get(a);
					if(null!=attachDetails.getAttachmentType() && attachDetails.getAttachmentType().equals("DOC_ATTACHMENT"))
					{
						if(null!=attachDetails.getAttachmentTitle() && !"".equals(attachDetails.getAttachmentTitle()))
						{
							// CHECK FOR THE ATTACHMENT FILE IF EXISTS AT SOURCE LOCATION OR NOT
							attachmentFile = searchAttachmentFile(attachDetails.getAttachmentTitle());
							if(null!=attachmentFile && attachmentFile.isFile() && attachmentFile.exists())
							{
								/*
								 * CHECK HERE ATTACHMENT NAME IS MORE THAN 100 CHARS
								 * THEN RENAME THE FILE TO 100 CHARS LENGTH AS KA DOES NOT ACCEPT 
								 * ANY FILE NAME MORE THAN 100 CHARS
								 */
								if(attachmentFile.getName().length()>100)
								{
									name = attachmentFile.getName();
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
									// rename File and move to another directory
									boolean flag = true;
									try
									{
										renamedFile = new File(ApplicationProperties.getProperty("tmp.attachments.folder.path")+name);
										if(!renamedFile.exists())
										{
											renamedFile.createNewFile();
										}
										// copy file to tmp directory
										copyFile(attachmentFile, renamedFile);
									}
									catch(Exception e)
									{
										// set flag to false;
										Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processAttachmentsForKA()", e);
									}
									if(flag==true)
									{
										// set Attachment Path
										attachDetails.setAttachmentPath(renamedFile.getAbsolutePath());
										// set Attachment Name
										attachDetails.setAttachmentName(renamedFile.getName());
										// set extension
										attachDetails.setExtension(attachDetails.getAttachmentName().substring(attachDetails.getAttachmentName().lastIndexOf(".")+1, attachDetails.getAttachmentName().length()));
										// set PROCESSING STATUS AS SUCCESS
										attachDetails.setProcessingStatus("SUCCESS");
										// set Attachment Size
										attachDetails.setAttachmentSize(String.valueOf(renamedFile.length()));
									}
									else
									{
										// set Attachment Path
										attachDetails.setAttachmentPath(attachmentFile.getAbsolutePath());
										// set Attachment Name
										attachDetails.setAttachmentName(attachmentFile.getName());
										// set extension
										attachDetails.setExtension(attachDetails.getAttachmentName().substring(attachDetails.getAttachmentName().lastIndexOf(".")+1, attachDetails.getAttachmentName().length()));
										// set Attachment Size
										attachDetails.setAttachmentSize(String.valueOf(attachmentFile.length()));

										// set PROCESSING STATUS AS FAILURE
										attachDetails.setProcessingStatus("FAILURE");
										attachDetails.setErrorMessage("FAILED TO RENAME FILE WITH FILE NAME EQUALS TO 100 CHARS. AND MOVE TO TMP DIRECTORY.");
									}
									name = null;
									renamedFile = null;
								}
								else
								{
									// set Attachment Path
									attachDetails.setAttachmentPath(attachmentFile.getAbsolutePath());
									// set Attachment Name
									attachDetails.setAttachmentName(attachmentFile.getName());
									// set extension
									attachDetails.setExtension(attachDetails.getAttachmentName().substring(attachDetails.getAttachmentName().lastIndexOf(".")+1, attachDetails.getAttachmentName().length()));
									// set PROCESSING STATUS AS SUCCESS
									attachDetails.setProcessingStatus("SUCCESS");
									// set Attachment Size
									attachDetails.setAttachmentSize(String.valueOf(attachmentFile.length()));
								}
							}
							else
							{
								// set PROCESSING STATUS AS FAILURE
								attachDetails.setProcessingStatus("FAILURE");
								attachDetails.setErrorMessage("ATTACHMENT DOES NOT EXIST AT SOURCE LOCATION.");
							}
						}
						else
						{
							// set PROCESSING STATUS AS FAILURE
							attachDetails.setProcessingStatus("FAILURE");
							attachDetails.setErrorMessage("ATTACHMENT NAME NOT FOUND FROM SOURCE DB.");
						}
					}
					else if(null!=attachDetails.getAttachmentType() && attachDetails.getAttachmentType().equals("INLINE_ATTACHMENT"))
					{
						if(null!=attachDetails.getInlineAttachmentPath() && !"".equals(attachDetails.getInlineAttachmentPath()))
						{
							fileName=  null;
							if(attachDetails.getInlineAttachmentPath().lastIndexOf("/")!=-1)
							{
								fileName=  attachDetails.getInlineAttachmentPath().substring(attachDetails.getInlineAttachmentPath().lastIndexOf("/")+1,attachDetails.getInlineAttachmentPath().length());
							}
							
							if(null!=fileName && !"".equals(fileName))
							{
								// CHECK FOR THE ATTACHMENT FILE IF EXISTS AT SOURCE LOCATION OR NOT
								attachmentFile = searchInlineAttachmentFile(fileName);
								if(null!=attachmentFile && attachmentFile.isFile() && attachmentFile.exists())
								{
									/*
									 * CHECK HERE ATTACHMENT NAME IS MORE THAN 100 CHARS
									 * THEN RENAME THE FILE TO 100 CHARS LENGTH AS KA DOES NOT ACCEPT 
									 * ANY FILE NAME MORE THAN 100 CHARS
									 */
									if(attachmentFile.getName().length()>100)
									{
										name = attachmentFile.getName();
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
										// rename File and move to another directory
										boolean flag = true;
										try
										{
											renamedFile = new File(ApplicationProperties.getProperty("tmp.attachments.folder.path")+name);
											if(!renamedFile.exists())
											{
												renamedFile.createNewFile();
											}
											// copy file to tmp directory
											copyFile(attachmentFile, renamedFile);
										}
										catch(Exception e)
										{
											// set flag to false;
											Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processAttachmentsForKA()", e);
										}
										if(flag==true)
										{
											// set Attachment Path
											attachDetails.setAttachmentPath(renamedFile.getAbsolutePath());
											// set Attachment Name
											attachDetails.setAttachmentName(renamedFile.getName());
											// set extension
											attachDetails.setExtension(attachDetails.getAttachmentName().substring(attachDetails.getAttachmentName().lastIndexOf(".")+1, attachDetails.getAttachmentName().length()));
											// set PROCESSING STATUS AS SUCCESS
											attachDetails.setProcessingStatus("SUCCESS");
											// set Attachment Size
											attachDetails.setAttachmentSize(String.valueOf(renamedFile.length()));
										}
										else
										{
											// set Attachment Path
											attachDetails.setAttachmentPath(attachmentFile.getAbsolutePath());
											// set Attachment Name
											attachDetails.setAttachmentName(attachmentFile.getName());
											// set extension
											attachDetails.setExtension(attachDetails.getAttachmentName().substring(attachDetails.getAttachmentName().lastIndexOf(".")+1, attachDetails.getAttachmentName().length()));
											// set Attachment Size
											attachDetails.setAttachmentSize(String.valueOf(attachmentFile.length()));

											// set PROCESSING STATUS AS FAILURE
											attachDetails.setProcessingStatus("FAILURE");
											attachDetails.setErrorMessage("FAILED TO RENAME FILE WITH FILE NAME EQUALS TO 100 CHARS. AND MOVE TO TMP DIRECTORY.");
										}
										name = null;
										renamedFile = null;
									}
									else
									{
										// set Attachment Path
										attachDetails.setAttachmentPath(attachmentFile.getAbsolutePath());
										// set Attachment Name
										attachDetails.setAttachmentName(attachmentFile.getName());
										// set extension
										attachDetails.setExtension(attachDetails.getAttachmentName().substring(attachDetails.getAttachmentName().lastIndexOf(".")+1, attachDetails.getAttachmentName().length()));
										// set PROCESSING STATUS AS SUCCESS
										attachDetails.setProcessingStatus("SUCCESS");
										// set Attachment Size
										attachDetails.setAttachmentSize(String.valueOf(attachmentFile.length()));
									}
								}
								else
								{
									// set PROCESSING STATUS AS FAILURE
									attachDetails.setProcessingStatus("FAILURE");
									attachDetails.setErrorMessage("ATTACHMENT DOES NOT EXIST AT SOURCE LOCATION.");
								}	
							}
							else
							{
								// set PROCESSING STATUS AS FAILURE
								attachDetails.setProcessingStatus("FAILURE");
								attachDetails.setErrorMessage("FILE NAME CANNOT BE IDENTIFIED FROM THE INLINE ATTACHMENT SOURCE PATH.");
							}
							fileName = null;
						}
						else
						{
							// set PROCESSING STATUS AS FAILURE
							attachDetails.setProcessingStatus("FAILURE");
							attachDetails.setErrorMessage("INLINE ATTACHMENT SOURCE PATH IS NULL IN SOURCE DB.");
						}
					}

					attachDetails = null;
					attachmentFile = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processAttachmentsForKA()", e);
		}
		return details;
	}


	private static File searchAttachmentFile(String attachmentName)
	{
		File attachmentFile = null;
		try
		{
			String sourceLocation = ApplicationProperties.getProperty("ATTACHMENTS.SOURCE.LOCATION");
			attachmentName = attachmentName.replace(" ", "_");
			File dir = new File(sourceLocation);
			File[] listFiles = dir.listFiles();
			File childFile = null;
			if(null!=listFiles && listFiles.length>0)
			{
				/*
				 * CHECK IN ALL FOLDERS EXCEPT IMAGES
				 */
				File[] childList = null;
				for(int a=0;a<listFiles.length;a++)
				{
					childFile= (File)listFiles[a];
					if(childFile.isDirectory() && !childFile.getName().trim().toLowerCase().equals("images"))
					{
						childList = childFile.listFiles();
						if(null!=childList && childList.length>0)
						{
							for(int b=0;b<childList.length;b++)
							{
								if(childList[b].isFile() && childList[b].exists())
								{
									if(childList[b].getName().trim().toLowerCase().contains(attachmentName.trim().toLowerCase()))
									{
										// FILE FOUND
										attachmentFile = childList[b];
										break;
									}
								}
							}
						}
						childList = null;
					}
					childFile = null;
				}
				childList = null;
			}
			listFiles = null;
			sourceLocation = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "searchAttachmentFile()", e);
		}
		return attachmentFile;
	}

	private static File searchInlineAttachmentFile(String attachmentName)
	{
		File attachmentFile = null;
		try
		{
			String sourceLocation = ApplicationProperties.getProperty("ATTACHMENTS.SOURCE.LOCATION");
			File dir = new File(sourceLocation);
			File[] listFiles = dir.listFiles();
			File childFile = null;
			if(null!=listFiles && listFiles.length>0)
			{
				/*
				 * CHECK IN ALL FOLDERS EXCEPT IMAGES
				 */
				File[] childList = null;
				for(int a=0;a<listFiles.length;a++)
				{
					childFile= (File)listFiles[a];
					if(childFile.isDirectory() && !childFile.getName().trim().toLowerCase().equals("images"))
					{
						childList = childFile.listFiles();
						if(null!=childList && childList.length>0)
						{
							for(int b=0;b<childList.length;b++)
							{
								if(childList[b].isFile() && childList[b].exists())
								{
									if(childList[b].getName().trim().toLowerCase().equals(attachmentName.trim().toLowerCase()))
									{
										// FILE FOUND
										attachmentFile = childList[b];
										break;
									}
								}
							}
						}
						childList = null;
					}
					childFile = null;
				}
				childList = null;
			}
			listFiles = null;
			sourceLocation = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "searchInlineAttachmentFile()", e);
		}
		return attachmentFile;
	}

	
	public static  String removeBackTotopLink(String htmlContent)
	{
		try
		{
			if(null!=htmlContent && !"".equals(htmlContent))
			{
				Document doc = Jsoup.parse(htmlContent);
				if(null!=doc)
				{
					Element bodyElement = doc.select("body").first();
					if(null!=bodyElement)
					{
						// set elementsList
						Elements elementsList = bodyElement.children();
						if(null!=elementsList && elementsList.size()>0)
						{
							Element element = null;
							for(int a=0;a<elementsList.size();a++)
							{
								element = (Element)elementsList.get(a);
								if(null!=element.text() && element.text().trim().toLowerCase().equals("back to top"))
								{
									element.remove();
								}
								element  =null;
							}
						}
						// set rest of the content
						htmlContent = bodyElement.html();
					}
				}
				doc = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "removeBackTotopLink()", e);
		}
		return htmlContent;
	}

	public static  String removeEmptyStyleAttributes(String htmlContent)
	{
		try
		{
			if(null!=htmlContent && !"".equals(htmlContent))
			{
				Document doc = Jsoup.parse(htmlContent);
				if(null!=doc)
				{
					Element bodyElement= null;
//					Element bodyElement = doc.select("body").first();
//					if(null!=bodyElement)
					{
						// set elementsList
						Elements elementsList = doc.getAllElements();
						if(null!=elementsList && elementsList.size()>0)
						{
							Element element = null;
							String styleAttrValue=null;
							for(int a=0;a<elementsList.size();a++)
							{
								element = (Element)elementsList.get(a);
								styleAttrValue = element.attr("style");
								if(null==styleAttrValue || styleAttrValue.equals(""))
								{
									// remove style attribute
									element.removeAttr("style");
								}
								element  =null;
							}
						}
						// set rest of the content
						bodyElement=doc.select("body").first();
						htmlContent = bodyElement.html();
					}
				}
				doc = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "removeEmptyStyleAttributes()", e);
		}
		return htmlContent;
	}

	
	public static void writeHTMLContent(String htmlContent, String typeArticleId,String channel)
	{
		try
		{
			String path = ApplicationProperties.getProperty("HTMLS.SOURCE.LOCATION")+channel;
			File f = new File(path);
			if(!f.isDirectory())
			{
				f.mkdir();
			}
			f = null;
			// write file
			f = new File(path+"/"+"KM"+typeArticleId+".html");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(htmlContent.getBytes(Charset.forName("UTF-8")));
			fos.flush();
			fos.close();
			fos = null;
			f = null;
		}
		catch(Exception re)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "writeHTMLContent()", re);
		}
	}

	public static DocumentDetails processRelatedlinksForKA(DocumentDetails details)
	{
		DestinationDocumentsDataDAO destDao = new DestinationDocumentsDataDAO();
		try
		{
			if(null!=details.getRelatedManualLinksList() && details.getRelatedManualLinksList().size()>0)
			{
				String relatedContentHTML="";
				RelatedManualLinkDetails linkDetails = null;
				for(int a=0;a<details.getRelatedManualLinksList().size();a++)
				{
					linkDetails=  (RelatedManualLinkDetails)details.getRelatedManualLinksList().get(a);
					// fetch Answer Id for the RelatedLink
					if(null!=linkDetails.getRmTypeArticleId() && !"".equals(linkDetails.getRmTypeArticleId()))
					{
						linkDetails.setKaArticleId(destDao.getLinksKAAnswerId(linkDetails.getRmTypeArticleId()));
					}
					if(null!=linkDetails.getKaArticleId() && !"".equals(linkDetails.getKaArticleId()))
					{
						// PREPARE KA DOCUMENT URL
						if(null==linkDetails.getRmLinkTitle())
						{
							linkDetails.setRmLinkTitle("");
						}
//						relatedContentHTML+="<span data-answerid=\""+linkDetails.getKaArticleId()+"\" data-contents=\""+linkDetails.getRmLinkTitle()+"\" data-linktype=\"1\" title=\""+linkDetails.getRmLinkTitle()+"\">"+linkDetails.getRmLinkTitle()+"</span>";
						relatedContentHTML+="<a href=\"/app/answers/answer_view/a_id/"+linkDetails.getKaArticleId()+"\">"+linkDetails.getRmLinkTitle()+"</a>";
						relatedContentHTML+="<br>";
						// set InnerLink Processing status as SUCCESS
						linkDetails.setProcessingStatus("SUCCESS");
					}
					else
					{
						// PREPARE KA DOCUMENT URL
						if(null==linkDetails.getRmLinkTitle())
						{
							linkDetails.setRmLinkTitle("");
						}
//						relatedContentHTML+="<span data-answerid=\"\" data-contents=\""+linkDetails.getRmLinkTitle()+"\" data-linktype=\"1\" title=\""+linkDetails.getRmLinkTitle()+"\">"+linkDetails.getRmLinkTitle()+"</span>";
						relatedContentHTML+="<a href=\"#\">"+linkDetails.getRmLinkTitle()+"</a>";
						relatedContentHTML+="<br>";
						// set InnerLink Processing status as FAILURE
						linkDetails.setProcessingStatus("FAILURE");
						linkDetails.setErrorMessage("FAILED TO IDENTIFY KA ANSWER ID FOR THE RELATEDLINK TYPE ID.");
					}
					linkDetails = null;
				}
				// if relatedContentHTML is not null - set HTML Content for it
				if(null!=relatedContentHTML && !"".equals(relatedContentHTML))
				{
					details.setRelatedLinkContent(relatedContentHTML);
				}
				relatedContentHTML = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "processRelatedlinksForKA()", e);
		}
		finally
		{
			destDao = null;
		}
		return details;
	}

	private static void copyFile(File srcFile, File destFile) throws IOException {

	    InputStream oInStream = new FileInputStream(srcFile);
	    OutputStream oOutStream = new FileOutputStream(destFile);

	    // Transfer bytes from in to out
	    byte[] oBytes = new byte[1024];
	    int nLength;

	    BufferedInputStream oBuffInputStream = new BufferedInputStream(oInStream);
	    while((nLength = oBuffInputStream.read(oBytes)) > 0) {
	        oOutStream.write(oBytes, 0, nLength);
	    }
	    oInStream.close();
	    oOutStream.close();
	}
	
}
