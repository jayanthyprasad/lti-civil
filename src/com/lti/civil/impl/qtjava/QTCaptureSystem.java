/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.qtjava;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import quicktime.QTException;
import quicktime.QTSession;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.impl.common.CaptureDeviceInfoImpl;
import com.lti.civil.utility.LoggerSingleton;

/**
 *
 * @author Ken Larson
 */
public class QTCaptureSystem implements CaptureSystem
{
	private static final Logger logger = LoggerSingleton.logger;

	public QTCaptureSystem()
	{
		super();
	}
	public void init() throws CaptureException
	{
		try
		{
			logger.fine("Initializing quicktime");
			QTSession.open();
		}
		catch (QTException e)
		{
			throw new CaptureException(e);
		}

	}
	public void dispose() throws CaptureException
	{
		QTSession.close();
	}



	public List<CaptureDeviceInfo> getCaptureDeviceInfoList() throws CaptureException
	{

        final List<CaptureDeviceInfo> result = new ArrayList<CaptureDeviceInfo>();
		// TODO: description.  TODO: migrate Native info.
		result.add(new CaptureDeviceInfoImpl("?", "?"));

		return result;
	}
	public CaptureStream openCaptureDeviceStream(final String deviceId) throws CaptureException
	{

		try
		{
			return new QTCaptureStream();
		}
		catch (QTException e)
		{
			throw new CaptureException(e);
		}

	}
	public CaptureStream openCaptureDeviceStreamOutput(String deviceId,
			int output, int input) throws CaptureException {
		if (output != 0) {
			throw new CaptureException("Output index out of range", output);
		}
		if (input != 0) {
			throw new CaptureException("Input index out of range", input);
		}
		try
		{
			return new QTCaptureStream();
		}
		catch (QTException e)
		{
			throw new CaptureException(e);
		}
	}

}
