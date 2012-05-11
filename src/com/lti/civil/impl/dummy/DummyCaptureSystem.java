/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.dummy;

import java.util.ArrayList;
import java.util.List;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.impl.common.CaptureDeviceInfoImpl;

/**
 * Dummy capture: CaptureSystem implementation.
 * @author Ken Larson
 */
public class DummyCaptureSystem implements CaptureSystem
{
	public DummyCaptureSystem()
	{
		super();
	}
	public void init() throws CaptureException
	{
	}
	public void dispose() throws CaptureException
	{
	}


	public List<CaptureDeviceInfo> getCaptureDeviceInfoList() throws CaptureException
	{
        final List result = new ArrayList();
       	result.add(new CaptureDeviceInfoImpl("Dummy", "Dummy"));
		return result;
	}
	public CaptureStream openCaptureDeviceStream(final String deviceId) throws CaptureException
	{

		return new DummyCaptureStream();
	}
	public CaptureStream openCaptureDeviceStreamOutput(String deviceId,
			int output, int input) throws CaptureException {
		if (output != 0) {
			throw new CaptureException("Output index out of range", output);
		}
		if (input != 0) {
			throw new CaptureException("Input index out of range", input);
		}
		return new DummyCaptureStream();
	}

}
