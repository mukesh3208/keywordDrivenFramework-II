/**
 * Last Changes Done on Jan 29, 2015 2:25:31 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;





public class TestNgLib {

	static Logger logger = Logger.getLogger(TestNgLib.class.getName());
	
	public static void RunReRunTest(Map<String,ArrayList<String>> classNames, String logFileLocation, String logFileName) throws IOException
	{
		TestNG testng = new TestNG();
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put("browser","chrome");
		parameters.put("ReRun","Yes");
		parameters.put("logFileLocation",logFileLocation);
		parameters.put("logFileName", "ReRun_" + logFileName);
		parameters.put("serveSanityCampaign", "No");
		parameters.put("smokeTest","Yes");

		ArrayList<String> listeners = new ArrayList<String>();
		listeners.add("org.uncommons.reportng.HTMLReporter");
		listeners.add("org.uncommons.reportng.JUnitXMLReporter");

		XmlSuite suite = new XmlSuite();
		suite.setName("Vdopia_Automation");
		suite.setParallel(ParallelMode.FALSE);
		suite.setListeners(listeners);
		suite.setParameters(parameters);

		//Add Classes
		XmlTest adserveTest = new XmlTest(suite);
		adserveTest.setName("ReRun");
		adserveTest.setPreserveOrder("true");
		XmlClass Class;
		ArrayList<XmlClass> adserveclasses = new ArrayList<XmlClass>();

		for (Map.Entry<String, ArrayList<String>> entry : classNames.entrySet()) 
		{
			String runClassName = entry.getKey();
			ArrayList<String> runMethodNames = entry.getValue();
			Class = new XmlClass();
			Class.setName(runClassName);

			if(runMethodNames != null)
			{
				ArrayList<XmlInclude> methodsToRun = new ArrayList<XmlInclude>();
				for (String  method : runMethodNames)
				{
					methodsToRun.add(new XmlInclude(method));
				}
				Class.setIncludedMethods(methodsToRun);
			}
			adserveclasses.add(Class);
			adserveTest.setXmlClasses(adserveclasses);
		}

		//		for (String  runclassname : classNames)
		//		{
		//			// If the classname is portalcheck : then use include cases, otherwise simply add the class for rerun 
		//
		//
		//		}

		//Add the suite to the list of suites.
		List<XmlSuite> mySuites = new ArrayList<XmlSuite>();
		mySuites.add(suite);

		//Set the list of Suites to the testNG object you created earlier.
		testng.setXmlSuites(mySuites);

		//Printing in Log
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ===================================================================================");
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Now Running ReRun TestNG File as below: ");
		
		logger.info(suite.toXml());
		
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ===================================================================================");

		//invoke run() - this will run your class.
		testng.run();


		//		File file = new File("/Users/user/Desktop/work/qascripting/Vdopia_Automation/src/vlib/TestNG.xml");
		//		System.out.println("file"+file);
		//
		//		FileWriter writer = new FileWriter(file);
		//		writer.write(suite.toXml());
		//		writer.close(); 
	}

	//	public static void main(String args[]) 
	//	{
	//		doit();
	//	}
	//	public static void doit() 
	//	{
	//		System.out.println(StringLib.trace(Thread.currentThread().getStackTrace()));
	//	}


	//		Map<String, ArrayList<String>> rerunClassNames=new HashMap<String, ArrayList<String>>();
	//		ArrayList<String> methodNmaes = new ArrayList<String>();
	//		methodNmaes.add("niket1");
	//		methodNmaes.add("niket2");
	//		rerunClassNames.put("projects.adserve.mobileAdServe.MobileAdServingTests",methodNmaes);
	//		String logFileLocation="/Users/user/Desktop/work/qascripting/Vdopia_Automation/logs/06_04_2014";
	//		String logFileName = "06_04_2014_124152.txt";
	//		RunReRunTest(rerunClassNames, logFileLocation, logFileName);
	//	}

}