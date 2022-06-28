package com.pge.dataload.utils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class DBConnector {

	private static Logger logger = Logger.getLogger(DBConnector.class);
	
	public static Connection getSorceConnection()
	{
		Connection conn = null;
		try
		{
			/*
			 * Load all the required parameters from properties file DataBase
			 * Driver ClassName Database URL Database UserName Database Password
			 */
			String databaseURL = ApplicationProperties.getProperty("jdbc.source.databaseurl");
			String databaseUserName= ApplicationProperties.getProperty("jdbc.source.databaseUserName");
			String databasePassword = ApplicationProperties.getProperty("jdbc.source.databasePassword");
			/*
			 * Load Database Driver Class
			 */
			Driver myDriver = new com.microsoft.sqlserver.jdbc.SQLServerDriver();
			DriverManager.registerDriver(myDriver);
			
			// getConnection Object
			if(null!=databaseUserName && !"".equals(databaseUserName) && null!=databasePassword && !"".equals(databasePassword))
			{
				logger.info("getSorceConnection :: When DB Credentials are Provided.");
				conn = DriverManager.getConnection(databaseURL,databaseUserName, databasePassword);
			}
			else
			{
				logger.info("getSorceConnection :: Only with DB URL.");
				conn = DriverManager.getConnection(databaseURL);
			}
			// set databaseURL to null
			databaseURL = null;
			databaseUserName = null;
			databasePassword = null;
			myDriver = null;
		}
		catch (Exception e) {
			logger.error("getSorceConnection() :: Exception :: >" + e.getMessage());
			// set connection to null
			conn = null;
			Utilities.printStackTraceToLogs(DBConnector.class.getName(), "getSorceConnection()", e);
		}
		return conn;
	}

	public static Connection getDestinationConnection()
	{
		Connection conn = null;
		try
		{
			/*
			 * Load all the required parameters from properties file DataBase
			 * Driver ClassName Database URL Database UserName Database Password
			 */
			String databaseURL = ApplicationProperties.getProperty("jdbc.dest.databaseurl");
			String databaseUserName= ApplicationProperties.getProperty("jdbc.dest.databaseUserName");
			String databasePassword = ApplicationProperties.getProperty("jdbc.dest.databasePassword");
			/*
			 * Load Database Driver Class
			 */
			Driver myDriver = new com.microsoft.sqlserver.jdbc.SQLServerDriver();
			DriverManager.registerDriver(myDriver);
			// getConnection Object
			if(null!=databaseUserName && !"".equals(databaseUserName) && null!=databasePassword && !"".equals(databasePassword))
			{
				logger.info("getDestinationConnection :: When DB Credentials are Provided.");
				conn = DriverManager.getConnection(databaseURL,databaseUserName, databasePassword);
			}
			else
			{
				logger.info("getDestinationConnection :: Only with DB URL.");
				conn = DriverManager.getConnection(databaseURL);
			}
			// set databaseURL to null
			databaseURL = null;
			databaseUserName = null;
			databasePassword = null;
			myDriver = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("getDestinationConnection() :: Exception :: >" + e.getMessage());
			// set connection to null
			conn = null;
			Utilities.printStackTraceToLogs(DBConnector.class.getName(), "getDestinationConnection()", e);
		}
		return conn;
	}

}
