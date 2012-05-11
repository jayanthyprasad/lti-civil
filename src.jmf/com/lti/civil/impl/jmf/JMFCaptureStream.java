/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.jmf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.Manager;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.impl.common.BufferedImageImage;
import com.lti.utils.synchronization.CloseableThread;

// code adapted from http://www.uk-dave.com/bytes/java/jmf-framegrab.shtml
// TODO: bad flicker
// TODO: do we get the same frame twice?
/**
 * 
 * @author Ken Larson
 */
public class JMFCaptureStream  implements CaptureStream
{
	private static final Logger logger = Logger.global;

	private final CaptureDeviceInfo deviceInfo;
	private GrabberThread thread;
	private CaptureObserver observer;
	private Player player;
	
	public JMFCaptureStream(final CaptureDeviceInfo deviceInfo)
	{	this.deviceInfo = deviceInfo;
	}

	public void setObserver(CaptureObserver observer)
	{	this.observer = observer;
	}
	
	public void start() throws CaptureException
	{
		//Format format = deviceInfo.getFormats()[0];	// TODO: choose a format that we like

        
        try
		{
			player = Manager.createRealizedPlayer(deviceInfo.getLocator());
		} catch (Exception e)
		{	throw new CaptureException(e);
		}
        player.start();

        // Wait a few seconds for camera to initialise (otherwise img==null)
//        try
//		{
//			Thread.sleep(2500);
//		} catch (InterruptedException e)
//		{
//			return;
//		}

        // Grab a frame from the capture device
        FrameGrabbingControl frameGrabber = (FrameGrabbingControl)player.getControl("javax.media.control.FrameGrabbingControl");

        if (thread == null)
        {
	        thread = new GrabberThread(frameGrabber);
	        thread.start();
        }

		
	}
	
	int fileIndex;
	class GrabberThread extends CloseableThread
	{
		private final FrameGrabbingControl frameGrabber;
		
		public GrabberThread(FrameGrabbingControl frameGrabber)
		{
			super(Thread.currentThread().getThreadGroup(), "GrabberThread");
			this.frameGrabber = frameGrabber;
		}

		public void close()
		{
			setClosing();	// don't interrupt
		}

		public void run()
		{
			while (!isClosing())
			{
		        Buffer buf = frameGrabber.grabFrame();
	
		        // Convert frame to an buffered image so it can be processed and saved
		        java.awt.Image img = (new BufferToImage((VideoFormat)buf.getFormat()).createImage(buf));
		        if (img == null)	// happens if camera not ready.
		        {	try
					{
						Thread.sleep(100);
					} catch (InterruptedException e)
					{
						break;
					}
		        	continue;
		        }
		        BufferedImage buffImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		        
		        {
		        Graphics2D g = buffImg.createGraphics();
		        g.drawImage(img, null, null);
		
		        // Overlay curent time on image
//		        g.setColor(Color.RED);
//		        g.setFont(new Font("Verdana", Font.BOLD, 16));
//		        g.drawString((new Date()).toString(), 10, 25);
		        }

		        if (observer != null)
		        	observer.onNewImage(JMFCaptureStream.this, new BufferedImageImage(buffImg));
		        
		        try
		        {
		        	Thread.sleep(1);
		        }
		        catch (InterruptedException e)
				{
					break;
				}
		        
		        // Save image to disk as PNG
		        if (false)
		        {
			        try
					{
			        	String path = "webcam" + fileIndex++ + ".png";
						ImageIO.write(buffImg, "png", new File(path));
						logger.fine("Wrote " + path);
					} catch (IOException e)
					{
						throw new RuntimeException(e);
					}
		        }
			}
			if (player != null)
			{
		        player.close();
		        player.deallocate();
		        player = null;
			}
			
			setClosed();
		}

	}
	
	public void stop() throws CaptureException
	{
		// TODO: implement.

		
	}

	public void dispose() throws CaptureException
	{
		if (thread != null)
		{
//			try
//			{
				thread.close();
//				logger.fine("Waiting for GrabberThread to complete...");
//				thread.waitUntilClosed();
//			} catch (InterruptedException e)
//			{
//				logger.log(Level.WARNING, "" + e, e);
//				return;
//			}
		}
//		logger.fine("GrabberThread completed");
		
	}
}
