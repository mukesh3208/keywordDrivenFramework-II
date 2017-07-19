/**
 * Last Changes Done on Jan 23, 2015 3:45:57 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: Implemented logger, added support for rtb_win and rtb_bp trackers for hudson requests
 */

package vlib;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jxl.Sheet;
import jxl.Workbook;
import jxl.biff.CellFinder;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.configuration.*;
import org.apache.commons.io.FileUtils;
import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;




public class GenericMethodsLib 
{

	static Logger logger = Logger.getLogger(GenericMethodsLib.class.getName());

	public static PropertiesConfiguration propertyConfigFile;
	public static String configFlag = "desktop";
	public static String isVast2vdo = "onlineplayertype";
	public static boolean isDeviceConnected = false;

	public static String hudsonFlag = "nonHudson";
	public static int expectedaiTracker_Hudson = 0;
	public static int expectedrtbbpTracker_Hudson = 0;

	public static String adFormat = "";


	/** Used in webservice code, migrated to bq
	 * 
	 * @param configFlag
	 */
	public GenericMethodsLib(String configFlag)
	{
		GenericMethodsLib.configFlag = configFlag;
		logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : initializing constructor MobileTestClass_Methods: "+ " configFlag: "+configFlag);
	}

	/** Used in MobileAdServingTests, migrated to bq
	 * @param adFormat
	 */
	public GenericMethodsLib(Object adFormat)
	{
		GenericMethodsLib.adFormat = (String) adFormat;
		logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : initializing constructor MobileTestClass_Methods: "+ " adFormat: "+adFormat);
	}

	/** Used in online serving, not migrated to bq yet
	 * 
	 * @param configFlag
	 * @param isVast2vdo
	 */
	public GenericMethodsLib(String configFlag, String isVast2vdo)
	{
		GenericMethodsLib.configFlag = configFlag;
		GenericMethodsLib.isVast2vdo = isVast2vdo;
		logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : initializing constructor MobileTestClass_Methods: "+ " configFlag: "+configFlag + ", isVast2vdo: "+isVast2vdo);
	}

	/** Used in chocolate, not yet migrated to bq
	 * 
	 * @param hudsonFlag
	 * @param expectedaiTracker_Hudson
	 * @param expectedrtbbpTracker_Hudson
	 */
	public GenericMethodsLib(String hudsonFlag, int expectedaiTracker_Hudson, int expectedrtbbpTracker_Hudson)
	{
		GenericMethodsLib.hudsonFlag = hudsonFlag;
		GenericMethodsLib.expectedaiTracker_Hudson = expectedaiTracker_Hudson;
		GenericMethodsLib.expectedrtbbpTracker_Hudson = expectedrtbbpTracker_Hudson;
		logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : initializing constructor MobileTestClass_Methods: "+ "hudsonFlag: "+hudsonFlag + ", expectedaiTracker_Hudson: "+expectedaiTracker_Hudson + ", expectedrtbbpTracker_Hudson: "+expectedrtbbpTracker_Hudson);
	}

	/** Used in MobileAdServingTests, migrated to bq
	 * 
	 * @param isDeviceConnected
	 */
	public GenericMethodsLib(boolean isDeviceConnected)
	{
		GenericMethodsLib.isDeviceConnected = isDeviceConnected;
		logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : initializing constructor MobileTestClass_Methods: "+ " isDeviceConnected: "+isDeviceConnected);

	}


	/***
	 * This method initialize the webdriver based on supplied browser type. New Way implemented for Chrome Driver:
	 * Now we'll start the chrome server and then wait until server is started and then create a remote driver.
	 * @param browser
	 * @param capabilities
	 * @return
	 */
	public static WebDriver WebDriverSetUp (String browser, String[] capabilities) 
	{

		WebDriver driver = null;
		try
		{
			logger.info(browser+" is being setup on " +System.getProperty("os.name"));

			if(browser.equalsIgnoreCase("FireFox"))
			{
				driver = new FirefoxDriver();
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Firefox is being setup");
			}
			else if (browser.equalsIgnoreCase("Chrome")) 
			{
				String chromeDriver;
				if(System.getProperty("os.name").matches("^Windows.*"))
				{
					chromeDriver = TestSuiteClass.AUTOMATION_HOME.concat("/tpt/chromedriver.exe");
				}else
				{
					//ExecuteCommands.ExecuteMacCommand_ReturnsExitStatus("killall chromedriver");
					chromeDriver = TestSuiteClass.AUTOMATION_HOME.concat("/tpt/chromedriver");
				}

				/** create chrome driver service */
				ChromeDriverService service = retryChromeDriverService(chromeDriver);				

				if(service != null && service.isRunning())
				{
					DesiredCapabilities cap = DesiredCapabilities.chrome();

					try{
						driver = new RemoteWebDriver(service.getUrl(), cap);
					}catch (SessionNotCreatedException e) 
					{
						/** if session is not created successfully then re-try to create it. Calling recursion */
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome driver session not setup, retrying ... ");
						
						driver = WebDriverSetUp(browser, capabilities);
					}
					catch (WebDriverException e) 
					{
						/** if session is not created successfully then re-try to create it. Calling recursion */
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome driver session not setup coz of webdriver exception, retrying ... ");
						
						driver = WebDriverSetUp(browser, capabilities);
					}
				}
				else
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome driver service seems not started while setting up driver ... ");
				}

				/** browsing google.com to check if driver is launched successfully */
				try{driver.get("http://www.google.com");}catch(NoSuchWindowException n)
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome browser was closed coz of unknown reason, retrying ... ");
					
					driver = WebDriverSetUp(browser, capabilities);
				}
			}
			else 
			{	
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : No Support For: "+browser +" Browser. ");
			}			

			int driverImplicitDelay = Integer.parseInt(propertyConfigFile.getProperty("driverImplicitDelay").toString());
			driver.manage().window().maximize();
			driver.manage().deleteAllCookies();

			/** setting up implicit driver delay */
			driver.manage().timeouts().implicitlyWait(driverImplicitDelay, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while setting up browser: " + browser, e);
		} 

		return driver;
	}


	/** This method will attempt to start chrome driver service, earler we were using recursion for retry that may result in
	 * infinite loops, now limiting max attempts to 10.
	 * 
	 * @param chromeDriver
	 * @return
	 */
	public static ChromeDriverService retryChromeDriverService(String chromeDriver) 
	{
		ChromeDriverService service = null;

		int i = 0;
		while(i <= 10)
		{
			if(service != null)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome driver service is started yet, attempt: "+i);
				break;
			}
			else
			{
				service = getChromeDriverService(chromeDriver);
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome driver service is not started yet, attempt: "+i);
			}
		}

		/** wait for chrome driver to start */ 
		if(service != null)
		{
			waitForChromeDriverToStart(service);
		}

		return service;
	}


	/** Get chromedriver service instance.
	 * 
	 * @param chromeDriver
	 * @return
	 */
	public static ChromeDriverService getChromeDriverService(String chromeDriver)
	{
		ChromeDriverService service = null;
		try
		{
			service = new ChromeDriverService.Builder()
					.usingDriverExecutable(new File(chromeDriver))
					.usingAnyFreePort()
					.build();
			service.start();

			Thread.sleep(1000);

		}catch(Exception io){
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while starting the chrome driver service: "+ io);
		}

		return service;
	}


	/** This method waits for chrome driver to start, earlier we were putting infinite loop for wait, now limiting 10 attempts.
	 * 
	 * @param service
	 */
	public static void waitForChromeDriverToStart(ChromeDriverService service)
	{
		int i = 0;

		/** wait until chrome driver server is started -- maximum 10 attempts */
		while(i <= 10)
		{
			String output = httpClientWrap.sendGetRequest((service.getUrl().toString()));
			if(output.isEmpty())
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome driver is not started yet, attempt: "+i);
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}
			else
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chrome driver is started, exiting loop at attempt: "+i);
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
				break;
			}
		}
	}


	//********** Initializing Configuration File: *********************************************//
	// AUTOMATION_HOME is defined in the RUN CONFIGURATION settings of eclipse.
	public static void InitializeConfiguration()  
	{	
		//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Automation_Home: " + TestSuiteClass.AUTOMATION_HOME);
		try
		{
			propertyConfigFile = new PropertiesConfiguration();

			String varAutomationHome = "";

			if(configFlag.equalsIgnoreCase("webservice"))
			{
				if (System.getProperty("os.name").toLowerCase().matches("^mac.*"))
				{
					varAutomationHome = "/Users/user/Documents/ProjectAdServingWebservice/VdopiaAdserving";
				}
				else
				{
					varAutomationHome = "C:\\WebService\\ProjectAdServingWebservice\\VdopiaAdserving";
				}
			}
			else
			{
				varAutomationHome = TestSuiteClass.AUTOMATION_HOME;
			}

			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Environment Variable= " +varAutomationHome + " Has Been Set. ");

			// Now we will add path to conf folder and qaconf.properties is the file which will be needed to fetch the configurations.
			String config = varAutomationHome.concat("/conf/qaconf.properties");

			propertyConfigFile.load(config);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Error occurred While Reading Config File, Ensure that Config file is at the mentioned path. ", e);
		}
		//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Property File Is Successfully Loaded:" +System.getenv("AUTOMATION_CONF").toString());
	}


	//********** Establishing JDBC Connection to Mysql database: *********************************************//
	public static Connection CreateSQLConnection()  
	{
		Connection qaConnection = null;
		try
		{
			GenericMethodsLib.InitializeConfiguration();

			String dbClass = "com.mysql.jdbc.Driver";		
			Class.forName(dbClass);

			// Getting Values for dburl,dbUsername and dbPassword from configuration file
			String dburl = propertyConfigFile.getProperty("dbURL").toString();
			String dbuserName = propertyConfigFile.getProperty("dbUserName").toString();
			String dbpassword = propertyConfigFile.getProperty("dbPassword").toString();

			qaConnection = (Connection) DriverManager.getConnection (dburl,dbuserName,dbpassword);
		}
		catch(NullPointerException e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : NullPointerException Handled By Method CreateSQLConnection, Plz check Config Values or Initialize Config by calling Method - InitializeConfiguration", e);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while creating sql connection. ", e);
		}
		//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : SQL Connection Was Made Successfully By Method CreateSQLConnection: " +url + " ; " + userName + " ; " +password);
		return qaConnection;
	}


	//********** Establishing JDBC Connection: *********************************************//
	public static Connection CreateServeSQLConnection()  
	{
		Connection qaServeConnection = null;
		int i = 0;
		try
		{
			GenericMethodsLib.InitializeConfiguration();

			String dbClass = "com.mysql.jdbc.Driver";		
			Class.forName(dbClass);

			// Getting Values from configuration file
			String url = propertyConfigFile.getProperty("serveDBURL").toString();
			String userName = propertyConfigFile.getProperty("serveDBUserName").toString();
			String password = propertyConfigFile.getProperty("serveDBPassword").toString();

			qaServeConnection = (Connection) DriverManager.getConnection (url,userName,password);
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : SQL Connection Was Made Successfully By Method CreateSQLConnection: " +url + " ; " + userName + " ; " +password);
		}
		catch(NullPointerException n)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There is SQL Connection Problem: By Method - CreateServeSQLConnection: ", n);
			if(qaServeConnection == null && i < 5)
			{
				i++;
				/** retry to get session with serve db */
				qaServeConnection = GenericMethodsLib.CreateServeSQLConnection();	
			}

			if(qaServeConnection == null && i >= 5)
			{
				logger.error("DB connection wasn't made, exiting tests ..."); 
				Assert.fail("DB connection wasn't made, exiting tests ... ");
			}
		}
		catch (Exception e) {
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while creating serve sql connection. ", e);
			if(qaServeConnection == null && i < 5)
			{
				i++;
				/** retry to get session with serve db */
				qaServeConnection = GenericMethodsLib.CreateServeSQLConnection();	
			}

			if(qaServeConnection == null && i >= 5)
			{
				logger.error("DB connection wasn't made, exiting tests ..."); 
				Assert.fail("DB connection wasn't made, exiting tests ... ");
			}
		}
		return qaServeConnection;
	}


	//********** Establishing JDBC Connection: *********************************************//
	public static Connection CreateReportSQLConnection() throws SQLException, ClassNotFoundException 
	{
		Connection qaReportConnection = null;
		try
		{
			String dbClass = "com.mysql.jdbc.Driver";		
			Class.forName(dbClass);

			// Getting Values from configuration file
			String url = propertyConfigFile.getProperty("reportDBURL").toString();
			String userName = propertyConfigFile.getProperty("reportDBUserName").toString();
			String password = propertyConfigFile.getProperty("reportDBPassword").toString();

			qaReportConnection = (Connection) DriverManager.getConnection (url,userName,password);
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : SQL Connection Was Made Successfully By Method CreateSQLConnection: " +url + " ; " + userName + " ; " +password);
		}
		catch(NullPointerException n)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There is SQL Connection Problem: By Method - CreateReportSQLConnection: ", n);
		}
		return qaReportConnection;
	}



	//********** Executing MySQL Query and Returning Result Set: *********************************************//
	public static ResultSet ExecuteMySQLQueryReturnsResultSet(Connection con, String sqlQuery) throws SQLException 
	{		
		try{
			Statement stmt = (Statement) con.createStatement();
			ResultSet rs = (ResultSet) stmt.executeQuery(sqlQuery);
			return rs;
		}catch(MySQLSyntaxErrorException m){
			logger.error(m.getMessage());
			return null;
		}
	}



	//********** Get Current Time: *********************************************//
	public static String GetCurrentDBTime()  
	{	
		String currentTime = "";

		try
		{
			String sqlQuery = "Select NOW() as CurrentDateTime;";
			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Getting current database time by executing query: "+sqlQuery);

			Connection con = CreateServeSQLConnection();
			Statement stmt = (Statement) con.createStatement();
			ResultSet rs = (ResultSet) stmt.executeQuery(sqlQuery);

			while(rs.next())
			{
				currentTime = rs.getString("CurrentDateTime");
			}
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Current DB Time Is: " +currentTime);
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting current database time: ", e);
		}
		return currentTime;
	}






	//********** Get Input Data From MySQL DB Using A QUERY And Returning Two D Array: ***********************************//
	public static String [][] GetInputDataFromMySQL(Connection con, String deviceType) 
	{               
		String vast2Vdo = "";
		String online_Channel_Supported_Ad_Formats = "";

		if(isVast2vdo.equalsIgnoreCase("vast2vdo"))
		{
			vast2Vdo = " = ";
		}
		else
		{
			vast2Vdo = " <> ";
		}

		if(deviceType.equalsIgnoreCase("pc"))
		{
			online_Channel_Supported_Ad_Formats = "IFNULL(chset.ad_format,0) AS Online_Channel_Supported_Ad_Formats, ";
		}

		String publisherEmail = propertyConfigFile.getProperty("publisherEmail").toString();
		//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : PUBLISHER EMAIL: " +publisherEmail.toString());

		String [][]arrayRecords = null;

		//replacing [] - which is coming from config file on giving multiple values
		publisherEmail = publisherEmail.replace("[", "");
		publisherEmail = publisherEmail.replace("]", "");


		try
		{
			//Added cam.review_status = 'Approved' to get only started campaigns
			String sqlSelectQuery = "SELECT pub.email AS Publisher_Email, cam.name AS Campaign_Name, ch.apikey AS Channel_APIKEY, ch.publisher_id AS Publisher_ID, " +
					" ch.id AS Channel_ID, cam.id AS Campaign_ID, ad.id AS ADS_ID, IFNULL(cam.video_choice,0) AS Video_Choice, IFNULL(cam.custom_details,'') AS Custom_Details," +
					" IFNULL(chset.additional_settings, '') AS Channel_Settings, " +
					" IFNULL(ad.ad_format, 'NoAdFormatSaved') AS Ad_Format, CEIL(IFNULL(ad.duration,0)) AS Ads_Duration, IFNULL(ad.dimension,0) AS Ads_Dimension, IFNULL(ad.tracker_url,0) AS Tracker_URL, " +
					" IFNULL(ad.destination_url,0) AS Destination_URL, cam.device As Device_Type, " +
					" IFNULL(ad.action_type,0) AS Action_Type, "+ online_Channel_Supported_Ad_Formats +" IFNULL(ad.ad_details, 0) AS Ad_Details, " +
					" IFNULL(ad.branded_img_bot_ref_txbody, '') AS CompanionBanner " +
					" FROM channels ch INNER JOIN channel_settings chset ON ch.id = chset.channel_id INNER JOIN publisher pub ON ch.publisher_id = pub.id " +
					" INNER JOIN campaign cam ON ch.id = cam.channel_choice " +
					" INNER JOIN campaign_members camb ON cam.id = camb.cid " + "INNER JOIN ads ad ON ad.id = camb.ad_id " +
					" AND cam.status = 'active' AND camb.status = 'enabled' AND cam.review_status = 'Approved' AND cam.validto > CURDATE() AND pub.email in ("+ publisherEmail +") AND cam.device = " + "'" + deviceType + "'" +
					" where cam.id NOT IN (SELECT cid from campaign_target) AND ad.ad_format <> 'tracker' " +
					" AND ad.ad_format "+ vast2Vdo +" 'vast2vdo' " +
					" ORDER BY cam.id ASC;";


			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL Query To Get Test DATA : " +sqlSelectQuery);

			Statement stmt = (Statement) con.createStatement();
			ResultSet rs = (ResultSet) stmt.executeQuery(sqlSelectQuery);

			rs.last();      // Setting the cursor at last
			int rows = rs.getRow();
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - GetInputDataFromMySQL: rows in result set: " +rows);

			int columns = rs.getMetaData().getColumnCount();
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - GetInputDataFromMySQL: columns in result set: "+columns);

			arrayRecords = new String[rows+1][columns];

			rs.beforeFirst();       // Setting the cursor at first line

			while (rs.next())
			{
				for(int i=1;i<=columns;i++)
				{
					if(rs.getRow()==1)
					{
						String strRecord = rs.getMetaData().getColumnLabel(i).toString();
						arrayRecords[rs.getRow()-1][i-1] = strRecord;

						String strRecord_1 = rs.getString(i).toString();
						arrayRecords[rs.getRow()][i-1] = strRecord_1;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Content Getting Stored: " +strRecord);
					}
					else
					{
						String strRecord = rs.getString(i).toString();
						arrayRecords[rs.getRow()][i-1] = strRecord;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Content Getting Stored: " +strRecord);
					}
				}
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ");
			}                            
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : MySQL Data with Column Names Was Successfully Exported By Method GetInputData. Rows: " +arrayRecords.length + ", Columns: "+arrayRecords[0].length);
		}
		catch(NullPointerException n)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : A NULL Record is returned in some column, Check The Query Result, returning a NULL array by Method : GetInputDataFromMySQL:", n);
		}
		catch (ArrayIndexOutOfBoundsException a) {
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set for the given publisher, returning a NULL array by Method : GetInputDataFromMySQL:", a);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by Method : GetInputDataFromMySQL: ",e);
		}
		return arrayRecords;
	}


	//********** For Preroll - Get Input Data From MySQL DB Using A QUERY And Returning Two D Array: ***********************************//
	public static String [][] GetInputDataFromMySQLForSDK(Connection con, String deviceType, String sdkType) 
	{               
		String publisherEmail = propertyConfigFile.getProperty("publisherEmail").toString();
		//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : PUBLISHER EMAIL: " +publisherEmail.toString());

		String sdkSubQuery;
		String [][]arrayRecords = null;

		//replacing [] - which is coming from config file 
		publisherEmail = publisherEmail.replace("[", "");
		publisherEmail = publisherEmail.replace("]", "");

		if(sdkType.equalsIgnoreCase("MediaPlayer"))
		{
			sdkSubQuery = " and ad.ad_format = 'preroll' " ;
		}
		else
		{
			sdkSubQuery = " and ad.ad_format not like '%preroll%' ";
		}

		try
		{
			//Added cam.review_status = 'Approved' to get only started campaigns
			String sqlSelectQuery = "SELECT pub.email AS Publisher_Email, cam.name AS Campaign_Name, ch.apikey AS Channel_APIKEY, ch.publisher_id AS Publisher_ID, " +
					" ch.id AS Channel_ID, cam.id AS Campaign_ID, ad.id AS ADS_ID, IFNULL(cam.video_choice,0) AS Video_Choice, IFNULL(cam.custom_details,0) AS Custom_Details," +
					" IFNULL(ad.ad_format, 'NoAdFormatSaved') AS Ad_Format, CEIL(IFNULL(ad.duration,0)) AS Ads_Duration, IFNULL(ad.dimension,0) AS Ads_Dimension, cam.device As Device_Type, " +
					" IFNULL(ad.action_type,0) AS Action_Type, IFNULL(cam.expandable,0) AS Expandable_Video, IFNULL(ad.template_layout,0) AS Ad_Template_Layout, " +
					" IFNULL(ad.branded_img_bot_ref_txbody, '') AS CompanionBanner " +
					" FROM channels ch INNER JOIN channel_settings chset ON ch.id = chset.channel_id INNER JOIN publisher pub ON ch.publisher_id = pub.id " +
					" INNER JOIN campaign cam ON ch.id = cam.channel_choice " +
					" INNER JOIN campaign_members camb ON cam.id = camb.cid " + "INNER JOIN ads ad ON ad.id = camb.ad_id " +
					" AND cam.status = 'active' AND cam.review_status = 'Approved' AND cam.validto > CURDATE() AND pub.email in ("+ publisherEmail +") AND cam.device = " + "'" + deviceType + "'" +
					" where cam.id NOT IN (SELECT cid from campaign_target) AND ad.ad_format <> 'tracker' " + sdkSubQuery +
					" ORDER BY cam.id ASC ";


			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL Query To Get Test DATA : " +sqlSelectQuery);

			Statement stmt = (Statement) con.createStatement();
			ResultSet rs = (ResultSet) stmt.executeQuery(sqlSelectQuery);

			rs.last();      // Setting the cursor at last
			int rows = rs.getRow();
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - GetInputDataFromMySQL: rows in result set: " +rows);

			int columns = rs.getMetaData().getColumnCount();
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - GetInputDataFromMySQL: columns in result set: "+columns);

			arrayRecords = new String[rows+1][columns];

			rs.beforeFirst();       // Setting the cursor at first line

			while (rs.next())
			{
				for(int i=1;i<=columns;i++)
				{
					if(rs.getRow()==1)
					{
						String strRecord = rs.getMetaData().getColumnLabel(i).toString();
						arrayRecords[rs.getRow()-1][i-1] = strRecord;

						String strRecord_1 = rs.getString(i).toString();
						arrayRecords[rs.getRow()][i-1] = strRecord_1;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Content Getting Stored: " +strRecord);
					}
					else
					{
						String strRecord = rs.getString(i).toString();
						arrayRecords[rs.getRow()][i-1] = strRecord;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Content Getting Stored: " +strRecord);
					}
				}
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ");
			}                                
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : MySQL Data with Column Names Was Successfully Exported By Method GetInputData. Rows: " +arrayRecords.length + ", Columns: "+arrayRecords[0].length);
		}
		catch(NullPointerException n)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : A NULL Record is returned in some column, Check The Query Result, returning a NULL array by Method : GetInputDataFromMySQL:", n);
		}
		catch (ArrayIndexOutOfBoundsException a) {
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set for the given publisher, returning a NULL array by Method : GetInputDataFromMySQL:", a);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by Method : GetInputDataFromMySQL: ", e);
		}
		return arrayRecords;
	}



	//********** Executing MySQL Query and Returning 2 D Array containing the Result Set without Column Name) *********************************************//
	public static String [][] ExecuteMySQLQueryReturnsArray(Connection con, String sqlQuery) 
	{		

		String [][]arrayRecords = null;

		try
		{
			ResultSet rs = ExecuteMySQLQueryReturnsResultSet(con, sqlQuery);

			rs.last();	// Setting the cursor at last
			int rows = rs.getRow();
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : rows in result set: " +rows);

			int columns = rs.getMetaData().getColumnCount();
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" :  Column Count: "+columns);

			arrayRecords = new String[rows][columns];

			rs.beforeFirst();	// Setting the cursor at first line	
			while (rs.next())
			{
				for(int i=1;i<=columns;i++)
				{
					String strRecord = rs.getString(i).toString();
					arrayRecords[rs.getRow()-1][i-1] = strRecord;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writing Rows BY METHOD - ExecuteMySQLQueryReturnsArray: " +strRecord);
					//}
				}
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ");
			}			
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : MySQL Data Was Successfully Exported By Method ExecuteMySQLQueryReturnsArray. Rows: " +arrayRecords.length + ", Columns: "+arrayRecords[0].length);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set, Therefore returning a NULL array by Method : ExecuteMySQLQueryReturnsArray:", e);
		}
		catch (NullPointerException e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : NullPointerExpection Handled By: ExecuteMySQLQueryReturnsArray", e);
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Used MySQL query may have returned a NULL column in Result Set, Therefore use IFNULL with that particular column in query.", e);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Expection Handled By: ExecuteMySQLQueryReturnsArray", e);
		}

		return arrayRecords;
	}



	//********** Executing MySQL Query and Returning 1 D Array containing the Result Set without Column Name *********************************************//
	public static String [] ExecuteMySQLQueryReturns1DArray(Connection con, String sqlQuery) 
	{		
		String []arrayRecords = null;

		try
		{
			ResultSet rs = ExecuteMySQLQueryReturnsResultSet(con, sqlQuery);

			if(rs !=null)
			{
				int columns = rs.getMetaData().getColumnCount();
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" :  Column Count: "+columns);

				arrayRecords = new String[columns];

				rs.beforeFirst();	// Setting the cursor at first line	
				while (rs.next())
				{
					for(int i=1;i<=columns;i++)
					{
						String strRecord = rs.getString(i).toString();
						arrayRecords[i-1] = strRecord;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writing Rows BY METHOD - ExecuteMySQLQueryReturns1DArray: " +arrayRecords[i-1]);
					}
				}	
			}
			else
			{
				logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Received NULL record set for the supplied query: "+sqlQuery);
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set, Therefore returning a NULL array by Method : ExecuteMySQLQueryReturns1DArray:", e);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled By: ExecuteMySQLQueryReturns1DArray. ", e);
		}

		return arrayRecords;
	}


	/** Executing MySQL Query and Returning 1 D Array containing the Result Set without Column Name 
	 * 
	 * @param con
	 * @param sqlQuery
	 * @return
	 */
	@SuppressWarnings("finally")
	public static List<String> ExecuteMySQLQueryReturnsList(Connection con, String sqlQuery)
	{		
		List<String> recordList = new ArrayList<String>();

		try
		{
			ResultSet rs = ExecuteMySQLQueryReturnsResultSet(con, sqlQuery);

			int columns = rs.getMetaData().getColumnCount();

			rs.beforeFirst();	// Setting the cursor at first line	
			while (rs.next())
			{
				for(int i=1;i<=columns;i++)
				{
					String strRecord = rs.getString(i).toString().trim();
					recordList.add(strRecord);
				}
			}		
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set, Therefore returning a NULL array by Method : ExecuteMySQLQueryReturns1DArray:", e);
		}
		catch (NullPointerException e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : NullPointerExpection Handled By: ExecuteMySQLQueryReturns1DArray", e);
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Used MySQL query may have returned a NULL column in Result Set, Therefore use IFNULL with that particular column in query.", e);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Expection Handled By: ExecuteMySQLQueryReturnsList. " ,e);
		}
		finally
		{
			return recordList;
		}
	}


	//********** Executing MySQL Query and Returning 1 D Array containing the Only Column Name Of Result Set *********************************************//
	public static String [] ExecuteMySQLQueryReturnsOnlyColumnNames(Connection con, String sqlQuery) throws SQLException
	{		
		String []arrayRecords = null;

		try
		{
			ResultSet rs = ExecuteMySQLQueryReturnsResultSet(con, sqlQuery);

			int columns = rs.getMetaData().getColumnCount();
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" :  Column Count: "+columns);

			arrayRecords = new String[columns];

			rs.beforeFirst();	// Setting the cursor at first line	
			while (rs.next())
			{
				for(int i=1;i<=columns;i++)
				{
					String strRecord = rs.getMetaData().getColumnLabel(i).toString();
					arrayRecords[i-1] = strRecord;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writing Rows BY METHOD - ExecuteMySQLQueryReturnsOnlyColumnNames: " +strRecord);
				}
			}		
			con.close();			
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set, Therefore returning a NULL array by Method : ExecuteMySQLQueryReturnsOnlyColumnNames:", e);
		}
		catch (NullPointerException e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : NullPointerExpection Handled By: ExecuteMySQLQueryReturnsOnlyColumnNames", e);
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Used MySQL query may have returned a NULL column in Result Set, Therefore use IFNULL with that particular column in query.",e);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Expection Handled By: ExecuteMySQLQueryReturnsOnlyColumnNames. ", e);
		}

		return arrayRecords;
	}



	//********** Executing MySQL Query and Returning 2 D Array containing the Result Set with Column Name) *********************************************//
	public static String [][] ExecuteMySQLQueryReturnsArrayWithColumnName(Connection con, String sqlQuery) 
	{		
		String [][]arrayRecords = null;
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running this query: "+sqlQuery);

		try
		{
			ResultSet rs = ExecuteMySQLQueryReturnsResultSet(con, sqlQuery);
			/*
			//Un-comment this for debugging
			while (rs.next())
			{
				for(int i=1;i<=rs.getMetaData().getColumnCount();i++)
				{
					String strRecord = rs.getString(i).toString();
					System.out.print(" : "+strRecord);
				}
				logger.info();
			}
			 */
			rs.last();	// Setting the cursor at last
			int rows = rs.getRow();
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : rows in result set: " +rows);

			int columns = rs.getMetaData().getColumnCount();
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" :  Column Count: "+columns);

			arrayRecords = new String[rows+1][columns];

			rs.beforeFirst();	// Setting the cursor at first line

			while (rs.next())
			{
				int currentRow = rs.getRow();

				for(int i=1;i<=columns;i++)
				{
					if(currentRow == 1)
					{
						String strRecord = rs.getMetaData().getColumnLabel(i).toString();
						arrayRecords[currentRow-1][i-1] = strRecord;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Column Label: " +strRecord);

						String strRecord_1 = rs.getString(i).toString();
						arrayRecords[currentRow][i-1] = strRecord_1;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Record: " +strRecord_1);
					}
					else
					{
						String strRecord = rs.getString(i).toString();
						arrayRecords[currentRow][i-1] = strRecord;
						//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : record in result set: " +strRecord);
					}
				}

			}					
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : MySQL Data Was Successfully Exported By Method ExecuteMySQLQueryReturnsArray. Rows: " +arrayRecords.length + ", Columns: "+arrayRecords[0].length);

		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set, Therefore returning a NULL array by Method : ExecuteMySQLQueryReturnsArray:", e);
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : ExecuteMySQLQueryReturnsArray: ",e);
		}

		/*
		// Only for debugging
		for(int i=0; i<arrayRecords.length; i++)
		{
			for(int j=0; j<arrayRecords[0].length; j++)
			{
				System.out.print(" : " +arrayRecords[i][j]);
			}
			logger.info();
		}
		 */

		return arrayRecords;
	}



	//*********************** Writing MYSQL Data Into Excel Sheet: *********************************************//
	public static void WritingMySQLRecordsInExcelSheet (String[][] strRecord) throws IOException, RowsExceededException, WriteException
	{
		//Creating Test Data Folder if it doesn't exist.
		File testDataFolderLocation = new File(TestSuiteClass.AUTOMATION_HOME.concat("/tc_data/adserve/mobileAdServe/DataToFormURL"));

		if(!(testDataFolderLocation.exists()))
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test Data folder doesn't exist at " +TestSuiteClass.AUTOMATION_HOME.concat("/tc_data/adserve/mobileAdServe/DataToFormURL"));
			boolean b = testDataFolderLocation.mkdirs();

			if(b)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test data folder was created successfully "); 
			}
			else
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test data folder wasn't created");
			}
		}

		//Read file name from configuration file and forming the file path using the system environment variable
		String fileName = propertyConfigFile.getProperty("testDataFile").toString();
		String fileNameWithLocation = TestSuiteClass.AUTOMATION_HOME.concat("/tc_data/adserve/mobileAdServe/DataToFormURL/").concat(fileName).toString();

		String sheetName = propertyConfigFile.getProperty("testDataSheet").toString();

		//Delete Existing Test Data File Before Creating It 
		File checkFile;
		checkFile = new File(fileNameWithLocation);

		if (checkFile.exists())
		{
			checkFile.delete();
		}

		//Writing The Data Into Excel After Deleting Existing File
		FileOutputStream outputfile = new FileOutputStream(fileNameWithLocation,true);
		WritableWorkbook book = Workbook.createWorkbook(outputfile);
		WritableSheet sheet = book.createSheet(sheetName, 0);

		try
		{
			for(int i=0; i<strRecord.length; i++)		// For Every Row
			{
				for(int j=0; j<strRecord[0].length; j++)	// For Every Column
				{
					Label lblRecordData = new Label(j, i, strRecord[i][j]);
					sheet.addCell(lblRecordData);
					// logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Printing Elements : "+strRecord[i][j]);
				}
				// logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ");
			}
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : WritingMySQLRecordsInExcelSheet:", e);
		}
		finally
		{
			book.write();
			book.close();		
		}
		//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Data Was Successfully Written In Excel Sheet By Method WritingExcelSheet: " + " Rows: " +strRecord.length + ", Total Columns: " +strRecord[0].length);
	}			    


	/** This method will return the flag to be used to determine medium or max video / also Banner or expandable banner based on
	 * supplied Campaign.Custom_Details
	 * 
	 * @param str
	 * @return
	 */

	public static String getFlagForMaxVideoAndFullScreenBanner(String str)
	{
		//String x = "{\"vdo_lm_txt\":\"Learn More - Modified\",\"vdo_adsby_text\":\"Ads By Vdopia - Modified\",\"fullscreenbanner\":null,\"fullscreen\":null,\"slideInter\":null}";

		String flag = "";
		String fullscreenvideo = "0";
		String fullscreenbanner = "0";

		List<String> customDetails = Arrays.asList(str.split(","));

		for(int i=0; i<customDetails.size(); i++)
		{
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Details Of Custom Details separated by , : "+customDetails.get(i));

			if (customDetails.get(i).toLowerCase().contains("\"fullscreenbanner\":1"))
			{
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Getting Full Screen Banner Flag From String - " +customDetails.get(i));

				List<String> list = Arrays.asList(customDetails.get(i).split(":"));
				try
				{
					fullscreenbanner = list.get(1);
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Full Screen Banner Flag For Video Ad: " +fullscreenbanner );
				}
				catch (ArrayIndexOutOfBoundsException e) 
				{
					fullscreenbanner = "0";
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Full Screen Banner Flag For Video Ad: " + "0");
				}
			}
			else if(customDetails.get(i).toLowerCase().contains("\"fullscreen\":1") )
			{
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Getting Max / Med Video Flag From String - " +customDetails.get(i));

				List<String> list = Arrays.asList(customDetails.get(i).split(":"));
				try
				{
					fullscreenvideo = list.get(1);
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Max/Med Video Flag: " +flag );
				}
				catch (ArrayIndexOutOfBoundsException e) 
				{
					fullscreenvideo = "0";
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Max/Med Video Flag: " + "0");
				}
			}
		}

		flag = fullscreenbanner + "," + fullscreenvideo ;
		return flag;
	}


	//******************** Reading Test URLs, Ad Type, Campaign Id from DataToFormURL.xls Spreadsheet and Return 2 D Array *******************************************//
	public static String[][] FetchTestURLFromExcelSheet() throws IOException, RowsExceededException, WriteException, BiffException          
	{       
		//Read file name from configuration file and forming the file path using the system environment variable
		String fileName = propertyConfigFile.getProperty("testDataFile").toString();
		String fileNameWithLocation = TestSuiteClass.AUTOMATION_HOME.concat("/tc_data/adserve/mobileAdServe/DataToFormURL/").concat(fileName).toString();

		String sheetName = propertyConfigFile.getProperty("testDataSheet").toString();

		Workbook book = Workbook.getWorkbook(new File(fileNameWithLocation));
		//WritableWorkbook copiedBook = Workbook.createWorkbook(new File(fileNameWithLocation), book);  
		Sheet sheet = book.getSheet(sheetName);

		CellFinder cellFind = new CellFinder(sheet);    //Finding the Cell with a particular text and later on get the corresponding Row or Column;

		//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : rows: " +sheet.getRows() + " column: " +sheet.getColumns());

		String [][]testDataProvider = new String[sheet.getRows()-1][5];
		// This will work as Data Provider - returning columns as - Ad_Format, Test_URLs, Campaign_ID, Channel_ID

		for(int row=1;row<sheet.getRows();row++)
		{
			//Getting device = iphone
			String strDevice = sheet.getCell(cellFind.findLabelCell("Device_Type").getColumn(), row).getContents().toString();;

			//Getting data only for device = iphone, in case test data excel sheet contain data for both mobile and online  
			if(strDevice.equalsIgnoreCase("iphone"))
			{
				String strAd_Format = sheet.getCell(cellFind.findLabelCell("Ad_Format").getColumn(), row).getContents().toString();
				testDataProvider[row-1][0] = strAd_Format;
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : strAdFormat: " +strAd_Format);

				String strTest_URLs = sheet.getCell(cellFind.findLabelCell("Test_URLs").getColumn(), row).getContents().toString();
				testDataProvider[row-1][1] = strTest_URLs;
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : strTest_URLs: " +strTest_URLs);

				String strADS_ID = sheet.getCell(cellFind.findLabelCell("Campaign_ID").getColumn(), row).getContents().toString();
				testDataProvider[row-1][2] = strADS_ID;
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : strTest_URLs: " +strCAMPAIGN_ID);

				String strChannel_ID = sheet.getCell(cellFind.findLabelCell("Channel_ID").getColumn(), row).getContents().toString();
				testDataProvider[row-1][3] = strChannel_ID;
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : strTest_URLs: " +strChannel_ID);

				String strAds_Duration = sheet.getCell(cellFind.findLabelCell("Ads_Duration").getColumn(), row).getContents().toString();
				testDataProvider[row-1][4] = strAds_Duration;
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : strAds_Duration: " +strAds_Duration);
			}
		}      
		book.close();

		return testDataProvider;                            
	}



	//********* Forming Test URLs For Various Mobile Ads **********************************************//
	public static String formChannelTestURLForMobileAds(String strAdType, String strChannelAPIKeyFromExcel, String strAdDimension, String flagMedMaxVdo, String strActionType) 
	{
		String strTestURL = "NOTHING";


		//Reading Base Test URL For Mobile from configuration file
		String strBaseTestURL = propertyConfigFile.getProperty("mobileBaseTestURL").toString();

		if(strAdType.equalsIgnoreCase("banner"))
		{   	
			//check if banner is a leader board
			if(strAdDimension.equalsIgnoreCase("728x90"))
			{
				//Actual URL - http://qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;adFormat=banner;ak=a8eb8668d47346afd2219aab2f32129b;version=1.0;dimension=728x90;cb=[timestamp];output=html;

				String strTestURL_Part2 = "sleepAfter=0;adFormat=banner";
				String strTestURL_Part3 = ";ak=";
				String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
				String strTestURL_Part5 = ";version=1.0;dimension=728x90;cb=[timestamp];output=html";

				//Forming Test URL
				strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL For Tablet Banner, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
			}
			else
			{
				List<String> flags = Arrays.asList(flagMedMaxVdo.split(","));
				String fullscreenbanner = flags.get(0);
				//String fullscreenvideo = flags.get(1);

				//Check if Banner is fullscreenbanner
				if(fullscreenbanner.equalsIgnoreCase("1"))
				{
					String strTestURL_Part2 = "sleepAfter=0;adFormat=banner";
					String strTestURL_Part3 = ";ak=";
					String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
					String strTestURL_Part5 = ";version=1.0;dimension=[320x480];cb=[timestamp];output=html";

					//Forming Test URL
					strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL For Tablet Banner, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
				}
				else
				{
					String strTestURL_Part2 = "sleepAfter=0;adFormat=";
					String strTestURL_Part3 = ";ak=";
					String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
					String strTestURL_Part5 = ";version=1.0;cb=[timestamp];output=html";

					//Forming Test URL
					strTestURL = strBaseTestURL + strTestURL_Part2 + "banner" + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL For Banner, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
				}
			}
		}
		else if(strAdType.equalsIgnoreCase("html"))		
		{   		
			//Actual URL - http://serve.qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;adFormat=banner;ak=9a5051eea1e7592ea3b80d5393cf5cdc;version=1.0;expandable=required;cb=[timestamp]

			String strTestURL_Part2 = "sleepAfter=0;adFormat=banner";
			String strTestURL_Part3 = ";ak=";
			String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
			String strTestURL_Part5 = ";version=1.0;expandable=required;cb=[timestamp];output=html";

			//Forming Test URL
			strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for HTML Ad, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
		}
		else if(strAdType.equalsIgnoreCase("vdobanner"))	
		{   			
			//Actual URL - http://serve.qa.vdopia.com/adserver/html5/inwapads?version=1.0;ak=e4ddc9afeb416f555bc59a4977434760;adFormat=vdobanner;cb=[timestamp]

			String strTestURL_Part2 = "version=1.0";
			String strTestURL_Part3 = ";ak=";
			String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
			String strTestURL_Part5 = ";adFormat=vdobanner;cb=[timestamp];output=html";

			//Forming Test URL
			strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for MiniVDO, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
		}
		else if(strAdType.equalsIgnoreCase("appinterstitial"))	
		{   			
			//Actual URL - http://serve.qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;adFormat=preinter;ak=ee0ec38ee13dc47653a48d4b6ec0da38;version=1.0;fullscreen=1;vdo=1;cb=[timestamp]

			String strTestURL_Part2 = "sleepAfter=0;adFormat=preinter";
			String strTestURL_Part3 = ";ak=";
			String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
			String strTestURL_Part5 = ";version=1.0;fullscreen=1;vdo=1;cb=[timestamp];output=html";

			//Forming Test URL
			strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for Interstitial, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
		}
		else if(strAdType.equalsIgnoreCase("video") || strAdType.equalsIgnoreCase("vastfeed"))	
		{   	
			//Checking if Video ad is actually a Video or a Vast Parent Ad
			if(strActionType.equalsIgnoreCase("vastfeed"))
			{
				//This the vast feed parent URL formation

				String strTestURL_Part2 = "adFormat=preappvideo;version=1.0";
				String strTestURL_Part3 = ";ak=";
				String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
				String strTestURL_Part5 = ";cb=[timestamp];output=vast";

				//Forming Test URL
				strTestURL = strBaseTestURL  + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			}
			else
			{
				//Actual URL Max Vdo - http://qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;adFormat=preappvideo;ak=c365d0596335a00501d7d36f7122b2c4;version=1.0;fullscreen=1;vdo=1;cb=[timestamp]
				//Actual URL Med Vdo - http://serve.qa.vdopia.com/adserver/html5/inwapads/?slide=1;sleepAfter=0;adFormat=preappvideo;ak=2cfcd634953c3e39a88df147251c8303;version=1.0;vdo=1;cb=[timestamp]

				//flagMedMaxVdo is passed as comma separated string --> fullscreenbanner + "," + fullscreen, hence splitting this string to get flag for both video and expandable banner

				List<String> flags = Arrays.asList(flagMedMaxVdo.split(","));
				String fullscreenbanner = flags.get(0);
				String fullscreenvideo = flags.get(1);

				if (	fullscreenvideo.equalsIgnoreCase("1")	&&	fullscreenbanner.equalsIgnoreCase("1")	)
				{
					String strTestURL_Part2 = "sleepAfter=0;adFormat=expandablebanner";
					String strTestURL_Part3 = ";ak=";
					String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
					String strTestURL_Part5 = ";version=1.0;fullscreen=1;vdo=1;cb=[timestamp];output=html";

					//Forming Test URL
					strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for Max Vdo, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
				}	
				else if (	fullscreenvideo.equalsIgnoreCase("1")	&&	(!fullscreenbanner.equalsIgnoreCase("1"))	) 
				{
					String strTestURL_Part2 = "sleepAfter=0;adFormat=preappvideo";
					String strTestURL_Part3 = ";ak=";
					String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
					String strTestURL_Part5 = ";version=1.0;fullscreen=1;vdo=1;cb=[timestamp];output=html";

					//Forming Test URL
					strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for Max Vdo, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
				}
				else if (	(!fullscreenvideo.equalsIgnoreCase("1"))	&&	fullscreenbanner.equalsIgnoreCase("1")	) 
				{
					String strTestURL_Part2 = "sleepAfter=0;adFormat=expandablebanner";
					String strTestURL_Part3 = ";ak=";
					String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
					String strTestURL_Part5 = ";version=1.0;fullscreen=1;vdo=1;cb=[timestamp];output=html";

					//Forming Test URL
					strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for Max Vdo, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
				}
				else
				{	// VAST campaigns are saved as Video therefore this URL can be used for VAST Ads also.
					String strTestURL_Part2 = "slide=1;sleepAfter=0;adFormat=preappvideo";
					String strTestURL_Part3 = ";ak=";
					String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
					String strTestURL_Part5 = ";version=1.0;vdo=1;cb=[timestamp];output=html";

					//Forming Test URL
					strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for Med Vdo, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
				}
			}
		}
		else if(strAdType.equalsIgnoreCase("leadervdo"))	
		{   			
			//Actual URL - http://serve.qa.vdopia.com/adserver/html5/inwapads?slide=1;version=1.0;vdo=1;sleepAfter=0;adFormat=leadervdo;ak=a741164f8a1c863ee0ebf681b119b3d5;cb=[timestamp]

			String strTestURL_Part2 = "slide=1;version=1.0;vdo=1;sleepAfter=0;adFormat=leadervdo";
			String strTestURL_Part3 = ";ak=";
			String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
			String strTestURL_Part5 = ";cb=[timestamp];output=html";

			//Forming Test URL
			strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for Leadervdo, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
		}
		else if(strAdType.equalsIgnoreCase("jsbanner"))	
		{   			
			//Actual URL - http://serve.qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;adFormat=jsbanner;ak=5d8732e7a88ca4706e6d69c6f61d0165;version=1.0;dimension=320x48;showClose=1;cb=[timestamp];output=html

			String strTestURL_Part2 = "sleepAfter=0;adFormat=jsbanner";
			String strTestURL_Part3 = ";ak=";
			String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
			String strTestURL_Part5 = ";version=1.0;dimension="+strAdDimension+";showClose=1;cb=[timestamp];output=html";

			//Forming Test URL
			strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for JS Banner, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
		}
		else if(strAdType.equalsIgnoreCase("htmlinter"))	
		{   			
			//Actual URL - http://serve.qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;adFormat=jsinter;ak=d5697f6376765eebd3072357577f6af8;version=1.0;dimension=320x480;showClose=1;cb=[timestamp];output=html

			String strTestURL_Part2 = "sleepAfter=0;adFormat=htmlinter";
			String strTestURL_Part3 = ";ak=";
			String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
			String strTestURL_Part5 = ";version=1.0;dimension="+strAdDimension+";showClose=1;cb=[timestamp];output=html";

			//Forming Test URL
			strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for JS Inter, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
		}
		else if(strAdType.startsWith("inview"))
		{ 
			//In-view Vast Ad
			if(strActionType.equalsIgnoreCase("vastfeed"))
			{
				//Actual URL - http://serve.vdopia.com/adserver/html5/inwapads/?responds_to_scroll=1;target_div_id=rand_tar_id_1407305377;sleepAfter=0;adFormat=inview;ak=b2e8c24139fc7c020673ee9e4b1c324d;version=1.0;showClose=1;cb=[timestamp];output=html
				String strTestURL_Part2 = "responds_to_scroll=1;target_div_id=rand_tar_id_1407305377;sleepAfter=0;adFormat=inview";
				String strTestURL_Part3 = ";ak=";
				String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
				String strTestURL_Part5 = ";version=1.0;showClose=1;cb=[timestamp];output=html";

				//Forming Test URL
				strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
			}
			//In-view Video Ad
			else
			{
				if(isDeviceConnected)
				{
					String onlineBaseTestURL = GenericMethodsLib.propertyConfigFile.getProperty("onlineBaseTestURL").toString(); 
					strTestURL = onlineBaseTestURL + "/InviewAd/" + strChannelAPIKeyFromExcel + "_" + GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss_ms") +".html";		
				}
				else
				{
					//Actual URL - http://serve.vdopia.com/adserver/html5/inwapads/?responds_to_scroll=1;target_div_id=rand_tar_id_1407305297;sleepAfter=0;adFormat=inview;ak=6e8735ca444b54c4747e4f898448f85d;version=1.0;showClose=1;cb=[timestamp];output=html

					String strTestURL_Part2 = "responds_to_scroll=1;target_div_id=rand_tar_id_1407305297;sleepAfter=0;adFormat=inview";
					String strTestURL_Part3 = ";ak=";
					String strTestURL_Part4 = strChannelAPIKeyFromExcel; // channelAPIKeyFromExcel is coming from the Excel Sheet Containing the Record Set 
					String strTestURL_Part5 = ";version=1.0;showClose=1;cb=[timestamp];output=html";

					//Forming Test URL
					strTestURL = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test URL for In-View Video Ad, is Successfully Formed by Method FormingTestURLForMobileAds : " +strTestURL);
				}
			}
		}
		else if(strAdType.contains("prerol"))
		{
			strTestURL = "SDKs can be served only in attached mobile device not on browser. \n Choose SDK test suite for execution. ";
		}
		else
		{
			strTestURL = "NO_URL_FOR_THIS_AD_YET";
		}


		return strTestURL;
	}



	//********* Forming Test URLs For Various Mobile Ads **********************************************//
	@SuppressWarnings("finally")
	public static String formCampaignTestURLForMobileAds(String strAdType, String strCampaignID, String strAdId, String strFlagMedMaxVdo, String strActionType) 
	{
		String strTestURL = "";
		String adTypeForURL = null;

		try
		{
			if(strAdType.equalsIgnoreCase("banner") || strAdType.equalsIgnoreCase("html"))
			{   	
				adTypeForURL = "banner";
			}
			//			else if(strAdType.equalsIgnoreCase("html"))		
			//			{   		
			//				adTypeForURL = "htmlbanner";
			//			}
			else if(strAdType.equalsIgnoreCase("vdobanner"))	
			{
				adTypeForURL = "vdobanner";
			}
			else if(strAdType.equalsIgnoreCase("appinterstitial"))	
			{   			
				adTypeForURL = "preinter";
			}
			else if(strAdType.equalsIgnoreCase("video") || strAdType.equalsIgnoreCase("vastfeed") || strAdType.equalsIgnoreCase("inview"))	
			{   	
				adTypeForURL = "preappvideo";
			}
			else if(strAdType.equalsIgnoreCase("leadervdo"))	
			{   			
				adTypeForURL = "leadervdo";
			}
			else if(strAdType.equalsIgnoreCase("jsbanner"))	
			{   	
				adTypeForURL = "jsbanner";
			}
			else if(strAdType.equalsIgnoreCase("htmlinter"))	
			{   	
				adTypeForURL = "htmlinter";
			}
			//			else if(strAdType.equalsIgnoreCase("inview"))	
			//			{   
			//				adTypeForURL = "inview";
			//			}
			else
			{
				strTestURL = "NO_URL_FOR_THIS_AD_YET";
			}

			//Test URL = http://serve.qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;version=1.0;adFormat=preappvideo;ak=AX123;ai=155272;ci=30758;vdo=1;cb=2841178722984;output=html

			//Reading Base Test URL For Mobile from configuration file
			strTestURL = propertyConfigFile.getProperty("mobileBaseTestURL").toString().concat("sleepAfter=0;version=1.0;");
			strTestURL = strTestURL.concat("adFormat="+adTypeForURL+";ak=AX123;ai="+strAdId+";ci="+strCampaignID+";vdo=1;fullscreen=1;cb=2841178722984;output=html");			
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while forming campaign test url for ad format: "+adTypeForURL + ", campaign id: "+strCampaignID + ", ad id: "+strAdId, e);
		}
		finally
		{
			return strTestURL;
		}
	}



	//******************** Copy Test Data File Into Test Results Folder With Name As TestResults  *************************************************//
	public static File CopyTestDataInResultsFolder() throws IOException, RowsExceededException, WriteException, BiffException, InterruptedException
	{	
		//Getting Test Data WorkBook
		String testDatafileName = propertyConfigFile.getProperty("testDataFile").toString();
		String testDatafileNameWithLocation = TestSuiteClass.AUTOMATION_HOME.concat("/tc_data/adserve/mobileAdServe/DataToFormURL/").concat(testDatafileName).toString();

		File testDataFile = new File(testDatafileNameWithLocation);

		//Defining Test Result WorkBook and Appending DateTime Stamp On File Name 
		String dateTimeStamp = DateTimeStamp();
		String testResultFileName = propertyConfigFile.getProperty("testResultFile").toString().concat("_").concat(dateTimeStamp).concat(".xls");
		String testResultFileNameWithLocation = TestSuiteClass.AUTOMATION_HOME.concat("/results/mobileAdServe/Test_Results/").concat(testResultFileName).toString();
		//System.out.print(testResultFileNameWithLocation);
		File testResultFile = new File(testResultFileNameWithLocation);

		//Copying Test Data File to Test Results Folder
		FileUtils.copyFile(testDataFile, testResultFile);

		return testResultFile;	
	}


	//******************** Write the given list in Test Results excel sheet *************************************************//
	public static void WritingTestResultsInExcelSheet(File testResultFile, List<String> resultsList) throws IOException, RowsExceededException, WriteException, BiffException 
	{      

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : writting results .... ");

		Workbook book = Workbook.getWorkbook(testResultFile);
		WritableWorkbook copiedBook = Workbook.createWorkbook(testResultFile, book);
		WritableSheet sheet = copiedBook.getSheet(0);
		/*    
                    //Setting Cell Format - Right Alignment and Wrap Text
                    WritableCellFormat cellFormat = new WritableCellFormat();
                    cellFormat.setAlignment(Alignment.RIGHT);
                    cellFormat.setWrap(true);
		 */

		int column = sheet.getColumns();
		Label lblColumnName = new Label(column, 0, "Test_Results");     //Adding Column Name = Test_Results in last Column and first row
		sheet.addCell(lblColumnName);

		CellFinder cellFind = new CellFinder(sheet);    //Finding the Cell with a particular text and later on, get the corresponding Row or Column;

		try {
			for(int row=1;row<sheet.getRows();row++)
			{
				String testResult = resultsList.get(row-1).trim().toString();

				//un-comment all syso for debugging.
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : print received result string from tests: " +testResult);

				int testResultColumnNo = cellFind.findLabelCell("Test_Results").getColumn();

				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Test Results Column No: " +testResultColumnNo);

				Label lblTestResult = new Label(testResultColumnNo, row, testResult);

				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Label: " +lblTestResult.toString());

				sheet.addCell(lblTestResult);
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test Results was written Successfully :" +testResult);
			}
		} 
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while writing test results. ", e);
		}
		finally 
		{
			try
			{
				copiedBook.write();
				copiedBook.close();
				book.close();
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test Results was written successfully");
			}
			catch(NullPointerException n)
			{
				logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : NullPointerException Handled while writing test results, file format may have some issues. ", n);
			}
			catch(Exception n)
			{
				logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled while writing test results. ", n);
			}
		}  
	}



	//******************** Write the given list in Test Results excel sheet, here test result column name can be given *************************************************//
	public static void WritingTestResultsInExcelSheet(File testResultFile, List<String> resultsList, String testResultsColumnName) throws IOException, RowsExceededException, WriteException, BiffException, InterruptedException
	{       
		Workbook book = Workbook.getWorkbook(testResultFile);
		WritableWorkbook copiedBook = Workbook.createWorkbook(testResultFile, book);
		WritableSheet sheet = copiedBook.getSheet(0);
		/*    
	                    //Setting Cell Format - Right Alignment and Wrap Text
	                    WritableCellFormat cellFormat = new WritableCellFormat();
	                    cellFormat.setAlignment(Alignment.RIGHT);
	                    cellFormat.setWrap(true);
		 */

		int column = sheet.getColumns();
		Label lblColumnName = new Label(column, 0, testResultsColumnName);     //Adding Column Name = Test_Results in last Column and first row
		sheet.addCell(lblColumnName);

		CellFinder cellFind = new CellFinder(sheet);    //Finding the Cell with a particular text and later on, get the corresponding Row or Column;

		try 
		{
			for(int row=1;row<sheet.getRows();row++)
			{
				String testResult = resultsList.get(row-1).trim().toString();

				//un-comment all syso for debugging.
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : print received result string from tests: " +testResult);

				int testResultColumnNo = cellFind.findLabelCell(testResultsColumnName).getColumn();

				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Test Results Column No: " +testResultColumnNo);

				Label lblTestResult = new Label(testResultColumnNo, row, testResult);

				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Label: " +lblTestResult.toString());

				sheet.addCell(lblTestResult);
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test Results was written Successfully :" +testResult);
			}
		} 
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : WritingTestResultsInExcelSheet. ",e);
		}
		finally 
		{
			try
			{
				copiedBook.write();
				copiedBook.close();
				book.close();
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test Results was written successfully");
			}
			catch(NullPointerException n)
			{
				logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : NullPointerException Handled while writing test results, file format may have some issues. ", n);
			}
			catch(Exception n)
			{
				logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled while writing test results. ", n);
			}
		} 
	}


	//******************** Get Current Date Time Stamp *************************************************//
	public static String DateTimeStamp()
	{
		try
		{
			//Reading Date Format From Configuration File. 
			String dateStampFormat = propertyConfigFile.getProperty("dateStampFormatForFileName").toString();
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Date Time Stamp Format will be:" +dateStampFormat);

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(dateStampFormat);
			String formattedDate = sdf.format(date);
			return formattedDate;
		}catch(Exception n)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Please check variable - dateStampFormatForFileName in config file.", n);
			return null;
		}
	}



	//******************** Get Current Date Time Stamp *************************************************//
	public static String DateTimeStamp(String dateStampFormat)
	{
		try
		{
			//Sample: MMddyyyy_hhmmss
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(dateStampFormat);
			String formattedDate = sdf.format(date);
			return formattedDate;
		}
		catch(Exception n)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Please check the supplied date format. " , n);
			return null;
		}
	}



	//******************** Writing The Date Time Stamp *************************************************//
	public static String DateTimeStampWithMiliSecond()
	{
		try
		{
			String dateStampFormat = "MMddyyyy_hhmmss_ms";
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Date Time Stamp Format will be:" +dateStampFormat);

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(dateStampFormat);
			String formattedDate = sdf.format(date);
			return formattedDate;
		}
		catch(Exception n)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: DateTimeStampWithMiliSecond. ", n);
			return null;
		}
	}



	//******************** Mobile Tracker Validation Upon Browsing The Test URLs *************************************************//
	@SuppressWarnings("finally")
	public static String MobileAds_VdopiaTrackerValidation(String adFormat, String campaignID, String channelID, String trackerStartTime) throws SQLException, ClassNotFoundException
	{
		String passResult = "PASS:";
		String failResult = "FAIL:";
		String finalResult = "";

		try{	
			String trackerEndTime = GetCurrentDBTime();

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DB End Time For Vdopia Tracker Calculation: " +trackerEndTime);
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Tracker Validation Started At: "+GenericMethodsLib.DateTimeStamp("yyMMdd_hhmmss"));

			String sqlQueryForChannelTracker = 	" SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
					" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
					" WHERE channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '"+ trackerEndTime + "') " +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log1 " +
					" WHERE channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '" + trackerEndTime + "') " +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure ";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Channel Tracker Validation: " +sqlQueryForChannelTracker);

			String sqlQueryForCampaignTracker = " SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
					" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '"+ trackerEndTime + "') " +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker  FROM ad_log1 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '" + trackerEndTime + "') " +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Campaign Tracker Validation: " +sqlQueryForCampaignTracker);

			//Getting Channel Trackers In 2 D Array. 
			Connection serveConnectionForChannel = CreateServeSQLConnection();
			String [][] validationDataForChannelTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForChannel, sqlQueryForChannelTracker);
			serveConnectionForChannel.close();

			if (adFormat.equalsIgnoreCase("html") || adFormat.equalsIgnoreCase("banner") || adFormat.equalsIgnoreCase("appinterstitial")  
					|| adFormat.equalsIgnoreCase("jsbanner") || adFormat.equalsIgnoreCase("htmlinter"))
			{
				//First Check - ai tracker
				int aiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ai");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ai : " +aiTrackerCount);

				/*
				if (aiTrackerCount<1)
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("ai:").concat(String.valueOf(aiTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ai Tracker wasn't fired other trackers will not be checked: " +adFormat +" :: " +finalResult);
				}
				else
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Passing at ai Tracker: " +adFormat +" :: " +aiTrackerCount);
				 */
				int uiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ui");	
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ui : " +uiTrackerCount);
				/*
				//Second check - ui tracker - here not checking the multiple occurrence of ui
				if (uiTrackerCount>0)
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
					finalResult = finalResult.concat(":").concat("ui:").concat(String.valueOf(uiTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Inventory Unavailable: ui Tracker: " +adFormat +" :: " +finalResult);
				}

				//Third check - vi / si trackers - here not checking the multiple occurrence of ui
				else
				{
				 */
				//Getting Campaign Trackers In 2 D Array. 
				Connection serveConnectionForCampaign = CreateServeSQLConnection();
				String [][] validationDataForCampaignTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForCampaign, sqlQueryForCampaignTracker);
				serveConnectionForCampaign.close();

				int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : vi tracker for banners: " +viTrackerCount);

				int siTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "si");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : si tracker for banners: " +siTrackerCount);


				if((viTrackerCount<1 || viTrackerCount >1) || (siTrackerCount<1 || siTrackerCount >1) || (aiTrackerCount<1 || aiTrackerCount>1) || uiTrackerCount>0)
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
					finalResult = finalResult.concat(":").concat("ui:").concat(String.valueOf(uiTrackerCount));
					finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));

					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Failing after checking si and vi Tracker for : " +adFormat +" :: " +finalResult);
				}
				else
				{
					finalResult =  passResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
					finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));

					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Passing after checking si and vi Tracker for : " +adFormat +" :: " +finalResult);
				}	
				//}
				//} 
			}

			else if( adFormat.equalsIgnoreCase("video") || adFormat.equalsIgnoreCase("vastfeed") || adFormat.equalsIgnoreCase("leadervdo") || adFormat.equalsIgnoreCase("vdobanner")
					|| adFormat.equalsIgnoreCase("preroll") || adFormat.equalsIgnoreCase("s2rpreroll") || adFormat.equalsIgnoreCase("sobpreroll")
					|| adFormat.equalsIgnoreCase("t2mpreroll") || adFormat.equalsIgnoreCase("mvtpreroll") )
			{
				//First Check - ai tracker - here not checking the multiple occurrence of ui
				int aiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ai");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ai : " +aiTrackerCount);

				/*
				if (aiTrackerCount<1)
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("ai:").concat(String.valueOf(aiTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ai Tracker wasn't fired other trackers will not be checked: " +adFormat +" :: " +finalResult);
				}
				else
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Passing at ai Tracker: " +adFormat +" :: " +finalResult);
				 */
				int uiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ui");	
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ui : " +uiTrackerCount);

				/*
				//Second check - ui tracker - here not checking the multiple occurrence of ui
				if (uiTrackerCount>0)
				{
					finalResult =  failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
					finalResult = finalResult.concat(":").concat("ui:").concat(String.valueOf(uiTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Inventory Unavailable: ui Tracker: " +adFormat +" :: " +finalResult);
				}

				//Third check - vi, si, vi_0, vi_25, vi_50, vi_75, ae trackers
				else
				{
				 */

				//Getting Campaign Trackers In 2 D Array. 
				Connection serveConnectionForCampaign = CreateServeSQLConnection();
				String [][] validationDataForCampaignTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForCampaign, sqlQueryForCampaignTracker);
				serveConnectionForCampaign.close();

				int siTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "si");
				int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
				int vi_0TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_0");
				int vi_25TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_25");
				int vi_50TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_50");
				int vi_75TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_75");
				int aeTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "ae");

				if((viTrackerCount<1 || viTrackerCount >1) || (vi_0TrackerCount<1 || vi_0TrackerCount >1) 
						|| (vi_25TrackerCount<1 || vi_25TrackerCount >1) || (vi_50TrackerCount<1 || vi_50TrackerCount >1) 
						|| (vi_75TrackerCount<1 || vi_75TrackerCount >1) || (aeTrackerCount<1 || aeTrackerCount >1)
						|| (siTrackerCount<1 || siTrackerCount >1) || (aiTrackerCount<1 || aiTrackerCount>1) || uiTrackerCount>0)
				{
					finalResult =  failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
					finalResult = finalResult.concat(":").concat("ui:").concat(String.valueOf(uiTrackerCount));
					finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));
					finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
					finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));	
				}
				else
				{
					finalResult =  passResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
					finalResult = finalResult.concat(":").concat("ui:").concat(String.valueOf(uiTrackerCount));
					finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));
					finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
					finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));
				}
				//}
				//}
			}			
			else
			{
				finalResult = finalResult.concat(failResult).concat(adFormat).concat(" - THIS AD FORMAT IS NOT CODED YET, PLEASE INFORM AUTOMATION TEAM");
			}
		}catch(Exception e)
		{
			logger.info(e.getStackTrace());
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : TrackerValidation: "+e.getMessage());
		}
		finally
		{
			return finalResult;
		}
	}


	//This method will build query without using unique parameter of test url to get channel tracker
	public static String sqlQueryWithOutUniqueParamForChannelTracker(String channelID, String trackerStartTime, String trackerEndTime)
	{
		String sqlQueryForChannelTracker = 	" SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
				" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
				" WHERE channel_id = '" + channelID + "'" + 
				" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '"+ trackerEndTime + "') " +
				" GROUP BY measure " +
				" UNION ALL " +
				" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log1 " +
				" WHERE channel_id = '" + channelID + "'" + 
				" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '" + trackerEndTime + "') " +
				" GROUP BY measure " +
				") AS tmp GROUP BY measure ";

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Channel Tracker Validation: " +sqlQueryForChannelTracker);

		return sqlQueryForChannelTracker;
	}



	//This method will build query without using unique parameter of test url to get campaign tracker
	public static String sqlQueryWithOutUniqueParamForCampaignTracker(String channelID, String campaignID, String trackerStartTime, String trackerEndTime)
	{
		String sqlQueryForCampaignTracker = " SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
				" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
				" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
				" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '"+ trackerEndTime + "') " +
				" GROUP BY measure " +
				" UNION ALL " +
				" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker  FROM ad_log1 " +
				" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
				" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '" + trackerEndTime + "') " +
				" GROUP BY measure " +
				") AS tmp GROUP BY measure ";

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Campaign Tracker Validation: " +sqlQueryForCampaignTracker);

		return sqlQueryForCampaignTracker;
	}



	//This method will build query using unique parameter of test url to get campaign tracker
	public static String sqlQueryWithUniqueParamForCampaignTracker(String channelID, String trackerStartTime, String uniqueRequestParam)
	{
		String sqlQueryForCampaignTracker = " SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
				" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
				" WHERE channel_id = '"+ channelID +"' " +
				" AND uniq_id = '"+ uniqueRequestParam +"' " +

				//" AND tstamp >= (SELECT tstamp FROM ad_log0 WHERE uniq_id = '"+ uniqueRequestParam +"' ORDER  BY  tstamp DESC LIMIT 1 ) " + 
				//" AND cookie_val = (SELECT cookie_val FROM ad_log0 WHERE uniq_id = '"+ uniqueRequestParam +"' ORDER  BY tstamp DESC LIMIT 1) " +

				" AND tstamp >= '" + trackerStartTime + "' " +
				" GROUP BY measure " +
				" UNION ALL " +
				" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker  FROM ad_log1 " +
				" WHERE channel_id = '"+ channelID +"' " +
				" AND uniq_id = '"+ uniqueRequestParam +"' " +

				//" AND tstamp >= (SELECT tstamp FROM ad_log1 WHERE uniq_id = '"+ uniqueRequestParam +"' ORDER  BY  tstamp DESC LIMIT 1 ) " +  
				//" AND cookie_val = (SELECT cookie_val FROM ad_log1 WHERE uniq_id = '"+ uniqueRequestParam +"' ORDER  BY tstamp DESC LIMIT 1) " +

				" AND tstamp >= '" + trackerStartTime + "' " +
				" GROUP BY measure " +
				") AS tmp GROUP BY measure ";

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Campaign Tracker Validation: " +sqlQueryForCampaignTracker);

		return sqlQueryForCampaignTracker;
	}


	//This method will build query using unique parameter of test url to get campaign tracker
	public static String sqlQueryWithUniqueParamForChannelTracker(String trackerStartTime, String uniqueRequestParam)
	{
		String sqlQueryForChannelTracker = 	" SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
				" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
				" WHERE uniq_id = '" + uniqueRequestParam + "'" +  
				" AND tstamp >= '" + trackerStartTime + "' " +
				" GROUP BY measure " +
				" UNION ALL " +
				" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log1 " +
				" WHERE uniq_id = '" + uniqueRequestParam + "'" +  
				" AND tstamp >= '" + trackerStartTime + "' " +
				" GROUP BY measure " +
				") AS tmp GROUP BY measure ";

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Channel Tracker Validation: " +sqlQueryForChannelTracker);

		return sqlQueryForChannelTracker;
	}


	/** Mobile Tracker Validation Upon Browsing The Test URLs in mobile device, this will check the Click, Pause / Unpause, Mute/UnmUte etc.. 
	 * This method is very crucial for ad serving validation as it is being used by Desktop, Mobile Device, Mobile App Ad Serving Validation
	 * All trackers are validated in mysql. 
	 *  
	 * @param adFormat
	 * @param campaignID
	 * @param channelID
	 * @param trackerStartTime
	 * @param uniqueRequestParam
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings({ "finally" })
	public static String MobileAds_VdopiaTrackerValidationForUIOperations(String adFormat, String campaignID, String channelID, String trackerStartTime, String uniqueRequestParam) 
	{
		String failResult = "FAIL:";
		String finalResult = "";
		String resultForTracker = "";

		try
		{	
			String trackerEndTime = GetCurrentDBTime();

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DB END Time For Tracker Calculation: " +trackerEndTime);
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Tracker Validation Started At: "+GenericMethodsLib.DateTimeStamp("yyMMdd_hhmmss"));

			/** Getting query to find out the number of channel and campaign tracker
			 */

			String sqlQueryForChannelTracker = "";
			String sqlQueryForCampaignTracker = "";

			/** Build query for ad serving on webservice
			 */
			if(configFlag.equalsIgnoreCase("webservice"))
			{
				sqlQueryForChannelTracker = sqlQueryWithUniqueParamForChannelTracker(trackerStartTime, uniqueRequestParam);
				sqlQueryForCampaignTracker = sqlQueryWithUniqueParamForCampaignTracker(channelID, trackerStartTime, uniqueRequestParam);
			}

			/** Build query for ad serving other than webservice
			 */
			else
			{
				sqlQueryForChannelTracker = 	sqlQueryWithOutUniqueParamForChannelTracker(channelID, trackerStartTime, trackerEndTime);
				sqlQueryForCampaignTracker = sqlQueryWithOutUniqueParamForCampaignTracker(channelID, campaignID, trackerStartTime, trackerEndTime);
			}

			/** Getting Channel Trackers In 2 D Array. 
			 */
			Connection serveConnectionForChannel = CreateServeSQLConnection();
			String [][] validationDataForChannelTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForChannel, sqlQueryForChannelTracker);
			serveConnectionForChannel.close();

			/** First Check - ai tracker */
			int aiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ai");
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ai : " +aiTrackerCount);

			finalResult =  finalResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));

			int uiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ui");	
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ui : " +uiTrackerCount);
			finalResult =  finalResult.concat(":").concat("ui").concat(":").concat(String.valueOf(uiTrackerCount));

			/** Getting Campaign Trackers In 2 D Array. */ 
			Connection serveConnectionForCampaign = CreateServeSQLConnection();
			String [][] validationDataForCampaignTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForCampaign, sqlQueryForCampaignTracker);
			serveConnectionForCampaign.close();

			int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : vi tracker for " + adFormat + ": " +viTrackerCount);


			/**
			 * Constructor hudsonFlag = hudson, will instruct not check si tracker, else check it.
			 * rtb_bp and rtb_win trackers are required to check in case of hudson requests. 
			 */
			int siTrackerCount = 0;
			int rtbbpTrackerCount = 0;
			int rtbwinTrackerCount = 0;
			if(hudsonFlag.equalsIgnoreCase("hudson"))
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : si tracker will not be checked for hudson requests, checking rtb_win, rtb_bp trackers... ");

				rtbbpTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "rtb_bp");
				finalResult = finalResult.concat(":").concat("rtb_bp:").concat(String.valueOf(rtbbpTrackerCount));

				rtbwinTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "rtb_win");
				finalResult = finalResult.concat(":").concat("rtb_win:").concat(String.valueOf(rtbwinTrackerCount));
			}
			else
			{
				siTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "si");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : si tracker for " + adFormat + ": " +siTrackerCount);
				finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));
			}


			if (adFormat.equalsIgnoreCase("html") || adFormat.equalsIgnoreCase("banner") || adFormat.equalsIgnoreCase("jsbanner") )
			{
				resultForTracker = MobileAdsTrackerforBanner(adFormat, validationDataForCampaignTracker, finalResult);
			}
			else if(adFormat.equalsIgnoreCase("appinterstitial") || adFormat.equalsIgnoreCase("htmlinter"))
			{
				resultForTracker = MobileAdsTrackerforInterstitial(adFormat,validationDataForCampaignTracker,finalResult);
			}
			else if(adFormat.equalsIgnoreCase("video") || adFormat.equalsIgnoreCase("vastfeed") 
					|| adFormat.equalsIgnoreCase("inview") || adFormat.equalsIgnoreCase("leadervdo") || adFormat.equalsIgnoreCase("vdobanner"))
			{
				/** flagForCloseButtonTestForVideo - is not used in the below method any more. */
				resultForTracker = MobileAdsTrackerforVideo(adFormat,validationDataForCampaignTracker,finalResult);	
			}
			else
			{
				resultForTracker = "SKIP: THIS AD FORMAT IS NOT CODED YET, PLEASE INFORM QA AUTOMATION TEAM.";
			}

			if(configFlag.equalsIgnoreCase("webservice"))
			{
				/** Server side trackers validation excluding for Jaadoo - Ad Serving */
				if(uiTrackerCount>0 || siTrackerCount<1 || siTrackerCount >1)
				{
					if(!(resultForTracker.startsWith("FAIL:")))
					{
						/** in case campaign side tracker result is pass and server side tracker is fail then replace PASS in campaign side results.*/
						resultForTracker = resultForTracker.replace("PASS:", failResult);
					}
				}
			}
			else
			{
				/** Server side trackers validation for non Jaadoo - Ad Serving 
				 * 
				 * In case of hudson requests, do not validate si trackers rather validate rtb_bp and rtbwin trackers.
				 * in case of hudson, count of ai = number of rtb vdopia bidders picked up for bidding
				 * and count of rtb_bp = number of vdopia rtb bidders responded with correct response
				 * 
				 *  removing condition on ui tracker in case of hudson
				 */
				if(hudsonFlag.equalsIgnoreCase("hudson"))
				{
					boolean checkFlag1 = false;
					boolean checkFlag2 = false;
					boolean checkFlag3 = false;

					if(aiTrackerCount!=expectedaiTracker_Hudson)
					{
						checkFlag1 = true;
						resultForTracker = resultForTracker+"\nExpected ai="+expectedaiTracker_Hudson + ", Actual ai="+aiTrackerCount;
					}
					if(rtbbpTrackerCount!=expectedrtbbpTracker_Hudson)
					{
						checkFlag2 = true;
						resultForTracker = resultForTracker+"\nExpected rtb_bp="+expectedrtbbpTracker_Hudson + ", Actual rtb_bp="+rtbbpTrackerCount;
					}
					if(rtbwinTrackerCount!=1)
					{
						checkFlag3 = true;
					}

					if(checkFlag1 || checkFlag2 || checkFlag3)
					{
						if(!(resultForTracker.startsWith("FAIL:")))
						{
							/** in case campaign side tracker result is pass and server side tracker result is fail 
							 * then replace PASS in final results with FAIL.
							 */
							resultForTracker = resultForTracker.replace("PASS:", failResult);
						}
					}
				}
				else
				{
					/**
					 * Normal ad serving - non jaadoo serving
					 */
					if(aiTrackerCount<1 || aiTrackerCount>1 || uiTrackerCount>0 || siTrackerCount<1 || siTrackerCount >1)
					{
						if(!(resultForTracker.startsWith("FAIL:")))
						{
							/** in case campaign side tracker result is pass and server side tracker result is fail 
							 * then replace PASS in final results with FAIL.
							 */
							resultForTracker = resultForTracker.replace("PASS:", failResult);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : MobileAdsTrackerValidationForUIOperations: ",e);
		}
		finally
		{
			return resultForTracker;
		}
	}


	//******************** Mobile Tracker Validation Upon Browsing The Test Page from portal *************************************************//
	public static String MobileAds_VdopiaTrackerValidationForCampaignTestPage(String adFormat, String campaignID, String channelID, String trackerStartTime) throws SQLException, ClassNotFoundException
	{
		String passResult = "PASS:";
		String failResult = "FAIL:";
		String finalResult = "";

		try
		{	
			String trackerEndTime = GetCurrentDBTime();

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Start Time For Tracker Calculation: " +trackerStartTime);
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : End Time For Tracker Calculation: " +trackerEndTime);

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Tracker Validation Started At: "+GenericMethodsLib.DateTimeStamp("yyMMdd_hhmmss"));

			String sqlQueryForCampaignTracker = " SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
					" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND (tstamp > '" + trackerStartTime + "' AND tstamp < '"+ trackerEndTime + "') " +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker  FROM ad_log1 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND (tstamp > '" + trackerStartTime + "' AND tstamp < '" + trackerEndTime + "') " +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Campaign Tracker Validation: " +sqlQueryForCampaignTracker);

			if (adFormat.equalsIgnoreCase("html") || adFormat.equalsIgnoreCase("banner") || adFormat.equalsIgnoreCase("appinterstitial")  
					|| adFormat.equalsIgnoreCase("jsbanner") || adFormat.equalsIgnoreCase("htmlinter"))
			{

				//Getting Campaign Trackers In 2 D Array. 
				Connection serveConnectionForCampaign = CreateServeSQLConnection();
				String [][] validationDataForCampaignTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForCampaign, sqlQueryForCampaignTracker);
				serveConnectionForCampaign.close();

				int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : vi tracker for banners: " +viTrackerCount);

				if((viTrackerCount<1 || viTrackerCount >1))
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Failing after checking vi Tracker for : " +adFormat +" :: " +finalResult);
				}
				else
				{
					finalResult = passResult.concat(adFormat).concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Passing after checking vi Tracker for : " +adFormat +" :: " +finalResult);
				}	
			}

			else if(adFormat.equalsIgnoreCase("video") || adFormat.equalsIgnoreCase("leadervdo") || adFormat.equalsIgnoreCase("vdobanner"))
			{
				//Getting Campaign Trackers In 2 D Array. 
				Connection serveConnectionForCampaign = CreateServeSQLConnection();
				String [][] validationDataForCampaignTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForCampaign, sqlQueryForCampaignTracker);
				serveConnectionForCampaign.close();

				int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
				int vi_0TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_0");
				int vi_25TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_25");
				int vi_50TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_50");
				int vi_75TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_75");
				int aeTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "ae");

				if((viTrackerCount<1 || viTrackerCount >1) || (vi_0TrackerCount<1 || vi_0TrackerCount >1) 
						|| (vi_25TrackerCount<1 || vi_25TrackerCount >1) || (vi_50TrackerCount<1 || vi_50TrackerCount >1) 
						|| (vi_75TrackerCount<1 || vi_75TrackerCount >1) || (aeTrackerCount<1 || aeTrackerCount >1))
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
					finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));	
				}
				else
				{
					finalResult = passResult.concat(adFormat).concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
					finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
					finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
					finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));
				}	
			}			
			else
			{
				finalResult = finalResult.concat(failResult).concat(adFormat).concat(" - THIS AD FORMAT IS NOT CODED YET, PLEASE INFORM AUTOMATION TEAM");
			}
		}catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : MobileAdsTrackerValidationForCampaignTestPage: ", e);
		}

		return finalResult;
	}



	//******************** This Method will return Tracker Count and will be used by TrackerValidation *************************************************//
	public static int TrackerCalculation(String [][]validationData, String trackerName)
	{		
		int trackerCount=0;

		try
		{
			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - TrackerCalculation: Print arraylength: row: " +validationData.length + " column: " + validationData[0].length);

			// Un Comment This TO - Find The Data In 2 D Array.
			/*
			for (int i = 0; i<validationData.length; i++)
			{
				for(int j= 0;j<validationData[0].length;j++)
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Debug: Coulmn:  "+ j +": Checking The Data In 2 D Array Being Used By Method - TrackerCalculation : " +validationData[i][j]);
					System.out.print("   :   ");
				}
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Row:  " +i);
			}

			 */
			for (int i=0;i<validationData.length;i++)
			{	
				if(validationData[i][2].equalsIgnoreCase(trackerName))
				{
					trackerCount = Integer.parseInt(validationData[i][3].toString());
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - TrackerCalculation: Tracker Count for Tracker : " +trackerName + " = " + validationData[i][2] + "  is: " + trackerCount);
					break;
				}
			}
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - TrackerCalculation: Returning - Tracker Count for Tracker: " +trackerName + "  is: " + trackerCount);

		} 
		catch(ArrayIndexOutOfBoundsException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Method - TrackerCalculation: Tracker - " +trackerName + " was not fired, no record in array, returing count = " +trackerCount, e);
		}

		return trackerCount;
	}



	public static String MobileAds_VdopiaTargetingTrackerValidation(String adFormat, String campaignID, String channelID, String trackerStartTime, String filterDetail, String result) throws SQLException, ClassNotFoundException
	//public static String TrackerValidation(Connection serveConnection, String adFormat, String campaignID, String trackerStartTime) throws SQLException, ClassNotFoundException
	{

		String passResult;
		String failResult;
		if (Integer.parseInt(filterDetail) == 1)
		{
			passResult = "PASS:";
			failResult = "FAIL:";
		}
		else
		{
			passResult = "FAIL:";
			failResult = "PASS:";
		}
		String finalResult = "";

		try
		{	
			String trackerEndTime = GetCurrentDBTime();

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DB END Time For Tracker Calculation: " +trackerEndTime);
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Tracker Validation Started At: "+GenericMethodsLib.DateTimeStamp("yyMMdd_hhmmss"));

			String sqlQueryForChannelTracker = 	" SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
					" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
					" WHERE channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '"+ trackerEndTime + "') " +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log1 " +
					" WHERE channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '" + trackerEndTime + "') " +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Channel Tracker Validation: " +sqlQueryForChannelTracker);

			String sqlQueryForCampaignTracker = " SELECT IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID, Channel_ID AS Channel_ID, measure AS Tracker_Name, SUM(NumberOfTracker) AS Tracker_Count" +
					" FROM " +" (SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker FROM ad_log0 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '"+ trackerEndTime + "') " +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT campaign_id, channel_id, measure, COUNT(measure) AS NumberOfTracker  FROM ad_log1 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND (tstamp >= '" + trackerStartTime + "' AND tstamp <= '" + trackerEndTime + "') " +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print SQL For Campaign Tracker Validation: " +sqlQueryForCampaignTracker);

			//Getting Channel Trackers In 2 D Array. 
			Connection serveConnectionForChannel = CreateServeSQLConnection();
			String [][] validationDataForChannelTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForChannel, sqlQueryForChannelTracker);
			serveConnectionForChannel.close();

			if (adFormat.equalsIgnoreCase("html") || adFormat.equalsIgnoreCase("banner") || adFormat.equalsIgnoreCase("appinterstitial")  
					|| adFormat.equalsIgnoreCase("jsbanner") || adFormat.equalsIgnoreCase("htmlinter"))
			{
				//First Check - ai tracker
				int aiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ai");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ai : " +aiTrackerCount);

				if (aiTrackerCount<1)
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("ai:").concat(String.valueOf(aiTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ai Tracker wasn't fired other trackers will not be checked: " +adFormat +" :: " +finalResult);
				}
				else
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Passing at ai Tracker: " +adFormat +" :: " +aiTrackerCount);

					int uiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ui");	
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ui : " +uiTrackerCount);

					//Second check - ui tracker - here not checking the multiple occurrence of ui
					if (uiTrackerCount>0)
					{
						finalResult = failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
						finalResult = finalResult.concat(":").concat("ui:").concat(String.valueOf(uiTrackerCount));
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Inventory Unavailable: ui Tracker: " +adFormat +" :: " +finalResult);
					}

					//Third check - vi / si trackers - here not checking the multiple occurrence of ui
					else
					{
						//Getting Campaign Trackers In 2 D Array. 
						Connection serveConnectionForCampaign = CreateServeSQLConnection();
						String [][] validationDataForCampaignTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForCampaign, sqlQueryForCampaignTracker);
						serveConnectionForCampaign.close();

						int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : vi tracker for banners: " +viTrackerCount);

						int siTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "si");
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : si tracker for banners: " +siTrackerCount);


						if((viTrackerCount<1 || viTrackerCount >1) || (siTrackerCount<1 || siTrackerCount >1))
						{
							finalResult = failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
							finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
							finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));

							logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Failing after checking si and vi Tracker for : " +adFormat +" :: " +finalResult);
						}
						else
						{
							finalResult =  passResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
							finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
							finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));

							logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Passing after checking si and vi Tracker for : " +adFormat +" :: " +finalResult);
						}	
					}
				} 
			}

			else if(adFormat.equalsIgnoreCase("video") || adFormat.equalsIgnoreCase("leadervdo") || adFormat.equalsIgnoreCase("vdobanner"))
			{
				//First Check - ai tracker - here not checking the multiple occurrence of ui
				int aiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ai");
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print Channel Tracker ai : " +aiTrackerCount);

				if (aiTrackerCount<1)
				{
					finalResult = failResult.concat(adFormat).concat(":").concat("ai:").concat(String.valueOf(aiTrackerCount));
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ai Tracker wasn't fired other trackers will not be checked: " +adFormat +" :: " +finalResult);
				}
				else
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Passing at ai Tracker: " +adFormat +" :: " +finalResult);
					int uiTrackerCount = TrackerCalculation(validationDataForChannelTracker, "ui");	

					//Second check - ui tracker - here not checking the multiple occurrence of ui
					if (uiTrackerCount>0)
					{
						finalResult =  failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
						finalResult = finalResult.concat(":").concat("ui:").concat(String.valueOf(uiTrackerCount));
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Inventory Unavailable: ui Tracker: " +adFormat +" :: " +finalResult);
					}

					//Third check - vi, si, vi_0, vi_25, vi_50, vi_75, ae trackers
					else
					{
						//Getting Campaign Trackers In 2 D Array. 
						Connection serveConnectionForCampaign = CreateServeSQLConnection();
						String [][] validationDataForCampaignTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForCampaign, sqlQueryForCampaignTracker);
						serveConnectionForCampaign.close();

						int siTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "si");
						int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
						int vi_0TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_0");
						int vi_25TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_25");
						int vi_50TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_50");
						int vi_75TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_75");
						int aeTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "ae");

						if((viTrackerCount<1 || viTrackerCount >1) || (vi_0TrackerCount<1 || vi_0TrackerCount >1) 
								|| (vi_25TrackerCount<1 || vi_25TrackerCount >1) || (vi_50TrackerCount<1 || vi_50TrackerCount >1) 
								|| (vi_75TrackerCount<1 || vi_75TrackerCount >1) || (aeTrackerCount<1 || aeTrackerCount >1)
								|| (siTrackerCount<1 || siTrackerCount >1) )
						{
							finalResult =  failResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
							finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));
							finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
							finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
							finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
							finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
							finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
							finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));	
						}
						else
						{
							finalResult =  passResult.concat(adFormat).concat(":").concat("ai").concat(":").concat(String.valueOf(aiTrackerCount));
							finalResult = finalResult.concat(":").concat("si:").concat(String.valueOf(siTrackerCount));
							finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
							finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
							finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
							finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
							finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
							finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));
						}	
					}
				}
			}			
			else
			{
				finalResult = finalResult.concat(failResult).concat(adFormat).concat(" - NOT YET DETERMINED");
			}
		}catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : TrackerValidation:", e);
		}

		return finalResult;
	}



	/** Third party click trackers are not applicable to adformats: appinterstitial, htmlinter and jsbanner, therefore these will be excluded
	 * in validation part.
	 * 
	 * @param channelID
	 * @param trackerStartTime
	 * @param expectedImpressionTracker
	 * @param expectedClickTracker
	 * @return
	 */

	@SuppressWarnings("finally")
	public static String MobileAds_ThirdPartyTrackerValidation(String channelID, String trackerStartTime, String expectedImpressionTracker, String expectedClickTracker)
	{
		String finalResult = "";
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Third Party Tracker Validation Started At Time: "+ GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss"));

		try
		{	
			if(	(expectedImpressionTracker.equalsIgnoreCase("") || expectedImpressionTracker.equalsIgnoreCase("0")	)
					&&
					(	expectedClickTracker.equalsIgnoreCase("") || expectedClickTracker.equalsIgnoreCase("0")	)	
					)
			{
				//finalResult = "There is no third party tracker associated with this campaign.";
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There is no third party tracker associated with this campaign.");
			}
			else
			{
				//Remove [timestamp] param from tracker url, as its a dynamic field.
				expectedImpressionTracker = expectedImpressionTracker.replace("[timestamp]", "").trim();
				expectedClickTracker = expectedClickTracker.replace("[timestamp]", "").trim();

				String trackerEndTime = GetCurrentDBTime();
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Time End For Third Party Tracker Calculation: " +trackerEndTime);

				String sqlQueryForUniqID = " SELECT uniq_id FROM ad_log0 WHERE channel_id = '"+ channelID +"' AND measure = 'ai' AND " +
						" (tstamp >= '"+ trackerStartTime +"' AND tstamp <= '"+ trackerEndTime +"') " +
						" UNION ALL " +
						" SELECT uniq_id FROM ad_log1 WHERE channel_id = '"+ channelID +"' AND measure = 'ai' AND " +
						" (tstamp >= '"+ trackerStartTime +"' AND tstamp <= '"+ trackerEndTime +"') ";

				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : SQL Query To Get Uniq ID For Third Party Tracker Calculation: " +sqlQueryForUniqID);

				//Get Uniq ID
				String [][] uniqID = null;
				try
				{
					//Getting DATA In 2 D Array. 
					Connection serveConnectionForChannel = CreateServeSQLConnection();
					uniqID = ExecuteMySQLQueryReturnsArray(serveConnectionForChannel, sqlQueryForUniqID);
					serveConnectionForChannel.close();

				}catch(Exception e)
				{
					uniqID = null;
				}

				if(uniqID.length > 0)
				{
					//get third party impression tracker data
					String thirdpartyImpressionTracker[][] = getThirdPartyTrackerData(trackerStartTime, trackerEndTime, uniqID[0][0], "ttu");

					//get third party click tracker data
					String thirdpartyClickTracker[][] = getThirdPartyTrackerData(trackerStartTime, trackerEndTime, uniqID[0][0], "rdu");


					//********* ADD CODE TO VALIDATE CLICK AND IMPRESSION TRACKER **************** 
					if(	expectedImpressionTracker.equalsIgnoreCase("") || expectedImpressionTracker.equalsIgnoreCase("0"))
					{
						finalResult = "There is no third party impression tracker associated with this campaign.";
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There is no third party impression tracker associated with this campaign.");
					}
					else
					{
						finalResult = finalResult + getThirdPartyTrackerResults(expectedImpressionTracker, thirdpartyImpressionTracker, "Impression");
					}

					if((expectedClickTracker.equalsIgnoreCase("") || expectedClickTracker.equalsIgnoreCase("0")))
					{
						finalResult = "There is no destination url associated with this campaign.";
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There is no destination url associated with this campaign.");
					}
					else
					{
						/** for ad format: jsbanner, htmlinter and appinterstitial, click tracker will not fired. 
						 */
						if(adFormat.equalsIgnoreCase("jsbanner") || adFormat.equalsIgnoreCase("htmlinter") || adFormat.equalsIgnoreCase("appinterstitial"))
						{
							finalResult = finalResult + "Click tracker is not fired for ad format: "+adFormat +" therefore wasn't checked.";
						}
						else if(adFormat.equalsIgnoreCase("vdobanner") || adFormat.equalsIgnoreCase("leadervdo"))
						{
							finalResult = finalResult + "Automation doesn't perform click on "+ adFormat +" therefore click tracker wasn't checked.";
						}
						else
						{
							finalResult = finalResult + getThirdPartyTrackerResults(expectedClickTracker, thirdpartyClickTracker, "Click");
						}
					}
					//****************************************************************************

					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Third Party Tracker Result:  "+finalResult);
				}
				else
				{
					finalResult = "SKIP: Couldn't Find Uniq ID, Hence Skipping Third Party Tracker Validation For This Iteration. ";
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Couldn't Find Uniq ID, Hence Skipping Third Party Tracker Validation For This Iteration. ");
				}
			}
		}
		catch(Exception e)
		{
			finalResult = "FAIL: Exception occurred while checking Third Party Tracker for this iteration. ";
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception Handled by Method : MobileAds_ThirdPartyTrackerValidation: ",e);
		}
		finally
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Third Party Tracker Validation Ended At Time: "+ GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss"));

			if(!finalResult.isEmpty())
			{
				finalResult = "Third Party Trackers: \n" + finalResult;
				logger.debug(finalResult);
			}
			return finalResult;
		}
	}



	//This method will return the final result out comaring the expected and actual value of third party tracker
	@SuppressWarnings("finally")
	public static String getThirdPartyTrackerResults(String expectedThirdPartyTracker, String thirdpartyTrackerData[][], String clickImpressionFlag)
	{
		String finalResult = "";
		Object flag = false;

		try
		{
			//Splitting the Tracker URL as multiple urls are saved separated by space
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Expected Third Party URLs For This Iterations are: " +expectedThirdPartyTracker);
			List<String> trackerURLs = Arrays.asList(expectedThirdPartyTracker.trim().split(" "));

			//Checking if impression tracker urls are saved in db
			for(int i=0 ;i<trackerURLs.size(); i++)
			{
				String expectedTracker = trackerURLs.get(i).trim().toString();

				//Calling method based on what kind of url is being sent
				if(clickImpressionFlag.equalsIgnoreCase("impression"))
				{
					flag = VerifyThirdPartyImpressionTracker(thirdpartyTrackerData, expectedTracker);
				}
				else if(clickImpressionFlag.equalsIgnoreCase("click"))
				{
					flag = VerifyDestinationURL(thirdpartyTrackerData, expectedTracker);
				}

				boolean checkFlag = false;
				try{
					checkFlag = Boolean.parseBoolean((String) flag);
				}catch(Exception e){
				}

				if(checkFlag)
				{
					finalResult = finalResult + clickImpressionFlag + " Tracker: " + expectedTracker + " was fired. " + "\n";
				}
				else
				{
					finalResult = finalResult + clickImpressionFlag + " Tracker: " + expectedTracker + " was not fired. " + "\n" + flag;
				}	
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting results of third party trackers. ", e);
		}
		finally
		{
			if(finalResult.contains("not"))
			{
				finalResult = "FAIL: "  + finalResult;
			}
			else
			{
				finalResult = "PASS: "  + finalResult;
			}
			return finalResult;
		}
	}


	//This method will form query to get third party tracker
	@SuppressWarnings("finally")
	public static String[][] getThirdPartyTrackerData(String trackerStartTime, String trackerEndTime, String uniq_id, String eventType)
	{
		//uniqID[0][0]
		String thirdpartyTracker[][] = null;
		try
		{
			String sqlQueryForThirdPartyTracker = 	" SELECT IFNULL(event_info,0), IFNULL(ref_url, 0) FROM event_log0 WHERE event_type = '"+ eventType +"' AND uniq_id = '"+ uniq_id +"' AND " + 
					" (tstamp >= '"+ trackerStartTime +"' AND tstamp <= '"+ trackerEndTime +"') " +
					" UNION ALL " +
					" SELECT IFNULL(event_info,0), IFNULL(ref_url, 0) FROM event_log1 WHERE event_type = '"+ eventType +"' AND uniq_id = '"+ uniq_id +"' AND " + 
					" (tstamp >= '"+ trackerStartTime +"' AND tstamp <= '"+ trackerEndTime +"') ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : SQL Query For Third Party Tracker Validation: " +sqlQueryForThirdPartyTracker);

			//Getting Tracker Data in 2 D Array
			Connection serveConnectionForChannel = CreateServeSQLConnection();
			thirdpartyTracker = ExecuteMySQLQueryReturnsArray(serveConnectionForChannel, sqlQueryForThirdPartyTracker);
			serveConnectionForChannel.close();
		}
		catch(Exception e)
		{
			thirdpartyTracker = null;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting data from event log tables for third party trackers.", e);
		}
		finally
		{
			return thirdpartyTracker;
		}
	}


	//******************** This method will check the third party impression url in eventlog table ****************************************//
	@SuppressWarnings("finally")
	public static boolean VerifyThirdPartyImpressionTracker(String [][]trackerData, String expectedImpressionTracker)
	{
		boolean flag = false;
		try
		{
			for(int j=0; j<trackerData.length; j++)
			{
				String thirdPartyData  = trackerData[j][0].replace("\\", "");

				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Received Third Party Data From DB: " +thirdPartyData);

				List<String> list = Arrays.asList(thirdPartyData.split(","));

				for(int i=0; i<list.size(); i++)
				{
					if(list.get(i).contains("url") && list.get(i).contains(expectedImpressionTracker))
					{
						flag = true;
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Third Party Impression Tracker: "+expectedImpressionTracker + " is found in database. ");
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: VerifyThirdPartyTracker while checking third party trackers. " , e);
		}
		finally
		{
			return flag;
		}	

	}


	//******************** This method will check the third party click url in eventlog table ****************************************//
	@SuppressWarnings("finally")
	public static Object VerifyDestinationURL(String [][]trackerData, String expectedClickTracker)
	{
		Object flag = false;
		try
		{
			/** Proceed only if any data is received from bq */
			if(trackerData != null)
			{
				for(int j=0; j<trackerData.length; j++)
				{
					String thirdPartyData  = trackerData[j][0].replace("\\", "");

					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Received Third Party Data From DB: " +thirdPartyData);

					List<String> list = Arrays.asList(thirdPartyData.split(","));

					for(int i=0; i<list.size(); i++)
					{
						String data = list.get(i);

						if(data.contains("redirectUrl"))
						{
							/** Removing http://www from the expected click tracker for better comparison
							 */
							expectedClickTracker = expectedClickTracker.replace("http://", "").replace("www", "").trim();
							data = data.replace("http://", "").replace("www", "").trim();

							if(data.contains(expectedClickTracker))
							{
								flag = true;
								logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Third Party Click Tracker: "+expectedClickTracker + " is found in database. ");
								break;
							}
						}
					}
				}
			}
			else
			{
				flag = "NO DATA RECIEVED FROM BQ TO VALIDATE THIRD PARTY CLICK TRACKER. ";
			}
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: VerifyThirdPartyTracker while checking third party trackers. ", e);
		}
		finally
		{
			return flag;
		}	

	}


	//******************** This method will check the appearance of ae tracker *************************************************//
	public static boolean WaitForAdEndTracker(String campaignID, String channelID, String trackerStartTime, int testDuration) throws SQLException, ClassNotFoundException, InterruptedException, InvocationTargetException
	{
		GenericMethodsLib.InitializeConfiguration();
		boolean flag = false;

		//Wait for UI tracker for 5 sec, if found then don't wait anymore and if not, then wait for ae.
		if(WaitForUiTracker(campaignID, channelID, trackerStartTime, 5))
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Ui Tracker is fired, stopping this ad now. ");
		}
		else
		{
			String sqlQueryForAeTracker = " SELECT measure AS Tracker_Name, Campaign_ID  " +
					" FROM " +" (SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log0 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND measure = 'ae' AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log1 " +
					" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
					" AND measure = 'ae' AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure LIMIT 1; ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print query for checking ae tracker: " +sqlQueryForAeTracker);

			for (int i=0; i<testDuration; i++)
			{
				Connection con = GenericMethodsLib.CreateServeSQLConnection();

				String [][] aeTracker = GenericMethodsLib.ExecuteMySQLQueryReturnsArray(con, sqlQueryForAeTracker);
				con.close();

				if(aeTracker.length<1)
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : checking if ae tracker is fired yet, attempt# " +i);
				}
				else
				{
					if(aeTracker[0][0].equalsIgnoreCase("ae"))
					{
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Found tracker: " +aeTracker[0][0] + ", exiting this iteration.");
						flag = true;
						break;
					}
				}
				Thread.sleep(1000);
			}
		}
		return flag;
	}	


	//******************** This method will check the appearance of vi tracker *************************************************//
	public static boolean WaitForViTracker(String campaignID, String channelID, String trackerStartTime, int testDuration) throws SQLException, ClassNotFoundException, InterruptedException, InvocationTargetException
	{
		boolean flag = false;
		GenericMethodsLib.InitializeConfiguration();

		String sqlQueryForviTracker = " SELECT measure AS Tracker_Name, Campaign_ID  " +
				" FROM " +" (SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log0 " +
				" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
				" AND measure = 'vi' AND tstamp >= '" + trackerStartTime + "'" +
				" GROUP BY measure " +
				" UNION ALL " +
				" SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log1 " +
				" WHERE campaign_id = '" + campaignID + "' AND channel_id = '" + channelID + "'" + 
				" AND measure = 'vi' AND tstamp >= '" + trackerStartTime + "'" +
				" GROUP BY measure " +
				") AS tmp GROUP BY measure LIMIT 1; ";

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print query for checking vi tracker: " +sqlQueryForviTracker);

		for (int i=0; i<testDuration; i++)
		{
			Connection con = GenericMethodsLib.CreateServeSQLConnection();
			String [][] viTracker = GenericMethodsLib.ExecuteMySQLQueryReturnsArray(con, sqlQueryForviTracker);
			con.close();

			if(viTracker.length<1)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : checking if vi tracker is fired yet, attempt# " +i);
			}
			else
			{
				if(viTracker[0][0].equalsIgnoreCase("vi"))
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Found tracker: " +viTracker[0][0] + ", exiting this iteration.");
					flag = true;
					break;
				}
			}
			Thread.sleep(1000);
		}
		return flag;
	}	


	//******************** This method will check the appearance of the given campaign tracker *************************************************//
	@SuppressWarnings("finally")
	public static boolean waitForSpecificCampaignTracker(String campaignTracker, String campaignID, String channelID, String trackerStartTime, int testDuration) 
	{
		boolean flag = false;
		try
		{
			/** Adding subquery to put condition on campaignID so that specific campaign tracker can be retrieved 
			 *  even without campaignID and with Only channelID
			 */

			String subQuery = "";

			if(campaignID == null)
			{
				subQuery = "";
			}
			else if(campaignID.trim().isEmpty())
			{
				subQuery = "";
			}
			else
			{
				subQuery = " campaign_id = '" + campaignID + "' AND ";
			}

			String sqlQueryForCampaignTracker = " SELECT measure AS Tracker_Name, Campaign_ID  " +
					" FROM " +" (SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log0 " +
					" WHERE " +
					subQuery +
					" channel_id = '" + channelID + "'" + 
					" AND measure = '"+ campaignTracker +"' AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log1 " +
					" WHERE " +
					subQuery +
					" channel_id = '" + channelID + "'" + 
					" AND measure = '"+ campaignTracker +"'  AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure LIMIT 1; ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Executing query to check: "+campaignTracker+" tracker: " +sqlQueryForCampaignTracker);

			for (int i=0; i<testDuration; i++)
			{
				Connection con = GenericMethodsLib.CreateServeSQLConnection();
				String [][] tracker = GenericMethodsLib.ExecuteMySQLQueryReturnsArray(con, sqlQueryForCampaignTracker);
				con.close();

				if(tracker.length<1)
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : checking if "+campaignTracker +" tracker is fired yet, attempt# " +i);
				}
				else
				{
					if(tracker[0][0].equalsIgnoreCase(campaignTracker))
					{
						logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Found tracker: " +tracker[0][0] + ", exiting this iteration.");
						flag = true;
						break;
					}
				}

				Thread.sleep(1000);
			}
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while waiting for campaign tracker: "+campaignTracker + " of campaign id: "+campaignID + ", channel id: "+channelID, e);
		}
		finally
		{
			return flag;
		}
	}	



	//******************** This method will check the appearance of ui tracker *************************************************//
	public static boolean WaitForUiTracker(String campaignID, String channelID, String trackerStartTime, int testDuration) throws SQLException, ClassNotFoundException, InterruptedException, InvocationTargetException
	{
		boolean flag = false;
		GenericMethodsLib.InitializeConfiguration();

		String sqlQueryForuiTracker = " SELECT measure AS Tracker_Name, Campaign_ID  " +
				" FROM " +" (SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log0 " +
				" WHERE channel_id = '" + channelID + "'" + 
				" AND measure = 'ui' AND tstamp >= '" + trackerStartTime + "'" +
				" GROUP BY measure " +
				" UNION ALL " +
				" SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log1 " +
				" WHERE channel_id = '" + channelID + "'" + 
				" AND measure = 'ui' AND tstamp >= '" + trackerStartTime + "'" +
				" GROUP BY measure " +
				") AS tmp GROUP BY measure LIMIT 1; ";

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Print query for checking ui tracker: " +sqlQueryForuiTracker);

		for (int i=0; i<testDuration; i++)
		{
			Connection con = GenericMethodsLib.CreateServeSQLConnection();
			String [][] uiTracker = GenericMethodsLib.ExecuteMySQLQueryReturnsArray(con, sqlQueryForuiTracker);
			con.close();

			if(uiTracker.length<1)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : checking if ui tracker is fired yet, attempt# " +i);
			}
			else
			{
				if(uiTracker[0][0].equalsIgnoreCase("ui"))
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Found tracker: " +uiTracker[0][0] + ", exiting this iteration.");
					flag = true;
					break;
				}
			}
			Thread.sleep(1000);
		}
		return flag;
	}	



	//******************** This method will check the appearance of ae tracker *************************************************//
	@SuppressWarnings("finally")
	public static boolean WaitForAeTrackerAfterCheckingUiAndVi(String campaignID, String channelID, String trackerStartTime, int testDuration) throws SQLException, ClassNotFoundException, InterruptedException, InvocationTargetException
	{
		boolean campaignflag = false;
		boolean channelFlag = false;
		boolean aeFlag = false;

		try
		{
			GenericMethodsLib.InitializeConfiguration();

			//for first 5 seconds check if either of vi / ui is fired
			for(int i=0; i<5; i++)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Checking if ui or vi tracker is fired yet... attempt# "+i);

				if(GetChannelTracker("ui", channelID, trackerStartTime))
				{	
					channelFlag = true;
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ui Tracker is fired, ae tracker will not be checked now. ");
					break;
				}
				else if(GetCampaignTracker("vi", campaignID, channelID, trackerStartTime))
				{
					campaignflag = true;
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : vi Tracker is fired, ae tracker will be checked now. ");
					break;
				}

				Thread.sleep(1000);	
			}

			//Check ae tracker only if vi is fired.
			if(campaignflag)
			{
				for(int i=0; i<testDuration; i++)
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Waiting for completion of ad... by checking if ae tracker is fired yet... attempt# "+i);

					if(GetCampaignTracker("ae", campaignID, channelID, trackerStartTime))
					{
						aeFlag = true;
						logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ae Tracker is fired. ");
						break;
					}
					Thread.sleep(1000);	
				}
			}
			else
			{
				if(channelFlag)
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ui Tracker is fired, will not wait any more... ");
				}
				else
				{
					//In case neither ui or vi is fired then sleep till duration passed in definition
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Neither of ui and vi tracker is fired... waiting for completion for: "+testDuration + " seconds. ");
					Thread.sleep(testDuration*1000);
				}

			}
		}
		catch(Exception e)
		{
			aeFlag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: WaitForAdEndTracker, while checking ae tracker. ", e);
		}
		finally
		{
			return aeFlag;
		}
	}	



	//******************** This method will execute query to get a channel side tracker *************************************************//
	@SuppressWarnings("finally")
	public static boolean GetChannelTracker(String channelTracker, String channelID, String trackerStartTime) 
	{
		boolean flag = false;

		try
		{
			String sqlQueryForChannelTracker = " SELECT measure AS Tracker_Name, Campaign_ID  " +
					" FROM " +" (SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log0 " +
					" WHERE channel_id = '" + channelID + "'" + 
					" AND measure = '"+ channelTracker +"' AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log1 " +
					" WHERE channel_id = '" + channelID + "'" + 
					" AND measure = '"+ channelTracker +"' AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure LIMIT 1; ";

			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Executing query to check channel tracker: " +sqlQueryForChannelTracker);

			Connection con = GenericMethodsLib.CreateServeSQLConnection();
			String [][] channelTrackerData = GenericMethodsLib.ExecuteMySQLQueryReturnsArray(con, sqlQueryForChannelTracker);
			con.close();

			if(channelTrackerData.length<1)
			{
				flag = false;
				logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Channel Tracker: "+ channelTracker +" is not fired yet." );
			}
			else
			{
				if(channelTrackerData[0][0].equalsIgnoreCase(channelTracker))
				{
					flag = true;
					logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Found Channel Tracker: " +channelTrackerData[0][0] );
				}
			}
		}
		catch (Exception e) 
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while checking channel side tracker: "+channelTracker, e);
		}
		finally
		{
			return flag;
		}
	}	



	//******************** This method will execute query to get a campaign side tracker *************************************************//
	@SuppressWarnings("finally")
	public static boolean GetCampaignTracker(String campaignTracker, String campaignID, String channelID, String trackerStartTime) 
	{
		boolean flag = false;

		try
		{			
			/** Adding subquery to put condition on campaignID so that specific campaign tracker can be retrieved 
			 *  even without campaignID and with Only channelID
			 */

			String subQuery = "";

			if(campaignID == null)
			{
				subQuery = "";
			}
			else if(campaignID.trim().isEmpty())
			{
				subQuery = "";
			}
			else
			{
				subQuery = " campaign_id = '" + campaignID + "' AND ";
			}

			String sqlQueryForCampaignTracker = " SELECT measure AS Tracker_Name, Campaign_ID  " +
					" FROM " +" (SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log0 " +
					" WHERE " +
					subQuery +
					" channel_id = '" + channelID + "'" + 
					" AND measure = '" + campaignTracker + "' AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					" UNION ALL " +
					" SELECT measure, IFNULL(campaign_id, 'NoCampaign') AS Campaign_ID FROM ad_log1 " +
					" WHERE " +
					subQuery +
					" channel_id = '" + channelID + "'" + 
					" AND measure = '"+ campaignTracker +"' AND tstamp >= '" + trackerStartTime + "'" +
					" GROUP BY measure " +
					") AS tmp GROUP BY measure LIMIT 1; ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Checking campaign side tracker by executing query: " +sqlQueryForCampaignTracker);

			Connection con = GenericMethodsLib.CreateServeSQLConnection();
			String [][] CampaignTrackerData = GenericMethodsLib.ExecuteMySQLQueryReturnsArray(con, sqlQueryForCampaignTracker);
			con.close();

			if(CampaignTrackerData.length<1)
			{
				flag = false;
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Campaign Tracker: "+campaignTracker +" is not fired yet. ");
			}
			else
			{
				if(CampaignTrackerData[0][0].equalsIgnoreCase(campaignTracker))
				{
					flag = true;
					logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Found Campaign Tracker: " +CampaignTrackerData[0][0] );
				}
			}
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while checking campaign side tracker: "+campaignTracker, e);
		}
		finally
		{
			return flag;			
		}
	}



	//******************** This method will check the tracker for Banner ad format *************************************************//
	public static String MobileAdsTrackerforBanner(String adFormat, String[][] validationDataForCampaignTracker, String finalResult)
	{
		String passResult = "PASS:";
		String failResult = "FAIL:";

		int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
		int clTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "cl");

		/** Validate trackers and display results for mobile app Jaadoo using webservice, on app we are not checking any tracker fired on UI Operations except sk.
		 * as user can mute or unmute, click etc.. any number of times... 
		 */
		if(configFlag.equalsIgnoreCase("webservice"))
		{			
			finalResult = getBannerResultForJaadoo(viTrackerCount, clTrackerCount, failResult, passResult, finalResult);
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : App Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);
		}

		/** Validate tracker on desktop or mobile browser */
		else
		{
			/** cl tracker is not fired for jsbanner and htmlinterstitial, handling: 
			 * collecting result clTrackerFlag of condition = clTrackerCount<1 || clTrackerCount >1
			 * in case of js banner, considering clTrackerFlag = false, because when this flag is used with vi tracker result, only vi result 
			 * is the final result and clTrackerFlag has no impact on validation;
			 * 
			 * in case of connected device click is not being perfomed hence ommiting click tracker validation 
			 */
			boolean clTrackerFlag = clTrackerCount<1 || clTrackerCount >1;

			if(adFormat.equalsIgnoreCase("jsbanner") || isDeviceConnected)
			{
				clTrackerFlag = false;
			}

			if((viTrackerCount<1 || viTrackerCount >1) || clTrackerFlag)
			{
				finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));

				/** In case of js banner and connected device, cl tracker is not supported, hence not collecting result. */ 
				if(!adFormat.equalsIgnoreCase("jsbanner") || isDeviceConnected)
				{
					finalResult = finalResult.concat(":").concat("cl:").concat(String.valueOf(clTrackerCount));
				}

				/** Collecting failed result */
				finalResult = failResult + finalResult;
			}
			else
			{
				finalResult =  finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));

				/** In case of js banner, cl tracker is not supported, hence not collecting result. */
				if(!adFormat.equalsIgnoreCase("jsbanner") || isDeviceConnected)
				{
					finalResult = finalResult.concat(":").concat("cl:").concat(String.valueOf(clTrackerCount));
				}

				/** Collecting passed result */
				finalResult = passResult + finalResult;
			}	

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Non App Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);
		}

		return finalResult;
	}


	public static String getBannerResultForJaadoo(int viTrackerCount, int clTrackerCount, String failResult, String passResult, String finalResult)
	{
		if(viTrackerCount<1 || viTrackerCount >1)
		{
			finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
			finalResult = failResult + finalResult;
		}
		else
		{
			finalResult =  finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
			finalResult = passResult + finalResult;
		}

		//Display click tracker separately in case of webservice
		finalResult = finalResult.concat("\nTrackers By UI Operations").concat(":\n");
		finalResult = finalResult.concat("cl:").concat(String.valueOf(clTrackerCount));

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : App Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);

		return finalResult;
	}


	//******************** This method will check the tracker for Interstitial ad format *************************************************//
	public static String MobileAdsTrackerforInterstitial(String adFormat,String[][] validationDataForCampaignTracker,String finalResult)
	{
		String passResult = "PASS:";
		String failResult = "FAIL:";

		int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");

		finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));

		//Validate trackers and display results for mobile app Jaadoo using webservice, on app we are not checking any tracker fired on UI Operations except sk.
		//as user can mute or unmute, click etc.. any number of times... 
		if(configFlag.equalsIgnoreCase("webservice"))
		{
			if(viTrackerCount<1 || viTrackerCount >1) 		//(|| (skTrackerCount<1 || skTrackerCount >1)) // in case of interstitial on app, close X will be user's choice
			{
				finalResult = failResult + finalResult;
			}
			else
			{	
				finalResult = passResult + finalResult;
			}

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : App Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);
		}

		//Checking trackers on desktop or mobile browsers
		else
		{
			if((viTrackerCount<1 || viTrackerCount >1))
			{
				finalResult = failResult + finalResult;
			}
			else
			{
				finalResult = passResult + finalResult;
			}	

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Non App Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);
		}
		return finalResult;
	}



	//******************** This method will check the tracker for Video ad format *************************************************//
	public static String MobileAdsTrackerforVideo(String adFormat, String[][] validationDataForCampaignTracker, String finalResult)
	{
		String passResult = "PASS:";
		String failResult = "FAIL:";

		int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
		int vi_0TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_0");
		int vi_25TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_25");
		int vi_50TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_50");
		int vi_75TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_75");
		int aeTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "ae");

		//Getting Trackers fired coz of UI Operations
		int clTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "cl");
		int paTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "pa");
		int muTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "mu");
		int umTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "um");
		int skTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "sk");

		//Getting all trackers in a result string
		finalResult = finalResult.concat("\n").concat("vi:").concat(String.valueOf(viTrackerCount));
		finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
		finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
		finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
		finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
		finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));

		finalResult = finalResult.concat("\nTrackers By UI Operations").concat(":\n");

		//Display click and pause trackers in case of webservice
		if(configFlag.equalsIgnoreCase("webservice"))
		{
			finalResult = finalResult.concat("cl:").concat(String.valueOf(clTrackerCount));
			finalResult = finalResult.concat(":").concat("pa:").concat(String.valueOf(paTrackerCount)).concat(":");
			finalResult = finalResult.concat("mu:").concat(String.valueOf(muTrackerCount));
			finalResult = finalResult.concat(":").concat("um:").concat(String.valueOf(umTrackerCount));
		}

		if(!(aeTrackerCount == 1))
		{
			finalResult = finalResult.concat(":").concat("sk:").concat(String.valueOf(skTrackerCount));
		}


		//Validate trackers and display results for mobile app Jaadoo using webservice, 
		//on app we are not checking any tracker fired on UI Operations except sk.
		//as user can mute or unmute any number of times... 
		if(configFlag.equalsIgnoreCase("webservice"))
		{
			//Checking all vdopia quartile trackers
			if(	(viTrackerCount<1 || viTrackerCount >1) || (vi_0TrackerCount<1 || vi_0TrackerCount >1) 
					|| (vi_25TrackerCount<1 || vi_25TrackerCount >1) || (vi_50TrackerCount<1 || vi_50TrackerCount >1) 
					|| (vi_75TrackerCount<1 || vi_75TrackerCount >1) )  
			{
				//Both ae and sk can't be fired at the same time, adding custom text in final result
				if(aeTrackerCount > 0 && skTrackerCount > 0)
				{
					finalResult = finalResult + "\n sk and ae can't be fired at the same time.";
				}

				//Adding fail flag to final result
				finalResult = failResult + finalResult;
			}
			else
			{
				//Check ae / sk, if either of sk or ae is fired, then count of fired tracker should be 1
				if(aeTrackerCount > 0 && skTrackerCount > 0)
				{
					finalResult = failResult + finalResult + "\n sk and ae can't be fired at the same time.";
				}
				//if ae is fired then sk should be zero vice versa...
				else if(skTrackerCount < 1 && aeTrackerCount !=1)
				{
					finalResult = failResult + finalResult;
				}
				else if(aeTrackerCount < 1 && skTrackerCount != 1)
				{
					finalResult = failResult + finalResult;
				}
				else
				{
					finalResult = passResult + finalResult;
				}
			}

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : App Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);
		}

		//Validate trackers and display results for mobile device or desktop browser without using webservice
		else
		{
			boolean muteunmutevalidate;
			boolean clTrackerValidate = false;
			boolean inviewTrackers = false;
			boolean pauseTracker = false;

			// For connected device, we are validating only mute and unmute
			if(isDeviceConnected)
			{
				muteunmutevalidate = (muTrackerCount<1 || muTrackerCount>1) || (umTrackerCount<1 || umTrackerCount>1);
				finalResult = finalResult.concat("mu:").concat(String.valueOf(muTrackerCount));
				finalResult = finalResult.concat(":").concat("um:").concat(String.valueOf(umTrackerCount));
				clTrackerValidate = false;
				pauseTracker = false;
			}
			else
			{
				//on browser, not checking mute, unmute
				muteunmutevalidate = false;

				// For leaderVdo and vdobanner adformat we are not clicking, pause-ing on ad. hence not validating
				//if(!(adFormat.equalsIgnoreCase("leadervdo")) && (!adFormat.equalsIgnoreCase("vdobanner")))
				if(!(adFormat.equalsIgnoreCase("leadervdo") || adFormat.equalsIgnoreCase("vdobanner")))
				{
					finalResult = finalResult.concat("cl:").concat(String.valueOf(clTrackerCount));
					finalResult = finalResult.concat(":").concat("pa:").concat(String.valueOf(paTrackerCount));
					clTrackerValidate = (clTrackerCount<1 || clTrackerCount>1);
					pauseTracker = (paTrackerCount<1) || (paTrackerCount>1);
				}
			}
			// For inview AdFormat
			if(adFormat.equalsIgnoreCase("inview"))
			{
				int stlTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "stl");
				int sivTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "siv");
				int upaTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "upa");
				if(isDeviceConnected)
				{
					finalResult = finalResult.concat(":").concat("pa:").concat(String.valueOf(paTrackerCount));
				}
				finalResult = finalResult.concat(":").concat("upa:").concat(String.valueOf(upaTrackerCount));
				finalResult = finalResult.concat(":").concat("stl:").concat(String.valueOf(stlTrackerCount));
				finalResult = finalResult.concat(":").concat("siv:").concat(String.valueOf(sivTrackerCount));
				inviewTrackers = (stlTrackerCount<1 || stlTrackerCount>1) || (sivTrackerCount<1 || sivTrackerCount>1);
			}


			// upa is not fired in automation hence not validating it.
			if((viTrackerCount<1 || viTrackerCount >1) || (vi_0TrackerCount<1 || vi_0TrackerCount >1) 
					|| (vi_25TrackerCount<1 || vi_25TrackerCount >1) || (vi_50TrackerCount<1 || vi_50TrackerCount >1) 
					|| (vi_75TrackerCount<1 || vi_75TrackerCount >1) || pauseTracker
					|| muteunmutevalidate || clTrackerValidate || inviewTrackers)
			{
				//Both ae and sk can't be fired at the same time, adding custom text in final result
				if(aeTrackerCount > 0 && skTrackerCount > 0)
				{
					finalResult = finalResult + "\n sk and ae can't be fired at the same time.";
				}

				//Adding fail flag to final result
				finalResult = failResult + finalResult;
			}
			else
			{
				//Check ae / sk, if either of sk or ae is fired, then count of fired tracker should be 1
				if(aeTrackerCount > 0 && skTrackerCount > 0)
				{
					finalResult = failResult + finalResult + "\n sk and ae can't be fired at the same time.";
				}
				//if ae is fired then sk should be zero vice versa...
				else if(skTrackerCount < 1 && aeTrackerCount !=1)
				{
					finalResult = failResult + finalResult;
				}
				else if(aeTrackerCount < 1 && skTrackerCount != 1)
				{
					finalResult = failResult + finalResult;
				}
				else
				{
					finalResult = passResult + finalResult;
				}
			}

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Non App Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);
		}

		return finalResult;
	}



	//******************** This method will check the tracker for Leader Vdo ad format  *************************************************//
	public static String MobileAdsTrackerforleadervdo(String adFormat,String[][] validationDataForCampaignTracker,String finalResult)
	{
		String passResult = "PASS:";
		String failResult = "FAIL:";

		int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
		int vi_0TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_0");
		int vi_25TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_25");
		int vi_50TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_50");
		int vi_75TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_75");
		int aeTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "ae");

		//Getting Trackers fired coz of UI Operations
		int muTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "mu");
		int umTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "um");

		if((viTrackerCount<1 || viTrackerCount >1) || (vi_0TrackerCount<1 || vi_0TrackerCount >1) 
				|| (vi_25TrackerCount<1 || vi_25TrackerCount >1) || (vi_50TrackerCount<1 || vi_50TrackerCount >1) 
				|| (vi_75TrackerCount<1 || vi_75TrackerCount >1) || (aeTrackerCount<1 || aeTrackerCount >1)
				|| (muTrackerCount<1) || (muTrackerCount>1)
				|| (umTrackerCount<1) || (umTrackerCount>1))
		{

			finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
			finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
			finalResult = finalResult.concat(":").concat("mu:").concat(String.valueOf(muTrackerCount));
			finalResult = finalResult.concat(":").concat("um:").concat(String.valueOf(umTrackerCount));
			finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));

			finalResult = failResult + finalResult;
		}
		else
		{
			finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
			finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
			finalResult = finalResult.concat(":").concat("mu:").concat(String.valueOf(muTrackerCount));
			finalResult = finalResult.concat(":").concat("um:").concat(String.valueOf(umTrackerCount));
			finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));

			finalResult = passResult + finalResult;
		}

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);

		return finalResult;
	}



	//******************** This method will check the tracker for Vdo Banner ad format  *************************************************//
	public static String MobileAdsTrackerforvdobanner(String adFormat,String[][] validationDataForCampaignTracker,String finalResult)
	{
		String passResult = "PASS:";
		String failResult = "FAIL:";

		int viTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi");
		int vi_0TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_0");
		int vi_25TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_25");
		int vi_50TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_50");
		int vi_75TrackerCount = TrackerCalculation(validationDataForCampaignTracker, "vi_75");
		int aeTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "ae");

		//Getting Trackers fired coz of UI Operations
		int muTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "mu");
		int umTrackerCount = TrackerCalculation(validationDataForCampaignTracker, "um");

		if((viTrackerCount<1 || viTrackerCount >1) || (vi_0TrackerCount<1 || vi_0TrackerCount >1) 
				|| (vi_25TrackerCount<1 || vi_25TrackerCount >1) || (vi_50TrackerCount<1 || vi_50TrackerCount >1) 
				|| (vi_75TrackerCount<1 || vi_75TrackerCount >1) || (aeTrackerCount<1 || aeTrackerCount >1)
				|| (muTrackerCount<1) || (muTrackerCount>1)	|| (umTrackerCount<1) || (umTrackerCount>1))
		{

			finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
			finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
			finalResult = finalResult.concat(":").concat("mu:").concat(String.valueOf(muTrackerCount));
			finalResult = finalResult.concat(":").concat("um:").concat(String.valueOf(umTrackerCount));
			finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));			
			finalResult = failResult + finalResult;
		}
		else
		{
			finalResult = finalResult.concat(":").concat("vi:").concat(String.valueOf(viTrackerCount));
			finalResult = finalResult.concat(":").concat("vi_0:").concat(String.valueOf(vi_0TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_25:").concat(String.valueOf(vi_25TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_50:").concat(String.valueOf(vi_50TrackerCount));
			finalResult = finalResult.concat(":").concat("vi_75:").concat(String.valueOf(vi_75TrackerCount));
			finalResult = finalResult.concat(":").concat("mu:").concat(String.valueOf(muTrackerCount));
			finalResult = finalResult.concat(":").concat("um:").concat(String.valueOf(umTrackerCount));
			finalResult = finalResult.concat(":").concat("ae:").concat(String.valueOf(aeTrackerCount));			
			finalResult = passResult + finalResult;
		}
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Tracker Validation Result For Ad Format: "+adFormat + ": " +finalResult);
		return finalResult;
	}



	//This method will browse the url for vast campaign
	public static void BrowseVastTestURL(String serveBrowser, String apiKey, String adFormat) throws MalformedURLException, InterruptedException 
	{
		GenericMethodsLib.InitializeConfiguration();
		String url = "";

		//Reading Base Test URL For Mobile from configuration file
		String strBaseTestURL = propertyConfigFile.getProperty("mobileBaseTestURL").toString();

		if(adFormat.equalsIgnoreCase("VAST Feed"))
		{
			//Forming Test URL for Vast Ads
			url = strBaseTestURL + "slide=1;sleepAfter=0;adFormat=preappvideo;ak="+apiKey+";version=1.0;vdo=1;cb=[timestamp];output=js";
		}
		else if(adFormat.equalsIgnoreCase("In-View VDO"))
		{
			String strTestURL_Part2 = "responds_to_scroll=1;target_div_id=rand_tar_id_1407305377;sleepAfter=0;adFormat=inview";
			String strTestURL_Part3 = ";ak=";
			String strTestURL_Part4 = apiKey;  
			String strTestURL_Part5 = ";version=1.0;showClose=1;cb=[timestamp];output=js";

			//Forming Test URL
			url = strBaseTestURL + strTestURL_Part2 + strTestURL_Part3 + strTestURL_Part4 + strTestURL_Part5;
		}

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ***** For Ad Format: " + adFormat + ", Browsing Vast Ad Test URL: "+url + " To Send Request For Creating Child Ad. ******** ");

		WebDriver driver = GenericMethodsLib.WebDriverSetUp(serveBrowser, null);

		//Sending vast request 3 times for child creation
		driver.get(url);
		Thread.sleep(1500);
		driver.get(url);
		Thread.sleep(1500);
		driver.get(url);
		Thread.sleep(1500);
		driver.quit();
	}



	//******************** Copy Test Data File Into Test Results Folder With Name As TestResults and Write The Test URLS Browsing Results Into It *************************************************//
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void CreateTestResultsInExcelSheet(String ResultFileLocation, String Sheetname, Map <Integer, List<String>> Result_map) throws IOException, RowsExceededException, WriteException, BiffException, InterruptedException
	{       
		try {

			WritableWorkbook workbook = Workbook.createWorkbook(new File(ResultFileLocation));
			workbook.createSheet(Sheetname, 0);
			WritableSheet workSheet = workbook.getSheet(0);

			Iterator it = Result_map.entrySet().iterator();
			while (it.hasNext()) 
			{
				Map.Entry pairs = (Map.Entry)it.next();
				logger.info(pairs.getKey() + " = " + pairs.getValue());
				it.remove();

				List<String> list = (List<String>) pairs.getValue();

				for (int i = 0; i < list.size(); i++) 
				{
					Label label = new Label( i, Integer.parseInt( pairs.getKey().toString()), list.get(i).toString());
					workSheet.addCell(label);
				}
			}

			workbook.write();
			workbook.close();
		} catch (Exception e) {
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while writing sheet. ", e);
		}
	}

	/** This method cleans all the process.
	 * 
	 * @param suiteStartTime
	 */
	public static void cleanProcesses()
	{
		String strProcess = "chromedriver";
		String strCommand;
	
		/** Close all the remaining instance of the browser. */
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
		{
			strCommand = "taskkill /F /IM " + strProcess + ".exe";
		}
		else
		{
			strCommand = "killall " + strProcess;
		}
	
		/** Running Command */
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running command to close Chromedriver instance if any remaining:");
		logger.info(strCommand);
		try {
			Runtime.getRuntime().exec(strCommand);
		} catch (IOException e) {
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error while cleaning up.", e);
		}
	}



}







