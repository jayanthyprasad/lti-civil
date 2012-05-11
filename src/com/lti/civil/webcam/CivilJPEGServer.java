package com.lti.civil.webcam;

import java.util.List;
import java.util.logging.Logger;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.utility.LoggerSingleton;


/**
 * Simple application which runs an embedded web server and serves up images from a capture device.
 * @author Ken Larson
 *
 */
public class CivilJPEGServer
{
	private static final Logger logger = LoggerSingleton.logger;

	public static void main(String[] args) throws Exception
	{

		final CaptureSystemFactory factory = DefaultCaptureSystemFactorySingleton.instance();
		final CaptureSystem system = factory.createCaptureSystem();
		system.init();
		final List<CaptureDeviceInfo> list = system.getCaptureDeviceInfoList();
		for (int i = 0; i < list.size(); ++i)
		{
			final CaptureDeviceInfo info = list.get(i);

			logger.info("Device ID " + i + ": " + info.getDeviceID());
			logger.info("Description " + i + ": "
					+ info.getDescription());

			CaptureStream captureStream = system.openCaptureDeviceStream(info
					.getDeviceID());
			CivilJPEG_HTTPD.storeMostRecent_CaptureObserver = new StoreMostRecent_CaptureObserver();
			captureStream.setObserver(CivilJPEG_HTTPD.storeMostRecent_CaptureObserver, 2);
			captureStream.start();

			break;
			// captureStream.stop();
			// captureStream.dispose();

		}
		// system.dispose();

		logger.info("Starting CIVIL web server on port 8090...");

		new CivilJPEG_HTTPD(8090);

		while (true)
		{
			Thread.sleep(10000);
		}
	}
}