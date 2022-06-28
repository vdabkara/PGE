package com.pge.dataload.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pge.dataload.vo.DocumentDetails;
import com.pge.dataload.vo.SchemaFieldDetails;

public class ContentSchemaUtils {

	private static Logger logger = Logger.getLogger(ContentSchemaUtils.class);
	
	public static DocumentDetails preparedChannelSchemaDetails(DocumentDetails details)
	{
		if(null!=details.getKaChannelRefKey() && !"".equals(details.getKaChannelRefKey()))
		{
			if(null!=details.getKaChannelRefKey() && details.getKaChannelRefKey().equals("FAQ"))
			{
				details = faqSchemaProcessing(details);
			}
			else if(details.getKaChannelRefKey().equals("JOB_AID"))
			{
				details = jobAidSchemaProcessing(details);
			}
			else if(details.getKaChannelRefKey().equals("PROCEDURE"))
			{
				details = procedureSchemaProcessing(details);
			}
			else if(details.getKaChannelRefKey().equals("ALERT"))
			{
				details = alertSchemaProcessing(details);
			}
			else if(details.getKaChannelRefKey().equals("REFERENCE"))
			{
				details = referenceSchemaProcessing(details);
			}
		}
		return details;
	}
	
	/**
	 * prepare schema details for FAQ Channel
	 * @param details
	 * @return
	 */
	private static DocumentDetails faqSchemaProcessing(DocumentDetails details)
	{
		try
		{
			if(null!=details.getHtmlContent() && !"".equals(details.getHtmlContent()))
			{
				/*
				 * IDENTIFY PATTERN
				 * LOOK FOR THE SUMMARY TAG
				 * IF FOUND - PATTERN_1
				 * ELSE PATTERN_2
				 */
				String htmlContent=  details.getHtmlContent();
				// set defaultValue = PATTERN_2
				String pattern="PATTERN_2";
				Document doc = Jsoup.parse(htmlContent);
				SchemaFieldDetails schemaDetails = new SchemaFieldDetails();
				if(null!=doc)
				{
					/*
					 * look for summary tag with <h1>
					 */
					Elements h1ElementList = doc.select("h1");
					if(null!=h1ElementList && h1ElementList.size()>0)
					{
						Element h1Ele = null;
						for(int a=0;a<h1ElementList.size();a++)
						{
							h1Ele = (Element)h1ElementList.get(a);
							if(null!=h1Ele.text() && h1Ele.text().trim().toLowerCase().equals("Summary".toLowerCase()))
							{
								// SUMMARY TAG FOUND SO PATTERN_1
								pattern = "PATTERN_1";
								break;
							}
							h1Ele =null;
						}
					}
					h1ElementList = null;
					/*
					 * SET FIELDS CONTENT
					 */
					Element bodyElement = doc.select("body").first();
					if(null!=bodyElement)
					{
						// set elementsList
						Elements elementsList = bodyElement.children();
						if(null!=elementsList && elementsList.size()>0)
						{
							Element element = null;
							if(pattern.equals("PATTERN_1"))
							{
								/*
								 * IDENTIFY SUMMARY
								 * AND THEN Q & A LIST
								 */
								String summaryContent="";
								element = null;
								for(int a=0;a<elementsList.size();a++)
								{
									element = (Element)elementsList.get(a);
									if(null!=element.text() && element.text().trim().toLowerCase().contains("Summary".toLowerCase()))
									{
										/*
										 * START READING DATA OF ALL NEXT ELEMENTS UNTIL TABLE IS BEING RENDERED
										 */
										summaryContent = getNextElementContentForFAQ(element, summaryContent);
										System.out.println("--- summaryContent :: >"+ summaryContent);
										break;
									}
									element = null;
								}
								schemaDetails.setSummary(summaryContent);
								summaryContent = null;
								
								/*
								 * NOW IDENTIFY Q & A LIST
								 */
								Elements trList = doc.select("tr");
								if(null!=trList && trList.size()>0)
								{
									SchemaFieldDetails qaDetails = null;
									Element qEle = null;
									Element aEle = null;
									int loopCount = trList.size() / 2;
									int count=0;
									for(int a=0;a<loopCount;a++)
									{
										try
										{
											qEle = trList.get(count);
											// increment count
											count++;
											aEle = trList.get(count);
											// increment count
											count++;
											qaDetails = new SchemaFieldDetails();
											if(null!=qEle)
											{
												qaDetails.setQuestion(qEle.child(0).html());
											}
											if(null!=aEle)
											{
												qaDetails.setAnswer(aEle.child(0).html());
											}
//											logger.info("faqSchemaProcessing :: Question :: >"+qaDetails.getQuestion());
//											logger.info("faqSchemaProcessing :: Answer :: >"+qaDetails.getAnswer());
											// add to QA List
											if(null==schemaDetails.getQuestionAnswerList() || schemaDetails.getQuestionAnswerList().size()<=0)
											{
												schemaDetails.setQuestionAnswerList(new ArrayList<SchemaFieldDetails>());
											}
											schemaDetails.getQuestionAnswerList().add(qaDetails);
											qaDetails=  null;
											qEle = null;
											aEle=  null;
										}
										catch(Exception e)
										{
											Utilities.printStackTraceToLogs(ContentSchemaUtils.class.getName(), "faqSchemaProcessing()", e);
										}
									}
								}
								trList = null;
							}
							else
							{
								/*
								 *  PATTERN_2 MAPPING
								 *  SET COMPLETE HTML CONTENT IN ONE SOURCE CONTENT
								 */
								schemaDetails.setOneSourceContent(htmlContent);
							}
						}
						elementsList = null;
					}
					bodyElement=  null;
				}
				// set schemaDetails
				details.setSchemaDetails(schemaDetails);
				// set contentPattern
				details.setKaContentPattern(pattern);
				schemaDetails = null;
				pattern=  null;
				htmlContent = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentSchemaUtils.class.getName(), "faqSchemaProcessing()", e);
		}
		return details;
	}
	
	/**
	 * PREPARE SCHEMA DETAILS FOR JOB AID CHANNEL
	 * @param details
	 * @return
	 */
	private static DocumentDetails jobAidSchemaProcessing(DocumentDetails details)
	{
		try
		{
			if(null!=details.getHtmlContent() && !"".equals(details.getHtmlContent()))
			{
				/*
				 * SET COMPLETE CONTENT IN ONE SOURCE
				 */
				// set defaultValue = PATTERN_1
				String pattern="PATTERN_1";
				SchemaFieldDetails schemaDetails = new SchemaFieldDetails();
				schemaDetails.setSummary(details.getHtmlContent());
				// set schemaDetails
				details.setSchemaDetails(schemaDetails);
				// set contentPattern
				details.setKaContentPattern(pattern);
				schemaDetails = null;
				pattern=  null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentSchemaUtils.class.getName(), "jobAidSchemaProcessing()", e);
		}
		return details;
	}
	
	private static DocumentDetails alertSchemaProcessing(DocumentDetails details)
	{
		try
		{
			if(null!=details.getHtmlContent() && !"".equals(details.getHtmlContent()))
			{
				/*
				 * IDENTIFY PATTERN
				 * 	CHECK FOR H1 TAGS - IF FOUND
				 * 		THEN EITHER PATTERN 1 OR PATTERN 2
				 * IF NO H1 TAGS
				 * 		THEN PATTERN 3
				 */
				String htmlContent=  details.getHtmlContent();
				// set defaultValue = NULL
				String pattern="";
				Document doc = Jsoup.parse(htmlContent);
				SchemaFieldDetails schemaDetails = new SchemaFieldDetails();
				if(null!=doc)
				{
					/*
					 * look for summary tag with <h1>
					 */
					Elements h1ElementList = doc.select("h1");
					if(null!=h1ElementList && h1ElementList.size()>0)
					{
						Element h1Ele = null;
						boolean backgroundTagFound=false;
						for(int a=0;a<h1ElementList.size();a++)
						{
							h1Ele = (Element)h1ElementList.get(a);
							if(null!=h1Ele.text() && h1Ele.text().trim().toLowerCase().equals("Background".toLowerCase()))
							{
								// BACKGROUND TAG FOUND SO PATTERN_2
								pattern = "PATTERN_2";
								// set backgroundTagFound to true;
								backgroundTagFound = true;
								break;
							}
							h1Ele =null;
						}
						
						if(backgroundTagFound==false)
						{
							// set pattern to PATTERN_1
							pattern = "PATTERN_1";
						}
					}
					else
					{
						// NO H1 TAGS FOUND
						pattern = "PATTERN_3";
					}
					h1ElementList = null;
					/*
					 * SET FIELDS CONTENT
					 */
					if(pattern.equals("PATTERN_1"))
					{
						// SET Complete COntent in KnowledgeAlertDescription
						schemaDetails.setKnowledgeAlertDescription(details.getHtmlContent());
					}
					else if(pattern.equals("PATTERN_3"))
					{
						// SET Complete COntent in OneSourceContent
						schemaDetails.setOneSourceContent(details.getHtmlContent());
					}
					else if(pattern.equals("PATTERN_2"))
					{
						/*
						 * get all H1 Elements Content
						 */
						doc=  removeH1TagsWithNBSP(doc);
						Element bodyElement = doc.select("body").first();
						if(null!=doc)
						{
							if(null!=bodyElement)
							{
								// set elementsList
								Elements elementsList = bodyElement.children();
								if(null!=elementsList && elementsList.size()>0)
								{
									Element element = null;
									/*
									 * get all H1 TAGS & THERI CONTENT
									 */
									
									String content="";
									element = null;
									List<SchemaFieldDetails> list = new ArrayList<>();
									SchemaFieldDetails tempDetails = null;
									int index=0;
									for(int a=0;a<elementsList.size();a++)
									{
										element = (Element)elementsList.get(a);
										if(element.nodeName().toLowerCase().equals("h1".toLowerCase()))
										{
											content="";
											/*
											 * START READING ALL CONTENT TILL NEXT H1 TAG
											 */
											content = getContentForH1Tags(element, content);
											if(null!=element.text() && !"".equals(element.text().trim()) && !" ".equals(element.text().trim()))
//											if(null!=element.text() && !"".equals(element.text()) && null!=content && !"".equals(content))
											{
												// add to h1Tags with Content List
												tempDetails = new SchemaFieldDetails();
												tempDetails.setContentHeaderHTML(element.html());
												tempDetails.setContentHeader(element.text());
												tempDetails.setDescription(content);
												list.add(tempDetails);
												tempDetails = null;
												// increment index by 1
												index++;
											}
											else
											{
												if(null!=content && !"".equals(content))
												{
													/*
													 * THIS IS THE CASE WHERE HEADER WAS NULL, BUT CONTENT WAS FOUND
													 * IN THIS CASE
													 * CHECK IF LIST IS NULL E.G. FIRST HEADING THEN ADD TO LIST
													 * 	TO AVOID CONTENT DISAPPEARING
													 * IF LIST IS NOT NULL
													 * 	IDENTIFY THE PREVIOUS INDEX AND ADD CONTENT IT PREVIOUS INDEX DESCRIPTION
													 */
													if(null!=list && list.size()>0)
													{
														tempDetails = null;
														for(int g=0;g<list.size();g++)
														{
															tempDetails = (SchemaFieldDetails)list.get(g);
															if(g==(index-1))
															{
																if(null!=tempDetails)
																{
																	if(null!=tempDetails.getDescription() && !"".equals(tempDetails.getDescription()))
																	{
																		tempDetails.setDescription(tempDetails.getDescription()+content);
																	}
																	else
																	{
																		tempDetails.setDescription(content);
																	}
																}
																break;
															}
															tempDetails = null;
														}
													}
													else
													{
														// add to h1Tags with Content List
														tempDetails = new SchemaFieldDetails();
														tempDetails.setContentHeaderHTML(element.html());
														tempDetails.setContentHeader(element.text().trim());
														tempDetails.setDescription(content);
														list.add(tempDetails);
														tempDetails = null;
														// increment index
														index++;
													}
												}
											}
											content = null;
										}
										element = null;
									}
									
									/*
									 * START IDENTIFYING FLOOWING NODES
									 * BACKGROUND
									 * QUESTIONS
									 * DETAILS
									 */
									if(null!=list && list.size()>0)
									{
										tempDetails = null;
										SchemaFieldDetails dtData = null;
										for(int a=0;a<list.size();a++)
										{
											tempDetails = (SchemaFieldDetails)list.get(a);
											if(tempDetails.getContentHeader().trim().toLowerCase().equals("background"))
											{
												schemaDetails.setBackground(tempDetails.getDescription());
											}
											else if(tempDetails.getContentHeader().trim().toLowerCase().equals("Questions?".toLowerCase()))
											{
												schemaDetails.setQuestion(tempDetails.getDescription());
											}
											else
											{
												// all others will go in Details Node
												dtData = new SchemaFieldDetails();
												dtData.setContentHeader(tempDetails.getContentHeaderHTML());
												dtData.setDescription(tempDetails.getDescription());
												if(null==schemaDetails.getDetailsNodeList() || schemaDetails.getDetailsNodeList().size()<=0)
												{
													schemaDetails.setDetailsNodeList(new ArrayList<SchemaFieldDetails>());
												}
												schemaDetails.getDetailsNodeList().add(dtData);
												dtData=  null;
											}
											tempDetails = null;
										}
									}
								}
								elementsList = null;
							}
							bodyElement=  null;
						}
					}
				}
				// set schemaDetails
				details.setSchemaDetails(schemaDetails);
				// set contentPattern
				details.setKaContentPattern(pattern);
				schemaDetails = null;
				pattern=  null;
				htmlContent = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentSchemaUtils.class.getName(), "alertSchemaProcessing()", e);
		}
		return details;
	}
	
	private static DocumentDetails procedureSchemaProcessing(DocumentDetails details)
	{
		try
		{
			if(null!=details.getHtmlContent() && !"".equals(details.getHtmlContent()))
			{
				/*
				 * IDENTIFY PATTERN
				 * 	CHECK FOR H1 TAGS - IF FOUND
				 * 		THEN PATTERN 1 
				 * IF NO H1 TAGS
				 * 		THEN PATTERN 2
				 */
				String htmlContent=  details.getHtmlContent();
				// set defaultValue = "PATTERN_2"
				String pattern="PATTERN_2";
				Document doc = Jsoup.parse(htmlContent);
				SchemaFieldDetails schemaDetails = new SchemaFieldDetails();
				if(null!=doc)
				{
					/*
					 * look for summary tag with <h1>
					 */
					Elements h1ElementList = doc.select("h1");
					if(null!=h1ElementList && h1ElementList.size()>0)
					{
						// set pattern to PATTERN_1
						pattern = "PATTERN_1";
					}
					else
					{
						// NO H1 TAGS FOUND
						pattern = "PATTERN_2";
					}
					h1ElementList = null;
					/*
					 * SET FIELDS CONTENT
					 */
					if(pattern.equals("PATTERN_2"))
					{
						// SET Complete COntent in OneSourceContent
						schemaDetails.setOneSourceContent(details.getHtmlContent());
					}
					if(pattern.equals("PATTERN_1"))
					{
						// remove H1 Tags with blank nodes
						doc = removeH1TagsWithNBSP(doc);
						if(null!=doc)
						{
							Element bodyElement = doc.select("body").first();
							if(null!=bodyElement)
							{
								// set elementsList
								Elements elementsList = bodyElement.children();
								System.out.println("------- child List :: >"+ elementsList.size());
								if(null!=elementsList && elementsList.size()>0)
								{
									/*
									 * CHECK FOR THE FIRST CHILD IN BODY
									 * 	DOES IT START WITH SUMMARY OR NOT
									 */
									Element element = null;
									String additonalSummaryContent="";
									if(!elementsList.get(0).nodeName().toLowerCase().equals("h1"))
									{
										/*
										 * GET ALL THE CONTENT BEFORE FIRST H1 TAG
										 */
										additonalSummaryContent = elementsList.get(0).outerHtml();
										additonalSummaryContent = getContentForH1Tags(elementsList.get(0), additonalSummaryContent);
									}
									// Content starts with H1
									/*
									 * IDENTIFY SUMMARY, HIGH LEVEL PROCESS & DETAILED PROCESS
									 * AND THEN DETAILS NODE LIST
									 */
									String content="";
									element = null;
									List<SchemaFieldDetails> list = new ArrayList<>();
									SchemaFieldDetails tempDetails = null;
									int index=0;
									for(int a=0;a<elementsList.size();a++)
									{
										element = (Element)elementsList.get(a);
										if(element.nodeName().toLowerCase().equals("h1".toLowerCase()))
										{
											System.out.println("----- h1 ele :: >"+ element.text());
											content="";
											/*
											 * START READING ALL CONTENT TILL NEXT H1 TAG
											 */
											content = getContentForH1Tags(element, content);
//											if(null!=element.text() && !"".equals(element.text()) && 
//													(!element.text().trim().equals("")) && null!=content && !"".equals(content))
											if(null!=element.text() && !"".equals(element.text().trim()) && !" ".equals(element.text().trim()))
											{
												// add to h1Tags with Content List
												tempDetails = new SchemaFieldDetails();
												tempDetails.setContentHeaderHTML(element.html());
												tempDetails.setContentHeader(element.text());
												tempDetails.setDescription(content);
												list.add(tempDetails);
												tempDetails = null;
												// increment index by 1
												index++;
											}
											else
											{
												if(null!=content && !"".equals(content))
												{
													/*
													 * THIS IS THE CASE WHERE HEADER WAS NULL, BUT CONTENT WAS FOUND
													 * IN THIS CASE
													 * CHECK IF LIST IS NULL E.G. FIRST HEADING THEN ADD TO LIST
													 * 	TO AVOID CONTENT DISAPPEARING
													 * IF LIST IS NOT NULL
													 * 	IDENTIFY THE PREVIOUS INDEX AND ADD CONTENT IT PREVIOUS INDEX DESCRIPTION
													 */
													if(null!=list && list.size()>0)
													{
														tempDetails = null;
														for(int g=0;g<list.size();g++)
														{
															tempDetails = (SchemaFieldDetails)list.get(g);
															if(g==(index-1))
															{
																if(null!=tempDetails)
																{
																	if(null!=tempDetails.getDescription() && !"".equals(tempDetails.getDescription()))
																	{
																		tempDetails.setDescription(tempDetails.getDescription()+content);
																	}
																	else
																	{
																		tempDetails.setDescription(content);
																	}
																}
																break;
															}
															tempDetails = null;
														}
													}
													else
													{
														// add to h1Tags with Content List
														tempDetails = new SchemaFieldDetails();
														tempDetails.setContentHeaderHTML(element.html());
														tempDetails.setContentHeader(element.text().trim());
														tempDetails.setDescription(content);
														list.add(tempDetails);
														tempDetails = null;
														// increment index
														index++;
													}
												}
											}
											content = null;
										}
										element = null;
									}
									/*
									 * START IDENTIFYING FOLLOWING NODES
									 * SUMMARY
									 * HIGH LEVEL PROCESS
									 * DETAILED PROCESS
									 * DETAILS NODE
									 */
									if(null!=list && list.size()>0)
									{
										tempDetails = null;
										for(int a=0;a<list.size();a++)
										{
											tempDetails = (SchemaFieldDetails)list.get(a);
											System.out.println("=== header :: >"+ tempDetails.getContentHeader()+".");
											if(tempDetails.getContentHeader().trim().toLowerCase().equals("summary") || 
													tempDetails.getContentHeader().trim().toLowerCase().equals("Summary ".toLowerCase()))
											{
												schemaDetails.setSummary(tempDetails.getDescription());
												// REMOVE THIS NODE FROM LIST
												list.remove(a);
												a--;
											}
										}
									}
									if(null!=list && list.size()>0)
									{
										tempDetails = null;
										for(int a=0;a<list.size();a++)
										{
											tempDetails = (SchemaFieldDetails)list.get(a);
											if(tempDetails.getContentHeader().trim().toLowerCase().equals("High Level Process".toLowerCase()) || 
													tempDetails.getContentHeader().trim().toLowerCase().equals("High Level Process ".toLowerCase()))
											{
												schemaDetails.setHighLevelProcess(tempDetails.getDescription());
												// REMOVE THIS NODE FROM LIST
												list.remove(a);
												a--;
											}
											/*
											 *  remove detailed process and add it in Content Header List - 22 May 2022
											 */
//											else if(tempDetails.getContentHeader().trim().toLowerCase().equals("Detailed Process".toLowerCase()))
//											{
//												schemaDetails.setDetailedProcess(tempDetails.getDescription());
//												// REMOVE THIS NODE FROM LIST
//												list.remove(a);
//												a--;
//											}
											tempDetails = null;
										}
									}
									
									// IF LIST IS STILL NOT NULL - E.G. OTHER NODES ADD THEM TO DETAILED PROCESS NODE
									if(null!=list && list.size()>0)
									{
										tempDetails = null;
										SchemaFieldDetails dtData = null;
										for(int a=0;a<list.size();a++)
										{
											tempDetails = (SchemaFieldDetails)list.get(a);
											// all others will go in Details Node
											dtData = new SchemaFieldDetails();
											dtData.setContentHeader(tempDetails.getContentHeaderHTML());
											dtData.setDescription(tempDetails.getDescription());
											if(null==schemaDetails.getDetailsNodeList() || schemaDetails.getDetailsNodeList().size()<=0)
											{
												schemaDetails.setDetailsNodeList(new ArrayList<SchemaFieldDetails>());
											}
											schemaDetails.getDetailsNodeList().add(dtData);
											dtData=  null;
										}
									}
									list = null;
									
									/*
									 * CHECK FOR ADDITONAL CONTENT
									 */
									if(null!=additonalSummaryContent && !"".equals(additonalSummaryContent))
									{
										if(null!=schemaDetails.getSummary() && !"".equals(schemaDetails.getSummary()))
										{
											schemaDetails.setSummary(additonalSummaryContent+ schemaDetails.getSummary());;
										}
										else
										{
											schemaDetails.setSummary(additonalSummaryContent);
										}
									}
									additonalSummaryContent  =null;
								}
								elementsList = null;
							}
							bodyElement=  null;
						}
					}
				}
				// set schemaDetails
				details.setSchemaDetails(schemaDetails);
				// set contentPattern
				details.setKaContentPattern(pattern);
				schemaDetails = null;
				pattern=  null;
				htmlContent = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentSchemaUtils.class.getName(), "procedureSchemaProcessing()", e);
		}
		return details;
	}

	private static DocumentDetails referenceSchemaProcessing(DocumentDetails details)
	{
		try
		{
			if(null!=details.getHtmlContent() && !"".equals(details.getHtmlContent()))
			{
				/*
				 * IDENTIFY PATTERN
				 * 	CHECK FOR H1 TAG WITH SUMMARY
				 * 		IF FOUND - THEN PATTERN 1
				 * ELSE PATTERN 2
				 */
				String htmlContent=  details.getHtmlContent();
				// set defaultValue = PATTERN_2
				String pattern="PATTERN_2";
				Document doc = Jsoup.parse(htmlContent);
				SchemaFieldDetails schemaDetails = new SchemaFieldDetails();
				if(null!=doc)
				{
					/*
					 * look for summary tag with <h1>
					 */
					Elements h1ElementList = doc.select("h1");
					if(null!=h1ElementList && h1ElementList.size()>0)
					{
						// set pattern to PATTERN_1
						pattern = "PATTERN_1";
					}
					h1ElementList = null;
					/*
					 * SET FIELDS CONTENT
					 */
					if(pattern.equals("PATTERN_2"))
					{
						// SET Complete COntent in OneSourceContent
						schemaDetails.setOneSourceContent(details.getHtmlContent());
					}
					else if(pattern.equals("PATTERN_1"))
					{
						// remove H1 Tags with blank nodes
						doc = removeH1TagsWithNBSP(doc);
						if(null!=doc)
						{
							Element bodyElement = doc.select("body").first();
							if(null!=bodyElement)
							{
								// set elementsList
								Elements elementsList = bodyElement.children();
								if(null!=elementsList && elementsList.size()>0)
								{
									/*
									 * CHECK FOR THE FIRST CHILD IN BODY
									 * 	DOES IT START WITH SUMMARY OR NOT
									 */
									Element element = null;
									String additonalSummaryContent="";
									if(!elementsList.get(0).nodeName().toLowerCase().equals("h1"))
									{
										/*
										 * GET ALL THE CONTENT BEFORE FIRST H1 TAG
										 */
										additonalSummaryContent = elementsList.get(0).outerHtml();
										additonalSummaryContent = getContentForH1Tags(elementsList.get(0), additonalSummaryContent);
									}
									// Content starts with H1
									/*
									 * IDENTIFY SUMMARY
									 * DETAILS HEADING
									 * AND THEN DETAILS NODE LIST (ALL OTHER HI TAGS
									 */
									String content="";
									element = null;
									List<SchemaFieldDetails> list = new ArrayList<>();
									SchemaFieldDetails tempDetails = null;
									int index = 0;
									for(int a=0;a<elementsList.size();a++)
									{
										element = (Element)elementsList.get(a);
										if(element.nodeName().toLowerCase().equals("h1".toLowerCase()))
										{
											content="";
											/*
											 * START READING ALL CONTENT TILL NEXT H1 TAG
											 */
											content = getContentForH1Tags(element, content);
											if(null!=element.text() && !"".equals(element.text().trim()) && !" ".equals(element.text().trim()))
											{
												// add to h1Tags with Content List
												tempDetails = new SchemaFieldDetails();
												tempDetails.setContentHeaderHTML(element.html());
												tempDetails.setContentHeader(element.text().trim());
												tempDetails.setDescription(content);
												
												list.add(tempDetails);
												tempDetails = null;
												// increment index
												index++;
											}
											else
											{
												if(null!=content && !"".equals(content))
												{
													/*
													 * THIS IS THE CASE WHERE HEADER WAS NULL, BUT CONTENT WAS FOUND
													 * IN THIS CASE
													 * CHECK IF LIST IS NULL E.G. FIRST HEADING THEN ADD TO LIST
													 * 	TO AVOID CONTENT DISAPPEARING
													 * IF LIST IS NOT NULL
													 * 	IDENTIFY THE PREVIOUS INDEX AND ADD CONTENT IT PREVIOUS INDEX DESCRIPTION
													 */
													if(null!=list && list.size()>0)
													{
														tempDetails = null;
														for(int g=0;g<list.size();g++)
														{
															tempDetails = (SchemaFieldDetails)list.get(g);
															if(g==(index-1))
															{
																if(null!=tempDetails)
																{
																	if(null!=tempDetails.getDescription() && !"".equals(tempDetails.getDescription()))
																	{
																		tempDetails.setDescription(tempDetails.getDescription()+content);
																	}
																	else
																	{
																		tempDetails.setDescription(content);
																	}
																}
																break;
															}
															tempDetails = null;
														}
													}
													else
													{
														// add to h1Tags with Content List
														tempDetails = new SchemaFieldDetails();
														tempDetails.setContentHeaderHTML(element.html());
														tempDetails.setContentHeader(element.text().trim());
														tempDetails.setDescription(content);
														list.add(tempDetails);
														tempDetails = null;
														// increment index
														index++;
													}
												}
											}
											
											content = null;
										}
										element = null;
									}
									
									/*
									 * START IDENTIFYING FLOOWING NODES
									 * SUMMARY
									 * DETAILS NODE
									 */
									if(null!=list && list.size()>0)
									{
										tempDetails = null;
										SchemaFieldDetails dtData = null;
										for(int a=0;a<list.size();a++)
										{
											tempDetails = (SchemaFieldDetails)list.get(a);
											if(tempDetails.getContentHeader().trim().toLowerCase().equals("summary"))
											{
												schemaDetails.setSummary(tempDetails.getDescription());
											}
											else if(tempDetails.getContentHeader().trim().toLowerCase().equals("details"))
											{
												schemaDetails.setDetailsTextArea(tempDetails.getDescription());
											}
											else
											{
												// all others will go in Details Node
												dtData = new SchemaFieldDetails();
												dtData.setContentHeader(tempDetails.getContentHeaderHTML());
												dtData.setDescription(tempDetails.getDescription());
												if(null==schemaDetails.getDetailsNodeList() || schemaDetails.getDetailsNodeList().size()<=0)
												{
													schemaDetails.setDetailsNodeList(new ArrayList<SchemaFieldDetails>());
												}
												schemaDetails.getDetailsNodeList().add(dtData);
												dtData=  null;
											}
											tempDetails = null;
										}
									}
									list = null;
									
									/*
									 * CHECK FOR ADDITONAL CONTENT
									 */
									if(null!=additonalSummaryContent && !"".equals(additonalSummaryContent))
									{
										if(null!=schemaDetails.getSummary() && !"".equals(schemaDetails.getSummary()))
										{
											schemaDetails.setSummary(additonalSummaryContent+ schemaDetails.getSummary());;
										}
										else
										{
											schemaDetails.setSummary(additonalSummaryContent);
										}
									}
									additonalSummaryContent  =null;
								}
								elementsList = null;
							}
							bodyElement=  null;
						}
					}
				}
				// set schemaDetails
				details.setSchemaDetails(schemaDetails);
				// set contentPattern
				details.setKaContentPattern(pattern);
				schemaDetails = null;
				pattern=  null;
				htmlContent = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentSchemaUtils.class.getName(), "referenceSchemaProcessing()", e);
		}
		return details;
	}

	
	private static String getNextElementContentForFAQ(Element ele,String content)
	{
		Element nextEle=null;
		if(null!=ele)
		{
			if(null!=ele.nextElementSibling())
			{
				nextEle = ele.nextElementSibling();
				/*
				 * CHECK IF ELE.nextElementSibling IS A <P> TAG
				 * 	IF YES - THEN CHECK IFIT CONTAINS STYLE TAG WITH TEXT-ALIGN:RIGHT
				 * 		IF YES - REPLACE IT WITH TEXT-ALIGN:LEFT
				 */
				if(!nextEle.nodeName().equals("table"))
				{
					/*
					 * CHECK IF HEADING IS Frequently Asked Questions
					 * DO NOT ADD THE HEADING THEN
					 */
					String textData= nextEle.text();
					// remove this condition - let the blank nodes go as it is
//					if(null!=textData && !"".equalsIgnoreCase(textData))
					{
						if(!textData.trim().toLowerCase().equals("Frequently Asked Questions".toLowerCase()))
						{
							content+=nextEle.outerHtml();
						}
					}
					textData = null;
					// call recursive function
					content = getNextElementContentForFAQ(nextEle, content);
					nextEle = null;
				}
			}
		}
		return content;
	}
	
	private static String getContentForH1Tags(Element ele,String content)
	{
		if(null!=ele)
		{
			if(null!=ele.nextElementSibling())
			{
				/*
				 * CHECK IF ELE.nextElementSibling IS A <P> TAG
				 * 	IF YES - THEN CHECK IFIT CONTAINS STYLE TAG WITH TEXT-ALIGN:RIGHT
				 * 		IF YES - REPLACE IT WITH TEXT-ALIGN:LEFT
				 */
				if(!ele.nextElementSibling().nodeName().toLowerCase().equals("h1".toLowerCase()))
				{
					// remove this condition - let the blank nodes go as it is
//					if(null!=ele.nextElementSibling().text() && !"".equals(ele.nextElementSibling().text()))
					{
						content+=ele.nextElementSibling().outerHtml();
					}
					// call recursive function
					content = getContentForH1Tags(ele.nextElementSibling(), content);
				}
			}
		}
		return content;
	}
	
	private static Document removeH1TagsWithNBSP(Document doc)
	{
		try
		{
			if(null!=doc)
			{
				Elements h1Ele = doc.select("h1");
				if(null!=h1Ele && h1Ele.size()>0)
				{
					for(int a=0;a<h1Ele.size();a++)
					{
						System.out.println("ht :: >"+ h1Ele.get(a).html());
						if(h1Ele.get(a).html().trim().toLowerCase().equals("&nbsp;") || 
								h1Ele.get(a).html().trim().toLowerCase().equals("&nbsp;&nbsp;") || 
								h1Ele.get(a).html().trim().toLowerCase().equals("&nbsp;<br />"))
						{
							System.out.println("------------ removing");
							// remove from doc
							h1Ele.get(a).remove();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ContentSchemaUtils.class.getName(), "removeH1TagsWithNBSP()", e);
		}
		return doc;
	}
}
