/*
 * Created on May 25, 2005
 */
package com.lti.civil.impl.jni;

import com.googlecode.vicovre.utils.nativeloader.NativeLoader;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;

/**
 *
 * @author Ken Larson
 */
public class NativeCaptureSystemFactory implements CaptureSystemFactory
{

	public CaptureSystem createCaptureSystem() throws CaptureException
	{
		try
		{
		    NativeLoader.loadLibrary(getClass(), "civil");
		}
		catch (UnsatisfiedLinkError e)
		{	throw new CaptureException(e);
		}

		return newCaptureSystemObj();
	}
	private static native CaptureSystem newCaptureSystemObj();

}
