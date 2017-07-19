/**
 * Last Changes Done on Jan 20, 2015 12:41:21 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */

package projects;


import java.io.File;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Parameters;

import vlib.FileLib;
import vlib.GenericMethodsLib;
import vlib.TestNgLib;
import vlib.XlsLib;


public class TestSuiteClass {

	public static String executionResult;
	public static String rerunExecutionResult;

	public static String resultFileLocation;
	public static TreeMap<String, Integer> totalTC = new TreeMap<String, Integer>();
	public static boolean isFresh;
	public static boolean rerun;
	public static Map<String, ArrayList<String>> rerunClassNames=new HashMap<String, ArrayList<String>>();
	public static String local_logFileLocation;
	public static String local_logFileName;

	public static String suiteStartTime;
	public static String suiteEndTime;
	public static String executedOnMachine;
	public static String environment;

	public static String AUTOMATION_HOME;
	public static boolean ifNodeRunning = false;

	/** setting up unique execution id to be used in case of threads */
	public static ThreadLocal<Object> UNIQ_EXECUTION_ID = new ThreadLocal<>();


	//Declaring logger
	Logger logger = Logger.getLogger(TestSuiteClass.class.getName());

	//@Parameters({"logFileLocation", "logFileName", "ReRun"})
	@Parameters("ReRun")
	@BeforeSuite
	//public void beforeSuite(String logFileLocation, String logFileName, String ReRun)
	public void beforeSuite(String ReRun)
	{

		try
		{
			logger.info("###################################################################");
			logger.info("******* Test Started At Time ********: " +GenericMethodsLib.DateTimeStamp("MMddyyyy_hhmmss"));
			logger.info("###################################################################");

			/** setting up automation_home */
			AUTOMATION_HOME = TestSuiteClass.class.getProtectionDomain().getCodeSource().getLocation().getFile().toString().replace("/bin/", "");
			System.out.println("AUTOMATION_HOME is: "+AUTOMATION_HOME);

			/** 1. Initialize configuration */
			GenericMethodsLib.InitializeConfiguration();

			/** Getting suite start time, this time will also be saved in id column of each module specific table at the time of saving results. */
			suiteStartTime = GenericMethodsLib.DateTimeStamp("yyyy-MM-dd HH:mm:ss");

			/** Getting environment and machine name where test is performed --> to be used while saving results in database */
			String ip = Inet4Address.getLocalHost().getHostAddress();
			String name = Inet4Address.getLocalHost().getHostName();
			executedOnMachine = name+"/"+ip;
			environment = GenericMethodsLib.propertyConfigFile.getProperty("currentTestEnvironment").toString();

			/** Loading log4j.properties file for logger and creating logs folder in advance */
			PropertyConfigurator.configure(TestSuiteClass.AUTOMATION_HOME.concat("/conf/log4j.properties"));
			FileLib.CreateDirectory(TestSuiteClass.AUTOMATION_HOME.concat("/logs"));

			if(ReRun.equalsIgnoreCase("Yes"))
			{
				rerun = true;
			}
			else
			{
				rerun = false;
			}

			isFresh = true;

			/** Commenting this code, no more required to use
			 */
			//3. Set up sikuli in Mac and Windows Machines.
			//FileLib.SetUpSikuli();

			/** 4.Check if result file exists or not. */
			String resultFileName =  null;
			String dateTimeStmap = GenericMethodsLib.DateTimeStamp("MMddyyyy_hhmmss");

			resultFileName = "Main_Result_".concat(dateTimeStmap);

			resultFileLocation = TestSuiteClass.AUTOMATION_HOME.concat("/results/").concat(resultFileName).toString();	
			File ResultFile = new File(resultFileLocation);

			if(!(ResultFile.exists()))
			{
				logger.info("Main Result Folder doesn't exist at " + resultFileLocation);

				boolean b = ResultFile.mkdirs();

				if(b)
				{
					logger.info("Main Result folder is created successfully ");
				}
				else
				{
					logger.info("Main Result folder can not be created");
				}
			}

			executionResult = resultFileLocation.concat("/").concat(resultFileName).concat(".xls");

			logger.info("Main Result file location : " + executionResult);

			XlsLib result = new XlsLib();

			/** 4. Create Empty Excel file */
			result.emptyExcel(executionResult);
		}
		catch(Exception e)
		{
			logger.error("Exception handled during execution of beforeTestSuite. ", e); 
		}

	}

	@AfterSuite
	public void afterSuite()  
	{
		try{

			if(rerun)
			{
				logger.info("###################################################################");
				logger.info(" Test Suite Execution is Started for ReRun");
				logger.info("###################################################################");
				isFresh = false;

				TestNgLib.RunReRunTest(rerunClassNames,local_logFileLocation,local_logFileName);

				logger.info("###################################################################");
				logger.info(" Test Suite Execution is completed for ReRun");
				logger.info("###################################################################");
			}


			/** Generate Summary of Results */ 
			XlsLib test = new XlsLib();
			test.generateFinalResult(executionResult);

			/* Commenting out the html result now ... due to some bug
			XlsLib test = new XlsLib();
			test.generateFinalResult(executionResult);
			String executionResultHtml = executionResult + ".html";
			Excel2Html.GenerateMainResultExcelintoHTML(executionResult, executionResultHtml);

			//Open generated html results
			FileLib.OpenResult(executionResultHtml);
			 */

			try
			{
				/** Clean processes */
				if(!ifNodeRunning)
				{
					GenericMethodsLib.cleanProcesses();
				}

				/** inserting execution entry in db - Commenting this for time being.. */
				//suiteEndTime = MobileTestClass_Methods.DateTimeStamp("yyyy-MM-dd hh:mm:ss");
				//DBLib.insertExecutionLog(suiteStartTime, suiteEndTime);
			}
			catch(Exception e)
			{
				logger.error("Exception occurred during killing residual chrome driver process. ", e);
			}

			/** Printing Test End Time. */
			logger.info("###################################################################");
			logger.info("******* Test End Time ********: " +GenericMethodsLib.DateTimeStamp("MMddyyyy_hhmmss"));
			logger.info("###################################################################");

		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}

	}

}
