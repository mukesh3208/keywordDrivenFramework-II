/**
 * Last Changes Done on 5 Mar, 2015 12:07:46 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package projects.portal;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;



public class WriteTestResults {

	/**
	 * @param args
	 */

	Logger logger = Logger.getLogger(WriteTestResults.class.getName());

	public boolean writeTestStepResult(String fileName, HashMap<String, String> hashmap)
	{
		boolean flag = false;
		try
		{
			ReadTestCases readTestCase = new ReadTestCases();
			String separator = readTestCase.separator;
			String teststepsSheet = readTestCase.testStepSheet;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Received separator: "+separator + " to split column, row and values from hashmap values.");
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writing test step result in sheet: "+teststepsSheet);

			//Get the existing workbook
			Workbook book = Workbook.getWorkbook(new File(fileName));
			WritableWorkbook copiedBook = Workbook.createWorkbook(new File(fileName), book);	
			WritableSheet sheet = copiedBook.getSheet(teststepsSheet);

			for(Entry<String, String> map: hashmap.entrySet())
			{
				String value = map.getValue();

				int column = Integer.parseInt(value.split(separator)[0]);
				int row = Integer.parseInt(value.split(separator)[1]);
				String result = "";

				try{
					result = value.split(separator)[2];
				}catch(ArrayIndexOutOfBoundsException a){
					logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : No Result received for column = "+column + " and row = "+row + " reassging a space. ");
				}

				//logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" :  writing test step result: "+ result + " in column " +column + " and row: "+row);

				Label lblRecordData = new Label(column, row, result);
				sheet.addCell(lblRecordData);
			}

			copiedBook.write();
			copiedBook.close();
			book.close();

			flag = true;
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while writing test steps results. ", e);
		}
		return flag;

	}

	public void addResultColumn(File testResultFile, String sheetName, String resultLabel)
	{
		try{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Adding label: "+resultLabel +" column in file: "+testResultFile + " in sheet: "+sheetName);

			Workbook book = Workbook.getWorkbook(testResultFile);
			WritableWorkbook copiedBook = Workbook.createWorkbook(testResultFile, book);
			WritableSheet sheet = copiedBook.getSheet(sheetName);

			Label lblColumnName = new Label(sheet.getColumns(), 0, resultLabel);
			sheet.addCell(lblColumnName);

			copiedBook.write();
			copiedBook.close();
			book.close();
		}catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while adding Test Result column in file: "+testResultFile, e);
		}
	}

	public boolean writeTestCaseResult(File testResultFile, HashMap<String, Boolean> hashmap)
	{
		boolean flag;
		try{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writting test results in test summary file "+testResultFile);
			Workbook book = Workbook.getWorkbook(testResultFile);
			WritableWorkbook copiedBook = Workbook.createWorkbook(testResultFile, book);

			ReadTestCases readTest = new ReadTestCases();
			String summarySheet = readTest.testCaseSummarySheet;
			WritableSheet sheet = copiedBook.getSheet(summarySheet);

			int tcIDcolumn = sheet.findCell(readTest.tcSummaryTCIdColumn, 0, 0, sheet.getColumns(),0, false).getColumn();
			int tcResultscolumn = sheet.findCell(readTest.tcSummaryResultColumn, 0, 0, sheet.getColumns(),0, false).getColumn();

			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Test Id column: "+tcIDcolumn + " and test result column: "+tcResultscolumn + " in test summary file.");

			for(Entry<String, Boolean> map: hashmap.entrySet())
			{
				String tcID = map.getKey();
				String tcstatus;

				if(map.getValue())
				{
					tcstatus = "Pass";					
				}
				else
				{
					tcstatus = "Fail";
				}

				for(int i=1; i<sheet.getRows(); i++)
				{
					String testCaseID = sheet.getCell(tcIDcolumn, i).getContents().toString();

					if(testCaseID.equalsIgnoreCase(tcID))
					{
						Label lblColumnName = new Label(tcResultscolumn, i, tcstatus);
						sheet.addCell(lblColumnName);

						logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writitng Result: "+tcstatus + " For Test Case Id: "+testCaseID);
						break;
					}
				}
			}
			copiedBook.write();
			copiedBook.close();
			book.close();
			flag = true; 
		}catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while writting resutls in test summary file: " + testResultFile, e);
		}
		return flag;
	}


	/** This is a synchronized method to be used by Threads and Non-Threads to write test case results in excel sheet.
	 * 
	 * @param testResultFile
	 * @param testCaseObject
	 * @return
	 */
	public synchronized boolean writeTestCaseObjectResult(WritableWorkbook copiedBook, TestCaseObjects testCaseObject)
	{
		boolean flag;
		try{

			ReadTestCases readTest = new ReadTestCases();
			String summarySheet = readTest.testCaseSummarySheet;
			WritableSheet sheet = copiedBook.getSheet(summarySheet);

			int tcIDcolumn = sheet.findCell(readTest.tcSummaryTCIdColumn, 0, 0, sheet.getColumns(),0, false).getColumn();
			int tcResultscolumn = sheet.findCell(readTest.tcSummaryResultColumn, 0, 0, sheet.getColumns(),0, false).getColumn();

			/** get test case details from received test case object */
			String receivedTestCaseId = testCaseObject.getTestCaseId();
			int testCaseIdRowNumber = testCaseObject.getTestCaseIdRowNumber();
			String testCaseResult = testCaseObject.getTestCaseResult();

			/** get the test case id from the sheet, from the Test Case Id Column and match again -- just to double sure
			 * before putting the results */
			String testCaseIDFromSheet = sheet.getCell(tcIDcolumn, testCaseIdRowNumber).getContents().toString();

			if(testCaseIDFromSheet.equalsIgnoreCase(receivedTestCaseId)){
				/** adding result at the row number of received test case id --- therefore avoiding the iteration of whole sheet */
				Label lblColumnName = new Label(tcResultscolumn, testCaseIdRowNumber, testCaseResult);
				sheet.addCell(lblColumnName);

				logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writitng Result: "+testCaseResult + " For Test Case Id: "+receivedTestCaseId);
			}else{
				logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Strange Case Found Here: "+testCaseResult + " For Test Case Id: "+receivedTestCaseId);
			}

			flag = true; 
		}catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while writting resutls in test summary file: ", e);
		}

		return flag;
	}


	/** This method is also special and innovative, this will start writing test results of received test step object in synchronized way 
	 * 
	 * @param fileName
	 * @param testCaseObject
	 * @return
	 */
	public synchronized boolean writeTestStepObjectResult(WritableWorkbook copiedBook, TestCaseObjects testCaseObject)
	{
		boolean flag = false;
		try
		{
			ReadTestCases readTestCase = new ReadTestCases();
			String teststepsSheet = readTestCase.testStepSheet;

			/** get the test step objects from the received test case object */
			List<TestStepObjects> testStepObjectsList = testCaseObject.gettestStepObjectsList();

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writing test step result in sheet: "+teststepsSheet);

			/** Get the existing workbook - sheet */	
			WritableSheet sheet = copiedBook.getSheet(teststepsSheet);

			/** get test step id and test result column number */
			int testStepIDcolumn = sheet.findCell(readTestCase.tcStepTCStepIDColumn, 0, 0, sheet.getColumns(),0, false).getColumn();
			int testStepResultscolumn = sheet.findCell(readTestCase.tcStepResultColumn, 0, 0, sheet.getColumns(),0, false).getColumn();

			/** iterate the test step objects list */
			for(TestStepObjects testStepObject : testStepObjectsList)
			{
				int row = testStepObject.getTestStepIdRowNumber();
				String receivedTestStepId = testStepObject.getTestStepId();
				String testStepIdFromSheet = sheet.getCell(testStepIDcolumn, row).getContents().trim();

				/** write test step results only if the received test step is matching the test step from the sheet, at the row */
				if(receivedTestStepId.equalsIgnoreCase(testStepIdFromSheet))
				{
					String result = testStepObject.getTestStepResult();
					Label lblTestStepResult = new Label(testStepResultscolumn, row, result);
					sheet.addCell(lblTestStepResult);
				}				
			}

			flag = true;
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while writing test steps results. ", e);
		}
		return flag;

	}


	/** This one is the only method to write the test results in test case.
	 * 
	 * @param testResultFile
	 * @param testCaseObject
	 * @return
	 */
	public synchronized boolean writeTestObjectResults(File testResultFile, TestCaseObjects testCaseObject)
	{
		boolean flag = false;

		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writting test results in test case file "+testResultFile);

			Workbook book = Workbook.getWorkbook(testResultFile);
			WritableWorkbook copiedBook = Workbook.createWorkbook(testResultFile, book);

			/** write test case result */
			boolean a = writeTestCaseObjectResult(copiedBook, testCaseObject);

			/** write test step result */
			boolean b = writeTestStepObjectResult(copiedBook, testCaseObject);

			/** get the final flag */
			flag = a && b;

			copiedBook.write();
			copiedBook.close();
			book.close();
		}catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : "+"Error occurred while writting resutls", e);
		}

		return flag;
	}
}
