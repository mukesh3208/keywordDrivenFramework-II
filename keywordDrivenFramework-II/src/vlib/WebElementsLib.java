/**
 * Last Changes Done on 5 Mar, 2015 12:07:46 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import projects.TestSuiteClass;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;


public class WebElementsLib {

	static String objectRepositoryLocation = "";
	static String channelCampaignFlag = "";
	static WebDriver driver;

	//THIS IS THE CONSTRUCTOR - WHICH IS USED TO INITIALIZE THE VARIABLES BEING USED IN THIS CLASS.
	@SuppressWarnings("static-access")
	public WebElementsLib(WebDriver driver, String repositoryLocation, String channelCampaignNotifier)
	{
		objectRepositoryLocation = repositoryLocation;
		channelCampaignFlag = channelCampaignNotifier;
		this.driver = driver;

		System.out.println("Constructor WebElementsLib is setting Object Repository: " +objectRepositoryLocation  + " For Module: "+ channelCampaignFlag + " And Driver: "+driver.toString());
	}


	//This method identifies a single web element, it accepts inputs as - Element Name, Identifier Method And Identifier Value
	//To Be Used For Identification from Repository sheet.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static WebElement FindSingleWebElement (String webElementNameFromRepository, String webElementIdentifierByMethod, String webElementIdentifierValue)   
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException 
			{
		By byObjectToInvokeMethod = null;
		By byObjectCollectWebElement = null;

		WebElement webElementFound = null;

		String locationToSaveSceenShot = TestSuiteClass.AUTOMATION_HOME.concat("/screenshots/").concat(GenericMethodsLib.DateTimeStamp("MMddyy")).concat("/").concat("Webelement_"+GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss")+".png");

		try
		{
			//Get By Class Dynamically
			Class byClass = Class.forName(By.class.getName());

			//Get By Method - to be used while finding web elements
			Method byMethod = byClass.getMethod(webElementIdentifierByMethod, String.class);

			//Invoke By Method like By.cssSelector("v") and collect value
			byObjectCollectWebElement = (By) byMethod.invoke(byObjectToInvokeMethod, webElementIdentifierValue); 

			//Get Webdriver class dynamically
			Class webDriverClass = Class.forName(WebDriver.class.getName());

			//Get Webdriver Method - to be used while finding web elements
			Method webDriverMethod = webDriverClass.getMethod("findElement", new Class[]{By.class});

			//Invoke WebDriver method like w.findElement(By.cssSelector("v")) and collect Web Element 
			webElementFound = (WebElement) webDriverMethod.invoke(driver, byObjectCollectWebElement);

		}
		catch (Exception e) 
		{
			//Capture Screenshot
			CaptureScreenShotLib.captureScreenShot(driver, locationToSaveSceenShot);

			System.out.println("Exception Handled By Method - WebElementsLib.FindSingleWebElement For Web Element Having Repository Name: " +webElementNameFromRepository +" And Definition: " +webElementIdentifierValue + ", Identifier: " +webElementIdentifierByMethod);
			System.out.println(e.getMessage());
			Assert.fail("Exception Handled By Method - WebElementsLib.FindSingleWebElement For Web Element Having Repository Name: " +webElementNameFromRepository +" And Definition: " +webElementIdentifierValue + ", Identifier: " +webElementIdentifierByMethod);
		}

		return webElementFound;

		/*	Sample Code To Understand Reflection	***** 
	  WebDriver w = null;
	  w.findElement(By.cssSelector("v"));
	  w.findElement(By.tagName("name"));
		 */

			}


	//This Method Identifies All The Web Elements Present In The Supplied Screen (From Object Repository) And Returns Them In A <WebElement>List.
	public static List<WebElement> FindPageWebElements (String pageName)
			throws BiffException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
			{

		String locationToSaveSceenShot = TestSuiteClass.AUTOMATION_HOME.concat("/screenshots/").concat(GenericMethodsLib.DateTimeStamp("MMddyy")).concat("/").concat("Webelement_"+GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss")+".png");

		//Object Repository Sheet Location Will be set by calling the WebElementsLib constructor.
		Workbook wb = Workbook.getWorkbook(new File(objectRepositoryLocation));
		Sheet sheet = wb.getSheet(0);

		String []arrFieldsFromRepository ={"Fields", "Identifier", "Value"};
		int moduleColumn = sheet.findCell("Modules").getColumn(); 

		//**** This Code Will Read The First And Last Line Line Containing String - Channel or Campaign, And Constructor will be called to set Campaign or Channel

		//int channelcampaignStartRow = sheet.findCell("Channel").getRow();
		//int channelcampaignEndRow = sheet.findCell("Channel", channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();
		//System.out.println("channelcampaignStartRow: " +channelcampaignStartRow + "channelcampaignEndRow: " +channelcampaignEndRow);

		int channelcampaignColumn = sheet.findCell("Channel/Campaign").getColumn();
		int channelcampaignStartRow = sheet.findCell(channelCampaignFlag).getRow();
		int channelcampaignEndRow = sheet.findCell(channelCampaignFlag, channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();

		//********************************************************************************

		int pageStartRow = 0;
		int pageEndRow = 0;

		try
		{
			//pageStartRow = sheet.findCell(pageName).getRow();
			//pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, sheet.getRows(), false).getRow();

			//************** This Code Will Read The First And Last Line Line Containing The Supplied String pageName ****************

			pageStartRow = sheet.findCell(pageName, moduleColumn, channelcampaignStartRow, moduleColumn, channelcampaignEndRow, false).getRow();

			pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, channelcampaignEndRow, false).getRow();

			//********************************
		}
		catch(NullPointerException n)
		{
			pageEndRow = pageStartRow;
			//System.out.println("In Case Of Only One Element In A Page: "+ pageName +" - Null Pointer Exception Handled By Method: WebElementsLib.FindPageWebElements");
		}
		catch(Exception n)
		{
			System.out.println("Exception Handled By Method: WebElementsLib.FindPageWebElements " +n.getMessage());
		}


		String [][] arrWebElements = new String[(pageEndRow-pageStartRow)+1][arrFieldsFromRepository.length];

		for(int i=pageStartRow;i<=pageEndRow;i++)
		{
			for(int j=0;j<arrFieldsFromRepository.length;j++)
			{  	
				arrWebElements[i-pageStartRow][j] = sheet.getCell(sheet.findLabelCell(arrFieldsFromRepository[j]).getColumn(), i).getContents().toString().trim();
				//System.out.print(arrWebElements[i-pageStartRow][j] + "   ");
			}
			//System.out.println();
		}

		//Closing The Workbook after reading values.
		wb.close();

		List<WebElement> webElementList = new ArrayList<WebElement>(); 

		//Reading The Array Containing The webElements, Finding These Elements And Adding Them Is List.
		for (int i=0;i<arrWebElements.length;i++)
		{
			try
			{
				//After Identification of Web Elements and Adding Them In A List And Returning This As <WebElement>List.
				webElementList.add(i,FindSingleWebElement(arrWebElements[i][0], arrWebElements[i][1], arrWebElements[i][2]));
				System.out.println("webElement: " +webElementList.get(i) + " is present on supplied screen: " +pageName);
			}
			catch (Exception e) 
			{
				//Capture Screenshot
				CaptureScreenShotLib.captureScreenShot(driver, locationToSaveSceenShot);

				System.out.println("Exception Handled By Method - WebElementsLib.FindPageWebElements. Repository Field: " +arrWebElements[i][0] +" was not found on supplied screen: "+pageName);
				Assert.fail("Exception Handled By Method - WebElementsLib.FindPageWebElements. Repository Field: " +arrWebElements[i][0] +" was not found on supplied screen: "+pageName);
			}
		}

		return webElementList;

			}


	//This Method Identifies All The Web Elements Present In The Supplied Screen (From Object Repository) And Returns Elements With Labels In A <WebElement, String> HashMap.
	public static HashMap<WebElement, String> FindPageWebElementsWithLabel (String pageName)
			throws BiffException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
			{
		//Object Repository Sheet Location Will be set by calling the WebElementsLib constructor.
		//Workbook wb = Workbook.getWorkbook(new File("/Users/user/Desktop/Automation/qascripting/Vdopia_Automation/object_repository/mobileObjectRepository/mobile_ObjectRepository.xls"));
		Workbook wb = Workbook.getWorkbook(new File(objectRepositoryLocation));
		Sheet sheet = wb.getSheet(0);

		String locationToSaveSceenShot = TestSuiteClass.AUTOMATION_HOME.concat("/screenshots/").concat(GenericMethodsLib.DateTimeStamp("MMddyy")).concat("/").concat("Webelement_"+GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss")+".png");

		String []arrFieldsFromRepository ={"Fields", "Identifier", "Value", "Labels"};
		int moduleColumn = sheet.findCell("Modules").getColumn(); 

		//**** This Code Will Read The First And Last Line Line Containing String - Channel or Campaign, And Constructor will be called to set Campaign or Channel

		//int channelcampaignStartRow = sheet.findCell("Channel").getRow();
		//int channelcampaignEndRow = sheet.findCell("Channel", channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();
		//System.out.println("channelcampaignStartRow: " +channelcampaignStartRow + "channelcampaignEndRow: " +channelcampaignEndRow);

		int channelcampaignColumn = sheet.findCell("Channel/Campaign").getColumn();
		int channelcampaignStartRow = sheet.findCell(channelCampaignFlag).getRow();
		int channelcampaignEndRow = sheet.findCell(channelCampaignFlag, channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();

		//********************************************************************************

		int pageStartRow = 0;
		int pageEndRow = 0;

		try
		{
			//pageStartRow = sheet.findCell(pageName).getRow();
			//pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, sheet.getRows(), false).getRow();

			//************** This Code Will Read The First And Last Line Line Containing The Supplied String pageName ****************

			pageStartRow = sheet.findCell(pageName, moduleColumn, channelcampaignStartRow, moduleColumn, channelcampaignEndRow, false).getRow();
			pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, channelcampaignEndRow, false).getRow();

			//********************************

		}catch(NullPointerException n)
		{
			pageEndRow = pageStartRow;
			//System.out.println("In Case Of Only One Element In A Page: "+ pageName +" - Null Pointer Exception Handled By Method: WebElementsLib.FindPageWebElements");
		}

		String [][] arrWebElements = new String[(pageEndRow-pageStartRow)+1][arrFieldsFromRepository.length];

		for(int i=pageStartRow;i<=pageEndRow;i++)
		{
			for(int j=0;j<arrFieldsFromRepository.length;j++)
			{  	
				arrWebElements[i-pageStartRow][j] = sheet.getCell(sheet.findLabelCell(arrFieldsFromRepository[j]).getColumn(), i).getContents().toString().trim();
				//System.out.print(arrWebElements[i-pageStartRow][j] + "   ");
			}
			System.out.println();
		}

		wb.close();

		//List<WebElement> webElementList = new ArrayList<WebElement>(); 
		HashMap<WebElement,	String> webElementWithLabelhashMap = new HashMap<WebElement, String>(); 

		for (int i=0;i<arrWebElements.length;i++)
		{
			try
			{
				//After Identification of Web Elements and Adding Them In A List And Returning This <WebElement>List.
				//webElementList.add(i,FindSingleWebElement(arrWebElements[i][1], arrWebElements[i][2]));

				//After Identification of Web Elements and Adding Them In A Hash Map And Returning This <WebElement> Hash Map.
				webElementWithLabelhashMap.put(FindSingleWebElement(arrWebElements[i][0], arrWebElements[i][1], arrWebElements[i][2]), arrWebElements[i][3].trim());
			}
			catch (Exception e) 
			{
				//Capture Screenshot
				CaptureScreenShotLib.captureScreenShot(driver, locationToSaveSceenShot);

				System.out.println("NoSuchElementException Handled By Method - WebElementsLib.FindPageWebElementsWithLabel. Repository Field: " +arrWebElements[i][0] +" And Label: " + arrWebElements[i][3] +" was not found on supplied screen: "+pageName);
				Assert.fail("NoSuchElementException Handled By Method - WebElementsLib.FindPageWebElementsWithLabel. Repository Field: " +arrWebElements[i][0] +" And Label: " + arrWebElements[i][3] +" was not found on supplied screen: "+pageName);
			}
		}
		return webElementWithLabelhashMap;	

			}


	//This Method Identifies All The Web Elements Present In The Supplied Screen (From Object Repository) And Returns single Element having the matching Field name.
	public static WebElement FindWebElementByFieldName(String pageName, String fieldName)
			throws BiffException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
			{

		String locationToSaveSceenShot = TestSuiteClass.AUTOMATION_HOME.concat("/screenshots/").concat(GenericMethodsLib.DateTimeStamp("MMddyy")).concat("/").concat("Webelement_"+GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss")+".png");

		//Object Repository Sheet Location Will be set by calling the WebElementsLib constructor.
		//Workbook wb = Workbook.getWorkbook(new File("/Users/user/Desktop/Automation/qascripting/Vdopia_Automation/object_repository/mobileObjectRepository/mobile_ObjectRepository.xls"));
		Workbook wb = Workbook.getWorkbook(new File(objectRepositoryLocation));
		Sheet sheet = wb.getSheet(0);

		String []arrFieldsFromRepository ={"Fields", "Identifier", "Value", "Labels"};
		int moduleColumn = sheet.findCell("Modules").getColumn(); 

		//**** This Code Will Read The First And Last Line Line Containing String - Channel or Campaign, And Constructor will be called to set Campaign or Channel

		//int channelcampaignStartRow = sheet.findCell("Channel").getRow();
		//int channelcampaignEndRow = sheet.findCell("Channel", channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();
		//System.out.println("channelcampaignStartRow: " +channelcampaignStartRow + "channelcampaignEndRow: " +channelcampaignEndRow);

		int channelcampaignColumn = sheet.findCell("Channel/Campaign").getColumn();
		int channelcampaignStartRow = sheet.findCell(channelCampaignFlag).getRow();
		int channelcampaignEndRow = sheet.findCell(channelCampaignFlag, channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();

		//********************************************************************************

		int pageStartRow = 0;
		int pageEndRow = 0;

		try
		{
			//pageStartRow = sheet.findCell(pageName).getRow();
			//pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, sheet.getRows(), false).getRow();

			//************** This Code Will Read The First And Last Line Line Containing The Supplied String pageName ****************

			pageStartRow = sheet.findCell(pageName, moduleColumn, channelcampaignStartRow, moduleColumn, channelcampaignEndRow, false).getRow();
			pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, channelcampaignEndRow, false).getRow();

			//********************************

		}catch(NullPointerException n)
		{
			pageEndRow = pageStartRow;
			//System.out.println("In Case Of Only One Element In A Page: "+ pageName +" - Null Pointer Exception Handled By Method: WebElementsLib.FindPageWebElements");
		}

		String [][] arrWebElements = new String[(pageEndRow-pageStartRow)+1][arrFieldsFromRepository.length];

		for(int i=pageStartRow;i<=pageEndRow;i++)
		{
			for(int j=0;j<arrFieldsFromRepository.length;j++)
			{  	
				arrWebElements[i-pageStartRow][j] = sheet.getCell(sheet.findLabelCell(arrFieldsFromRepository[j]).getColumn(), i).getContents().toString().trim();
				//System.out.print(arrWebElements[i-pageStartRow][j] + "   ");
			}
			System.out.println();
		}

		wb.close();

		//List<WebElement> webElementList = new ArrayList<WebElement>(); 
		HashMap<String, WebElement> webElementWithFieldsHashMap = new HashMap<String, WebElement>(); 

		for (int i=0;i<arrWebElements.length;i++)
		{
			try
			{
				//After Identification of Web Elements and Adding Them In A Hash Map 
				webElementWithFieldsHashMap.put(arrWebElements[i][0], FindSingleWebElement(arrWebElements[i][0], arrWebElements[i][1], arrWebElements[i][2]));
			}
			catch (Exception e) 
			{
				//Capture Screenshot
				CaptureScreenShotLib.captureScreenShot(driver, locationToSaveSceenShot);

				System.out.println("Exception Handled By Method - WebElementsLib.FindSignleElementOutOfPage. Repository Field: " +arrWebElements[i][0] +" And Label: " + arrWebElements[i][3] +" was not found on supplied screen: "+pageName);
				Assert.fail("Exception Handled By Method - WebElementsLib.FindSignleElementOutOfPage. Repository Field: " +arrWebElements[i][0] +" And Label: " + arrWebElements[i][3] +" was not found on supplied screen: "+pageName);
			}
		}


		WebElement webelement = null;
		//finding Returning single web element out of found Hash Map of web elements. 
		for (Map.Entry<String, WebElement> entry : webElementWithFieldsHashMap.entrySet()) 
		{
			if (fieldName.equalsIgnoreCase(entry.getKey())) 
			{
				webelement = entry.getValue();
				System.out.println(webelement.toString() + " is being returned.");
				break;
			}
		}	

		return webelement;	

			}


	//This Method Identifies All The Web Elements Present In The Supplied Screen (From Object Repository) And Returns single Element having the matching Field name.
	public static String GetWebElementLocatorValueByFieldName(String pageName, String fieldName) throws BiffException, IOException		
	{

		Workbook wb = Workbook.getWorkbook(new File(objectRepositoryLocation));
		Sheet sheet = wb.getSheet(0);

		String []arrFieldsFromRepository ={"Fields", "Value"};
		int moduleColumn = sheet.findCell("Modules").getColumn(); 

		//**** This Code Will Read The First And Last Line Line Containing String - Channel or Campaign, And Constructor will be called to set Campaign or Channel

		//int channelcampaignStartRow = sheet.findCell("Channel").getRow();
		//int channelcampaignEndRow = sheet.findCell("Channel", channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();
		//System.out.println("channelcampaignStartRow: " +channelcampaignStartRow + "channelcampaignEndRow: " +channelcampaignEndRow);

		int channelcampaignColumn = sheet.findCell("Channel/Campaign").getColumn();
		int channelcampaignStartRow = sheet.findCell(channelCampaignFlag).getRow();
		int channelcampaignEndRow = sheet.findCell(channelCampaignFlag, channelcampaignColumn, channelcampaignStartRow+1, channelcampaignColumn, sheet.getRows(), false).getRow();

		//********************************************************************************

		int pageStartRow = 0;
		int pageEndRow = 0;

		try
		{
			//pageStartRow = sheet.findCell(pageName).getRow();
			//pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, sheet.getRows(), false).getRow();

			//************** This Code Will Read The First And Last Line Line Containing The Supplied String pageName ****************

			pageStartRow = sheet.findCell(pageName, moduleColumn, channelcampaignStartRow, moduleColumn, channelcampaignEndRow, false).getRow();
			pageEndRow = sheet.findCell(pageName, moduleColumn, pageStartRow+1, moduleColumn, channelcampaignEndRow, false).getRow();

			//********************************

		}catch(NullPointerException n)
		{
			pageEndRow = pageStartRow;
			//System.out.println("In Case Of Only One Element In A Page: "+ pageName +" - Null Pointer Exception Handled By Method: WebElementsLib.FindPageWebElements");
		}

		//Store Field and Locator in HashMap 
		HashMap<String, String> webElementWithFieldsHashMap = new HashMap<String, String>();

		for(int i=pageStartRow;i<=pageEndRow;i++)
		{
			String key = sheet.getCell(sheet.findLabelCell(arrFieldsFromRepository[0]).getColumn(), i).getContents().toString().trim();
			String value = sheet.getCell(sheet.findLabelCell(arrFieldsFromRepository[1]).getColumn(), i).getContents().toString().trim();

			webElementWithFieldsHashMap.put(key, value);

			//System.out.print(arrWebElements[i-pageStartRow][j] + "   ");
		}

		wb.close();


		String elementLocatorValue = null;

		//finding Returning single web element out of found Hash Map of web elements. 
		for (Map.Entry<String, String> entry : webElementWithFieldsHashMap.entrySet()) 
		{
			if (fieldName.equalsIgnoreCase(entry.getKey())) 
			{
				elementLocatorValue = entry.getValue();
				System.out.println(elementLocatorValue + " is being returned.");
				break;
			}
		}	

		return elementLocatorValue;	
	}


	// This method will validate presence of screen.
	public static boolean ValidateScreen(String pageName)
	{
		String locationToSaveSceenShot = TestSuiteClass.AUTOMATION_HOME.concat("/screenshots/").concat(GenericMethodsLib.DateTimeStamp("MMddyy")).concat("/").concat("Webelement_"+GenericMethodsLib.DateTimeStamp("MMddyy_hhmmss")+".png");

		boolean status = false;
		try
		{
			List<WebElement> element = FindPageWebElements(pageName);
			if(element.size() == 0)
			{
				status = false;
				System.out.println("No Element is present in Object repository for Screen: " + pageName);
			}
			else
			{

				status = true;
				System.out.println("Screen: " + pageName + " is successfully validated");
			}
		}
		catch(Exception e)
		{
			//Capture Screenshot
			CaptureScreenShotLib.captureScreenShot(driver, locationToSaveSceenShot);

			status = false;
			System.out.println("Unable to Validate Screen.");
			System.out.println("Exception occured in Function ValidateScreen. Execption occured is: " + e.getMessage());
		}
		return status;
	}

}
