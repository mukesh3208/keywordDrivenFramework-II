/**
 * Last Changes Done on Jan 27, 2015 12:44:43 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.json.JSONObject;


import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.MySQLNonTransientConnectionException;


public class DBLib 
{
	static Logger logger = Logger.getLogger(DBLib.class.getName());

	Connection connection; 

	/** Need to have this
	 * 
	 */
	public DBLib()
	{

	}

	/** Getting db connection 
	 * 
	 * @param connection
	 */
	public DBLib(Connection connection)
	{
		this.connection = connection;
	}


	public static String adlogCount() 
	{
		String ad_format_value = "0";
		try
		{
			String NewSqlQuery = "SELECT sum(COUNT) FROM (SELECT COUNT(*) AS COUNT FROM adplatform.ad_log1 UNION ALL SELECT COUNT(*) AS COUNT FROM adplatform.ad_log0) AS temp;";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			//Connection NewCon =  MobileTestClass_Methods.CreateServeSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				ad_format_value =  NewRs.getString("ad_format").toString();
			}

			NewCon.close();
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting adlog count:", e);
		}
		return ad_format_value;

	}


	public static String changeChannelRONSettingFalse(String channel_id)
	{
		String result = "";
		String additional_settings = "";
		try
		{
			String NewSqlQuery = "select additional_settings from  channel_settings where channel_id = "+ channel_id + ";";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			//Connection NewCon =  MobileTestClass_Methods.CreateSQLConnection();
			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				additional_settings =  NewRs.getString("additional_settings").toString();
			}

			NewCon.close();

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Current settings of the channel  : " + additional_settings);

			additional_settings = additional_settings.replace("\"run_untargeted\":true", "\"run_untargeted\":false");

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : New settings of channel : " + additional_settings);

			String updateSqlQuery = "update channel_settings set additional_settings = '" + additional_settings + "' where channel_id  = "+ channel_id + ";";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + updateSqlQuery);

			//Connection NewCon =  MobileTestClass_Methods.CreateSQLConnection();
			Connection updateCon =  GenericMethodsLib.CreateSQLConnection();

			Statement updateStmt = (Statement) updateCon.createStatement();
			updateStmt.executeUpdate(updateSqlQuery);

			result = result + "PASS : RON setting of channel is changed to false.\n";
			updateCon.close();

		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception in ChangeChannelRONSettingFalse Function : Exception message is " + e.getMessage());

			result = result + "FAIL : Unable to change RON setting of channel to false\n";

		}

		return result;

	}


	/** This method will get the playlist_ad_ref from qa.vdopia.com.adplateform db
	 * 
	 * @param channelApiKey
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String getChannelName(String channelApiKey)
	{
		String channelName = null;
		try
		{
			String NewSqlQuery = " select name from channels where apikey = '"+channelApiKey+"' ; ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Channel Name From DB: " + NewSqlQuery);

			GenericMethodsLib.InitializeConfiguration();

			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				channelName =  NewRs.getString("name").toString();
			}

			if(channelName != null)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Channel name is : "+ channelName);
			}

			NewCon.close();
		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.GetChannelName while getting the channel name. "+e.getMessage());
		}
		finally
		{
			return channelName;
		}
	}


	/** This method will get the channel application type from qa.vdopia.com.adplateform db
	 * 
	 * @param channelAPIKey
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String getChannelApplicationType(String channelAPIKey) 
	{
		String applicationtype = "";
		try
		{
			String NewSqlQuery = " select appType from channels where apikey = '"+channelAPIKey+"' ; ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Channel Application Type From DB: " + NewSqlQuery);

			GenericMethodsLib.InitializeConfiguration();

			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				applicationtype =  NewRs.getString("appType").toString();
			}

			if(!applicationtype.isEmpty())
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Application Type for channel is: "+applicationtype);
			}

			NewCon.close();
		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.GetChannelAPIKey while getting the ApiKey. "+e.getMessage());
		}
		finally
		{
			return applicationtype;
		}
	}


	/** This method will get the channel application type from qa.vdopia.com.adplateform db
	 * 
	 * @param channelAPIKey
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String getChannelID(String channelAPIKey, Connection connection) 
	{
		String channelID = "";
		try
		{
			String NewSqlQuery = " select id from channels where apikey = '"+channelAPIKey+"' ; ";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Channel ID From DB: " + NewSqlQuery);

			Statement NewStmt = (Statement) connection.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				channelID =  NewRs.getString("id").toString();
			}

			if(!channelID.isEmpty())
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Channel ID for channel is: "+channelID);
			}
		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.GetChannelID while getting the Channel ID. "+e.getMessage());
		}
		finally
		{
			return channelID;
		}
	}


	/** This method will get the Channel Api Key from qa.vdopia.com.adplateform db
	 * 
	 * @param channelName
	 * @return
	 */
	public String getChannelAPIKey(String channelID) 
	{
		String apiKey = "";
		try
		{
			String NewSqlQuery = " select apikey from channels where id = '"+channelID+"' ; ";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Channel Api_Key From DB: " + NewSqlQuery);

			Statement NewStmt = (Statement) connection.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				apiKey =  NewRs.getString("apikey").toString();
			}

			if(apiKey != null)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : APIKey for channel: "+ channelID +" is: "+apiKey);
			}
		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.GetChannelAPIKey while getting the ApiKey. "+e.getMessage());
		}
		return apiKey;
	}


	/** This method will get the category_id from db
	 * 
	 * @param sql
	 * @return
	 */
	public String getCategory(Connection connection, String sql) 
	{
		String category_id = "";
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Category From DB: " + sql);

			Statement NewStmt = (Statement) connection.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(sql);

			while(NewRs.next())
			{
				category_id =  NewRs.getString("category_id").toString();
			}

			if(category_id != null)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : category_id: "+ category_id +" is: "+category_id);
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+e.getMessage() + "\n SQL: "+sql, e);
		}
		return category_id;
	}


	/** This method will get the campaign id from qa.vdopia.com.adplateform db
	 * 
	 * @param campaignName
	 * @return
	 */
	public static String getCampaignID(String campaignName) 
	{
		String ID = null;
		try
		{
			String NewSqlQuery = "select id from adplatform.campaign where name = '" + campaignName + "';";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			GenericMethodsLib.InitializeConfiguration();

			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				ID =  NewRs.getString("id").toString();
			}

			if(ID != null)
			{
				logger.info(campaignName +" having Campaign_ID: "+ID + " is found in campaign Table.");
			}

			NewCon.close();
		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.GetCampaignID while getting the campaign_id. "+e.getMessage());
		}
		return ID;
	}


	/** This method will get the campaign id from qa.vdopia.com.adplateform db
	 *
	 * @param campaignId
	 * @return
	 */
	public static boolean verifyCampaignInMemcamp(String campaignId)
	{
		boolean flag = false;
		String ID = null;
		try
		{
			String NewSqlQuery = " SELECT cid AS Campaign_ID FROM memcamp where cid = "+ campaignId + " limit 1 ;"; 

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query: " + NewSqlQuery + " to check campaign entry in memcamp table. ");

			GenericMethodsLib.InitializeConfiguration();

			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				ID =  NewRs.getString("Campaign_ID").toString();
			}

			NewCon.close();

			if(ID != null)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Campaign: " +campaignId +" is found in Memecamp Table with CID: "+ID);
				flag = true;
			}
			else
			{
				logger.info(campaignId +" is not found in Memecamp Table.");
				flag = false;
			}
		}
		catch(Exception e)
		{
			flag = false;
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.VerifyCampaignInMemcamp, while getting the campaign_id from Memcamp Table. "+e.getMessage());
		}
		return flag;
	}


	/** This method will get the playlist_ad_ref from qa.vdopia.com.adplateform db
	 * 
	 * @param campaignName
	 * @return
	 */
	public static String getPlaylist_Ad_Ref(String campaignName) 
	{
		String playListRef = null;
		try
		{
			String NewSqlQuery = " SELECT IFNULL(playlist_ad_ref,0) AS PlayListRef FROM ads ad INNER JOIN campaign_members cm ON ad.id = cm.ad_id " +
					" INNER JOIN campaign cam ON cam.id = cm.cid AND ad.AD_FORMAT = 'VIDEO' AND cam.name = '"+ campaignName+"' ;";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Playlist_Ad_Ref From DB: " + NewSqlQuery);

			GenericMethodsLib.InitializeConfiguration();

			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				playListRef =  NewRs.getString("PlayListRef").toString();
			}

			if(playListRef != null)
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : PlayListRef for "+ campaignName +" is: "+playListRef);
			}

			NewCon.close();
		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.GetPlaylist_Ad_Ref while getting the PlayListRef. "+e.getMessage());
		}
		return playListRef;
	}


	/** This method will get the playlist_ad_ref from qa.vdopia.com.adplateform db
	 * 
	 * @param channelApikey
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String[] getChannelDetails(String channelApikey) 
	{
		String []channelDetails = null;
		try
		{
			String NewSqlQuery = " SELECT ch.name, ch.domain, ch.apikey, p.id, p.company_name, ch.url FROM channels ch INNER JOIN publisher p " +
					" ON ch.publisher_id = p.id WHERE ch.apikey = '"+ channelApikey + "'";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Channel Details From DB: " + NewSqlQuery);

			GenericMethodsLib.InitializeConfiguration();

			Connection NewCon =  GenericMethodsLib.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			int columns = NewRs.getMetaData().getColumnCount();
			channelDetails = new String[columns];

			NewRs.beforeFirst();	// Setting the cursor at first line	

			while (NewRs.next())
			{
				for(int i=1;i<=columns;i++)
				{
					String strRecord = NewRs.getString(i);
					channelDetails[i-1] = strRecord;
					//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Writing Rows BY METHOD - ExecuteMySQLQueryReturnsOnlyColumnNames: " +strRecord);
				}
			}

			NewCon.close();
		}
		catch(Exception e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method - DBLib.GetChannelDetails while getting the ApiKey. "+e.getMessage());
		}
		finally
		{
			return channelDetails;
		}
	}


	/** Executing MySQL Query and Returning 1 D Array containing the Result Set without Column Name
	 *  
	 * @return
	 */
	@SuppressWarnings("finally")
	public static HashMap<String, String> getActiveHudsonVastBidderURLs() 
	{		
		HashMap<String, String> records = new HashMap<String, String>();

		try
		{
			GenericMethodsLib.InitializeConfiguration();

			String sqlQuery = " select IFNULL(bidderid, 0), IFNULL(bidderurl,0) from hudsonBidder " +
					" where BidderType in ('vast_fixed_price', 'vast_vdopia_extn_price') " +
					" and BidderStatus = 'Approved' and bidderid " +
					" not in (select bidder_id from hudson_converted_ads) ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Query to get all active vast bidders: "+sqlQuery);

			Connection con = GenericMethodsLib.CreateSQLConnection();
			ResultSet rs = GenericMethodsLib.ExecuteMySQLQueryReturnsResultSet(con, sqlQuery);

			rs.beforeFirst();	// Setting the cursor at first line	
			while (rs.next())
			{
				String bidderID = rs.getString(1).toString();
				String bidderURL = rs.getString(2).toString();

				records.put(bidderID, bidderURL);
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Bidder ID: "+bidderID + " Bidder URL: "+bidderURL);
			}		
			con.close();			
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was no record found in the Result Set, Therefore returning a NULL array by Method : getActiveHudsonVastBidderURLs:");
			e.printStackTrace();
		}
		catch (NullPointerException e) 
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : NullPointerExpection Handled By: getActiveHudsonVastBidderURLs");
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Used MySQL query may have returned a NULL column in Result Set, Therefore use IFNULL with that particular column in query.");
			e.printStackTrace();
		}
		catch (Exception e) 
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Expection Handled By: getActiveHudsonVastBidderURLs. " +e);
		}
		finally
		{
			return records;
		}
	}


	/** This method will get the bidder type from db
	 * 
	 * @param bidderID
	 * @param NewCon
	 * @return
	 */
	@SuppressWarnings("finally")
	public static int getVdopiaRTBBidderCount(List<String> bidderID, Connection NewCon) 
	{
		String bidderCount = "";

		try
		{			
			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Received actually selected bidders: "+bidderID);

			if(!bidderID.isEmpty())
			{
				String bidder = "";

				/**
				 * Splitting the received list and forming a comma separated string to be used in sub query in below main query.
				 */
				for (int i=1; i<=bidderID.size(); i++)
				{
					bidder = bidder + "'" + bidderID.get(i-1) + "'";

					if(i!=bidderID.size())
					{
						bidder = bidder + ",";
					}
				}

				String query =
						" select count(*) AS bidderCount from hudsonBidder " +
								" where BidderType like 'rtb%' and IsVdopiaBidder = '1' " +
								" and bidderid in ("+ bidder +"); ";

				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Count Of Vdopia RTB Bidder From DB: " + query);

				//Connection NewCon =  MobileTestClass_Methods.CreateSQLConnection();
				Statement NewStmt = (Statement) NewCon.createStatement();
				ResultSet NewRs = (ResultSet) NewStmt.executeQuery(query);

				while(NewRs.next())
				{
					bidderCount =  NewRs.getString("bidderCount").toString();
				}

				if(bidderCount != null)
				{
					logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Vdopia RTB Bidder Count: "+ bidderCount);
				}

				//NewCon.close();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting the count of RTB Bidder from received actual selected bidder list: "+ bidderID, e);
		}
		finally
		{
			try{
				return Integer.parseInt(bidderCount);
			}catch(NumberFormatException n)
			{
				logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Returning vdopia rtb bidder count = 0 ", n);
				return 0;
			}
		}
	}


	/** This method will get the bidder price from db
	 * 
	 * @param bidderID
	 * @param NewCon
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String getBidderPrice(String bidderID, Connection NewCon) 
	{
		String bidderPrice = "";
		try
		{
			String query = " select BidderPrice from hudsonBidder where bidderid = '"+ bidderID + "' ";

			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Bidder Price From DB: " + query);

			//Connection NewCon =  MobileTestClass_Methods.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(query);

			while(NewRs.next())
			{
				bidderPrice =  NewRs.getString("BidderPrice").toString();
			}

			if(bidderPrice != null)
			{
				logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : BidderPrice: "+ bidderPrice + " For BidderID: "+bidderID);
			}

			//NewCon.close();
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting the BidderPrice of BidderID: "+ bidderID, e);
		}
		finally
		{
			return bidderPrice;
		}
	}


	/** Getting bidder type from db
	 * 
	 * @param bidderID
	 * @param NewCon
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String getBidderType(String bidderID, Connection NewCon) 
	{
		String bidderType = "";
		try
		{
			String query = " select BidderType from hudsonBidder where bidderid = '"+ bidderID + "' ";

			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Bidder Type From DB: " + query);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(query);

			while(NewRs.next())
			{
				bidderType =  NewRs.getString("BidderType").toString();
			}

			if(bidderType != null)
			{
				logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : BidderType: "+ bidderType + " For BidderID: "+bidderID);
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting the BidderType of BidderID: "+ bidderID, e);
		}
		finally
		{
			return bidderType;
		}
	}


	/**  This method will get the values of all parameters: urlParam = type, pageURL, siteName, domain, appBundle, appName, category, refURL
	 * listed in config: hudsonURLParam.properties.
	 * Getting the value of there parameter from channels table for the given apikey.
	 * refURL is not a column in channel table hence added this will always return NULL by executing below query, this parameter will get value
	 * from headers later on not from db. 
	 */
	@SuppressWarnings("finally")
	public static HashMap<String, String> getRequiredHudsonParamsFromDB(String channelAPIKey, Connection NewCon) 
	{
		HashMap<String, String> hashmap = new HashMap<String, String>();

		try
		{
			String NewSqlQuery = 
					" SELECT CASE WHEN ch.appType = 'MobileWeb' THEN 'site' ELSE 'app' END AS 'type', " +
							" IFNULL(ch.url, '') AS pageURL, " +
							" CASE WHEN (IFNULL(ch.pretty_name,'') = '' AND ch.appType ='MobileWeb') THEN 'site_hudson' ELSE ch.pretty_name END AS siteName, " +
							" IFNULL(ch.domain, '') AS domain,  IFNULL(ch.app_bundle, '') AS appBundle, " +
							" CASE WHEN (IFNULL(ch.pretty_name, '') = '' AND IFNULL(ch.appType,'') <> 'MobileWeb') THEN 'app_hudson' ELSE ch.pretty_name END AS appName, " +
							" IFNULL(ch.channel_category, '') AS category,  IFNULL(ch.publisher_id, '') AS publisher_id, " +
							" ch.market_place_setting AS market_place_setting, ch.id AS channel_id, ch.video_unit_size AS size, " +
							" CASE WHEN (ch.skippable = '1') THEN 'true' ELSE 'false' END AS skippable, " +
							" CASE WHEN (ch.sound = '1') THEN 'true' ELSE 'false' END AS sound, " +
							" CASE WHEN (ch.autoplay = '1') THEN 'true' ELSE 'false' END AS autoplay, " +
							" CASE WHEN (ch.incentivized = '1') THEN 'true' ELSE 'false' END AS incentivized, " +
							" CASE WHEN (ch.in_stream = '1') THEN 'true' ELSE 'false' END AS instream, " +
							" ch.clickable AS clickable, CASE WHEN (ch.direct = '0') THEN 'direct' ELSE 'nondirect' END AS integration, " +
							" chst.additional_settings AS additionalSettings, IFNULL(vt.tag, '') AS vast_tag, pub.email AS publisher_email, " +
							" price.floor_price_usd AS channel_floor_price " +
							" FROM publisher pub INNER JOIN channels ch ON pub.id = ch.publisher_id " +
							" INNER JOIN channel_floor_price price ON ch.id = price.channel_id " +
							" INNER JOIN channel_settings chst on ch.id = chst.channel_id " +
							" LEFT OUTER JOIN vast_tags vt ON chst.channel_id = vt.channel_id AND vt.status = 'enable' " +
							" WHERE ch.apikey = '" + channelAPIKey + "'; "; 


			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Details From DB: " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet recordSet = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			int columns = recordSet.getMetaData().getColumnCount();
			recordSet.beforeFirst();	// Setting the cursor at first line	

			//getting all record into a hashmap as column name as Key and value as Value
			while(recordSet.next())
			{
				for(int i=1; i<=columns; i++)
				{

					String key = recordSet.getMetaData().getColumnLabel(i).toString().trim(); 

					String value = "";
					try{
						value = recordSet.getString(i).toString().trim(); }
					catch(Exception e)
					{
						if(key.equalsIgnoreCase("appName"))
						{
							value = "app_hudson";
						}
						else if(key.equalsIgnoreCase("siteName"))
						{
							value = "site_hudson";
						}
						else if(key.equalsIgnoreCase("domain"))
						{
							value = "www.default.com";
						}
						else
						{
							value = null;
						}
					}

					hashmap.put(key, value);
				}
			}

			/** Get the channel floor price and put in the final map
			 */
			if(!hashmap.isEmpty())
			{
				String channelId = hashmap.get("channel_id");
				String channelfloorprice_usd = getChannelFloorPrice(channelId, NewCon);
				hashmap.put("floor_price_usd", channelfloorprice_usd);

				/** Get the value of channel_settings.addition_setting from recordSet map and 
				 * parse the value as a pair of key value and put it in the final map
				 */ 
				String add_set = hashmap.get("additionalSettings");
				System.out.println("add_set"+ add_set);
				JSONObject additionalSettings = new JSONObject(add_set);

				@SuppressWarnings("unchecked")
				Iterator<String> keys = additionalSettings.keys();
				//System.out.println("Keys is :" keys);
				while(keys.hasNext()){
					String key = keys.next();
					//System.out.println("key is :" + key);

					Object value = additionalSettings.get(key);
					//System.out.println("value is :" + value);

					hashmap.put(key, String.valueOf(value));
				}       

			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting required values from db. ", e);
		}
		finally
		{
			return hashmap;
		}
	}


	/** Returns the map of db information, for this, supplied select query should return only one row. 
	 * 
	 * @param NewCon
	 * @return
	 */
	public HashMap<String, String> getDBInformationMap(Connection NewCon, String sqlQuery) 
	{
		HashMap<String, String> hashmap = new HashMap<String, String>();
		
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query - " + sqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet recordSet = (ResultSet) NewStmt.executeQuery(sqlQuery);

			int columns = recordSet.getMetaData().getColumnCount();
			recordSet.beforeFirst();	// Setting the cursor at first line

			while(recordSet.next())
			{
				for(int i=1; i<=columns; i++)
				{
					String key = recordSet.getMetaData().getColumnLabel(i).toString().trim(); 
					String value = recordSet.getString(i).toString().trim();
					hashmap.put(key, value);
				}
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting the information from db: ", e);
		}

		return hashmap;
	}


	/** This method will check the if there is any converted ad exists for a vast bidder
	 * 
	 * @param bidderId
	 * @param md5SumValue
	 * @return
	 */
	@SuppressWarnings("finally")
	public static boolean checkConvertedAdForVastBidder(String bidderId, String md5SumValue, Connection NewCon) 
	{
		boolean isVastBidderOk = false;

		try
		{
			String NewSqlQuery = 
					" SELECT COUNT(*) AS COUNT FROM hudson_ads ad INNER JOIN hudson_converted_ads con " +
							" ON ad.id = con.ad_id " +
							" WHERE ad.status = '-1' " +
							" AND ad.playlist_ad_ref <> 'ad_conversion_failed' " +
							" AND ad.playlist_ad_ref <> 'vdofy_failed' " +
							" AND con.bidder_id = '"+ bidderId +"' " +
							" AND con.converted_ad_id = '"+ md5SumValue +"' " ;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running query to check if there is any vast converted ad available in db: " + NewSqlQuery);

			//Connection NewCon =  MobileTestClass_Methods.CreateSQLConnection();
			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet recordSet = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			recordSet.beforeFirst();	// Setting the cursor at first line

			String value = "0";
			while(recordSet.next())
			{
				value = recordSet.getString("COUNT").toString().trim();
				logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Received COUNT from above query: "+value);
			}

			//NewCon.close();

			if(Integer.parseInt(value) > 0)
			{
				isVastBidderOk = true;
			}
			else
			{
				isVastBidderOk = false;
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while checking correct vast bidder: "+ bidderId, e); 
		}
		finally
		{
			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : isVastBidderOk: "+String.valueOf(isVastBidderOk));
			return isVastBidderOk;
		}
	}


	/** This method will return the bidder url
	 * @param bidderID
	 * @return
	 */
	public String getBidderURL(String bidderID, Connection NewCon) 
	{
		String bidderURL="";
		try
		{
			String NewSqlQuery = "SELECT BidderUrl FROM hudsonBidder WHERE bidderId = '"+ bidderID +"'";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				bidderURL =  NewRs.getString("BidderUrl").toString();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting bidderURL for bidder: "+bidderID, e);
		}
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Bidder URL: "+bidderURL);
		return bidderURL;

	}


	/** This method will return the ad format of supplied adid
	 * @param bidderID
	 * @return
	 */
	public static String getAdFormat(String adid, Connection NewCon) 
	{
		String adformat="";
		try
		{
			String NewSqlQuery = "SELECT ad_format FROM ads WHERE id = "+adid;
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				adformat =  NewRs.getString("ad_format").toString().trim();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting ad format of adid: "+adid, e);
		}
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Ad format of ad id: "+adid);
		return adformat;

	}


	/** This method will return the bidderVdopiaMargin of supplied bidder
	 * @param bidderID
	 * @return
	 */
	public static String getBidderVdopiaMargin(String bidderid, Connection NewCon) 
	{
		String bidderVdopiaMargin="";
		try
		{
			String NewSqlQuery = "SELECT BidderVdopiaMargin FROM hudsonBidder where bidderid = '"+bidderid +"'"; 
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				bidderVdopiaMargin =  NewRs.getString("BidderVdopiaMargin").toString().trim();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting bidderVdopiaMargin of bidder: "+bidderid, e);
		}
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : bidderVdopiaMargin: "+bidderVdopiaMargin);

		return bidderVdopiaMargin;
	}


	/**
	 * This method will return the channel floor price of supplied channel id.
	 * 
	 * @param bidderid
	 * @param NewCon
	 * @return
	 */
	public static String getChannelFloorPrice(String channelId, Connection NewCon) 
	{
		String floor_price_usd="";
		try
		{
			String NewSqlQuery = "SELECT floor_price_usd FROM channel_floor_price WHERE channel_id = '"+channelId+"'"; 
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				floor_price_usd =  NewRs.getString("floor_price_usd").toString().trim();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting floor_price_usd of channel: "+channelId, e);
		}

		if(floor_price_usd.isEmpty())
		{
			floor_price_usd = "1.0";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : No Channel Floor Price Received, Reassigning To 1.00");
		}
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : floor_price_usd: "+floor_price_usd);

		return floor_price_usd;
	}


	/** This method will execute query to find out the filtered domain of the supplied bidder.
	 * 
	 * @param bidderID
	 * @param status
	 * @param platform
	 * @param connection
	 * @return
	 */
	public static List<String> getFilteredBidderDomain(String bidderID, String status, String platform, Connection connection)
	{
		List<String> domain = new ArrayList<>();
		try
		{
			String sqlQuery = " select IFNULL(domain, '') AS domain from hudson_bidder_whitelist_blacklist " +
					" where status = '"+ status +"' and platform = '"+ platform +"' and bidder_id = '"+ bidderID +"' " ;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query: " + sqlQuery);

			Statement NewStmt = (Statement) connection.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(sqlQuery);

			while(NewRs.next())
			{
				domain.add(NewRs.getString("domain").toString().trim().toLowerCase());
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting filtered domain for bidder: "+bidderID, e);
		}
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Bidder Filtered Domain: "+domain);

		return domain;
	}


	/** This method will insert the execution entry in qaautomation db.
	 * 
	 * @return
	 */
	public static boolean insertExecutionLog(String startTime, String endTime)
	{
		boolean flag;
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Inserting execution log in db ... ");
			String ip = Inet4Address.getLocalHost().getHostAddress();
			String name = Inet4Address.getLocalHost().getHostName();
			String machine = name+"/"+ip;

			Connection qaConnection = SaveResultsToMySql.getAutomationConnection();
			String sql = "INSERT INTO ExecutionLog (start_time, end_time, machine) VALUES " +
					" ('"+startTime+"', '"+endTime+"', '"+machine+"') "; 

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + sql);

			Statement statement = (Statement) qaConnection.createStatement();
			statement.executeUpdate(sql);

			qaConnection.close();
			flag = true;
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while executing insert sql. "+e.getMessage());
		}

		return flag;
	}


	/** This method will execute the update / insert query.
	 * 
	 * @return
	 */
	public boolean executeUpdateInsertQuery(Connection connection, String sql)
	{
		boolean flag;
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + sql);
			Statement statement = (Statement) connection.createStatement();
			statement.executeUpdate(sql);

			flag = true;
		}catch(CommunicationsException | MySQLNonTransientConnectionException w)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : SQL connection was closed while executing query. ");
		}
		catch(Exception e)
		{
			flag = false;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while executing query: "+sql, e);
		}
		return flag;
	}


	/** This method will get the snapshot url from campaign table of qa.vdopia.com.adplateform db
	 * 
	 * @param campaignId
	 * @param NewCon
	 * @return
	 */
	@SuppressWarnings("finally")
	public static HashMap<String, String> getCampaignInformation(String campaignId, Connection NewCon) 
	{
		HashMap<String, String> hashmap = new HashMap<String, String>();

		try
		{
			String NewSqlQuery = 
					" select IFNULL(screenshot_url, '') AS screenshot_url " +
							" from campaign where id = '" + campaignId +"' ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get campaign Information From DB: " + NewSqlQuery);

			//Connection NewCon =  MobileTestClass_Methods.CreateSQLConnection();

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet recordSet = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			int columns = recordSet.getMetaData().getColumnCount();
			recordSet.beforeFirst();	// Setting the cursor at first line

			while(recordSet.next())
			{
				for(int i=1; i<=columns; i++)
				{
					String key = recordSet.getMetaData().getColumnLabel(i).toString().trim(); 
					String value = recordSet.getString(i).toString().trim();
					hashmap.put(key, value);
				}
			}

			//NewCon.close();
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting the values for bidder: "+ campaignId, e); 
		}
		finally
		{
			return hashmap;
		}
	}


	/** This method will fetch the required information to be used in MOAT verification based
	 * on supplied ad id/apikey from mysql. 
	 * 
	 * @param connection
	 * @param adID
	 * @return
	 */
	@SuppressWarnings("finally")
	public static TreeMap<String, String> getMoatInformation(Connection connection,String bidderType, String ChannelName, String adId, String moatPartnerCode, String cid)
	{
		TreeMap<String, String> TreeMap = new TreeMap<String, String>();
		String NewSqlQuery = null;
		try
		{
			if(bidderType.startsWith("rtb"))
			{	
				NewSqlQuery = 
						" SELECT '' AS site,'on' AS moat_on_off,'"+moatPartnerCode+"' AS partner_id, plac.id AS line_item, ad.id AS crid, " +
								" 'Vdopia Inc' AS adv_nm, cam.id AS placement, cam.id AS cid" 
								+ " FROM placements plac INNER JOIN campaign cam ON plac.id = cam.placement_id "
								+ " INNER JOIN campaign_members cm ON cam.id = cm.cid "
								+ " INNER JOIN ads ad ON cm.ad_id = ad.id "
								+ " INNER JOIN advertiser adv ON cam.advertiser_id = adv.id "
								+ " WHERE cam.id = "+cid+"; ";
			}

			else if(bidderType.startsWith("vast") || bidderType.startsWith("vpaid"))
			{	
				NewSqlQuery = 
						" SELECT '' AS site,'on' AS moat_on_off, '"+moatPartnerCode+"' AS partner_id, '' AS line_item, "+adId+" AS crid, " +
								" 'Vdopia Inc' AS adv_nm, '' AS placement, "+adId+" AS cid "
								+ " FROM channels as ch "
								+ " WHERE ch.name = '"+ChannelName+"'; ";
			}

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Moat Information From DB: " + NewSqlQuery);

			Statement NewStmt = (Statement) connection.createStatement();
			ResultSet recordSet = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			int columns = recordSet.getMetaData().getColumnCount();
			recordSet.beforeFirst();

			/** converting record set to a map */
			while(recordSet.next())
			{
				for(int i=1; i<=columns; i++)
				{
					String key = recordSet.getMetaData().getColumnLabel(i).toString().trim(); 
					String value = recordSet.getString(i).toString().trim();
					TreeMap.put(key, value);
				}
			}

			/** change params based on certain conditions */
			//TreeMap = ValidationHandler.applyRules(TreeMap, moatPartnerCode);
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting the values from db for supplied adid: "+ adId, e); 
		}
		finally
		{
			return TreeMap;
		}

	}


	/** This method will get the snapshot url from campaign table of qa.vdopia.com.adplateform db
	 * 
	 * @param campaignId
	 * @param NewCon
	 * @return
	 */
	@SuppressWarnings("finally")
	public static HashMap<String, String> getPublisherInformation(Connection NewCon) 
	{
		HashMap<String, String> hashmap = new HashMap<String, String>();

		try
		{
			String NewSqlQuery = 
					" select IFNULL(id, '') AS id " +
							" from publisher where email = 'hudson@vdopia.com' ";

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get publisher Information From DB: " + NewSqlQuery);

			//Connection NewCon =  MobileTestClass_Methods.CreateSQLConnection();

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet recordSet = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			int columns = recordSet.getMetaData().getColumnCount();
			recordSet.beforeFirst();	// Setting the cursor at first line

			while(recordSet.next())
			{
				for(int i=1; i<=columns; i++)
				{
					String key = recordSet.getMetaData().getColumnLabel(i).toString().trim(); 
					String value = recordSet.getString(i).toString().trim();
					hashmap.put(key, value);
				}
			}

			//NewCon.close();
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception occurred while getting the publisher Information", e); 
		}
		finally
		{
			return hashmap;
		}
	}


	/** This method will get the channel id and template_layout from mysql adplateform db and return it in map
	 * 
	 * @param channelAPIKey
	 * @return
	 */
	@SuppressWarnings("finally")
	public static HashMap<String, String> getSDKInformation(String channelAPIKey, Connection connection) 
	{
		String channelID = "";
		String template_layout = "";
		String NewSqlQuery = "";
		HashMap<String, String> map = new HashMap<String, String>();
		try
		{
			NewSqlQuery = " select IFNULL(ch.id,'') as channelid, IFNULL(a.template_layout, '') as template_layout from ads a "
					+ " inner join campaign_members cm on a.id = cm.ad_id "
					+ " inner join campaign cam on cm.cid = cam.id " 
					+ " inner join channels ch on cam.channel_choice = ch.id "
					+ " where ch.apikey = '" + channelAPIKey + "'"
					+ " and a.parent_ad_id is null ;" ;

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query To Get Channel ID and template_layout From DB: " + NewSqlQuery);

			Statement NewStmt = (Statement) connection.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				channelID =  NewRs.getString("channelid").toString().trim();
				map.put("channelid", channelID);

				template_layout = NewRs.getString("template_layout").toString().trim();
				map.put("template_layout", template_layout);
			}
		}
		catch(Exception e)
		{	
			logger.error(e.getMessage() + " Query: "+NewSqlQuery,e);
		}
		finally
		{
			return map;
		}
	}


	/**
	 * This method will return the bidder category expression of supplied bidder id.
	 * 
	 * @param bidderid
	 * @param NewCon
	 * @return
	 */
	public String getBidderCategoryExpression(String bidderID, Connection NewCon) 
	{
		String expr="";
		try
		{
			String NewSqlQuery = "select expr from targeting_package where package_id = '"+bidderID+"'";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				expr =  NewRs.getString("expr").toString().trim();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting Bidder Category Expression of bidder: "+bidderID, e);
		}

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Bidder Category Expression: "+expr);
		return expr;
	}


	/**
	 * This method will return the package category expression of supplied package id.
	 * 
	 * @param bidderid
	 * @param NewCon
	 * @return
	 */
	public String getPackageCategoryExpression(String packageID, Connection NewCon) 
	{
		String data_provider_expr="";
		try
		{
			String NewSqlQuery = "select data_provider_expr from packages where id = '"+packageID+"'";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				data_provider_expr =  NewRs.getString("data_provider_expr").toString().trim();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting Package Category Expression of bidder: "+packageID, e);
		}

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Package Category Expression: "+data_provider_expr);
		return data_provider_expr;
	}


	/**
	 * This method will return the campaign category expression of supplied bidder id.
	 * 
	 * @param bidderid
	 * @param NewCon
	 * @return
	 */
	public String getCampaignCategoryExpression(String campaignID, Connection NewCon) 
	{
		String expr="";
		try
		{
			String NewSqlQuery = "select expr from targeting_package where package_id = '"+campaignID+"'";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				expr =  NewRs.getString("expr").toString().trim();
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting Campaign Category Expression of campaign: "+campaignID, e);
		}

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Campaign Category Expression: "+expr);
		return expr;
	}


	public double getCategoryPrice(String category,Connection NewCon) 
	{
		double price= 0.0;
		String [] categories = category.split("_");
		String provider_prefix = categories[0].toUpperCase()+"_";
		String prv_cat_id = categories[1];
		try
		{
			String NewSqlQuery = "select cpm from provider_rate_card where prv_cat_id = "+prv_cat_id+ " and provider_prefix = '"+provider_prefix+"'";
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Running Query in DB : " + NewSqlQuery);

			Statement NewStmt = (Statement) NewCon.createStatement();
			ResultSet NewRs = (ResultSet) NewStmt.executeQuery(NewSqlQuery);

			while(NewRs.next())
			{
				price =  NewRs.getDouble("cpm");
			}
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error occurred while getting Category price of : "+category, e);
		}

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Campaign Category Expression: "+price);
		return price;
	}



}
