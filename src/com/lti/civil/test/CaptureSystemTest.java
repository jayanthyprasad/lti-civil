/*
 * Created on May 25, 2005
 */
package com.lti.civil.test;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.List;

import javax.imageio.ImageIO;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.Image;
import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;

/**
 * A test application to iterate through some capture devices and formats, and capture a few images.
 * @author Ken Larson
 */
public class CaptureSystemTest
{
	public static void main(String[] args) throws CaptureException
	{
		final CaptureSystemFactory factory = DefaultCaptureSystemFactorySingleton.instance();
		final CaptureSystem system = factory.createCaptureSystem();
		system.init();
		final List<CaptureDeviceInfo> list = system.getCaptureDeviceInfoList();
		
		System.out.println("Number of capture devices: " + list.size());
		
		for (int i = 0; i < list.size(); ++i)
		{
			final CaptureDeviceInfo info = list.get(i);

			System.out.println("Device ID " + i + ": " + info.getDeviceID());
			System.out.println("Description " + i + ": " + info.getDescription());


			final CaptureStream captureStream = system.openCaptureDeviceStream(info.getDeviceID());

			System.out.println("Current format " + videoFormatToString(captureStream.getVideoFormat()));

			captureStream.setObserver(new MyCaptureObserver(), 10);
			System.out.println("Available formats:");
			for (VideoFormat format : captureStream.enumVideoFormats())
			{
				System.out.println(" " + videoFormatToString(format));
			}

			final int MAX_FORMATS = 2;
			int count = 0;
			for (VideoFormat format : captureStream.enumVideoFormats())
			{
				if (count > MAX_FORMATS)
				{	System.out.println("Stopping after " + MAX_FORMATS + " formats.");	// could be a lot
					break;
				}
				System.out.println("Choosing format: " + videoFormatToString(format));
				captureStream.setVideoFormat(format);

				System.out.println("Capturing for 2 seconds...");
				captureStream.start();
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{	return;
				}
				captureStream.stop();
				++count;
			}
			System.out.println("disposing stream...");
			captureStream.dispose();

		}
		System.out.println("disposing system...");
		system.dispose();
		System.out.println("done.");


	}

	public static String videoFormatToString(VideoFormat f)
	{
		return "Type=" + formatTypeToString(f.getFormatType()) + " Width=" + f.getWidth() + " Height=" + f.getHeight() + " FPS=" + f.getFPS();
	}

	private static String formatTypeToString(int f)
	{
		switch (f)
		{
			case VideoFormat.RGB24:
				return "RGB24";
			case VideoFormat.RGB32:
				return "RGB32";
			default:
				return "" + f + " (unknown)";
		}
	}

}

class MyCaptureObserver implements CaptureObserver
{

	public void onError(CaptureStream sender, CaptureException e)
	{	System.err.println("onError " + sender);
		e.printStackTrace();
	}


	public void onNewImage(CaptureStream sender, Image image)
	{
		final BufferedImage bimg;
		try
		{
			final VideoFormat format = image.getFormat();
			System.out.println("onNewImage format=" + CaptureSystemTest.videoFormatToString(format) + " length=" + Array.getLength(image.getObject()));
			bimg = AWTImageConverter.toBufferedImage(image);
		}
		catch (Exception e)
		{	e.printStackTrace();
			return;
		}

//		 Encode as a JPEG
		try
		{
			final FileOutputStream fos = new FileOutputStream("out.jpg");
			ImageIO.write(bimg, "JPG", fos);
			fos.close();
		}
		catch (Exception e)
		{	e.printStackTrace();
		}
	}

}
