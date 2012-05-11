/*
 * Created on May 25, 2005
 */
package com.lti.civil.impl.common;

import com.lti.civil.CaptureDeviceInfo;

/**
 * Default implementation of {@link CaptureDeviceInfo}.
 * @author Ken Larson
 */
public class CaptureDeviceInfoImpl implements CaptureDeviceInfo
{
	private String deviceID;
	private String description;
	private String[] outputName = new String[0];
	private String[][] inputName = new String[0][0];
	public CaptureDeviceInfoImpl(String deviceID, String description)
	{
		super();

		this.deviceID = deviceID;
		this.description = description;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public String getDeviceID()
	{
		return deviceID;
	}
	public void setDeviceID(String deviceID)
	{
		this.deviceID = deviceID;
	}

	public void setNoOutputs(int noOutputs) {
		outputName = new String[noOutputs];
		inputName = new String[noOutputs][];
	}

	public void setOutputName(int output, String name) {
		if (output < outputName.length) {
		    outputName[output] = name;
		}
	}

	public void setNoInputs(int output, int noInputs) {
		if (output < inputName.length) {
			inputName[output] = new String[noInputs];
		}
	}

	public void setInputName(int output, int input, String name) {
		if ((output < inputName.length) && (input < inputName[output].length)) {
			inputName[output][input] = name;
		}
	}

	public String[] getOutputNames() {
		return outputName;
	}

	public String[] getInputNames(int output) {
		return inputName[output];
	}
}
