/**
 * Last Changes Done on 14 Oct, 2015 2:15:07 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import projects.TestSuiteClass;import org.apache.log4j.Logger; 


import projects.portal.ReadTestCases;


import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;


public class SaveResultsToMySql
{

	static Logger logger = Logger.getLogger(SaveResultsToMySql.class.getName());

	/** This main can be used to load results in db manually, just call the respective method - thats it.
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws BiffException 
	 * @throws WriteException 
	 * @throws RowsExceededException 
	 */
	public static void main(String[] args) throws RowsExceededException, WriteException, BiffException, IOException 
	{
		saveChocolateResults("/Users/user/Downloads/Automation_Execution_Results.xls", "2015-10-16 02:01:06", "VDO102/172.16.0.241", "qa");
	}


	/** This method will save chocolate results into mysql db qaautomation.chocolateResults.
	 * 
	 * @param resultFile
	 * @param executionID
	 * @return
	 */
	public static boolean saveChocolateResults(String resultFile, String executionID, String executedOnMachine, String environment)
	{
		boolean flag = false;
		String insertSQL = "";

		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chocolate results are being saved into db.... ");

			Connection connection = getAutomationConnection();
			String [][] data = FileLib.FetchDataFromExcelSheet(resultFile, "GetRequestURLs_Or_PostRequests", "Module_Name", "Test_Results");

			/** form insert query */
			insertSQL = " insert into chocolateResults (execution_id, executed_on_machine, environment, requests, module, results) values ";
			String subQuery = getSubQueryFromResultSheet(data, executionID, executedOnMachine, environment);
			insertSQL = insertSQL + subQuery;

			Statement statement = (Statement) connection.createStatement();
			statement.executeUpdate(insertSQL);
			connection.close();

			flag = true;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Chocolate results are saved into db. ");
		}
		catch(MySQLSyntaxErrorException e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Check Query: "+insertSQL);
		}
		catch(Exception t)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while saving chocolate results to mysql db. ", t);
		}

		return flag;
	}


	/** This method will save portal summary results and portal detail results in to database,
	 * in case of portal - transformerPortalSummary table will contain all the test cases whereas transformerPortalDetailResults will contain
	 * only those test cases which were executed to avoid dumping of whole not executed test cases.
	 * 
	 * @param resultFile
	 * @param executionID
	 * @return
	 */
	public static boolean savePortalResults(String resultFile, String executionID, String executedOnMachine, String environment)
	{
		boolean flag = false;
		String insertSQL = "";
		String insertSQLDetail = "";
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Portal results are being saved into db.... ");

			/** getting summary and test case step sheet names */
			ReadTestCases readTest = new ReadTestCases();
			String testStepSheetName = readTest.testStepSheet;
			String testSummarySheetName = readTest.testCaseSummarySheet;

			/** get data from portal sheet */
			String [][] summaryData = FileLib.FetchDataFromExcel_SpecificSheet(resultFile, testSummarySheetName, "TC_ID", "Run", "Description",	"Comments",	"Test_Results");
			String [][] detailData = getPortalRunnableTestStepsData(summaryData, resultFile, testStepSheetName);

			Connection connection = getAutomationConnection();

			/** form insert query for summary results */
			insertSQL = " insert into transformerPortalSummary (execution_id, executed_on_machine, environment, TC_ID, Run, Description, Comments, Test_Results) values ";
			String subQuery = getSubQueryFromResultSheet(summaryData, executionID, executedOnMachine, environment);
			insertSQL = insertSQL + subQuery;

			Statement statement = (Statement) connection.createStatement();
			statement.executeUpdate(insertSQL);

			/** form insert query for detail results */
			insertSQLDetail = " insert into transformerPortalDetailResults (execution_id, executed_on_machine, environment, TC_ID, Step_ID, Description, Keyword, objectName, inputData_Production, inputData_QA, Regression_Test_Cases_Covered, Test_Results) values ";
			String subQueryDetail = getSubQueryFromResultSheet(detailData, executionID, executedOnMachine, environment);
			insertSQLDetail = insertSQLDetail + subQueryDetail;

			Statement statementDetail = (Statement) connection.createStatement();
			statementDetail.executeUpdate(insertSQLDetail);

			connection.close();

			flag = true;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Portal results are saved into db. ");
		}
		catch(MySQLSyntaxErrorException e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Check Summary Query: "+insertSQL);
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Check Detail Results Query: "+insertSQLDetail);
		}
		catch(Exception t)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while saving portal results to mysql db. ", t);
		}

		return flag;
	}


	/** get only runnable portal test case data.
	 * 
	 * @param summaryData
	 * @param resultFile
	 * @param testStepSheetName
	 * @return
	 */
	public static String [][] getPortalRunnableTestStepsData(String [][]summaryData, String resultFile, String testStepSheetName)
	{
		List<String> runnableTC_Id = new ArrayList<String>();
		for(int i=0; i<summaryData.length; i++)
		{
			if(summaryData[i][1].equalsIgnoreCase("Yes"))
			{
				runnableTC_Id.add(summaryData[i][0]);
			}
		}

		String [][] detailData = FileLib.FetchPortalTestCases(resultFile, testStepSheetName, runnableTC_Id, "TC_ID", "Step_ID",	"Description", "Keyword", "objectName",	"inputData_Production",	"inputData_QA",	"Regression_Test_Cases_Covered", "Test_Results");
		return detailData;
	}


	/**
	 * This method will save results in mysql db
	 * qaautomation.mobileAdServingResults this method saves result for two
	 * tests: HttpsCheck and VdopiaMobileserving, and this information about
	 * suite is stored in Other_Information column
	 * 
	 * @param resultFile
	 * @param executionID
	 * @param executedOnMachine
	 * @param environment
	 * @param otherInformation
	 * @return
	 */
	public static boolean saveMobileAdServingResults(String resultFile, String executionID, String executedOnMachine, String environment, String otherInformation)
	{
		boolean flag = false;
		String insertSQL = "";

		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Mobile Adserving results are being saved into db.... ");

			Connection connection = getAutomationConnection();
			String [][] data = FileLib.FetchDataFromExcelSheet(resultFile, "Publisher_Email", "Campaign_Name", "Channel_APIKEY", "Publisher_ID", "Channel_ID", 
					"Campaign_ID", "ADS_ID", "Video_Choice", "Custom_Details", "Channel_Settings", "Ad_Format",	"Ads_Duration",	"Ads_Dimension",
					"Tracker_URL", 	"Destination_URL", "Device_Type", "Action_Type", "Ad_Details", "CompanionBanner", "Test_URLs", "Test_Results" );

			/** form insert query */
			insertSQL = " insert into mobileAdServingResults (execution_id, executed_on_machine, environment, Other_Information, Publisher_Email, Campaign_Name, Channel_APIKEY, Publisher_ID, Channel_ID, " +
					" Campaign_ID, ADS_ID, Video_Choice, Custom_Details, Channel_Settings, Ad_Format, Ads_Duration, Ads_Dimension, Tracker_URL, " +
					" Destination_URL, Device_Type, Action_Type, Ad_Details, CompanionBanner, Test_URLs, Test_Results) " +
					" values ";

			String subQuery = getSubQueryFromResultSheet(data, executionID, executedOnMachine, environment, otherInformation);
			insertSQL = insertSQL + subQuery;

			Statement statement = (Statement) connection.createStatement();
			statement.executeUpdate(insertSQL);
			connection.close();

			flag = true;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Mobile Adserving results are saved into db. ");
		}
		catch(MySQLSyntaxErrorException e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Check Query: "+insertSQL);
		}
		catch(Exception t)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while saving Mobile Adserving results to mysql db. ", t);
		}

		return flag;

	}


	/**
	 * get connection to qaautomation db. 
	 * 
	 * @return
	 */
	public static Connection getAutomationConnection()
	{
		Connection qaConnection = null;
		try
		{
			String dbClass = "com.mysql.jdbc.Driver";		
			Class.forName(dbClass);

			String dburl = "jdbc:mysql://serve.qa.vdopia.com:3306/qaautomation?";
			String dbuserName = "root";
			String dbpassword = "QA@1234";
			qaConnection = (Connection) DriverManager.getConnection (dburl,dbuserName,dbpassword);
		}
		catch(SQLException | ClassNotFoundException t)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting automation connection. ", t);
		}
		return qaConnection;
	}


	/** this method will form the subquery like VALUES ==> ('1', '2', '3', '4') from the supplied data array
	 * and a supplied execution id will appended before of each row value.
	 * @param data
	 * @return
	 */
	public static String getSubQueryFromResultSheet(String [][]data, String executionID, String executedOnMachine, String environment)
	{
		/** creating insert sql sub query for each row retrieved from above array */
		String subQuery = "";

		for(int r=0; r<data.length; r++)
		{
			subQuery = subQuery + " ( " + "'" +executionID +"', " + "'"+ executedOnMachine + "', " + "'" + environment + "', " ;

			for(int c=0; c<data[0].length; c++)
			{
				String str = data[r][c].replace("'", "").replace("\"", "");
				subQuery = subQuery + " '"+ str +"' ";

				/** putting a comma between all values in sub query */
				if(c!=data[0].length-1)
				{
					subQuery = subQuery + " , ";
				}
			}
			subQuery = subQuery + " ) ";

			/** putting a comma between all values in sub query */
			if(r!=data.length-1)
			{
				subQuery = subQuery + " , ";
			}
		}

		return subQuery;
	}


	/** this method will form the subquery like VALUES ==> ('1', '2', '3', '4') from the supplied data array
	 * and a supplied execution id will appended before of each row value.
	 * @param data
	 * @return
	 */
	public static String getSubQueryFromResultSheet(String [][]data, String executionID, String executedOnMachine, String environment, String otherInformation)
	{
		/** creating insert sql sub query for each row retrieved from above array */
		String subQuery = "";

		for(int r=0; r<data.length; r++)
		{
			subQuery = subQuery + " ( " + "'" +executionID +"', " + "'"+ executedOnMachine + "', " + "'" + environment + "', " + "'" + otherInformation + "', ";

			for(int c=0; c<data[0].length; c++)
			{
				String str = data[r][c].replace("'", "").replace("\"", "");
				subQuery = subQuery + " '"+ str +"' ";

				/** putting a comma between all values in sub query */
				if(c!=data[0].length-1)
				{
					subQuery = subQuery + " , ";
				}
			}
			subQuery = subQuery + " ) ";

			/** putting a comma between all values in sub query */
			if(r!=data.length-1)
			{
				subQuery = subQuery + " , ";
			}
		}

		return subQuery;
	}


}

