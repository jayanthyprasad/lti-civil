/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.jmf;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.format.VideoFormat;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.impl.common.CaptureDeviceInfoImpl;

/**
 * 
 * @author Ken Larson
 */
public class JMFCaptureSystem implements CaptureSystem
{
	private static final Logger logger = Logger.global;

	public JMFCaptureSystem()
	{
		
	}
	public void init() throws CaptureException
	{
	}
	public void dispose() throws CaptureException
	{
	}
	
	private Vector getVideoDevices()
	{
		final java.util.Vector vectorDevices = CaptureDeviceManager.getDeviceList(null);
		if (vectorDevices == null)
		{
			return new Vector();	// empty
			//throw new CaptureException("... error: media device list vector is null, program aborted", -1);
		}
		if (vectorDevices.size() == 0)
		{
			return new Vector();	// empty
		}

		
		final Vector vectorVideoDevices = new Vector ();
        for ( int i = 0;  i < vectorDevices.size();  i++ ) {
        	CaptureDeviceInfo infoCaptureDevice = (CaptureDeviceInfo) vectorDevices.elementAt ( i );
            Format[] arrFormats = infoCaptureDevice.getFormats ();
            for ( int j = 0;  j < arrFormats.length;  j++ ) {
                if ( arrFormats[j] instanceof VideoFormat ) {
                    vectorVideoDevices.addElement ( infoCaptureDevice );
                    break;
                }
            }
        }		
        return vectorVideoDevices;
	}
	
	public List<CaptureDeviceInfo> getCaptureDeviceInfoList() throws CaptureException
	{	
        
        final List<CaptureDeviceInfo> result = new ArrayList<CaptureDeviceInfo>();
        final Vector vectorVideoDevices = getVideoDevices();
		for (int i = 0; i < vectorVideoDevices.size(); i++)
		{
			final CaptureDeviceInfo deviceInfo = vectorVideoDevices.elementAt(i);
			String deviceInfoText = deviceInfo.getName();
			logger.fine("device " + i + ": " + deviceInfoText);
			// TODO: description.  TODO: migrate Native info.
			result.add(new CaptureDeviceInfoImpl(deviceInfoText, deviceInfoText));

		}
		return result;
	}
	public CaptureStream openCaptureDeviceStream(final String deviceId) throws CaptureException
	{	
		final Vector vectorVideoDevices = getVideoDevices();
		for (int i = 0; i < vectorVideoDevices.size(); i++)
		{
			// display device name
			final CaptureDeviceInfo deviceInfo = (CaptureDeviceInfo) vectorVideoDevices.elementAt(i);
			final String deviceInfoText = deviceInfo.getName();
			if (deviceInfoText.equals(deviceId))
			{
				return new JMFCaptureStream(deviceInfo);
			}
			

		}	
		throw new CaptureException("Unknown device " + deviceId, -1);
	}

}
