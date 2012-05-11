package com.lti.utils;

import java.util.logging.Logger;

import com.lti.civil.utility.LoggerSingleton;


/**
 * 
 * @author ken
 *
 */
public final class OSUtils 
{
	private static final Logger logger = LoggerSingleton.logger;

	private OSUtils()
	{	super();
		logger.fine("OS: " + System.getProperty("os.name"));
	}
	
	public static final boolean isLinux()
	{
		return System.getProperty("os.name").equals("Linux");
	}
	
	public static final boolean isMacOSX()
	{
		return System.getProperty("os.name").equals("Mac OS X");
	}
	
	public static final boolean isWindows()
	{
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static final boolean isSolaris()
	{
		return System.getProperty("os.name").equals("SunOS");
	}

}
