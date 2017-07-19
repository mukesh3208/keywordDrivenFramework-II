/**
 * Last Changes Done on Jan 27, 2015 1:17:05 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */

package vlib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 




import vlib.XlsLib;


public class Excel2Html 
{

	static Logger logger = Logger.getLogger(Excel2Html.class.getName());
	
	public static String width = "4500";

	//This method will convert existing excel sheet into html file.
	@SuppressWarnings("finally")
	public static boolean GenerateWebServiceHTMLResult(String excelFile, String desiredHtmlFile, String dateTimeStamp)
	{
		boolean flag = false;
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Excel File: "+excelFile + " is being converted into HTML File... ");
			
			String Data[][] = FileLib.FetchDataFromExcelSheetWithColumnNames(excelFile, "Campaign_ID", "ADS_ID", "Test_Results");
			
			
			//Get desired html file directory and check if directory exists, if not create it.
			String desiredHTMLFileDir = StringLib.splitDirectoryFromFileLocation(desiredHtmlFile);

			if(!(new File(desiredHTMLFileDir).exists()))
			{
				File dir = new File(desiredHTMLFileDir);

				if(!dir.mkdirs())
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Desired HTML Directory: "+dir.toString() + " wasn't created. " );
				}

			}


			File htmlfile = new File(desiredHtmlFile);

			// if file does not exists, then create it
			if (!htmlfile.exists()) 
			{
				if(htmlfile.createNewFile())
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Created New File: "+htmlfile);
				}
				else
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Couldn't Create File: "+htmlfile);
				}
			}

			FileWriter fw = new FileWriter(htmlfile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			//bw.write(admValue);
			String HTML_header = "<html> \n" +
					"<head> \n" +
					"<title>Result Page</title> \n" +
					"<style> \n" + 
					"table,th,td" + "{" +
					"border:1px solid black;" + 
					"border-collapse:collapse;" + 
					"}" + 
					"</style>" +
					"</head> \n" +
					"<body> \n" +
					"<h2 align=\"center\">Adserving Result</h2> \n" +
					"<table style=\"width:300px\" align=\"center\"> \n";

			String HTML_Footer = "</table> \n <h5 align=\"center\">Generated at: "+ dateTimeStamp + "</h5> \n </body>\n </html>\n";

			bw.write(HTML_header);

			for(int i=0; i<Data.length; i++)		// For Every Row
			{
				bw.write("<tr>\n");
				for(int j=0; j<Data[0].length; j++)	// For Every Column
				{
					
					
					if(Data[0][j].equalsIgnoreCase("Campaign_ID"))
					{
						if(i == 0)
						{
							//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
							bw.write("<th bgcolor=\"C0C0C0\">" + "Campaign_ID" + " </th>\n");
						}
						else
						{
							if(Data[i][j+2].toLowerCase().contains("fail"))
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FD8A88\">" + Data[i][j] + "</td>\n");	
							}
							else if(Data[i][j+2].toLowerCase().contains("pass"))
							{	
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"95EB9A\">" + Data[i][j] + "</td>\n");
							}
							else
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FBF8A6\">" + Data[i][j] + "</td>\n");
							}
						}
					}
					if(Data[0][j].equalsIgnoreCase("ADS_ID"))
					{
						if(i == 0)
						{
							//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
							bw.write("<th bgcolor=\"C0C0C0\">" + "ADS_ID" + " </th>\n");
						}
						else
						{
							if(Data[i][j+1].toLowerCase().contains("fail"))
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FD8A88\">" + Data[i][j] + "</td>\n");	
							}
							else if(Data[i][j+1].toLowerCase().contains("pass"))
							{	
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"95EB9A\">" + Data[i][j] + "</td>\n");
							}
							else
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FBF8A6\">" + Data[i][j] + "</td>\n");
							}
						}
					}
					if(Data[0][j].equalsIgnoreCase("Test_Results"))
					{
						if(i == 0)
						{
							//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
							bw.write("<th bgcolor=\"C0C0C0\">" + "Results" + " </th>\n");
						}
						else
						{
							if(Data[i][j].toLowerCase().contains("fail"))
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FD8A88\">" + Data[i][j] + "</td>\n");	
							}
							else if(Data[i][j].toLowerCase().contains("pass"))
							{	
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"95EB9A\">" + Data[i][j] + "</td>\n");
							}
							else
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FBF8A6\">" + Data[i][j] + "</td>\n");
							}
						}
					}

					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Printing Elements : "+strRecord[i][j]);
				}
				bw.write("</tr>\n");
			}

			bw.write(HTML_Footer);
			bw.close();

			flag = true;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Excel File: "+excelFile + " is converted into HTML File: "+desiredHtmlFile);
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Excel File: "+excelFile + " is not converted into HTML File. ", e);
		}
		finally
		{
			return flag;
		}
	}


	/*

	//This method will convert existing excel sheet into html file.
	@SuppressWarnings("finally")
	public static boolean GenerateWebServiceHTMLResult(String excelFile, String desiredHtmlFile, String dateTimeStamp)
	{
		boolean flag = false;
		try
		{
			logger.info();
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Excel File: "+excelFile + " is being converted into HTML File... ");

			XlsLib result = new XlsLib();
			String Data[][] = result.dataFromExcel(excelFile);

			//Get desired html file directory and check if directory exists, if not create it.
			String desiredHTMLFileDir = StringLib.splitDirectoryFromFileLocation(desiredHtmlFile);

			if(!(new File(desiredHTMLFileDir).exists()))
			{
				File dir = new File(desiredHTMLFileDir);

				if(!dir.mkdirs())
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Desired HTML Directory: "+dir.toString() + " wasn't created. " );
				}

			}


			File htmlfile = new File(desiredHtmlFile);

			// if file does not exists, then create it
			if (!htmlfile.exists()) 
			{
				if(htmlfile.createNewFile())
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Created New File: "+htmlfile);
				}
				else
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Couldn't Create File: "+htmlfile);
				}
			}

			FileWriter fw = new FileWriter(htmlfile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			//bw.write(admValue);
			String HTML_header = "<html> \n" +
					"<head> \n" +
					"<title>Result Page</title> \n" +
					"<style> \n" + 
					"table,th,td" + "{" +
					"border:1px solid black;" + 
					"border-collapse:collapse;" + 
					"}" + 
					"</style>" +
					"</head> \n" +
					"<body> \n" +
					"<h2 align=\"center\">Adserving Result</h2> \n" +
					"<table style=\"width:300px\" align=\"center\"> \n";

			String HTML_Footer = "</table> \n <h5 align=\"center\">Generated at: "+ dateTimeStamp + "</h5> \n </body>\n </html>\n";

			bw.write(HTML_header);

			for(int i=0; i<Data.length; i++)		// For Every Row
			{
				bw.write("<tr>\n");
				for(int j=0; j<Data[0].length; j++)	// For Every Column
				{
					if(Data[0][j].equalsIgnoreCase("Test_Description"))
					{
						if(i == 0)
						{
							//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
							bw.write("<th bgcolor=\"C0C0C0\">" + "Description" + " </th>\n");
						}
						else
						{
							if(Data[i][j+1].toLowerCase().contains("fail"))
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FD8A88\">" + Data[i][j] + "</td>\n");	
							}
							else if(Data[i][j+1].toLowerCase().contains("pass"))
							{	
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"95EB9A\">" + Data[i][j] + "</td>\n");
							}
							else
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FBF8A6\">" + Data[i][j] + "</td>\n");
							}
						}
					}
					if(Data[0][j].equalsIgnoreCase("Test_Results"))
					{
						if(i == 0)
						{
							//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
							bw.write("<th bgcolor=\"C0C0C0\">" + "Results" + " </th>\n");
						}
						else
						{
							if(Data[i][j].toLowerCase().contains("fail"))
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FD8A88\">" + Data[i][j] + "</td>\n");	
							}
							else if(Data[i][j].toLowerCase().contains("pass"))
							{	
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"95EB9A\">" + Data[i][j] + "</td>\n");
							}
							else
							{
								//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
								bw.write("<td bgcolor=\"FBF8A6\">" + Data[i][j] + "</td>\n");
							}
						}
					}

					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Printing Elements : "+strRecord[i][j]);
				}
				bw.write("</tr>\n");
			}

			bw.write(HTML_Footer);
			bw.close();

			flag = true;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Excel File: "+excelFile + " is converted into HTML File: "+desiredHtmlFile);
		}
		catch(Exception e)
		{
			flag = false;
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Excel File: "+excelFile + " is not converted into HTML File. ");
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: GenerateWebServiceHTMLResult. "+e.getMessage());
		}
		finally
		{
			return flag;
		}
	}

	*/
	
	
	
	@SuppressWarnings("finally")
	public static boolean GenerateResultExcelintoHTML(String excelFile, String desiredHtmlFile)
	{
		String dateTimeStamp = GenericMethodsLib.DateTimeStamp("dd-MMM-yyyy hh:mm:ss");
		boolean flag = false;
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Process Start => Excel File: "+excelFile + " is being converted into HTML File : " + desiredHtmlFile);

			XlsLib result = new XlsLib();
			String Data[][] = result.dataFromExcel(excelFile);

			File htmlfile = new File(desiredHtmlFile);

			// if file does not exists, then create it
			if (!htmlfile.exists()) 
			{
				htmlfile.createNewFile();
			}

			FileWriter fw = new FileWriter(htmlfile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			//bw.write(admValue);
			String HTML_header = "<html> \n" +
					"<head> \n" +
					"<title>Result Page</title> \n" +
					"<style> \n" + 
					"body { \n" +
					//"width: 600px; \n" + 
					"margin: 40px auto; \n" +
					"font-family: 'trebuchet MS', 'Lucida sans', Arial;  \n" +
					"font-size: 18px;  \n" +
					"color: #444; \n" +
					"} \n" + 

					"table { \n" + 
					"   *border-collapse: collapse;  \n" + 
					"   border-spacing: 0;  \n" + 
					//"   width: 100%;     \n" + 
					"}  \n" + 

					".bordered {  \n" + 
					"   border: solid #ccc 1px; \n" + 
					"   -moz-border-radius: 6px;  \n" + 
					" -webkit-border-radius: 6px;  \n" + 
					"  border-radius: 6px;  \n" + 
					"   -webkit-box-shadow: 0 1px 1px #ccc;  \n" +  
					"   -moz-box-shadow: 0 1px 1px #ccc;   \n" + 
					"    box-shadow: 0 1px 1px #ccc;  \n" + 
					"}  \n" + 

					//".bordered tr:hover {  \n" +
					//"    background: #fbf8e9;  \n" +
					//"    -o-transition: all 0.1s ease-in-out;  \n" +
					//"    -webkit-transition: all 0.1s ease-in-out;  \n" +
					//"    -moz-transition: all 0.1s ease-in-out;  \n" +
					//"    -ms-transition: all 0.1s ease-in-out;  \n" +
					//"    transition: all 0.1s ease-in-out;       \n" +
					//"}      \n" +

					".bordered td, .bordered th {   \n" +
					"    border-left: 1px solid #ccc;  \n" +
					"    border-top: 1px solid #ccc;  \n" +
					"    padding: 10px;  \n" +
					"    text-align: left;     \n" +
					"}  \n" +

					".bordered th {   \n" +
					"    background-color: #dce9f9;  \n" +
					"    background-image: -webkit-gradient(linear, left top, left bottom, from(#ebf3fc), to(#dce9f9));  \n" +
					"    background-image: -webkit-linear-gradient(top, #ebf3fc, #dce9f9);  \n" +
					"    background-image:    -moz-linear-gradient(top, #ebf3fc, #dce9f9);  \n" +
					"    background-image:     -ms-linear-gradient(top, #ebf3fc, #dce9f9);  \n" +
					"    background-image:      -o-linear-gradient(top, #ebf3fc, #dce9f9);  \n" +
					"    background-image:         linear-gradient(top, #ebf3fc, #dce9f9);  \n" +
					"    -webkit-box-shadow: 0 1px 0 rgba(255,255,255,.8) inset;  \n" +
					"    -moz-box-shadow:0 1px 0 rgba(255,255,255,.8) inset;   \n" +
					"    box-shadow: 0 1px 0 rgba(255,255,255,.8) inset;         \n" +
					"    border-top: none;  \n" +
					"    text-shadow: 0 1px 0 rgba(255,255,255,.5);  \n" +
					"}  \n" +

					".bordered td:first-child, .bordered th:first-child {      \n" +
					"    border-left: none;  \n" +
					"}  \n" +
					".bordered th:first-child {  \n" +
					"    -moz-border-radius: 6px 0 0 0;  \n" +
					"    -webkit-border-radius: 6px 0 0 0;  \n" +
					"    border-radius: 6px 0 0 0;  \n" +
					"}  \n" +

					".bordered th:last-child {  \n" +
					"    -moz-border-radius: 0 6px 0 0;  \n" +
					"    -webkit-border-radius: 0 6px 0 0;  \n" +
					"    border-radius: 0 6px 0 0;  \n" +
					"}  \n" +

					".bordered th:only-child{  \n" +
					"    -moz-border-radius: 6px 6px 0 0;  \n" +
					"    -webkit-border-radius: 6px 6px 0 0;  \n" +
					"    border-radius: 6px 6px 0 0;  \n" +
					"}  \n" +

					".bordered tr:last-child td:first-child {  \n" +
					"    -moz-border-radius: 0 0 0 6px;  \n" +
					"    -webkit-border-radius: 0 0 0 6px;  \n" +
					"    border-radius: 0 0 0 6px;  \n" +
					"}  \n" +

					".bordered tr:last-child td:last-child {  \n" +
					"    -moz-border-radius: 0 0 6px 0;  \n" +
					"    -webkit-border-radius: 0 0 6px 0;  \n" +
					"    border-radius: 0 0 6px 0;  \n" +
					"} \n" +

					"</style>  \n" +

					"</head> \n" +
					"<body> \n" +
					"<h3 align=\"center\">Result of Automation Execution</h3> \n" +
					"<table width='" + width + "' class = \"bordered\" class=\"sortable\" id=\"result_table\"> \n";

			String HTML_Footer = "</table> \n <h5 align=\"center\">Generated at: "+ dateTimeStamp + "</h5> \n" +
					"<script type=\"text/javascript\" src=\"" + TestSuiteClass.AUTOMATION_HOME + "/tpt/html_js_css/sorttable.js\"></script> \n" +
					" </body>\n </html>\n";

			bw.write(HTML_header);

			for(int i=0; i<Data.length; i++)		// For Every Row
			{
				String row_data = "";
				row_data = row_data + "<tr>\n";

				for(int j=0; j<Data[0].length; j++)	// For Every Column
				{
					Data[i][j] = Data[i][j].replace("<", "&lt;");
					Data[i][j] = Data[i][j].replace(">", "&gt;");


					if( i == 0)
					{

						row_data = row_data + "<th>" + Data[i][j] + " </th>\n";
						//bw.write("<th>" + Data[i][j] + " </th>\n");
					}
					else
					{
						if(Data[0][j].equalsIgnoreCase("Test_Results"))
						{
							if(i != 0)
							{
								if(Data[i][j].toLowerCase().contains("fail"))
								{
									//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
									//	bw.write("<td>" + Data[i][j] + "</td>\n");
									row_data = row_data.replace("<tr>", "<tr bgcolor=\"FD8A88\">");
								}
								else if(Data[i][j].toLowerCase().contains("pass"))
								{	
									//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
									//	bw.write("<td>" + Data[i][j] + "</td>\n");
									row_data = row_data.replace("<tr>", "<tr bgcolor=\"95EB9A\">");
								}
								else
								{
									//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : DATA for " + Data[i][j]);
									//		bw.write("<td>" + Data[i][j] + "</td>\n");
									row_data = row_data.replace("<tr>", "<tr bgcolor=\"FBF8A6\">");
								}
							}
						}
						row_data = row_data + "<td>" + Data[i][j] + " </td>\n";
						//bw.write("<td>" + Data[i][j] + " </td>\n");	
					}


				}
				row_data = row_data + "</tr>\n";

				bw.write(row_data);
			}

			bw.write(HTML_Footer);
			bw.close();

			flag = true;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Process End => Excel File: "+excelFile + " is converted into HTML File: "+desiredHtmlFile);
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Process Error => Excel File: "+excelFile + " is not converted into HTML File. ",e);
		}
		finally
		{
			return flag;
		}
	}



	@SuppressWarnings("finally")
	public static boolean GenerateMainResultExcelintoHTML(String excelFile, String desiredHtmlFile)
	{


		String dateTimeStamp = GenericMethodsLib.DateTimeStamp("dd-MMM-yyyy hh:mm:ss");
		boolean flag = false;
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Process Start => Excel File: "+excelFile + " is being converted into HTML File : " + desiredHtmlFile);

			XlsLib result = new XlsLib();
			String Data[][] = result.dataFromExcel(excelFile);

			File htmlfile = new File(desiredHtmlFile);

			// if file does not exists, then create it
			if (!htmlfile.exists()) 
			{
				htmlfile.createNewFile();
			}

			FileWriter fw = new FileWriter(htmlfile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			//bw.write(admValue);
			String HTML_header = "<html> \n" +
					"<head> \n" +
					"<title>Main Result Page</title> \n" +
					"<meta charset=\"UTF-8\"> \n" +
					"<title>Main Result Table</title> \n" + 
					"<link rel=\"stylesheet\" href=\"file://" + TestSuiteClass.AUTOMATION_HOME + "/tpt/html_js_css/main_table_style.css\" media=\"screen\" type=\"text/css\" /> \n" + 
					"</head> \n" +
					"<body> \n" +
					"<h2 align=\"center\">Main Result of Automation Execution</h2> \n" +
					"<table class=\"sortable\" id=\"main_result_table\"> \n";

			String HTML_Footer = "</table> \n <h5 align=\"center\">Generated at: "+ dateTimeStamp + "</h5> \n" +
					//"<script type=\"text/javascript\" src=\"" + TestSuiteClass.AUTOMATION_HOME + "/tpt/html_js_css/sorttable.js\"></script> \n" +
					" </body>\n </html>\n";

			bw.write(HTML_header);

			for(int i=0; i<Data.length; i++)		// For Every Row
			{
				if(i ==0)
				{
					bw.write("<tr  id=\"firstrow\">\n");
				}
				else
				{
					bw.write("<tr>\n");	
				}

				for(int j=0; j<Data[0].length; j++)	// For Every Column
				{

					Data[i][j] = Data[i][j].replace("<", "&lt;");
					Data[i][j] = Data[i][j].replace(">", "&gt;");

					if(j == 1)
					{
						if( i == 0 )
						{
							bw.write("<th bgcolor=\"6495ED\">" + Data[i][j] + " </th>\n");
						}
						else
						{
							bw.write("<td><a href=\"file://" + TestSuiteClass.resultFileLocation + "/" + Data[i][j] + ".html" + "\">" + Data[i][j] + "</a></td>");
						}
					}
					else
					{
						if( i == 0 )
						{
							bw.write("<th bgcolor=\"6495ED\">" + Data[i][j] + " </th>\n");
						}
						else
						{
							bw.write("<td>" + Data[i][j] + "</td>\n");
						}


					}

					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Printing Elements : "+strRecord[i][j]);
				}
				bw.write("</tr>\n");
			}

			bw.write(HTML_Footer);
			bw.close();

			flag = true;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Process End => Excel File: "+excelFile + " is converted into HTML File: "+desiredHtmlFile);
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Process Error => Excel File: "+excelFile + " is not converted into HTML File. ", e);
		}
		finally
		{
			return flag;
		}
	}


}

