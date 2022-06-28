package com.pge.dataload.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class Utilities {

	static Logger logger = Logger.getLogger(Utilities.class);

	public static void printStackTraceToLogs(String className,
			String methodName, Exception e) {
		try {
			Writer writer = new StringWriter();
			PrintWriter print = new PrintWriter(writer);
			e.printStackTrace(print);

			logger.info(className + "::" + methodName + ":: Error :: > "
					+ e.getMessage());
			logger.info(className + "::" + methodName + ":: Error :: > "
					+ writer.toString());
			// String errorCode = e.getMessage();
			// String errorMessage = writer.toString();

			print = null;
			writer = null;
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public static String readInputStramToString(InputStream is) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			if (null != is) {
				String line;
				br = new BufferedReader(new InputStreamReader(is));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public static String fromatDateForEmail(Date date) {
		String convertedDate = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		convertedDate = sdf.format(date);
		return convertedDate;
	}

	public static String convertStringForDisplay(String name) {
		try {
			if (null != name && !"".equals(name)) {
				name = name.trim();
				String newName = "";
				/*
				 * here prepare the name in format - General Information
				 */
				String[] tokens = name.split(" ");
				if (null != tokens && tokens.length > 0) {
					for (int i = 0; i < tokens.length; i++) {
						String key = tokens[i];
						String newKey = "";
						if (null != key && !"".equals(key)) {
							String firstAlphabet = key.substring(0, 1);
							String restAlphabet = key
									.substring(1, key.length());
							// now Update key
							if (null != firstAlphabet
									&& !"".equals(firstAlphabet)) {
								newKey = firstAlphabet.toUpperCase();
							}
							if (null != restAlphabet
									&& !"".equals(restAlphabet)) {
								newKey = newKey + restAlphabet.toLowerCase();
							}
							firstAlphabet = null;
							restAlphabet = null;
						}

						if (null == newKey || "".equals(newKey)) {
							newKey = key;
						}
						// add newKey to newName
						if (null != newKey && !"".equals(newKey)) {
							if (null != newName && !"".equals(newName)) {
								newName = newName + " " + newKey;
							} else {
								newName = newKey;
							}
						}
						newKey = null;
						key = null;
					}
				}

				if (null != newName && !"".equals(newName)) {
					name = newName;
				}
				newName = null;
			}
		} catch (Exception e) {
			printStackTraceToLogs(Utilities.class.getName(),
					"convertStringForDisplay()", e);
		}
		return name;
	}

	public static String convertStringForCode(String code) {
		try {
			if (null != code && !"".equals(code)) {
				code = code.trim();
				String newCode = "";
				/*
				 * here prepare the name in format - General Information
				 */
				String[] tokens = code.split("-");
				if (null != tokens && tokens.length > 0) {
					String key = tokens[0];
					String keyAfter = tokens[1];
					String newKey = "";
					newKey = key.toLowerCase() + "-" + keyAfter.toUpperCase();

					// add newKey to newName
					if (null != newKey && !"".equals(newKey)) {
						newCode = newKey;
					}
					newKey = null;
					key = null;
					keyAfter = null;
				}

				if (null != newCode && !"".equals(newCode)) {
					code = newCode;
				}
				newCode = null;
			}
		} catch (Exception e) {
			printStackTraceToLogs(Utilities.class.getName(),
					"convertStringForCode()", e);
		}
		return code;
	}

	public static String replaceRefKeys(String refKey) {
		refKey = refKey.replace("-", "_");
		refKey = refKey.replace("*", "_");
		refKey = refKey.replace("#", "_");
		return refKey;
	}

	public static String getStringFromXML(File f) throws IOException {
		StringBuffer xmlData = new StringBuffer();
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					xmlData.append(line);
					xmlData.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			logger.error("getStringFromXML :: Cannot Read the Input FileL :: "
					+ f.getName());
			throw ex;
		}
		String articleXML = xmlData.toString();
		return articleXML;
	}

	public static Timestamp readLastProcessedTime() throws IOException {
		Timestamp lastProcessedTime = null;
		try {
			String templatePath = ApplicationProperties.getProperty("LAST.PROCESSED.TIMESTAMP.FILE.PATH");
			File file = new File(templatePath);
			if (file.isFile() && file.exists()) {
				String fileText = getStringFromXML(file);
				if (null != fileText && !"".equals(fileText)) {
					fileText = fileText.trim();
					logger.info("readLastProcessedTime :: Last Processed Time Read is :: > "
							+ fileText);
					/*
					 * CONVERT THIS TEXT TO TIME STAMP
					 */
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
					Date processedDate = sdf.parse(fileText);
					if (null != processedDate) {
						lastProcessedTime = new Timestamp(
								processedDate.getTime());
					}
					processedDate = null;
					sdf = null;
					fileText = null;
				} else {
					logger.info("readLastProcessedTime :: No Last Processed Time is Defined. Fetch all the Modified Data.");
				}
			} else {
				logger.info("readLastProcessedTime :: No Last Processed Time is Defined. Fetch all the Modified Data.");
			}

			file = null;
			templatePath = null;
		} catch (Exception e) {
			Utilities.printStackTraceToLogs(Utilities.class.getName(),
					"readLastProcessedTime()", e);
		}
		return lastProcessedTime;
	}

	public static void writeLastProcessedTime(Timestamp lastProcessedTime)
			throws IOException {
		try {
			if (null != lastProcessedTime) {
				String fileText = convertTimeToPattern(lastProcessedTime);

				logger.info("writeLastProcessedTime :: Last Processed Time to be Set is :: > "
						+ fileText);

				String templatePath = ApplicationProperties.getProperty("LAST.PROCESSED.TIMESTAMP.FILE.PATH");
				if (null != templatePath && !"".equals(templatePath)) {
					templatePath = templatePath.replace("/", "\\");
					logger.info("writeLastProcessedTime :: Updating Last Processed File at path :: > "
							+ templatePath);
					File outPutFile = new File(templatePath);
					outPutFile.createNewFile();

					FileOutputStream fos = new FileOutputStream(outPutFile);
					fos.write(fileText.getBytes());
					fos.close();
					fos.flush();
					fos = null;
				} else {
					logger.info("writeLastProcessedTime :: Processing File Path is null. Cannot update Last Modified Time.");
				}
				templatePath = null;
				fileText = null;
			}
		} catch (Exception e) {
			Utilities.printStackTraceToLogs(Utilities.class.getName(),
					"writeLastProcessedTime()", e);
		}
	}
	
	public static String convertTimeToPattern(Timestamp timestamp)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
		String convertedTime = sdf.format(timestamp);
		sdf = null;
		return convertedTime;
	}
	
	/**
	 * Reads all you can from an input stream.
	 * 
	 * @param inputStream
	 * @return
	 */
	public static String toString(InputStream inputStream)
	{
		try
		{
			if(null!=inputStream)
			{
//				IOUtils.copy(inputStream, writer);
//				String response = writer.toString();
				 int bufferSize = 1024;
				 char[] buffer = new char[bufferSize];
				 StringBuilder out = new StringBuilder();
				 Reader in = new InputStreamReader(inputStream);
				 for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
				     out.append(buffer, 0, numRead);
				 }
				 return out.toString();
//				return response;
			}
		}
		catch (IOException e)
		{
			printStackTraceToLogs(Utilities.class.getName(), "toString()", e);
		}
		return null;
	}

	
}
