/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.jmf;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;

/**
 * 
 * @author Ken Larson
 */
public class JMFCaptureSystemFactory implements CaptureSystemFactory
{

	public CaptureSystem createCaptureSystem() throws CaptureException
	{
		return new JMFCaptureSystem();
	}
	
}
