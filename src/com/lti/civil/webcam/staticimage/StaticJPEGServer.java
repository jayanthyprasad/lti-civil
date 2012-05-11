package com.lti.civil.webcam.staticimage;

/**
 * Simple web server application which serves up a single static image.
 * Useful for simulating a simple IP camera, for example.
 * @author Ken Larson
 *
 */
public class StaticJPEGServer
{

	public static void main(String[] list) throws Exception
	{
		new StaticJPEG_HTTPD(8090);

		while (true)
		{
			Thread.sleep(10000);
		}
	}
}