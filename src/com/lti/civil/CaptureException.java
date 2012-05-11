/*
 * Created on May 25, 2005
 */
package com.lti.civil;

/**
 * A capture exception.  Contains an optional integer error code which is 
 * useful for exceptions which are thrown from native capture systems which
 * have defined error codes.
 * @author Ken Larson
 */
public class CaptureException extends Exception
{
	private int errorCode;
	
	public CaptureException()
	{
		super();
		
	}

	public CaptureException(String message, final int errorCode, Throwable cause)
	{
		super(message + ": " + errorCode, cause);
		this.errorCode = errorCode;
	}

	public CaptureException(String message, final int errorCode)
	{
		super(message + ": " + errorCode);
		this.errorCode = errorCode;
		
	}

	public CaptureException(Throwable cause)
	{
		super(cause);
		
	}

	public int getErrorCode()
	{
		return errorCode;
	}

}
