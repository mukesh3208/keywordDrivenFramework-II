/**
 * Last Changes Done on Jan 16, 2015 12:04:40 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */

package projects.portal;

import java.awt.Robot;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.mysql.jdbc.Connection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;



import vlib.CaptureScreenShotLib;
import vlib.FileLib;
import vlib.KeyBoardActionsUsingRobotLib;
import vlib.GenericMethodsLib;
import vlib.XlsLib;

public class ExecuteTestObjectsSequentially {

	static String suiteName = ""; 
	String testCaseFile;
	String testResultFile;
	File resultFile;
	HashMap<String, Boolean> map = new HashMap<>();
	Logger logger = Logger.getLogger(ExecuteTestObjectsSequentially.class.getName());

	static Connection connectionServe;
	static JSONObject jsonObjectRepo = new JSONObject();
	static List<TestCaseObjects> testCaseObjectList;

	/** setting up configuration before test */
	@SuppressWarnings("unused")
	@BeforeClass
	public void beforeClass() 
	{
		try
		{
			logger.info(" : ################### Portal Test Started. ########################");
			suiteName = "TransformerPortal";
			GenericMethodsLib.InitializeConfiguration();

			/** Initializing constructor of KeyBoardActionsUsingRobotLib and CaptureScreenShotLib here, 
			 * so that focus on chrome browser is not disturbed. 
			 */
			Robot rt = new Robot();
			KeyBoardActionsUsingRobotLib keyBoard = new KeyBoardActionsUsingRobotLib(rt);
			CaptureScreenShotLib captureScreenshot = new CaptureScreenShotLib(rt);

			connectionServe = GenericMethodsLib.CreateServeSQLConnection();
			testCaseFile = TestSuiteClass.AUTOMATION_HOME.toString().concat("/tc_cases/transformerportal/Test_Cases_Transformer.xls");

			logger.debug(" : Test Cases File Located at: "+testCaseFile);
			testResultFile = TestSuiteClass.resultFileLocation.concat("/transformerPortal/TestResults");

			resultFile = FileLib.CopyExcelFile(testCaseFile, testResultFile);
			logger.debug(" : Test Cases Result File Located at: "+resultFile);

			/** get object repository as json object */
			String objectRepo = TestSuiteClass.AUTOMATION_HOME.concat("/object_repository/portalObjectRepository/transformerPortal_ObjectRepository.xls");
			jsonObjectRepo = new GetObjectRepoAsJson().getObjectRepoAsJson(objectRepo);

			ReadTestCases readTest = new ReadTestCases();
			String testStepResultColumnLabel = readTest.tcStepResultColumn;
			String testStepSheetName = readTest.testStepSheet;

			WriteTestResults writeResult = new WriteTestResults();
			writeResult.addResultColumn(resultFile, testStepSheetName, testStepResultColumnLabel);

			String testSummaryResultColumnLabel = readTest.tcSummaryResultColumn;
			String testSummarySheetName = readTest.testCaseSummarySheet;
			writeResult.addResultColumn(resultFile, testSummarySheetName, testSummaryResultColumnLabel);

			/** load test case objects */
			testCaseObjectList = readTest.getRunnableTestCaseObjects(testCaseFile);
		}
		catch (Exception e)
		{
			logger.error(" : Error occurred before starting the portal test", e);
		}
	}


	/** running tests */
	@Test
	public void runTests()
	{
		ReadTestCases readTestCases = new ReadTestCases();

		/** iterate the test case object list and execute test case and write results */
		for(TestCaseObjects testCaseObject : testCaseObjectList)
		{
			readTestCases.executeTestCaseObject(testCaseObject, connectionServe, jsonObjectRepo);
			new WriteTestResults().writeTestObjectResults(resultFile, testCaseObject);
		}
	}

	
	/** finishing tests, writing results and saving in db */
	@AfterClass
	public void afterClass()  
	{
		try {
			connectionServe.close();

			/** Get Total number of test cases executed */
			File f = new File(resultFile.toString());
			int totalTestCase = (XlsLib.getTotalRowOfExcelWorkbook(f))-1;

			totalTestCase = map.size();
			TestSuiteClass.totalTC.put(new ReadTestCases().gettestCaseSummarySheet(), totalTestCase);

			/** Updating portal execution summary and test steps results to main results sheet */
			String summaryData[][] = new XlsLib().dataFromExcel(resultFile.toString(), new ReadTestCases().gettestCaseSummarySheet());
			new XlsLib().updateResultInNewSheet(TestSuiteClass.executionResult, new ReadTestCases().gettestCaseSummarySheet(), summaryData);

			String stepsResultData[][] = new XlsLib().dataFromExcel(resultFile.toString(), new ReadTestCases().gettestStepSheet());
			new XlsLib().updateResultInNewSheet(TestSuiteClass.executionResult, new ReadTestCases().gettestStepSheet(), stepsResultData);

			logger.info(" : ################### Test Ended. ########################");
			
		}catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}


	/**
	 * Setting up SSP Suite Name to decide which OR needs to be loaded.
	 * @return
	 */
	public static String getSuiteName()
	{
		return suiteName; 
	}
}
