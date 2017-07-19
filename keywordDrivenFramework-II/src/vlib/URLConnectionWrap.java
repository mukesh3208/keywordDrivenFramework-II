/**
 * Last Changes Done on 5 Mar, 2015 12:07:45 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import vlib.processJSON;

public class URLConnectionWrap 

{
	
	
	public static String sentGet(String ServerURL) throws Exception 
	{
		URL obj = new URL(ServerURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		// optional default is GET
		con.setRequestMethod("GET");
		
		//add request header
		//con.setRequestProperty("User-Agent", "USER_AGENT_STRING");
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + ServerURL);
		System.out.println("Response Code of request is: " + responseCode);
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());
		return response.toString();
		
	}
	
	public static String sendPost(String ServerURL, String PostData) throws Exception {

		
		URL obj = new URL(ServerURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", USER_AGENT);
		//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		//String urlParameters = "{	\"imp\": [    	{            \"h\": 48,            \"battr\": [                \"9\",                \"10\",                \"12\"        	],            \"api\": 3,            \"w\": 320,            \"instl\": 0,            \"impid\": \"5d6dedf3-17bb-11e2-b5c0-1040f38b83e0\"    	}	],	\"app\": {        \"name\": \"Test App\",        \"pid\": \"agltb3B1Yi1pbmNyAEsBS0GjZ291bnQY9Iv5FAw\",        \"pub\": \"Test Pub\",        \"cat\": [            \"Games\"    	],        \"paid\": 0,    	\"aid\": \"agltb3B1Yi1pdmNyBAsSA0FwcBjRmvkVDB\",        \"global_aid\": \"385763430\"	},	\"pf\": 0.05,	\"at\": 2,    \"restrictions\": {        \"badv\": [],        \"bcat\": [            \"IAB25\",            \"IAB7-39\",            \"IAB8-5\",        	\"IAB8-18\",            \"IAB9-9\",            \"IAB14-1\"    	]	},    \"device\": {        \"os\": \"iPhone OS\",        \"ip\": \"204.28.127.10\",        \"js\": 1,        \"dpid\": \"32b1c496b84ea549191cdc65d32ade0c8d74c91b\",        \"osv\": \"4.2.1\",        \"loc\": \"37.7,-122.4\",        \"country\": \"USA\",        \"make\": \"Apple\",        \"carrier\": \"Wi-Fi\",        \"model\": \"iPhone\",        \"ua\": \"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7\"	},	\"tmax\": 200,	\"id\": \"31c64cf8-17bb-11e2-a4c5-1040f38b83e0\",	\"user\": {        \"uid\": \"32B1C496B84EA549191CDC65d32ADE0C8D74C91B\",        \"zip\": \"94110\",        \"gender\": \"F\",        \"yob\": 1990,    	\"country\": \"USA\",        \"keywords\": \"m_age:22,startups\"	}}";
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(PostData);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + ServerURL);
		System.out.println("Post parameters : " + PostData);
		System.out.println("Response Code of request is : " + responseCode);

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());
		return response.toString();

	}
	
	public static void main(String[] args) throws Exception {

		//URLConnectionWrap http = new URLConnectionWrap();

//		System.out.println("Testing 1 - Send Http GET request");
//		http.sendGet();

		System.out.println("\nSend Http POST request : ");
		String urlParameters = "{	\"imp\": [    	{            \"h\": 48,            \"battr\": [                \"9\",                \"10\",                \"12\"        	],            \"api\": 3,            \"w\": 320,            \"instl\": 0,            \"impid\": \"5d6dedf3-17bb-11e2-b5c0-1040f38b83e0\"    	}	],	\"app\": {        \"name\": \"Test App\",        \"pid\": \"agltb3B1Yi1pbmNyAEsBS0GjZ291bnQY9Iv5FAw\",        \"pub\": \"Test Pub\",        \"cat\": [            \"Games\"    	],        \"paid\": 0,    	\"aid\": \"agltb3B1Yi1pdmNyBAsSA0FwcBjRmvkVDB\",        \"global_aid\": \"385763430\"	},	\"pf\": 0.05,	\"at\": 2,    \"restrictions\": {        \"badv\": [],        \"bcat\": [            \"IAB25\",            \"IAB7-39\",            \"IAB8-5\",        	\"IAB8-18\",            \"IAB9-9\",            \"IAB14-1\"    	]	},    \"device\": {        \"os\": \"iPhone OS\",        \"ip\": \"204.28.127.10\",        \"js\": 1,        \"dpid\": \"32b1c496b84ea549191cdc65d32ade0c8d74c91b\",        \"osv\": \"4.2.1\",        \"loc\": \"37.7,-122.4\",        \"country\": \"USA\",        \"make\": \"Apple\",        \"carrier\": \"Wi-Fi\",        \"model\": \"iPhone\",        \"ua\": \"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7\"	},	\"tmax\": 200,	\"id\": \"31c64cf8-17bb-11e2-a4c5-1040f38b83e0\",	\"user\": {        \"uid\": \"32B1C496B84EA549191CDC65d32ADE0C8D74C91B\",        \"zip\": \"94110\",        \"gender\": \"F\",        \"yob\": 1990,    	\"country\": \"USA\",        \"keywords\": \"m_age:22,startups\"	}}";
		
		String responseData = sendPost("http://serve.qa.vdopia.com/adserver/rtb/adrequest/mopub", urlParameters);
		System.out.println("Value of ID is : " + processJSON.getValueStr(responseData, "id").toString());
		System.out.println("Value of nbr is : " + processJSON.getValueInt(responseData, "nbr").toString());
		System.out.println("Value of bidid is : " + processJSON.getValueStr(responseData, "bidid").toString());

	}
	
}
