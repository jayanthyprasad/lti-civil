package org.apache.log4j;

/** A quick hack because swtutils uses log4j, but lti-civil doesn't. */
public class Logger 
{
	public static Logger getLogger(String name)
	{	return new Logger();
	}
	
	public void error(Object s, Throwable t)
	{
		System.err.println("ERROR: " + s);
		t.printStackTrace();
	}
	
	public void warn(Object s, Throwable t)
	{
		System.err.println("WARN: " + s);
		t.printStackTrace();
	}
	
	public void debug(String s)
	{	// ignore
	}
}
