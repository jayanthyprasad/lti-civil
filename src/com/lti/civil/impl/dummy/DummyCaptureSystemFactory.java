/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.dummy;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;

/**
 * Dummy capture: CaptureSystemFactory implementation.
 * @author Ken Larson
 */
public class DummyCaptureSystemFactory implements CaptureSystemFactory
{

	public CaptureSystem createCaptureSystem() throws CaptureException
	{
		return new DummyCaptureSystem();
	}
	
}
