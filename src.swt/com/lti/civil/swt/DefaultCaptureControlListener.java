/*
 * Created on Jun 1, 2005
 */
package com.lti.civil.swt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.lti.civil.awt.AWTImageConverter;
import com.lti.civil.utility.LoggerSingleton;

/**
 * Saves snapshot to a file.
 * @author Ken Larson
 */
public class DefaultCaptureControlListener implements CaptureControlListener
{
	private static final Logger logger = LoggerSingleton.logger;

	private String outputPath = "out.jpg";
	
	public void onSnap(com.lti.civil.Image image)
	{
//		 Encode as a JPEG
		if (image == null)
			return;
		try
		{
			final FileOutputStream fos = new FileOutputStream(outputPath);
			ImageIO.write(AWTImageConverter.toBufferedImage(image), "JPG", fos);
			fos.close();
		}
		catch (IOException e)
		{	logger.log(Level.WARNING, "" + e, e);
		}		
	}

}
