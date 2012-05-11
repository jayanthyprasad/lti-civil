/*
 * Created on May 25, 2005
 */
package com.lti.civil;

import java.util.List;

/**
 * A capture system, which represents an underlying native capture system, and access
 * to the capture devices and streams supported by that system.
 * @author Ken Larson
 */
public interface CaptureSystem
{
	/** Initialize the capture system.  Must be called prior to any other methods being called.  */
	public void init() throws CaptureException;
	/** Dispose the capture system.  No other methods may be called after disposal. */
	public void dispose() throws CaptureException;
	/**
	 * Get a list of all capture devices available.
	 */
	public List<CaptureDeviceInfo> getCaptureDeviceInfoList() throws CaptureException;
	/**
	 * Open a capture stream
	 * @param deviceId a valid deviceId from a CaptureDeviceInfo returned from getCaptureDeviceInfoList.
	 * @return the opened CaptureStream.
	 * @throws CaptureException
	 */
	public CaptureStream openCaptureDeviceStream(String deviceId) throws CaptureException;

	/**
	 * Open a specific output and input in a capture stream
	 * @param deviceId a valid deviceId from a CaptureDeviceInfo returned from getCaptureDeviceInfoList.
	 * @param output the index of the output to open
	 * @param input the index of the input to open
	 * @return the opened CaptureStream.
	 * @throws CaptureException
	 */
	public CaptureStream openCaptureDeviceStreamOutput(String deviceId, int output, int input) throws CaptureException;
}
