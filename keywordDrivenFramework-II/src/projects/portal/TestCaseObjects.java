package projects.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class TestCaseObjects {

	String testCaseId;
	String testCaseResult;

	AtomicInteger testCaseExecutionProgressStatus;
	AtomicBoolean ifTestCaseQueued;

	List<TestStepObjects> testStepObjectsList = new ArrayList<>();

	int testCaseIdRowNumber;

	
	public int getTestCaseIdRowNumber() {
		return testCaseIdRowNumber;
	}

	public void setTestCaseIdRowNumber(int testCaseIdRowNumber) {
		this.testCaseIdRowNumber = testCaseIdRowNumber;
	}

	public AtomicBoolean getIfTestCaseQueued() {
		return ifTestCaseQueued;
	}

	/** atomic flag = false means - not queued for execution and true means queued, false is set at the time of loading test case objects
	 * and true is set at the time of queuing the test case
	 * 
	 * @param ifTestCaseQueued
	 */
	public void setIfTestCaseQueued(AtomicBoolean ifTestCaseQueued) {
		this.ifTestCaseQueued = ifTestCaseQueued;
	}

	public String getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getTestCaseResult() {
		return testCaseResult;
	}

	public void setTestCaseResult(String testCaseResult) {
		this.testCaseResult = testCaseResult;
	}

	public AtomicInteger getTestCaseExecutionProgressStatus() {
		return testCaseExecutionProgressStatus;
	}

	/** atomic integer = 0 means - in progress and 1 means completed, this value to be set at the of execution. 
	 * 
	 * @param testCaseExecutionProgressStatus
	 */
	public void setTestCaseExecutionProgressStatus(AtomicInteger testCaseExecutionProgressStatus) {
		this.testCaseExecutionProgressStatus = testCaseExecutionProgressStatus;
	}

	public List<TestStepObjects> gettestStepObjectsList() {
		return testStepObjectsList;
	}

	public void settestStepObjectsList(List<TestStepObjects> testStepObjectsList) {
		this.testStepObjectsList = testStepObjectsList;
	}

}