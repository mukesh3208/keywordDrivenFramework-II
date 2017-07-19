/**
 * Last Changes Done on 5 Mar, 2015 12:07:49 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UrlLibs 
{
	
	//public static String urlopen;
	
	
	
	public static String getUrlSource(String urlopen, String userAgent) 
	{
		// TODO Auto-generated method stub
		

		try {
			//String text = "http://serve.qa.vdopia.com/adserver/html5/inwapads/?sleepAfter=0;adFormat=banner;ak=0cc679d0b6fc17c2f7f7b03808a64238;version=1.0;expandable=required;cb=[timestamp]";
			URL url = new URL(urlopen);
			URLConnection conn = url.openConnection();
			// fake request coming from browser
			if(userAgent != null)
			{
				conn.setRequestProperty("User-Agent", userAgent);
				
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			//In in = new In(url);
			String f = null;
			while(in.readLine()!=null) {
				f = f + in.readLine();
			}
			in.close();	
			return f;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
}
