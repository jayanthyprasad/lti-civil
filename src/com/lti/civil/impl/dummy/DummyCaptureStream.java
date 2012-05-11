/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.dummy;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.VideoFormat;
import com.lti.civil.impl.common.BufferedImageImage;
import com.lti.civil.impl.common.VideoFormatImpl;
import com.lti.civil.utility.LoggerSingleton;
import com.lti.utils.synchronization.CloseableThread;

// code adapted from http://www.uk-dave.com/bytes/java/jmf-framegrab.shtml
// TODO: bad flicker
// TODO: do we get the same frame twice?
/**
 * Dummy capture: CaptureStream implementation.
 * @author Ken Larson
 */
public class DummyCaptureStream  implements CaptureStream
{
	private static final Logger logger = LoggerSingleton.logger;

	private static final int WIDTH = 320;
	private static final int HEIGHT = 240;
	private static final int FPS = 15;

	public List<VideoFormat> enumVideoFormats() throws CaptureException
	{
		final List<VideoFormat> result = new ArrayList<VideoFormat>();
		result.add(new VideoFormatImpl(VideoFormat.RGB24, WIDTH, HEIGHT, VideoFormat.FPS_UNKNOWN, VideoFormat.DATA_TYPE_BYTE_ARRAY));
		return result;
	}

	public VideoFormat getVideoFormat() throws CaptureException
	{	return enumVideoFormats().get(0);
	}

	public void setVideoFormat(VideoFormat f) throws CaptureException
	{	// TODO
	}


	private GrabberThread thread;
	private CaptureObserver observer;

	public DummyCaptureStream()
	{
	}

	public void setObserver(CaptureObserver observer, int numBuffersHint)
	{	this.observer = observer;
	}

	public void setObserver(CaptureObserver observer) 
	{
		this.setObserver(observer, NUM_BUFFERS_HINT_DEFAULT);
	}

	public void start() throws CaptureException
	{
        if (thread == null)
        {
	        thread = new GrabberThread();
	        thread.start();
        }


	}

	private final DateFormat df = new SimpleDateFormat("M/d/yyyy HH:mm:ss.SSS");
	int fileIndex;
	class GrabberThread extends CloseableThread
	{

		public GrabberThread()
		{
			super(Thread.currentThread().getThreadGroup(), "GrabberThread");
			setDaemon(true);

		}

		public void close()
		{
			setClosing();	// don't interrupt
		}

		public void run()
		{
			while (!isClosing())
			{

				final int width = WIDTH;
				final int height = HEIGHT;
		        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		        final Date d = new Date();
		        {
		        Graphics2D g = buffImg.createGraphics();
		        //g.drawImage(img, null, null);

		        // Overlay curent time on image
		        g.setColor(Color.RED);
		        g.setFont(new Font("Verdana", Font.BOLD, 16));

		        g.drawString(df.format(d), 10, 25);
		        }

		        if (observer != null)
		        	observer.onNewImage(DummyCaptureStream.this, new BufferedImageImage(buffImg, d.getTime()));

				try
				{
					Thread.sleep(1000 / FPS);
				} catch (InterruptedException e1)
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
