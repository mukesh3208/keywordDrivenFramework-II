/**
 * Last Changes Done on Jan 19, 2015 12:39:24 PM
 * Last Changes Done by Pankaj Katiyar
 * Change made in Vdopia_Automation
 * Purpose of change: 
 */
package vlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import projects.TestSuiteClass;import org.apache.log4j.Logger; 
import org.testng.Assert;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class ExecuteCommands 
{

	static Logger logger = Logger.getLogger(ExecuteCommands.class.getName());


	@SuppressWarnings("finally")
	public static String ExecuteCommand_ReturnsOutput(String inputCommand) throws IOException, InterruptedException 
	{	
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command: " + inputCommand + " is being executed:");
		StringBuffer output = new StringBuffer();
		try
		{
			Process p;
			p =  Runtime.getRuntime().exec(inputCommand);
			p.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while (reader.readLine()!= null) 
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Enterd In While Loop: " +reader.readLine());
				output.append(reader.readLine() + "\n");
			}

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command Output - " +output);
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteCommand_ReturnsOutput. ",e);
		}
		finally
		{
			return output.toString();
		}
	}


	//This is generic method to execute command array to return string
	@SuppressWarnings("finally")
	public static String ExecuteCommand_ReturnsOutput(String []inputCommand) throws IOException, InterruptedException 
	{	
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command: " + inputCommand.toString() + " is being executed:");
		StringBuffer output = new StringBuffer();
		try
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Reading Command Output....");
			ProcessBuilder builder = new ProcessBuilder( inputCommand);
			builder.redirectErrorStream(true);
			Process p = builder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while (reader.readLine()!= null) 
			{
				output.append(reader.readLine() + "\n");
			}

			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command Exit Status: "+p.exitValue());
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteCommand_ReturnsOutput. ", e);
		}
		finally
		{
			return output.toString();
		}
	}


	//This is generic method to execute command array and return exit status
	@SuppressWarnings("finally")
	public static int ExecuteCommand_ReturnsExitStatus(String []inputCommand) throws IOException, InterruptedException 
	{	
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command: " + inputCommand.toString() + " is being executed:");
		int exitStatus = 777777;
		StringBuffer output = new StringBuffer();
		try
		{
			ProcessBuilder builder = new ProcessBuilder( inputCommand);
			builder.redirectErrorStream(true);
			Process p = builder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while (reader.readLine()!= null) 
			{
				//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Entered In While Loop: " +reader.readLine());
				output.append(reader.readLine() + "\n");
			}

			exitStatus = p.exitValue();

			logger.debug(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command Exit Status: "+exitStatus);
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteCommand_ReturnsOutput. ", e);
		}
		finally
		{
			return exitStatus;
		}
	}


	@SuppressWarnings("finally")
	public static int ExecuteCommand_ReturnsExitStatus(String inputCommand) throws IOException, InterruptedException 
	{	
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command: " + inputCommand + " is being executed:");
		int exitStatus = 777777777;

		try
		{
			ProcessBuilder builder = new ProcessBuilder(inputCommand);
			builder.redirectErrorStream(true);
			Process p = builder.start();
			p.waitFor();

			exitStatus = p.exitValue();
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exit Status: " +exitStatus);
		}
		catch(Exception e)
		{
			exitStatus = 777777777;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteCommand_ReturnsExitStatus. ", e);
		}
		finally
		{
			return exitStatus;
		}
	}


	@SuppressWarnings("finally")
	public static String ExecuteMacCommand_ReturnsOutput(String inputCommand)  
	{	
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Executing Command: " + inputCommand);
		String line = "";
		String output = "";

		try
		{
			ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", inputCommand);
			builder.redirectErrorStream(true);
			Process p = builder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Reading Command Output....");

			while ((line = reader.readLine())!= null) 
			{
				output = output + line + "\n";
			}
		}
		catch(Exception e)
		{
			output = "";
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteMacCommand_ReturnsOutput. ", e);
		}
		finally
		{
			return output.trim();
		}
	}


	@SuppressWarnings("finally")
	public static int ExecuteMacCommand_ReturnsExitStatus(String inputCommand)  
	{	
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command: " + inputCommand + " is being executed:");		
		int output = 777777777;

		try
		{
			ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", inputCommand);
			builder.redirectErrorStream(true);
			Process p = builder.start();
			p.waitFor();

			output = p.exitValue();
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exit Status: " +output);
		}
		catch(Exception e)
		{			
			output = 007;
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteMacCommand_ReturnsExitStatus. ", e);
		}
		finally
		{
			return output;
		}
	}


	//This method create session with server using host name and user name and password
	public static Session createSessionWithPassword(String userName, String password, String host) throws JSchException, InterruptedException
	{
		JSch jsch = new JSch();
		Session session = null;

		try
		{
			session = jsch.getSession(userName, host);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(15000);

			if(session.isConnected())
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Session is established with host: " +host);
			}
			else
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ******* Session is not established with host: " +host+ " ************");
				Assert.fail("Session is not established with host: " +host);
			}
		}
		catch(JSchException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was a problem while establishing connection with host: " +host, e);
			Assert.fail("There was a problem while establishing connection with host: " +host, e);
		}

		return session;
	}


	//This method create session with server using host name and user name and private key
	@SuppressWarnings("unused")
	public static Session createSessionWithPrivateKey(String userName, String privateKeyLocation, String host)
	{
		JSch jsch = new JSch();
		Session session = null;
		int i = 0;
		try
		{
			//Use private key
			//jsch.addIdentity(privateKeyLocation);
			jsch.addIdentity(privateKeyLocation, "!nopassword!");
			session = jsch.getSession(userName, host);

			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(15000);

			if(session.isConnected())
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Session is established with host: " +host);
			}
			else
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : ******* Session is not established with host: " +host+ " ************");

				if(session == null && i < 5)
				{
					i++;
					/** retry to get session with serve machine */
					session = ExecuteCommands.createSessionWithPrivateKey(userName,privateKeyLocation,host);
				}
				if(session == null && i >= 5)
				{
					Assert.fail("Session is not established with host: " +host);
				}

			}
		}
		catch(JSchException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : There was a problem while establishing connection with host: " +host, e);
			if(session == null && i < 5)
			{
				i++;
				/** retry to get session with serve machine */
				session = ExecuteCommands.createSessionWithPrivateKey(userName,privateKeyLocation,host);
			}
			if(session == null && i >= 5)
			{
				Assert.fail("There was a problem while establishing connection with host: " +host, e);
			}
		}

		return session;
	}


	public static void EndSession(Session session) throws JSchException, InterruptedException
	{
		session.disconnect();
		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : session with " +session.getHost() + " is terminated");
	}


	public static void ExecuteCommandUsingJsch(String Command) throws JSchException, IOException, InterruptedException
	{
		JSch jsch = new JSch();

		Session session = jsch.getSession("pankaj", "qa.vdopia.com");
		session.setPassword("pankaj123");
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect(15000);

		if(session.isConnected())
		{
			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Session is Connected");
		}

		Thread.sleep(1000);

		Channel channel = session.openChannel("exec");

		((ChannelExec)channel).setCommand(Command);

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : COMMAND  : " + Command);

		channel.connect(5000);

		channel.setOutputStream(System.out);

		BufferedReader in=new BufferedReader(new InputStreamReader(channel.getInputStream()));

		String msg=null;
		while((msg=in.readLine())!=null)
		{
			logger.info(msg);
		} 

		channel.disconnect();
		session.disconnect();

		logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command Is executed: " );

	}


	public static void ExecuteCommandUsingJsch(Session session, String Command) 
	{
		try
		{
			Channel channel = session.openChannel("exec");

			((ChannelExec)channel).setCommand(Command);

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command is being executed : ");
			logger.info(Command);

			channel.connect(5000);

			logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exit Status: " +channel.getExitStatus() + " For Executed Command. ");

			channel.disconnect();
		}
		catch(JSchException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : JSchException handled by method: ExecuteTerminalCommandUsingJsch. ", e);
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteTerminalCommandUsingJsch. ", e);
		}
	}


	@SuppressWarnings("finally")
	public static String ExecuteCommandUsingJschReturnsOutput(Session session, String Command)
	{
		String msg="";
		String output = "";

		try
		{
			//Channel channel = session.openChannel("exec");
			//((ChannelExec)channel).setCommand(Command);

			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(Command);

			InputStream is = channel.getInputStream();

			//logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command is being executed : " + Command);

			channel.connect(5000);

			BufferedReader in=new BufferedReader(new InputStreamReader(is));

			while((msg=in.readLine())!=null)
			{
				output = output + msg + "\n";
				//logger.info(msg);
			} 

			in.close();
			is.close();
			channel.disconnect();
		}
		catch(JSchException e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : JSchException handled by method: ExecuteTerminalCommandUsingJschReturnsOutput. ", e);
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception handled by method: ExecuteTerminalCommandUsingJschReturnsOutput. ", e);
		}
		finally
		{
			return output;
		}
	}


	@SuppressWarnings("finally")
	/**
	 * Return the exit status of an executed command.
	 * @param session
	 * @param Command
	 * @return
	 */
	public Object ExecuteCommandUsingJschReturnsExitStatus(Session session, String Command)
	{
		Object exitStatus = null;
		try
		{
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(Command);

			InputStream is = channel.getInputStream();
			channel.connect(5000);

			exitStatus = channel.getExitStatus();
			is.close();
			channel.disconnect();
		}
		catch(Exception e)
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Exception: ", e);
		}
		finally
		{
			return exitStatus;
		}
	}


	public static boolean GetIphoneConnectionStatus()
	{
		boolean stat = false;
		Process process = null;
		try {
			if(!(System.getProperty("os.name").matches("^Windows.*")))
			{
				process = Runtime.getRuntime().exec("system_profiler SPUSBDataType");
			}
			else
			{
				logger.info(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Command yet to coded.");
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) 
			{
				if (line.toLowerCase().contains("iphone"))
				{
					stat= true;
					break;
				}
			}
		}
		catch (IOException e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : Error in running command ' system_profiler SPUSBDataType ' to get iphone status.Error is: ", e);
		}

		return stat;		
	}


	public static ArrayList<String> GetConnectedAndroidDeviceList(String strAndroidHome)
	{
		ArrayList<String> list = new ArrayList<String>();
		Process process = null;
		Matcher matcher;

		try 
		{
			//Granting permission to execute adb on mac machine
			if(!(System.getProperty("os.name").matches("^Windows.*")))
			{
				process = Runtime.getRuntime().exec("chmod 777 "+ strAndroidHome);
			}

			process = Runtime.getRuntime().exec(strAndroidHome + " devices");
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			Pattern pattern = Pattern.compile("^([a-zA-Z0-9\\-]+)(\\s+)(device)");

			while ((line = in.readLine()) != null) {
				if (line.matches(pattern.pattern())) {
					matcher = pattern.matcher(line);
					if (matcher.find())
					{
						list.add(matcher.group(1));
					}
				}
			}
		}
		catch (IOException e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : IOException Handled By Method: GetConnectedAndroidDeviceList ", e);
		}
		return list;
	}


	@SuppressWarnings("finally")
	public static boolean UninstallAppCommand_AndroidDevice(String strAndroidHome, String packageName, String androidDeviceID)
	{
		boolean flag = false;
		Process process = null;
		try 
		{
			//Granting permission to execute adb on mac machine
			if(!(System.getProperty("os.name").matches("^Windows.*")))
			{
				process = Runtime.getRuntime().exec("chmod 777 "+ strAndroidHome);
			}

			process = Runtime.getRuntime().exec(strAndroidHome + " -s "+ androidDeviceID +" uninstall " +packageName);
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			while ((line = in.readLine()) != null) 
			{
				if (line.toLowerCase().contains("success"))
				{
					flag = true;
				}
			}
		}
		catch (Exception e) 
		{
			logger.error(TestSuiteClass.UNIQ_EXECUTION_ID.get()+" : IOException Handled By Method: UninstallAppCommand_AndroidDevice ", e);
		}
		finally
		{
			return flag;
		}
	}


}
