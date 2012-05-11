/*
 * Created on May 25, 2005
 */
package com.lti.civil;

/**
 * A factory to create a {@link CaptureSystem}.
 * @author Ken Larson
 */
public interface CaptureSystemFactory
{
	/** Create the capture system. */
	public CaptureSystem createCaptureSystem() throws CaptureException;
}
