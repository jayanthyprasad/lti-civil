package com.lti.civil;

/**
 * A video format.  Currently only a narrow range of video formats is supported:
 * 24- and 32-bit RGB.
 * @author Ken Larson
 *
 */
public interface VideoFormat
{
	/** The format type. */
	public int getFormatType();

	// format types
	/** Format type: 24-bit RGB. */
	public static final int RGB24 = 1;
	// TODO clarify endian/alignment
	/** Format type: 32-bit RGB. */
	public static final int RGB32 = 2;

	public static final int RGB565    = 3;
	public static final int RGB555    = 4;
	public static final int ARGB1555  = 5;
	public static final int ARGB32    = 6;
	public static final int UYVY      = 7;
	public static final int YUYV      = 8;
	public static final int YVYU      = 9;
	public static final int YUY2     = 10;
	public static final int YV12     = 11;
	public static final int I420     = 12;
	public static final int IYUV     = 13;
	public static final int NV12     = 14;
	public static final int Y411     = 15;
	public static final int YVU9     = 16;
	public static final int MJPG     = 17;
	public static final int DVSL     = 18;
	public static final int DVSD     = 19;
	public static final int DVHD     = 20;


	public static final int DATA_TYPE_BYTE_ARRAY = 1;
	public static final int DATA_TYPE_SHORT_ARRAY = 2;
	public static final int DATA_TYPE_INT_ARRAY = 4;

	/** The width of the image. */
	public int getWidth();
	/** The height of the image. */
	public int getHeight();
	/** Frames per second, FPS_UNKNOWN if unknown. */
	public float getFPS();

	public int getDataType();

	public static final float FPS_UNKNOWN = -1.f;
}
