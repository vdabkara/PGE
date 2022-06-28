package com.pge.dataload.impl;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pge.dataload.utils.ContentSchemaUtils;
import com.pge.dataload.utils.Utilities;
import com.pge.dataload.vo.SchemaFieldDetails;

public class PatternIdentification {

	private static Logger logger = Logger.getLogger(PatternIdentification.class);
	
	public static void main(String[] args) {
		// initialize loggers
		File f = new File(StartDataLoadImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		PropertyConfigurator.configure(f.getParentFile().getAbsolutePath()+"/log.properties");
		f=  null;
		try
		{
			/*
			 * IDENTIFY PATTERN
			 * LOOK FOR THE SUMMARY TAG
			 * IF FOUND - PATTERN_1
			 * ELSE PATTERN_2
			 */
			String htmlContent=  Utilities.getStringFromXML(new File("\\\\jwtcvpxprf02\\profiles\\E77645\\Downloads\\PGE_WD\\htmls\\1008403.html"));
			// set defaultValue = PATTERN_2
			String pattern="PATTERN_2";
			Document doc = Jsoup.parse(htmlContent);
			SchemaFieldDetails schemaDetails = new SchemaFieldDetails();
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
					System.out.println(htmlContent);
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(PatternIdentification.class.getName(), "main()", e);
		}
	}
	
	private static String getNextElementContent(Element ele,String content)
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
				if(!ele.nextElementSibling().nodeName().equals("table"))
				{
					content+=ele.nextElementSibling().outerHtml();
					// call recursive function
					content = getNextElementContent(ele.nextElementSibling(), content);
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
					content+=ele.nextElementSibling().outerHtml();
					// call recursive function
					content = getContentForH1Tags(ele.nextElementSibling(), content);
				}
			}
		}
		return content;
	}

}
