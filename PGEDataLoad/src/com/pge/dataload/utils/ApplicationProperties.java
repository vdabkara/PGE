/**
 * 
 */
package com.pge.dataload.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * @author darora
 * 
 */
public class ApplicationProperties {

	public static String getProperty(String key) {
		String value = null;
		FileInputStream fis = null;
		try {
			Properties mainProperties = new Properties();
			File jarPath = new File(ApplicationProperties.class
					.getProtectionDomain().getCodeSource().getLocation()
					.getPath());

			String propertiesPath = jarPath.getParentFile().getAbsolutePath();
			if (null != propertiesPath && !"".equals(propertiesPath)) {
				propertiesPath = propertiesPath.replace("\\", "/");
			}

			/*
			 * When Running from Info Manager, remove the lib folder from the
			 * propertiesPath
			 */
			// String propertiesPath = System.getProperty("IM_HOME")
			// + "/config/IMADMIN";// path of properties file

			fis = new FileInputStream(propertiesPath
					+ "/application.properties");
			mainProperties.load(fis);
			value = mainProperties.getProperty(key);

		} catch (IOException ioe) {
//			log.error(ioe.getMessage());
		} catch (Exception e) {
//			log.error(e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
//					log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return value;
	}
}
