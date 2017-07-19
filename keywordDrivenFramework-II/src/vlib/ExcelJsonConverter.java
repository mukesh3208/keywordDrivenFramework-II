package vlib;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger; 
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;


import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;



public class ExcelJsonConverter 
{

	Logger logger = Logger.getLogger(ExcelJsonConverter.class.getName());

	public static void main(String[] args) throws JSONException, BiffException, IOException, SQLException 
	{

		test();

		/** convert excel sheet into json */
		//new ExcelJsonConverter().excel2Json("/Users/Pankaj/Desktop/qaAutomation/qascripting/Vdopia_Automation/tc_data/e2e/E2E_Test_Data.xls");

		/** convert json file to excel sheet */
		//new ExcelJsonConverter().json2Excel("/Users/Pankaj/Desktop/qaAutomation/qascripting/Vdopia_Automation/tc_data/e2e/E2E_Test_Data.json");
	}


	/** Convert all data in every sheet into json object and write it in a file 
	 * sample:
	 * {
	"sheet1": {
		"column-row1": "data",
		"column-row2": "data"
	}
	}	  
	 * @param filename
	 * @return
	 */
	public JSONObject excel2Json(String filename)
	{
		JSONObject jsonObj = new JSONObject();	

		try
		{
			Workbook book = Workbook.getWorkbook(new File(filename));
			String[] sheets = book.getSheetNames();

			for(int i=0; i<sheets.length; i++)
			{
				Sheet sheet = book.getSheet(sheets[i]);
				JSONObject json = getJson(book, sheet);

				/** adding location also along with sheet name */
				jsonObj.put(sheet.getName()+":"+i, json);
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}		

		/** write json object string in file */
		filename = filename.replace("xls", "json");
		File file = new File(filename);

		if(file.exists())
		{
			String dateTime = String.valueOf(new Date().getTime());
			file.renameTo(new File(filename.replace(".json", ".json".concat(dateTime))));
			System.out.println("existing json file renamed to something like: "+dateTime);
		}

		/** setting up formatting */
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(jsonObj.toString());
		String prettyJson = gson.toJson(je);
		//System.out.println(prettyJson);


		FileLib.WriteTextInFile(filename, prettyJson);		
		return jsonObj;
	}


	/** This method will convert the received excel sheet in json object, we need to find the primary key first and then this will handle everything.  
	 * 
	 * sample:
	 * {
	"column-row1": "data",
	"column-row2": "data"
	} 
	 * @param jsonObj
	 * @return
	 */
	public JSONObject getJson(Workbook book, Sheet sheet)
	{
		JSONObject jsonObj = new JSONObject();	

		try
		{
			for(int row =0; row<sheet.getRows(); row++)
			{	
				for(int column=0; column<sheet.getColumns(); column++)
				{
					/** get content of the above column */
					String data = sheet.getCell(column, row).getContents().trim();

					/** create key with coordinate as column:row and value will be the cell data */
					String key = column+":"+row;

					/** put into json object */
					jsonObj.put(key, data);
				}				
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}		

		return jsonObj;
	}


	/** This method is used by json2Excel method, this will create an empty excel sheet with desired sheets.
	 * 
	 * @param filename
	 * @return
	 */
	public String createExcelFileStructure (String filename)
	{
		String excelFile = filename.replace("json", "xls");

		try
		{
			/** delete the existing file */
			if(new File(excelFile).exists())
			{
				String renameFile = String.valueOf(new Date().getTime());
				new File(excelFile).renameTo(new File(excelFile.concat(renameFile)));

				System.out.println("existing excel file renamed to something like: "+renameFile );
			}

			/** get json string and from that get all sheet names  */
			JSONObject json = new JSONObject(FileLib.ReadContentOfFile(filename));
			Iterator<?> sheetNames = json.keys();

			/** create workbook */
			WritableWorkbook book = Workbook.createWorkbook(new File(excelFile));

			/** iterate each sheet and create it */
			while(sheetNames.hasNext())
			{
				/** every sheet name is appended with location, so split the location from sheet */
				String key = (String) sheetNames.next();
				String sheetName = key.split(":")[0];
				String sheetLocation = key.split(":")[1];

				/** create sheet with the received name and position */
				book.createSheet(sheetName, Integer.parseInt(sheetLocation));
			}

			book.write();
			book.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return excelFile;
	}


	/** This method will convert the supplied json into excel sheet, its two step process:
	 * 1. First create an empty excel with sheet and column names
	 * 2.Then fill all the data in respective sheet and column - row
	 * 
	 * @param filename
	 */
	public String json2Excel(String filename)
	{
		String excelFile = null;
		System.out.println(FileLib.ReadContentOfFile(filename));
		try
		{
			/** create json object from supplied json file */
			JSONObject jsonObject = null;
			try{
				jsonObject = new JSONObject(FileLib.ReadContentOfFile(filename));
			}catch(JSONException j){
				j.printStackTrace();
			}

			/** create the skeleton of supplied json in excel file */
			if(jsonObject != null)
			{
				/** first create the excel structure */
				excelFile = createExcelFileStructure(filename);

				Workbook book = Workbook.getWorkbook(new File(excelFile));
				WritableWorkbook copiedBook = Workbook.createWorkbook(new File(excelFile), book);

				/** iterating received json -- structure: 
					{"sheetname:sheetlocation": {"column:row": "data"}}
				 * */
				Iterator<?> jsonIterator = jsonObject.keys();
				while(jsonIterator.hasNext())
				{
					/** get sheet name, sheet name has location appended, therefore splitting it  */
					String sheetKey = (String) jsonIterator.next();
					String sheetName = sheetKey.split(":")[0];

					WritableSheet sheet = copiedBook.getSheet(sheetName);

					/** get json object of received sheetName, remember json object has key like : sheetname:location */
					JSONObject sheetData = jsonObject.getJSONObject(sheetKey);

					/** iterate the sheetData json object and which has key = "Column:Data" and Value= "Actual Data" */
					Iterator<?> sheetDataIterator = sheetData.keys();
					while(sheetDataIterator.hasNext())
					{
						/** get the key -- which is a y,x coordinate  */
						String key = (String) sheetDataIterator.next();
						String column = key.split(":")[0];
						String row = key.split(":")[1];

						/** get data */
						String data = sheetData.getString(key);

						/** adding data in respective sheets in above received y,x coordinates */
						Label label = new Label(Integer.parseInt(column), Integer.parseInt(row), data);
						sheet.addCell(label);
					}
				}
				copiedBook.write();
				copiedBook.close();
				book.close();

				System.out.println("Data is written ... ");
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return excelFile;
	}


	public static void test() throws BiffException, IOException, SQLException
	{
		try
		{
			Workbook book = Workbook.getWorkbook(new File("/Users/Pankaj/Desktop/results.xls"));
			Sheet sheet = book.getSheet(0);

			Connection connection = SaveResultsToMySql.getAutomationConnection();
			for(int i=1; i<sheet.getRows(); i++)
			{
				String average_cpu = sheet.getCell(9, i).getContents(); average_cpu = average_cpu.isEmpty() ? "0" :average_cpu;
				String average_memory= sheet.getCell(10, i).getContents(); average_memory = average_memory.isEmpty() ? "0" :average_memory;
				String average_response_time_ms= sheet.getCell(8, i).getContents(); average_response_time_ms = average_response_time_ms.isEmpty() ? "0" :average_response_time_ms;
				String bidder_depth= sheet.getCell(4, i).getContents(); bidder_depth = bidder_depth.isEmpty() ? "0" :bidder_depth;

				String concurrent_requests_applied= sheet.getCell(5, i).getContents(); concurrent_requests_applied = concurrent_requests_applied.isEmpty() ? "0" :concurrent_requests_applied;
				String duration_hrs= sheet.getCell(6, i).getContents();duration_hrs = duration_hrs.isEmpty() ? "0" :duration_hrs;
				String execution_date= sheet.getCell(3, i).getContents();execution_date = execution_date.isEmpty() ? "''" :execution_date;

				String fill_rate = sheet.getCell(12, i).getContents();fill_rate = fill_rate.isEmpty() ? "0" :fill_rate;
				fill_rate = fill_rate.replace("%", "");

				String machine_configuration = sheet.getCell(1, i).getContents();
				String number_requests_processed = sheet.getCell(7, i).getContents();number_requests_processed = number_requests_processed.isEmpty() ? "''" :number_requests_processed;
				number_requests_processed = number_requests_processed.replace(",", "").trim();
				String observations= sheet.getCell(13, i).getContents();
				String tag = sheet.getCell(0, i).getContents();
				String test_scenario = sheet.getCell(2, i).getContents();

				String throughput = sheet.getCell(11, i).getContents();	throughput = throughput.isEmpty() ? "0" :throughput;


				if(average_cpu.contains("="))
				{
					observations = observations.concat("\n CPU: "+average_cpu);
					average_cpu = average_cpu.toLowerCase().replace(" ", "");
					average_cpu = average_cpu.substring(average_cpu.indexOf("avg=")+4, average_cpu.length());
				}
				else if(average_cpu.contains(":"))
				{
					observations = observations.concat("\n CPU: "+average_cpu);
					average_cpu = average_cpu.toLowerCase().replace(" ", "");
					average_cpu = average_cpu.substring(average_cpu.indexOf("avg:")+4, average_cpu.length());
				}


				if(average_memory.contains("="))
				{
					observations = observations.concat("\n CPU: "+average_memory);
					average_memory = average_memory.toLowerCase().replace(" ", "");
					average_memory = average_memory.substring(average_memory.indexOf("avg=")+4, average_memory.length());
				}
				else if(average_memory.contains(":"))
				{
					observations = observations.concat("\n CPU: "+average_memory);
					average_memory = average_memory.toLowerCase().replace(" ", "");
					average_memory = average_memory.substring(average_memory.indexOf("avg:")+4, average_memory.length());
				}

				String sql = "INSERT INTO performanceResults "
						+ "( average_cpu, average_memory, average_response_time_ms, bidder_depth, concurrent_requests_applied, "+
						"duration_hrs, execution_date, fill_rate, machine_configuration, number_requests_processed, "
						+ "observations, tag, test_scenario, throughput ) " + 

					" VALUES ("+ average_cpu +" ,"+average_memory+" ,"+average_response_time_ms+" ,"+bidder_depth+" ,"+concurrent_requests_applied+" ,"
					+ duration_hrs +"," + "STR_TO_DATE('"+execution_date+"', '%d/%m/%Y') ," +fill_rate +"," +"'"+machine_configuration+"'"+","+number_requests_processed +","
					+ "'"+observations+"'"+ "," + "'"+tag +"'"+ "," + "'"+test_scenario+"'" + "," + throughput + ")";

				System.out.println();
				System.out.println(i+ "," + tag + " ==== " + sql);
				System.out.println();

				Statement statement = (Statement) connection.createStatement();
				statement.executeUpdate(sql);

			}		

			connection.close();
			book.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}



}
