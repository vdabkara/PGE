package com.pge.dataload.osvc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.pge.dataload.utils.ApplicationProperties;
import com.pge.dataload.utils.Utilities;



public class OSVCandKAWebServiceCaller
{
	private Logger logger = Logger.getLogger(OSVCandKAWebServiceCaller.class);

	private String			authenticationToken				= null;
	private String			userToken						= null;

	private static boolean	USE_ANONYMOUS_AUTHENTICATION	= false;



	/**
	 * Calls the KA Webservice with the specified subURL to access different web service functions.
	 * If it fails, returns null.
	 * 
	 * https://docs.oracle.com/cloud/latest/servicecs_gs/CXSKA/
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject callKAWebservice(String subURL,String method) throws JSONException, FileNotFoundException
	{
		// System.out.println("Calling KA webservice with subURL " + subURL);
		JSONObject contentObj = new JSONObject();
		String url = ApplicationProperties.getProperty("apiURL") + subURL;
		logger.info("callKAWebservice :: Complete URL :: >"+ url);
		// System.out.println("complete URL = " + url);

		JSONObject kmAuthTokenObject = new JSONObject();

		String authenticationToken = getAuthenticationToken();
		logger.info("callKAWebservice :: authenticationToken :: > "+ authenticationToken);
		String userToken = getUserToken(authenticationToken);
		if (USE_ANONYMOUS_AUTHENTICATION)
		{
			userToken = null;
		}

		kmAuthTokenObject.put("siteName", ApplicationProperties.getProperty("sitename"));
		kmAuthTokenObject.put("integrationUserToken", authenticationToken);
		kmAuthTokenObject.put("locale", ApplicationProperties.getProperty("kaLocale"));
		kmAuthTokenObject.put("userToken", userToken);
		try
		{

			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			if(method.equals("POST"))
			{
				connection.setDoOutput(true); // Triggers POST.
				connection.setRequestMethod("POST");
			}
			else
			{
				connection.setRequestMethod("GET");
			}
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("content-type", "application/json");
			connection.setRequestProperty("kmauthtoken", kmAuthTokenObject.toString());

			int responseCode = connection.getResponseCode();

			 logger.info("callKAWebservice :: Response Code :: >"+ responseCode);
			if (responseCode == HttpStatus.SC_CREATED || responseCode == HttpStatus.SC_OK || responseCode== HttpStatus.SC_NO_CONTENT)
			{
				// handle the response
				InputStream inputStream = connection.getInputStream();
				String response = null;
				if(null!=inputStream)
				{
					response = Utilities.toString(inputStream);
					inputStream.close();
				}
				inputStream = null;
				// String response = StringUtility.toString(inputStream);
				if(!method.equals("GET"))
				{
					logger.info("callKAWebservice :: response :: >"+ response);
				}
				
				contentObj.put("CALL_STATUS", "SUCCESS");
				contentObj.put("ERROR_DATA", new JSONObject());
				if(null!=response && !"".equals(response))
				{
					contentObj.put("FINAL_DATA", new JSONObject(response));
				}
				else
				{
					contentObj.put("FINAL_DATA", new JSONObject());
				}
				contentObj.put("ERROR_MESSAGE", "");
			}
			else
			{
				InputStream errorStream = connection.getErrorStream();
				String error=null;
				if(null!=errorStream)
				{
					error = Utilities.toString(errorStream);
					errorStream.close();
				}
				errorStream = null;
				logger.info("callKAWebservice :: error :: >"+ error);
				contentObj.put("CALL_STATUS", "FAILURE");
				if(null!=error && !"".equals(error))
				{
					contentObj.put("ERROR_DATA", new JSONObject(error));
				}
				else
				{
					contentObj.put("ERROR_DATA", new JSONObject());
				}
				contentObj.put("FINAL_DATA", new JSONObject());
				contentObj.put("ERROR_MESSAGE", "");
				error = null;
			}
			authenticationToken = null;
			userToken=null;
			kmAuthTokenObject =null;
			connection = null;
			url=null;
		}
		catch (Exception e)
		{
			Utilities.printStackTraceToLogs(OSVCandKAWebServiceCaller.class.getName(), "callKAWebservice()", e);
			contentObj.put("CALL_STATUS", "FAILURE");
			contentObj.put("ERROR_DATA", new JSONObject());
			contentObj.put("FINAL_DATA", new JSONObject());
			contentObj.put("ERROR_MESSAGE", "Failed to Invoke KA WebService for URL :: >"+ url+". With Exception :: >"+ e.getMessage());
		}
		return contentObj;
	}

	/**
	 * Gets the authentication token from KA.
	 * 
	 * @return
	 * @throws JSONException
	 */
	private String getAuthenticationToken() throws JSONException
	{
		if (this.authenticationToken != null)
			return this.authenticationToken;
		logger.info("Getting authentication token");

		JSONObject kmAuthTokenObject = new JSONObject();

		kmAuthTokenObject.put("siteName", ApplicationProperties.getProperty("sitename"));
		kmAuthTokenObject.put("localeId", ApplicationProperties.getProperty("kaLocale"));

		JSONObject payloadJSON = new JSONObject();

		payloadJSON.put("login", ApplicationProperties.getProperty("kaApiUsername"));
		payloadJSON.put("password", ApplicationProperties.getProperty("kaApiPassword"));
		payloadJSON.put("siteName", ApplicationProperties.getProperty("sitename"));

		try
		{
			String charset = "UTF-8";

			String url = ApplicationProperties.getProperty("authenticationURL");

			// System.out.println("url = " + url);
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);

			connection.setRequestProperty("accept", "application/json");

			// Response response = client.target(kaConfigurationManager.getAuthenticationURL())
			// .request().header("accept", "application/json")
			// .header("content-type", "application/json")
			// .header("kmauthtoken", kmAuthTokenObject.toString())
			// .post(Entity.json(payloadJSON.toString()));

			// write the payload
			OutputStream output = connection.getOutputStream();
			output.write(payloadJSON.toString().getBytes(charset));

			int responseCode = connection.getResponseCode();
			// System.out.println("response code : " + responseCode);

			if (responseCode != 200)
			{
				System.out.println("response code : " + responseCode);
				logger.info("getAuthenticationToken :: response code : " + responseCode);
				InputStream errorStream = connection.getErrorStream();
				String error = Utilities.toString(errorStream);
				logger.info("getAuthenticationToken :: error :: >"+ error);
				error=null;
				errorStream.close();
				errorStream = null;
			}
			else
			{
				// read the response
				InputStream inputStream = connection.getInputStream();
				StringBuilder sb = new StringBuilder();
				while (inputStream.available() > 0)
					sb.append((char) inputStream.read());

				String response = sb.toString();
				JSONObject responseJSON = new JSONObject(response);
				String authenticationToken = responseJSON.getString("authenticationToken");
				responseJSON = null;
				sb = null;
				inputStream.close();
				inputStream = null;
				this.authenticationToken = authenticationToken;
				authenticationToken = null;
			}


			output.close();
			output=  null;
			connection = null;
			url = null;
			kmAuthTokenObject =null;
			charset=null;
			payloadJSON = null;

			return authenticationToken;
		}
		catch (MalformedURLException e)
		{
			Utilities.printStackTraceToLogs(OSVCandKAWebServiceCaller.class.getName(), "getAuthenticationToken()", e);
		}
		catch (IOException e)
		{
			Utilities.printStackTraceToLogs(OSVCandKAWebServiceCaller.class.getName(), "getAuthenticationToken()", e);
		}

		return null;
	}

	/**
	 * Gets the user token from KA given the authentication token.
	 * 
	 * @param authenticationToken
	 * @return
	 * @throws JSONException
	 */
	private String getUserToken(String authenticationToken) throws JSONException
	{
		if (this.userToken != null)
			return this.userToken;
		logger.info("Getting user token");
		String payload =
				String.format("userName=%s&password=%s&siteName=%s&userExternalType=ACCOUNT",
						ApplicationProperties.getProperty("accountUsername"), ApplicationProperties.getProperty("accountPassword"),
						ApplicationProperties.getProperty("sitename"));
		JSONObject kmAuthTokenObject = new JSONObject();

		kmAuthTokenObject.put("siteName", ApplicationProperties.getProperty("sitename"));
		kmAuthTokenObject.put("localeId", ApplicationProperties.getProperty("kaLocale"));
		kmAuthTokenObject.put("integrationUserToken", authenticationToken);

		try
		{
			String charset = "UTF-8";
			HttpURLConnection connection = (HttpURLConnection) new URL(ApplicationProperties.getProperty("authorizationURL")).openConnection();
			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("kmauthtoken", kmAuthTokenObject.toString());

			// Response response = client.target(kaConfigurationManager.getAuthorizationURL())
			// .request().header("accept", "application/json")
			// .header("kmauthtoken", kmAuthTokenObject.toString()).post(Entity.json(payload));

			OutputStream output = connection.getOutputStream();
			output.write(payload.toString().getBytes(charset));

			int responseCode = connection.getResponseCode();
			// System.out.println("response code : " + responseCode);

			if (responseCode != 200)
			{
				InputStream errorStream = connection.getErrorStream();
				String error = Utilities.toString(errorStream);
				logger.info("getUserToken :: error :: >"+ error);
				error=null;
				errorStream.close();
				errorStream = null;
			}
			else
			{
				// read the response
				InputStream inputStream = connection.getInputStream();

				StringBuilder sb = new StringBuilder();
				while (inputStream.available() > 0)
					sb.append((char) inputStream.read());

				String response = sb.toString();

				JSONObject responseJSON = new JSONObject(response);
				String authObjStr = responseJSON.getString("authenticationToken");
				JSONObject authObj = new JSONObject(authObjStr);
				String userToken = authObj.getString("userToken");
				logger.info("getUserToken :: User Token :: >" + userToken);
				this.userToken = userToken;

				userToken = null;
				authObj = null;
				authObjStr=null;
				response = null;
				inputStream.close();
				inputStream = null;
				responseJSON = null;
			}
			output.close();
			output= null;
			connection = null;
			charset = null;
			kmAuthTokenObject=null;
			payload =null;
			return userToken;
		}
		catch (MalformedURLException e)
		{
			Utilities.printStackTraceToLogs(OSVCandKAWebServiceCaller.class.getName(),"getUserToken()" , e);
		}
		catch (IOException e)
		{
			Utilities.printStackTraceToLogs(OSVCandKAWebServiceCaller.class.getName(),"getUserToken()" , e);
		}
		return null;
	}

	public JSONObject createOperation(String payLoad, List<File> uploadFiles, String subURL) throws JSONException
	{
		payLoad = payLoad.replace("\\/", "/");
		payLoad = payLoad.replace("\\n", "");
		logger.info("payLoad :: "+ payLoad);
		// System.out.println("Calling KA webservice with subURL " + subURL);
		JSONObject contentObj = new JSONObject();
		String url = ApplicationProperties.getProperty("apiURL") + subURL;
		logger.info("createOperation :: Complete URL :: >"+ url);
		// System.out.println("complete URL = " + url);
		JSONObject kmAuthTokenObject = new JSONObject();

		String authenticationToken = getAuthenticationToken();
		logger.info("createOperation :: authenticationToken :: > "+ authenticationToken);
		String userToken = getUserToken(authenticationToken);
		kmAuthTokenObject.put("siteName", ApplicationProperties.getProperty("sitename"));
		kmAuthTokenObject.put("integrationUserToken", authenticationToken);
		kmAuthTokenObject.put("locale", ApplicationProperties.getProperty("kaLocale"));
		kmAuthTokenObject.put("userToken", userToken);
		try
		{
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpRequest  = new HttpPost(url);;
			httpRequest.addHeader("kmauthtoken", kmAuthTokenObject.toString());
			MultipartEntityBuilder multiPartEntityBuilder = null;
			HttpEntity reqEntity = null;
			if (null!=uploadFiles && uploadFiles.size() > 0) 
			{
				logger.info("createOperation :: create content call execution WITH file attachments.");
				httpRequest.addHeader("Accept", "application/json");
				httpRequest.addHeader("Connection", "keep-alive");
				httpRequest.addHeader("Accept-Charset", Charset.forName("UTF-8").toString());
				multiPartEntityBuilder = MultipartEntityBuilder.create();
				for (File uploadFile : uploadFiles) 
				{
					multiPartEntityBuilder.setCharset(Charset.forName("UTF-8"));
					StringBody sBody = new StringBody(payLoad, ContentType.APPLICATION_JSON);
					multiPartEntityBuilder.addPart("contentBO", sBody);
					logger.info("createOperation :: Content.createContentWithFiles Uploading file " + uploadFile.getAbsolutePath());
					FileBody fileBody = new FileBody(uploadFile);
					multiPartEntityBuilder.addPart("filesToUpload", fileBody);
					fileBody = null;
					uploadFile = null;
				}
				reqEntity = multiPartEntityBuilder.build();
				logger.info("createOperation :: Content.createContentWithFiles entity with files = " + reqEntity.toString());
				httpRequest.setEntity(reqEntity);
			} 
			else 
			{
				logger.info("createOperation :: create content call execution WITHOUT file attachments.");
				httpRequest.addHeader("Accept", "application/json");
				httpRequest.addHeader("Connection", "keep-alive");
				httpRequest.addHeader("Content-Type", "application/json");
				httpRequest.addHeader("Accept-Charset", Charset.forName("UTF-8").toString());
				httpRequest.setEntity(new StringEntity(payLoad,Charset.forName("UTF-8")));
			}
			
			logger.info("createOperation :: Content.createContentWithFiles entity with http_request = " + httpRequest.toString());

			HttpResponse http_response = httpClient.execute(httpRequest);
			int statusCode = http_response.getStatusLine().getStatusCode();
			logger.info("createOperation :: create content service response code " + statusCode);
			HttpEntity entity = http_response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			if(statusCode==HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT || statusCode==HttpStatus.SC_CREATED)
			{
				logger.info("createOperation :: Create Content Operation executed Successfully in KA. ");
				contentObj.put("CALL_STATUS", "SUCCESS");
				contentObj.put("ERROR_DATA", new JSONObject());
				contentObj.put("FINAL_DATA", new JSONObject(responseString));
				contentObj.put("ERROR_MESSAGE", "");
			}
			else
			{
				logger.info("createOperation :: Failed to perform Create Content Operation in KA. Error :: > " + responseString);
				contentObj.put("CALL_STATUS", "FAILURE");
				contentObj.put("ERROR_DATA", new JSONObject(responseString));
				contentObj.put("FINAL_DATA", new JSONObject());
				contentObj.put("ERROR_MESSAGE", "");
			}
			responseString = null;
			entity = null;
			http_response = null;
			httpRequest = null;
			reqEntity = null;
			multiPartEntityBuilder = null;
			httpClient= null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(OSVCandKAWebServiceCaller.class.getName(), "createOperation()", e);
			contentObj.put("CALL_STATUS", "FAILURE");
			contentObj.put("ERROR_DATA", new JSONObject());
			contentObj.put("FINAL_DATA", new JSONObject());
			contentObj.put("ERROR_MESSAGE", "Failed to Invoke KA WebService for URL :: >"+ url+". With Exception :: >"+ e.getMessage());
		}
		return contentObj;
	}

	public JSONObject updateOperation(String payLoad, List<File> uploadFiles, String subURL) throws JSONException
	{
		payLoad = payLoad.replace("\\/", "/");
		payLoad = payLoad.replace("\\n", "");
//		logger.info("payLoad :: "+ payLoad);
		// System.out.println("Calling KA webservice with subURL " + subURL);
		JSONObject contentObj = new JSONObject();
		String url = ApplicationProperties.getProperty("apiURL") + subURL;
		logger.info("updateOperation :: Complete URL :: >"+ url);
		// System.out.println("complete URL = " + url);

		JSONObject kmAuthTokenObject = new JSONObject();

		String authenticationToken = getAuthenticationToken();
		logger.info("updateOperation :: authenticationToken :: > "+ authenticationToken);
		String userToken = getUserToken(authenticationToken);
		kmAuthTokenObject.put("siteName", ApplicationProperties.getProperty("sitename"));
		kmAuthTokenObject.put("integrationUserToken", authenticationToken);
		kmAuthTokenObject.put("locale", ApplicationProperties.getProperty("kaLocale"));
		kmAuthTokenObject.put("userToken", userToken);
		try
		{
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPut httpPut  = new HttpPut(url);
			httpPut.addHeader("kmauthtoken", kmAuthTokenObject.toString());
			MultipartEntityBuilder multiPartEntityBuilder = null;
			HttpEntity reqEntity = null;
			if (null!=uploadFiles && uploadFiles.size() > 0) 
			{
				logger.info("updateOperation :: update content call execution WITH file attachments.");
				httpPut.addHeader("Accept", "application/json");
				httpPut.addHeader("Connection", "keep-alive");
				httpPut.addHeader("Accept-Charset", Charset.forName("UTF-8").toString());
				multiPartEntityBuilder = MultipartEntityBuilder.create();
				for (File uploadFile : uploadFiles) 
				{
					multiPartEntityBuilder.setCharset(Charset.forName("UTF-8"));
					StringBody sBody = new StringBody(payLoad, ContentType.APPLICATION_JSON);
					multiPartEntityBuilder.addPart("contentBO", sBody);
					logger.info("updateOperation :: Content.updateContentWithFiles Uploading file " + uploadFile.getAbsolutePath());
					FileBody fileBody = new FileBody(uploadFile);
					multiPartEntityBuilder.addPart("filesToUpload", fileBody);
					fileBody = null;
					uploadFile = null;
				}
				reqEntity = multiPartEntityBuilder.build();
				logger.info("updateOperation :: Content.updateContentWithFiles entity with files = " + reqEntity.toString());
				httpPut.setEntity(reqEntity);
			} 
			else 
			{
				logger.info("updateOperation :: update content call execution WITHOUT file attachments.");
				httpPut.addHeader("Accept", "application/json");
				httpPut.addHeader("Connection", "keep-alive");
				httpPut.addHeader("Content-Type", "application/json");
				httpPut.addHeader("Accept-Charset", Charset.forName("UTF-8").toString());
				httpPut.setEntity(new StringEntity(payLoad, Charset.forName("UTF-8")));
			}
			
			logger.info("updateOperation :: Content.updateContentWithFiles entity with httpPut = " + httpPut.toString());

			HttpResponse http_response = httpClient.execute(httpPut);
			int statusCode = http_response.getStatusLine().getStatusCode();
			logger.info("updateOperation :: update content service response code " + statusCode);
			HttpEntity entity = http_response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			if(statusCode==HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT || statusCode==HttpStatus.SC_CREATED)
			{
				logger.info("updateOperation :: Update Content Operation executed Successfully in KA. ");
				contentObj.put("CALL_STATUS", "SUCCESS");
				contentObj.put("ERROR_DATA", new JSONObject());
				contentObj.put("FINAL_DATA", new JSONObject(responseString));
				contentObj.put("ERROR_MESSAGE", "");
			}
			else
			{
				logger.info("updateOperation :: Failed to perform Update Content Operation in KA. Error :: > " + responseString);
				contentObj.put("CALL_STATUS", "FAILURE");
				contentObj.put("ERROR_DATA", new JSONObject(responseString));
				contentObj.put("FINAL_DATA", new JSONObject());
				contentObj.put("ERROR_MESSAGE", "");
			}
			responseString = null;
			entity = null;
			http_response = null;
			httpPut = null;
			reqEntity = null;
			multiPartEntityBuilder = null;
			httpClient= null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(OSVCandKAWebServiceCaller.class.getName(), "updateOperation()", e);
			contentObj.put("CALL_STATUS", "FAILURE");
			contentObj.put("ERROR_DATA", new JSONObject());
			contentObj.put("FINAL_DATA", new JSONObject());
			contentObj.put("ERROR_MESSAGE", "Failed to Invoke KA WebService for URL :: >"+ url+". With Exception :: >"+ e.getMessage());
		}
		return contentObj;
	}
}