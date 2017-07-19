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
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.json.JSONObject;
import org.testng.annotations.Test;
import com.mysql.jdbc.Connection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;



import vlib.CaptureScreenShotLib;
import vlib.FileLib;
import vlib.GenericMethodsLib;
import vlib.KeyBoardActionsUsingRobotLib;
import vlib.XlsLib;

public class ExecuteTestObjectsParallely {

	static String suiteName = ""; 
	String testCaseFile;
	String testResultFile;
	File resultFile;
	HashMap<String, Boolean> map = new HashMap<>();
	Logger logger = Logger.getLogger(ExecuteTestObjectsParallely.class.getName());

	static Connection connectionServe;
	static JSONObject jsonObjectRepo = new JSONObject();
	static TreeMap<Integer, TestCaseObjects> testCaseObjectMap;

	ExecutorService executor = Executors.newCachedThreadPool();
	CompletableFuture<?> [] completableFutureObjects;


	/** setting up configuration before test */
	@SuppressWarnings("unused")
	@BeforeClass
	public void beforeClass() 
	{
		try
		{
			logger.info(" ################### Welcome To Portal Test Object Parallel Execution ########################");
			suiteName = "TransformerPortal";
			GenericMethodsLib.InitializeConfiguration();

			/** Initializing constructor of KeyBoardActionsUsingRobotLib and CaptureScreenShotLib here, 
			 * so that focus on chrome browser is not disturbed. */
			Robot rt = new Robot();
			KeyBoardActionsUsingRobotLib keyBoard = new KeyBoardActionsUsingRobotLib(rt);
			CaptureScreenShotLib captureScreenshot = new CaptureScreenShotLib(rt);

			connectionServe = GenericMethodsLib.CreateServeSQLConnection();
			testCaseFile = TestSuiteClass.AUTOMATION_HOME.toString().concat("/tc_cases/transformerportal/Test_Cases_Transformer.xls");

			logger.debug(" Test Cases File Located at: "+testCaseFile);
			testResultFile = TestSuiteClass.resultFileLocation.concat("/transformerPortal/TestResults");

			resultFile = FileLib.CopyExcelFile(testCaseFile, testResultFile);
			logger.debug(" Test Cases Result File Located at: "+resultFile);

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

			/** load test case objects in a map from the actual executable test case list */
			testCaseObjectMap = new ExecuteTestObjectsHandler().getTestCaseObjectMap(readTest.getRunnableTestCaseObjects(testCaseFile));

			/** set the completable future array size */
			completableFutureObjects = new ExecuteTestObjectsHandler().getCompletableFuture(testCaseObjectMap);
		}
		catch (Exception e)
		{
			logger.error(" Error occurred before starting the portal test", e);
		}
	}


	/** running tests */
	@Test
	public void runTests()
	{
		/** assign tasks with recursion */
		assignTasks();

		/** wait until all tasks from test case object map are not completed --> progress status = 1 and picked up status flag = true; */
		while(!new ExecuteTestObjectsHandler().ifAllTestCaseObjectsAreExecuted())
		{
			/** handle code */
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}


	@SuppressWarnings("static-access")
	public Object assignTasks()
	{
		for(CompletableFuture<?> futureObject : completableFutureObjects)
		{
			/** first get a not executed test case */
			TreeMap<Integer, TestCaseObjects> testCaseObjectMapToBeExecuted = new ExecuteTestObjectsHandler().getFreeTestCase();

			if(testCaseObjectMapToBeExecuted != null)
			{	
				logger.info("Executing Free Test Case: "+testCaseObjectMapToBeExecuted.firstEntry().getValue().getTestCaseId());

				CompletableFuture<?> futureObjectOutput = futureObject.supplyAsync(() -> new ExecuteTestObjectsHandler().
						executeTask(testCaseObjectMapToBeExecuted, connectionServe, jsonObjectRepo, resultFile), executor);

				CompletableFuture.anyOf(futureObjectOutput).thenRunAsync(() -> assignTasks(), executor);
			}	
		}


		return "Task_Completed_Again";
	}


	/** finishing tests, writing results and saving in db */
	@AfterClass
	public void afterClass()  
	{
		try {
			logger.info("Shutting down executor ... ");
			
			executor.shutdownNow();
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

