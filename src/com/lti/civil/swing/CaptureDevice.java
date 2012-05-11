package com.lti.civil.swing;

import com.lti.civil.CaptureDeviceInfo;

public class CaptureDevice {

	private CaptureDeviceInfo info = null;

	private int output = -1;

	public CaptureDevice(CaptureDeviceInfo info) {
		this.info = info;
	}

	public CaptureDevice(CaptureDeviceInfo info, int output) {
		this.info = info;
		this.output = output;
	}

	public CaptureDeviceInfo getInfo() {
		return info;
	}

	public int getOutput() {
		if (output == -1) {
			return 0;
		}
		return output;
	}

	public String toString() {
	    if (output == -1) {
	    	return info.getDescription();
	    }
	    return info.getDescription() + " - " + info.getOutputNames()[output];
	}


}
