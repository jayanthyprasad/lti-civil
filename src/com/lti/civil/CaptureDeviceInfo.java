/*
 * Created on May 25, 2005
 */
package com.lti.civil;

/**
 * Information about a capture device.
 * @author Ken Larson
 */
public interface CaptureDeviceInfo
{
	/** A machine-readable identifier which uniquely identifies the capture device. */
	public String getDeviceID();
	/** A human-readable description of the capture device. */
	public String getDescription();

	public String[] getOutputNames();

	public String[] getInputNames(int output);
}
