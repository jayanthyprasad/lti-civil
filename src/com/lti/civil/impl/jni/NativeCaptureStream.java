/*
 * Created on May 25, 2005
 */
package com.lti.civil.impl.jni;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.VideoFormat;
import com.lti.civil.utility.LoggerSingleton;

/**
 *
 * @author Ken Larson
 */
public class NativeCaptureStream extends Peered implements CaptureStream
{
	private static final Logger logger = LoggerSingleton.logger;

	private NativeCaptureStreamThread thread;
	public NativeCaptureStream(long ptr)
	{
		super(ptr);

	}

	public synchronized native List<VideoFormat> enumVideoFormats() throws CaptureException;

	public synchronized native void setVideoFormat(VideoFormat f) throws CaptureException;

	public synchronized native VideoFormat getVideoFormat() throws CaptureException;

	public synchronized native void setObserver(CaptureObserver observer, int numBuffersHint);

	public void setObserver(CaptureObserver observer) 
	{
		this.setObserver(observer, NUM_BUFFERS_HINT_DEFAULT);
	}
	
	private boolean started;

	public synchronized void start() throws CaptureException
	{
		if (started)
			return;

		if (thread == null)
		{
			thread = new NativeCaptureStreamThread();
			thread.setName("NativeCaptureStreamThread " + getPeerPtr());
			thread.setDaemon(true);
			thread.start();	// TODO: when to stop?
		}

		nativeStart();

		started = true;
	}

	public synchronized void stop() throws CaptureException
	{
		if (!started)
			return;
		nativeStop();
		started = false;
	}

	public synchronized void dispose() throws CaptureException
	{	nativeDispose();
		if (thread != null)
		{	try
			{
				thread.join();
			} catch (InterruptedException e)
			{
				logger.log(Level.WARNING, "" + e, e);
			}
		}
	}

	private synchronized native void nativeStart() throws CaptureException;
	private synchronized native void nativeStop() throws CaptureException;
	private synchronized native void nativeDispose() throws CaptureException;
	/** This is called from a new thread.  This allows us to provide threading from Java, rather than implement it in C++. Thread should terminate on dispose.*/
	public native void threadMain();

	class NativeCaptureStreamThread extends Thread
	{
		public void run()
		{
			logger.fine("NativeCaptureStreamThread running");

			try
			{
				threadMain();
			}
			catch (Throwable t)
			{	logger.log(Level.SEVERE, "" + t, t);
			}
			logger.fine("NativeCaptureStreamThread exiting");

		}
	}
}
