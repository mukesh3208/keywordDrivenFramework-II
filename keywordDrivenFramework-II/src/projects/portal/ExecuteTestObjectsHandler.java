package projects.portal;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.json.JSONObject;

import com.mysql.jdbc.Connection;



public class ExecuteTestObjectsHandler {

	Logger logger = Logger.getLogger(ExecuteTestObjectsHandler.class.getName());


	/** This method returns the free test case object which is not yet picked up by any thread
	 * once this is identified, set getIfTestCaseQueued = true so that it doesn't picked up again and
	 * setTestCaseExecutionStatus = 0 to mark it as in progress. 
	 * 
	 * @param testCaseObjects
	 * @return
	 */
	public synchronized TreeMap<Integer, TestCaseObjects> getFreeTestCase(){

		TreeMap<Integer, TestCaseObjects> freeTestCaseObjectMap = null;

		try
		{
			for(Entry<Integer, TestCaseObjects> tcObj : ExecuteTestObjectsParallely.testCaseObjectMap.entrySet()){

				TestCaseObjects testCaseObject = tcObj.getValue();
				int testCaseKey = tcObj.getKey();

				if(testCaseObject.getIfTestCaseQueued().get()==false){

					/** set flag to true so that it doesn't get picked up again */
					testCaseObject.setIfTestCaseQueued(new AtomicBoolean(true));
					testCaseObject.setTestCaseExecutionProgressStatus(new AtomicInteger(0));

					/** before returning the test object, update the actual map */
					ExecuteTestObjectsParallely.testCaseObjectMap.put(testCaseKey, testCaseObject);

					/** return the map containing the key and testcase object like: <1, <TestCaseObject>> 
					 * this will help in updating the progress of test case object based on received key in the
					 * global test case object map */
					freeTestCaseObjectMap = new TreeMap<>();
					freeTestCaseObjectMap.put(testCaseKey, testCaseObject);

					logger.info("Returning Free Test Case ID: "+testCaseObject.getTestCaseId());
					return freeTestCaseObjectMap;
				}
				else{
					continue;
				}
			}
		}catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return freeTestCaseObjectMap;
	}


	/** Find if all tasks are executed by checking  getTestCaseExecutionStatus, which should be 1
	 * 
	 * @return
	 */
	public synchronized boolean ifAllTestCaseObjectsAreExecuted(){
		
		boolean flag = true;

		try{
			for(Entry<Integer, TestCaseObjects> tcObj : ExecuteTestObjectsParallely.testCaseObjectMap.entrySet()){

				TestCaseObjects testCaseObject = tcObj.getValue();

				/** that means there is still some task are going on, if no 0 then all were executed */
				if(testCaseObject.getTestCaseExecutionProgressStatus().get()==(Integer.parseInt("0"))){
					return false;
				}
				else{
					continue;
				}
			}
		}catch(Exception e)
		{
			flag = false;
			logger.error(e.getMessage(), e);
		}

		return flag;
	}


	/** Setting up the limit of tasks which can be executed in parallel -- Now so far in my knowledge there are two ways:
	 * 1. Set the limit of Executor.newFixedThreadPool(1) then no matter what you have in CompletableFuture Array, 
	 * it won't be more than one at a time
	 * 
	 * 2. Use Executors.newCachedThreadPool(); and Set the limit of CompletableFuture Array then there won't be any limit on threads 
	 * by there will be limit on tasks which can be picked up for asynchronous execution.
	 */
	public CompletableFuture<?> [] getCompletableFuture(TreeMap<Integer, TestCaseObjects> testCaseObjectMap)
	{
		CompletableFuture<?> [] completableFuture;

		/** declare the size of tasks which needs to be assigned */
		int maxAllowedTasks = 8;

		if(testCaseObjectMap.size() > maxAllowedTasks){
			completableFuture = new CompletableFuture<?> [maxAllowedTasks];
		}else{

			/** It should be testCaseObjectList.size still I am keeping half of test cases, to be tested ... then change */
			completableFuture = new CompletableFuture<?> [testCaseObjectMap.size()];	
		}

		logger.info("Configuring completable future task size: "+completableFuture.length);
		return completableFuture;
	}


	/** why this is required ?? actually after a test case object is executed, I need to put the executed object back into same static list
	 * but if I do that --> then list will keep on increasing as every time I add object in a list, it treats that a different object and this
	 * way, I would have twice of the actual size of objects in test case lis, therefore better to have a map and in that I can put the update object
	 * back in the same key. 
	 * 
	 * @param testCaseObjectList
	 * @return
	 */
	public TreeMap<Integer, TestCaseObjects> getTestCaseObjectMap(List<TestCaseObjects> testCaseObjectList)
	{
		TreeMap<Integer, TestCaseObjects> testCaseObjectMap = new TreeMap<>();

		for(int i=0; i<testCaseObjectList.size(); i++)
		{
			testCaseObjectMap.put(i, testCaseObjectList.get(i));
		}

		return testCaseObjectMap;
	}


	/** add separate method to execute test case and write respective results 
	 * 
	 * @param testCaseObject
	 * @return
	 */
	public synchronized Object executeTask(TreeMap<Integer, TestCaseObjects> testCaseObjectMapToBeExecuted, 
			Connection connectionServe, JSONObject jsonObjectRepo, File resultFile)
	{

		/** while execution, received map will always contain a single entry */
		TestCaseObjects testCaseObject = testCaseObjectMapToBeExecuted.firstEntry().getValue();

		/** setting up execution id */
		TestSuiteClass.UNIQ_EXECUTION_ID.set(testCaseObject.getTestCaseId());
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+": entering executing task method ... ");

			int testCaseKey = testCaseObjectMapToBeExecuted.firstEntry().getKey();

			new ReadTestCases().executeTestCaseObject(testCaseObject, connectionServe, jsonObjectRepo);
			new WriteTestResults().writeTestObjectResults(resultFile, testCaseObject);

			/** mark test case progress to 1 -- completed and add that back into global test object map */
			testCaseObject.setTestCaseExecutionProgressStatus(new AtomicInteger(1));
			ExecuteTestObjectsParallely.testCaseObjectMap.put(testCaseKey, testCaseObject);
		}
		catch(Exception e){
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get() +" : " +e.getMessage(), e);
		}
		return TestSuiteClass.UNIQ_EXECUTION_ID.get()+" - Test_Completed";
	}

}