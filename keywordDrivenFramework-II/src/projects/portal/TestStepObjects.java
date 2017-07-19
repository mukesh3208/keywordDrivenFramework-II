package projects.portal;

import org.apache.log4j.Logger; 

/** This class is a storage of object which contains the testCaseId, testStepId, data, keyword, objectName
 * The object of this class will contain all the information of a testStep
 * 
 * @author Pankaj
 *
 */


public class TestStepObjects {

	Logger logger = Logger.getLogger(TestStepObjects.class.getName());

	String testCaseId;
	String testStepId;

	String data;
	String keyword;
	String objectName;
	String testStepResult;

	int testStepIdRowNumber;
	
	public int getTestStepIdRowNumber() {
		return testStepIdRowNumber;
	}

	public void setTestStepIdRowNumber(int testStepIdRowNumber) {
		this.testStepIdRowNumber = testStepIdRowNumber;
	}

	public String getTestStepResult() {
		return testStepResult;
	}

	public void setTestStepResult(String testStepResult) {
		this.testStepResult = testStepResult;
	}

	public String getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getTestStepId() {
		return testStepId;
	}

	public void setTestStepId(String testStepId) {
		this.testStepId = testStepId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

}
