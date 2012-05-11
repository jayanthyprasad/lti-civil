/*
 * Created on May 25, 2005
 */
package com.lti.civil;

import java.util.List;

/**
 * A stream of images coming from a capture device.
 * @author Ken Larson
 */
public interface CaptureStream
{
	/** A list of the video formats available from this stream. */
	public List<VideoFormat> enumVideoFormats() throws CaptureException;
	// TODO: clarify when video format may be set.  Must the stream be started?  must it be stopped?
	// for now, it may only be set before starting.
	/** Set the video format for this stream.  Must be a video format returned from enumVideoFormats.
	 * May not be called while the stream is started; must be called before start or after stop and before start.*/
	public void setVideoFormat(VideoFormat f) throws CaptureException;
	/**
	 * Get the video format that images from this stream will be in.  May be called before stream is started.
	 */
	public VideoFormat getVideoFormat() throws CaptureException;
	/** Start the stream.  Once started, images and errors will be given to the CaptureObserver (previously) passed in to setObserver. */
	public void start() throws CaptureException;
	/** Stop the stream.  Stream may be started again by calling start.*/
	public void stop() throws CaptureException;
	/** Dispose the stream.  No other methods may be called after disposal. */
	public void dispose() throws CaptureException;
	/** Set the observer to receive images and errors.  May only be called prior to start.
	 *  numBuffersHint is the minimum number of buffers that will be used by the stream (so can only store this many before they are overwritten).
	 */
	public void setObserver(CaptureObserver observer, int numBuffersHint);
	public static final int NUM_BUFFERS_HINT_DEFAULT = 2;
	public void setObserver(CaptureObserver observer);
}
