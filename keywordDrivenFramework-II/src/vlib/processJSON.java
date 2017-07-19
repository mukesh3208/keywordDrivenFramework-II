/**
 * Last Changes Done on 5 Mar, 2015 12:07:45 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;
import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;



import org.json.simple.parser.JSONParser;



public class processJSON 
{
	static Logger logger = Logger.getLogger(processJSON.class.getName());

	public static JSONObject parseJSON(String JSON_Str) 
	{
		JSONParser parser=new JSONParser();
		//String JSON_Str = "{\"id\":\"31c64cf8-17bb-11e2-a4c5-1040f38b83e0\",\"nbr\":0,\"bidid\":\"dummyBidId\"}";
		try
		{
			Object obj = parser.parse(JSON_Str);
			JSONObject jsonObject = (JSONObject) obj;
			return jsonObject;
		}
		catch(ParseException pe)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : position: " + pe.getPosition());
			logger.info(pe);
			return null;
		}
	}
	public static String getValueStr(String data, String var)
	{
		String value = null;
		try
		{
			JSONObject jsonObject = parseJSON(data);
			value = (String) jsonObject.get(var);
		}
		catch(NullPointerException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Nullpointer exception:", e);
		}
		return value;

	}
	public static Long getValueLong(String data, String var)
	{
		Long value = null;
		try
		{
			JSONObject jsonObject = parseJSON(data);

			value = (Long) jsonObject.get(var);
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Value of Variable " + var + " is : " + value);
		}
		catch(NullPointerException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Nullpointer exception:", e);
		}
		return value;
	}

	public static Integer getValueInt(String data, String var)
	{
		Integer value = null;
		try
		{
			JSONObject jsonObject = parseJSON(data);
			value = Integer.parseInt(jsonObject.get(var).toString());

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Value of Variable " + var + " is : " + value);
		}
		catch(NullPointerException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Nullpointer exception:", e);
		}
		return value;
	}

	public static boolean keyExist(String data, String var)
	{
		boolean exist = false;
		try
		{
			JSONObject jsonObject = parseJSON(data);

			for (Object key :jsonObject.keySet())
			{
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Matching JSON key : " + key + " with expected Key : " + var); 
				if(key.toString().compareToIgnoreCase(var) == 0) 
				{
					exist = true;
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Key " + var + " exist in the JSON object");
					return exist;
				}
			}

		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : KeyExist Exception : Exception while searching key in the JASON object", e);
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : KeyExist Exception : " + e.getMessage());
		}
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Key " + var + " does not exist in the JSON object");
		return exist;
	}

}