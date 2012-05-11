/*
 * Created on May 25, 2005
 */
package com.lti.civil;

import com.lti.civil.awt.AWTImageConverter;


/**
 * Represents an image.  Can be transformed into a BufferedImage using {@link AWTImageConverter}.
 * @author Ken Larson
 */
public interface Image
{
	/** Get the raw object for this image. */
	public Object getObject();
	/** Get the offset into the object for this image. */
	public int getOffset();
	/** Get the video format for this image. */
	public VideoFormat getFormat();
	/** Get the time when this image was acquired, in milliseconds, like System.currentTimeMillis. */
	public long getTimestamp();
}
